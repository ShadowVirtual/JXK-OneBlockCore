package com.bgsoftware.superiorskyblock.module.evolvedskills.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.evolvedskills.EvolvedSkillsService;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdSpawner implements ISuperiorCommand {

    private final EvolvedSkillsService service;

    public CmdSpawner(EvolvedSkillsService service) {
        this.service = service;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("spawner");
    }

    @Override
    public String getPermission() {
        return "superior.island.spawner";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "spawner [status|harvest|upgrade]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Manage evolved spawners.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        if (superiorPlayer == null) {
            return;
        }

        service.executeSpawnerCommand(sender, superiorPlayer, args);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            return filter(args[1], Arrays.asList("status", "harvest", "upgrade"));
        }

        return Collections.emptyList();
    }

    private List<String> filter(String input, List<String> options) {
        String normalized = input == null ? "" : input.toLowerCase(java.util.Locale.ENGLISH);
        List<String> output = new java.util.ArrayList<>();
        for (String option : options) {
            if (option.startsWith(normalized)) {
                output.add(option);
            }
        }
        return output;
    }

}
