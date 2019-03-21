package com.ericlam.mc.enderdragon.respawn.memories;

import com.ericlam.mc.enderdragon.respawn.events.EnderListeners;
import com.ericlam.mc.enderdragon.respawn.events.FakeBossBar;
import com.ericlam.mc.enderdragon.respawn.main.ConfigManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemoryManager {
    private static MemoryManager memoryManager;
    private Plugin plugin;
    private File restoreFolder;
    private ArrayList<EnderRestore> restores = new ArrayList<>();
    public static MemoryManager getInstance() {
        if (memoryManager == null) memoryManager = new MemoryManager();
        return memoryManager;
    }

    public Optional<EnderRestore> findFormWorld(World world){
        return restores.stream().filter(restore -> restore.getWorld().equals(world)).findFirst();
    }

    public String[] getRestoreList(){
        List<String> strings = new ArrayList<>();
        restores.forEach(restore -> strings.add("§7- §f"+restore.getWorld().getName()));
        return strings.toArray(new String[0]);
    }

    public void setup(Plugin plugin) throws IOException {
        this.plugin = plugin;
        restoreFolder = new File(plugin.getDataFolder(),"RestoreData");
        if (!restoreFolder.exists()) FileUtils.forceMkdir(restoreFolder);
        File[] files = restoreFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!FilenameUtils.getExtension(file.getPath()).equals("yml")) {
                FileUtils.forceDelete(file);
                continue;
            }
            FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
            World world = Bukkit.getWorld(yml.getString("world"));
            if (world == null) continue;
            Location enderLoc = (Location)yml.get("ender-dragon");
            ConfigurationSection crystals = yml.getConfigurationSection("ender-crystals");
            ConfigurationSection obsidian = yml.getConfigurationSection("obsidians");
            ConfigurationSection ironBar = yml.getConfigurationSection("iron-bars");
            List<Location> crystalsLoc = new ArrayList<>();
            List<Location> obsidianLoc = new ArrayList<>();
            List<Location> ironBarLoc = new ArrayList<>();
            if (crystals != null){
                for (String key : crystals.getKeys(false)) {
                    crystalsLoc.add((Location)crystals.get(key));
                }
            }
            if (obsidian != null){
                for (String key : obsidian.getKeys(false)) {
                    obsidianLoc.add((Location)obsidian.get(key));
                }
            }
            if (ironBar != null){
                for (String key : ironBar.getKeys(false)) {
                    ironBarLoc.add((Location)ironBar.get(key));
                }
            }
            if (enderLoc == null) continue;
            EnderRestore restore = new EnderRestore(obsidianLoc,crystalsLoc,ironBarLoc,enderLoc,world);
            if (ConfigManager.isInstantAfterRestart()){
                plugin.getLogger().info("restoring EnderDragon at "+restore.getWorld().getName());
                restore(restore,false);
            }else{
                addRestores(restore);
            }
            FileUtils.forceDelete(file);
        }
    }

    public void addRestores(EnderRestore restore){
        this.restores.add(restore);
        SchedulerTask.getInstance().startCountdown(restore);
    }

    private void removeRestores(EnderRestore restore){
        this.restores.remove(restore);
    }

    public void saveAllFile(){
        for (EnderRestore restore : restores) {
            try {
                saveFile(restore);
            } catch (IOException e) {
                plugin.getLogger().info("Cannot save "+restore.getWorld().getName()+" ! skipping");
            }
        }
    }

    private void saveFile(EnderRestore restore) throws IOException {
        File save = new File(restoreFolder,restore.getWorld().getName()+".yml");
        if (!save.exists()) save.createNewFile();
        FileConfiguration data = YamlConfiguration.loadConfiguration(save);
        data.set("world",restore.getWorld().getName());
        data.set("ender-dragon",restore.getEnderDragon());
        for (int i = 0; i < restore.getEnderCrystal().size(); i++) {
            data.set("ender-crystals."+i,restore.getEnderCrystal().get(i));
        }
        for (int i = 0; i < restore.getObdsidian().size(); i++) {
            data.set("obsidians."+i,restore.getObdsidian().get(i));
        }
        for (int i = 0; i < restore.getIronBars().size(); i++) {
            data.set("iron-bars."+i,restore.getIronBars().get(i));
        }
        data.save(save);
    }

    public void restore(EnderRestore restore, boolean fakebar) {
        World world = restore.getWorld();
        for (Location location : restore.getObdsidian()) {
            if (location == null) continue;
            Block block = location.getBlock();
            if (block == null) continue;
            if (block.getType() == Material.OBSIDIAN) continue;
            block.setType(Material.OBSIDIAN);
        }
        for (Location location : restore.getEnderCrystal()) {
            if (location == null) continue;
            Block block = location.getBlock();
            if (block != null && block.getType() != Material.AIR) block.setType(Material.AIR);
            world.spawnEntity(location, EntityType.ENDER_CRYSTAL);
        }
        for (Location location : restore.getIronBars()) {
            if (location == null) continue;
            Block block = location.getBlock();
            if (block == null) continue;
            if (block.getType() == Material.IRON_BARS) continue;
            block.setType(Material.IRON_BARS);
            block.getState().update(true);
        }
        Location enderLoc = restore.getEnderDragon();
        if (enderLoc == null) return;
        if (world.getLivingEntities().stream().anyMatch(livingEntity -> livingEntity.getType() == EntityType.ENDER_DRAGON)) return;
        EnderDragon dragon = (EnderDragon)world.spawnEntity(enderLoc,EntityType.ENDER_DRAGON);
        dragon.setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);
        if (fakebar) EnderListeners.getBossbars().add(new FakeBossBar(world,dragon));
        removeRestores(restore);
        SchedulerTask.getInstance().stop(restore.getWorld());
    }

}
