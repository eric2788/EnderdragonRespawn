package com.ericlam.mc.enderdragon.respawn.events;

import com.ericlam.mc.enderdragon.respawn.main.ConfigManager;
import com.ericlam.mc.enderdragon.respawn.main.EnderDragonRespawn;
import com.ericlam.mc.enderdragon.respawn.memories.EnderRestore;
import com.ericlam.mc.enderdragon.respawn.memories.MemoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EnderListeners implements Listener {

    private static HashSet<EnderRestore> preRestore = new HashSet<>();
    private static HashSet<Location> puts = new HashSet<>();
    private static HashSet<FakeBossBar> bossbars = new HashSet<>();

    public static HashSet<FakeBossBar> getBossbars() {
        return bossbars;
    }

    private static FakeBossBar findById(UUID uid){
        Optional<FakeBossBar> bossBarOptional = bossbars.stream().filter(fakeBossBar -> fakeBossBar.getEntityUID().equals(uid)).findFirst();
        return bossBarOptional.orElse(null);
    }

    private static FakeBossBar findById(EnderDragon dragon){
        Optional<FakeBossBar> bossBarOptional = bossbars.stream().filter(fakeBossBar -> fakeBossBar.getEntityUID().equals(dragon.getUniqueId())).findFirst();
        return bossBarOptional.orElse(null);
    }

    public static void restore(){
        for (Location put : puts) {
            if (put == null) continue;
            put.getBlock().setType(Material.AIR);
        }
        for (EnderRestore restore : preRestore) {
            MemoryManager.getInstance().restore(restore,false);
        }
    }

    public static void restore(World world){
        Optional<EnderRestore> restore = preRestore.stream().filter(restore1 -> restore1.getWorld().equals(world)).findFirst();
        if (!restore.isPresent()) return;
        MemoryManager.getInstance().restore(restore.get(),false);
        preRestore.removeIf(enderRestore -> enderRestore.getWorld().equals(world));
    }

    private EnderRestore findFromWorld(World world){
        for (EnderRestore restore : preRestore) {
            if (restore.getWorld().equals(world)) return restore;
        }
        return null;
    }

    @EventHandler
    public void onPlayerPut(BlockPlaceEvent e){
        Material material = e.getBlock().getType();
        if (material != Material.OBSIDIAN && material != Material.IRON_BARS) return;
        Location loc = e.getBlock().getLocation();
        if (!ConfigManager.getWorlds().contains(loc.getWorld())) return;
        puts.add(loc);
    }

    @EventHandler
    public void onCrystalExplode(EntityExplodeEvent e){
        if (e.getEntityType() != EntityType.ENDER_CRYSTAL) return;
        World world = e.getLocation().getWorld();
        if (!ConfigManager.getWorlds().contains(world)) return;
        EnderRestore restore = findFromWorld(world);
        if (restore == null) {
            restore = new EnderRestore(world);
            preRestore.add(restore);
        }
        for (Block block : e.blockList()) {
            Location loc = block.getLocation();
            if (block.getType() == Material.IRON_BARS) restore.addIronBars(loc);
        }
    }

    @EventHandler
    public void onCrystalBorken(EntityDamageEvent e){
        Location loc = e.getEntity().getLocation();
        if (!ConfigManager.getWorlds().contains(loc.getWorld())) return;
        if (e.getEntityType() != EntityType.ENDER_CRYSTAL) return;
        EnderRestore restore = findFromWorld(loc.getWorld());
        if (restore == null) {
            restore = new EnderRestore(loc.getWorld());
            preRestore.add(restore);
        }
        restore.addEnderCrystal(loc);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Material type = e.getBlock().getType();
        if (type != Material.OBSIDIAN && type != Material.IRON_BARS) return;
        Location loc = e.getBlock().getLocation();
        if (puts.contains(loc)) return;
        if (!ConfigManager.getWorlds().contains(loc.getWorld())) return;
        EnderRestore restore = findFromWorld(loc.getWorld());
        if (restore == null) {
            restore = new EnderRestore(loc.getWorld());
            preRestore.add(restore);
        }
        if (type == Material.OBSIDIAN) restore.addObsidian(loc);
        else restore.addIronBars(loc);

    }

    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent e){
        if (e.getEntityType() != EntityType.ENDER_DRAGON) return;
        Location loc = e.getEntity().getLocation();
        World world = loc.getWorld();
        if (!ConfigManager.getWorlds().contains(world)) return;
        EnderRestore restore = findFromWorld(world);
        if (restore == null) restore = new EnderRestore(world);
        restore.setEnderDragon(loc);
        FakeBossBar bossBar = findById(e.getEntity().getUniqueId());
        if (bossBar != null && bossBar.cancelled()) bossbars.remove(bossBar);
        MemoryManager.getInstance().addRestores(restore);
        preRestore.remove(restore);
        for (Location put : puts) {
            if (put == null) continue;
            if (!put.getWorld().equals(world)) continue;
            put.getBlock().setType(Material.AIR);
        }
        puts.removeIf(location -> location.getWorld().equals(world));
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof EnderDragon)) return;
        Player player = (Player)e.getDamager();
        if (EnderDragonRespawn.tester.contains(player)) e.setDamage(100);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        restoreBossBar(player);
    }

    private void restoreBossBar(Player player) {
        Bukkit.getScheduler().runTaskLater(EnderDragonRespawn.plugin,()->{
            World world = player.getWorld();
            if (world.getEnvironment() == World.Environment.THE_END){
                Set<LivingEntity> entities = world.getLivingEntities().stream().filter(livingEntity -> livingEntity instanceof EnderDragon).collect(Collectors.toSet());
                for (LivingEntity entity : entities) {
                    EnderDragon dragon = (EnderDragon) entity;
                    FakeBossBar bossBar = findById(dragon);
                    if (bossBar == null) continue;
                    bossBar.addPlayer(player);
                    bossBar.updateDragon(dragon);
                }
            }
        },40L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        Player player = e.getPlayer();
        World from = e.getFrom();

        Set<LivingEntity> livingEntities = from.getLivingEntities().stream().filter(livingEntity -> livingEntity instanceof EnderDragon).collect(Collectors.toSet());
        for (LivingEntity entity : livingEntities) {
            EnderDragon dragon = (EnderDragon) entity;
            FakeBossBar bossBar = findById(dragon);
            if (bossBar == null) continue;
            bossBar.removePlayer(player);
        }

        restoreBossBar(player);

    }



}
