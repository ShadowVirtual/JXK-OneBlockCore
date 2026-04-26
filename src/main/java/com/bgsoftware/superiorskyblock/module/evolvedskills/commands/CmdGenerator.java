package com.bgsoftware.superiorskyblock.module.evolvedskills.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.evolvedskills.EvolvedSkillsService;

public class CmdGenerator implements ISuperiorCommand {

    private final EvolvedSkillsService service;

    public CmdGenerator(EvolvedSkillsService service) {
        this.service = service;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("generator");
    }

    @Override
    public String getPermission() {
        return "superior.island.generator";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "generator [status|claim [material|all]|expand]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Manage evolved passive generator storage and expansion.";
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

        service.executeGeneratorCommand(sender, superiorPlayer, args);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            return filter(args[1], Arrays.asList("status", "claim", "expand"));
        }

        if (args.length == 3 && "claim".equalsIgnoreCase(args[1])) {
            return filter(args[2], Arrays.asList("all", "cobblestone", "coal", "iron_ingot", "gold_ingot", "diamond"));
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
