package com.meow.meowkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeowKitTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数（子命令）补全
            if (sender.hasPermission("mkit.admin")) {
                completions.addAll(Arrays.asList("add", "manage"));
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("manage")) {
                // 第二个参数补全（kitName 或 cdk）
                completions.add("kitName"); // 这里可以根据实际的礼包名称提供补全
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            // 第三个参数补全（可选的 permission）
            completions.add("meowkit.receive.default");
            completions.add("meowkit.receive.all");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("manage")) {
            if (args[1].equalsIgnoreCase("cdk")) {
                // 为 manage cdk 命令提供补全（需要传入新的 cdk）
                completions.add("newCdk"); // 这里可以根据实际情况进行补全
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("manage")) {
            if (args[1].equalsIgnoreCase("command")) {
                if (args[2].equalsIgnoreCase("add")) {
                    // 为 manage command add 提供补全（指令）
                    completions.add("commandName");
                } else if (args[2].equalsIgnoreCase("remove")) {
                    // 为 manage command remove 提供补全（指令ID）
                    completions.add("commandId");
                }
            }
        }

        return completions;
    }
}
