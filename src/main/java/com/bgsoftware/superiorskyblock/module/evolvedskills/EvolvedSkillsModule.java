package com.bgsoftware.superiorskyblock.module.evolvedskills;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdAdminEvolvedSkills;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdGenerator;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdJourney;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdNode;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdOneBlock;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdSkills;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdSpawner;
import com.bgsoftware.superiorskyblock.module.evolvedskills.commands.CmdTreasure;
import org.bukkit.event.Listener;

public class EvolvedSkillsModule extends BuiltinModule<EvolvedSkillsModule.Configuration> {

    private EvolvedSkillsService service;

    public EvolvedSkillsModule() {
        super("evolvedskills");
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        if (!isEnabled()) {
            return;
        }

        this.service = new EvolvedSkillsService(plugin, this);
        this.service.enable();
    }

    @Override
    public void onReload(SuperiorSkyblockPlugin plugin) {
        if (this.service != null) {
            this.service.disable();
            this.service = null;
        }

        super.onReload(plugin);
        onEnable(plugin);
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (this.service != null) {
            this.service.disable();
            this.service = null;
        }
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
        if (this.service != null) {
            this.service.loadPersistentState();
        }
    }

    @Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        if (this.service == null) {
            return null;
        }

        return new Listener[]{new EvolvedSkillsListener(this.service)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        if (this.service == null) {
            return null;
        }

        return new SuperiorCommand[]{
                new CmdSkills(this.service),
                new CmdOneBlock(this.service),
                new CmdNode(this.service),
                new CmdSpawner(this.service),
                new CmdGenerator(this.service),
                new CmdJourney(this.service),
                new CmdTreasure(this.service)
        };
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        if (this.service == null) {
            return null;
        }

        return new SuperiorCommand[]{new CmdAdminEvolvedSkills(this.service)};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    public EvolvedSkillsService getService() {
        return this.service;
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final int autosaveSeconds;
        private final int nodeTickSeconds;
        private final int spawnerTickSeconds;
        private final int generatorTickSeconds;
        private final String resetTimeZone;
        private final String resetLocalTime;

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled", false);
            this.autosaveSeconds = Math.max(10, config.getInt("runtime.autosave-seconds", 30));
            this.nodeTickSeconds = Math.max(1, config.getInt("runtime.node-tick-seconds", 1));
            this.spawnerTickSeconds = Math.max(1, config.getInt("runtime.spawner-tick-seconds", 1));
            this.generatorTickSeconds = Math.max(5, config.getInt("runtime.generator-tick-seconds", 60));
            this.resetTimeZone = config.getString("daily-reset.time-zone", "UTC");
            this.resetLocalTime = config.getString("daily-reset.local-time", "00:00");
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        public int getAutosaveSeconds() {
            return autosaveSeconds;
        }

        public int getNodeTickSeconds() {
            return nodeTickSeconds;
        }

        public int getSpawnerTickSeconds() {
            return spawnerTickSeconds;
        }

        public int getGeneratorTickSeconds() {
            return generatorTickSeconds;
        }

        public String getResetTimeZone() {
            return resetTimeZone;
        }

        public String getResetLocalTime() {
            return resetLocalTime;
        }
    }
}
