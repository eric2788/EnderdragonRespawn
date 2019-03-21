package com.ericlam.mc.enderdragon.respawn.memories;

import com.ericlam.mc.enderdragon.respawn.events.EnderListeners;
import com.ericlam.mc.enderdragon.respawn.main.ConfigManager;
import com.ericlam.mc.enderdragon.respawn.main.EnderDragonRespawn;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;

public class SchedulerTask {
    private static SchedulerTask schedulerTask;

    public static SchedulerTask getInstance() {
        if (schedulerTask == null) schedulerTask = new SchedulerTask();
        return schedulerTask;
    }

    private HashMap<World,Integer> tasks = new HashMap<>();

    public void startCountdown(EnderRestore restore){
        int task = Bukkit.getScheduler().scheduleSyncDelayedTask(EnderDragonRespawn.plugin,()->{
            EnderListeners.restore(restore.getWorld());
            MemoryManager.getInstance().restore(restore,true);
            tasks.remove(restore.getWorld());
        }, ConfigManager.getHours() * 3600 * 20L);
        tasks.put(restore.getWorld(),task);
    }

    public void stop(World world){
        if (!tasks.containsKey(world)) return;
        Bukkit.getScheduler().cancelTask(tasks.get(world));
    }
}
