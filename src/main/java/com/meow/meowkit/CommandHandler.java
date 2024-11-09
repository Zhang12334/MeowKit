package com.meow.meowkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements org.bukkit.command.CommandExecutor {
    private final MeowKit plugin;
    private final StorageManager storageManager;

    public CommandHandler(MeowKit plugin) {
        this.plugin = plugin;
        this.storageManager = new StorageManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mkit")) {
            if (args.length < 3) {
                sender.sendMessage("用法: /mkit add <cdk> <kitName> [permission] 或 /mkit manage <kitName> <action> <args>");
                return false;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("add") && !sender.hasPermission("mkit.admin")) {
                sender.sendMessage("你没有权限执行此命令！");
                return false;
            }

            if (subCommand.equalsIgnoreCase("add")) {
                String cdk = args[1];
                String kitName = args[2];
                String permission = args.length > 3 ? args[3] : "meowkit.receive.default";
                storageManager.addKit(kitName, cdk, permission);
                sender.sendMessage("礼包 " + kitName + " 已添加！");
                return true;
            } else if (subCommand.equalsIgnoreCase("manage")) {
                return storageManager.manageKit(sender, args);
            }
        }
        return false;
    }
}
