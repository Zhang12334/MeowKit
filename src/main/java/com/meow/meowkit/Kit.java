package com.meow.meowkit;

import java.util.ArrayList;
import java.util.List;

public class Kit {
    private String cdk;
    private final String permission;
    private final List<String> commands;

    public Kit(String cdk, String permission) {
        this.cdk = cdk;
        this.permission = permission;
        this.commands = new ArrayList<>();
    }

    public String getCdk() {
        return cdk;
    }

    public void setCdk(String cdk) {
        this.cdk = cdk;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void addCommand(String command) {
        commands.add(command);
    }

    public void removeCommand(int commandId) {
        if (commandId >= 0 && commandId < commands.size()) {
            commands.remove(commandId);
        }
    }
}
