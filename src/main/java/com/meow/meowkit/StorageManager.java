package com.meow.meowkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private final MeowKit plugin;
    private Connection mysqlConnection;
    private File kitsFile;
    private YamlConfiguration kitsConfig;

    public StorageManager(MeowKit plugin) {
        this.plugin = plugin;
        this.initStorage();
    }

    // 初始化存储方式（MySQL 或 YML）
    private void initStorage() {
        String storageType = plugin.getConfig().getString("storage-type", "yml");

        if ("mysql".equalsIgnoreCase(storageType)) {
            this.initMySQL();
        } else {
            this.initYML();
        }
    }

    // 初始化 YML 存储
    private void initYML() {
        kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            try {
                kitsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    // 初始化 MySQL 连接
    private void initMySQL() {
        String host = plugin.getConfig().getString("mysql.host");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = plugin.getConfig().getString("mysql.database");
        String username = plugin.getConfig().getString("mysql.username");
        String password = plugin.getConfig().getString("mysql.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
        try {
            mysqlConnection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("MySQL 连接成功");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("无法连接到 MySQL 数据库，请检查配置！");
        }
    }

    // 添加礼包到存储中
    public void addKit(String kitName, String cdk, String permission) {
        Kit kit = new Kit(cdk, permission);
        if ("mysql".equalsIgnoreCase(plugin.getConfig().getString("storage-type", "yml"))) {
            this.saveToMySQL(kitName, cdk, permission);
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

    // 将礼包数据保存到 MySQL
    private void saveToMySQL(String kitName, String cdk, String permission) {
        try (Statement stmt = mysqlConnection.createStatement()) {
            String query = "INSERT INTO kits (name, cdk, permission) VALUES ('" + kitName + "', '" + cdk + "', '" + permission + "')";
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改礼包的 CDK
    public void changeKitCDK(String kitName, String newCdk) {
        if ("mysql".equalsIgnoreCase(plugin.getConfig().getString("storage-type", "yml"))) {
            this.updateMySQLCDK(kitName, newCdk);
        } else {
            kitsConfig.set("kits." + kitName + ".cdk", newCdk);
            try {
                kitsConfig.save(kitsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 更新 MySQL 中的 CDK
    private void updateMySQLCDK(String kitName, String newCdk) {
        try (Statement stmt = mysqlConnection.createStatement()) {
            String query = "UPDATE kits SET cdk = '" + newCdk + "' WHERE name = '" + kitName + "'";
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 添加礼包指令
    public void addKitCommand(String kitName, String command) {
        Kit kit = getKit(kitName);
        if (kit != null) {
            kit.addCommand(command);
            if ("mysql".equalsIgnoreCase(plugin.getConfig().getString("storage-type", "yml"))) {
                this.saveToMySQLCommand(kitName, command);
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

    // 保存指令到 MySQL
    private void saveToMySQLCommand(String kitName, String command) {
        try (PreparedStatement stmt = mysqlConnection.prepareStatement(
                "INSERT INTO kit_commands (kit_name, command) VALUES (?, ?)")) {
            stmt.setString(1, kitName);
            stmt.setString(2, command);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除礼包指令
    public void removeKitCommand(String kitName, int commandId) {
        Kit kit = getKit(kitName);
        if (kit != null) {
            kit.removeCommand(commandId);
            if ("mysql".equalsIgnoreCase(plugin.getConfig().getString("storage-type", "yml"))) {
                this.removeFromMySQLCommand(kitName, commandId);
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

    // 从 MySQL 删除指令
    private void removeFromMySQLCommand(String kitName, int commandId) {
        try (PreparedStatement stmt = mysqlConnection.prepareStatement(
                "DELETE FROM kit_commands WHERE kit_name = ? AND id = ?")) {
            stmt.setString(1, kitName);
            stmt.setInt(2, commandId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取礼包
    private Kit getKit(String kitName) {
        // 可以根据存储方式来决定是从 MySQL 还是 YML 中获取数据
        Kit kit = new Kit("", "meowkit.receive.default"); // 示例，返回一个默认值
        // 添加从 YML 或 MySQL 获取的具体逻辑
        return kit;
    }

    // 关闭 MySQL 连接
    public void close() {
        if (mysqlConnection != null) {
            try {
                mysqlConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
