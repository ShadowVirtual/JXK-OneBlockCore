package com.bgsoftware.superiorskyblock.module.evolvedskills.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.evolvedskills.EvolvedSkillsService;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdTreasure implements ISuperiorCommand {

    private final EvolvedSkillsService service;

    public CmdTreasure(EvolvedSkillsService service) {
        this.service = service;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("treasure");
    }

    @Override
    public String getPermission() {
        return "superior.island.treasure";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "treasure open <pool>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Open a configured treasure pool.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 3;
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

        service.executeTreasureCommand(sender, superiorPlayer, args);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            return filter(args[1], Collections.singletonList("open"));
        }

        if (args.length == 3 && "open".equalsIgnoreCase(args[1])) {
            return filter(args[2], Arrays.asList("basic", "rare", "epic"));
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
