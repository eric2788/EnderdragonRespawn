package com.ericlam.mc.enderdragon.respawn.main;

import com.ericlam.mc.enderdragon.respawn.events.EnderListeners;
import com.ericlam.mc.enderdragon.respawn.memories.EnderRestore;
import com.ericlam.mc.enderdragon.respawn.memories.MemoryManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

public class EnderDragonRespawn extends JavaPlugin implements CommandExecutor {
    public static Plugin plugin;
    public static HashSet<Player> tester = new HashSet<>();
    @Override
    public void onEnable() {
        plugin = this;
        new ConfigManager(this);
        try {
            MemoryManager.getInstance().setup(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new EnderListeners(),this);
        getCommand("er").setExecutor(this);
    }

    @Override
    public void onDisable() {
        MemoryManager.getInstance().saveAllFile();
        EnderListeners.restore();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("er.restart")){
            sender.sendMessage("§5終界龍定時重生 插件 §7- §c作者: §e"+getDescription().getAuthors()+" §b版本: §a"+getDescription().getVersion());
            return false;
        }
        if (args.length < 1){
            sender.sendMessage("§c/er restart <世界> - 即時恢復終界龍");
            sender.sendMessage("§c/er list - 查看正在恢復中的世界");
            sender.sendMessage("§c/er tester - 啟用測試者模式，終界龍傷害 -100");
            return false;
        }
        if (args[0].equalsIgnoreCase("tester")){
            if (!(sender instanceof Player)){
                sender.sendMessage("§c不是玩家。");
                return false;
            }
            Player player = (Player) sender;
            boolean contain = tester.contains(player);
            if (contain) tester.remove(player);
            else tester.add(player);
            player.sendMessage("§a已把你 "+(contain?"移除":"添加")+" 到測試者列表。");
            return true;
        }
        if (args[0].equalsIgnoreCase("list")){
            String[] msgs = MemoryManager.getInstance().getRestoreList();
            sender.sendMessage("§e以下世界正在定時恢復中: ");
            sender.sendMessage(msgs);
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("restart")){
            sender.sendMessage("§c/er restart <世界> - 即時恢復終界龍");
            return false;
        }
        World world = Bukkit.getWorld(args[1]);
        if (world == null || world.getEnvironment() != World.Environment.THE_END){
            sender.sendMessage("§c找不到此世界或此世界並不是終界!");
            return false;
        }
        Optional<EnderRestore> restoreOptional = MemoryManager.getInstance().findFormWorld(world);
        if (!restoreOptional.isPresent()){
            sender.sendMessage("§c此世界並沒有正在進行的定時重生。");
            return false;
        }
        EnderRestore restore = restoreOptional.get();
        EnderListeners.restore(restore.getWorld());
        MemoryManager.getInstance().restore(restore,true);
        sender.sendMessage("§a即時恢復成功。");
        return true;
    }
}
