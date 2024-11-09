package com.meow.meowkit;

import org.bukkit.plugin.java.JavaPlugin;

public class MeowKit extends JavaPlugin {
    private CommandHandler commandHandler;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        // bstats
        int pluginId = 23851;
        Metrics metrics = new Metrics(this, pluginId);
        saveDefaultConfig();
        commandHandler = new CommandHandler(this);
        storageManager = new StorageManager(this);

        getCommand("mkit").setExecutor(commandHandler);
        getLogger().info("MeowKit 插件已启用！");
    }

    @Override
    public void onDisable() {
        storageManager.close();
        getLogger().info("MeowKit 插件已禁用！");
    }
}
