package com.meow.meowkit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeowKit extends JavaPlugin {
    private FileConfiguration config;
    private Connection mysqlConnection;
    private final Map<String, Kit> kits = new HashMap<>();  // 缓存所有礼包数据
    private File kitsFile;
    private FileConfiguration kitsConfig;

    @Override
    public void onEnable() {
        // bstats
        int pluginId = 23851;
        Metrics metrics = new Metrics(this, pluginId);
        this.getCommand("mkit").setExecutor(this);
        this.getCommand("mkit").setTabCompleter(new MeowKitTabCompleter()); // 注册 Tab 完成器
        loadConfig();
        loadKitsConfig();
        initStorage();
        this.getCommand("mkit").setExecutor(this);
        getLogger().info("MeowKit 插件已启用！");
    }

    @Override
    public void onDisable() {
        if (mysqlConnection != null) {
            try {
                mysqlConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        getLogger().info("MeowKit 插件已禁用！");
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void loadKitsConfig() {
        kitsFile = new File(getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            try {
                kitsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    private void initStorage() {
        String storageType = config.getString("storage-type", "yml");

        if ("mysql".equalsIgnoreCase(storageType)) {
            getLogger().info("使用 MySQL 存储");
            initMySQL();
        } else {
            getLogger().info("使用本地 YML 存储");
        }
    }

    private void initMySQL() {
        String host = config.getString("mysql.host");
        int port = config.getInt("mysql.port", 3306);
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
        try {
            mysqlConnection = DriverManager.getConnection(url, username, password);
            getLogger().info("MySQL 连接成功");
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("无法连接到 MySQL 数据库，请检查配置！");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mkit")) {
            if (args.length < 3) {
                sender.sendMessage("用法: /mkit add <cdk> <kitName> [permission] 或 /mkit manage <kitName> <action> <args>");
                return false;
            }

            String subCommand = args[0];

            // 检查权限，只有拥有 mkit.admin 权限的人才能执行管理命令
            if (subCommand.equalsIgnoreCase("add") && !sender.hasPermission("mkit.admin")) {
                sender.sendMessage("你没有权限执行此命令！");
                return false;
            }

            if (subCommand.equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage("用法: /mkit add <cdk> <kitName> [permission]");
                    return false;
                }
                String cdk = args[1];  // CDK必须在前
                String kitName = args[2];
                String permission = args.length > 3 ? args[3] : "meowkit.receive.default";
                addKit(kitName, cdk, permission);
                sender.sendMessage("礼包 " + kitName + " 已添加！");
                return true;
            } else if (subCommand.equalsIgnoreCase("manage")) {
                if (args.length < 4) {
                    sender.sendMessage("用法: /mkit manage <kitName> <action> <args>");
                    return false;
                }
                String kitName = args[1];
                String action = args[2];

                // 管理命令
                if (action.equalsIgnoreCase("cdk") && args.length == 4) {
                    String newCdk = args[3];
                    changeKitCDK(kitName, newCdk);
                    sender.sendMessage("礼包 " + kitName + " 的 CDK 已更改为 " + newCdk);
                } else if (action.equalsIgnoreCase("command")) {
                    if (args.length < 5) {
                        sender.sendMessage("用法: /mkit manage <kitName> command <add/remove> <command|command_id>");
                        return false;
                    }
                    String commandAction = args[3];
                    if (commandAction.equalsIgnoreCase("add")) {
                        String commandContent = String.join(" ", args).substring(4); // 取从第5个参数开始的内容作为指令
                        addKitCommand(kitName, commandContent);
                        sender.sendMessage("指令已添加到礼包 " + kitName);
                    } else if (commandAction.equalsIgnoreCase("remove")) {
                        int commandId = Integer.parseInt(args[4]);
                        removeKitCommand(kitName, commandId);
                        sender.sendMessage("指令ID " + commandId + " 已从礼包 " + kitName + " 中删除");
                    } else {
                        sender.sendMessage("无效的管理指令！");
                    }
                } else {
                    sender.sendMessage("未知的子指令：" + action);
                }
                return true;
            }
        }
        return false;
    }

    private void addKit(String kitName, String cdk, String permission) {
        Kit kit = new Kit(cdk, permission);
        kits.put(kitName, kit);

        if ("mysql".equalsIgnoreCase(config.getString("storage-type", "yml"))) {
            try (Statement stmt = mysqlConnection.createStatement()) {
                String query = "INSERT INTO kits (name, cdk, permission) VALUES ('" + kitName + "', '" + cdk + "', '" + permission + "')";
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            kitsConfig.set("kits." + kitName + ".cdk", cdk);
            kitsConfig.set("kits." + kitName + ".permission", permission);
            try {
                kitsConfig.save(kitsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeKitCDK(String kitName, String newCdk) {
        Kit kit = kits.get(kitName);
        if (kit != null) {
            kit.setCdk(newCdk);

            if ("mysql".equalsIgnoreCase(config.getString("storage-type", "yml"))) {
                try (Statement stmt = mysqlConnection.createStatement()) {
                    String query = "UPDATE kits SET cdk = '" + newCdk + "' WHERE name = '" + kitName + "'";
                    stmt.executeUpdate(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                kitsConfig.set("kits." + kitName + ".cdk", newCdk);
                try {
                    kitsConfig.save(kitsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addKitCommand(String kitName, String commandContent) {
        Kit kit = kits.get(kitName);
        if (kit != null) {
            kit.addCommand(commandContent);
            if ("mysql".equalsIgnoreCase(config.getString("storage-type", "yml"))) {
                try (Statement stmt = mysqlConnection.createStatement()) {
                    String query = "UPDATE kits SET command = '" + commandContent + "' WHERE name = '" + kitName + "'";
                    stmt.executeUpdate(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                kitsConfig.set("kits." + kitName + ".commands", kit.getCommands());
                try {
                    kitsConfig.save(kitsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void removeKitCommand(String kitName, int commandId) {
        Kit kit = kits.get(kitName);
        if (kit != null) {
            kit.removeCommand(commandId);
            if ("mysql".equalsIgnoreCase(config.getString("storage-type", "yml"))) {
                try (Statement stmt = mysqlConnection.createStatement()) {
                    String query = "UPDATE kits SET command = '" + kit.getCommands() + "' WHERE name = '" + kitName + "'";
                    stmt.executeUpdate(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                kitsConfig.set("kits." + kitName + ".commands", kit.getCommands());
                try {
                    kitsConfig.save(kitsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Kit {
        private String cdk;
        private final String permission;
        private final List<String> commands = new ArrayList<>();

        public Kit(String cdk, String permission) {
            this.cdk = cdk;
            this.permission = permission;
        }

        public void setCdk(String cdk) {
            this.cdk = cdk;
        }

        public void addCommand(String command) {
            commands.add(command);
        }

        public void removeCommand(int commandId) {
            if (commandId >= 0 && commandId < commands.size()) {
                commands.remove(commandId);
            }
        }

        public List<String> getCommands() {
            return commands;
        }

        public String getCdk() {
            return cdk;
        }

        public String getPermission() {
            return permission;
        }
    }
}
