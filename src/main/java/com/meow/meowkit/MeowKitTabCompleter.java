package com.meow.meowkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeowKitTabCompleter implements TabCompleter {

    // 定义常量，存储可补全的命令
    private static final List<String> ADD_COMMANDS = Arrays.asList("cdk", "kitName");
    private static final List<String> MANAGE_COMMANDS = Arrays.asList("kitName", "cdk", "command", "list");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        // 不再检查sender是否为玩家，直接根据命令参数进行补全
        if (args.length == 1) {
            // 处理第一个参数，添加 "add" 和 "manage" 命令
            completions.addAll(Arrays.asList("add", "manage"));
        } else if (args.length == 2) {
            // 处理第二个参数，根据第一个参数决定补全内容
            switch (args[0].toLowerCase()) {
                case "add":
                    completions.addAll(ADD_COMMANDS); // "add" 命令相关补全项
                    break;
                case "manage":
                    completions.addAll(MANAGE_COMMANDS); // "manage" 命令相关补全项
                    break;
            }
        } else if (args.length == 3) {
            // 处理第三个参数，根据第一个和第二个参数决定补全内容
            switch (args[0].toLowerCase()) {
                case "add":
                    completions.add("kitName"); // "add" 命令下，第三个参数是 "kitName"
                    break;
                case "manage":
                    if ("cdk".equalsIgnoreCase(args[1])) {
                        completions.add("newCdk"); // "manage" 命令下，第二个参数为 "cdk" 时，补全 "newCdk"
                    } else if ("command".equalsIgnoreCase(args[1])) {
                        completions.addAll(Arrays.asList("add", "remove")); // "manage" 命令下，第二个参数为 "command" 时，补全 "add" 和 "remove"
                    }
                    break;
            }
        } else if (args.length == 4) {
            // 处理第四个参数，根据前面几个参数决定补全内容
            if ("add".equalsIgnoreCase(args[0])) {
                completions.addAll(Arrays.asList("meowkit.receive.default", "meowkit.receive.all")); // "add" 命令下，补全权限参数
            } else if ("manage".equalsIgnoreCase(args[1])) {
                switch (args[2].toLowerCase()) {
                    case "cdk":
                        completions.add("newCdk"); // "manage" 命令下，第三个参数为 "cdk" 时，补全 "newCdk"
                        break;
                    case "command":
                        if ("add".equalsIgnoreCase(args[3])) {
                            completions.add("command"); // "manage" 命令下，第三个参数为 "command"，第四个参数为 "add" 时，补全 "command"
                        } else if ("remove".equalsIgnoreCase(args[3])) {
                            completions.add("commandId"); // "manage" 命令下，第三个参数为 "command"，第四个参数为 "remove" 时，补全 "commandId"
                        }
                        break;
                    case "list":
                        // "manage" 命令下，第三个参数为 "list" 时，不需要补全项
                        break;
                }
            }
        }

        return completions;
    }
}
