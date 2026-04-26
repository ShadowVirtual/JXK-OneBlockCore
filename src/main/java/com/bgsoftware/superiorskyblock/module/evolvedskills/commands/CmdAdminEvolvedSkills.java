package com.bgsoftware.superiorskyblock.module.evolvedskills.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.evolvedskills.EvolvedSkillsService;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminEvolvedSkills implements ISuperiorCommand {

    private final EvolvedSkillsService service;

    public CmdAdminEvolvedSkills(EvolvedSkillsService service) {
        this.service = service;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("evolvedskills", "eskills");
    }

    @Override
    public String getPermission() {
        return "superior.admin.evolvedskills";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin evolvedskills <reload|status|setanchor|givenode|givespawner|givetreasure|setskill|resetplayer>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Admin controls for evolved skills module.";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 7;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        service.executeAdminCommand(sender, args);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return service.tabCompleteAdmin(args);
    }

}
