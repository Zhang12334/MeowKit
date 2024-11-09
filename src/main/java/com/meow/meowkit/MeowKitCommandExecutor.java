package com.meow.meowkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeowKitCommandExecutor implements TabCompleter {

    // 处理 tab 补全
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("add", "manage", "list"));
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("manage")) {
                // 在 "manage" 子命令下补全 kitName
                suggestions.add("<kitName>");
            } else if (args[0].equalsIgnoreCase("add")) {
                // 在 "add" 子命令下补全 cdk
                suggestions.add("<cdk>");
            }
        }

        return suggestions;
    }

    // 处理命令执行
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length != 2) {
                sender.sendMessage("用法: /mkit add <cdk>");
                return false;
            }
            String cdk = args[1];
            // 在这里添加礼包添加的逻辑
            sender.sendMessage("CDK " + cdk + " 已添加！");
            return true;
        }

        if (args[0].equalsIgnoreCase("manage")) {
            if (args.length != 2) {
                sender.sendMessage("用法: /mkit manage <kitName>");
                return false;
            }
            String kitName = args[1];
            // 这里可以执行管理礼包的操作
            sender.sendMessage("管理礼包 " + kitName);
            return true;
        }

        return false;
    }
}
