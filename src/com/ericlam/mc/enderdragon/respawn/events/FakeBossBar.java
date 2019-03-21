package com.ericlam.mc.enderdragon.respawn.events;

import com.ericlam.mc.enderdragon.respawn.main.ConfigManager;
import com.ericlam.mc.enderdragon.respawn.main.EnderDragonRespawn;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class FakeBossBar {
    private EnderDragon enderDragon;
    private BossBar bossbar;
    private BukkitTask runnable;

    public FakeBossBar(World world, EnderDragon enderDragon) {
        this.enderDragon = enderDragon;
        this.bossbar = Bukkit.createBossBar(ConfigManager.getBossbarTitle().replace("<health>",Math.rint(this.enderDragon.getHealth())+""), BarColor.PINK, BarStyle.SOLID, BarFlag.values());
        world.getPlayers().forEach(this.bossbar::addPlayer);
        this.launch();
    }

    public UUID getEntityUID(){
        return enderDragon.getUniqueId();
    }

    public void removePlayer(Player player){
        this.bossbar.removePlayer(player);
    }

    public void addPlayer(Player player){
        this.bossbar.addPlayer(player);
    }

    public void updateDragon(EnderDragon dragon){
        this.enderDragon = dragon;
    }

    public boolean cancelled(){
        return runnable.isCancelled();
    }

    private void launch(){
        if (runnable != null) return;
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (bossbar.getProgress() > 0){
                    double max = enderDragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                    double hp = enderDragon.getHealth() / max;
                    bossbar.setProgress(hp < 0 ? 0 : hp);
                    bossbar.setTitle(ConfigManager.getBossbarTitle().replace("<health>",Math.rint(enderDragon.getHealth())+""));
                }else{
                    bossbar.removeAll();
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(EnderDragonRespawn.plugin,0L,1L);
    }
}
