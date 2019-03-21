package com.ericlam.mc.enderdragon.respawn.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static int hours;
    private static boolean instantAfterRestart;
    private static String bossbarTitle = "終界龍";
    private static List<World> worlds = new ArrayList<>();
    public ConfigManager(Plugin plugin){
        File configFile = new File(plugin.getDataFolder(),"config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml",true);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        for (String string : config.getStringList("ender-worlds")) {
            World world = Bukkit.getWorld(string);
            if (world == null) continue;
            if (world.getEnvironment() == World.Environment.THE_END) worlds.add(world);
        }
        hours = config.getInt("restore-hours");
        instantAfterRestart = config.getBoolean("restore-instantly-after-restart");
        bossbarTitle = ChatColor.translateAlternateColorCodes('&',config.getString("bossbar-title"));
    }

    public static List<World> getWorlds() {
        return worlds;
    }

    public static int getHours() {
        return hours;
    }

    public static boolean isInstantAfterRestart() {
        return instantAfterRestart;
    }

    public static String getBossbarTitle() {
        return bossbarTitle;
    }
}
