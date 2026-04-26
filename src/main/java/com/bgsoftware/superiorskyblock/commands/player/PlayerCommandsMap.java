package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.CommandsMap;

public class PlayerCommandsMap extends CommandsMap {

    public PlayerCommandsMap(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadDefaultCommands() {
        clearCommands();

        registerCommand(new CmdAccept());
        registerCommand(new CmdAdmin());
        registerCommand(new CmdBan());
        registerCommand(new CmdBans());
        registerCommand(new CmdBorder());
        registerCommand(new CmdClose());
        registerCommand(new CmdCounts());
        registerCommand(new CmdCreate());
        registerCommand(new CmdDemote());
        registerCommand(new CmdDisband());
        registerCommand(new CmdInvite());
        registerCommand(new CmdKick());
        registerCommand(new CmdLeave());
        registerCommand(new CmdMembers());
        registerCommand(new CmdOpen());
        registerCommand(new CmdPromote());
        registerCommand(new CmdSettings());
        registerCommand(new CmdTop());
        registerCommand(new CmdTransfer());
    }

}
