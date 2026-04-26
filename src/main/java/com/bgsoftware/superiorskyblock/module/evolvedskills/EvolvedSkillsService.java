package com.bgsoftware.superiorskyblock.module.evolvedskills;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandHomeTeleportEvent;
import com.bgsoftware.superiorskyblock.api.events.PostIslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;

public class EvolvedSkillsService {

    private static final String PREFIX = "&a&lᴏɴᴇʙʟᴏᴄᴋ &7» &r";
    private static final String ITEM_TAG_PREFIX = ChatColor.COLOR_CHAR + "0ESKILL|";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.##");

    private static final Material SPAWNER_MATERIAL = EnumHelper.getEnum(Material.class, "SPAWNER", "MOB_SPAWNER");
    private static final Material AIR_MATERIAL = EnumHelper.getEnum(Material.class, "AIR");
    private static final Map<Material, Material> BLOCK_TO_ITEM_ICON;
    static {
        BLOCK_TO_ITEM_ICON = new HashMap<>();
        putBlockItemMapping("CARROTS",             "CARROT");
        putBlockItemMapping("POTATOES",            "POTATO");
        putBlockItemMapping("BEETROOTS",           "BEETROOT");
        putBlockItemMapping("SWEET_BERRY_BUSH",    "SWEET_BERRIES");
        putBlockItemMapping("COCOA",               "COCOA_BEANS");
        putBlockItemMapping("CAVE_VINES",          "GLOW_BERRIES");
        putBlockItemMapping("CAVE_VINES_PLANT",    "GLOW_BERRIES");
        putBlockItemMapping("KELP_PLANT",          "KELP");
        putBlockItemMapping("TWISTING_VINES_PLANT","TWISTING_VINES");
        putBlockItemMapping("WEEPING_VINES_PLANT", "WEEPING_VINES");
        putBlockItemMapping("TORCHFLOWER_CROP",    "TORCHFLOWER");
        putBlockItemMapping("PITCHER_CROP",        "PITCHER_POD");
        putBlockItemMapping("CHORUS_PLANT",        "CHORUS_FRUIT");
        putBlockItemMapping("NETHER_WART_BLOCK",   "NETHER_WART");
        putBlockItemMapping("MELON",               "MELON_SLICE");
        putBlockItemMapping("WHEAT",               "WHEAT");
    }
    private static void putBlockItemMapping(String blockName, String itemName) {
        Material block = EnumHelper.getEnum(Material.class, blockName);
        Material item  = EnumHelper.getEnum(Material.class, itemName);
        if (block != null && item != null) {
            BLOCK_TO_ITEM_ICON.put(block, item);
        }
    }

    private static final Map<String, String> TREE_TYPE_TO_SAPLING_KEY;
    static {
        TREE_TYPE_TO_SAPLING_KEY = new HashMap<>();
        TREE_TYPE_TO_SAPLING_KEY.put("TREE", "oak_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("BIG_TREE", "oak_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("SWAMP", "oak_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("BIRCH", "birch_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("TALL_BIRCH", "birch_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("REDWOOD", "spruce_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("TALL_REDWOOD", "spruce_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("MEGA_REDWOOD", "spruce_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("PINE", "spruce_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("JUNGLE", "jungle_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("SMALL_JUNGLE", "jungle_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("COCOA_TREE", "jungle_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("JUNGLE_BUSH", "jungle_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("ACACIA", "acacia_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("DARK_OAK", "dark_oak_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("CHERRY", "cherry_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("MANGROVE", "mangrove_propagule");
        TREE_TYPE_TO_SAPLING_KEY.put("PALE_OAK", "pale_oak_sapling");
        TREE_TYPE_TO_SAPLING_KEY.put("CRIMSON_FUNGUS", "crimson_fungus");
        TREE_TYPE_TO_SAPLING_KEY.put("WARPED_FUNGUS", "warped_fungus");
    }

    private static final Map<String, String> FRAGMENT_NEXO_IDS;
    static {
        Map<String, String> m = new HashMap<>();
        
        m.put("iron_ore", "iron_fragment"); m.put("deepslate_iron_ore", "iron_fragment");
        m.put("coal_ore", "coal_fragment"); m.put("deepslate_coal_ore", "coal_fragment");
        m.put("gold_ore", "gold_fragment"); m.put("deepslate_gold_ore", "gold_fragment");
        m.put("diamond_ore", "diamond_fragment"); m.put("deepslate_diamond_ore", "diamond_fragment");
        m.put("emerald_ore", "emerald_fragment"); m.put("deepslate_emerald_ore", "emerald_fragment");
        m.put("lapis_ore", "lapis_fragment"); m.put("deepslate_lapis_ore", "lapis_fragment");
        m.put("redstone_ore", "redstone_fragment"); m.put("deepslate_redstone_ore", "redstone_fragment");
        m.put("copper_ore", "copper_fragment"); m.put("deepslate_copper_ore", "copper_fragment");
        m.put("nether_quartz_ore", "netherquartz_fragment");
        m.put("ancient_debris", "netherite_fragment");
        
        m.put("iron", "iron_fragment"); m.put("coal", "coal_fragment");
        m.put("gold", "gold_fragment"); m.put("diamond", "diamond_fragment");
        m.put("emerald", "emerald_fragment"); m.put("lapis", "lapis_fragment");
        m.put("redstone", "redstone_fragment"); m.put("copper", "copper_fragment");
        m.put("nether_quartz", "netherquartz_fragment"); m.put("netherite", "netherite_fragment");
        
        m.put("red_sand", "redsand_fragment");
        m.put("soul_sand", "soulsand_fragment"); m.put("soul_soil", "soulsand_fragment");
        m.put("end_stone", "endstone_fragment");
        m.put("dripstone_block", "dripstone_fragment"); m.put("pointed_dripstone", "dripstone_fragment");
        m.put("amethyst_block", "amethyst_fragment"); m.put("amethyst_cluster", "amethyst_fragment");
        m.put("small_amethyst_bud", "amethyst_fragment"); m.put("medium_amethyst_bud", "amethyst_fragment");
        m.put("large_amethyst_bud", "amethyst_fragment");
        m.put("smooth_basalt", "basalt_fragment");
        m.put("snow_block", "snow_fragment");
        
        m.put("stone", "stone_fragment"); m.put("dirt", "dirt_fragment");
        m.put("sand", "sand_fragment"); m.put("gravel", "gravel_fragment");
        m.put("andesite", "andesite_fragment"); m.put("granite", "granite_fragment");
        m.put("diorite", "diorite_fragment"); m.put("tuff", "tuff_fragment");
        m.put("clay", "clay_fragment"); m.put("mud", "mud_fragment");
        m.put("calcite", "calcite_fragment"); m.put("basalt", "basalt_fragment");
        m.put("blackstone", "blackstone_fragment"); m.put("deepslate", "deepslate_fragment");
        m.put("netherrack", "netherrack_fragment"); m.put("glowstone", "glowstone_fragment");
        m.put("snow", "snow_fragment");
        
        m.put("pale_oak_log", "pale_log_fragment");
        m.put("glow_berries", "glowberry_fragment");
        m.put("torchflower", "torch_flower_fragment"); m.put("torchflower_crop", "torch_flower_fragment");
        m.put("pitcher_pod", "pitcher_plant_fragment"); m.put("pitcher_crop", "pitcher_plant_fragment");
        m.put("sweet_berries", "wild_berry_fragment"); m.put("sweet_berry_bush", "wild_berry_fragment");
        m.put("cocoa_beans", "cacao_bean_fragment"); m.put("cocoa", "cacao_bean_fragment");
        m.put("warped_stem", "warped_log_fragment"); m.put("stripped_warped_stem", "warped_log_fragment");
        m.put("nether_wart", "netherwart_fragment"); m.put("nether_wart_block", "netherwart_fragment");
        m.put("melon_slice", "melon_fragment"); m.put("melon_block", "melon_fragment");
        
        m.put("hay_block", "wheat_fragment");
        m.put("brown_mushroom_block", "carrot_fragment");
        m.put("red_mushroom_block", "potato_fragment");
        m.put("rooted_dirt", "beetroot_fragment");
        m.put("dried_kelp_block", "kelp_fragment");
        m.put("stripped_jungle_log", "cacao_bean_fragment");
        m.put("moss_block", "glowberry_fragment");
        m.put("crimson_nylium", "weeping_vines_fragment");
        m.put("warped_nylium", "twisting_vines_fragment");
        
        m.put("wheat", "wheat_fragment"); m.put("carrot", "carrot_fragment");
        m.put("potato", "potato_fragment"); m.put("beetroot", "beetroot_fragment");
        m.put("pumpkin", "pumpkin_fragment"); m.put("melon", "melon_fragment");
        m.put("sugar_cane", "sugar_cane_fragment"); m.put("cactus", "cactus_fragment");
        m.put("bamboo", "bamboo_fragment"); m.put("kelp", "kelp_fragment");
        m.put("vine", "vine_fragment");
        m.put("weeping_vines", "weeping_vines_fragment"); m.put("twisting_vines", "twisting_vines_fragment");
        m.put("chorus_flower", "chorus_flower_fragment");
        m.put("oak_log", "oak_log_fragment"); m.put("birch_log", "birch_log_fragment");
        m.put("spruce_log", "spruce_log_fragment"); m.put("jungle_log", "jungle_log_fragment");
        m.put("acacia_log", "acacia_log_fragment"); m.put("dark_oak_log", "dark_oak_log_fragment");
        m.put("mangrove_log", "mangrove_log_fragment"); m.put("cherry_log", "cherry_log_fragment");
        m.put("crimson_stem", "crimson_stem_fragment");
        
        m.put("mushroom_cow", "mushroom_fragment"); m.put("mooshroom", "mushroom_fragment");
        
        m.put("zombie", "zombie_fragment"); m.put("skeleton", "skeleton_fragment");
        m.put("creeper", "creeper_fragment"); m.put("spider", "spider_fragment");
        m.put("enderman", "enderman_fragment"); m.put("blaze", "blaze_fragment");
        m.put("ghast", "ghast_fragment"); m.put("magma_cube", "magma_cube_fragment");
        m.put("slime", "slime_fragment"); m.put("cow", "cow_fragment");
        m.put("sheep", "sheep_fragment"); m.put("pig", "pig_fragment");
        m.put("chicken", "chicken_fragment"); m.put("rabbit", "rabbit_fragment");
        m.put("guardian", "guardian_fragment"); m.put("elder_guardian", "elder_guardian_fragment");
        m.put("warden", "warden_fragment"); m.put("wither_skeleton", "wither_skeleton_fragment");
        m.put("phantom", "phantom_fragment"); m.put("drowned", "drowned_fragment");
        m.put("strider", "strider_fragment"); m.put("shulker", "shulker_fragment");
        m.put("armadillo", "armadillo_fragment"); m.put("bee", "bee_fragment");
        m.put("bogged", "bogged_fragment"); m.put("breeze", "breeze_fragment");
        m.put("evoker", "evoker_fragment"); m.put("frog", "frog_fragment");
        m.put("squid", "squid_fragment"); m.put("stray", "stray_fragment");
        m.put("nautilus", "nautilus_fragment");
        FRAGMENT_NEXO_IDS = Collections.unmodifiableMap(m);
    }

    private static final Map<String, Integer> FRAGMENT_CMD_MAP;
    static {
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("acacia_log_fragment", 1048); m2.put("bamboo_fragment", 1049);
        m2.put("beetroot_fragment", 1050); m2.put("birch_log_fragment", 1051);
        m2.put("cacao_bean_fragment", 1052); m2.put("cactus_fragment", 1053);
        m2.put("carrot_fragment", 1054); m2.put("cherry_log_fragment", 1055);
        m2.put("chorus_flower_fragment", 1057); m2.put("crimson_stem_fragment", 1058);
        m2.put("dark_oak_log_fragment", 1059); m2.put("glowberry_fragment", 1060);
        m2.put("jungle_log_fragment", 1061); m2.put("kelp_fragment", 1062);
        m2.put("mangrove_log_fragment", 1063); m2.put("melon_fragment", 1064);
        m2.put("netherwart_fragment", 1065); m2.put("oak_log_fragment", 1066);
        m2.put("pale_log_fragment", 1067); m2.put("pitcher_plant_fragment", 1068);
        m2.put("potato_fragment", 1069); m2.put("pumpkin_fragment", 1070);
        m2.put("spruce_log_fragment", 1071); m2.put("sugar_cane_fragment", 1072);
        m2.put("torch_flower_fragment", 1073); m2.put("twisting_vines_fragment", 1074);
        m2.put("vine_fragment", 1075); m2.put("warped_log_fragment", 1076);
        m2.put("weeping_vines_fragment", 1077); m2.put("wheat_fragment", 1078);
        m2.put("wild_berry_fragment", 1079); m2.put("amethyst_fragment", 1080);
        m2.put("andesite_fragment", 1081); m2.put("basalt_fragment", 1082);
        m2.put("blackstone_fragment", 1083); m2.put("calcite_fragment", 1084);
        m2.put("clay_fragment", 1085); m2.put("coal_fragment", 1086);
        m2.put("copper_fragment", 1087); m2.put("deepslate_fragment", 1088);
        m2.put("diamond_fragment", 1089); m2.put("diorite_fragment", 1090);
        m2.put("dirt_fragment", 1091); m2.put("dripstone_fragment", 1092);
        m2.put("emerald_fragment", 1093); m2.put("endstone_fragment", 1094);
        m2.put("glowstone_fragment", 1095); m2.put("gold_fragment", 1096);
        m2.put("granite_fragment", 1097); m2.put("gravel_fragment", 1098);
        m2.put("iron_fragment", 1099); m2.put("lapis_fragment", 1100);
        m2.put("mud_fragment", 1101); m2.put("netherite_fragment", 1102);
        m2.put("netherquartz_fragment", 1103); m2.put("netherrack_fragment", 1104);
        m2.put("redsand_fragment", 1105); m2.put("redstone_fragment", 1106);
        m2.put("sand_fragment", 1107); m2.put("snow_fragment", 1108);
        m2.put("soulsand_fragment", 1109); m2.put("stone_fragment", 1110);
        m2.put("tuff_fragment", 1111); m2.put("armadillo_fragment", 1112);
        m2.put("bee_fragment", 1113); m2.put("blaze_fragment", 1114);
        m2.put("bogged_fragment", 1115); m2.put("breeze_fragment", 1116);
        m2.put("chicken_fragment", 1117); m2.put("cow_fragment", 1118);
        m2.put("creeper_fragment", 1119); m2.put("drowned_fragment", 1120);
        m2.put("elder_guardian_fragment", 1121); m2.put("enderman_fragment", 1122);
        m2.put("evoker_fragment", 1123); m2.put("frog_fragment", 1124);
        m2.put("ghast_fragment", 1125); m2.put("guardian_fragment", 1126);
        m2.put("magma_cube_fragment", 1127); m2.put("mushroom_fragment", 1128);
        m2.put("nautilus_fragment", 1129); m2.put("phantom_fragment", 1130);
        m2.put("pig_fragment", 1131); m2.put("rabbit_fragment", 1132);
        m2.put("sheep_fragment", 1133); m2.put("shulker_fragment", 1134);
        m2.put("skeleton_fragment", 1135); m2.put("slime_fragment", 1136);
        m2.put("spider_fragment", 1137); m2.put("squid_fragment", 1138);
        m2.put("stray_fragment", 1139); m2.put("strider_fragment", 1140);
        m2.put("warden_fragment", 1141); m2.put("wither_skeleton_fragment", 1142);
        m2.put("zombie_fragment", 1143);
        FRAGMENT_CMD_MAP = Collections.unmodifiableMap(m2);
    }

    private static final String DAILY_TASK_KILL_MOBS = "kill_mobs";
    private static final String DAILY_TASK_MINE_RESOURCES = "mine_resources";
    private static final String DAILY_TASK_HARVEST_CROPS = "harvest_crops";
    private static final int[] DAILY_TASK_MENU_SLOTS = new int[]{20, 22, 24};
    private static final int DAILY_ONEBLOCK_DAILY_SLOT = 31;
    private static final int[] DAILY_CHALLENGE_SLOTS = {27, 28, 29, 30, 32, 33, 34, 36, 37, 38};
    private static final int GOAL_PROGRESS_BAR_SEGMENTS = 16;

        private static final int[] GUI_GRID_SLOTS = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

    private final SuperiorSkyblockPlugin plugin;
    private final EvolvedSkillsModule module;
    private final Random random = new Random();

    private final Map<UUID, PlayerState> playerStates = new HashMap<>();
    private final Map<UUID, IslandState> islandStates = new HashMap<>();
    private final Map<LocationKey, NodeState> nodeStates = new HashMap<>();
    private final Map<LocationKey, SpawnerState> spawnerStates = new HashMap<>();
    private final Map<LocationKey, GeneratorState> generatorStates = new HashMap<>();
    private final Map<UUID, LocationKey> openGeneratorMenuViewers = new HashMap<>();
    private final Set<LocationKey> placedBlocks = new HashSet<>();
    private final Map<UUID, Object> activeBossBars = new HashMap<>();
    private final Map<UUID, BukkitTask> activeBossBarTasks = new HashMap<>();
    private final Map<UUID, Object> journeyBossBars = new HashMap<>();
    private final Map<LocationKey, BukkitTask> nodeCooldownTasks = new HashMap<>();
    private final Map<UUID, UUID> spawnedByEvolvedSpawner = new HashMap<>(); 
    private final Set<LocationKey> suppressDropLocations = new HashSet<>();
    private final Set<UUID> upgradeSoundPlayed = new HashSet<>();
    private final Map<LocationKey, UUID> recentBreakers = new HashMap<>();
    private final Map<UUID, String> editingPoolKey = new HashMap<>();

    private static final Set<Material> UNSTABLE_BLOCK_TYPES;
    static {
        Set<Material> s = new HashSet<>();
        for (String name : java.util.Arrays.asList(
                "SUGAR_CANE", "SWEET_BERRY_BUSH", "CACTUS", "TORCHFLOWER",
                "PITCHER_PLANT", "CHORUS_FLOWER", "KELP", "KELP_PLANT",
                "BAMBOO", "VINE", "TWISTING_VINES", "WEEPING_VINES",
                "CAVE_VINES", "CAVE_VINES_PLANT", "BIG_DRIPLEAF",
                "SMALL_DRIPLEAF", "HANGING_ROOTS", "SPORE_BLOSSOM")) {
            Material m = Material.matchMaterial(name);
            if (m != null) s.add(m);
        }
        UNSTABLE_BLOCK_TYPES = java.util.Collections.unmodifiableSet(s);
    }
    private final Set<UUID> pendingAnchorSetup = new HashSet<>();
    private final Map<UUID, String> pendingNodeCrafts = new HashMap<>();

    private EvolvedConfig config;
    private File stateFile;
    private long lastDailyResetEpoch;
    private List<String> assignedDailyChallengeKeys = new ArrayList<>();

    private BukkitTask autosaveTask;
    private BukkitTask nodeTickTask;
    private BukkitTask spawnerTickTask;
    private BukkitTask generatorTickTask;
    private BukkitTask dailyTickTask;
    private BukkitTask growthResetTask;
    private BukkitTask journeyBossBarRefreshTask;
    private long lastGrowthResetMinecraftDay = -1L;

    private transient boolean itemsAdderGlyphMethodResolved;
    private transient Method itemsAdderReplaceFontImagesMethod;

    private transient boolean oraxenGlyphMethodsResolved;
    private transient Method oraxenPluginGetMethod;
    private transient Method oraxenGetFontManagerMethod;
    private transient Method oraxenGetGlyphByPlaceholderMapMethod;
    private transient Method oraxenGlyphGetCharacterMethod;

    private transient boolean nexoGlyphMethodsResolved;
    private transient Method nexoPluginInstanceMethod;
    private transient Method nexoFontManagerMethod;
    private transient Method nexoGetPlaceholderGlyphMapMethod;
    private transient Method nexoGlyphCharacterMethod;

    public EvolvedSkillsService(SuperiorSkyblockPlugin plugin, EvolvedSkillsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    public void enable() {
        ensureDefaultConfigFiles();
        reloadConfiguration();
        File dataStoreFolder = module.getDataStoreFolder();
        if (!dataStoreFolder.exists()) {
            
            dataStoreFolder.mkdirs();
        }
        this.stateFile = new File(dataStoreFolder, "state.yml");
        registerStandaloneCommands();
        registerBlockDropItemListener();
        registerNodeCraftingListener();
        registerPapiExpansion();
    }

    private void registerPapiExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        try {
            new EvolvedSkillsPapi().register();
        } catch (Throwable t) {
            plugin.getLogger().warning("[EvolvedSkills] Failed to register PlaceholderAPI expansion: " + t.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void registerBlockDropItemListener() {
        try {
            Class<? extends org.bukkit.event.Event> dropItemClass =
                    (Class<? extends org.bukkit.event.Event>) Class.forName("org.bukkit.event.block.BlockDropItemEvent");
            org.bukkit.event.Listener dummyListener = new org.bukkit.event.Listener() {};
            Bukkit.getPluginManager().registerEvent(dropItemClass, dummyListener,
                    org.bukkit.event.EventPriority.LOWEST,
                    (listener, event) -> {
                        try {
                            Block block = (Block) event.getClass().getMethod("getBlock").invoke(event);
                            LocationKey key = LocationKey.fromBlock(block);
                            if (suppressDropLocations.contains(key)) {
                                ((org.bukkit.event.Cancellable) event).setCancelled(true);
                                return;
                            }
                            UUID breakerUUID = recentBreakers.get(key);
                            if (breakerUUID != null) {
                                Player breaker = Bukkit.getPlayer(breakerUUID);
                                if (breaker != null && breaker.isOnline()) {
                                    @SuppressWarnings("unchecked")
                                    java.util.List<org.bukkit.entity.Item> items =
                                        (java.util.List<org.bukkit.entity.Item>) event.getClass().getMethod("getItems").invoke(event);
                                    for (org.bukkit.entity.Item item : items) {
                                        giveItemToPlayer(breaker, item.getItemStack());
                                    }
                                    if (!items.isEmpty()) playEffect(breaker, "sounds:item_pick_up", 0.5f, 1.0f);
                                    ((org.bukkit.event.Cancellable) event).setCancelled(true);
                                }
                            }
                        } catch (Exception ignored) {}
                    },
                    plugin, false);
        } catch (ClassNotFoundException ignored) {
            
        }
    }

    @SuppressWarnings("unchecked")
    private void registerNodeCraftingListener() {
        try {
            Class<? extends org.bukkit.event.Event> prepareClass =
                (Class<? extends org.bukkit.event.Event>) Class.forName("org.bukkit.event.inventory.PrepareItemCraftEvent");
            org.bukkit.event.Listener listener = new org.bukkit.event.Listener() {};
            Bukkit.getPluginManager().registerEvent(prepareClass, listener,
                org.bukkit.event.EventPriority.HIGHEST,
                (l, event) -> {
                    try {
                        org.bukkit.inventory.CraftingInventory inv =
                            (org.bukkit.inventory.CraftingInventory) prepareClass.getMethod("getInventory").invoke(event);
                        UUID crafterUUID = null;
                        try {
                            Object view = event.getClass().getMethod("getView").invoke(event);
                            org.bukkit.entity.HumanEntity human = (org.bukkit.entity.HumanEntity)
                                view.getClass().getMethod("getPlayer").invoke(view);
                            crafterUUID = human.getUniqueId();
                        } catch (Exception ignored3) {}
                        if (crafterUUID != null) pendingNodeCrafts.remove(crafterUUID);
                        ItemStack[] matrix = inv.getMatrix();
                        if (matrix.length < 9) return;
                        String fragmentKey = null;
                        for (int i = 0; i < 9; i++) {
                            ItemStack slot = matrix[i];
                            if (slot == null || slot.getType() == AIR_MATERIAL) return;
                            String tag = getItemTag(slot, "FRAGMENT");
                            if (tag == null) return;
                            if (fragmentKey == null) {
                                fragmentKey = tag.toLowerCase(Locale.ENGLISH);
                            } else if (!fragmentKey.equals(tag.toLowerCase(Locale.ENGLISH))) {
                                return;
                            }
                        }
                        if (fragmentKey == null) return;
                        String fragNexoId = FRAGMENT_NEXO_IDS.get(fragmentKey);
                        String baseKey = fragNexoId != null ? fragNexoId.replace("_fragment", "") : fragmentKey;
                        NodeTypeDefinition nodeDef = config.nodeTypes.get(baseKey);
                        if (nodeDef != null) {
                            inv.setResult(createNodeItem(nodeDef.key, 1));
                            if (crafterUUID != null) pendingNodeCrafts.put(crafterUUID, "node:" + nodeDef.key);
                            return;
                        }
                        SpawnerTypeDefinition spawnerDef = config.spawnerTypes.get(baseKey);
                        if (spawnerDef != null) {
                            inv.setResult(createSpawnerItem(spawnerDef.key, 1));
                            if (crafterUUID != null) pendingNodeCrafts.put(crafterUUID, "spawner:" + spawnerDef.key);
                        }
                    } catch (Exception ignored2) {}
                },
                plugin, false);
        } catch (ClassNotFoundException ignored) {}

        
        
        try {
            @SuppressWarnings("unchecked")
            Class<? extends org.bukkit.event.Event> clickClass =
                (Class<? extends org.bukkit.event.Event>) Class.forName("org.bukkit.event.inventory.InventoryClickEvent");
            org.bukkit.event.Listener clickListener = new org.bukkit.event.Listener() {};
            Bukkit.getPluginManager().registerEvent(clickClass, clickListener,
                org.bukkit.event.EventPriority.HIGHEST,
                (l, event) -> {
                    try {
                        int rawSlot = (int) event.getClass().getMethod("getRawSlot").invoke(event);
                        if (rawSlot != 0) return;
                        org.bukkit.entity.HumanEntity who =
                            (org.bukkit.entity.HumanEntity) event.getClass().getMethod("getWhoClicked").invoke(event);
                        if (!(who instanceof Player)) return;
                        UUID uid = who.getUniqueId();
                        String craftKey = pendingNodeCrafts.get(uid);
                        if (craftKey == null) return;
                        Object topInv = event.getClass().getMethod("getInventory").invoke(event);
                        if (!(topInv instanceof org.bukkit.inventory.CraftingInventory)) return;
                        org.bukkit.inventory.CraftingInventory craftInv =
                            (org.bukkit.inventory.CraftingInventory) topInv;
                        ItemStack result = craftInv.getResult();
                        if (result == null || result.getType() == AIR_MATERIAL) return;
                        if (getItemTag(result, "NODE") == null && getItemTag(result, "SPAWNER") == null) return;
                        pendingNodeCrafts.remove(uid);
                        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer((Player) who);
                        if (sp == null) return;
                        PlayerState ps = getPlayerState(uid);
                        progressJourney(sp, ps, JourneyType.CRAFT_ITEM, craftKey, 1L);
                    } catch (Exception ignored5) {}
                },
                plugin, false);
        } catch (Exception ignored4) {}
    }

    private void registerStandaloneCommands() {
        try {
            org.bukkit.command.CommandMap commandMap;
            try {
                java.lang.reflect.Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                commandMap = (org.bukkit.command.CommandMap) f.get(Bukkit.getServer());
            } catch (Exception e) {
                plugin.getLogger().warning("[EvolvedSkills] Could not access CommandMap: " + e.getMessage());
                return;
            }

            Map<String, org.bukkit.command.Command> knownCommands = null;
            try {
                java.lang.reflect.Method getKnown = commandMap.getClass().getMethod("getKnownCommands");
                @SuppressWarnings("unchecked")
                Map<String, org.bukkit.command.Command> kc =
                        (Map<String, org.bukkit.command.Command>) getKnown.invoke(commandMap);
                knownCommands = kc;
            } catch (Exception ignored) {}

            String[] cmds = {"skills", "generator", "node", "spawner", "journey", "treasure", "daily", "treasurepool", "level", "glimits"};
            for (String name : cmds) {
                final String cmdName = name;
                org.bukkit.command.Command cmd = new org.bukkit.command.Command(name) {
                    @Override
                    public boolean execute(CommandSender sender, String label, String[] args) {
                        if (sender instanceof Player) {
                            String full = "/" + cmdName + (args.length > 0 ? " " + String.join(" ", args) : "");
                            PlayerCommandPreprocessEvent fake = new PlayerCommandPreprocessEvent((Player) sender, full);
                            handleCommand(fake);
                            return fake.isCancelled();
                        }
                        return false;
                    }
                };
                cmd.setDescription("EvolvedSkills " + name + " command");

                if (knownCommands != null) {
                    knownCommands.remove(name.toLowerCase(Locale.ENGLISH));
                    for (String key : new java.util.ArrayList<>(knownCommands.keySet())) {
                        if (key.endsWith(":" + name.toLowerCase(Locale.ENGLISH))) {
                            knownCommands.remove(key);
                        }
                    }
                }

                commandMap.register("evolvedskills", cmd);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[EvolvedSkills] Failed to register commands: " + e.getMessage());
        }
    }

    public void disable() {
        cancelTasks();
        
        savePersistentStateSync();
    }

    public void reloadConfiguration() {
        this.config = loadConfiguration(module.getModuleFolder());
        scheduleTasks();
    }

    public void loadPersistentState() {
        if (this.stateFile == null || !this.stateFile.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.stateFile);

        this.playerStates.clear();
        this.islandStates.clear();
        this.nodeStates.clear();
        this.spawnerStates.clear();
        this.generatorStates.clear();

        this.lastDailyResetEpoch = yaml.getLong("meta.last-daily-reset-epoch", 0L);
        this.lastGrowthResetMinecraftDay = yaml.getLong("meta.last-growth-reset-mc-day", -1L);
        this.assignedDailyChallengeKeys = new ArrayList<>(yaml.getStringList("meta.assigned-challenges"));

        ConfigurationSection playersSection = yaml.getConfigurationSection("players");
        if (playersSection != null) {
            for (String playerId : playersSection.getKeys(false)) {
                UUID uuid = parseUuid(playerId);
                if (uuid == null) {
                    continue;
                }

                ConfigurationSection playerSection = playersSection.getConfigurationSection(playerId);
                if (playerSection == null) {
                    continue;
                }

                PlayerState playerState = new PlayerState();
                
                long legacySkillXp = playerSection.getLong("skill-xp", 0L);
                int legacySkillLevel = playerSection.getInt("skill-level", 1);
                playerState.miningXp = playerSection.getLong("mining-xp", legacySkillXp);
                playerState.miningLevel = playerSection.getInt("mining-level", legacySkillLevel);
                playerState.farmingXp = playerSection.getLong("farming-xp", 0L);
                playerState.farmingLevel = playerSection.getInt("farming-level", 1);
                playerState.slayingXp = playerSection.getLong("slaying-xp", 0L);
                playerState.slayingLevel = playerSection.getInt("slaying-level", 1);
                
                int legacyPerkLevel = playerSection.getInt("perk-level", 0);
                if (legacyPerkLevel > 0) {
                    playerState.miningPerkLevels[0] = Math.min(legacyPerkLevel, 10);
                }
                loadPerkLevelArray(playerSection, "mining-perk-levels", playerState.miningPerkLevels);
                loadPerkLevelArray(playerSection, "farming-perk-levels", playerState.farmingPerkLevels);
                loadPerkLevelArray(playerSection, "slaying-perk-levels", playerState.slayingPerkLevels);
                playerState.playerXp = playerSection.getLong("player-xp", 0L);
                playerState.playerLevel = playerSection.getInt("player-level", 1);
                playerState.prestigeCount = playerSection.getInt("prestige-count", 0);
                playerState.dailyOneblockBroken = playerSection.getInt("daily-oneblock-broken", 0);
                playerState.dailyOneblockClaimed = playerSection.getBoolean("daily-oneblock-claimed", false);

                ConfigurationSection dailyProgressSection = playerSection.getConfigurationSection("daily-task-progress");
                if (dailyProgressSection != null) {
                    for (String taskKey : dailyProgressSection.getKeys(false)) {
                        playerState.dailyTaskProgress.put(taskKey.toLowerCase(Locale.ENGLISH),
                                Math.max(0L, dailyProgressSection.getLong(taskKey, 0L)));
                    }
                }

                ConfigurationSection claimedDailyStagesSection = playerSection.getConfigurationSection("claimed-daily-task-stages");
                if (claimedDailyStagesSection != null) {
                    for (String taskKey : claimedDailyStagesSection.getKeys(false)) {
                        playerState.claimedDailyTaskStages.put(taskKey.toLowerCase(Locale.ENGLISH),
                                Math.max(0, claimedDailyStagesSection.getInt(taskKey, 0)));
                    }
                }

                ConfigurationSection challengeProgressSection = playerSection.getConfigurationSection("challenge-progress");
                if (challengeProgressSection != null) {
                    for (String k : challengeProgressSection.getKeys(false)) {
                        playerState.challengeProgress.put(k.toLowerCase(Locale.ENGLISH), Math.max(0L, challengeProgressSection.getLong(k, 0L)));
                    }
                }
                playerState.claimedChallenges.addAll(playerSection.getStringList("claimed-challenges"));

                List<?> claimedPlayerLevelRewards = playerSection.getList("claimed-player-level-rewards");
                if (claimedPlayerLevelRewards != null) {
                    for (Object rawValue : claimedPlayerLevelRewards) {
                        int level;
                        if (rawValue instanceof Number) {
                            level = ((Number) rawValue).intValue();
                        } else {
                            level = parseInt(String.valueOf(rawValue), -1);
                        }

                        if (level > 0) {
                            playerState.claimedPlayerLevelRewards.add(level);
                        }
                    }
                }

                List<String> claimableJourney = playerSection.getStringList("claimable-journey");
                for (String taskKey : claimableJourney) {
                    playerState.completedJourney.add(taskKey.toLowerCase(Locale.ENGLISH));
                }

                List<String> claimedJourney = playerSection.getStringList("claimed-journey");
                for (String taskKey : claimedJourney) {
                    playerState.claimedJourney.add(taskKey.toLowerCase(Locale.ENGLISH));
                }

                
                if (playerState.claimedJourney.isEmpty() && playerState.completedJourney.isEmpty()) {
                    List<String> completedJourney = playerSection.getStringList("completed-journey");
                    for (String taskKey : completedJourney) {
                        String normalized = taskKey.toLowerCase(Locale.ENGLISH);
                        playerState.completedJourney.add(normalized);
                        playerState.claimedJourney.add(normalized);
                    }
                }

                ConfigurationSection journeyProgressSection = playerSection.getConfigurationSection("journey-progress");
                if (journeyProgressSection != null) {
                    for (String taskKey : journeyProgressSection.getKeys(false)) {
                        playerState.journeyProgress.put(taskKey.toLowerCase(Locale.ENGLISH),
                                journeyProgressSection.getLong(taskKey, 0L));
                    }
                }

                ConfigurationSection mileProgressSection = playerSection.getConfigurationSection("milestone-progress");
                if (mileProgressSection != null) {
                    for (String k : mileProgressSection.getKeys(false)) {
                        playerState.milestoneProgress.put(k, mileProgressSection.getLong(k, 0L));
                    }
                }
                ConfigurationSection mileTiersSection = playerSection.getConfigurationSection("milestone-tiers-claimed");
                if (mileTiersSection != null) {
                    for (String k : mileTiersSection.getKeys(false)) {
                        playerState.milestoneTiersClaimed.put(k, mileTiersSection.getInt(k, 0));
                    }
                }

                this.playerStates.put(uuid, playerState);
            }
        }

        ConfigurationSection islandsSection = yaml.getConfigurationSection("islands");
        if (islandsSection != null) {
            for (String islandId : islandsSection.getKeys(false)) {
                UUID uuid = parseUuid(islandId);
                if (uuid == null) {
                    continue;
                }

                ConfigurationSection islandSection = islandsSection.getConfigurationSection(islandId);
                if (islandSection == null) {
                    continue;
                }

                IslandState islandState = new IslandState();
                islandState.oneblockBrokenTotal = islandSection.getLong("oneblock-total-broken", 0L);
                islandState.unlockedMilestones.addAll(normalizeKeys(islandSection.getStringList("unlocked-milestones")));

                String serializedAnchor = islandSection.getString("oneblock-anchor");
                if (serializedAnchor != null && !serializedAnchor.isEmpty()) {
                    LocationKey locationKey = LocationKey.deserialize(serializedAnchor);
                    if (locationKey != null) {
                        islandState.oneblockAnchor = locationKey;
                    }
                }

                
                int legacyGeneratorLevel = islandSection.getInt("generator-level", 0);
                if (legacyGeneratorLevel > 0) {
                    islandState.generatorLevels.put("mining", Math.max(1, legacyGeneratorLevel));
                }

                
                ConfigurationSection legacyStorageSection = islandSection.getConfigurationSection("generator-storage");
                if (legacyStorageSection != null) {
                    Map<Material, Integer> miningStorage = islandState.generatorStorages.computeIfAbsent("mining", k -> new HashMap<>());
                    for (String materialName : legacyStorageSection.getKeys(false)) {
                        Material material = parseMaterial(materialName);
                        if (material == null) continue;
                        miningStorage.put(material, Math.max(0, legacyStorageSection.getInt(materialName, 0)));
                    }
                }

                
                ConfigurationSection genLevelsSection = islandSection.getConfigurationSection("generator-levels");
                if (genLevelsSection != null) {
                    for (String trackKey : genLevelsSection.getKeys(false)) {
                        islandState.generatorLevels.put(trackKey.toLowerCase(Locale.ENGLISH),
                                Math.max(1, genLevelsSection.getInt(trackKey, 1)));
                    }
                }

                
                for (String trackKey : asList("mining", "farming", "slaying")) {
                    ConfigurationSection trackStorageSection = islandSection.getConfigurationSection("generator-storage-" + trackKey);
                    if (trackStorageSection != null) {
                        Map<Material, Integer> trackStorage = islandState.generatorStorages.computeIfAbsent(trackKey, k -> new HashMap<>());
                        for (String materialName : trackStorageSection.getKeys(false)) {
                            Material material = parseMaterial(materialName);
                            if (material == null) continue;
                            trackStorage.merge(material, Math.max(0, trackStorageSection.getInt(materialName, 0)), Integer::sum);
                        }
                    }
                }

                ConfigurationSection nodeLimitTierSection = islandSection.getConfigurationSection("node-limit-tiers");
                if (nodeLimitTierSection != null) {
                    for (String nodeTypeKey : nodeLimitTierSection.getKeys(false)) {
                        islandState.nodeLimitTiers.put(nodeTypeKey.toLowerCase(Locale.ENGLISH),
                                Math.max(0, nodeLimitTierSection.getInt(nodeTypeKey, 0)));
                    }
                }

                ConfigurationSection spawnerLimitTierSection = islandSection.getConfigurationSection("spawner-limit-tiers");
                if (spawnerLimitTierSection != null) {
                    for (String spawnerTypeKey : spawnerLimitTierSection.getKeys(false)) {
                        islandState.spawnerLimitTiers.put(spawnerTypeKey.toLowerCase(Locale.ENGLISH),
                                Math.max(0, spawnerLimitTierSection.getInt(spawnerTypeKey, 0)));
                    }
                }

                ConfigurationSection growthCountSection = islandSection.getConfigurationSection("daily-growth-counts");
                if (growthCountSection != null) {
                    for (String k : growthCountSection.getKeys(false)) {
                        islandState.dailyGrowthCounts.put(k, growthCountSection.getInt(k, 0));
                    }
                }
                ConfigurationSection growthTierSection = islandSection.getConfigurationSection("growth-limit-tiers");
                if (growthTierSection != null) {
                    for (String k : growthTierSection.getKeys(false)) {
                        islandState.growthLimitTiers.put(k, growthTierSection.getInt(k, 0));
                    }
                }

                this.islandStates.put(uuid, islandState);
            }
        }

        ConfigurationSection nodesSection = yaml.getConfigurationSection("nodes");
        if (nodesSection != null) {
            for (String locationId : nodesSection.getKeys(false)) {
                LocationKey locationKey = LocationKey.deserialize(locationId);
                if (locationKey == null) {
                    continue;
                }

                ConfigurationSection nodeSection = nodesSection.getConfigurationSection(locationId);
                if (nodeSection == null) {
                    continue;
                }

                UUID islandId = parseUuid(nodeSection.getString("island"));
                if (islandId == null) {
                    continue;
                }

                NodeState nodeState = new NodeState();
                nodeState.islandId = islandId;
                nodeState.typeKey = nodeSection.getString("type", "wheat").toLowerCase(Locale.ENGLISH);
                nodeState.level = Math.max(1, nodeSection.getInt("level", 1));
                nodeState.progress = Math.max(0D, nodeSection.getDouble("progress", 0D));
                nodeState.stored = Math.max(0, nodeSection.getInt("stored", 0));
                
                nodeState.onCooldown = false;
                this.nodeStates.put(locationKey, nodeState);
                
                final LocationKey lk = locationKey;
                final String typeKey = nodeState.typeKey;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    NodeState current = nodeStates.get(lk);
                    if (current == null) return;
                    NodeTypeDefinition def = config == null ? null : config.nodeTypes.get(typeKey);
                    if (def == null) return;
                    Location restoreLoc = lk.toLocation();
                    if (restoreLoc != null && restoreLoc.getBlock().getType() == Material.BEDROCK) {
                        Material display = def.displayBlock != null ? def.displayBlock : Material.AIR;
                        restoreLoc.getBlock().setType(display);
                    }
                }, 1L);
            }
        }

        ConfigurationSection spawnersSection = yaml.getConfigurationSection("spawners");
        if (spawnersSection != null) {
            for (String locationId : spawnersSection.getKeys(false)) {
                LocationKey locationKey = LocationKey.deserialize(locationId);
                if (locationKey == null) {
                    continue;
                }

                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(locationId);
                if (spawnerSection == null) {
                    continue;
                }

                UUID islandId = parseUuid(spawnerSection.getString("island"));
                if (islandId == null) {
                    continue;
                }

                SpawnerState spawnerState = new SpawnerState();
                spawnerState.islandId = islandId;
                spawnerState.typeKey = spawnerSection.getString("type", "zombie").toLowerCase(Locale.ENGLISH);
                spawnerState.level = Math.max(1, spawnerSection.getInt("level", 1));
                spawnerState.progress = Math.max(0D, spawnerSection.getDouble("progress", 0D));
                spawnerState.stored = Math.max(0, spawnerSection.getInt("stored", 0));
                this.spawnerStates.put(locationKey, spawnerState);
            }
        }

        ConfigurationSection genSection = yaml.getConfigurationSection("generators");
        if (genSection != null) {
            for (String locRaw : genSection.getKeys(false)) {
                ConfigurationSection gs = genSection.getConfigurationSection(locRaw);
                if (gs == null) continue;
                String world = gs.getString("world");
                int x = gs.getInt("x");
                int y = gs.getInt("y");
                int z = gs.getInt("z");
                UUID islandId = parseUuid(gs.getString("island-id"));
                String trackKey = gs.getString("track", "mining");
                if (world == null || islandId == null) continue;
                LocationKey lk = new LocationKey(world, x, y, z);
                GeneratorState state = new GeneratorState();
                state.islandId = islandId;
                state.trackKey = trackKey;
                state.level = Math.max(1, gs.getInt("level", 1));
                state.tickProgress = gs.getDouble("tick-progress", 0.0);
                ConfigurationSection storageSection = gs.getConfigurationSection("storage");
                if (storageSection != null) {
                    for (String matName : storageSection.getKeys(false)) {
                        Material mat = parseMaterial(matName);
                        if (mat == null) continue;
                        state.storage.put(mat, Math.max(0, storageSection.getInt(matName, 0)));
                    }
                }
                generatorStates.put(lk, state);
                placedBlocks.add(lk);
            }
        }

        reconcileLevelsFromExperience();
    }

    public void savePersistentState() {
        if (this.stateFile == null) {
            return;
        }
        YamlConfiguration yaml = buildSaveYaml();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> writeYamlToDisk(yaml, stateFile));
    }

    private void savePersistentStateSync() {
        if (this.stateFile == null) {
            return;
        }
        writeYamlToDisk(buildSaveYaml(), stateFile);
    }

    private void writeYamlToDisk(YamlConfiguration yaml, File targetFile) {
        try {
            if (!targetFile.exists()) {
                File parent = targetFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    
                    parent.mkdirs();
                }
                
                targetFile.createNewFile();
            }
            yaml.save(targetFile);
        } catch (IOException error) {
            module.getLogger().severe(String.format("Failed to save evolved skills data: %s", error.getMessage()));
        }
    }

    private YamlConfiguration buildSaveYaml() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("meta.last-daily-reset-epoch", this.lastDailyResetEpoch);
        yaml.set("meta.last-growth-reset-mc-day", this.lastGrowthResetMinecraftDay);
        yaml.set("meta.assigned-challenges", new ArrayList<>(assignedDailyChallengeKeys));

        ConfigurationSection playersSection = yaml.createSection("players");
        for (Map.Entry<UUID, PlayerState> entry : this.playerStates.entrySet()) {
            ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());
            PlayerState state = entry.getValue();
            playerSection.set("mining-xp", state.miningXp);
            playerSection.set("mining-level", state.miningLevel);
            playerSection.set("farming-xp", state.farmingXp);
            playerSection.set("farming-level", state.farmingLevel);
            playerSection.set("slaying-xp", state.slayingXp);
            playerSection.set("slaying-level", state.slayingLevel);
            playerSection.set("mining-perk-levels", toIntList(state.miningPerkLevels));
            playerSection.set("farming-perk-levels", toIntList(state.farmingPerkLevels));
            playerSection.set("slaying-perk-levels", toIntList(state.slayingPerkLevels));
            playerSection.set("player-xp", state.playerXp);
            playerSection.set("player-level", state.playerLevel);
            playerSection.set("prestige-count", state.prestigeCount);
            playerSection.set("daily-oneblock-broken", state.dailyOneblockBroken);
            playerSection.set("daily-oneblock-claimed", state.dailyOneblockClaimed);
            playerSection.set("claimed-player-level-rewards", new ArrayList<>(state.claimedPlayerLevelRewards));

            ConfigurationSection dailyProgressSection = playerSection.createSection("daily-task-progress");
            for (Map.Entry<String, Long> dailyProgressEntry : state.dailyTaskProgress.entrySet()) {
                dailyProgressSection.set(dailyProgressEntry.getKey(), dailyProgressEntry.getValue());
            }

            ConfigurationSection claimedDailyStagesSection = playerSection.createSection("claimed-daily-task-stages");
            for (Map.Entry<String, Integer> claimedStageEntry : state.claimedDailyTaskStages.entrySet()) {
                claimedDailyStagesSection.set(claimedStageEntry.getKey(), claimedStageEntry.getValue());
            }

            ConfigurationSection challengeProgressSection = playerSection.createSection("challenge-progress");
            for (Map.Entry<String, Long> e : state.challengeProgress.entrySet()) {
                challengeProgressSection.set(e.getKey(), e.getValue());
            }
            playerSection.set("claimed-challenges", new ArrayList<>(state.claimedChallenges));

            playerSection.set("claimable-journey", new ArrayList<>(state.completedJourney));
            playerSection.set("claimed-journey", new ArrayList<>(state.claimedJourney));
            playerSection.set("completed-journey", new ArrayList<>(state.claimedJourney));

            ConfigurationSection journeySection = playerSection.createSection("journey-progress");
            for (Map.Entry<String, Long> journeyEntry : state.journeyProgress.entrySet()) {
                journeySection.set(journeyEntry.getKey(), journeyEntry.getValue());
            }

            ConfigurationSection mileProgressSection = playerSection.createSection("milestone-progress");
            for (Map.Entry<String, Long> e : state.milestoneProgress.entrySet()) {
                mileProgressSection.set(e.getKey(), e.getValue());
            }
            ConfigurationSection mileTiersSection = playerSection.createSection("milestone-tiers-claimed");
            for (Map.Entry<String, Integer> e : state.milestoneTiersClaimed.entrySet()) {
                mileTiersSection.set(e.getKey(), e.getValue());
            }
        }

        ConfigurationSection islandsSection = yaml.createSection("islands");
        for (Map.Entry<UUID, IslandState> entry : this.islandStates.entrySet()) {
            ConfigurationSection islandSection = islandsSection.createSection(entry.getKey().toString());
            IslandState state = entry.getValue();
            islandSection.set("oneblock-total-broken", state.oneblockBrokenTotal);
            islandSection.set("unlocked-milestones", new ArrayList<>(state.unlockedMilestones));
            islandSection.set("oneblock-anchor", state.oneblockAnchor == null ? null : state.oneblockAnchor.serialize());

            ConfigurationSection nodeLimitTierSection = islandSection.createSection("node-limit-tiers");
            for (Map.Entry<String, Integer> nodeLimitEntry : state.nodeLimitTiers.entrySet()) {
                nodeLimitTierSection.set(nodeLimitEntry.getKey(), nodeLimitEntry.getValue());
            }

            ConfigurationSection spawnerLimitTierSection = islandSection.createSection("spawner-limit-tiers");
            for (Map.Entry<String, Integer> spawnerLimitEntry : state.spawnerLimitTiers.entrySet()) {
                spawnerLimitTierSection.set(spawnerLimitEntry.getKey(), spawnerLimitEntry.getValue());
            }

            ConfigurationSection growthCountSection = islandSection.createSection("daily-growth-counts");
            for (Map.Entry<String, Integer> e : state.dailyGrowthCounts.entrySet()) {
                growthCountSection.set(e.getKey(), e.getValue());
            }
            ConfigurationSection growthTierSection = islandSection.createSection("growth-limit-tiers");
            for (Map.Entry<String, Integer> e : state.growthLimitTiers.entrySet()) {
                growthTierSection.set(e.getKey(), e.getValue());
            }
        }

        ConfigurationSection nodesSection = yaml.createSection("nodes");
        for (Map.Entry<LocationKey, NodeState> entry : this.nodeStates.entrySet()) {
            ConfigurationSection nodeSection = nodesSection.createSection(entry.getKey().serialize());
            NodeState nodeState = entry.getValue();
            nodeSection.set("island", nodeState.islandId.toString());
            nodeSection.set("type", nodeState.typeKey);
            nodeSection.set("level", nodeState.level);
            nodeSection.set("progress", nodeState.progress);
            nodeSection.set("stored", nodeState.stored);
        }

        ConfigurationSection spawnersSection = yaml.createSection("spawners");
        for (Map.Entry<LocationKey, SpawnerState> entry : this.spawnerStates.entrySet()) {
            ConfigurationSection spawnerSection = spawnersSection.createSection(entry.getKey().serialize());
            SpawnerState spawnerState = entry.getValue();
            spawnerSection.set("island", spawnerState.islandId.toString());
            spawnerSection.set("type", spawnerState.typeKey);
            spawnerSection.set("level", spawnerState.level);
            spawnerSection.set("progress", spawnerState.progress);
            spawnerSection.set("stored", spawnerState.stored);
        }

        ConfigurationSection genSection = yaml.createSection("generators");
        for (Map.Entry<LocationKey, GeneratorState> e : generatorStates.entrySet()) {
            LocationKey lk = e.getKey();
            GeneratorState gs2 = e.getValue();
            String locKey = lk.world + "," + lk.x + "," + lk.y + "," + lk.z;
            ConfigurationSection gs = genSection.createSection(locKey);
            gs.set("world", lk.world);
            gs.set("x", lk.x);
            gs.set("y", lk.y);
            gs.set("z", lk.z);
            gs.set("island-id", gs2.islandId.toString());
            gs.set("track", gs2.trackKey);
            gs.set("level", gs2.level);
            gs.set("tick-progress", gs2.tickProgress);
            if (!gs2.storage.isEmpty()) {
                ConfigurationSection storageSection = gs.createSection("storage");
                for (Map.Entry<Material, Integer> se : gs2.storage.entrySet()) {
                    storageSection.set(se.getKey().name().toLowerCase(Locale.ENGLISH), se.getValue());
                }
            }
        }

        return yaml;
    }

    public void ensurePlayerState(Player player) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        if (superiorPlayer == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());

        Island island = superiorPlayer.getIsland();
        if (island != null) {
            getIslandState(island.getUniqueId());
            showJourneyBossBar(superiorPlayer, playerState);
            checkAndAutoCompleteCurrentJourneyTask(superiorPlayer, playerState);
        } else {
            final SuperiorPlayer sp = superiorPlayer;
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!sp.hasIsland()) {
                        plugin.getGrid().createIsland(sp, "oneblock", BigDecimal.ZERO, Biome.PLAINS, "");
                    }
                }
            }, 20L);
        }
    }

    public void handlePostIslandCreate(PostIslandCreateEvent event) {
        Island island = event.getIsland();
        if (island == null || !"oneblock".equalsIgnoreCase(island.getSchematicName())) {
            return;
        }

        UUID islandId = island.getUniqueId();
        SuperiorPlayer superiorPlayer = event.getPlayer();
        pendingAnchorSetup.add(islandId);

        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!pendingAnchorSetup.contains(islandId)) {
                return;
            }
            Island freshIsland = plugin.getGrid().getIslandByUUID(islandId);
            if (freshIsland == null) {
                pendingAnchorSetup.remove(islandId);
                return;
            }
            placeOneBlockAnchor(freshIsland, superiorPlayer, islandId);
        }, 40L);
    }

    public void handleIslandHomeTeleport(IslandHomeTeleportEvent event) {
        Island island = event.getIsland();
        if (island == null || !"oneblock".equalsIgnoreCase(island.getSchematicName())) {
            return;
        }

        UUID islandId = island.getUniqueId();
        IslandState islandState = getIslandState(islandId);
        SuperiorPlayer teleportingPlayer = event.getPlayer();

        if (islandState.oneblockAnchor == null) {
            placeOneBlockAnchor(island, teleportingPlayer, islandId);
        } else {
            Location anchorLoc = islandState.oneblockAnchor.toLocation();
            if (anchorLoc != null && anchorLoc.getBlock().getType() == AIR_MATERIAL) {
                Material restoreMat = chooseOneBlockMaterial(islandState.oneblockBrokenTotal, 0);
                anchorLoc.getBlock().setType(restoreMat != null ? restoreMat : config.oneBlockFallbackMaterial, false);
                anchorLoc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK, false);
            }
        }

        if (teleportingPlayer != null) {
            showJourneyBossBar(teleportingPlayer, getPlayerState(teleportingPlayer.getUniqueId()));
        }

        // Bypass SSB2's safe-spot search — teleport directly to anchor top
        if (teleportingPlayer != null) {
            Player player = teleportingPlayer.asPlayer();
            if (player != null) {
                Location dest = null;
                if (islandState.oneblockAnchor != null) {
                    Location anchorLoc = islandState.oneblockAnchor.toLocation();
                    if (anchorLoc != null && anchorLoc.getWorld() != null) {
                        dest = anchorLoc.add(0, 1, 0);
                        dest.setYaw(player.getLocation().getYaw());
                        dest.setPitch(0f);
                    }
                }
                if (dest == null) {
                    dest = island.getIslandHome(event.getDimension());
                }
                if (dest != null) {
                    event.setCancelled(true);
                    player.teleport(dest);
                }
            }
        }
    }

    private void placeOneBlockAnchor(Island island, SuperiorPlayer superiorPlayer, UUID islandId) {
        Location anchorBase = resolveOneBlockAnchorBase(island, superiorPlayer);
        if (anchorBase == null || anchorBase.getWorld() == null) {
            return;
        }

        Location anchorLocation = anchorBase.clone();
        
        Location blockLoc = anchorLocation.clone().subtract(0, 1, 0);

        
        org.bukkit.World world = blockLoc.getWorld();
        int cx = blockLoc.getBlockX();
        int cy = blockLoc.getBlockY();
        int cz = blockLoc.getBlockZ();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = -2; dy <= 5; dy++) {
                    world.getBlockAt(cx + dx, cy + dy, cz + dz).setType(Material.AIR, false);
                }
            }
        }

        // Single BEDROCK block directly under the oneblock
        world.getBlockAt(blockLoc.getBlockX(), blockLoc.getBlockY() - 1, blockLoc.getBlockZ()).setType(Material.BEDROCK, false);

        // Place first configured oneblock material as the starter block
        WeightedMaterial starterWM = chooseOneBlockWeightedMaterial(0, 0, 0);
        Material starterMaterial = starterWM != null ? starterWM.material : config.oneBlockFallbackMaterial;
        Material starterDisplay = (starterWM != null && (starterWM.mobSpawn != null || UNSTABLE_BLOCK_TYPES.contains(starterMaterial)))
                ? config.oneBlockFallbackMaterial : starterMaterial;
        blockLoc.getBlock().setType(starterDisplay, false);

        IslandState islandState = getIslandState(islandId);
        islandState.oneblockAnchor = LocationKey.fromBlock(blockLoc.getBlock());

        // Pre-seed the drop so the first break gives an item
        if (starterWM != null && starterWM.dropOverride != null) {
            islandState.pendingDropOverride = new ItemStack(starterWM.dropOverride, starterWM.dropOverrideAmount);
        } else if (starterWM == null || starterWM.mobSpawn == null) {
            islandState.pendingDropOverride = new ItemStack(starterMaterial, 1);
        }

        island.setIslandHome(anchorBase);

        pendingAnchorSetup.remove(islandId);

        if (superiorPlayer != null) {
            send(superiorPlayer, msg("island-created"));
        }
    }

    public void handleIslandDisband(IslandDisbandEvent event) {
        UUID islandId = event.getIsland().getUniqueId();
        pendingAnchorSetup.remove(islandId);

        List<Player> onlineMembers = new ArrayList<>();
        for (SuperiorPlayer member : event.getIsland().getIslandMembers(true)) {
            Player p = member.asPlayer();
            if (p != null) onlineMembers.add(p);
        }

        islandStates.remove(islandId);

        generatorStates.entrySet().removeIf(e -> {
            if (!islandId.equals(e.getValue().islandId)) return false;
            placedBlocks.remove(e.getKey());
            removeGeneratorHologram(e.getValue().hologramUUID);
            return true;
        });

        nodeStates.entrySet().removeIf(e -> {
            if (!islandId.equals(e.getValue().islandId)) return false;
            placedBlocks.remove(e.getKey());
            BukkitTask t = nodeCooldownTasks.remove(e.getKey());
            if (t != null) t.cancel();
            return true;
        });

        spawnerStates.entrySet().removeIf(e -> {
            if (!islandId.equals(e.getValue().islandId)) return false;
            placedBlocks.remove(e.getKey());
            return true;
        });

        if (!onlineMembers.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player p : onlineMembers) {
                    if (p.isOnline()) p.performCommand("spawn");
                }
            }, 10L);
        }

        savePersistentState();
    }

    private void teleportPlayerToIslandHome(Player player, SuperiorPlayer superiorPlayer) {
        Island island = superiorPlayer.getIsland();
        if (island == null) {
            send(superiorPlayer, "&cYou don't have an island yet.");
            return;
        }

        if (!"oneblock".equalsIgnoreCase(island.getSchematicName())) {
            Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
            Location home = island.getIslandHome(dimension);
            if (home != null) player.teleport(home);
            return;
        }

        UUID islandId = island.getUniqueId();
        IslandState islandState = getIslandState(islandId);

        if (islandState.oneblockAnchor == null) {
            placeOneBlockAnchor(island, superiorPlayer, islandId);
        } else {
            Location anchorLoc = islandState.oneblockAnchor.toLocation();
            if (anchorLoc != null && anchorLoc.getBlock().getType() == AIR_MATERIAL) {
                Material restoreMat = chooseOneBlockMaterial(islandState.oneblockBrokenTotal, 0);
                anchorLoc.getBlock().setType(restoreMat != null ? restoreMat : config.oneBlockFallbackMaterial, false);
                anchorLoc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK, false);
            }
        }

        showJourneyBossBar(superiorPlayer, getPlayerState(player.getUniqueId()));

        Location dest = null;
        if (islandState.oneblockAnchor != null) {
            Location anchorLoc = islandState.oneblockAnchor.toLocation();
            if (anchorLoc != null && anchorLoc.getWorld() != null) {
                dest = anchorLoc.clone().add(0, 1, 0);
                dest.setYaw(player.getLocation().getYaw());
                dest.setPitch(0f);
            }
        }
        if (dest == null) {
            Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
            dest = island.getIslandHome(dimension);
        }
        if (dest == null) {
            dest = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorldDimension());
        }

        if (dest != null) {
            player.teleport(dest);
        }
    }

    private Location resolveOneBlockAnchorBase(Island island, SuperiorPlayer superiorPlayer) {
        Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();

        Location islandHome = island.getIslandHome(dimension);
        if (islandHome != null && islandHome.getWorld() != null) {
            return islandHome;
        }

        Location center = island.getCenter(dimension);
        if (center != null && center.getWorld() != null) {
            return center;
        }

        if (superiorPlayer != null) {
            Player player = superiorPlayer.asPlayer();
            if (player != null) {
                return player.getLocation();
            }
        }

        return null;
    }

    public void handleCommand(PlayerCommandPreprocessEvent event) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getPlayer());
        if (superiorPlayer == null) {
            return;
        }

        String message = event.getMessage();
        if (message == null || message.length() <= 1 || !message.startsWith("/")) {
            return;
        }

        String commandText = message.substring(1).trim();
        String[] parts = commandText.split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return;
        }

        String cmd = parts[0].toLowerCase(Locale.ENGLISH);

        
        
        switch (cmd) {
            case "island":
            case "is":
            case "islands": {
                String sub = parts.length >= 2 ? parts[1].toLowerCase(Locale.ENGLISH) : "";
                boolean isHomeCmd = sub.isEmpty() || sub.equals("home") || sub.equals("tp")
                        || sub.equals("go") || sub.equals("teleport");
                if (isHomeCmd) {
                    event.setCancelled(true);
                    teleportPlayerToIslandHome(event.getPlayer(), superiorPlayer);
                    return;
                }
                break;
            }
            case "skills":
                event.setCancelled(true);
                executeSkillsCommand(event.getPlayer(), superiorPlayer, parts);
                return;
            case "generator":
                event.setCancelled(true);
                executeGeneratorCommand(event.getPlayer(), superiorPlayer, parts);
                return;
            case "node":
                event.setCancelled(true);
                executeNodeCommand(event.getPlayer(), superiorPlayer, parts);
                return;
            case "spawner":
                event.setCancelled(true);
                executeSpawnerCommand(event.getPlayer(), superiorPlayer, parts);
                return;
            case "journey":
                event.setCancelled(true);
                executeJourneyCommand(event.getPlayer(), superiorPlayer, parts);
                return;
            case "treasure":
                event.setCancelled(true);
                executeTreasureCommand(event.getPlayer(), superiorPlayer, parts);
                return;
            case "daily":
                event.setCancelled(true);
                openDailiesMenu(superiorPlayer);
                return;
            case "treasurepool":
                event.setCancelled(true);
                executeTreasurePoolCommand(event.getPlayer(), superiorPlayer, parts);
                return;
            case "level":
                event.setCancelled(true);
                openPlayerLevelsMenu(superiorPlayer, 0);
                return;
            case "glimits":
                event.setCancelled(true);
                openGrowthLimitsMenu(superiorPlayer);
                return;
            default:
                break;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        progressJourney(superiorPlayer, playerState, JourneyType.USE_COMMAND, cmd, 1L);
    }

    public void handleBlockPlace(BlockPlaceEvent event) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getPlayer());
        if (superiorPlayer == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());

        ItemStack handItem = event.getItemInHand();
        LocationKey locationKey = LocationKey.fromBlock(event.getBlockPlaced());

        String nodeType = getItemTag(handItem, "NODE");
        if (nodeType != null) {
            placeNode(event, superiorPlayer, nodeType, locationKey);
            return;
        }

        String spawnerType = getItemTag(handItem, "SPAWNER");
        if (spawnerType != null) {
            placeSpawner(event, superiorPlayer, spawnerType, locationKey);
            return;
        }

        String generatorType = getItemTag(handItem, "GENERATOR");
        if (generatorType != null) {
            placeGenerator(event, superiorPlayer, generatorType, locationKey);
            return;
        }

        
        placedBlocks.add(locationKey);

        
        if (playerState.miningLevel <= 0) {
            playerState.miningLevel = 1;
        }
    }

    public void handleItemSpawn(org.bukkit.event.entity.ItemSpawnEvent event) {
        LocationKey key = LocationKey.fromLocation(event.getEntity().getLocation());
        if (suppressDropLocations.contains(key)) {
            event.setCancelled(true);
        }
    }

    public void handleBlockDamage(BlockDamageEvent event) {
        
    }

    public void handleBlockBreak(BlockBreakEvent event) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getPlayer());
        if (superiorPlayer == null) {
            return;
        }

        
        
        LocationKey locationKey = LocationKey.fromBlock(event.getBlock());

        NodeState hitNode = nodeStates.get(locationKey);
        if (hitNode != null) {
            NodeTypeDefinition hitNodeDef = config.nodeTypes.get(hitNode.typeKey);
            Material blockType = event.getBlock().getType();
            boolean validNodeBlock = blockType == Material.BEDROCK
                    || blockType == Material.NOTE_BLOCK
                    || (hitNodeDef != null && hitNodeDef.displayBlock != null && hitNodeDef.displayBlock == blockType);
            if (!validNodeBlock) {
                nodeStates.remove(locationKey);
                placedBlocks.remove(locationKey);
                BukkitTask staleTask = nodeCooldownTasks.remove(locationKey);
                if (staleTask != null) staleTask.cancel();
                hitNode = null;
            }
        }
        if (hitNode != null) {
            event.setCancelled(true);
            Island hitNodeIsland = plugin.getGrid().getIslandByUUID(hitNode.islandId);
            if (hitNodeIsland == null || (!superiorPlayer.equals(hitNodeIsland.getOwner()) && !hitNodeIsland.isMember(superiorPlayer) && !hitNodeIsland.isCoop(superiorPlayer))) {
                return;
            }
            final Block nodeBlock = event.getBlock();
            nodeBlock.setType(Material.AIR, false);
            if (event.getPlayer().isSneaking()) {
                nodeStates.remove(locationKey);
                placedBlocks.remove(locationKey);
                BukkitTask cooldownTask = nodeCooldownTasks.remove(locationKey);
                if (cooldownTask != null) cooldownTask.cancel();
                giveItemToPlayer(event.getPlayer(), createNodeItem(hitNode.typeKey, 1));
                NodeTypeDefinition reclaimedNodeDef = config.nodeTypes.get(hitNode.typeKey);
                String reclaimedNodeName = (reclaimedNodeDef != null) ? reclaimedNodeDef.displayName : hitNode.typeKey;
                send(superiorPlayer, msg("node-reclaimed").replace("%type%", reclaimedNodeName));
            } else {
                harvestNode(superiorPlayer, hitNode, locationKey);
                NodeTypeDefinition nodeDef = config.nodeTypes.get(hitNode.typeKey);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    NodeState current = nodeStates.get(locationKey);
                    if (current == null) return;
                    if (current.onCooldown) {
                        nodeBlock.setType(Material.BEDROCK);
                    } else if (nodeDef != null && nodeDef.nexoItemId != null) {
                        applyNexoNoteBlock(nodeBlock, nodeDef.nexoItemId);
                    } else if (nodeDef != null && nodeDef.displayBlock != null && nodeDef.displayBlock != AIR_MATERIAL) {
                        nodeBlock.setType(nodeDef.displayBlock);
                    } else {
                        nodeBlock.setType(Material.STONE);
                    }
                });
            }
            return;
        }

        SpawnerState hitSpawner = spawnerStates.get(locationKey);
        if (hitSpawner != null && event.getBlock().getType() != SPAWNER_MATERIAL) {
            spawnerStates.remove(locationKey);
            placedBlocks.remove(locationKey);
            hitSpawner = null;
        }
        if (hitSpawner != null) {
            event.setCancelled(true);
            Island hitSpawnerIsland = plugin.getGrid().getIslandByUUID(hitSpawner.islandId);
            if (hitSpawnerIsland == null || (!superiorPlayer.equals(hitSpawnerIsland.getOwner()) && !hitSpawnerIsland.isMember(superiorPlayer) && !hitSpawnerIsland.isCoop(superiorPlayer))) {
                return;
            }
            spawnerStates.remove(locationKey);
            placedBlocks.remove(locationKey);
            final Block spawnerBlock = event.getBlock();
            Bukkit.getScheduler().runTask(plugin, () -> spawnerBlock.setType(Material.AIR));
            giveItemToPlayer(event.getPlayer(), createSpawnerItem(hitSpawner.typeKey, 1));
            SpawnerTypeDefinition reclaimedSpDef = config.spawnerTypes.get(hitSpawner.typeKey);
            String reclaimedSpName = (reclaimedSpDef != null) ? reclaimedSpDef.displayName : hitSpawner.typeKey;
            send(superiorPlayer, msg("spawner-reclaimed").replace("%type%", reclaimedSpName));
            return;
        }

        Island locationIsland = plugin.getGrid().getIslandAt(event.getBlock().getLocation());
        if (locationIsland == null || (!superiorPlayer.equals(locationIsland.getOwner()) && !locationIsland.isMember(superiorPlayer) && !locationIsland.isCoop(superiorPlayer))) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        IslandState islandState = getIslandState(locationIsland.getUniqueId());

        GeneratorState removedGenerator = generatorStates.remove(locationKey);
        if (removedGenerator != null) {
            Material blockType = event.getBlock().getType();
            boolean isStale = blockType != menuMaterial("IRON_BLOCK", "IRON_ORE")
                    && blockType != menuMaterial("HAY_BLOCK", "SPONGE")
                    && blockType != menuMaterial("NETHER_BRICKS", "NETHER_BRICK");
            if (isStale) {
                placedBlocks.remove(locationKey);
                removedGenerator = null;
            }
        }
        if (removedGenerator != null) {
            event.setCancelled(true);
            placedBlocks.remove(locationKey);
            removeGeneratorHologram(removedGenerator.hologramUUID);
            final Block genBlock = event.getBlock();
            Bukkit.getScheduler().runTask(plugin, () -> genBlock.setType(Material.AIR));
            giveItemToPlayer(event.getPlayer(), createGeneratorItem(removedGenerator.trackKey, 1));
            send(superiorPlayer, msg("generator-reclaimed"));
            return;
        }

        if (isOneBlockAnchorBreak(event.getBlock(), islandState)) {
            event.setCancelled(true);
            final Material brokenType = event.getBlock().getType();
            ItemStack dropOverrideStack = islandState.pendingDropOverride;
            if (dropOverrideStack == null) {
                WeightedMaterial wmForCurrent = findWeightedMaterialForType(islandState.oneblockBrokenTotal, brokenType);
                if (wmForCurrent != null && wmForCurrent.dropOverride != null) {
                    dropOverrideStack = new ItemStack(wmForCurrent.dropOverride, wmForCurrent.dropOverrideAmount);
                }
            }
            if (dropOverrideStack != null) {
                islandState.pendingDropOverride = null;
            }
            processOneBlockBreak(superiorPlayer, playerState, locationIsland, islandState, event.getBlock(), brokenType);
            progressDailyChallenge(superiorPlayer, playerState, brokenType.name().toLowerCase(Locale.ENGLISH));
            if (dropOverrideStack != null) {
                giveItemToPlayer(event.getPlayer(), dropOverrideStack);
            }
            return;
        }

        boolean naturalBreak = !placedBlocks.remove(locationKey);
        if (!naturalBreak) {
            return;
        }

        Material blockMat = event.getBlock().getType();
        String blockTypeName = blockMat.name().toUpperCase(Locale.ENGLISH);
        boolean isCrop = isHarvestableCropBlock(event.getBlock());
        boolean isLog = blockTypeName.contains("LOG") || blockTypeName.contains("WOOD");
        SkillTrack breakTrack = isCrop || isLog ? SkillTrack.FARMING : SkillTrack.MINING;
        progressJourney(superiorPlayer, playerState, JourneyType.BREAK_BLOCK,
                blockMat.name().toLowerCase(Locale.ENGLISH), 1L);

        incrementDailyTaskProgress(superiorPlayer, playerState, DAILY_TASK_MINE_RESOURCES, 1L);
        if (isCrop) {
            incrementDailyTaskProgress(superiorPlayer, playerState, DAILY_TASK_HARVEST_CROPS, 1L);
        }

        if (isCrop && isCropFullyGrown(event.getBlock())) {
            incrementMilestoneProgressByBlock(superiorPlayer, playerState, blockMat);
            showMilestoneProgressBossBar(superiorPlayer, playerState, SkillTrack.FARMING, blockMat);
        } else if (isLog && islandState.grownTreeLogLocations.remove(locationKey)) {
            incrementMilestoneProgressByBlock(superiorPlayer, playerState, blockMat);
            showMilestoneProgressBossBar(superiorPlayer, playerState, SkillTrack.FARMING, blockMat);
        }

        rollFragmentDrop(superiorPlayer, playerState, blockMat.name().toLowerCase(Locale.ENGLISH), breakTrack, config.fragmentBaseChance);
        rollTreasureDrop(superiorPlayer, playerState, breakTrack,
            blockMat.name().toLowerCase(Locale.ENGLISH));
        progressDailyChallenge(superiorPlayer, playerState, blockMat.name().toLowerCase(Locale.ENGLISH));
    }

    public void handleBlockGrow(BlockGrowEvent event) {
        if (event.getBlock() == null) {
            return;
        }

        Island island = plugin.getGrid().getIslandAt(event.getBlock().getLocation());
        if (island == null || island.getOwner() == null) {
            return;
        }

        IslandState islandState = getIslandState(island.getUniqueId());
        String growthMaterialKey = event.getNewState().getType().name().toLowerCase(Locale.ENGLISH);
        int growthLimit = getGrowthLimit(islandState, growthMaterialKey);
        if (growthLimit > 0) {
            int todayCount = islandState.dailyGrowthCounts.getOrDefault(growthMaterialKey, 0);
            if (todayCount >= growthLimit) {
                event.setCancelled(true);
                return;
            }
            islandState.dailyGrowthCounts.put(growthMaterialKey, todayCount + 1);
        }

        placedBlocks.remove(LocationKey.fromBlock(event.getBlock()));

        PlayerState ownerState = getPlayerState(island.getOwner().getUniqueId());
        int cropGrowthPerk = ownerState.farmingPerkLevels[4];
        if (cropGrowthPerk <= 0) {
            return;
        }

        
        if (random.nextDouble() <= cropGrowthPerk * config.perkCropGrowthBonusPerLevel) {
            event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), org.bukkit.Effect.MOBSPAWNER_FLAMES, 0);
        }
    }

    public void handleStructureGrow(org.bukkit.event.world.StructureGrowEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getPlayer());
        if (superiorPlayer == null) {
            return;
        }

        Island island = superiorPlayer.getIsland();
        if (island != null) {
            IslandState islandState = getIslandState(island.getUniqueId());
            String speciesName = event.getSpecies().name();
            String saplingKey = TREE_TYPE_TO_SAPLING_KEY.containsKey(speciesName)
                    ? TREE_TYPE_TO_SAPLING_KEY.get(speciesName)
                    : speciesName.toLowerCase(Locale.ENGLISH) + "_sapling";
            int growthLimit = getGrowthLimit(islandState, saplingKey);
            if (growthLimit > 0) {
                int todayCount = islandState.dailyGrowthCounts.getOrDefault(saplingKey, 0);
                if (todayCount >= growthLimit) {
                    event.setCancelled(true);
                    return;
                }
                islandState.dailyGrowthCounts.put(saplingKey, todayCount + 1);
            }
            for (org.bukkit.block.BlockState blockState : event.getBlocks()) {
                String typeName = blockState.getType().name().toUpperCase(Locale.ENGLISH);
                if ((typeName.contains("LOG") || typeName.contains("WOOD"))
                        && islandState.grownTreeLogLocations.size() < IslandState.GROWN_LOCATION_CAP) {
                    islandState.grownTreeLogLocations.add(LocationKey.fromLocation(blockState.getLocation()));
                }
            }
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        progressJourney(superiorPlayer, playerState, JourneyType.USE_COMMAND, "tree_grow", 1L);
    }

    public void handleCreatureSpawn(CreatureSpawnEvent event) {
    }

    public void handleEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        if (spawnedByEvolvedSpawner.containsKey(event.getEntity().getUniqueId())) {
            event.blockList().clear();
        }
    }

    public void handleEntityDeath(EntityDeathEvent event) {
        
        spawnedByEvolvedSpawner.remove(event.getEntity().getUniqueId());

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(killer);
        if (superiorPlayer == null || !superiorPlayer.hasIsland()) {
            return;
        }

        Island playerIsland = superiorPlayer.getIsland();
        Island locationIsland = plugin.getGrid().getIslandAt(event.getEntity().getLocation());

        if (playerIsland == null || locationIsland == null || !Objects.equals(playerIsland.getUniqueId(), locationIsland.getUniqueId())) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        progressJourney(superiorPlayer, playerState, JourneyType.KILL_MOB,
                event.getEntityType().name().toLowerCase(Locale.ENGLISH), 1L);
        incrementDailyTaskProgress(superiorPlayer, playerState, DAILY_TASK_KILL_MOBS, 1L);
        progressDailyChallenge(superiorPlayer, playerState, event.getEntityType().name().toLowerCase(Locale.ENGLISH));
        incrementMilestoneProgressByEntity(superiorPlayer, playerState, event.getEntityType());
        showMilestoneProgressBossBar(superiorPlayer, playerState, event.getEntityType());
        rollFragmentDrop(superiorPlayer, playerState, event.getEntityType().name().toLowerCase(Locale.ENGLISH), SkillTrack.SLAYING, config.fragmentBaseChance);
        rollTreasureDrop(superiorPlayer, playerState, SkillTrack.SLAYING,
            event.getEntityType().name().toLowerCase(Locale.ENGLISH));
    }

    public void handleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        if (superiorPlayer == null) {
            return;
        }

        ItemStack item = event.getItem();
        if (item != null && item.getType() != AIR_MATERIAL) {
            String poolName = getItemTag(item, "TREASURE");
            if (poolName != null) {
                event.setCancelled(true);
                PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
                if (openTreasurePool(superiorPlayer, playerState, poolName)) {
                    consumeOneItem(item);
                }
                return;
            }

            String spawnerType = getItemTag(item, "SPAWNER");
            if (spawnerType != null) {
                if (tryPlaceSpawnerFromEgg(event, superiorPlayer, spawnerType)) {
                    consumeOneItem(item);
                }
                return;
            }

            String nodeType = getItemTag(item, "NODE");
            if (nodeType != null) {
                if (tryPlaceNodeFromInteract(event, superiorPlayer, nodeType)) {
                    consumeOneItem(item);
                }
                return;
            }
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        LocationKey locationKey = LocationKey.fromBlock(event.getClickedBlock());

        NodeState nodeState = nodeStates.get(locationKey);
        if (nodeState != null) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
            return;
        }

        
        

        GeneratorState generatorState = generatorStates.get(locationKey);
        if (generatorState != null) {
            event.setCancelled(true);
            Island playerIsland = superiorPlayer.getIsland();
            Island locationIsland = plugin.getGrid().getIslandAt(event.getClickedBlock().getLocation());
            boolean owns = (playerIsland != null && Objects.equals(generatorState.islandId, playerIsland.getUniqueId()))
                    || (locationIsland != null && Objects.equals(generatorState.islandId, locationIsland.getUniqueId())
                        && (locationIsland.isMember(superiorPlayer) || locationIsland.isCoop(superiorPlayer)));
            if (!owns) {
                send(superiorPlayer, msg("generator-wrong-island"));
                return;
            }
            openPerGeneratorMenu(superiorPlayer, locationKey);
        }
    }

    public void handleCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        if (superiorPlayer == null) {
            return;
        }

        
        String pendingKey = pendingNodeCrafts.remove(superiorPlayer.getUniqueId());
        if (pendingKey != null) {
            PlayerState ps = getPlayerState(superiorPlayer.getUniqueId());
            progressJourney(superiorPlayer, ps, JourneyType.CRAFT_ITEM, pendingKey, 1L);
            return;
        }

        
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (matrix.length == 9) {
            String fragmentKey = null;
            boolean allFragments = true;
            for (int i = 0; i < 9; i++) {
                ItemStack slot = matrix[i];
                if (slot == null || slot.getType() == AIR_MATERIAL) { allFragments = false; break; }
                String tag = getItemTag(slot, "FRAGMENT");
                if (tag == null) { allFragments = false; break; }
                if (fragmentKey == null) fragmentKey = tag.toLowerCase(Locale.ENGLISH);
                else if (!fragmentKey.equals(tag.toLowerCase(Locale.ENGLISH))) { allFragments = false; break; }
            }
            if (allFragments && fragmentKey != null) {
                String fragNexoId = FRAGMENT_NEXO_IDS.get(fragmentKey);
                String baseKey = fragNexoId != null ? fragNexoId.replace("_fragment", "") : fragmentKey;
                PlayerState fragState = getPlayerState(superiorPlayer.getUniqueId());
                NodeTypeDefinition nodeDef = config.nodeTypes.get(baseKey);
                if (nodeDef != null) {
                    progressJourney(superiorPlayer, fragState, JourneyType.CRAFT_ITEM, "node:" + nodeDef.key, 1L);
                    return;
                }
                SpawnerTypeDefinition spawnerDef = config.spawnerTypes.get(baseKey);
                if (spawnerDef != null) {
                    progressJourney(superiorPlayer, fragState, JourneyType.CRAFT_ITEM, "spawner:" + spawnerDef.key, 1L);
                    return;
                }
            }
        }

        
        if (event.isCancelled()) {
            return;
        }

        if (event.getRecipe() == null || event.getRecipe().getResult() == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());

        ItemStack result = event.getRecipe().getResult();
        int amount = Math.max(1, result.getAmount());

        addPlayerXp(superiorPlayer, playerState, config.xpPerCraft * amount);

        progressJourney(superiorPlayer, playerState, JourneyType.CRAFT_ITEM,
                result.getType().name().toLowerCase(Locale.ENGLISH), amount);
    }

    public void handleInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        openGeneratorMenuViewers.remove(player.getUniqueId());
        if (editingPoolKey.containsKey(player.getUniqueId())
                && !(event.getInventory().getHolder() instanceof EvolvedGuiHolder)) {
            savePoolFromInventory(player, event.getInventory());
        }
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof EvolvedGuiHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        int clickedSlot = event.getRawSlot();
        if (clickedSlot < 0 || clickedSlot >= event.getInventory().getSize()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        EvolvedGuiHolder holder = (EvolvedGuiHolder) event.getInventory().getHolder();
        if (!holder.viewerId.equals(player.getUniqueId())) {
            return;
        }

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        if (superiorPlayer == null) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        switch (holder.menuType) {
            case SKILL_HUB:
                handleSkillHubMenuClick(superiorPlayer, clickedSlot);
                break;
            case MILESTONES:
                handleMilestonesMenuClick(superiorPlayer, holder.skillTrack, clickedSlot, holder.page);
                break;
            case PERKS:
                handlePerksMenuClick(superiorPlayer, holder.skillTrack, clickedSlot);
                break;
            case LIMITS:
                handleLimitsMenuClick(superiorPlayer, holder.skillTrack, clickedSlot, holder.page);
                break;
            case GROWTH_LIMITS:
                handleGrowthLimitsMenuClick(superiorPlayer, clickedSlot);
                break;
            case FARMING_LIMITS:
                handleFarmingLimitsChoiceMenuClick(superiorPlayer, clickedSlot);
                break;
            case JOURNEY:
                handleJourneyMenuClick(superiorPlayer, clickedSlot, holder.page);
                break;
            case DAILIES:
                handleDailiesMenuClick(superiorPlayer, clickedSlot);
                break;
            case DAILY_CHALLENGES:
                handleDailyChallengesMenuClick(superiorPlayer, clickedSlot);
                break;
            case PLAYER_LEVELS:
                handlePlayerLevelsMenuClick(superiorPlayer, holder.page, clickedSlot);
                break;
            case GENERATOR:
                handleGeneratorMenuClick(superiorPlayer, event.isRightClick(), clickedSlot);
                break;
            case TREASURE_POOL_VIEW:
                handleTreasurePoolViewClick(superiorPlayer, clickedSlot);
                break;
            default:
                break;
        }
        if (!upgradeSoundPlayed.remove(player.getUniqueId())) {
            if (clickedItem != null && clickedItem.getType() != Material.AIR && !isFillerItem(clickedItem)) {
                playEffect(player, "sounds:ui_click", 0.3f, 1.0f);
            }
        }
    }

    public void executeSkillsCommand(CommandSender sender, SuperiorPlayer superiorPlayer, String[] args) {
        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());

        if (args.length == 1 || "menu".equalsIgnoreCase(args[1])) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        if ("status".equalsIgnoreCase(args[1])) {
            sendSkillsStatus(sender, superiorPlayer, playerState);
            return;
        }

        String action = args[1].toLowerCase(Locale.ENGLISH);

        if ("perks".equals(action)) {
            sendPerksStatus(sender, playerState);
            return;
        }

        if ("journey".equals(action)) {
            openJourneyMenu(superiorPlayer, 0);
            return;
        }

        if ("dailies".equals(action) || "daily".equals(action)) {
            openDailiesMenu(superiorPlayer);
            return;
        }

        if ("levels".equals(action) || "playerlevels".equals(action)) {
            openPlayerLevelsMenu(superiorPlayer, 0);
            return;
        }

        if ("perkup".equals(action)) {
            
            if (args.length < 4) {
                send(sender, "Usage: /skills perkup <mining|farming|slaying> <1-5> [amount]");
                return;
            }
            SkillTrack track = parseSkillTrack(args[2]);
            if (track == null) {
                send(sender, "Unknown skill: " + args[2] + ". Use: mining, farming, slaying.");
                return;
            }
            int perkIndex = parseInt(args[3], -1) - 1;
            if (perkIndex < 0 || perkIndex > 4) {
                send(sender, "Perk index must be 1-5.");
                return;
            }
            int amount = args.length >= 5 ? Math.max(1, parseInt(args[4], 1)) : 1;
            performPerkUpgrade(superiorPlayer, playerState, track, perkIndex, amount);
            return;
        }

        send(sender, "Usage: /" + plugin.getCommands().getLabel() + " skills [menu|status|journey|dailies|levels|perks|perkup <skill> <1-5> [amount]]");
    }

    public void executeOneBlockCommand(CommandSender sender, SuperiorPlayer superiorPlayer, String[] args) {
        if (!superiorPlayer.hasIsland()) {
            send(sender, "You must have an island to use one-block features.");
            return;
        }

        Island island = superiorPlayer.getIsland();
        if (island == null) {
            send(sender, "You must have an island to use one-block features.");
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        IslandState islandState = getIslandState(island.getUniqueId());

        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendOneBlockStatus(sender, islandState, playerState);
            return;
        }

        if ("claimdaily".equalsIgnoreCase(args[1])) {
            claimOneBlockDaily(superiorPlayer, playerState);
            return;
        }

        send(sender, "Usage: /" + plugin.getCommands().getLabel() + " oneblock [status|claimdaily]");
    }

    public void executeNodeCommand(CommandSender sender, SuperiorPlayer superiorPlayer, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "This command can only be used by players.");
            return;
        }

        if (!superiorPlayer.hasIsland()) {
            send(sender, "You must have an island to use node commands.");
            return;
        }

        
        if (args.length == 1) {
            openLimitsMenu(superiorPlayer, SkillTrack.MINING, 0);
            return;
        }

        Player player = (Player) sender;
        LocationKey target = getTargetBlockKey(player, 6);
        if (target == null) {
            send(sender, "Look at a node block first, then use /node status|harvest|upgrade.");
            return;
        }

        NodeState nodeState = nodeStates.get(target);
        if (nodeState == null) {
            send(sender, "The targeted block is not a node.");
            return;
        }

        Island island = plugin.getGrid().getIslandByUUID(nodeState.islandId);
        if (island == null || (!island.isMember(superiorPlayer) && !island.isCoop(superiorPlayer))) {
            send(sender, "You can only manage your island's nodes.");
            return;
        }

        if ("status".equalsIgnoreCase(args[1])) {
            sendNodeStatus(sender, nodeState);
            return;
        }

        if ("harvest".equalsIgnoreCase(args[1])) {
            harvestNode(superiorPlayer, nodeState, target);
            Location harvestLoc = target.toLocation();
            if (harvestLoc != null) {
                harvestLoc.getBlock().setType(Material.BEDROCK);
            }
            return;
        }

        if ("upgrade".equalsIgnoreCase(args[1])) {
            upgradeNode(superiorPlayer, nodeState);
            return;
        }

        send(sender, "Usage: /node | /node status|harvest|upgrade (look at a node)");
    }

    public void executeSpawnerCommand(CommandSender sender, SuperiorPlayer superiorPlayer, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "This command can only be used by players.");
            return;
        }

        if (!superiorPlayer.hasIsland()) {
            send(sender, "You must have an island to use spawner commands.");
            return;
        }

        Player player = (Player) sender;
        LocationKey target = getTargetBlockKey(player, 6);
        if (target == null) {
            send(sender, "Look at a spawner block first.");
            return;
        }

        SpawnerState spawnerState = spawnerStates.get(target);
        if (spawnerState == null) {
            send(sender, "The targeted block is not an evolved spawner.");
            return;
        }

        Island island = plugin.getGrid().getIslandByUUID(spawnerState.islandId);
        if (island == null || (!island.isMember(superiorPlayer) && !island.isCoop(superiorPlayer))) {
            send(sender, "You can only manage your island's spawners.");
            return;
        }

        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendSpawnerStatus(sender, spawnerState);
            return;
        }

        if ("harvest".equalsIgnoreCase(args[1])) {
            harvestSpawner(superiorPlayer, spawnerState, target);
            return;
        }

        if ("upgrade".equalsIgnoreCase(args[1])) {
            upgradeSpawner(superiorPlayer, spawnerState);
            return;
        }

        send(sender, "Usage: /" + plugin.getCommands().getLabel() + " spawner [status|harvest|upgrade]");
    }

    public void executeGeneratorCommand(CommandSender sender, SuperiorPlayer superiorPlayer, String[] args) {
        if (!superiorPlayer.hasIsland()) {
            send(sender, "You must have an island to use generator commands.");
            return;
        }

        Island island = superiorPlayer.getIsland();
        if (island == null) {
            send(sender, "You must have an island to use generator commands.");
            return;
        }

        UUID islandId = island.getUniqueId();

        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendGeneratorStatus(sender, islandId, null);
            return;
        }

        String trackKey = args[1].toLowerCase(Locale.ENGLISH);
        if (!config.generators.containsKey(trackKey)) {
            send(sender, "Unknown generator track: " + args[1] + ". Available: " + String.join(", ", config.generators.keySet()));
            return;
        }

        if (args.length == 2 || "status".equalsIgnoreCase(args[2])) {
            sendGeneratorStatus(sender, islandId, trackKey);
            return;
        }

        if ("claim".equalsIgnoreCase(args[2])) {
            boolean claimed = false;
            for (GeneratorState gs : generatorStates.values()) {
                if (gs.islandId.equals(islandId) && gs.trackKey.equalsIgnoreCase(trackKey)) {
                    claimGeneratorStorage(superiorPlayer, gs);
                    claimed = true;
                }
            }
            if (!claimed) send(superiorPlayer, msg("generator-storage-empty").replace("%track%", trackKey));
            return;
        }

        send(sender, "Usage: /" + plugin.getCommands().getLabel() + " generator [<mining|farming|slaying>] [status|claim]");
    }

    public void executeJourneyCommand(CommandSender sender, SuperiorPlayer superiorPlayer, String[] args) {
        if (args.length == 1 || "menu".equalsIgnoreCase(args[1])) {
            openJourneyMenu(superiorPlayer, 0);
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        sendJourneyStatus(sender, playerState);
    }

    public void executeTreasureCommand(CommandSender sender, SuperiorPlayer superiorPlayer, String[] args) {
        if (args.length < 3 || !"open".equalsIgnoreCase(args[1])) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " treasure open <pool>");
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        openTreasurePool(superiorPlayer, playerState, args[2]);
    }

    public void executeTreasurePoolCommand(Player player, SuperiorPlayer superiorPlayer, String[] args) {
        if (!player.isOp() && !player.hasPermission("evolvedskills.admin")) {
            send(player, "&cNo permission.");
            return;
        }
        if (args.length < 2) {
            send(player, "Usage: /treasurepool <save <key>|view>");
            return;
        }
        String sub = args[1].toLowerCase(Locale.ENGLISH);
        if ("save".equals(sub)) {
            if (args.length < 3) {
                send(player, "Usage: /treasurepool save <key>");
                return;
            }
            String poolKey = args[2].toLowerCase(Locale.ENGLISH);
            Block target = getTargetBlock(player, 5);
            if (target == null || !(target.getState() instanceof InventoryHolder)) {
                send(player, "&cLook at a chest.");
                return;
            }
            org.bukkit.inventory.Inventory chestInv = ((InventoryHolder) target.getState()).getInventory();
            List<Map<String, Object>> entries = new ArrayList<>();
            for (ItemStack item : chestInv.getContents()) {
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("weight", 1);
                entry.put("item", item.getType().name() + ":" + item.getAmount());
                entries.add(entry);
            }
            if (entries.isEmpty()) {
                send(player, "&cChest is empty.");
                return;
            }
            File cfgFile = new File(module.getModuleFolder(), "treasure/pools.yml");
            YamlConfiguration cfgYaml = YamlConfiguration.loadConfiguration(cfgFile);
            cfgYaml.set("pools." + poolKey + ".display-name", poolKey);
            cfgYaml.set("pools." + poolKey + ".entries", entries);
            try {
                cfgYaml.save(cfgFile);
                reloadConfiguration();
                send(player, "&aSaved pool &f" + poolKey + " &awith &f" + entries.size() + " &aentries.");
            } catch (IOException e) {
                send(player, "&cFailed to save config: " + e.getMessage());
            }
        } else if ("view".equals(sub)) {
            openTreasurePoolViewMenu(superiorPlayer);
        } else {
            send(player, "Usage: /treasurepool <save <key>|view>");
        }
    }

    private void openTreasurePoolViewMenu(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }
        int poolCount = config.treasurePools.size();
        int size = poolCount == 0 ? 9 : Math.min(54, ((poolCount + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(
            new EvolvedGuiHolder(GuiMenuType.TREASURE_POOL_VIEW, null, player.getUniqueId()),
            size, "\u00a7b\u1d1b\u0280\u1d07\u1d00\ua731\u1d1c\u0280\u1d07 \u1d18\u1d0f\u1d0f\u029f\ua731"
        );
        int slot = 0;
        for (TreasurePoolDefinition pool : config.treasurePools.values()) {
            if (slot >= size) {
                break;
            }
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Entries: \u00a7f" + pool.entries.size());
            for (WeightedReward wr : pool.entries) {
                StringBuilder line = new StringBuilder("\u00a78\u2022 \u00a77w:" + wr.weight);
                if (!wr.reward.items.isEmpty()) {
                    ItemStack ri = wr.reward.items.get(0);
                    line.append(" \u00a7f").append(ri.getType().name().toLowerCase(Locale.ENGLISH));
                    if (ri.getAmount() > 1) {
                        line.append("x").append(ri.getAmount());
                    }
                } else if (wr.reward.money > 0) {
                    line.append(" \u00a76$").append((int) wr.reward.money);
                } else if (wr.reward.skillXp > 0) {
                    line.append(" \u00a7bxp:").append(wr.reward.skillXp);
                }
                lore.add(line.toString());
            }
            inv.setItem(slot++, createMenuItem(Material.CHEST, "\u00a7b" + pool.displayName, lore));
        }
        player.openInventory(inv);
    }

    private void handleTreasurePoolViewClick(SuperiorPlayer superiorPlayer, int slot) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) return;
        List<TreasurePoolDefinition> poolList = new ArrayList<>(config.treasurePools.values());
        if (slot < 0 || slot >= poolList.size()) return;
        TreasurePoolDefinition pool = poolList.get(slot);
        Inventory editInv = Bukkit.createInventory(null, 54, "§8ᴇᴅɪᴛ ᴘᴏᴏʟ: §f" + pool.displayName);
        for (WeightedReward wr : pool.entries) {
            if (!wr.reward.items.isEmpty()) {
                editInv.addItem(wr.reward.items.get(0).clone());
            }
        }
        editingPoolKey.put(player.getUniqueId(), pool.key);
        player.openInventory(editInv);
    }

    private void savePoolFromInventory(Player player, Inventory inv) {
        String poolKey = editingPoolKey.remove(player.getUniqueId());
        if (poolKey == null) return;
        List<Map<String, Object>> entries = new ArrayList<>();
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("weight", 1);
            entry.put("item", item.getType().name() + ":" + item.getAmount());
            entries.add(entry);
        }
        File cfgFile = new File(module.getModuleFolder(), "treasure/pools.yml");
        YamlConfiguration cfgYaml = YamlConfiguration.loadConfiguration(cfgFile);
        String displayName = cfgYaml.getString("pools." + poolKey + ".display-name", poolKey);
        cfgYaml.set("pools." + poolKey + ".display-name", displayName);
        cfgYaml.set("pools." + poolKey + ".entries", entries.isEmpty() ? new ArrayList<>() : entries);
        try {
            cfgYaml.save(cfgFile);
            reloadConfiguration();
            send(player, "&aPool &f" + poolKey + " &asaved with &f" + entries.size() + " &aentries.");
        } catch (IOException e) {
            send(player, "&cFailed to save pool: " + e.getMessage());
        }
    }

    public void executeAdminCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " admin evolvedskills <reload|setanchor|givenode|givespawner|givegenerator|givetreasure|setskill|resetplayer>");
            return;
        }

        String action = args[2].toLowerCase(Locale.ENGLISH);

        switch (action) {
            case "reload":
                savePersistentState();
                reloadConfiguration();
                loadPersistentState();
                send(sender, "Reloaded evolved skills module configuration and state.");
                return;
            case "assignchallenges":
                assignedDailyChallengeKeys.clear();
                if (!config.dailyChallengePool.isEmpty()) {
                    List<String> poolKeys = new ArrayList<>();
                    for (DailyChallengeDefinition d : config.dailyChallengePool) poolKeys.add(d.key);
                    java.util.Collections.shuffle(poolKeys, random);
                    int count = Math.min(config.dailyChallengeCount, poolKeys.size());
                    for (int i = 0; i < count; i++) assignedDailyChallengeKeys.add(poolKeys.get(i));
                }
                send(sender, "Assigned " + assignedDailyChallengeKeys.size() + " daily challenges: " + assignedDailyChallengeKeys);
                return;
            case "status":
                handleAdminStatus(sender, args);
                return;
            case "setanchor":
                handleAdminSetAnchor(sender, args);
                return;
            case "givenode":
                handleAdminGiveNode(sender, args);
                return;
            case "givespawner":
                handleAdminGiveSpawner(sender, args);
                return;
            case "givegenerator":
                handleAdminGiveGenerator(sender, args);
                return;
            case "givetreasure":
                handleAdminGiveTreasure(sender, args);
                return;
            case "setskill":
                handleAdminSetSkill(sender, args);
                return;
            case "resetplayer":
                handleAdminResetPlayer(sender, args);
                return;
            case "cleargenerators": {
                int genCount = generatorStates.size();
                for (Map.Entry<LocationKey, GeneratorState> e : new ArrayList<>(generatorStates.entrySet())) {
                    removeGeneratorHologram(e.getValue().hologramUUID);
                    placedBlocks.remove(e.getKey());
                }
                generatorStates.clear();
                savePersistentState();
                send(sender, "Cleared &f" + genCount + " &7generator states and saved.");
                return;
            }
            default:
                send(sender, "Unknown action. Use reload, status, setanchor, givenode, givespawner, givegenerator, givetreasure, setskill, resetplayer, cleargenerators.");
        }
    }

    public List<String> tabCompleteAdmin(String[] args) {
        if (args.length == 3) {
            return filterStartsWith(args[2], asList("reload", "status", "setanchor", "givenode", "givespawner", "givegenerator", "givetreasure", "setskill", "resetplayer", "cleargenerators"));
        }

        if (args.length == 4 && ("givenode".equalsIgnoreCase(args[2]) || "givespawner".equalsIgnoreCase(args[2]) ||
                "givegenerator".equalsIgnoreCase(args[2]) || "givetreasure".equalsIgnoreCase(args[2]) ||
                "setskill".equalsIgnoreCase(args[2]) || "resetplayer".equalsIgnoreCase(args[2]) ||
                "status".equalsIgnoreCase(args[2]))) {
            return filterStartsWith(args[3], getOnlinePlayerNames());
        }

        if (args.length == 5 && "givenode".equalsIgnoreCase(args[2])) {
            return filterStartsWith(args[4], new ArrayList<>(config.nodeTypes.keySet()));
        }

        if (args.length == 5 && "givespawner".equalsIgnoreCase(args[2])) {
            return filterStartsWith(args[4], new ArrayList<>(config.spawnerTypes.keySet()));
        }

        if (args.length == 5 && "givegenerator".equalsIgnoreCase(args[2])) {
            return filterStartsWith(args[4], new ArrayList<>(config.generators.keySet()));
        }

        if (args.length == 5 && "givetreasure".equalsIgnoreCase(args[2])) {
            return filterStartsWith(args[4], new ArrayList<>(config.treasurePools.keySet()));
        }

        return Collections.emptyList();
    }

    private void scheduleTasks() {
        cancelTasks();

        int autosaveTicks = module.getConfiguration().getAutosaveSeconds() * 20;
        this.autosaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::savePersistentState, autosaveTicks, autosaveTicks);

        int nodeTicks = module.getConfiguration().getNodeTickSeconds() * 20;
        this.nodeTickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickNodes, nodeTicks, nodeTicks);

        int spawnerTicks = module.getConfiguration().getSpawnerTickSeconds() * 20;
        this.spawnerTickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickSpawners, spawnerTicks, spawnerTicks);

        int generatorTicks = module.getConfiguration().getGeneratorTickSeconds() * 20;
        this.generatorTickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickGenerators, generatorTicks, generatorTicks);

        this.dailyTickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkDailyReset, 1200L, 1200L);
        this.growthResetTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkGrowthReset, 100L, 100L);
        this.journeyBossBarRefreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, PlayerState> entry : playerStates.entrySet()) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p == null) continue;
                SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(p);
                if (sp == null || sp.getIsland() == null) continue;
                showJourneyBossBar(sp, entry.getValue());
            }
        }, 200L, 200L);
    }

    private void cancelTasks() {
        cancelTask(autosaveTask);
        cancelTask(nodeTickTask);
        cancelTask(spawnerTickTask);
        cancelTask(generatorTickTask);
        cancelTask(dailyTickTask);
        cancelTask(growthResetTask);
        cancelTask(journeyBossBarRefreshTask);
    }

    private void cancelTask(BukkitTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    private void reconcileLevelsFromExperience() {
        for (PlayerState playerState : this.playerStates.values()) {
            int targetMiningLevel = resolveLevelFromXp(playerState.miningXp, config.skillLevelThresholds, config.skillLevelStepXp);
            if (targetMiningLevel > playerState.miningLevel) {
                playerState.miningLevel = targetMiningLevel;
            }

            int targetFarmingLevel = resolveLevelFromXp(playerState.farmingXp, config.skillLevelThresholds, config.skillLevelStepXp);
            if (targetFarmingLevel > playerState.farmingLevel) {
                playerState.farmingLevel = targetFarmingLevel;
            }

            int targetSlayingLevel = resolveLevelFromXp(playerState.slayingXp, config.skillLevelThresholds, config.skillLevelStepXp);
            if (targetSlayingLevel > playerState.slayingLevel) {
                playerState.slayingLevel = targetSlayingLevel;
            }

            int targetPlayerLevel = resolveLevelFromXp(playerState.playerXp, config.playerLevelThresholds, config.playerLevelStepXp);
            if (targetPlayerLevel > playerState.playerLevel) {
                playerState.playerLevel = targetPlayerLevel;
            }
        }
    }

    private void processOneBlockBreak(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                      Island island, IslandState islandState, Block block) {
        processOneBlockBreak(superiorPlayer, playerState, island, islandState, block, block.getType());
    }

    private void processOneBlockBreak(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                      Island island, IslandState islandState, Block block, Material brokenType) {
        islandState.oneblockBrokenTotal += 1L;
        playerState.dailyOneblockBroken += 1;
        incrementDailyTaskProgress(superiorPlayer, playerState, DAILY_TASK_MINE_RESOURCES, 1L);

        progressJourney(superiorPlayer, playerState, JourneyType.ONEBLOCK_BREAK, null, 1L);

        String brokenTypeKey = brokenType.name().toLowerCase(Locale.ENGLISH);
        String brokenTypeName = brokenType.name().toUpperCase(Locale.ENGLISH);
        boolean isOneblockCrop = isHarvestableCropMaterial(brokenType);
        boolean isOneblockLog = brokenTypeName.contains("LOG") || brokenTypeName.contains("STEM");
        SkillTrack oneblockTrack = (isOneblockCrop || isOneblockLog) ? SkillTrack.FARMING : SkillTrack.MINING;
        rollFragmentDrop(superiorPlayer, playerState, brokenTypeKey, oneblockTrack, config.fragmentBaseChance);
        rollTreasureDrop(superiorPlayer, playerState, oneblockTrack, brokenTypeKey);

        incrementMilestoneProgressByBlock(superiorPlayer, playerState, brokenType);
        progressJourney(superiorPlayer, playerState, JourneyType.BREAK_BLOCK, brokenType.name().toLowerCase(Locale.ENGLISH), 1L);
        showMilestoneProgressBossBar(superiorPlayer, playerState, SkillTrack.MINING, brokenType);

        WeightedMaterial nextWM = chooseOneBlockWeightedMaterial(islandState.oneblockBrokenTotal, playerState.miningPerkLevels[0], playerState.slayingPerkLevels[0]);
        Material nextMaterial = (nextWM != null) ? nextWM.material : config.oneBlockFallbackMaterial;
        final String mobToSpawn = (nextWM != null) ? nextWM.mobSpawn : null;
        if (nextWM != null && nextWM.dropOverride != null) {
            islandState.pendingDropOverride = new ItemStack(nextWM.dropOverride, nextWM.dropOverrideAmount);
        } else if (mobToSpawn != null) {
            islandState.pendingDropOverride = null;
        } else {
            islandState.pendingDropOverride = new ItemStack(nextMaterial, 1);
        }
        islandState.pendingMobSpawn = null;

        
        final Material displayMaterial = (mobToSpawn != null || UNSTABLE_BLOCK_TYPES.contains(nextMaterial))
                ? config.oneBlockFallbackMaterial : nextMaterial;

        Bukkit.getScheduler().runTask(plugin, () -> {
            block.setType(displayMaterial, false);
            // After the block respawns, clip any player whose feet are inside it
            // upward to the surface so they land on top instead of being pushed sideways
            double safeY = block.getY() + 1.01;
            for (org.bukkit.entity.Entity e : block.getWorld().getNearbyEntities(
                    block.getLocation().add(0.5, 1.0, 0.5), 0.7, 1.2, 0.7)) {
                if (!(e instanceof Player)) continue;
                Location eLoc = e.getLocation();
                if (eLoc.getY() < safeY) {
                    ((Player) e).teleport(new Location(eLoc.getWorld(), eLoc.getX(), safeY, eLoc.getZ(), eLoc.getYaw(), eLoc.getPitch()));
                }
            }
            if (mobToSpawn != null) {
                spawnMobFromOneBlock(block, mobToSpawn, islandState);
            }
        });
    }

    private boolean isOneBlockAnchorBreak(Block block, IslandState islandState) {
        return islandState.oneblockAnchor != null && islandState.oneblockAnchor.matches(block.getLocation());
    }

    private void spawnMobFromOneBlock(Block block, String entityTypeName, IslandState islandState) {
        try {
            if (islandState != null && islandState.lastOneBlockMobEntityId != null) {
                try {
                    java.lang.reflect.Method getEntityByUUID = Class.forName("org.bukkit.Bukkit")
                            .getMethod("getEntity", UUID.class);
                    org.bukkit.entity.Entity existing = (org.bukkit.entity.Entity)
                            getEntityByUUID.invoke(null, islandState.lastOneBlockMobEntityId);
                    if (existing != null && !existing.isDead()) return;
                } catch (Exception ignored) {}
            }
            EntityType entityType = EnumHelper.getEnum(EntityType.class, entityTypeName.toUpperCase(Locale.ENGLISH));
            if (entityType == null || !entityType.isAlive()) {
                plugin.getLogger().warning("[EvolvedSkills] Unknown or non-living entity type for oneblock spawn: " + entityTypeName);
                return;
            }
            Location spawnLoc = block.getLocation().add(0.5, 1.0, 0.5);
            org.bukkit.entity.Entity spawned = block.getWorld().spawnEntity(spawnLoc, entityType);
            if (islandState != null) {
                islandState.lastOneBlockMobEntityId = spawned.getUniqueId();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[EvolvedSkills] Failed to spawn mob " + entityTypeName + ": " + e.getMessage());
        }
    }

    private void applyNexoNoteBlock(Block block, String nexoItemId) {
        try {
            Class<?> factoryClass = Class.forName("com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory");
            Object companion = factoryClass.getField("Companion").get(null);
            companion.getClass().getMethod("setBlockModel", Block.class, String.class).invoke(companion, block, nexoItemId);
        } catch (Exception e) {
            plugin.getLogger().warning("[EvolvedSkills] applyNexoNoteBlock failed for " + nexoItemId + ": " + e.getMessage());
        }
    }

    private void placeNode(BlockPlaceEvent event, SuperiorPlayer superiorPlayer, String nodeTypeKey, LocationKey locationKey) {
        NodeTypeDefinition definition = config.nodeTypes.get(nodeTypeKey.toLowerCase(Locale.ENGLISH));
        if (definition == null) {
            event.setCancelled(true);
            send(event.getPlayer(), "Unknown node type: " + nodeTypeKey + ".");
            return;
        }

        if (!superiorPlayer.hasIsland()) {
            event.setCancelled(true);
            send(event.getPlayer(), "You must have an island to place nodes.");
            return;
        }

        Island island = superiorPlayer.getIsland();
        if (island == null) {
            send(event.getPlayer(), "You must have an island to place nodes.");
            event.setCancelled(true);
            return;
        }

        Island locationIsland = plugin.getGrid().getIslandAt(event.getBlockPlaced().getLocation());
        if (locationIsland == null || !Objects.equals(island.getUniqueId(), locationIsland.getUniqueId())) {
            event.setCancelled(true);
            send(event.getPlayer(), "Nodes can only be placed on your island.");
            return;
        }

        if (nodeStates.containsKey(locationKey) || spawnerStates.containsKey(locationKey)) {
            event.setCancelled(true);
            send(event.getPlayer(), "This location is already occupied by an evolved object.");
            return;
        }

        String normalizedNodeTypeKey = nodeTypeKey.toLowerCase(Locale.ENGLISH);
        IslandState islandState = getIslandState(island.getUniqueId());
        int currentPlacements = countNodeTypePlacements(island.getUniqueId(), normalizedNodeTypeKey);
        int placementLimit = getNodePlacementLimit(islandState, normalizedNodeTypeKey);
        if (currentPlacements >= placementLimit) {
            event.setCancelled(true);
            send(event.getPlayer(), "You reached the placement limit for " + definition.displayName + " (&f" + placementLimit + "&7). Open Mining/Farming Limits to upgrade.");
            return;
        }

        NodeState nodeState = new NodeState();
        nodeState.islandId = island.getUniqueId();
        nodeState.typeKey = normalizedNodeTypeKey;
        nodeState.level = 1;
        nodeState.progress = 0D;
        nodeState.stored = 0;

        nodeStates.put(locationKey, nodeState);
        placedBlocks.add(locationKey);

        if (definition.nexoItemId != null) {
            applyNexoNoteBlock(event.getBlockPlaced(), definition.nexoItemId);
        } else if (definition.displayBlock != null && definition.displayBlock != AIR_MATERIAL) {
            event.getBlockPlaced().setType(definition.displayBlock);
        }

        send(event.getPlayer(), "Placed node: " + definition.displayName + ".");
    }

    private void placeSpawner(BlockPlaceEvent event, SuperiorPlayer superiorPlayer, String spawnerTypeKey, LocationKey locationKey) {
        if (!placeSpawnerAt(superiorPlayer, event.getPlayer(), spawnerTypeKey, locationKey, event.getBlockPlaced())) {
            event.setCancelled(true);
        }
    }

    private boolean tryPlaceSpawnerFromEgg(PlayerInteractEvent event, SuperiorPlayer superiorPlayer, String spawnerTypeKey) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getBlockFace() == null) {
            return false;
        }
        event.setCancelled(true);

        Block targetBlock = event.getClickedBlock().getRelative(event.getBlockFace());
        if (targetBlock.getType() != AIR_MATERIAL) {
            send(event.getPlayer(), "You can only place a spawner egg in an empty block space.");
            return false;
        }

        LocationKey locationKey = LocationKey.fromBlock(targetBlock);
        return placeSpawnerAt(superiorPlayer, event.getPlayer(), spawnerTypeKey, locationKey, targetBlock);
    }

    private boolean tryPlaceNodeFromInteract(PlayerInteractEvent event, SuperiorPlayer superiorPlayer, String nodeTypeKey) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getBlockFace() == null) {
            return false;
        }
        event.setCancelled(true);

        Block targetBlock = event.getClickedBlock().getRelative(event.getBlockFace());
        if (targetBlock.getType() != AIR_MATERIAL) {
            send(event.getPlayer(), "You can only place a node in an empty block space.");
            return false;
        }

        NodeTypeDefinition definition = config.nodeTypes.get(nodeTypeKey.toLowerCase(Locale.ENGLISH));
        if (definition == null) {
            send(event.getPlayer(), "Unknown node type: " + nodeTypeKey + ".");
            return false;
        }

        if (!superiorPlayer.hasIsland()) {
            send(event.getPlayer(), "You must have an island to place nodes.");
            return false;
        }

        Island island = superiorPlayer.getIsland();
        if (island == null) {
            send(event.getPlayer(), "You must have an island to place nodes.");
            return false;
        }

        Island locationIsland = plugin.getGrid().getIslandAt(targetBlock.getLocation());
        if (locationIsland == null || !Objects.equals(island.getUniqueId(), locationIsland.getUniqueId())) {
            send(event.getPlayer(), "Nodes can only be placed on your island.");
            return false;
        }

        LocationKey locationKey = LocationKey.fromBlock(targetBlock);

        if (nodeStates.containsKey(locationKey) || spawnerStates.containsKey(locationKey)) {
            send(event.getPlayer(), "This location is already occupied by an evolved object.");
            return false;
        }

        String normalizedNodeTypeKey = nodeTypeKey.toLowerCase(Locale.ENGLISH);
        IslandState islandState = getIslandState(island.getUniqueId());
        int currentPlacements = countNodeTypePlacements(island.getUniqueId(), normalizedNodeTypeKey);
        int placementLimit = getNodePlacementLimit(islandState, normalizedNodeTypeKey);
        if (currentPlacements >= placementLimit) {
            send(event.getPlayer(), "You reached the placement limit for " + definition.displayName + " (&f" + placementLimit + "&7). Open Mining/Farming Limits to upgrade.");
            return false;
        }

        if (definition.nexoItemId != null) {
            applyNexoNoteBlock(targetBlock, definition.nexoItemId);
        } else if (definition.displayBlock != null && definition.displayBlock != AIR_MATERIAL) {
            targetBlock.setType(definition.displayBlock);
        }

        NodeState nodeState = new NodeState();
        nodeState.islandId = island.getUniqueId();
        nodeState.typeKey = normalizedNodeTypeKey;
        nodeState.level = 1;
        nodeState.progress = 0D;
        nodeState.stored = 0;

        nodeStates.put(locationKey, nodeState);
        placedBlocks.add(locationKey);

        send(event.getPlayer(), "Placed node: " + definition.displayName + ".");
        return true;
    }

    private boolean placeSpawnerAt(SuperiorPlayer superiorPlayer, CommandSender feedbackSender,
                                   String spawnerTypeKey, LocationKey locationKey, Block placedBlock) {
        SpawnerTypeDefinition definition = config.spawnerTypes.get(spawnerTypeKey.toLowerCase(Locale.ENGLISH));
        if (definition == null) {
            send(feedbackSender, "Unknown spawner type: " + spawnerTypeKey + ".");
            return false;
        }

        if (!superiorPlayer.hasIsland()) {
            send(feedbackSender, "You must have an island to place spawners.");
            return false;
        }

        Island island = superiorPlayer.getIsland();
        if (island == null) {
            send(feedbackSender, "You must have an island to place spawners.");
            return false;
        }

        Island locationIsland = plugin.getGrid().getIslandAt(placedBlock.getLocation());
        if (locationIsland == null || !Objects.equals(island.getUniqueId(), locationIsland.getUniqueId())) {
            send(feedbackSender, "Spawners can only be placed on your island.");
            return false;
        }

        if (nodeStates.containsKey(locationKey) || spawnerStates.containsKey(locationKey)) {
            send(feedbackSender, "This location is already occupied by an evolved object.");
            return false;
        }

        String normalizedSpawnerTypeKey = spawnerTypeKey.toLowerCase(Locale.ENGLISH);
        IslandState islandState = getIslandState(island.getUniqueId());
        int currentPlacements = countSpawnerTypePlacements(island.getUniqueId(), normalizedSpawnerTypeKey);
        int placementLimit = getSpawnerPlacementLimit(islandState, normalizedSpawnerTypeKey);
        if (currentPlacements >= placementLimit) {
            send(feedbackSender, "You reached the placement limit for " + definition.displayName + " (&f" + placementLimit + "&7). Open Slaying Limits to upgrade.");
            return false;
        }

        if (SPAWNER_MATERIAL != null) {
            placedBlock.setType(SPAWNER_MATERIAL);
        }

        if (placedBlock.getState() instanceof CreatureSpawner) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) placedBlock.getState();
            creatureSpawner.setSpawnedType(definition.entityType);
            creatureSpawner.setDelay(Integer.MAX_VALUE);
            creatureSpawner.update(true, false);
        }

        SpawnerState spawnerState = new SpawnerState();
        spawnerState.islandId = island.getUniqueId();
        spawnerState.typeKey = normalizedSpawnerTypeKey;
        spawnerState.level = 1;
        spawnerState.progress = 0D;
        spawnerState.stored = 0;

        spawnerStates.put(locationKey, spawnerState);
        placedBlocks.add(locationKey);

        send(feedbackSender, "Placed spawner: " + definition.displayName + ".");
        return true;
    }

    private void placeGenerator(BlockPlaceEvent event, SuperiorPlayer superiorPlayer,
                                String trackKey, LocationKey locationKey) {
        if (!superiorPlayer.hasIsland()) {
            event.setCancelled(true);
            send(event.getPlayer(), "You must have an island to place generators.");
            return;
        }
        Island island = superiorPlayer.getIsland();
        Island locationIsland = plugin.getGrid().getIslandAt(event.getBlockPlaced().getLocation());
        if (island == null || locationIsland == null
                || !Objects.equals(island.getUniqueId(), locationIsland.getUniqueId())) {
            event.setCancelled(true);
            send(event.getPlayer(), "Generators can only be placed on your island.");
            return;
        }
        if (generatorStates.containsKey(locationKey) || nodeStates.containsKey(locationKey)
                || spawnerStates.containsKey(locationKey)) {
            event.setCancelled(true);
            send(event.getPlayer(), "This location is already occupied.");
            return;
        }

        String normalizedTrack = trackKey.toLowerCase(Locale.ENGLISH);
        GeneratorSettings genSettings = config.generators.get(normalizedTrack);
        if (genSettings != null) {
            SkillTrack genTrack = parseSkillTrackOrMining(normalizedTrack);
            PlayerState ownerState = getPlayerState(superiorPlayer.getUniqueId());
            int limitPerk = getPerkLevels(ownerState, genTrack)[3];
            int maxPlacements = genSettings.baseMaxPlacements + limitPerk * genSettings.maxPlacementsPerPerkLevel;
            if (countGeneratorTypePlacements(island.getUniqueId(), normalizedTrack) >= maxPlacements) {
                event.setCancelled(true);
                String trackDisplay = normalizedTrack.substring(0, 1).toUpperCase(Locale.ENGLISH) + normalizedTrack.substring(1);
                send(event.getPlayer(), "You've reached the " + trackDisplay + " Generator limit (&f" + maxPlacements + "&7). Upgrade your Generator Limit perk.");
                return;
            }
        }

        GeneratorState state = new GeneratorState();
        state.islandId = island.getUniqueId();
        state.trackKey = trackKey.toLowerCase(Locale.ENGLISH);

        Location hologramLoc = event.getBlockPlaced().getLocation().add(0.5, 0.25, 0.5);
        state.hologramUUID = spawnGeneratorHologram(hologramLoc, trackKey);

        generatorStates.put(locationKey, state);
        placedBlocks.add(locationKey);
        send(superiorPlayer, trackKey.substring(0, 1).toUpperCase(Locale.ENGLISH)
            + trackKey.substring(1) + " Generator placed.");
    }

    private UUID spawnGeneratorHologram(Location location, String trackKey) {
        String trackDisplay = trackKey.substring(0, 1).toUpperCase(Locale.ENGLISH) + trackKey.substring(1);
        try {
            org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand)
                location.getWorld().spawnEntity(location, org.bukkit.entity.EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setCustomName(color("&e" + trackDisplay + " Generator"));
            stand.setCustomNameVisible(true);
            return stand.getUniqueId();
        } catch (Exception e) {
            return null;
        }
    }

    private void removeGeneratorHologram(UUID hologramUUID) {
        if (hologramUUID == null) return;
        for (World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (hologramUUID.equals(entity.getUniqueId())) {
                    entity.remove();
                    return;
                }
            }
        }
    }

    private int countGeneratorTypePlacements(UUID islandId, String trackKey) {
        int count = 0;
        for (GeneratorState gs : generatorStates.values()) {
            if (islandId.equals(gs.islandId) && trackKey.equalsIgnoreCase(gs.trackKey)) count++;
        }
        return count;
    }

    private SkillTrack parseSkillTrackOrMining(String trackKey) {
        if ("farming".equalsIgnoreCase(trackKey)) return SkillTrack.FARMING;
        if ("slaying".equalsIgnoreCase(trackKey)) return SkillTrack.SLAYING;
        return SkillTrack.MINING;
    }

    private void harvestNode(SuperiorPlayer superiorPlayer, NodeState nodeState, LocationKey locationKey) {
        Location nodeLoc = locationKey.toLocation();
        Island island = nodeLoc != null ? plugin.getGrid().getIslandAt(nodeLoc) : null;
        if (island == null || (!island.isMember(superiorPlayer) && !island.isCoop(superiorPlayer))) {
            send(superiorPlayer, msg("node-not-your-island"));
            return;
        }

        NodeTypeDefinition definition = config.nodeTypes.get(nodeState.typeKey);
        if (definition == null) {
            send(superiorPlayer, msg("node-type-unconfigured"));
            return;
        }

        if (nodeState.onCooldown) {
            long remaining = Math.max(0L, (nodeState.cooldownEndMillis - System.currentTimeMillis()) / 1000L);
            send(superiorPlayer, msg("node-on-cooldown").replace("%seconds%", String.valueOf(remaining)));
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());

        superiorPlayer.runIfOnline(player -> {
            giveItemToPlayer(player, new ItemStack(definition.outputMaterial, 1));
            playEffect(player, "sounds:item_pick_up", 0.3f, 1.0f);
        });

        SkillTrack nodeTrack = definition.skillTrack != null ? definition.skillTrack : SkillTrack.MINING;
        rollFragmentDrop(superiorPlayer, playerState, definition.key, nodeTrack, definition.fragmentChance);
        rollTreasureDrop(superiorPlayer, playerState, nodeTrack, definition.outputMaterial.name().toLowerCase(Locale.ENGLISH));
        incrementMilestoneProgressByBlock(superiorPlayer, playerState, definition.outputMaterial);
        incrementDailyTaskProgress(superiorPlayer, playerState, DAILY_TASK_MINE_RESOURCES, 1L);
        progressDailyChallenge(superiorPlayer, playerState, definition.outputMaterial.name().toLowerCase(Locale.ENGLISH));
        progressJourney(superiorPlayer, playerState, JourneyType.BREAK_BLOCK, definition.outputMaterial.name().toLowerCase(Locale.ENGLISH), 1L);
        showMilestoneProgressBossBar(superiorPlayer, playerState, nodeTrack, definition.outputMaterial);

        nodeState.onCooldown = true;
        nodeState.cooldownEndMillis = System.currentTimeMillis() + (definition.cooldownTicks * 50L);
        nodeStates.put(locationKey, nodeState);

        
        BukkitTask existingTask = nodeCooldownTasks.remove(locationKey);
        if (existingTask != null) {
            existingTask.cancel();
        }

        
        Material restoreMaterial = definition.displayBlock != null ? definition.displayBlock : Material.AIR;
        String restoreItemId = definition.nexoItemId;
        BukkitTask cooldownTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            nodeCooldownTasks.remove(locationKey);
            NodeState current = nodeStates.get(locationKey);
            if (current != null) {
                current.onCooldown = false;
                nodeStates.put(locationKey, current);
            }
            Location restoreLoc = locationKey.toLocation();
            if (restoreLoc != null) {
                Block restoreBlock = restoreLoc.getBlock();
                if (restoreItemId != null) {
                    applyNexoNoteBlock(restoreBlock, restoreItemId);
                } else {
                    restoreBlock.setType(restoreMaterial);
                }
            }
        }, definition.cooldownTicks);
        nodeCooldownTasks.put(locationKey, cooldownTask);
    }

    private void harvestSpawner(SuperiorPlayer superiorPlayer, SpawnerState spawnerState, LocationKey locationKey) {
        Island island = plugin.getGrid().getIslandByUUID(spawnerState.islandId);
        if (island == null || (!island.isMember(superiorPlayer) && !island.isCoop(superiorPlayer))) {
            send(superiorPlayer, msg("not-your-island"));
            return;
        }

        SpawnerTypeDefinition definition = config.spawnerTypes.get(spawnerState.typeKey);
        if (definition == null) {
            send(superiorPlayer, msg("spawner-type-unconfigured"));
            return;
        }

        if (spawnerState.stored <= 0) {
            send(superiorPlayer, msg("spawner-no-drops"));
            return;
        }

        int amount = spawnerState.stored;

        superiorPlayer.runIfOnline(player -> {
            giveItemToPlayer(player, new ItemStack(definition.outputMaterial, amount));
            send(player, msg("spawner-harvested")
                .replace("%amount%", String.valueOf(amount))
                .replace("%material%", prettyMaterial(definition.outputMaterial))
                .replace("%name%", definition.displayName));
        });

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        rollFragmentDrop(superiorPlayer, playerState, definition.key, SkillTrack.SLAYING, definition.fragmentChance);

        spawnerState.stored = 0;
        spawnerState.progress = 0D;
        spawnerStates.put(locationKey, spawnerState);
    }

    private void upgradeNode(SuperiorPlayer superiorPlayer, NodeState nodeState) {
        NodeTypeDefinition definition = config.nodeTypes.get(nodeState.typeKey);
        if (definition == null) {
            send(superiorPlayer, msg("node-type-unconfigured"));
            return;
        }

        if (nodeState.level >= definition.maxLevel) {
            send(superiorPlayer, msg("node-max-level"));
            return;
        }

        double cost = definition.upgradeBaseCost * Math.pow(definition.upgradeCostMultiplier, nodeState.level - 1);
        if (!withdrawMoney(superiorPlayer, cost)) {
            send(superiorPlayer, msg("node-upgrade-insufficient-funds").replace("%cost%", format(cost)));
            return;
        }

        nodeState.level += 1;
        send(superiorPlayer, msg("node-upgraded").replace("%level%", String.valueOf(nodeState.level)));
    }

    private void upgradeSpawner(SuperiorPlayer superiorPlayer, SpawnerState spawnerState) {
        SpawnerTypeDefinition definition = config.spawnerTypes.get(spawnerState.typeKey);
        if (definition == null) {
            send(superiorPlayer, msg("spawner-type-unconfigured"));
            return;
        }

        if (spawnerState.level >= definition.maxLevel) {
            send(superiorPlayer, msg("spawner-max-level"));
            return;
        }

        double cost = definition.upgradeBaseCost * Math.pow(definition.upgradeCostMultiplier, spawnerState.level - 1);
        if (!withdrawMoney(superiorPlayer, cost)) {
            send(superiorPlayer, msg("spawner-upgrade-insufficient-funds").replace("%cost%", format(cost)));
            return;
        }

        spawnerState.level += 1;
        send(superiorPlayer, msg("spawner-upgraded").replace("%level%", String.valueOf(spawnerState.level)));
    }

    private void claimOneBlockDaily(SuperiorPlayer superiorPlayer, PlayerState playerState) {
        if (playerState.dailyOneblockClaimed) {
            send(superiorPlayer, msg("daily-oneblock-already-claimed"));
            return;
        }

        if (playerState.dailyOneblockBroken < config.dailyOneBlockTargetBreaks) {
            send(superiorPlayer, msg("daily-oneblock-requirement-not-met")
                .replace("%target%", String.valueOf(config.dailyOneBlockTargetBreaks))
                .replace("%current%", String.valueOf(playerState.dailyOneblockBroken)));
            return;
        }

        playerState.dailyOneblockClaimed = true;
        applyReward(superiorPlayer, playerState, config.dailyOneBlockReward,
                "Claimed one-block daily reward.", SkillTrack.MINING);
    }

    private void incrementDailyTaskProgress(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                            String taskKey, long amount) {
        if (amount <= 0L) {
            return;
        }

        DailyTaskDefinition definition = config.dailyTasks.get(taskKey);
        if (definition == null || definition.stageTargets.isEmpty()) {
            return;
        }

        long previousProgress = playerState.dailyTaskProgress.getOrDefault(taskKey, 0L);
        long maxProgress = definition.stageTargets.get(definition.stageTargets.size() - 1);
        long newProgress = Math.min(maxProgress, previousProgress + amount);

        if (newProgress == previousProgress) {
            return;
        }

        playerState.dailyTaskProgress.put(taskKey, newProgress);

        int claimedStage = playerState.claimedDailyTaskStages.getOrDefault(taskKey, 0);
        int previousReachedStage = getReachedDailyTaskStage(definition, previousProgress);
        int reachedStage = getReachedDailyTaskStage(definition, newProgress);

        if (reachedStage > previousReachedStage && reachedStage > claimedStage) {
            send(superiorPlayer, msg("daily-task-stage-ready")
                .replace("%task%", definition.displayName)
                .replace("%stage%", String.valueOf(claimedStage + 1)));
        }
    }

    private void progressDailyChallenge(SuperiorPlayer superiorPlayer, PlayerState playerState, String resourceKey) {
        String lowerKey = resourceKey.toLowerCase(Locale.ENGLISH);
        for (String challengeKey : assignedDailyChallengeKeys) {
            DailyChallengeDefinition def = null;
            for (DailyChallengeDefinition d : config.dailyChallengePool) {
                if (d.key.equals(challengeKey)) { def = d; break; }
            }
            if (def == null || !def.matchKey.equals(lowerKey)) continue;
            if (playerState.claimedChallenges.contains(challengeKey)) continue;
            long prev = playerState.challengeProgress.getOrDefault(challengeKey, 0L);
            if (prev >= def.target) continue;
            long next = prev + 1L;
            playerState.challengeProgress.put(challengeKey, next);
            if (next >= def.target) {
                send(superiorPlayer, msg("challenge-complete").replace("%name%", def.displayName));
            }
        }
    }

    private boolean claimNextDailyTaskStage(SuperiorPlayer superiorPlayer, PlayerState playerState, String taskKey) {
        DailyTaskDefinition definition = config.dailyTasks.get(taskKey);
        if (definition == null || definition.stageTargets.isEmpty()) {
            send(superiorPlayer, msg("daily-task-unconfigured"));
            return false;
        }

        int claimedStage = playerState.claimedDailyTaskStages.getOrDefault(taskKey, 0);
        int maxStage = definition.stageTargets.size();
        if (claimedStage >= maxStage) {
            send(superiorPlayer, msg("daily-all-claimed"));
            return false;
        }

        long progress = playerState.dailyTaskProgress.getOrDefault(taskKey, 0L);
        int reachedStage = getReachedDailyTaskStage(definition, progress);
        if (reachedStage <= claimedStage) {
            long target = definition.stageTargets.get(claimedStage);
            long remaining = Math.max(0L, target - progress);
            send(superiorPlayer, msg("daily-stage-not-ready")
                .replace("%task%", definition.displayName)
                .replace("%remaining%", String.valueOf(remaining)));
            return false;
        }

        int nextStage = claimedStage + 1;
        Reward reward = createDailyTaskStageReward(definition, nextStage);
        playerState.claimedDailyTaskStages.put(taskKey, nextStage);
        SkillTrack dailyTrack = dailyTaskKeyToSkillTrack(taskKey);
        applyReward(superiorPlayer, playerState, reward,
                "Claimed daily stage " + nextStage + " for " + definition.displayName + ".", dailyTrack);
        superiorPlayer.runIfOnline(p -> playEffect(p, "sounds:upgrade", 1.0f, 1.2f));
        progressJourney(superiorPlayer, playerState, JourneyType.COMPLETE_DAILY, taskKey, 1L);
        return true;
    }

    private int getReachedDailyTaskStage(DailyTaskDefinition definition, long progress) {
        int reached = 0;
        for (int index = 0; index < definition.stageTargets.size(); index++) {
            if (progress >= definition.stageTargets.get(index)) {
                reached = index + 1;
            } else {
                break;
            }
        }
        return reached;
    }

    private Reward createDailyTaskStageReward(DailyTaskDefinition definition, int stage) {
        Reward reward = definition.baseStageReward.copy();
        double multiplier = 1D + Math.max(0, stage - 1) * 0.2D;

        reward.money = Math.round(reward.money * multiplier);
        reward.skillXp = Math.max(0L, (long) Math.floor(reward.skillXp * multiplier));
        reward.playerXp = Math.max(0L, (long) Math.floor(reward.playerXp * multiplier));

        return reward;
    }

    private DailyTaskDefinition getDailyTaskForSlot(int slot) {
        for (Map.Entry<String, DailyTaskDefinition> taskEntry : config.dailyTasks.entrySet()) {
            DailyTaskDefinition definition = taskEntry.getValue();
            if (slot == getMenuSlotForDailyTask(definition.key)) {
                return definition;
            }
        }

        return null;
    }

    private int getMenuSlotForDailyTask(String taskKey) {
        if (DAILY_TASK_KILL_MOBS.equals(taskKey)) {
            return DAILY_TASK_MENU_SLOTS[0];
        }
        if (DAILY_TASK_MINE_RESOURCES.equals(taskKey)) {
            return DAILY_TASK_MENU_SLOTS[1];
        }
        if (DAILY_TASK_HARVEST_CROPS.equals(taskKey)) {
            return DAILY_TASK_MENU_SLOTS[2];
        }
        return -1;
    }

    private boolean isHarvestableCropBlock(Block block) {
        return block != null && isHarvestableCropMaterial(block.getType());
    }

    private boolean isHarvestableCropMaterial(Material type) {
        if (type == null || type == AIR_MATERIAL) return false;
        String typeName = type.name();
        if (type == Material.MELON || type == Material.PUMPKIN || type == Material.CACTUS ||
                type == Material.SUGAR_CANE || "SWEET_BERRY_BUSH".equals(typeName)) {
            return true;
        }
        return "WHEAT".equals(typeName) || "CARROT".equals(typeName) || "CARROTS".equals(typeName) ||
                "POTATO".equals(typeName) || "POTATOES".equals(typeName) || "BEETROOT".equals(typeName) ||
                "BEETROOTS".equals(typeName) || "NETHER_WART".equals(typeName) || "COCOA".equals(typeName);
    }

    private boolean isCropFullyGrown(Block block) {
        try {
            Object blockData = block.getClass().getMethod("getBlockData").invoke(block);
            Class<?> ageableClass = Class.forName("org.bukkit.block.data.Ageable");
            if (!ageableClass.isInstance(blockData)) {
                return true;
            }
            int age = (int) ageableClass.getMethod("getAge").invoke(blockData);
            int maxAge = (int) ageableClass.getMethod("getMaximumAge").invoke(blockData);
            return age >= maxAge;
        } catch (Exception ignored) {
            return true;
        }
    }

    private void claimGeneratorStorage(SuperiorPlayer superiorPlayer, GeneratorState state) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            send(superiorPlayer, msg("generator-must-be-online"));
            return;
        }

        if (state.storage.isEmpty()) {
            send(player, msg("generator-storage-empty").replace("%track%", state.trackKey));
            return;
        }

        List<Material> keys = new ArrayList<>(state.storage.keySet());
        for (Material material : keys) {
            int amount = state.storage.getOrDefault(material, 0);
            if (amount <= 0) continue;
            giveItemToPlayer(player, new ItemStack(material, amount));
            state.storage.remove(material);
        }

        playEffect(player, "sounds:item_pick_up", 0.5f, 1.0f);
        send(player, msg("generator-claimed").replace("%track%", state.trackKey));
    }

    private void sendSkillsStatus(CommandSender sender, SuperiorPlayer superiorPlayer, PlayerState state) {
        long nextMiningXp = getRequiredXpForLevel(state.miningLevel + 1, config.skillLevelThresholds, config.skillLevelStepXp);
        long nextFarmingXp = getRequiredXpForLevel(state.farmingLevel + 1, config.skillLevelThresholds, config.skillLevelStepXp);
        long nextSlayingXp = getRequiredXpForLevel(state.slayingLevel + 1, config.skillLevelThresholds, config.skillLevelStepXp);
        long nextPlayerXp = getRequiredXpForLevel(state.playerLevel + 1, config.playerLevelThresholds, config.playerLevelStepXp);

        send(sender, "Mining Level: &f" + state.miningLevel + " &7(XP: &f" + state.miningXp + "&7 / &f" + nextMiningXp + "&7)");
        send(sender, "Farming Level: &f" + state.farmingLevel + " &7(XP: &f" + state.farmingXp + "&7 / &f" + nextFarmingXp + "&7)");
        send(sender, "Slaying Level: &f" + state.slayingLevel + " &7(XP: &f" + state.slayingXp + "&7 / &f" + nextSlayingXp + "&7)");
        send(sender, "Player Level: &f" + state.playerLevel + " &7(XP: &f" + state.playerXp + "&7 / &f" + nextPlayerXp + "&7)");

        if (superiorPlayer.hasIsland()) {
            Island island = superiorPlayer.getIsland();
            if (island != null) {
                IslandState islandState = getIslandState(island.getUniqueId());
                send(sender, "Island One-Block Breaks: &f" + islandState.oneblockBrokenTotal);
            }
        }
    }

    private void sendPerksStatus(CommandSender sender, PlayerState state) {
        String[] perkNames = {"Resource Access", "Fragment Chance", "Treasure Chance", "Generator Limit", "Speed"};
        for (SkillTrack track : SkillTrack.values()) {
            int[] levels = getPerkLevels(state, track);
            StringBuilder sb = new StringBuilder(track.displayName + " perks: ");
            for (int i = 0; i < levels.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append("P").append(i + 1).append("=").append(levels[i]);
            }
            send(sender, sb.toString());
        }
        send(sender, "Perk effects per level:");
        send(sender, "- Crop growth bonus chance: &f" + formatPercent(config.perkCropGrowthBonusPerLevel));
        send(sender, "- Node yield bonus: &f" + formatPercent(config.perkNodeYieldBonusPerLevel));
        send(sender, "- Spawner speed bonus: &f" + formatPercent(config.perkSpawnerSpeedBonusPerLevel));
        send(sender, "- Generator yield bonus: &f" + formatPercent(config.perkGeneratorYieldBonusPerLevel));
        send(sender, "- One-block bonus drop chance: &f" + formatPercent(config.perkOneBlockBonusDropPerLevel));
    }

    private void sendOneBlockStatus(CommandSender sender, IslandState islandState, PlayerState playerState) {
        send(sender, "One-Block Total Breaks: &f" + islandState.oneblockBrokenTotal);
        send(sender, "Daily Progress: &f" + playerState.dailyOneblockBroken + "&7 / &f" + config.dailyOneBlockTargetBreaks);
        send(sender, "Daily Claimed: &f" + (playerState.dailyOneblockClaimed ? "Yes" : "No"));

        if (islandState.oneblockAnchor != null) {
            send(sender, "Anchor: &f" + islandState.oneblockAnchor.world + " " + islandState.oneblockAnchor.x + "," + islandState.oneblockAnchor.y + "," + islandState.oneblockAnchor.z);
        } else {
            send(sender, "Anchor: &cNot set (&7/admin evolvedskills setanchor&c)");
        }
    }

    private void sendNodeStatus(CommandSender sender, NodeState nodeState) {
        NodeTypeDefinition definition = config.nodeTypes.get(nodeState.typeKey);
        if (definition == null) {
            send(sender, "Unknown node type: " + nodeState.typeKey + ".");
            return;
        }

        int maxStorage = definition.maxStorageBase * nodeState.level;
        send(sender, "Node: &f" + definition.displayName);
        send(sender, "Level: &f" + nodeState.level + "&7 / &f" + definition.maxLevel);
        send(sender, "Stored: &f" + nodeState.stored + "&7 / &f" + maxStorage);
        send(sender, "Growth: &f" + formatPercent(Math.min(1D, nodeState.progress)));
    }

    private void sendSpawnerStatus(CommandSender sender, SpawnerState spawnerState) {
        SpawnerTypeDefinition definition = config.spawnerTypes.get(spawnerState.typeKey);
        if (definition == null) {
            send(sender, "Unknown spawner type: " + spawnerState.typeKey + ".");
            return;
        }

        int maxStorage = definition.maxStorageBase * spawnerState.level;
        send(sender, "Spawner: &f" + definition.displayName);
        send(sender, "Level: &f" + spawnerState.level + "&7 / &f" + definition.maxLevel);
        send(sender, "Stored drops: &f" + spawnerState.stored + "&7 / &f" + maxStorage);
        send(sender, "Spawn progress: &f" + formatPercent(Math.min(1D, spawnerState.progress)));
    }

    private void sendGeneratorStatus(CommandSender sender, UUID islandId, String trackKeyFilter) {
        boolean foundAny = false;
        for (GeneratorState state : generatorStates.values()) {
            if (!state.islandId.equals(islandId)) continue;
            if (trackKeyFilter != null && !state.trackKey.equalsIgnoreCase(trackKeyFilter)) continue;
            foundAny = true;

            GeneratorSettings settings = config.generators.get(state.trackKey);
            if (settings == null) continue;

            String trackDisplay = state.trackKey.substring(0, 1).toUpperCase() + state.trackKey.substring(1);
            send(sender, "&e" + trackDisplay + " Generator &7- Level &f" + state.level + " &7/ &f" + settings.maxLevel);

            if (state.storage.isEmpty()) {
                send(sender, "  Stored: &7none");
            } else {
                List<Map.Entry<Material, Integer>> entries = new ArrayList<>(state.storage.entrySet());
                entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                int shown = 0;
                for (Map.Entry<Material, Integer> entry : entries) {
                    send(sender, "  - &f" + prettyMaterial(entry.getKey()) + "&7: &f" + entry.getValue());
                    if (++shown >= 6) break;
                }
            }
        }
        if (!foundAny) {
            send(sender, "No generators placed on this island.");
        }
    }

    private void sendJourneyStatus(CommandSender sender, PlayerState playerState) {
        if (config.journeyTasks.isEmpty()) {
            send(sender, "No journey tasks are configured.");
            return;
        }

        List<JourneyTaskDefinition> orderedTasks = new ArrayList<>(config.journeyTasks.values());
        int currentTaskIndex = getCurrentJourneyTaskIndex(playerState, orderedTasks);

        send(sender, "Journey Progress:");
        for (int index = 0; index < orderedTasks.size(); index++) {
            JourneyTaskDefinition task = orderedTasks.get(index);

            boolean claimed = playerState.claimedJourney.contains(task.key);
            boolean claimable = !claimed && playerState.completedJourney.contains(task.key);
            boolean locked = !claimed && !claimable && index > currentTaskIndex;

            long progress = playerState.journeyProgress.getOrDefault(task.key, 0L);
            long cappedProgress = Math.min(task.target, progress);

            String status;
            if (claimed) {
                status = "&aClaimed";
            } else if (claimable) {
                status = "&bClaimable";
            } else if (locked) {
                status = "&cLocked";
            } else {
                status = "&eIn Progress";
            }

            send(sender, "- &f" + task.displayName + "&7: &f" + cappedProgress + "&7/&f" + task.target + " &8(" + status + "&8)");
        }
    }

    private void handleSkillHubMenuClick(SuperiorPlayer superiorPlayer, int slot) {
        switch (slot) {
            case 8:
                superiorPlayer.runIfOnline(Player::closeInventory);
                return;

            case 19:
                openMilestonesMenu(superiorPlayer, SkillTrack.MINING, 0);
                return;
            case 20:
                openLimitsMenu(superiorPlayer, SkillTrack.MINING, 0);
                return;
            case 21:
                openPerksMenu(superiorPlayer, SkillTrack.MINING);
                return;

            case 23:
                openMilestonesMenu(superiorPlayer, SkillTrack.SLAYING, 0);
                return;
            case 24:
                openLimitsMenu(superiorPlayer, SkillTrack.SLAYING, 0);
                return;
            case 25:
                openPerksMenu(superiorPlayer, SkillTrack.SLAYING);
                return;

            case 47:
                return;
            case 48:
                openMilestonesMenu(superiorPlayer, SkillTrack.FARMING, 0);
                return;
            case 49:
                openFarmingLimitsChoiceMenu(superiorPlayer);
                return;
            case 50:
                openPerksMenu(superiorPlayer, SkillTrack.FARMING);
                return;

            default:
                break;
        }
    }

    private void handleMilestonesMenuClick(SuperiorPlayer superiorPlayer, SkillTrack skillTrack, int slot, int page) {
        if (slot == 45) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        if (slot == 48) {
            openMilestonesMenu(superiorPlayer, skillTrack, page - 1);
            return;
        }

        if (slot == 50) {
            openMilestonesMenu(superiorPlayer, skillTrack, page + 1);
            return;
        }

        int gridIndex = findGridSlotIndex(slot);
        if (gridIndex < 0) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        List<MilestoneDefinition> milestones = getMilestonesForTrack(skillTrack);
        int milestoneIndex = page * GUI_GRID_SLOTS.length + gridIndex;
        if (milestoneIndex >= milestones.size()) {
            return;
        }

        MilestoneDefinition milestone = milestones.get(milestoneIndex);
        if (claimMilestoneTier(superiorPlayer, playerState, skillTrack, milestone)) {
            openMilestonesMenu(superiorPlayer, skillTrack, page);
        }
    }

    private void handlePerksMenuClick(SuperiorPlayer superiorPlayer, SkillTrack skillTrack, int slot) {
        if (slot == 18) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        
        int perkIndex = slot - 10;
        if (perkIndex < 0 || perkIndex > 4) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        if (performPerkUpgrade(superiorPlayer, playerState, skillTrack, perkIndex, 1)) {
            openPerksMenu(superiorPlayer, skillTrack);
        }
    }

    private void handleLimitsMenuClick(SuperiorPlayer superiorPlayer, SkillTrack skillTrack, int slot, int page) {
        if (slot == 45) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        if (slot == 48) {
            openLimitsMenu(superiorPlayer, skillTrack, page - 1);
            return;
        }

        if (slot == 50) {
            openLimitsMenu(superiorPlayer, skillTrack, page + 1);
            return;
        }

        int gridIndex = findGridSlotIndex(slot);
        if (gridIndex < 0) {
            return;
        }

        int index = page * GUI_GRID_SLOTS.length + gridIndex;

        Island island = superiorPlayer.getIsland();
        if (island == null) {
            send(superiorPlayer, msg("limits-need-island"));
            return;
        }

        IslandState islandState = getIslandState(island.getUniqueId());

        if (skillTrack == SkillTrack.SLAYING) {
            List<String> spawnerTypeKeys = new ArrayList<>(config.spawnerTypes.keySet());
            if (index >= spawnerTypeKeys.size()) {
                return;
            }

            if (upgradeSpawnerPlacementLimit(superiorPlayer, islandState, spawnerTypeKeys.get(index))) {
                openLimitsMenu(superiorPlayer, skillTrack, page);
            }
            return;
        }

        List<NodeTypeDefinition> nodeTypes = getNodeTypesForTrack(skillTrack);
        if (index >= nodeTypes.size()) {
            return;
        }

        if (upgradeNodePlacementLimit(superiorPlayer, islandState, nodeTypes.get(index).key)) {
            openLimitsMenu(superiorPlayer, skillTrack, page);
        }
    }

    private void handleJourneyMenuClick(SuperiorPlayer superiorPlayer, int slot, int page) {
        if (slot == 45) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        if (slot == 48) {
            openJourneyMenu(superiorPlayer, page - 1);
            return;
        }

        if (slot == 50) {
            openJourneyMenu(superiorPlayer, page + 1);
            return;
        }

        int gridIndex = findGridSlotIndex(slot);
        if (gridIndex < 0) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        List<JourneyTaskDefinition> tasks = new ArrayList<>(config.journeyTasks.values());
        int taskIndex = page * GUI_GRID_SLOTS.length + gridIndex;
        if (taskIndex >= tasks.size()) {
            return;
        }

        JourneyTaskDefinition task = tasks.get(taskIndex);
        int currentTaskIndex = getCurrentJourneyTaskIndex(playerState, tasks);

        if (taskIndex > currentTaskIndex && !playerState.claimedJourney.contains(task.key)) {
            send(superiorPlayer, msg("journey-task-locked"));
            return;
        }

        if (playerState.claimedJourney.contains(task.key)) {
            send(superiorPlayer, msg("journey-task-already-claimed").replace("%task%", task.displayName));
            return;
        }

        if (!playerState.completedJourney.contains(task.key)) {
            long progress = Math.min(task.target, playerState.journeyProgress.getOrDefault(task.key, 0L));
            send(superiorPlayer, msg("journey-task-incomplete")
                .replace("%task%", task.displayName)
                .replace("%progress%", String.valueOf(progress))
                .replace("%target%", String.valueOf(task.target)));
            return;
        }

        if (claimJourneyTask(superiorPlayer, playerState, task)) {
            int newIdx = getCurrentJourneyTaskIndex(playerState, tasks);
            openJourneyMenu(superiorPlayer, newIdx / GUI_GRID_SLOTS.length);
        }
    }

    private void handleDailiesMenuClick(SuperiorPlayer superiorPlayer, int slot) {
        if (slot == 45) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        if (slot == DAILY_ONEBLOCK_DAILY_SLOT) {
            PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
            claimOneBlockDaily(superiorPlayer, playerState);
            openDailiesMenu(superiorPlayer);
            return;
        }

        if (slot == 49) {
            openDailyChallengesMenu(superiorPlayer);
            return;
        }

        DailyTaskDefinition taskDefinition = getDailyTaskForSlot(slot);
        if (taskDefinition == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        if (claimNextDailyTaskStage(superiorPlayer, playerState, taskDefinition.key)) {
            openDailiesMenu(superiorPlayer);
        }
    }

    private void handlePlayerLevelsMenuClick(SuperiorPlayer superiorPlayer, int page, int slot) {
        if (slot == 53) {
            PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
            if (playerState.playerLevel >= 100) {
                if (!withdrawMoney(superiorPlayer, config.prestigeCost)) {
                    send(superiorPlayer, msg("prestige-insufficient-funds")
                        .replace("%cost%", DECIMAL_FORMAT.format(config.prestigeCost)));
                    return;
                }
                playerState.playerLevel = 1;
                playerState.playerXp = 0L;
                playerState.prestigeCount++;
                playerState.claimedPlayerLevelRewards.clear();
                send(superiorPlayer, msg("prestige-success")
                    .replace("%count%", String.valueOf(playerState.prestigeCount)));
                openPlayerLevelsMenu(superiorPlayer, 0);
            }
            return;
        }

        if (slot == 45) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        if (slot == 48) {
            openPlayerLevelsMenu(superiorPlayer, page - 1);
            return;
        }

        if (slot == 50) {
            openPlayerLevelsMenu(superiorPlayer, page + 1);
            return;
        }

        int gridIndex = findGridSlotIndex(slot);
        if (gridIndex < 0) {
            return;
        }

        int level = page * GUI_GRID_SLOTS.length + gridIndex + 1;
        if (level <= 0 || level > 100) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        if (claimPlayerLevelReward(superiorPlayer, playerState, level)) {
            openPlayerLevelsMenu(superiorPlayer, page);
        }
    }

    private void openSkillHubMenu(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        long nextPlayerXp = getRequiredXpForLevel(playerState.playerLevel + 1, config.playerLevelThresholds, config.playerLevelStepXp);

        Map<String, String> hubPlaceholders = new HashMap<>();
        hubPlaceholders.put("mining_level", String.valueOf(playerState.miningLevel));
        hubPlaceholders.put("farming_level", String.valueOf(playerState.farmingLevel));
        hubPlaceholders.put("slaying_level", String.valueOf(playerState.slayingLevel));
        hubPlaceholders.put("skill_level", String.valueOf(playerState.miningLevel)); 
        hubPlaceholders.put("player_level", String.valueOf(playerState.playerLevel));
        hubPlaceholders.put("player_xp", String.valueOf(playerState.playerXp));
        hubPlaceholders.put("next_player_xp", String.valueOf(nextPlayerXp));
        hubPlaceholders.put("xp_bar", buildGoalProgressBar(playerState.playerXp, nextPlayerXp));

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.SKILL_HUB, null, player.getUniqueId()),
                54,
            color(guiText("skill-hub.title", "§8ᴇᴠᴏʟᴠᴇᴅ ꜱᴋɪʟʟꜱ"))
        );

        fillMenuBackground(inventory, guiMaterial("skill-hub.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));

        setMenuSlots(inventory, new int[]{10, 11, 12}, createMenuItem(
            guiMaterial("skill-hub.sections.mining.material", menuMaterial("BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            applyGuiPlaceholders(guiText("skill-hub.sections.mining.name", "&b&lᴍɪɴɪɴɢ"), hubPlaceholders),
            applyGuiPlaceholders(guiTextList("skill-hub.sections.mining.lore", asList(
                "&7Earn Mining XP by claiming Milestones",
                "&7to level up and unlock new Perk Tiers.",
                "&7",
                "&bᴍɪɴɪɴɢ ʟᴇᴠᴇʟ: &f%mining_level% &7/ 50"
            )), hubPlaceholders)));
        setMenuSlots(inventory, new int[]{14, 15, 16}, createMenuItem(
            guiMaterial("skill-hub.sections.slaying.material", menuMaterial("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            applyGuiPlaceholders(guiText("skill-hub.sections.slaying.name", "&c&lꜱʟᴀʏɪɴɢ"), hubPlaceholders),
            applyGuiPlaceholders(guiTextList("skill-hub.sections.slaying.lore", asList(
                "&7Earn Slaying XP by claiming Milestones",
                "&7to level up and unlock new Perk Tiers.",
                "&7",
                "&bꜱʟᴀʏɪɴɢ ʟᴇᴠᴇʟ: &f%slaying_level% &7/ 50"
            )), hubPlaceholders)));
        setMenuSlots(inventory, new int[]{39, 40, 41}, createMenuItem(
            guiMaterial("skill-hub.sections.farming.material", menuMaterial("GREEN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            applyGuiPlaceholders(guiText("skill-hub.sections.farming.name", "&a&lꜰᴀʀᴍɪɴɢ"), hubPlaceholders),
            applyGuiPlaceholders(guiTextList("skill-hub.sections.farming.lore", asList(
                "&7Earn Farming XP by claiming Milestones",
                "&7to level up and unlock new Perk Tiers.",
                "&7",
                "&bꜰᴀʀᴍɪɴɢ ʟᴇᴠᴇʟ: &f%farming_level% &7/ 50"
            )), hubPlaceholders)));

        inventory.setItem(19, createMenuItem(
            guiMaterial("skill-hub.buttons.mining-milestones.material", menuMaterial("LIME_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.mining-milestones.name", "&a&lᴍɪʟᴇ"),
            guiTextList("skill-hub.buttons.mining-milestones.lore", asList(
                "&7Track your mining progress",
                "&7and claim tiered rewards."))));
        inventory.setItem(20, createMenuItem(
            guiMaterial("skill-hub.buttons.mining-limits.material", menuMaterial("LIGHT_BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.mining-limits.name", "&b&lʟɪᴍɪᴛꜱ"),
            guiTextList("skill-hub.buttons.mining-limits.lore", asList(
                "&7Expand your node placement",
                "&7limits for mining resources."))));
        inventory.setItem(21, createMenuItem(
            guiMaterial("skill-hub.buttons.mining-perks.material", menuMaterial("MAGENTA_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.mining-perks.name", "&d&lᴘᴇʀᴋ"),
            guiTextList("skill-hub.buttons.mining-perks.lore", asList(
                "&7Upgrade your mining perks",
                "&7to boost island production."))));

        inventory.setItem(23, createMenuItem(
            guiMaterial("skill-hub.buttons.slaying-milestones.material", menuMaterial("LIME_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.slaying-milestones.name", "&a&lᴍɪʟᴇ"),
            guiTextList("skill-hub.buttons.slaying-milestones.lore", asList(
                "&7Track your slaying progress",
                "&7and claim tiered rewards."))));
        inventory.setItem(24, createMenuItem(
            guiMaterial("skill-hub.buttons.slaying-limits.material", menuMaterial("LIGHT_BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.slaying-limits.name", "&b&lʟɪᴍɪᴛꜱ"),
            guiTextList("skill-hub.buttons.slaying-limits.lore", asList(
                "&7Expand your spawner placement",
                "&7limits for slaying resources."))));
        inventory.setItem(25, createMenuItem(
            guiMaterial("skill-hub.buttons.slaying-perks.material", menuMaterial("MAGENTA_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.slaying-perks.name", "&d&lᴘᴇʀᴋ"),
            guiTextList("skill-hub.buttons.slaying-perks.lore", asList(
                "&7Upgrade your slaying perks",
                "&7to boost island production."))));

        inventory.setItem(48, createMenuItem(
            guiMaterial("skill-hub.buttons.farming-milestones.material", menuMaterial("LIME_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.farming-milestones.name", "&a&lᴍɪʟᴇ"),
            guiTextList("skill-hub.buttons.farming-milestones.lore", asList(
                "&7Track your farming progress",
                "&7and claim tiered rewards."))));
        inventory.setItem(49, createMenuItem(
            guiMaterial("skill-hub.buttons.farming-limits.material", menuMaterial("LIGHT_BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.farming-limits.name", "&b&lʟɪᴍɪᴛꜱ"),
            guiTextList("skill-hub.buttons.farming-limits.lore", asList(
                "&7Access node limits and",
                "&7daily growth limits."))));
        inventory.setItem(50, createMenuItem(
            guiMaterial("skill-hub.buttons.farming-perks.material", menuMaterial("MAGENTA_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")),
            guiText("skill-hub.buttons.farming-perks.name", "&d&lᴘᴇʀᴋ"),
            guiTextList("skill-hub.buttons.farming-perks.lore", asList(
                "&7Upgrade your farming perks",
                "&7to boost island production."))));

        inventory.setItem(8, createMenuItem(
            guiMaterial("shared.close.material", menuMaterial("BARRIER", "REDSTONE_BLOCK")),
            guiText("shared.close.name", "&cᴄʟᴏꜱᴇ"),
            guiTextList("shared.close.lore", Collections.<String>emptyList())));

        inventory.setItem(4, createMenuItem(
            guiMaterial("skill-hub.header.material", menuMaterial("KNOWLEDGE_BOOK", "BOOK", "WRITABLE_BOOK")),
            applyGuiPlaceholders(guiText("skill-hub.header.name", "&e&lᴇᴠᴏʟᴠᴇᴅ ꜱᴋɪʟʟꜱ"), hubPlaceholders),
            applyGuiPlaceholders(guiTextList("skill-hub.header.lore", asList(
                "&bᴍɪɴɪɴɢ ʟᴇᴠᴇʟ: &f%mining_level%",
                "&bꜰᴀʀᴍɪɴɢ ʟᴇᴠᴇʟ: &f%farming_level%",
                "&bꜱʟᴀʏɪɴɢ ʟᴇᴠᴇʟ: &f%slaying_level%",
                "&7",
                "&bᴘʟᴀʏᴇʀ ʟᴇᴠᴇʟ: &f%player_level% &7(%player_xp%/%next_player_xp% xᴘ)",
                "%xp_bar%"
            )), hubPlaceholders)));

        player.openInventory(inventory);
    }

    private void openMilestonesMenu(SuperiorPlayer superiorPlayer, SkillTrack skillTrack, int page) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        List<MilestoneDefinition> milestones = getMilestonesForTrack(skillTrack);
        int maxPage = milestones.isEmpty() ? 0 : (milestones.size() - 1) / GUI_GRID_SLOTS.length;
        int safePage = Math.max(0, Math.min(maxPage, page));

        Map<String, String> trackPlaceholders = new HashMap<>();
        trackPlaceholders.put("track", skillTrack.displayName);

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.MILESTONES, skillTrack, player.getUniqueId(), safePage),
                54,
            color(applyGuiPlaceholders(guiText("milestones.title", "§8%track% ᴍɪʟᴇꜱᴛᴏɴᴇꜱ"), trackPlaceholders))
        );

        fillMenuBackground(inventory, guiMaterial("milestones.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));
        inventory.setItem(0, createMenuItem(
            guiMaterial("milestones.header.material", menuMaterial("BLAZE_ROD", "STICK")),
            applyGuiPlaceholders(guiText("milestones.header.name", "&eᴍɪʟᴇꜱᴛᴏɴᴇꜱ"), trackPlaceholders),
            applyGuiPlaceholders(guiTextList("milestones.header.lore", Collections.<String>emptyList()), trackPlaceholders)));
        inventory.setItem(45, createMenuItem(
            guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
            guiText("shared.back.name", "&aʙᴀᴄᴋ"),
            guiTextList("shared.back.lore", Collections.<String>emptyList())));

        if (safePage > 0) {
            inventory.setItem(48, createMenuItem(
                guiMaterial("milestones.nav.previous.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("milestones.nav.previous.name", "&eᴘʀᴇᴠɪᴏᴜꜱ ᴘᴀɢᴇ"),
                guiTextList("milestones.nav.previous.lore", Collections.<String>emptyList())));
        }
        if (safePage < maxPage) {
            inventory.setItem(50, createMenuItem(
                guiMaterial("milestones.nav.next.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("milestones.nav.next.name", "&eɴᴇxᴛ ᴘᴀɢᴇ"),
                guiTextList("milestones.nav.next.lore", Collections.<String>emptyList())));
        }

        String trackKey = skillTrack.name().toLowerCase(Locale.ENGLISH);
        Material allClaimedMat = menuMaterial("LIME_DYE", "INK_SACK");
        int startIndex = safePage * GUI_GRID_SLOTS.length;

        for (int gi = 0; gi < GUI_GRID_SLOTS.length; gi++) {
            int index = startIndex + gi;
            if (index >= milestones.size()) {
                break;
            }
            MilestoneDefinition milestone = milestones.get(index);
            String fullKey = trackKey + ":" + milestone.key;
            long progress = playerState.milestoneProgress.getOrDefault(fullKey, 0L);
            int highestClaimed = playerState.milestoneTiersClaimed.getOrDefault(fullKey, 0);
            int totalTiers = milestone.tiers.size();

            MilestoneTier nextTier = null;
            int nextTierNum = -1;
            for (Map.Entry<Integer, MilestoneTier> entry : milestone.tiers.entrySet()) {
                if (entry.getKey() <= highestClaimed) continue;
                nextTier = entry.getValue();
                nextTierNum = entry.getKey();
                break;
            }

            boolean allClaimed = nextTier == null;
            boolean canClaim = !allClaimed && progress >= nextTier.requiredCount;
            Material iconMaterial = allClaimed ? allClaimedMat : getMilestoneIconMaterial(skillTrack, milestone);

            List<String> lore = new ArrayList<>();
            if (!milestone.description.isEmpty()) {
                lore.add(color("&7" + milestone.description));
                lore.add(color("&7"));
            }

            if (allClaimed) {
                lore.add(color("&7Milestone Maxed!"));
            } else {
                lore.add(color("&bᴛɪᴇʀ " + nextTierNum + " ʀᴇǫᴜɪʀᴇᴍᴇɴᴛꜱ: &a" + progress + "&7/&f" + nextTier.requiredCount));
                lore.add(color(buildGoalProgressBar(progress, nextTier.requiredCount)));
                lore.add(color("&7"));
                lore.add(color("&bᴛɪᴇʀ " + nextTierNum + " ʀᴇᴡᴀʀᴅꜱ:"));
                if (nextTier.skillXp > 0) lore.add(color("&7  &f• &a+" + nextTier.skillXp + " &fꜱᴋɪʟʟ xᴘ"));
                if (nextTier.playerXp > 0) lore.add(color("&7  &f• &a+" + nextTier.playerXp + " &fᴘʟᴀʏᴇʀ xᴘ"));
                lore.add(color("&7"));
                lore.add(color(canClaim ? "&aClick to Claim!" : "&cRequirements not met!"));
            }

            inventory.setItem(GUI_GRID_SLOTS[gi], createMenuItem(
                    iconMaterial,
                    color("&b" + milestone.displayName + " &7(" + highestClaimed + "/" + totalTiers + ")"),
                    lore));
        }

        player.openInventory(inventory);
    }

    private Material getMilestoneIconMaterial(SkillTrack skillTrack, MilestoneDefinition milestone) {
        if (!milestone.targetBlocks.isEmpty()) {
            Material block = milestone.targetBlocks.get(0);
            Material item = BLOCK_TO_ITEM_ICON.get(block);
            return item != null ? item : block;
        }
        if (!milestone.targetEntities.isEmpty()) {
            EntityType et = milestone.targetEntities.get(0);
            Material spawnEgg = parseMaterial(et.name() + "_SPAWN_EGG");
            if (spawnEgg != null) return spawnEgg;
        }
        return getSkillPrimaryIcon(skillTrack);
    }

    private void openPerksMenu(SuperiorPlayer superiorPlayer, SkillTrack skillTrack) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        Map<String, String> trackPlaceholders = new HashMap<>();
        trackPlaceholders.put("track", sc(skillTrack.displayName));
        int trackSkillLevel = skillTrack == SkillTrack.MINING ? playerState.miningLevel :
                              skillTrack == SkillTrack.FARMING ? playerState.farmingLevel :
                              playerState.slayingLevel;
        trackPlaceholders.put("skill_level", String.valueOf(trackSkillLevel));
        String trackTitleColor = skillTrack == SkillTrack.MINING ? "&b" :
                                 skillTrack == SkillTrack.FARMING ? "&a" : "&c";

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.PERKS, skillTrack, player.getUniqueId()),
                27,
            color(applyGuiPlaceholders(guiText("perks.title", "§8%track% ᴘᴇʀᴋꜱ"), trackPlaceholders))
        );

        fillMenuBackground(inventory, guiMaterial("perks.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));

        inventory.setItem(0, createMenuItem(
            guiMaterial("perks.header.material", getSkillPrimaryIcon(skillTrack)),
            applyGuiPlaceholders(guiText("perks.header.name", trackTitleColor + "%track% ᴘᴇʀᴋꜱ"), trackPlaceholders),
            applyGuiPlaceholders(guiTextList("perks.header.lore", asList(
                "&7Upgrade your perks to enhance your",
                "&7island's production and resource rates.",
                "&7",
                "&bꜱᴋɪʟʟ ʟᴇᴠᴇʟ: &f%skill_level% &7/ 50"
            )), trackPlaceholders)));
        inventory.setItem(18, createMenuItem(
            guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
            guiText("shared.back.name", "&aʙᴀᴄᴋ"),
            guiTextList("shared.back.lore", Collections.<String>emptyList())));

        List<String> perkNames = new ArrayList<>();
        perkNames.add("Resource Access");
        perkNames.add("Fragment Chance");
        perkNames.add("Treasure Chance");
        perkNames.add("Generator Limit");
        perkNames.add(skillTrack == SkillTrack.MINING ? "Node Speed" :
                skillTrack == SkillTrack.FARMING ? "Crop Growth Speed" : "Spawner Speed");

        Material[] icons = new Material[]{
                menuMaterial("IRON_PICKAXE", "STONE_PICKAXE"),
                menuMaterial("PRISMARINE_SHARD", "DIAMOND"),
                menuMaterial("CHEST"),
                menuMaterial("RESPAWN_ANCHOR", "OBSIDIAN"),
                menuMaterial("CLOCK", "WATCH")
        };

        int[] trackPerkLevels = getPerkLevels(playerState, skillTrack);

        for (int i = 0; i < perkNames.size(); i++) {
            int requiredLevel = i < config.perkUnlockLevels.length ? config.perkUnlockLevels[i] : 1;
            boolean perkUnlocked = trackSkillLevel >= requiredLevel;
            if (!perkUnlocked) {
                inventory.setItem(10 + i, createMenuItem(
                    menuMaterial("GRAY_DYE", "INK_SACK"),
                    color("&8" + perkNames.get(i)),
                    asList(color("&cLocked — &7Unlocks at &fLevel " + requiredLevel))));
                continue;
            }

            String configuredName = applyGuiPlaceholders(guiText("perks.names.slot-" + (i + 1), perkNames.get(i)), trackPlaceholders);
            Material configuredIcon = guiMaterial("perks.icons.slot-" + (i + 1), icons[i]);

            int currentPerkLevel = trackPerkLevels[i];
            double upgradeCost = getPerkUpgradeCost(currentPerkLevel);
            String status;
            if (currentPerkLevel >= config.perkMaxLevel) {
                status = guiText("perks.entry.status.maxed", "&bMax Upgrade Reached!");
            } else {
                status = guiText("perks.entry.status.upgradable", "&aClick to upgrade this perk.");
            }

            Map<String, String> perkPlaceholders = new HashMap<>(trackPlaceholders);
            perkPlaceholders.put("name", configuredName);
            perkPlaceholders.put("current_tier", String.valueOf(currentPerkLevel));
            perkPlaceholders.put("max_tier", String.valueOf(config.perkMaxLevel));
            perkPlaceholders.put("tier_bar", buildGoalProgressBar(currentPerkLevel, config.perkMaxLevel));
            perkPlaceholders.put("upgrade_cost", "$" + format(upgradeCost));
            perkPlaceholders.put("required_level", String.valueOf(requiredLevel));
            perkPlaceholders.put("status", status);

            List<String> lore = applyGuiPlaceholders(guiTextList("perks.entry.lore", asList(
                    "&bᴛɪᴇʀ: &a%current_tier%&7/&f%max_tier%",
                    "%tier_bar%",
                    "&7",
                    "&bᴜᴘɢʀᴀᴅᴇ ᴄᴏꜱᴛ:",
                    "&7  &f• %upgrade_cost%",
                    "&7",
                    "%status%"
            )), perkPlaceholders);

            inventory.setItem(10 + i, createMenuItem(
                    configuredIcon,
                    applyGuiPlaceholders(guiText("perks.entry.name", "&f%name%"), perkPlaceholders),
                    lore));
        }

        player.openInventory(inventory);
    }

    private void openPerGeneratorMenu(SuperiorPlayer superiorPlayer, LocationKey locationKey) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) return;

        GeneratorState state = generatorStates.get(locationKey);
        if (state == null) return;

        GeneratorSettings settings = config.generators.get(state.trackKey);
        if (settings == null) return;

        String trackDisplay = state.trackKey.substring(0, 1).toUpperCase(Locale.ENGLISH) + state.trackKey.substring(1);

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.GENERATOR, null, player.getUniqueId()),
                27,
                color("§8" + sc(trackDisplay) + " ɢᴇɴᴇʀᴀᴛᴏʀ")
        );

        fillMenuBackground(inventory, menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE"));

        List<String> headerLore = new ArrayList<>();
        headerLore.add(color("&7Passively produces " + trackDisplay.toLowerCase() + " resources"));
        headerLore.add(color("&7over time. Collect from storage below."));
        headerLore.add(color("&7"));
        headerLore.add(color("&bʀᴀᴛᴇ: &f1 item every &f" + settings.productionRateSeconds + "s"));
        headerLore.add(color("&7"));
        if (state.storage.isEmpty()) {
            headerLore.add(color("&bꜱᴛᴏʀᴀɢᴇ: &7empty"));
        } else {
            headerLore.add(color("&bꜱᴛᴏʀᴀɢᴇ:"));
            List<Map.Entry<Material, Integer>> entries = new ArrayList<>(state.storage.entrySet());
            entries.sort(new Comparator<Map.Entry<Material, Integer>>() {
                @Override public int compare(Map.Entry<Material, Integer> a, Map.Entry<Material, Integer> b) {
                    return Integer.compare(b.getValue(), a.getValue());
                }
            });
            int shown = 0;
            for (Map.Entry<Material, Integer> e : entries) {
                headerLore.add(color("  &f" + prettyMaterial(e.getKey()) + " &7x&f" + e.getValue()));
                if (++shown >= 6) break;
            }
        }

        Material genIcon = "farming".equalsIgnoreCase(state.trackKey) ? menuMaterial("HAY_BLOCK", "SPONGE") :
                           "slaying".equalsIgnoreCase(state.trackKey) ? menuMaterial("NETHER_BRICKS", "NETHER_BRICK") :
                           menuMaterial("IRON_BLOCK", "IRON_ORE");
        inventory.setItem(4, createMenuItem(genIcon, "§e§l" + sc(trackDisplay) + " ɢᴇɴᴇʀᴀᴛᴏʀ", headerLore));

        List<String> claimLore = new ArrayList<>();
        if (state.storage.isEmpty()) {
            claimLore.add(color("&7The storage is currently empty."));
            claimLore.add(color("&7Come back once resources are ready."));
        } else {
            claimLore.add(color("&7Click to collect all stored resources."));
        }
        inventory.setItem(13, createMenuItem(
                state.storage.isEmpty() ? menuMaterial("GRAY_DYE", "INK_SACK") : menuMaterial("LIME_DYE", "INK_SACK"),
                state.storage.isEmpty() ? "&7ᴄʟᴀɪᴍ ꜱᴛᴏʀᴀɢᴇ" : "&aᴄʟᴀɪᴍ ꜱᴛᴏʀᴀɢᴇ",
                claimLore));

        player.openInventory(inventory);
        openGeneratorMenuViewers.put(player.getUniqueId(), locationKey);
    }

    private void handleGeneratorMenuClick(SuperiorPlayer superiorPlayer, boolean rightClick, int slot) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) return;

        LocationKey locationKey = openGeneratorMenuViewers.get(player.getUniqueId());
        if (locationKey == null) {
            player.closeInventory();
            return;
        }

        GeneratorState state = generatorStates.get(locationKey);
        if (state == null) {
            player.closeInventory();
            return;
        }

        if (slot == 13) {
            claimGeneratorStorage(superiorPlayer, state);
            openPerGeneratorMenu(superiorPlayer, locationKey);
        }
    }

    private void openLimitsMenu(SuperiorPlayer superiorPlayer, SkillTrack skillTrack, int page) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }

        Map<String, String> trackPlaceholders = new HashMap<>();
        trackPlaceholders.put("track", sc(skillTrack.displayName));

        int totalItems = skillTrack == SkillTrack.SLAYING
                ? config.spawnerTypes.size()
                : getNodeTypesForTrack(skillTrack).size();
        int maxPage = totalItems == 0 ? 0 : (totalItems - 1) / GUI_GRID_SLOTS.length;
        int safePage = Math.max(0, Math.min(maxPage, page));
        int startIndex = safePage * GUI_GRID_SLOTS.length;

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.LIMITS, skillTrack, player.getUniqueId(), safePage),
                54,
            color(applyGuiPlaceholders(guiText("limits.title", "§8%track% ʟɪᴍɪᴛꜱ"), trackPlaceholders))
        );

        fillMenuBackground(inventory, guiMaterial("limits.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));
        inventory.setItem(0, createMenuItem(
            guiMaterial("limits.header.material", menuMaterial("BLAZE_ROD", "STICK")),
            applyGuiPlaceholders(guiText("limits.header.name", "&eʟɪᴍɪᴛꜱ"), trackPlaceholders),
            applyGuiPlaceholders(guiTextList("limits.header.lore", Collections.<String>emptyList()), trackPlaceholders)));
        inventory.setItem(45, createMenuItem(
            guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
            guiText("shared.back.name", "&aʙᴀᴄᴋ"),
            guiTextList("shared.back.lore", Collections.<String>emptyList())));
        if (safePage > 0) {
            inventory.setItem(48, createMenuItem(
                guiMaterial("shared.prev-page.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("shared.prev-page.name", "&aᴘʀᴇᴠ ᴘᴀɢᴇ"),
                guiTextList("shared.prev-page.lore", Collections.<String>emptyList())));
        }
        if (safePage < maxPage) {
            inventory.setItem(50, createMenuItem(
                guiMaterial("shared.next-page.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("shared.next-page.name", "&aɴᴇxᴛ ᴘᴀɢᴇ"),
                guiTextList("shared.next-page.lore", Collections.<String>emptyList())));
        }

        Island island = superiorPlayer.getIsland();
        UUID islandId = island == null ? null : island.getUniqueId();
        IslandState islandState = islandId == null ? null : getIslandState(islandId);

        int slotIndex = 0;
        if (skillTrack == SkillTrack.SLAYING) {
            int itemIndex = 0;
            for (SpawnerTypeDefinition spawnerType : config.spawnerTypes.values()) {
                if (itemIndex < startIndex) { itemIndex++; continue; }
                if (slotIndex >= GUI_GRID_SLOTS.length) { break; }

                int placed = islandId == null ? 0 : countSpawnerTypePlacements(islandId, spawnerType.key);
                int tier = islandState == null ? 0 : getSpawnerLimitTier(islandState, spawnerType.key);
                LimitRule rule = getSpawnerLimitRule(spawnerType.key);
                int limit = islandState == null ? rule.getLimitForTier(0) : getSpawnerPlacementLimit(islandState, spawnerType.key);
                double nextCost = tier >= rule.maxUpgradeTier ? 0D : getLimitUpgradeCost(rule, tier);

                Map<String, String> placeholders = new HashMap<>(trackPlaceholders);
                placeholders.put("type", spawnerType.displayName);
                placeholders.put("placed", String.valueOf(placed));
                placeholders.put("limit", String.valueOf(limit));
                placeholders.put("placed_bar", buildGoalProgressBar(placed, limit));
                placeholders.put("tier", String.valueOf(tier));
                placeholders.put("max_tier", String.valueOf(rule.maxUpgradeTier));
                placeholders.put("tier_bar", buildGoalProgressBar(tier, rule.maxUpgradeTier));
                placeholders.put("next_add", String.valueOf(rule.getNextTierIncrease(tier)));
                placeholders.put("cost", format(nextCost));
                int spawnerFragCost = rule.getFragmentCostForTier(tier);
                placeholders.put("fragment_cost", String.valueOf(spawnerFragCost));
                if (tier >= rule.maxUpgradeTier) {
                    placeholders.put("status", guiText("limits.spawners.status.maxed", "&bMax limit tier reached."));
                } else {
                    placeholders.put("status", guiText("limits.spawners.status.upgradable", "&aClick to upgrade"));
                }

                List<String> lore = applyGuiPlaceholders(guiTextList("limits.spawners.entry.lore", asList(
                        "&7Your limit for how many &f" + spawnerType.displayName,
                        "&7&7spawners you can place at once.",
                        "&7",
                        "&bꜱᴘᴀᴡɴᴇʀꜱ ᴘʟᴀᴄᴇᴅ: &a%placed%&7/&f%limit%",
                        "%placed_bar%",
                        "&7",
                        "&7You can permanently expand this limit.",
                        "&7",
                        "&bᴜᴘɢʀᴀᴅᴇ ᴄᴏꜱᴛ:",
                        "&7  &f• $%cost%",
                        spawnerFragCost > 0 ? "&7  &f• %fragment_cost% " + spawnerType.displayName + " Fragments" : "",
                        "&7",
                        "%status%"
                )), placeholders);

                Material icon = guiMaterial("limits.spawners.icons." + spawnerType.key, resolveSpawnerItemMaterial(spawnerType));

                inventory.setItem(GUI_GRID_SLOTS[slotIndex], createMenuItem(
                        icon,
                        applyGuiPlaceholders(guiText("limits.spawners.entry.name", "&b%type% ꜱᴘᴀᴡɴᴇʀ ʟɪᴍɪᴛ &7(%placed%/%limit%)"), placeholders),
                        lore));
                slotIndex++;
                itemIndex++;
            }
        } else {
            int itemIndex = 0;
            for (NodeTypeDefinition nodeType : getNodeTypesForTrack(skillTrack)) {
                if (itemIndex < startIndex) { itemIndex++; continue; }
                if (slotIndex >= GUI_GRID_SLOTS.length) { break; }

                int placed = islandId == null ? 0 : countNodeTypePlacements(islandId, nodeType.key);
                int tier = islandState == null ? 0 : getNodeLimitTier(islandState, nodeType.key);
                LimitRule rule = getNodeLimitRule(nodeType.key);
                int limit = islandState == null ? rule.getLimitForTier(0) : getNodePlacementLimit(islandState, nodeType.key);
                double nextCost = tier >= rule.maxUpgradeTier ? 0D : getLimitUpgradeCost(rule, tier);

                Map<String, String> placeholders = new HashMap<>(trackPlaceholders);
                placeholders.put("type", nodeType.displayName);
                placeholders.put("placed", String.valueOf(placed));
                placeholders.put("limit", String.valueOf(limit));
                placeholders.put("placed_bar", buildGoalProgressBar(placed, limit));
                placeholders.put("tier", String.valueOf(tier));
                placeholders.put("max_tier", String.valueOf(rule.maxUpgradeTier));
                placeholders.put("tier_bar", buildGoalProgressBar(tier, rule.maxUpgradeTier));
                placeholders.put("next_add", String.valueOf(rule.getNextTierIncrease(tier)));
                placeholders.put("cost", format(nextCost));
                int nodeFragCost = rule.getFragmentCostForTier(tier);
                placeholders.put("fragment_cost", String.valueOf(nodeFragCost));
                if (tier >= rule.maxUpgradeTier) {
                    placeholders.put("status", guiText("limits.nodes.status.maxed", "&bMax limit tier reached."));
                } else {
                    placeholders.put("status", guiText("limits.nodes.status.upgradable", "&aClick to upgrade"));
                }

                List<String> lore = applyGuiPlaceholders(guiTextList("limits.nodes.entry.lore", asList(
                        "&7Your limit for how many &f" + nodeType.displayName,
                        "&7&7resource nodes you can place at once.",
                        "&7",
                        "&bɴᴏᴅᴇꜱ ᴘʟᴀᴄᴇᴅ: &a%placed%&7/&f%limit%",
                        "%placed_bar%",
                        "&7",
                        "&7You can permanently expand this limit.",
                        "&7",
                        "&bᴜᴘɢʀᴀᴅᴇ ᴄᴏꜱᴛ:",
                        "&7  &f• $%cost%",
                        nodeFragCost > 0 ? "&7  &f• %fragment_cost% " + nodeType.displayName + " Fragments" : "",
                        "&7",
                        "%status%"
                )), placeholders);

                Material rawIcon = nodeType.displayBlock == null ? menuMaterial("HAY_BLOCK", "STONE") : nodeType.displayBlock;
                Material icon = guiMaterial("limits.nodes.icons." + nodeType.key,
                        BLOCK_TO_ITEM_ICON.getOrDefault(rawIcon, rawIcon));
                ItemStack nodeIcon = createMenuItem(icon,
                        applyGuiPlaceholders(guiText("limits.nodes.entry.name", "&b%type% ɴᴏᴅᴇꜱ ʟɪᴍɪᴛ &7(%placed%/%limit%)"), placeholders),
                        lore);

                inventory.setItem(GUI_GRID_SLOTS[slotIndex], nodeIcon);
                slotIndex++;
                itemIndex++;
            }
        }

        while (slotIndex < GUI_GRID_SLOTS.length) {
            inventory.setItem(GUI_GRID_SLOTS[slotIndex], createMenuItem(
                    guiMaterial("limits.locked.material", menuMaterial("GRAY_DYE", "INK_SACK")),
                    guiText("limits.locked.name", "&8Locked"),
                    guiTextList("limits.locked.lore", asList("&7No configured limit entry."))));
            slotIndex++;
        }

        player.openInventory(inventory);
    }
    private void openGrowthLimitsMenu(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) return;

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.GROWTH_LIMITS, null, player.getUniqueId()),
                54,
                color(guiText("growth-limits.title", "§8ᴅᴀɪʟʏ ɢʀᴏᴡᴛʜ ʟɪᴍɪᴛꜱ")));

        fillMenuBackground(inventory, guiMaterial("growth-limits.background-material",
                menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")));
        inventory.setItem(0, createMenuItem(
                guiMaterial("growth-limits.header.material", menuMaterial("WHEAT", "LONG_GRASS")),
                guiText("growth-limits.header.name", "&aɢʀᴏᴡᴛʜ ʟɪᴍɪᴛꜱ"),
                guiTextList("growth-limits.header.lore", asList("&7Daily limits for crop and sapling growth."))));
        inventory.setItem(45, createMenuItem(
                guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("shared.back.name", "&aʙᴀᴄᴋ"),
                guiTextList("shared.back.lore", Collections.<String>emptyList())));

        Island island = superiorPlayer.getIsland();
        UUID islandId = island == null ? null : island.getUniqueId();
        IslandState islandState = islandId == null ? null : getIslandState(islandId);

        List<String> orderedKeys = new ArrayList<>(config.dailyGrowthLimits.keySet());
        Collections.sort(orderedKeys);

        int index = 0;
        for (String materialKey : orderedKeys) {
            if (index >= GUI_GRID_SLOTS.length) break;

            int tier = islandState == null ? 0 : islandState.growthLimitTiers.getOrDefault(materialKey, 0);
            int limit = getGrowthLimit(islandState == null ? new IslandState() : islandState, materialKey);
            int todayCount = islandState == null ? 0 : islandState.dailyGrowthCounts.getOrDefault(materialKey, 0);
            int maxTier = config.growthLimitMaxTier;
            boolean maxed = tier >= maxTier;
            double nextCost = maxed ? 0D : config.growthLimitUpgradeBaseCost * Math.pow(config.growthLimitUpgradeCostMultiplier, tier);
            int fragCost = maxed ? 0 : config.growthLimitFragmentCostBase * (tier + 1);

            String prettyKey = materialKey.replace('_', ' ');
            prettyKey = prettyKey.substring(0, 1).toUpperCase(Locale.ENGLISH) + prettyKey.substring(1).toLowerCase(Locale.ENGLISH);

            Map<String, String> ph = new HashMap<>();
            ph.put("type", prettyKey);
            ph.put("today", String.valueOf(todayCount));
            ph.put("limit", String.valueOf(limit));
            ph.put("bar", buildGoalProgressBar(todayCount, limit));
            ph.put("tier", String.valueOf(tier));
            ph.put("max_tier", String.valueOf(maxTier));
            ph.put("tier_bar", buildGoalProgressBar(tier, maxTier));
            ph.put("cost", format(nextCost));
            ph.put("fragment_cost", String.valueOf(fragCost));
            ph.put("status", maxed
                    ? guiText("growth-limits.status.maxed", "&bMax tier reached.")
                    : guiText("growth-limits.status.upgradable", "&aClick to upgrade"));

            List<String> lore = applyGuiPlaceholders(guiTextList("growth-limits.entry.lore", asList(
                    "&7Your daily limit for growing &f%type%&7.",
                    "&7Once reached, this crop will no longer",
                    "&7progress in growth stage until tomorrow.",
                    "&7",
                    "&b%type%s ɢʀᴏᴡɴ ᴛᴏᴅᴀʏ: &a%today%&7/&f%limit%",
                    "%bar%",
                    "&7",
                    "&7You can permanently expand this limit.",
                    "&7",
                    "&bᴜᴘɢʀᴀᴅᴇ ᴄᴏꜱᴛ:",
                    "&7  &f• $%cost%",
                    fragCost > 0 ? "&7  &f• %fragment_cost% Farming Fragments" : "",
                    "&7",
                    "%status%"
            )), ph);

            Material icon = guiMaterial("growth-limits.icons." + materialKey, resolveCropIcon(materialKey));
            inventory.setItem(GUI_GRID_SLOTS[index], createMenuItem(
                    icon,
                    applyGuiPlaceholders(guiText("growth-limits.entry.name", "&b%type% ɢʀᴏᴡᴛʜ ʟɪᴍɪᴛ &7(%tier%/%max_tier%)"), ph),
                    lore));
            index++;
        }

        while (index < GUI_GRID_SLOTS.length) {
            inventory.setItem(GUI_GRID_SLOTS[index], null);
            index++;
        }

        player.openInventory(inventory);
    }

    private Material resolveCropIcon(String materialKey) {
        Material mat = parseMaterial(materialKey);
        if (mat != null) {
            Material mapped = BLOCK_TO_ITEM_ICON.getOrDefault(mat, mat);
            if (mapped != null) return mapped;
        }
        return menuMaterial("WHEAT", "LONG_GRASS");
    }

    private void handleGrowthLimitsMenuClick(SuperiorPlayer superiorPlayer, int slot) {
        if (slot == 45) {
            openSkillHubMenu(superiorPlayer);
            return;
        }

        int clickedIndex = -1;
        for (int i = 0; i < GUI_GRID_SLOTS.length; i++) {
            if (GUI_GRID_SLOTS[i] == slot) { clickedIndex = i; break; }
        }
        if (clickedIndex < 0) return;

        List<String> orderedKeys = new ArrayList<>(config.dailyGrowthLimits.keySet());
        Collections.sort(orderedKeys);
        if (clickedIndex >= orderedKeys.size()) return;

        Island island = superiorPlayer.getIsland();
        if (island == null) return;
        IslandState islandState = getIslandState(island.getUniqueId());

        String materialKey = orderedKeys.get(clickedIndex);
        if (upgradeGrowthLimit(superiorPlayer, islandState, materialKey)) {
            openGrowthLimitsMenu(superiorPlayer);
        }
    }


    private void openFarmingLimitsChoiceMenu(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) return;

        Inventory inventory = Bukkit.createInventory(
            new EvolvedGuiHolder(GuiMenuType.FARMING_LIMITS, null, player.getUniqueId()),
            27,
            color("§8ꜰᴀʀᴍɪɴɢ ʟɪᴍɪᴛꜱ")
        );

        fillMenuBackground(inventory, guiMaterial("skill-hub.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));

        inventory.setItem(11, createMenuItem(
            menuMaterial("COMMAND_BLOCK", "DISPENSER"),
            "&b&lɴᴏᴅᴇ ʟɪᴍɪᴛꜱ",
            asList(
                "&7Expand your farming node",
                "&7placement limits.")));
        inventory.setItem(15, createMenuItem(
            menuMaterial("GREEN_DYE", "INK_SACK"),
            "&a&lɢʀᴏᴡᴛʜ ʟɪᴍɪᴛꜱ",
            asList(
                "&7Manage your daily crop",
                "&7growth limits.")));
        inventory.setItem(22, createMenuItem(
            guiMaterial("shared.close.material", menuMaterial("BARRIER", "REDSTONE_BLOCK")),
            guiText("shared.close.name", "&cᴄʟᴏꜱᴇ"),
            guiTextList("shared.close.lore", Collections.<String>emptyList())));

        player.openInventory(inventory);
    }

    private void handleFarmingLimitsChoiceMenuClick(SuperiorPlayer superiorPlayer, int slot) {
        if (slot == 11) {
            openLimitsMenu(superiorPlayer, SkillTrack.FARMING, 0);
        } else if (slot == 15) {
            openGrowthLimitsMenu(superiorPlayer);
        } else if (slot == 22) {
            openSkillHubMenu(superiorPlayer);
        }
    }

    private boolean upgradeGrowthLimit(SuperiorPlayer superiorPlayer, IslandState islandState, String materialKey) {
        int currentTier = islandState.growthLimitTiers.getOrDefault(materialKey, 0);
        if (currentTier >= config.growthLimitMaxTier) {
            send(superiorPlayer, msg("growth-limit-maxed").replace("%type%", materialKey.replace('_', ' ')));
            return false;
        }

        double cost = config.growthLimitUpgradeBaseCost * Math.pow(config.growthLimitUpgradeCostMultiplier, currentTier);
        int fragCost = config.growthLimitFragmentCostBase * (currentTier + 1);
        Player onlinePlayer = superiorPlayer.asPlayer();

        if (fragCost > 0) {
            if (onlinePlayer == null || countTrackFragmentsInInventory(onlinePlayer, SkillTrack.FARMING) < fragCost) {
                send(superiorPlayer, msg("growth-limit-insufficient-fragments").replace("%cost%", String.valueOf(fragCost)));
                return false;
            }
        }
        if (!withdrawMoney(superiorPlayer, cost)) {
            send(superiorPlayer, msg("growth-limit-insufficient-funds").replace("%cost%", format(cost)));
            return false;
        }
        if (fragCost > 0 && onlinePlayer != null) {
            consumeTrackFragmentsFromInventory(onlinePlayer, SkillTrack.FARMING, fragCost);
        }

        islandState.growthLimitTiers.put(materialKey, currentTier + 1);
        send(superiorPlayer, msg("growth-limit-upgraded")
            .replace("%type%", materialKey.replace('_', ' '))
            .replace("%tier%", String.valueOf(currentTier + 1)));
        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        progressJourney(superiorPlayer, playerState, JourneyType.EXPAND_GROWTH_LIMIT, materialKey, 1L);
        return true;
    }

    private void openJourneyMenu(SuperiorPlayer superiorPlayer, int page) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        List<JourneyTaskDefinition> tasks = new ArrayList<>(config.journeyTasks.values());
        int maxPage = tasks.isEmpty() ? 0 : (tasks.size() - 1) / GUI_GRID_SLOTS.length;
        int safePage = Math.max(0, Math.min(maxPage, page));

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.JOURNEY, null, player.getUniqueId(), safePage),
                54,
            color(guiText("journey.title", "§8ᴘʟᴀʏᴇʀ ᴊᴏᴜʀɴᴇʏ"))
        );

        fillMenuBackground(inventory, guiMaterial("journey.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));
        inventory.setItem(0, createMenuItem(
            guiMaterial("journey.header.material", menuMaterial("COMPASS", "BLAZE_ROD", "STICK")),
            config.journeyHeaderName != null ? color(config.journeyHeaderName) : color(guiText("journey.header.name", "&eᴘʟᴀʏᴇʀ ᨔᴏᴜʀɴᴇʏ")),
            config.journeyHeaderLore != null ? config.journeyHeaderLore : guiTextList("journey.header.lore", asList(
                "&7Complete tasks in order to progress",
                "&7through the island journey.",
                "&7",
                "&a&oɢʀᴇᴇɴ &7= ᴄʟᴀɪᴍᴇᴅ  &e&oʏᴇʟʟᴏᴡ &7= ɪɴ ᴘʀᴏɢʀᴇꜱꜱ  &c&oʀᴇᴅ &7= ʟᴏᴄᴋᴇᴅ"
            ))));
        inventory.setItem(45, createMenuItem(
            guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
            guiText("shared.back.name", "&aʙᴀᴄᴋ"),
            guiTextList("shared.back.lore", Collections.<String>emptyList())));

        if (safePage > 0) {
            inventory.setItem(48, createMenuItem(
                guiMaterial("journey.nav.previous.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("journey.nav.previous.name", "&eᴘʀᴇᴠɪᴏᴜꜱ ᴘᴀɢᴇ"),
                guiTextList("journey.nav.previous.lore", Collections.<String>emptyList())));
        }
        if (safePage < maxPage) {
            inventory.setItem(50, createMenuItem(
                guiMaterial("journey.nav.next.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("journey.nav.next.name", "&eɴᴇxᴛ ᴘᴀɢᴇ"),
                guiTextList("journey.nav.next.lore", Collections.<String>emptyList())));
        }

        int currentTaskIndex = getCurrentJourneyTaskIndex(playerState, tasks);
        int startIndex = safePage * GUI_GRID_SLOTS.length;

        for (int gi = 0; gi < GUI_GRID_SLOTS.length; gi++) {
            int index = startIndex + gi;
            if (index >= tasks.size()) {
                break;
            }
            JourneyTaskDefinition task = tasks.get(index);
            boolean claimed = playerState.claimedJourney.contains(task.key);
            boolean claimable = !claimed && playerState.completedJourney.contains(task.key);
            boolean locked = !claimed && !claimable && index > currentTaskIndex;

            long progress = playerState.journeyProgress.getOrDefault(task.key, 0L);
            long cappedProgress = Math.min(progress, task.target);

            Material icon;
            if (task.icon != null) {
                icon = task.icon;
            } else if (claimed) {
                icon = menuMaterial("LIME_DYE", "GREEN_DYE", "INK_SACK");
            } else if (locked) {
                icon = menuMaterial("RED_DYE", "ROSE_RED", "INK_SACK");
            } else {
                icon = menuMaterial("YELLOW_DYE", "DANDELION_YELLOW", "INK_SACK");
            }
            String nameColor = claimed ? "&a" : (locked ? "&c" : "&e");

            String status;
            if (locked) {
                status = guiText("journey.entry.status.locked", "&cTask Locked!");
            } else if (claimed) {
                status = guiText("journey.entry.status.claimed", "&7Task claimed and completed.");
            } else if (claimable) {
                status = guiText("journey.entry.status.claimable", "&aClick to Claim!");
            } else {
                status = guiText("journey.entry.status.in-progress", "&cRequirements not met!");
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", task.displayName);
            placeholders.put("progress", String.valueOf(cappedProgress));
            placeholders.put("target", String.valueOf(task.target));
            placeholders.put("bar", buildGoalProgressBar(cappedProgress, task.target));
            placeholders.put("status", status);

            List<String> lore = new ArrayList<>();
            if (task.description != null && !task.description.isEmpty()) {
                lore.add("&7" + task.description);
            }
            lore.add("&7");
            lore.addAll(applyGuiPlaceholders(guiTextList("journey.entry.lore", asList(
                    "&bᴛᴀꜱᴋ ᴘʀᴏɢʀᴇꜱꜱ: &a%progress%&7/&f%target%",
                    "%bar%",
                    "&7"
            )), placeholders));
            lore.add("&bʀᴇᴡᴀʀᴅꜱ:");
            lore.addAll(buildRewardLore(task.reward));
            lore.add("&7");
            lore.add(status);

            inventory.setItem(GUI_GRID_SLOTS[gi], createMenuItem(
                    icon,
                    nameColor + task.displayName,
                    lore));
        }

        player.openInventory(inventory);
    }

    private void openDailiesMenu(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        boolean claimable = !playerState.dailyOneblockClaimed &&
                playerState.dailyOneblockBroken >= config.dailyOneBlockTargetBreaks;

        Material oneBlockIcon = playerState.dailyOneblockClaimed ? menuMaterial("LIME_DYE", "INK_SACK") :
                claimable ? menuMaterial("CHEST_MINECART", "CHEST") :
                        menuMaterial("YELLOW_DYE", "INK_SACK");

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.DAILIES, null, player.getUniqueId()),
                54,
            color(guiText("dailies.title", "§8ᴅᴀɪʟʏ ᴛᴀꜱᴋꜱ"))
        );

        fillMenuBackground(inventory, guiMaterial("dailies.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));
        inventory.setItem(0, createMenuItem(
            guiMaterial("dailies.header.material", menuMaterial("CLOCK", "WATCH", "BLAZE_ROD")),
            guiText("dailies.header.name", "&eᴅᴀɪʟʏ ᴛᴀꜱᴋꜱ"),
            guiTextList("dailies.header.lore", asList(
                "&7Complete daily tasks and challenges",
                "&7to earn rewards and Player XP.",
                "&7Resets at midnight every day."
            ))));
        inventory.setItem(45, createMenuItem(
            guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
            guiText("shared.back.name", "&aʙᴀᴄᴋ"),
            guiTextList("shared.back.lore", Collections.<String>emptyList())));

        for (Map.Entry<String, DailyTaskDefinition> taskEntry : config.dailyTasks.entrySet()) {
            DailyTaskDefinition taskDefinition = taskEntry.getValue();
            int taskSlot = getMenuSlotForDailyTask(taskDefinition.key);
            if (taskSlot < 0) {
                continue;
            }

            long progress = playerState.dailyTaskProgress.getOrDefault(taskDefinition.key, 0L);
            int claimedStage = playerState.claimedDailyTaskStages.getOrDefault(taskDefinition.key, 0);
            int reachedStage = getReachedDailyTaskStage(taskDefinition, progress);
            int maxStage = taskDefinition.stageTargets.size();
            long maxTarget = maxStage <= 0 ? 0L : taskDefinition.stageTargets.get(maxStage - 1);
            long cappedProgress = Math.min(progress, maxTarget);
            int nextStage = Math.min(maxStage, claimedStage + 1);

            Material taskIcon;
            if (claimedStage >= maxStage) {
                taskIcon = menuMaterial("LIME_DYE", "INK_SACK");
            } else if (reachedStage >= nextStage) {
                taskIcon = menuMaterial("CHEST_MINECART", "CHEST");
            } else {
                taskIcon = taskDefinition.iconMaterial == null ? menuMaterial("YELLOW_DYE", "INK_SACK") : taskDefinition.iconMaterial;
            }

            long targetForNextStage = claimedStage < maxStage ? taskDefinition.stageTargets.get(nextStage - 1) : maxTarget;
            long remainingForNextStage = Math.max(0L, targetForNextStage - progress);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", taskDefinition.displayName);
            placeholders.put("total_progress", String.valueOf(cappedProgress));
            placeholders.put("total_target", String.valueOf(maxTarget));
            placeholders.put("total_bar", buildGoalProgressBar(cappedProgress, maxTarget));
            placeholders.put("claimed_stage", String.valueOf(claimedStage));
            placeholders.put("max_stage", String.valueOf(maxStage));
            placeholders.put("claimed_bar", buildGoalProgressBar(claimedStage, maxStage));
            placeholders.put("next_stage", String.valueOf(nextStage));
            placeholders.put("next_target", String.valueOf(targetForNextStage));
            placeholders.put("next_bar", buildGoalProgressBar(progress, targetForNextStage));
            placeholders.put("remaining", String.valueOf(remainingForNextStage));

            List<String> taskLore = new ArrayList<>();
            taskLore.addAll(applyGuiPlaceholders(guiTextList("dailies.task.entry.base-lore", asList(
                    "&7Complete each stage to earn greater rewards."
            )), placeholders));
            taskLore.add("&7");

            if (claimedStage < maxStage) {
                taskLore.addAll(applyGuiPlaceholders(guiTextList("dailies.task.entry.next-stage-lore", asList(
                        "&bꜱᴛᴀɢᴇ: &a%next_stage%&7/&f%max_stage%",
                        "&b" + taskDefinition.displayName + ": &a%total_progress%&7/&f%next_target%",
                        "%next_bar%",
                        "&7"
                )), placeholders));
                taskLore.add("&bꜱᴛᴀɢᴇ ʀᴇᴡᴀʀᴅꜱ:");
                taskLore.addAll(buildRewardLore(createDailyTaskStageReward(taskDefinition, nextStage)));
                taskLore.add("&7");

                if (reachedStage >= nextStage) {
                    placeholders.put("status", applyGuiPlaceholders(
                            guiText("dailies.task.entry.status.claimable", "&aClick to claim stage %next_stage%!"), placeholders));
                } else {
                    placeholders.put("status", applyGuiPlaceholders(
                            guiText("dailies.task.entry.status.in-progress", "&cRequirements not met!"), placeholders));
                }
                taskLore.add(placeholders.get("status"));
            } else {
                placeholders.put("status", guiText("dailies.task.entry.status.completed", "&7Final Daily Stage Claimed!"));
                taskLore.addAll(applyGuiPlaceholders(guiTextList("dailies.task.entry.completed-lore", asList("%status%")), placeholders));
            }

            inventory.setItem(taskSlot, createMenuItem(
                    taskIcon,
                    applyGuiPlaceholders(guiText("dailies.task.entry.name", "&eᴅᴀɪʟʏ ᴛᴀꜱᴋ: &f%name%"), placeholders),
                    taskLore));
        }

        long cappedOneBlockProgress = Math.min(playerState.dailyOneblockBroken, config.dailyOneBlockTargetBreaks);
        Map<String, String> oneBlockPlaceholders = new HashMap<>();
        oneBlockPlaceholders.put("name", "Break One-Blocks");
        oneBlockPlaceholders.put("progress", String.valueOf(cappedOneBlockProgress));
        oneBlockPlaceholders.put("target", String.valueOf(config.dailyOneBlockTargetBreaks));
        oneBlockPlaceholders.put("bar", buildGoalProgressBar(cappedOneBlockProgress, config.dailyOneBlockTargetBreaks));

        List<String> oneBlockLore = applyGuiPlaceholders(guiTextList("dailies.oneblock.entry.base-lore", asList(
                "&7Progress: &f%progress%&7/&f%target%",
                "%bar%"
        )), oneBlockPlaceholders);
        oneBlockLore.add("&7");
        oneBlockLore.addAll(buildRewardLore(config.dailyOneBlockReward));
        oneBlockLore.add("&7");
        if (playerState.dailyOneblockClaimed) {
            oneBlockPlaceholders.put("status", guiText("dailies.oneblock.entry.status.claimed", "&bAlready Claimed"));
        } else if (claimable) {
            oneBlockPlaceholders.put("status", guiText("dailies.oneblock.entry.status.claimable", "&aClick to claim reward"));
        } else {
            oneBlockPlaceholders.put("status", guiText("dailies.oneblock.entry.status.incomplete", "&eComplete the requirement first"));
        }
        oneBlockLore.add(oneBlockPlaceholders.get("status"));

        Material oneBlockEntryMaterial;
        if (playerState.dailyOneblockClaimed) {
            oneBlockEntryMaterial = guiMaterial("dailies.oneblock.entry.material.claimed", oneBlockIcon);
        } else if (claimable) {
            oneBlockEntryMaterial = guiMaterial("dailies.oneblock.entry.material.claimable", oneBlockIcon);
        } else {
            oneBlockEntryMaterial = guiMaterial("dailies.oneblock.entry.material.incomplete", oneBlockIcon);
        }

        inventory.setItem(DAILY_ONEBLOCK_DAILY_SLOT, createMenuItem(
                oneBlockEntryMaterial,
                applyGuiPlaceholders(guiText("dailies.oneblock.entry.name", "&f%name%"), oneBlockPlaceholders),
                oneBlockLore));

        PlayerState playerState2 = getPlayerState(superiorPlayer.getUniqueId());
        int completedChallenges = 0;
        int unlockedChallenges = 0;
        for (int i = 0; i < assignedDailyChallengeKeys.size() && i < config.dailyChallengeCount; i++) {
            String permNode = "evolvedskills.challenges." + (i + 1);
            if (!player.hasPermission(permNode)) continue;
            unlockedChallenges++;
            String challengeKey = assignedDailyChallengeKeys.get(i);
            if (playerState2.claimedChallenges.contains(challengeKey)) completedChallenges++;
        }
        List<String> challengeButtonLore = new ArrayList<>();
        challengeButtonLore.add("§7Complete randomized daily objectives");
        challengeButtonLore.add("§7for bonus rewards.");
        challengeButtonLore.add("§7");
        challengeButtonLore.add("§bᴄʟᴀɪᴍᴇᴅ: §a" + completedChallenges + "§7/§f" + unlockedChallenges);
        challengeButtonLore.add("§7");
        challengeButtonLore.add("§aClick to view challenges.");
        inventory.setItem(49, createMenuItem(
            menuMaterial("NETHER_STAR", "NETHER_STAR"),
            "§eᴅᴀɪʟʏ ᴄʜᴀʟʟᴇɴɢᴇꜱ",
            challengeButtonLore));

        player.openInventory(inventory);
    }

    private static final int[] CHALLENGE_GRID_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private void openDailyChallengesMenu(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) return;
        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());

        Inventory inventory = Bukkit.createInventory(
            new EvolvedGuiHolder(GuiMenuType.DAILY_CHALLENGES, null, player.getUniqueId()),
            54,
            color("§8ᴅᴀɪʟʏ ᴄʜᴀʟʟᴇɴɢᴇꜱ"));

        fillMenuBackground(inventory, menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE"));
        inventory.setItem(0, createMenuItem(
            menuMaterial("NETHER_STAR", "NETHER_STAR"),
            "§eᴅᴀɪʟʏ ᴄʜᴀʟʟᴇɴɢᴇꜱ",
            java.util.Arrays.asList(
                "§7Complete randomly assigned challenges",
                "§7each day to earn bonus rewards.",
                "§7Resets at midnight."
            )));
        inventory.setItem(45, createMenuItem(
            guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
            guiText("shared.back.name", "&aʙᴀᴄᴋ"),
            guiTextList("shared.back.lore", Collections.<String>emptyList())));

        for (int i = 0; i < CHALLENGE_GRID_SLOTS.length; i++) {
            int gridSlot = CHALLENGE_GRID_SLOTS[i];
            String permNode = "evolvedskills.challenges." + (i + 1);
            if (!player.hasPermission(permNode)) {
                inventory.setItem(gridSlot, createMenuItem(
                    menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"),
                    "§8ᴄʜᴀʟʟᴇɴɢᴇ ꜱʟᴏᴛ " + (i + 1),
                    java.util.Arrays.asList("§7Requires permission:", "§f" + permNode)));
                continue;
            }
            if (i >= assignedDailyChallengeKeys.size()) {
                inventory.setItem(gridSlot, createMenuItem(
                    menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"),
                    "§8No Challenge Today",
                    java.util.Arrays.asList("§7Check back after the daily reset.")));
                continue;
            }
            String challengeKey = assignedDailyChallengeKeys.get(i);
            DailyChallengeDefinition def = null;
            for (DailyChallengeDefinition d : config.dailyChallengePool) {
                if (d.key.equals(challengeKey)) { def = d; break; }
            }
            if (def == null) continue;
            long progress = playerState.challengeProgress.getOrDefault(challengeKey, 0L);
            long cappedProgress = Math.min(progress, def.target);
            boolean completed = cappedProgress >= def.target;
            boolean claimed = playerState.claimedChallenges.contains(challengeKey);
            Material icon;
            if (claimed) icon = menuMaterial("LIME_DYE", "INK_SACK");
            else if (completed) icon = menuMaterial("CHEST_MINECART", "CHEST");
            else icon = def.iconMaterial != null ? def.iconMaterial : menuMaterial("PAPER", "BOOK");
            String desc = def.description.replace("%amount%", String.valueOf(def.target));
            List<String> lore = new ArrayList<>();
            if (!desc.isEmpty()) {
                lore.add("§7" + desc);
                lore.add("§7");
            }
            lore.add("§bᴘʀᴏɢʀᴇꜱꜱ: §a" + cappedProgress + "§7/§f" + def.target);
            lore.add(color(buildGoalProgressBar(cappedProgress, def.target)));
            lore.add("§7");
            lore.add("§bʀᴇᴡᴀʀᴅꜱ:");
            lore.addAll(buildRewardLore(def.reward));
            lore.add("§7");
            if (claimed) {
                lore.add("§7Already Claimed.");
            } else if (completed) {
                lore.add("§aClick to claim reward!");
            }
            inventory.setItem(gridSlot, createMenuItem(icon, "§b" + def.displayName, lore));
        }

        player.openInventory(inventory);
    }

    private void handleDailyChallengesMenuClick(SuperiorPlayer superiorPlayer, int slot) {
        if (slot == 45) {
            openDailiesMenu(superiorPlayer);
            return;
        }

        for (int i = 0; i < CHALLENGE_GRID_SLOTS.length; i++) {
            if (slot != CHALLENGE_GRID_SLOTS[i]) continue;
            Player p = superiorPlayer.asPlayer();
            if (p == null) return;
            String permNode = "evolvedskills.challenges." + (i + 1);
            if (!p.hasPermission(permNode)) return;
            PlayerState ps = getPlayerState(superiorPlayer.getUniqueId());
            if (i >= assignedDailyChallengeKeys.size()) return;
            String challengeKey = assignedDailyChallengeKeys.get(i);
            if (ps.claimedChallenges.contains(challengeKey)) {
                send(superiorPlayer, msg("challenge-already-claimed"));
                return;
            }
            long progress = ps.challengeProgress.getOrDefault(challengeKey, 0L);
            DailyChallengeDefinition def = null;
            for (DailyChallengeDefinition d : config.dailyChallengePool) {
                if (d.key.equals(challengeKey)) { def = d; break; }
            }
            if (def == null || progress < def.target) {
                send(superiorPlayer, msg("challenge-not-complete"));
                return;
            }
            ps.claimedChallenges.add(challengeKey);
            applyReward(superiorPlayer, ps, def.reward, "&6Challenge Claimed! &f" + def.displayName, SkillTrack.MINING);
            if (p != null) playEffect(p, "sounds:upgrade", 1.0f, 1.0f);
            openDailyChallengesMenu(superiorPlayer);
            return;
        }
    }

    private void openPlayerLevelsMenu(SuperiorPlayer superiorPlayer, int page) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return;
        }

        PlayerState playerState = getPlayerState(superiorPlayer.getUniqueId());
        long nextPlayerXp = getRequiredXpForLevel(playerState.playerLevel + 1, config.playerLevelThresholds, config.playerLevelStepXp);
        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("current_level", String.valueOf(playerState.playerLevel));
        headerPlaceholders.put("player_xp", String.valueOf(playerState.playerXp));
        headerPlaceholders.put("next_player_xp", String.valueOf(nextPlayerXp));
        long xpForCurrentLevel = getRequiredXpForLevel(playerState.playerLevel, config.playerLevelThresholds, config.playerLevelStepXp);
        long progressWithinLevel = Math.max(0, playerState.playerXp - xpForCurrentLevel);
        long neededWithinLevel = Math.max(1, nextPlayerXp - xpForCurrentLevel);
        headerPlaceholders.put("xp_bar", buildGoalProgressBar(progressWithinLevel, neededWithinLevel));

        int maxPage = (100 - 1) / GUI_GRID_SLOTS.length;
        int safePage = Math.max(0, Math.min(maxPage, page));

        Inventory inventory = Bukkit.createInventory(
                new EvolvedGuiHolder(GuiMenuType.PLAYER_LEVELS, null, player.getUniqueId(), safePage),
                54,
            color(guiText("player-levels.title", "§8ᴘʟᴀʏᴇʀ ʟᴇᴠᴇʟ ʀᴇᴡᴀʀᴅꜱ"))
        );

        fillMenuBackground(inventory, guiMaterial("player-levels.background-material",
            menuMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE")));
        inventory.setItem(0, createMenuItem(
            guiMaterial("player-levels.header.material", menuMaterial("EXPERIENCE_BOTTLE", "EXP_BOTTLE")),
            applyGuiPlaceholders(guiText("player-levels.header.name", "&eᴘʟᴀʏᴇʀ ʟᴇᴠᴇʟꜱ"), headerPlaceholders),
            applyGuiPlaceholders(guiTextList("player-levels.header.lore", asList(
                "&7Current Level: &f%current_level%",
                "&7XP To Next: &f%player_xp%&7/&f%next_player_xp%",
                "%xp_bar%",
                "&7Claim rewards from unlocked levels."
            )), headerPlaceholders)));
        inventory.setItem(45, createMenuItem(
            guiMaterial("shared.back.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
            guiText("shared.back.name", "&aʙᴀᴄᴋ"),
            guiTextList("shared.back.lore", Collections.<String>emptyList())));

        if (safePage > 0) {
            inventory.setItem(48, createMenuItem(
                guiMaterial("player-levels.nav.previous.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("player-levels.nav.previous.name", "&eᴘʀᴇᴠɪᴏᴜꜱ ᴘᴀɢᴇ"),
                guiTextList("player-levels.nav.previous.lore", Collections.<String>emptyList())));
        }

        if (safePage < maxPage) {
            inventory.setItem(50, createMenuItem(
                guiMaterial("player-levels.nav.next.material", menuMaterial("ARROW", "SPECTRAL_ARROW")),
                guiText("player-levels.nav.next.name", "&eɴᴇxᴛ ᴘᴀɢᴇ"),
                guiTextList("player-levels.nav.next.lore", Collections.<String>emptyList())));
        }

        int startLevel = safePage * GUI_GRID_SLOTS.length + 1;

        for (int index = 0; index < GUI_GRID_SLOTS.length; index++) {
            int level = startLevel + index;
            if (level > 100) {
                break;
            }

            boolean claimed = playerState.claimedPlayerLevelRewards.contains(level);
            boolean claimable = !claimed && playerState.playerLevel >= level;

            Material icon = claimed ? menuMaterial("EMPTY_MINECART", "MINECART") :
                    claimable ? menuMaterial("CHEST_MINECART", "CHEST") :
                            menuMaterial("TNT_MINECART", "MINECART");

            Reward reward = createPlayerLevelReward(level);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("level", String.valueOf(level));
            String levelBarStr;
            if (!claimed && !claimable && level == playerState.playerLevel + 1) {
                long xpForCurrent = getRequiredXpForLevel(playerState.playerLevel, config.playerLevelThresholds, config.playerLevelStepXp);
                long xpForNext = getRequiredXpForLevel(playerState.playerLevel + 1, config.playerLevelThresholds, config.playerLevelStepXp);
                long progressWithin = Math.max(0, playerState.playerXp - xpForCurrent);
                long neededWithin = Math.max(1, xpForNext - xpForCurrent);
                levelBarStr = buildGoalProgressBar(progressWithin, neededWithin);
            } else {
                levelBarStr = "";
            }
            placeholders.put("level_bar", levelBarStr);

            String status;

            if (claimed) {
                status = guiText("player-levels.entry.status.claimed", "&7Rewards already claimed!");
            } else if (claimable) {
                status = guiText("player-levels.entry.status.claimable", "&aClick to claim!");
            } else {
                status = guiText("player-levels.entry.status.locked", "&cRequirements not met!");
            }
            placeholders.put("status", status);

            List<String> lore = new ArrayList<>();
            if (!claimed && !claimable && level == playerState.playerLevel + 1) {
                lore.add("&bᴘʟᴀʏᴇʀ xᴘ ʀᴇǫᴜɪʀᴇᴅ: &a" + progressWithinLevel + "&7/&f" + neededWithinLevel);
                lore.add(levelBarStr);
            } else {
                lore.add("&bʀᴇǫᴜɪʀᴇᴅ ʟᴇᴠᴇʟ: &f" + level);
            }
            lore.add("&7");
            lore.add("&bʟᴇᴠᴇʟ ʀᴇᴡᴀʀᴅꜱ:");
            lore.addAll(buildRewardLore(reward));
            lore.add("&7");
            lore.add(status);

            inventory.setItem(GUI_GRID_SLOTS[index], createMenuItem(
                    icon,
                    applyGuiPlaceholders(guiText("player-levels.entry.name", "&eʟᴇᴠᴇʟ %level% ʀᴇᴡᴀʀᴅꜱ"), placeholders),
                    lore));
        }

        if (playerState.playerLevel >= 100) {
            inventory.setItem(53, createMenuItem(
                menuMaterial("NETHER_STAR"),
                color("§e§lᴘʀᴇꜱᴛɪɢᴇ"),
                asList(
                    color("&7Reset your player level to 1 and"),
                    color("&7earn all level rewards again."),
                    color("&7"),
                    color("&bᴘʀᴇꜱᴛɪɢᴇ ᴄᴏᴜɴᴛ: &f" + playerState.prestigeCount + "x"),
                    color("&bᴄᴏꜱᴛ: &f$" + DECIMAL_FORMAT.format(config.prestigeCost)),
                    color("&7"),
                    color("&aClick to Prestige!")
                )));
        }

        player.openInventory(inventory);
    }

    private boolean claimPlayerLevelReward(SuperiorPlayer superiorPlayer, PlayerState playerState, int level) {
        if (level <= 0 || level > 100) {
            return false;
        }

        if (playerState.claimedPlayerLevelRewards.contains(level)) {
            send(superiorPlayer, msg("player-level-reward-already-claimed").replace("%level%", String.valueOf(level)));
            return false;
        }

        if (playerState.playerLevel < level) {
            send(superiorPlayer, msg("player-level-reward-locked").replace("%level%", String.valueOf(level)));
            return false;
        }

        playerState.claimedPlayerLevelRewards.add(level);
        applyReward(superiorPlayer, playerState, createPlayerLevelReward(level),
                "Claimed player level reward for level " + level + ".", SkillTrack.MINING);
        return true;
    }

    private Reward createPlayerLevelReward(int level) {
        Reward reward = new Reward();
        reward.money = Math.max(500D, level * 500D);
        reward.skillXp = Math.max(0L, level * 4L);

        if (level % 10 == 0) {
            reward.items.add(new ItemStack(menuMaterial("DIAMOND"), 1));
        }

        return reward;
    }

    private int getCurrentJourneyTaskIndex(PlayerState playerState, List<JourneyTaskDefinition> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            JourneyTaskDefinition task = tasks.get(i);
            if (!playerState.claimedJourney.contains(task.key)) {
                return i;
            }
        }

        return tasks.isEmpty() ? 0 : tasks.size() - 1;
    }

    private void checkAndAutoCompleteCurrentJourneyTask(SuperiorPlayer superiorPlayer, PlayerState playerState) {
        if (config == null || config.journeyTasks.isEmpty()) return;
        List<JourneyTaskDefinition> tasks = new ArrayList<>(config.journeyTasks.values());
        int idx = getCurrentJourneyTaskIndex(playerState, tasks);
        if (idx < 0 || idx >= tasks.size()) return;
        JourneyTaskDefinition task = tasks.get(idx);
        if (playerState.claimedJourney.contains(task.key) || playerState.completedJourney.contains(task.key)) return;
        if (isJourneyTaskAlreadyMet(superiorPlayer, playerState, task)) {
            playerState.journeyProgress.put(task.key, task.target);
            playerState.completedJourney.add(task.key);
            showJourneyBossBar(superiorPlayer, playerState);
            send(superiorPlayer, msg("journey-task-complete").replace("%task%", task.displayName));
        }
    }

    private boolean isJourneyTaskAlreadyMet(SuperiorPlayer superiorPlayer, PlayerState playerState, JourneyTaskDefinition task) {
        switch (task.type) {
            case REACH_SKILL_LEVEL: {
                for (String match : task.matchValues) {
                    int level = "mining".equals(match) ? playerState.miningLevel
                        : "farming".equals(match) ? playerState.farmingLevel
                        : "slaying".equals(match) ? playerState.slayingLevel : -1;
                    if (level >= (int) task.target) return true;
                }
                return false;
            }
            case REACH_MILESTONE_TIER: {
                for (String match : task.matchValues) {
                    if (playerState.milestoneTiersClaimed.getOrDefault(match, 0) >= (int) task.target) return true;
                    
                    String[] mParts = match.split(":", 2);
                    if (mParts.length == 2) {
                        SkillTrack mTrack = null;
                        for (SkillTrack t : SkillTrack.values()) {
                            if (t.name().equalsIgnoreCase(mParts[0])) { mTrack = t; break; }
                        }
                        if (mTrack != null) {
                            for (MilestoneDefinition ms : getMilestonesForTrack(mTrack)) {
                                if (!ms.key.equalsIgnoreCase(mParts[1])) continue;
                                MilestoneTier targetTier = ms.tiers.get((int) task.target);
                                if (targetTier != null) {
                                    long prog = playerState.milestoneProgress.getOrDefault(match, 0L);
                                    if (prog >= targetTier.requiredCount) return true;
                                }
                                break;
                            }
                        }
                    }
                }
                return false;
            }
            case REACH_PERK_LEVEL: {
                for (String match : task.matchValues) {
                    String[] p = match.split(":");
                    if (p.length < 3) continue;
                    try {
                        int perkIdx = Integer.parseInt(p[1]);
                        int reqLevel = Integer.parseInt(p[2]);
                        int[] perkLevels = "mining".equals(p[0]) ? playerState.miningPerkLevels
                            : "farming".equals(p[0]) ? playerState.farmingPerkLevels
                            : "slaying".equals(p[0]) ? playerState.slayingPerkLevels : null;
                        if (perkLevels != null && perkIdx >= 0 && perkIdx < perkLevels.length
                                && perkLevels[perkIdx] >= reqLevel) return true;
                    } catch (NumberFormatException ignored) {}
                }
                return false;
            }
            case EXPAND_GROWTH_LIMIT: {
                Island island = superiorPlayer.getIsland();
                if (island == null) return false;
                IslandState islandState = getIslandState(island.getUniqueId());
                for (String match : task.matchValues) {
                    if (islandState.growthLimitTiers.getOrDefault(match.toLowerCase(Locale.ENGLISH), 0) >= (int) task.target) return true;
                }
                return false;
            }
            default:
                return false;
        }
    }

    private boolean claimJourneyTask(SuperiorPlayer superiorPlayer, PlayerState playerState, JourneyTaskDefinition task) {
        if (playerState.claimedJourney.contains(task.key)) {
            return false;
        }

        if (!playerState.completedJourney.contains(task.key)) {
            return false;
        }

        applyReward(superiorPlayer, playerState, task.reward, "Claimed journey task: " + task.displayName + ".",
                journeyTypeToSkillTrack(task.type));
        playerState.claimedJourney.add(task.key);
        superiorPlayer.runIfOnline(p -> playEffect(p, "sounds:upgrade", 1.0f, 1.4f));
        showJourneyBossBar(superiorPlayer, playerState);
        checkAndAutoCompleteCurrentJourneyTask(superiorPlayer, playerState);
        return true;
    }

    private boolean performPerkUpgrade(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                       SkillTrack track, int perkIndex, int amount) {
        int requiredLevel = perkIndex < config.perkUnlockLevels.length ? config.perkUnlockLevels[perkIndex] : 1;
        int trackSkillLevel = getSkillLevel(playerState, track);
        if (trackSkillLevel < requiredLevel) {
            send(superiorPlayer, msg("perk-locked")
                .replace("%track%", track.displayName)
                .replace("%level%", String.valueOf(requiredLevel)));
            return false;
        }

        int[] perkLevels = getPerkLevels(playerState, track);
        int currentLevel = perkLevels[perkIndex];

        if (currentLevel >= config.perkMaxLevel) {
            send(superiorPlayer, msg("perk-max-tier"));
            return false;
        }

        double cost = getPerkUpgradeCost(currentLevel);
        if (!withdrawMoney(superiorPlayer, cost)) {
            send(superiorPlayer, msg("perk-insufficient-funds").replace("%cost%", format(cost)));
            return false;
        }

        perkLevels[perkIndex]++;
        String[] perkNames = {"Resource Access", "Fragment Chance", "Treasure Chance", "Generator Limit",
                track == SkillTrack.MINING ? "Node Speed" : track == SkillTrack.FARMING ? "Crop Growth Speed" : "Spawner Speed"};
        send(superiorPlayer, msg("perk-upgraded")
            .replace("%perk%", perkNames[perkIndex])
            .replace("%tier%", String.valueOf(perkLevels[perkIndex])));
        superiorPlayer.runIfOnline(p -> playEffect(p, "sounds:upgrade", 1.0f, 1.2f));
        String perkJourneyKey = track.name().toLowerCase(Locale.ENGLISH) + ":" + perkIndex + ":" + perkLevels[perkIndex];
        progressJourney(superiorPlayer, playerState, JourneyType.REACH_PERK_LEVEL, perkJourneyKey, 1L);
        return true;
    }

    private double getPerkUpgradeCost(int currentTier) {
        if (config.perkUpgradeCosts.isEmpty()) return 0D;
        int idx = Math.max(0, Math.min(currentTier, config.perkUpgradeCosts.size() - 1));
        return config.perkUpgradeCosts.get(idx);
    }

    private String msg(String key) {
        return config.messages.getOrDefault(key, key);
    }

    private int countNodeTypePlacements(UUID islandId, String nodeTypeKey) {
        int count = 0;
        for (NodeState nodeState : nodeStates.values()) {
            if (islandId.equals(nodeState.islandId) && nodeTypeKey.equals(nodeState.typeKey)) {
                count++;
            }
        }
        return count;
    }

    private int countSpawnerTypePlacements(UUID islandId, String spawnerTypeKey) {
        int count = 0;
        for (SpawnerState spawnerState : spawnerStates.values()) {
            if (islandId.equals(spawnerState.islandId) && spawnerTypeKey.equals(spawnerState.typeKey)) {
                count++;
            }
        }
        return count;
    }

    private int getNodeLimitTier(IslandState islandState, String nodeTypeKey) {
        return Math.max(0, islandState.nodeLimitTiers.getOrDefault(nodeTypeKey.toLowerCase(Locale.ENGLISH), 0));
    }

    private int getSpawnerLimitTier(IslandState islandState, String spawnerTypeKey) {
        return Math.max(0, islandState.spawnerLimitTiers.getOrDefault(spawnerTypeKey.toLowerCase(Locale.ENGLISH), 0));
    }

    private int getNodePlacementLimit(IslandState islandState, String nodeTypeKey) {
        LimitRule rule = getNodeLimitRule(nodeTypeKey);
        int tier = getNodeLimitTier(islandState, nodeTypeKey);
        return rule.getLimitForTier(tier);
    }

    private int getSpawnerPlacementLimit(IslandState islandState, String spawnerTypeKey) {
        LimitRule rule = getSpawnerLimitRule(spawnerTypeKey);
        int tier = getSpawnerLimitTier(islandState, spawnerTypeKey);
        return rule.getLimitForTier(tier);
    }

    private LimitRule getNodeLimitRule(String nodeTypeKey) {
        LimitRule rule = config.nodeLimitRules.get(nodeTypeKey.toLowerCase(Locale.ENGLISH));
        return rule == null ? config.defaultNodeLimitRule : rule;
    }

    private LimitRule getSpawnerLimitRule(String spawnerTypeKey) {
        LimitRule rule = config.spawnerLimitRules.get(spawnerTypeKey.toLowerCase(Locale.ENGLISH));
        return rule == null ? config.defaultSpawnerLimitRule : rule;
    }

    private double getLimitUpgradeCost(LimitRule rule, int currentTier) {
        return rule.getMoneyCostForTier(currentTier);
    }

    private boolean upgradeNodePlacementLimit(SuperiorPlayer superiorPlayer, IslandState islandState, String nodeTypeKey) {
        NodeTypeDefinition definition = config.nodeTypes.get(nodeTypeKey.toLowerCase(Locale.ENGLISH));
        if (definition == null) {
            send(superiorPlayer, msg("node-type-unconfigured"));
            return false;
        }

        LimitRule rule = getNodeLimitRule(nodeTypeKey);
        int currentTier = getNodeLimitTier(islandState, nodeTypeKey);
        if (currentTier >= rule.maxUpgradeTier) {
            send(superiorPlayer, msg("limits-node-maxed").replace("%type%", definition.displayName));
            return false;
        }

        double cost = rule.getMoneyCostForTier(currentTier);
        int fragmentCost = rule.getFragmentCostForTier(currentTier);
        Player onlinePlayer = superiorPlayer.asPlayer();
        if (fragmentCost > 0) {
            if (onlinePlayer == null || countFragmentsInInventory(onlinePlayer, definition.key) < fragmentCost) {
                send(superiorPlayer, msg("limits-node-insufficient-fragments")
                    .replace("%fragments%", String.valueOf(fragmentCost))
                    .replace("%type%", definition.displayName));
                return false;
            }
        }
        if (!withdrawMoney(superiorPlayer, cost)) {
            send(superiorPlayer, msg("limits-insufficient-funds").replace("%cost%", format(cost)));
            return false;
        }
        if (fragmentCost > 0 && onlinePlayer != null) {
            consumeFragmentsFromInventory(onlinePlayer, definition.key, fragmentCost);
        }

        int newTier = currentTier + 1;
        islandState.nodeLimitTiers.put(nodeTypeKey.toLowerCase(Locale.ENGLISH), newTier);
        send(superiorPlayer, msg("limits-node-upgraded")
            .replace("%type%", definition.displayName)
            .replace("%tier%", String.valueOf(newTier)));
        return true;
    }

    private boolean upgradeSpawnerPlacementLimit(SuperiorPlayer superiorPlayer, IslandState islandState, String spawnerTypeKey) {
        SpawnerTypeDefinition definition = config.spawnerTypes.get(spawnerTypeKey.toLowerCase(Locale.ENGLISH));
        if (definition == null) {
            send(superiorPlayer, msg("spawner-type-unconfigured"));
            return false;
        }

        LimitRule rule = getSpawnerLimitRule(spawnerTypeKey);
        int currentTier = getSpawnerLimitTier(islandState, spawnerTypeKey);
        if (currentTier >= rule.maxUpgradeTier) {
            send(superiorPlayer, msg("limits-spawner-maxed").replace("%type%", definition.displayName));
            return false;
        }

        double cost = rule.getMoneyCostForTier(currentTier);
        int fragmentCost = rule.getFragmentCostForTier(currentTier);
        Player onlinePlayer = superiorPlayer.asPlayer();
        if (fragmentCost > 0) {
            if (onlinePlayer == null || countFragmentsInInventory(onlinePlayer, definition.key) < fragmentCost) {
                send(superiorPlayer, msg("limits-spawner-insufficient-fragments")
                    .replace("%fragments%", String.valueOf(fragmentCost))
                    .replace("%type%", definition.displayName));
                return false;
            }
        }
        if (!withdrawMoney(superiorPlayer, cost)) {
            send(superiorPlayer, msg("limits-insufficient-funds").replace("%cost%", format(cost)));
            return false;
        }
        if (fragmentCost > 0 && onlinePlayer != null) {
            consumeFragmentsFromInventory(onlinePlayer, definition.key, fragmentCost);
        }

        int newTier = currentTier + 1;
        islandState.spawnerLimitTiers.put(spawnerTypeKey.toLowerCase(Locale.ENGLISH), newTier);
        send(superiorPlayer, msg("limits-spawner-upgraded")
            .replace("%type%", definition.displayName)
            .replace("%tier%", String.valueOf(newTier)));
        return true;
    }

    private List<NodeTypeDefinition> getNodeTypesForTrack(SkillTrack skillTrack) {
        List<NodeTypeDefinition> nodeTypes = new ArrayList<>();
        for (NodeTypeDefinition nodeType : config.nodeTypes.values()) {
            if (nodeType == null) {
                continue;
            }

            
            if (nodeType.skillTrack == null || nodeType.skillTrack == skillTrack) {
                nodeTypes.add(nodeType);
            }
        }

        return nodeTypes;
    }

    private int findGridSlotIndex(int slot) {
        for (int index = 0; index < GUI_GRID_SLOTS.length; index++) {
            if (GUI_GRID_SLOTS[index] == slot) {
                return index;
            }
        }

        return -1;
    }

    private List<String> buildRewardLore(Reward reward) {
        List<String> lore = new ArrayList<>();

        if (reward.money > 0D) {
            lore.add("&7  &f• &a$" + format(reward.money));
        }
        if (reward.skillXp > 0L) {
            lore.add("&7  &f• &a+" + reward.skillXp + " &fꜱᴋɪʟʟ xᴘ");
        }
        if (reward.playerXp > 0L) {
            lore.add("&7  &f• &a+" + reward.playerXp + " &fᴘʟᴀʏᴇʀ xᴘ");
        }

        for (ItemStack itemStack : reward.items) {
            if (itemStack == null || itemStack.getType() == null || itemStack.getType() == AIR_MATERIAL) {
                continue;
            }
            String itemLabel;
            if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) {
                itemLabel = itemStack.getItemMeta().getDisplayName();
            } else {
                itemLabel = "&f" + prettyMaterial(itemStack.getType());
            }
            lore.add("&7  &f• " + itemStack.getAmount() + "x " + itemLabel);
        }

        if (lore.isEmpty()) {
            lore.add("&7  None configured.");
        }

        return lore;
    }

    private Material getSkillPrimaryIcon(SkillTrack skillTrack) {
        switch (skillTrack) {
            case MINING:
                return menuMaterial("IRON_PICKAXE", "STONE_PICKAXE");
            case FARMING:
                return menuMaterial("IRON_HOE", "STONE_HOE");
            case SLAYING:
                return menuMaterial("IRON_SWORD", "STONE_SWORD");
            default:
                return menuMaterial("STONE");
        }
    }

    private boolean isFillerItem(ItemStack item) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String stripped = ChatColor.stripColor(meta.getDisplayName());
        return stripped == null || stripped.trim().isEmpty();
    }

    private void fillMenuBackground(Inventory inventory, Material backgroundMaterial) {
        ItemStack filler = createMenuItem(backgroundMaterial, "&f");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private void setMenuSlots(Inventory inventory, int[] slots, ItemStack itemStack) {
        for (int slot : slots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, itemStack);
            }
        }
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        List<String> loreLines = new ArrayList<>();
        if (lore != null) {
            Collections.addAll(loreLines, lore);
        }
        return createMenuItem(material, name, loreLines);
    }

    private ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(material == null ? menuMaterial("STONE") : material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(color(name));

            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(color(line));
            }

            itemMeta.setLore(coloredLore);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    private Material menuMaterial(String... materialNames) {
        for (String materialName : materialNames) {
            if (materialName == null || materialName.isEmpty()) {
                continue;
            }

            Material material = EnumHelper.getEnum(Material.class, materialName);
            if (material != null) {
                return material;
            }

            material = Material.matchMaterial(materialName);
            if (material != null) {
                return material;
            }
        }

        return Material.STONE;
    }

    private String guiText(String path, String fallback) {
        if (config == null || config.guiSection == null || path == null || path.isEmpty()) {
            return fallback;
        }

        String value = config.guiSection.getString(path);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private List<String> guiTextList(String path, List<String> fallback) {
        List<String> fallbackCopy = fallback == null ? new ArrayList<>() : new ArrayList<>(fallback);
        if (config == null || config.guiSection == null || path == null || path.isEmpty() || !config.guiSection.isSet(path)) {
            return fallbackCopy;
        }

        List<String> values = config.guiSection.getStringList(path);
        return values == null || values.isEmpty() ? fallbackCopy : new ArrayList<>(values);
    }

    private Material guiMaterial(String path, Material fallback) {
        if (config == null || config.guiSection == null || path == null || path.isEmpty()) {
            return fallback;
        }

        Material material = parseMaterial(config.guiSection.getString(path));
        return material == null ? fallback : material;
    }

    private String applyGuiPlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        String output = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("%" + entry.getKey() + "%", entry.getValue() == null ? "" : entry.getValue());
        }

        return output;
    }

    private List<String> applyGuiPlaceholders(List<String> lines, Map<String, String> placeholders) {
        List<String> output = new ArrayList<>();
        if (lines == null) {
            return output;
        }

        for (String line : lines) {
            output.add(applyGuiPlaceholders(line, placeholders));
        }

        return output;
    }

    private void handleAdminStatus(CommandSender sender, String[] args) {
        send(sender, "&aEvolvedSkills Module Status:");
        send(sender, "Loaded player states: &f" + playerStates.size());
        send(sender, "Loaded island states: &f" + islandStates.size());
        send(sender, "Active nodes: &f" + nodeStates.size());
        send(sender, "Active spawners: &f" + spawnerStates.size());
        send(sender, "Configured milestones: &f" + (config != null ? config.milestones.size() : 0));
        send(sender, "Configured node types: &f" + (config != null ? config.nodeTypes.size() : 0));
        send(sender, "Configured spawner types: &f" + (config != null ? config.spawnerTypes.size() : 0));
        send(sender, "Configured generators: &f" + (config != null ? String.join(", ", config.generators.keySet()) : "none"));

        
        if (args.length >= 4) {
            SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
            if (target == null) {
                send(sender, "Player &f" + args[3] + " &7not found.");
                return;
            }
            PlayerState state = getPlayerState(target.getUniqueId());
            long nextMiningXp = getRequiredXpForLevel(state.miningLevel + 1, config.skillLevelThresholds, config.skillLevelStepXp);
            long nextFarmingXp = getRequiredXpForLevel(state.farmingLevel + 1, config.skillLevelThresholds, config.skillLevelStepXp);
            long nextSlayingXp = getRequiredXpForLevel(state.slayingLevel + 1, config.skillLevelThresholds, config.skillLevelStepXp);
            send(sender, "&eplayer: &f" + target.getName());
            send(sender, "Mining L" + state.miningLevel + " &7(" + state.miningXp + "/" + nextMiningXp + " xp)");
            send(sender, "Farming L" + state.farmingLevel + " &7(" + state.farmingXp + "/" + nextFarmingXp + " xp)");
            send(sender, "Slaying L" + state.slayingLevel + " &7(" + state.slayingXp + "/" + nextSlayingXp + " xp)");
            send(sender, "Mining perks: &f" + formatPerkArray(state.miningPerkLevels));
            send(sender, "Farming perks: &f" + formatPerkArray(state.farmingPerkLevels));
            send(sender, "Slaying perks: &f" + formatPerkArray(state.slayingPerkLevels));
            if (target.hasIsland()) {
                Island island = target.getIsland();
                if (island != null) {
                    IslandState islandState = getIslandState(island.getUniqueId());
                    send(sender, "Island one-block breaks: &f" + islandState.oneblockBrokenTotal);
                    send(sender, "Anchor set: &f" + (islandState.oneblockAnchor != null ? "yes" : "no"));
                }
            }
        }
    }

    private String formatPerkArray(int[] perks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < perks.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(perks[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private void handleAdminSetAnchor(CommandSender sender, String[] args) {
        SuperiorPlayer targetSuperiorPlayer;

        if (args.length >= 4) {
            targetSuperiorPlayer = plugin.getPlayers().getSuperiorPlayer(args[3]);
            if (targetSuperiorPlayer == null || !targetSuperiorPlayer.hasIsland()) {
                send(sender, "Target player has no island: " + args[3] + ".");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                send(sender, "Console must specify a target player: /is admin evolvedskills setanchor <player>");
                return;
            }

            targetSuperiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
            if (targetSuperiorPlayer == null || !targetSuperiorPlayer.hasIsland()) {
                send(sender, "You must have an island to set anchor.");
                return;
            }
        }

        Player sourcePlayer = sender instanceof Player ? (Player) sender : targetSuperiorPlayer.asPlayer();
        if (sourcePlayer == null) {
            send(sender, "A source player must be online to pick the anchor block.");
            return;
        }

        LocationKey targetBlockKey = getTargetBlockKey(sourcePlayer, 8);
        if (targetBlockKey == null) {
            send(sender, "Look at a valid block to set as one-block anchor.");
            return;
        }

        Island island = targetSuperiorPlayer.getIsland();
        if (island == null) {
            send(sender, "Target player has no island.");
            return;
        }

        Location location = targetBlockKey.toLocation();
        if (location == null) {
            send(sender, "The target location belongs to an unloaded world.");
            return;
        }

        Island locationIsland = plugin.getGrid().getIslandAt(location);
        if (locationIsland == null || !Objects.equals(locationIsland.getUniqueId(), island.getUniqueId())) {
            send(sender, "Anchor must be inside the target island.");
            return;
        }

        IslandState islandState = getIslandState(island.getUniqueId());
        islandState.oneblockAnchor = targetBlockKey;

        
        Material material = chooseOneBlockMaterial(islandState.oneblockBrokenTotal, 0);
        if (material == null) {
            material = config.oneBlockFallbackMaterial;
        }

        location.getBlock().setType(material);
        send(sender, "Set one-block anchor for island " + island.getName() + " at " + targetBlockKey.world + " " + targetBlockKey.x + "," + targetBlockKey.y + "," + targetBlockKey.z + ".");
    }

    private void handleAdminGiveNode(CommandSender sender, String[] args) {
        if (args.length < 5) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " admin evolvedskills givenode <player> <type> [amount]");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            send(sender, "Invalid player: " + args[3] + ".");
            return;
        }

        String typeKey = args[4].toLowerCase(Locale.ENGLISH);
        NodeTypeDefinition definition = config.nodeTypes.get(typeKey);
        if (definition == null) {
            send(sender, "Unknown node type: " + args[4] + ".");
            return;
        }

        int amount = args.length >= 6 ? Math.max(1, parseInt(args[5], 1)) : 1;
        ItemStack itemStack = createNodeItem(typeKey, amount);

        if (target.asPlayer() == null) {
            send(sender, "Target must be online to receive items.");
            return;
        }

        giveItemToPlayer(target.asPlayer(), itemStack);
        send(sender, "Given " + amount + "x node item (" + definition.displayName + ") to " + target.getName() + ".");
    }

    private void handleAdminGiveSpawner(CommandSender sender, String[] args) {
        if (args.length < 5) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " admin evolvedskills givespawner <player> <type> [amount]");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            send(sender, "Invalid player: " + args[3] + ".");
            return;
        }

        String typeKey = args[4].toLowerCase(Locale.ENGLISH);
        SpawnerTypeDefinition definition = config.spawnerTypes.get(typeKey);
        if (definition == null) {
            send(sender, "Unknown spawner type: " + args[4] + ".");
            return;
        }

        int amount = args.length >= 6 ? Math.max(1, parseInt(args[5], 1)) : 1;
        ItemStack itemStack = createSpawnerItem(typeKey, amount);

        if (target.asPlayer() == null) {
            send(sender, "Target must be online to receive items.");
            return;
        }

        giveItemToPlayer(target.asPlayer(), itemStack);
        send(sender, "Given " + amount + "x spawner item (" + definition.displayName + ") to " + target.getName() + ".");
    }

    private void handleAdminGiveGenerator(CommandSender sender, String[] args) {
        if (args.length < 5) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " admin evolvedskills givegenerator <player> <track> [amount]");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            send(sender, "Invalid player: " + args[3] + ".");
            return;
        }

        String trackKey = args[4].toLowerCase(Locale.ENGLISH);
        if (!config.generators.containsKey(trackKey)) {
            send(sender, "Unknown generator track: " + args[4] + ". Available: " + String.join(", ", config.generators.keySet()));
            return;
        }

        int amount = args.length >= 6 ? Math.max(1, parseInt(args[5], 1)) : 1;
        ItemStack itemStack = createGeneratorItem(trackKey, amount);

        if (target.asPlayer() == null) {
            send(sender, "Target must be online to receive items.");
            return;
        }

        giveItemToPlayer(target.asPlayer(), itemStack);
        send(sender, "Given " + amount + "x " + trackKey + " generator to " + target.getName() + ".");
    }

    private void handleAdminGiveTreasure(CommandSender sender, String[] args) {
        if (args.length < 5) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " admin evolvedskills givetreasure <player> <pool> [amount]");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            send(sender, "Invalid player: " + args[3] + ".");
            return;
        }

        String poolKey = args[4].toLowerCase(Locale.ENGLISH);
        TreasurePoolDefinition pool = config.treasurePools.get(poolKey);
        if (pool == null || pool.entries.isEmpty()) {
            send(sender, "Unknown treasure pool: " + args[4] + ".");
            return;
        }

        int amount = args.length >= 6 ? Math.max(1, parseInt(args[5], 1)) : 1;
        ItemStack itemStack = createTreasureToken(poolKey, amount);

        if (target.asPlayer() == null) {
            send(sender, "Target must be online to receive items.");
            return;
        }

        giveItemToPlayer(target.asPlayer(), itemStack);
        send(sender, "Given " + amount + "x treasure token (" + pool.displayName + ") to " + target.getName() + ".");
    }

    private void handleAdminSetSkill(CommandSender sender, String[] args) {
        if (args.length < 6) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " admin evolvedskills setskill <player> <mining|farming|slaying> <level>");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            send(sender, "Invalid player: " + args[3] + ".");
            return;
        }

        SkillTrack track = EnumHelper.getEnum(SkillTrack.class, args[4].toUpperCase(Locale.ENGLISH));
        if (track == null) {
            send(sender, "Invalid skill: " + args[4] + ". Use mining, farming, or slaying.");
            return;
        }

        int level = Math.max(1, parseInt(args[5], 1));

        PlayerState state = getPlayerState(target.getUniqueId());
        int oldLevel = getSkillLevel(state, track);
        long requiredXp = getRequiredXpForLevel(level, config.skillLevelThresholds, config.skillLevelStepXp);
        setSkillLevel(state, track, level);
        setSkillXp(state, track, Math.max(getSkillXp(state, track), requiredXp));

        send(sender, "Set " + track.displayName + " level of " + target.getName() + " to " + level + ".");
    }

    private void handleAdminResetPlayer(CommandSender sender, String[] args) {
        if (args.length < 4) {
            send(sender, "Usage: /" + plugin.getCommands().getLabel() + " admin evolvedskills resetplayer <player>");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            send(sender, "Invalid player: " + args[3] + ".");
            return;
        }

        playerStates.remove(target.getUniqueId());
        send(sender, "Reset evolved skills state for " + target.getName() + ".");
    }

    private void tickNodes() {
        
        
    }

    private void tickSpawners() {
        if (spawnerStates.isEmpty()) {
            return;
        }

        
        Map<UUID, Integer> islandPerkCache = new HashMap<>();

        for (Map.Entry<LocationKey, SpawnerState> entry : spawnerStates.entrySet()) {
            SpawnerState spawnerState = entry.getValue();
            SpawnerTypeDefinition definition = config.spawnerTypes.get(spawnerState.typeKey);
            if (definition == null) {
                continue;
            }

            Island island = plugin.getGrid().getIslandByUUID(spawnerState.islandId);
            if (island == null || island.getOwner() == null) {
                continue;
            }

            
            Player ownerPlayer = island.getOwner().asPlayer();
            if (ownerPlayer == null || !ownerPlayer.isOnline()) {
                continue;
            }

            int perkLevel = islandPerkCache.computeIfAbsent(spawnerState.islandId,
                    id -> getPlayerState(island.getOwner().getUniqueId()).slayingPerkLevels[4]);

            double speedMultiplier = 1D + perkLevel * config.perkSpawnerSpeedBonusPerLevel;
            double progressPerTick = speedMultiplier / Math.max(1D, definition.spawnSeconds / (double) module.getConfiguration().getSpawnerTickSeconds());

            spawnerState.progress += progressPerTick;

            int maxStorage = Math.max(1, definition.maxStorageBase * spawnerState.level);
            int dropPerCycle = Math.max(1, definition.outputPerCycle * spawnerState.level);

            Location spawnerLoc = entry.getKey().toLocation();

            while (spawnerState.progress >= 1D) {
                spawnerState.progress -= 1D;
                spawnerState.stored = Math.min(maxStorage, spawnerState.stored + dropPerCycle);

                if (definition.entityType != null && spawnerLoc != null && spawnerLoc.getWorld() != null) {
                    Location spawnAt = spawnerLoc.clone().add(0.5, 1.0, 0.5);
                    try {
                        org.bukkit.entity.Entity mob = spawnerLoc.getWorld().spawnEntity(spawnAt, definition.entityType);
                        spawnedByEvolvedSpawner.put(mob.getUniqueId(), spawnerState.islandId);
                    } catch (Exception ignored) {}
                }

                if (spawnerState.stored >= maxStorage) {
                    spawnerState.progress = 0D;
                    break;
                }
            }
        }
    }

    private void tickGenerators() {
        if (generatorStates.isEmpty()) {
            return;
        }

        int generatorTickSeconds = module.getConfiguration().getGeneratorTickSeconds();
        if (generatorTickSeconds <= 0) generatorTickSeconds = 1;

        for (GeneratorState state : generatorStates.values()) {
            GeneratorSettings settings = config.generators.get(state.trackKey);
            if (settings == null || !settings.enabled || settings.weightedMaterials.isEmpty()) {
                continue;
            }

            Island island = plugin.getGrid().getIslandByUUID(state.islandId);
            if (island == null) continue;

            boolean hasOnlineMember = false;
            for (SuperiorPlayer member : island.getIslandMembers(true)) {
                if (member.isOnline()) { hasOnlineMember = true; break; }
            }
            if (!hasOnlineMember) continue;

            int totalStored = 0;
            for (int count : state.storage.values()) totalStored += count;
            if (totalStored >= settings.maxStorage) continue;

            SkillTrack genTrack = parseSkillTrackOrMining(state.trackKey);
            SuperiorPlayer owner = island.getOwner();
            PlayerState ownerState = owner != null ? getPlayerState(owner.getUniqueId()) : null;
            int resourceAccessPerk = ownerState != null ? getPerkLevels(ownerState, genTrack)[0] : 0;

            state.tickProgress += (double) generatorTickSeconds / settings.productionRateSeconds;

            while (state.tickProgress >= 1.0) {
                state.tickProgress -= 1.0;
                int baseYield = settings.baseYield + (Math.max(1, state.level) - 1) * settings.yieldPerLevel;
                int finalYield = Math.max(1, baseYield);
                int spaceLeft = settings.maxStorage - totalStored;
                int toAdd = Math.min(finalYield, spaceLeft);
                for (int i = 0; i < toAdd; i++) {
                    Material material = chooseWeightedMaterial(settings.weightedMaterials, resourceAccessPerk);
                    if (material == null) {
                        material = chooseWeightedMaterial(settings.weightedMaterials, 0);
                    }
                    if (material == null) continue;
                    state.storage.merge(material, 1, Integer::sum);
                    totalStored++;
                }
                if (totalStored >= settings.maxStorage) break;
            }
        }
    }

    private void checkDailyReset() {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(module.getConfiguration().getResetTimeZone());
        } catch (Exception ignored) {
            zoneId = ZoneId.of("UTC");
        }

        LocalTime resetTime;
        try {
            resetTime = LocalTime.parse(module.getConfiguration().getResetLocalTime());
        } catch (Exception ignored) {
            resetTime = LocalTime.MIDNIGHT;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime candidate = now.withHour(resetTime.getHour()).withMinute(resetTime.getMinute()).withSecond(0).withNano(0);
        if (now.isBefore(candidate)) {
            candidate = candidate.minusDays(1);
        }

        long resetEpoch = candidate.toEpochSecond();
        if (lastDailyResetEpoch >= resetEpoch) {
            return;
        }

        if (lastDailyResetEpoch == 0L) {
            lastDailyResetEpoch = resetEpoch;
            return;
        }

        lastDailyResetEpoch = resetEpoch;

        for (PlayerState playerState : playerStates.values()) {
            playerState.dailyOneblockBroken = 0;
            playerState.dailyOneblockClaimed = false;
            playerState.dailyTaskProgress.clear();
            playerState.claimedDailyTaskStages.clear();
            playerState.challengeProgress.clear();
            playerState.claimedChallenges.clear();

            for (JourneyTaskDefinition task : config.journeyTasks.values()) {
                if (!task.daily) {
                    continue;
                }
                playerState.completedJourney.remove(task.key);
                playerState.claimedJourney.remove(task.key);
                playerState.journeyProgress.remove(task.key);
            }
        }

        assignedDailyChallengeKeys.clear();
        if (!config.dailyChallengePool.isEmpty()) {
            List<String> poolKeys = new ArrayList<>();
            for (DailyChallengeDefinition d : config.dailyChallengePool) poolKeys.add(d.key);
            java.util.Collections.shuffle(poolKeys, random);
            int count = Math.min(config.dailyChallengeCount, poolKeys.size());
            for (int i = 0; i < count; i++) assignedDailyChallengeKeys.add(poolKeys.get(i));
        }

        Bukkit.getOnlinePlayers().forEach(player -> send(player, msg("daily-reset")));
    }

    private void checkGrowthReset() {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            return;
        }
        long currentDay = worlds.get(0).getFullTime() / 24000L;
        if (lastGrowthResetMinecraftDay < 0L) {
            lastGrowthResetMinecraftDay = currentDay;
            return;
        }
        if (currentDay <= lastGrowthResetMinecraftDay) {
            return;
        }
        lastGrowthResetMinecraftDay = currentDay;
        for (IslandState islandState : islandStates.values()) {
            islandState.dailyGrowthCounts.clear();
        }
    }

    private boolean openTreasurePool(SuperiorPlayer superiorPlayer, PlayerState playerState, String poolKeyRaw) {
        String poolKey = poolKeyRaw.toLowerCase(Locale.ENGLISH);
        TreasurePoolDefinition poolDefinition = config.treasurePools.get(poolKey);
        if (poolDefinition == null || poolDefinition.entries.isEmpty()) {
            send(superiorPlayer, msg("treasure-empty-pool").replace("%pool%", poolKeyRaw));
            return false;
        }

        Reward reward = chooseWeightedReward(poolDefinition.entries);
        if (reward == null) {
            send(superiorPlayer, msg("treasure-empty-pool").replace("%pool%", poolDefinition.displayName));
            return false;
        }

        applyReward(superiorPlayer, playerState, reward, "Opened treasure pool: " + poolDefinition.displayName + ".", SkillTrack.MINING);
        return true;
    }

    private Reward chooseWeightedReward(List<WeightedReward> weightedRewards) {
        int totalWeight = 0;
        for (WeightedReward weightedReward : weightedRewards) {
            totalWeight += Math.max(1, weightedReward.weight);
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;

        for (WeightedReward weightedReward : weightedRewards) {
            current += Math.max(1, weightedReward.weight);
            if (roll < current) {
                return weightedReward.reward.copy();
            }
        }

        return weightedRewards.get(weightedRewards.size() - 1).reward.copy();
    }

    private Material chooseOneBlockMaterial(long oneBlockBreaks, int playerPerkLevel) {
        OneBlockStage selectedStage = null;

        for (OneBlockStage stage : config.oneBlockStages) {
            if (oneBlockBreaks >= stage.requiredBreaks) {
                selectedStage = stage;
            }
        }

        if (selectedStage == null || selectedStage.weightedMaterials.isEmpty()) {
            return config.oneBlockFallbackMaterial;
        }

        
        
        Material result = chooseWeightedMaterial(selectedStage.weightedMaterials, playerPerkLevel);
        if (result == null) {
            result = chooseWeightedMaterial(selectedStage.weightedMaterials, 0);
        }
        return result != null ? result : config.oneBlockFallbackMaterial;
    }

    private WeightedMaterial chooseOneBlockWeightedMaterial(long oneBlockBreaks, int miningPerkLevel, int slayingPerkLevel) {
        OneBlockStage selectedStage = null;
        for (OneBlockStage stage : config.oneBlockStages) {
            if (oneBlockBreaks >= stage.requiredBreaks) {
                selectedStage = stage;
            }
        }
        if (selectedStage == null || selectedStage.weightedMaterials.isEmpty()) {
            return null;
        }
        WeightedMaterial result = chooseWeightedMaterialEntry(selectedStage.weightedMaterials, miningPerkLevel, slayingPerkLevel);
        if (result == null) {
            result = chooseWeightedMaterialEntry(selectedStage.weightedMaterials, 0, 0);
        }
        return result;
    }

    private WeightedMaterial chooseWeightedMaterialEntry(List<WeightedMaterial> weightedMaterials, int miningPerkLevel, int slayingPerkLevel) {
        int totalWeight = 0;
        for (WeightedMaterial wm : weightedMaterials) {
            int perkLevel = wm.mobSpawn != null ? slayingPerkLevel : miningPerkLevel;
            if (wm.minPerkLevel > perkLevel) continue;
            totalWeight += Math.max(1, wm.weight);
        }
        if (totalWeight <= 0) return null;
        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (WeightedMaterial wm : weightedMaterials) {
            int perkLevel = wm.mobSpawn != null ? slayingPerkLevel : miningPerkLevel;
            if (wm.minPerkLevel > perkLevel) continue;
            current += Math.max(1, wm.weight);
            if (roll < current) return wm;
        }
        WeightedMaterial last = null;
        for (WeightedMaterial wm : weightedMaterials) {
            int perkLevel = wm.mobSpawn != null ? slayingPerkLevel : miningPerkLevel;
            if (wm.minPerkLevel <= perkLevel) last = wm;
        }
        return last;
    }

    private WeightedMaterial findWeightedMaterialForType(long breaks, Material mat) {
        OneBlockStage stage = null;
        for (OneBlockStage s : config.oneBlockStages) {
            if (breaks >= s.requiredBreaks) stage = s;
        }
        if (stage == null) return null;
        for (WeightedMaterial wm : stage.weightedMaterials) {
            if (wm.material == mat && wm.dropOverride != null) return wm;
        }
        return null;
    }

    private Material chooseWeightedMaterial(List<WeightedMaterial> weightedMaterials) {
        return chooseWeightedMaterial(weightedMaterials, 0);
    }

    private Material chooseWeightedMaterial(List<WeightedMaterial> weightedMaterials, int playerPerkLevel) {
        int totalWeight = 0;
        for (WeightedMaterial weightedMaterial : weightedMaterials) {
            if (weightedMaterial.minPerkLevel > playerPerkLevel) {
                continue;
            }
            totalWeight += Math.max(1, weightedMaterial.weight);
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;

        for (WeightedMaterial weightedMaterial : weightedMaterials) {
            if (weightedMaterial.minPerkLevel > playerPerkLevel) {
                continue;
            }
            current += Math.max(1, weightedMaterial.weight);
            if (roll < current) {
                return weightedMaterial.material;
            }
        }

        
        Material last = null;
        for (WeightedMaterial weightedMaterial : weightedMaterials) {
            if (weightedMaterial.minPerkLevel <= playerPerkLevel) {
                last = weightedMaterial.material;
            }
        }
        return last;
    }

    private void progressJourney(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                 JourneyType type, String value, long amount) {
        if (amount <= 0) {
            return;
        }

        if (config.journeyTasks.isEmpty()) {
            return;
        }

        String normalizedValue = value == null ? null : value.toLowerCase(Locale.ENGLISH);

        List<JourneyTaskDefinition> orderedTasks = new ArrayList<>(config.journeyTasks.values());
        int currentTaskIndex = getCurrentJourneyTaskIndex(playerState, orderedTasks);
        if (currentTaskIndex < 0 || currentTaskIndex >= orderedTasks.size()) {
            return;
        }

        JourneyTaskDefinition currentTask = orderedTasks.get(currentTaskIndex);
        if (playerState.claimedJourney.contains(currentTask.key)) {
            return;
        }

        if (playerState.completedJourney.contains(currentTask.key)) {
            return;
        }

        if (currentTask.type != type || !currentTask.matches(normalizedValue)) {
            return;
        }

        if (type == JourneyType.REACH_SKILL_LEVEL) {
            long current = playerState.journeyProgress.getOrDefault(currentTask.key, 0L);
            long newVal = Math.max(current, amount);
            playerState.journeyProgress.put(currentTask.key, newVal);
            if (newVal >= currentTask.target && !playerState.completedJourney.contains(currentTask.key)) {
                playerState.completedJourney.add(currentTask.key);
                send(superiorPlayer, msg("journey-task-complete").replace("%task%", currentTask.displayName));
            }
            showJourneyBossBar(superiorPlayer, playerState);
            return;
        }

        if (type == JourneyType.REACH_MILESTONE_TIER || type == JourneyType.COMPLETE_DAILY
                || type == JourneyType.REACH_PERK_LEVEL || type == JourneyType.EXPAND_GROWTH_LIMIT) {
            long newCount = playerState.journeyProgress.getOrDefault(currentTask.key, 0L) + amount;
            playerState.journeyProgress.put(currentTask.key, newCount);
            if (newCount >= currentTask.target && !playerState.completedJourney.contains(currentTask.key)) {
                playerState.completedJourney.add(currentTask.key);
                send(superiorPlayer, msg("journey-task-complete").replace("%task%", currentTask.displayName));
            }
            showJourneyBossBar(superiorPlayer, playerState);
            return;
        }

        long progress = playerState.journeyProgress.getOrDefault(currentTask.key, 0L) + amount;
        long cappedProgress = Math.min(currentTask.target, progress);
        playerState.journeyProgress.put(currentTask.key, cappedProgress);

        if (cappedProgress >= currentTask.target) {
            playerState.completedJourney.add(currentTask.key);
            send(superiorPlayer, msg("journey-task-complete").replace("%task%", currentTask.displayName));
        }
        showJourneyBossBar(superiorPlayer, playerState);
    }

    private SkillTrack dailyTaskKeyToSkillTrack(String taskKey) {
        if (DAILY_TASK_KILL_MOBS.equals(taskKey)) {
            return SkillTrack.SLAYING;
        }
        if (DAILY_TASK_HARVEST_CROPS.equals(taskKey)) {
            return SkillTrack.FARMING;
        }
        return SkillTrack.MINING;
    }

    private SkillTrack journeyTypeToSkillTrack(JourneyType type) {
        if (type == null) {
            return SkillTrack.MINING;
        }
        switch (type) {
            case KILL_MOB: return SkillTrack.SLAYING;
            default: return SkillTrack.MINING;
        }
    }

    private void applyReward(SuperiorPlayer superiorPlayer, PlayerState playerState, Reward reward, String context, SkillTrack skillTrack) {
        if (reward.money > 0D) {
            EconomyProvider.EconomyResult result = plugin.getProviders().getEconomyProvider().depositMoney(superiorPlayer, reward.money);
            if (result.hasFailed()) {
                send(superiorPlayer, msg("reward-money-failed").replace("%error%", result.getErrorMessage()));
            }
        }

        if (reward.skillXp > 0L) {
            addSkillXp(superiorPlayer, playerState, skillTrack, reward.skillXp);
        }

        if (reward.playerXp > 0L) {
            addPlayerXp(superiorPlayer, playerState, reward.playerXp);
        }

        if (!reward.items.isEmpty()) {
            superiorPlayer.runIfOnline(player -> {
                for (ItemStack item : reward.items) {
                    giveItemToPlayer(player, item.clone());
                }
            });
        }

        if (!reward.commands.isEmpty()) {
            for (String command : reward.commands) {
                String parsedCommand = applyRewardPlaceholders(command, superiorPlayer, playerState);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            }
        }

        send(superiorPlayer, context);

        StringBuilder rewardsLine = new StringBuilder();
        if (reward.money > 0D) {
            rewardsLine.append('$').append(format(reward.money));
        }
        if (reward.skillXp > 0L) {
            appendRewardPart(rewardsLine, reward.skillXp + " skill-xp");
        }
        if (reward.playerXp > 0L) {
            appendRewardPart(rewardsLine, reward.playerXp + " player-xp");
        }
        if (!reward.items.isEmpty()) {
            appendRewardPart(rewardsLine, reward.items.size() + " item reward(s)");
        }

        if (rewardsLine.length() > 0) {
            send(superiorPlayer, msg("reward-received").replace("%rewards%", rewardsLine.toString()));
        }
    }

    private void appendRewardPart(StringBuilder builder, String text) {
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(text);
    }

    private String applyRewardPlaceholders(String command, SuperiorPlayer superiorPlayer, PlayerState playerState) {
        String islandName = "";
        if (superiorPlayer.hasIsland() && superiorPlayer.getIsland() != null) {
            islandName = superiorPlayer.getIsland().getName();
        }

        return command
                .replace("%player%", superiorPlayer.getName())
                .replace("%island%", islandName)
                .replace("%mining_level%", String.valueOf(playerState.miningLevel))
                .replace("%farming_level%", String.valueOf(playerState.farmingLevel))
                .replace("%slaying_level%", String.valueOf(playerState.slayingLevel))
                .replace("%skill_level%", String.valueOf(playerState.miningLevel))
                .replace("%player_level%", String.valueOf(playerState.playerLevel));
    }

    private void addSkillXp(SuperiorPlayer superiorPlayer, PlayerState state, SkillTrack track, long amount) {
        if (amount <= 0L) {
            return;
        }

        long currentXp = getSkillXp(state, track);
        int previousLevel = getSkillLevel(state, track);
        long newXp = currentXp + amount;
        setSkillXp(state, track, newXp);

        int nextLevel = resolveLevelFromXp(newXp, config.skillLevelThresholds, config.skillLevelStepXp);
        long xpForNext = getRequiredXpForLevel(nextLevel + 1, config.skillLevelThresholds, config.skillLevelStepXp);
        long xpForCurrent = getRequiredXpForLevel(nextLevel, config.skillLevelThresholds, config.skillLevelStepXp);
        long span = Math.max(1L, xpForNext - xpForCurrent);
        float progress = Math.max(0f, Math.min(1f, (float)(newXp - xpForCurrent) / span));

        if (nextLevel > previousLevel) {
            setSkillLevel(state, track, nextLevel);
            progressJourney(superiorPlayer, state, JourneyType.REACH_SKILL_LEVEL,
                track.name().toLowerCase(Locale.ENGLISH), (long) nextLevel);
            superiorPlayer.runIfOnline(player -> {
                send(player, msg("skill-level-up")
                    .replace("%track%", track.displayName)
                    .replace("%level%", String.valueOf(nextLevel)));
                playEffect(player, "sounds:upgrade", 1.0f, 1.0f);
            });
        }
    }

    private long getSkillXp(PlayerState state, SkillTrack track) {
        switch (track) {
            case MINING: return state.miningXp;
            case FARMING: return state.farmingXp;
            case SLAYING: return state.slayingXp;
            default: return state.miningXp;
        }
    }

    private void setSkillXp(PlayerState state, SkillTrack track, long xp) {
        switch (track) {
            case MINING: state.miningXp = xp; break;
            case FARMING: state.farmingXp = xp; break;
            case SLAYING: state.slayingXp = xp; break;
        }
    }

    private int getSkillLevel(PlayerState state, SkillTrack track) {
        switch (track) {
            case MINING: return state.miningLevel;
            case FARMING: return state.farmingLevel;
            case SLAYING: return state.slayingLevel;
            default: return state.miningLevel;
        }
    }

    private void setSkillLevel(PlayerState state, SkillTrack track, int level) {
        switch (track) {
            case MINING: state.miningLevel = level; break;
            case FARMING: state.farmingLevel = level; break;
            case SLAYING: state.slayingLevel = level; break;
        }
    }

    private void addPlayerXp(SuperiorPlayer superiorPlayer, PlayerState state, long amount) {
        if (amount <= 0L) {
            return;
        }

        double multiplier = getPlayerXpMultiplier(superiorPlayer);
        long adjustedAmount = Math.max(1L, Math.round(amount * multiplier));

        state.playerXp += adjustedAmount;

        int previousLevel = state.playerLevel;
        int nextLevel = resolveLevelFromXp(state.playerXp, config.playerLevelThresholds, config.playerLevelStepXp);

        if (nextLevel > previousLevel) {
            state.playerLevel = nextLevel;
            send(superiorPlayer, msg("player-level-up").replace("%level%", String.valueOf(nextLevel)));
            superiorPlayer.runIfOnline(p -> playEffect(p, "sounds:upgrade", 1.0f, 1.2f));
        }
    }

    private double getPlayerXpMultiplier(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) {
            return 1D;
        }

        double multiplier = 1D;

        if (player.hasPermission("oneblockmc.playerxp.multiplier.1_2")) {
            multiplier = Math.max(multiplier, 1.2D);
        }
        if (player.hasPermission("oneblockmc.playerxp.multiplier.1_3")) {
            multiplier = Math.max(multiplier, 1.3D);
        }
        if (player.hasPermission("oneblockmc.playerxp.multiplier.1_5")) {
            multiplier = Math.max(multiplier, 1.5D);
        }
        if (player.hasPermission("oneblockmc.playerxp.multiplier.2_0")) {
            multiplier = Math.max(multiplier, 2.0D);
        }
        if (player.hasPermission("oneblockmc.playerxp.multiplier.2_5")) {
            multiplier = Math.max(multiplier, 2.5D);
        }
        if (player.hasPermission("oneblockmc.playerxp.multiplier.3_0")) {
            multiplier = Math.max(multiplier, 3.0D);
        }

        return multiplier;
    }

    private int resolveLevelFromXp(long xp, TreeMap<Integer, Long> thresholds, long stepXp) {
        int level = 1;

        Integer lastMappedLevel = thresholds.isEmpty() ? null : thresholds.lastKey();
        int maxMappedLevel = lastMappedLevel == null ? 1 : lastMappedLevel;
        for (int targetLevel = 2; targetLevel <= Math.max(maxMappedLevel + 30, 200); targetLevel++) {
            long required = getRequiredXpForLevel(targetLevel, thresholds, stepXp);
            if (xp >= required) {
                level = targetLevel;
            } else {
                break;
            }
        }

        return level;
    }

    private long getRequiredXpForLevel(int level, TreeMap<Integer, Long> thresholds, long stepXp) {
        if (level <= 1) {
            return 0L;
        }

        Long mapped = thresholds.get(level);
        if (mapped != null) {
            return mapped;
        }

        return (level - 1L) * Math.max(1L, stepXp);
    }

    private List<MilestoneDefinition> getMilestonesForTrack(SkillTrack track) {
        switch (track) {
            case FARMING: return config.farmingMilestones;
            case SLAYING: return config.slayingMilestones;
            default:      return config.miningMilestones;
        }
    }

    private void incrementMilestoneProgressByBlock(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                                    Material blockType) {
        for (SkillTrack track : SkillTrack.values()) {
            for (MilestoneDefinition milestone : getMilestonesForTrack(track)) {
                if (!milestone.targetBlocks.contains(blockType)) continue;
                String fullKey = track.name().toLowerCase(Locale.ENGLISH) + ":" + milestone.key;
                long prev = playerState.milestoneProgress.getOrDefault(fullKey, 0L);
                long updated = prev + 1L;
                playerState.milestoneProgress.put(fullKey, updated);
                checkMilestoneTierNotification(superiorPlayer, playerState, milestone, fullKey, updated);
            }
        }
    }

    private void incrementMilestoneProgressByEntity(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                                     EntityType entityType) {
        for (MilestoneDefinition milestone : config.slayingMilestones) {
            if (!milestone.targetEntities.contains(entityType)) continue;
            String fullKey = "slaying:" + milestone.key;
            long prev = playerState.milestoneProgress.getOrDefault(fullKey, 0L);
            long updated = prev + 1L;
            playerState.milestoneProgress.put(fullKey, updated);
            checkMilestoneTierNotification(superiorPlayer, playerState, milestone, fullKey, updated);
        }
    }

    private void checkMilestoneTierNotification(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                                  MilestoneDefinition milestone, String fullKey, long progress) {
        int highestClaimed = playerState.milestoneTiersClaimed.getOrDefault(fullKey, 0);
        for (Map.Entry<Integer, MilestoneTier> entry : milestone.tiers.entrySet()) {
            int tierNum = entry.getKey();
            if (tierNum <= highestClaimed) continue;
            if (progress >= entry.getValue().requiredCount) {
                String notifyKey = fullKey + ":" + tierNum;
                if (playerState.notifiedMilestones.add(notifyKey)) {
                    sendMilestoneNotification(superiorPlayer, milestone, tierNum);
                }
                progressJourney(superiorPlayer, playerState, JourneyType.REACH_MILESTONE_TIER, fullKey, 1L);
            }
            break;
        }
    }

    private boolean claimMilestoneTier(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                     SkillTrack track, MilestoneDefinition milestone) {
        String fullKey = track.name().toLowerCase(Locale.ENGLISH) + ":" + milestone.key;
        long progress = playerState.milestoneProgress.getOrDefault(fullKey, 0L);
        int highestClaimed = playerState.milestoneTiersClaimed.getOrDefault(fullKey, 0);

        MilestoneTier nextTier = null;
        int nextTierNum = -1;
        for (Map.Entry<Integer, MilestoneTier> entry : milestone.tiers.entrySet()) {
            if (entry.getKey() <= highestClaimed) continue;
            nextTier = entry.getValue();
            nextTierNum = entry.getKey();
            break;
        }

        if (nextTier == null) {
            send(superiorPlayer, msg("milestone-all-claimed").replace("%name%", milestone.displayName));
            return false;
        }

        if (progress < nextTier.requiredCount) {
            send(superiorPlayer, msg("milestone-requirements-not-met")
                .replace("%required%", String.valueOf(nextTier.requiredCount))
                .replace("%name%", milestone.displayName)
                .replace("%progress%", String.valueOf(progress)));
            return false;
        }

        playerState.milestoneTiersClaimed.put(fullKey, nextTierNum);

        Reward reward = new Reward();
        reward.skillXp = nextTier.skillXp;
        reward.playerXp = nextTier.playerXp;
        applyReward(superiorPlayer, playerState, reward,
                "Milestone &f" + milestone.displayName + " &7Tier &f" + nextTierNum + " &aclaimed!", track);
        superiorPlayer.runIfOnline(p -> playEffect(p, "sounds:upgrade", 1.0f, 1.0f));

        progressJourney(superiorPlayer, playerState, JourneyType.REACH_MILESTONE_TIER, fullKey, 1L);
        return true;
    }

    private void sendMilestoneNotification(SuperiorPlayer superiorPlayer, MilestoneDefinition milestone, int tierNum) {
        Player player = superiorPlayer.asPlayer();
        if (player == null) return;
        String pfx = (config != null) ? config.messages.getOrDefault("prefix", PREFIX) : PREFIX;
        String milestoneMsg = (config != null)
            ? config.messages.getOrDefault("milestone-ready", "Milestone Ready! %name% Tier %tier% - Open Skills to claim.")
                .replace("%name%", milestone.displayName)
                .replace("%tier%", String.valueOf(tierNum))
            : "Milestone &f" + milestone.displayName + " &7Tier &f" + tierNum + " &7ready to claim!";
        net.md_5.bungee.api.chat.TextComponent msg = new net.md_5.bungee.api.chat.TextComponent(
            color(pfx + milestoneMsg + " "));
        net.md_5.bungee.api.chat.TextComponent link = new net.md_5.bungee.api.chat.TextComponent(
            color("&e[Click to view]"));
        link.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
            net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/skills menu"));
        link.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
            net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
            new net.md_5.bungee.api.chat.TextComponent[]{new net.md_5.bungee.api.chat.TextComponent("Open Skills menu")}));
        msg.addExtra(link);
        player.spigot().sendMessage(msg);
    }

    private int getGrowthLimit(IslandState islandState, String materialKey) {
        int base = config.dailyGrowthLimits.getOrDefault(materialKey, 100);
        int tier = islandState.growthLimitTiers.getOrDefault(materialKey, 0);
        return base + tier * config.growthLimitUpgradeAmount;
    }

    private void rollTreasureDrop(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                   SkillTrack track, String resourceKey) {
        String lowerKey = resourceKey.toLowerCase(Locale.ENGLISH);
        List<TreasureDropLink> links = config.treasureLinks.get(lowerKey);
        if (links == null || links.isEmpty()) return;
        int[] perkLevels = getPerkLevels(playerState, track);
        int treasurePerk = perkLevels.length > 2 ? perkLevels[2] : 0;
        for (TreasureDropLink link : links) {
            double finalChance = link.chance * (1.0 + treasurePerk * config.perkTreasureChanceBonusPerLevel);
            if (random.nextDouble() > finalChance) continue;
            TreasurePoolDefinition pool = config.treasurePools.get(link.pool);
            if (pool == null || pool.entries.isEmpty()) continue;
            Reward reward = chooseWeightedReward(pool.entries);
            if (reward == null) continue;
            applyReward(superiorPlayer, playerState, reward,
                "&6Treasure! &7You found a treasure from the &f" + link.pool + " &7pool.", track);
        }
    }

    @SuppressWarnings("unchecked")
    private Block getTargetBlock(Player player, int range) {
        
        try {
            java.lang.reflect.Method m = player.getClass().getMethod("getTargetBlock", java.util.Set.class, int.class);
            return (Block) m.invoke(player, null, range);
        } catch (Exception e1) {
            try {
                java.lang.reflect.Method m = player.getClass().getMethod("getTargetBlock", java.util.HashSet.class, int.class);
                return (Block) m.invoke(player, null, range);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private void sendActionBar(Player player, String message) {
        
        try {
            Class<?> serializerClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Object serializer = serializerClass.getMethod("legacyAmpersand").invoke(null);
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Object component = serializerClass.getMethod("deserialize", String.class).invoke(serializer, message);
            java.lang.reflect.Method m = player.getClass().getMethod("sendActionBar", componentClass);
            m.invoke(player, component);
            return;
        } catch (Exception e1) {
            plugin.getLogger().warning("[EvolvedSkills] sendActionBar (Adventure) failed: " + e1.getClass().getSimpleName() + ": " + e1.getMessage());
        }
        
        try {
            String colored = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
            java.lang.reflect.Method m = player.getClass().getMethod("sendActionBar", String.class);
            m.invoke(player, colored);
            return;
        } catch (Exception e2) {
            plugin.getLogger().warning("[EvolvedSkills] sendActionBar (String) failed: " + e2.getClass().getSimpleName() + ": " + e2.getMessage());
        }
        
        try {
            String colored = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
            net.md_5.bungee.api.chat.TextComponent component =
                new net.md_5.bungee.api.chat.TextComponent(colored);
            Class<?> chatMsgType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Object actionBar = chatMsgType.getField("ACTION_BAR").get(null);
            java.lang.reflect.Method m2 = player.spigot().getClass().getMethod(
                "sendMessage", chatMsgType, net.md_5.bungee.api.chat.BaseComponent[].class);
            m2.invoke(player.spigot(), actionBar,
                new net.md_5.bungee.api.chat.BaseComponent[]{component});
        } catch (Exception e3) {
            plugin.getLogger().warning("[EvolvedSkills] sendActionBar (BungeeCord) failed: " + e3.getClass().getSimpleName() + ": " + e3.getMessage());
        }
    }

    private void showSkillXpBossBar(Player player, SkillTrack track, int level, float progress) {
        try {
            String barColor = track == SkillTrack.MINING ? "GREEN"
                            : track == SkillTrack.FARMING ? "YELLOW" : "RED";
            String trackColor = track == SkillTrack.MINING ? "&b"
                             : track == SkillTrack.FARMING ? "&a" : "&c";
            String title = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                trackColor + "&l" + sc(track.displayName.toLowerCase(java.util.Locale.ENGLISH))
                + " &8│ &7ʟᴠ &e&l" + level
                + " &8│ " + buildBossBarProgress(progress));
            showBossBar(player, title, progress, barColor, "SOLID", 100L);
        } catch (Exception ignored) {}
    }
    private void sendLevelUpBossBar(Player player, SkillTrack track, int level) {
        try {
            String title = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&6&l✦ " + sc(track.displayName.toLowerCase(java.util.Locale.ENGLISH))
                + " &e&lʟᴇᴠᴇʟ " + level + " &6&l✦");
            showBossBar(player, title, 1f, "YELLOW", "SEGMENTED_10", 120L);
        } catch (Exception ignored) {}
    }
    private String buildBossBarProgress(float progress) {
        int filled = Math.round(progress * 10);
        StringBuilder bar = new StringBuilder("§8┃§a");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append('█');
            } else {
                bar.append("§8░");
            }
        }
        bar.append("§8┃ §f").append((int)(progress * 100)).append("§8%");
        return bar.toString();
    }
    private void showBossBar(Player player, String title, float progress, String colorName, String styleName, long durationTicks) {
        try {
            Class<?> barColorClass = Class.forName("org.bukkit.boss.BarColor");
            Class<?> barStyleClass = Class.forName("org.bukkit.boss.BarStyle");
            Class<?> bossBarClass = Class.forName("org.bukkit.boss.BossBar");
            @SuppressWarnings("unchecked")
            Object color = Enum.valueOf((Class<Enum>) barColorClass, colorName);
            @SuppressWarnings("unchecked")
            Object style = Enum.valueOf((Class<Enum>) barStyleClass, styleName);

            UUID uid = player.getUniqueId();

            BukkitTask existing = activeBossBarTasks.remove(uid);
            if (existing != null) {
                existing.cancel();
            }

            Class<?> barFlagClass = Class.forName("org.bukkit.boss.BarFlag");
            Object emptyFlags = java.lang.reflect.Array.newInstance(barFlagClass, 0);

            Object bossBar = activeBossBars.get(uid);
            if (bossBar == null) {
                java.lang.reflect.Method create = Bukkit.class.getMethod("createBossBar",
                    String.class, barColorClass, barStyleClass, emptyFlags.getClass());
                bossBar = create.invoke(null, title, color, style, emptyFlags);
                bossBarClass.getMethod("addPlayer", Player.class).invoke(bossBar, player);
                activeBossBars.put(uid, bossBar);
            } else {
                bossBarClass.getMethod("setTitle", String.class).invoke(bossBar, title);
                bossBarClass.getMethod("setColor", barColorClass).invoke(bossBar, color);
                bossBarClass.getMethod("setStyle", barStyleClass).invoke(bossBar, style);
                bossBarClass.getMethod("setVisible", boolean.class).invoke(bossBar, true);
            }

            bossBarClass.getMethod("setProgress", double.class).invoke(bossBar, (double) Math.max(0f, Math.min(1f, progress)));

            final Object finalBar = bossBar;
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    finalBar.getClass().getMethod("setVisible", boolean.class).invoke(finalBar, false);
                } catch (Exception ignored) {}
                activeBossBarTasks.remove(uid);
            }, durationTicks);
            activeBossBarTasks.put(uid, task);
        } catch (Exception e) {
            plugin.getLogger().warning("[EvolvedSkills] Boss bar failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void broadcastSkillLevelUp(SuperiorPlayer superiorPlayer, SkillTrack track, int level) {
        Island island = superiorPlayer.getIsland();
        if (island == null) {
            return;
        }
        int nextPerkUnlock = -1;
        for (int i = 0; i < config.perkUnlockLevels.length; i++) {
            if (config.perkUnlockLevels[i] > level) {
                nextPerkUnlock = config.perkUnlockLevels[i];
                break;
            }
        }
        String nextPerkLine = nextPerkUnlock > 0
            ? "\u00a77  Next perk unlock: Level " + nextPerkUnlock
            : "\u00a77  All perks unlocked!";
        String line1 = "\u00a78\u00a7m                    ";
        String line2 = "\u00a76\u00a7l  SKILL LEVEL UP!";
        String line3 = "\u00a7a  " + superiorPlayer.getName() + " \u00a77reached "
            + track.displayName + " Level \u00a7f" + level + "\u00a77!";
        String line4 = nextPerkLine;
        String line5 = "\u00a78\u00a7m                    ";
        for (SuperiorPlayer member : island.getIslandMembers(true)) {
            member.runIfOnline(p -> {
                p.sendMessage(line1);
                p.sendMessage(line2);
                p.sendMessage(line3);
                p.sendMessage(line4);
                p.sendMessage(line5);
            });
        }
    }

    private void showMilestoneProgressBossBar(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                               SkillTrack track, Material blockType) {
        String trackKey = track.name().toLowerCase(Locale.ENGLISH);
        for (MilestoneDefinition ms : getMilestonesForTrack(track)) {
            if (!ms.targetBlocks.contains(blockType)) continue;
            showSpecificMilestoneBossBar(superiorPlayer, playerState, track, ms, trackKey);
            return;
        }
    }

    private void showMilestoneProgressBossBar(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                               EntityType entityType) {
        for (MilestoneDefinition ms : config.slayingMilestones) {
            if (!ms.targetEntities.contains(entityType)) continue;
            showSpecificMilestoneBossBar(superiorPlayer, playerState, SkillTrack.SLAYING, ms, "slaying");
            return;
        }
    }

    private void showSpecificMilestoneBossBar(SuperiorPlayer superiorPlayer, PlayerState playerState,
                                               SkillTrack track, MilestoneDefinition ms, String trackKey) {
        String fullKey = trackKey + ":" + ms.key;
        int highestClaimed = playerState.milestoneTiersClaimed.getOrDefault(fullKey, 0);
        MilestoneTier nextTier = null;
        int nextTierNum = -1;
        for (Map.Entry<Integer, MilestoneTier> entry : ms.tiers.entrySet()) {
            if (entry.getKey() <= highestClaimed) continue;
            nextTier = entry.getValue();
            nextTierNum = entry.getKey();
            break;
        }
        if (nextTier == null) return;
        long current = playerState.milestoneProgress.getOrDefault(fullKey, 0L);
        float pct = nextTier.requiredCount > 0 ? Math.min(1f, (float) current / nextTier.requiredCount) : 1f;
        String barColor = track == SkillTrack.MINING ? "GREEN"
                        : track == SkillTrack.FARMING ? "YELLOW" : "RED";
        String bossTitle = org.bukkit.ChatColor.translateAlternateColorCodes('&',
            "&f&l" + ms.displayName + " &8│ &7ᴛ" + nextTierNum + " &8│ &a" + current + "&8/&f" + nextTier.requiredCount);
        String actionBarMsg = "&b" + ms.displayName + " &8» &a" + current + " &8/ &f" + nextTier.requiredCount + " &8[ᴛ" + nextTierNum + "]";
        final String finalActionBar = actionBarMsg;
        superiorPlayer.runIfOnline(player -> sendActionBar(player, finalActionBar));
    }

    private void showJourneyBossBar(SuperiorPlayer superiorPlayer, PlayerState playerState) {
        if (config == null || config.journeyTasks.isEmpty()) return;
        List<JourneyTaskDefinition> tasks = new ArrayList<>(config.journeyTasks.values());
        int idx = getCurrentJourneyTaskIndex(playerState, tasks);
        JourneyTaskDefinition task = tasks.get(idx);
        boolean allClaimed = playerState.claimedJourney.contains(task.key) && idx == tasks.size() - 1;
        String title;
        float pct;
        if (allClaimed) {
            title = color("&a&l✔ ᨔᴏᴜʀɴᴇʏ ᴄᴏᴍᴘʟᴇᴛᴇ!");
            pct = 1f;
        } else {
            long current = playerState.journeyProgress.getOrDefault(task.key, 0L);
            pct = task.target > 0 ? Math.min(1f, (float) current / task.target) : 0f;
            title = color("&e◆ &f" + task.displayName + " &8│ &a" + current + "&8/&f" + task.target);
        }
        final String finalTitle = title;
        final float finalPct = pct;
        superiorPlayer.runIfOnline(player -> showPersistentBossBar(player, finalTitle, finalPct, "BLUE", "SOLID"));
    }

    private void showPersistentBossBar(Player player, String title, float progress, String colorName, String styleName) {
        try {
            Class<?> barColorClass = Class.forName("org.bukkit.boss.BarColor");
            Class<?> barStyleClass = Class.forName("org.bukkit.boss.BarStyle");
            Class<?> bossBarClass = Class.forName("org.bukkit.boss.BossBar");
            @SuppressWarnings("unchecked")
            Object color = Enum.valueOf((Class<Enum>) barColorClass, colorName);
            @SuppressWarnings("unchecked")
            Object style = Enum.valueOf((Class<Enum>) barStyleClass, styleName);

            UUID uid = player.getUniqueId();

            Class<?> barFlagClass = Class.forName("org.bukkit.boss.BarFlag");
            Object emptyFlags = java.lang.reflect.Array.newInstance(barFlagClass, 0);

            Object bossBar = journeyBossBars.get(uid);
            if (bossBar == null) {
                java.lang.reflect.Method create = Bukkit.class.getMethod("createBossBar",
                    String.class, barColorClass, barStyleClass, emptyFlags.getClass());
                bossBar = create.invoke(null, title, color, style, emptyFlags);
                bossBarClass.getMethod("addPlayer", Player.class).invoke(bossBar, player);
                journeyBossBars.put(uid, bossBar);
            } else {
                bossBarClass.getMethod("setTitle", String.class).invoke(bossBar, title);
                bossBarClass.getMethod("setColor", barColorClass).invoke(bossBar, color);
                bossBarClass.getMethod("setStyle", barStyleClass).invoke(bossBar, style);
                bossBarClass.getMethod("setVisible", boolean.class).invoke(bossBar, true);
            }

            bossBarClass.getMethod("setProgress", double.class).invoke(bossBar, (double) Math.max(0f, Math.min(1f, progress)));
        } catch (Exception e) {
            plugin.getLogger().warning("[EvolvedSkills] Journey boss bar failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private String resolveFragmentDisplayName(String key) {
        if (config != null && config.nodeTypes.containsKey(key)) {
            return config.nodeTypes.get(key).displayName + " Node Fragment";
        }
        if (config != null && config.spawnerTypes.containsKey(key)) {
            return config.spawnerTypes.get(key).displayName + " Spawner Fragment";
        }
        StringBuilder name = new StringBuilder();
        for (String word : key.replace('_', ' ').split(" ")) {
            if (word.isEmpty()) continue;
            if (name.length() > 0) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        name.append(" Resource Fragment");
        return name.toString();
    }

    private ItemStack createFragmentItem(String resourceKey, String displayName, int amount) {
        List<String> lore = new ArrayList<>();
        for (String line : config.fragmentItemLore) {
            lore.add(line.replace("%type%", displayName));
        }
        String nexoId = FRAGMENT_NEXO_IDS.get(resourceKey.toLowerCase(Locale.ENGLISH));
        String tagData = "FRAGMENT|" + resourceKey.toLowerCase(Locale.ENGLISH);

        
        if (nexoId != null) {
            try {
                Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
                Object nexoItem = nexoItemsClass.getMethod("itemFromId", String.class).invoke(null, nexoId);
                if (nexoItem != null) {
                    ItemStack nexoBase = (ItemStack) nexoItem.getClass().getMethod("build").invoke(nexoItem);
                    if (nexoBase != null && nexoBase.getType() != Material.AIR) {
                        nexoBase.setAmount(Math.max(1, amount));
                        ItemMeta meta = nexoBase.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(color(displayName));
                            List<String> coloredLore = new ArrayList<>();
                            for (String line : lore) coloredLore.add(color(line));
                            if (!writePdcTag(meta, tagData)) {
                                coloredLore.add(ITEM_TAG_PREFIX + tagData);
                            }
                            meta.setLore(coloredLore);
                            nexoBase.setItemMeta(meta);
                        }
                        return nexoBase;
                    }
                }
            } catch (Exception ignored) {}
        }

        
        int cmd = nexoId != null ? FRAGMENT_CMD_MAP.getOrDefault(nexoId, 0) : 0;
        if (cmd > 0) {
            ItemStack item = createTaggedItem(Material.PAPER, Math.max(1, amount), displayName, lore, "FRAGMENT", resourceKey.toLowerCase(Locale.ENGLISH));
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    try {
                        Class.forName("org.bukkit.inventory.meta.ItemMeta").getMethod("setCustomModelData", Integer.class).invoke(meta, cmd);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[EvolvedSkills] setCustomModelData failed for fragment '" + resourceKey + "' (cmd=" + cmd + "): " + e.getMessage());
                    }
                    item.setItemMeta(meta);
                }
            }
            return item;
        }
        return createTaggedItem(config.fragmentItemMaterial, Math.max(1, amount), displayName, lore, "FRAGMENT", resourceKey.toLowerCase(Locale.ENGLISH));
    }

    private void rollFragmentDrop(SuperiorPlayer superiorPlayer, PlayerState playerState, String resourceKey, SkillTrack track, double baseChance) {
        int[] perkLevels = getPerkLevels(playerState, track);
        int fragmentPerk = perkLevels.length > 1 ? perkLevels[1] : 0;
        double totalChance = baseChance * (1.0 + fragmentPerk * config.fragmentChancePerLevel);
        if (random.nextDouble() >= totalChance) {
            return;
        }
        String nexoId = FRAGMENT_NEXO_IDS.get(resourceKey.toLowerCase(Locale.ENGLISH));
        if (nexoId == null) return;
        String baseKey = nexoId.replace("_fragment", "");
        String humanKey = baseKey.replace("_", " ");
        humanKey = humanKey.substring(0, 1).toUpperCase(Locale.ENGLISH) + humanKey.substring(1);
        String displayName = color(config.fragmentItemName.replace("%type%", humanKey));
        ItemStack fragment = createFragmentItem(resourceKey, displayName, 1);
        superiorPlayer.runIfOnline(player -> giveItemToPlayer(player, fragment));
    }

    private int countFragmentsInInventory(Player player, String resourceKey) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == AIR_MATERIAL) {
                continue;
            }
            String tag = getItemTag(item, "FRAGMENT");
            if (tag != null && tag.equalsIgnoreCase(resourceKey)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void consumeFragmentsFromInventory(Player player, String resourceKey, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == AIR_MATERIAL) {
                continue;
            }
            String tag = getItemTag(item, "FRAGMENT");
            if (tag == null || !tag.equalsIgnoreCase(resourceKey)) {
                continue;
            }
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
        player.updateInventory();
    }

    private int countTrackFragmentsInInventory(Player player, SkillTrack track) {
        int total = 0;
        for (MilestoneDefinition def : getMilestonesForTrack(track)) {
            total += countFragmentsInInventory(player, def.key);
        }
        return total;
    }

    private void consumeTrackFragmentsFromInventory(Player player, SkillTrack track, int amount) {
        int remaining = amount;
        for (MilestoneDefinition def : getMilestonesForTrack(track)) {
            if (remaining <= 0) break;
            int available = countFragmentsInInventory(player, def.key);
            if (available <= 0) continue;
            int toConsume = Math.min(available, remaining);
            consumeFragmentsFromInventory(player, def.key, toConsume);
            remaining -= toConsume;
        }
    }

    private PlayerState getPlayerState(UUID playerId) {
        PlayerState playerState = playerStates.get(playerId);
        if (playerState == null) {
            playerState = new PlayerState();
            playerStates.put(playerId, playerState);
        }
        return playerState;
    }

    private IslandState getIslandState(UUID islandId) {
        IslandState islandState = islandStates.get(islandId);
        if (islandState == null) {
            islandState = new IslandState();
            islandStates.put(islandId, islandState);
        }
        return islandState;
    }

    private boolean withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        if (amount <= 0D) {
            return true;
        }

        EconomyProvider.EconomyResult result = plugin.getProviders().getEconomyProvider().withdrawMoney(superiorPlayer, amount);
        return !result.hasFailed();
    }

    private void send(CommandSender sender, String message) {
        String pfx = (config != null) ? config.messages.getOrDefault("prefix", PREFIX) : PREFIX;
        sender.sendMessage(color(pfx + message));
    }

    private void send(SuperiorPlayer superiorPlayer, String message) {
        superiorPlayer.runIfOnline(player -> send(player, message));
    }

    private void playEffect(Player player, String soundName, float volume, float pitch) {
        try {
            player.getClass()
                .getMethod("playSound", org.bukkit.Location.class, String.class, float.class, float.class)
                .invoke(player, player.getLocation(), soundName, volume, pitch);
            if ("sounds:upgrade".equals(soundName)) {
                upgradeSoundPlayed.add(player.getUniqueId());
            }
        } catch (Exception ignored) {}
    }

    private String color(String message) {
        if (message == null) {
            return null;
        }

        String withGlyphs = applyResourcePackGlyphs(message);
        return ChatColor.translateAlternateColorCodes('&', withGlyphs);
    }

    private String applyResourcePackGlyphs(String text) {
        String output = text;
        output = applyItemsAdderGlyphs(output);
        output = applyOraxenGlyphs(output);
        output = applyNexoGlyphs(output);
        return output;
    }

    private String applyItemsAdderGlyphs(String text) {
        if (text == null || text.isEmpty() || !Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            return text;
        }

        Method method = resolveItemsAdderGlyphMethod();
        if (method == null) {
            return text;
        }

        try {
            Object output = method.invoke(null, text);
            return output instanceof String ? (String) output : text;
        } catch (Throwable ignored) {
            return text;
        }
    }

    private Method resolveItemsAdderGlyphMethod() {
        if (itemsAdderGlyphMethodResolved) {
            return itemsAdderReplaceFontImagesMethod;
        }

        itemsAdderGlyphMethodResolved = true;

        try {
            Class<?> fontImageWrapperClass = Class.forName("dev.lone.itemsadder.api.FontImages.FontImageWrapper");
            itemsAdderReplaceFontImagesMethod = fontImageWrapperClass.getMethod("replaceFontImages", String.class);
        } catch (Throwable ignored) {
            itemsAdderReplaceFontImagesMethod = null;
        }

        return itemsAdderReplaceFontImagesMethod;
    }

    private String applyOraxenGlyphs(String text) {
        if (text == null || text.isEmpty() || !Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
            return text;
        }

        if (!resolveOraxenGlyphMethods()) {
            return text;
        }

        try {
            Object oraxenPlugin = oraxenPluginGetMethod.invoke(null);
            Object fontManager = oraxenGetFontManagerMethod.invoke(oraxenPlugin);
            Object mapObject = oraxenGetGlyphByPlaceholderMapMethod.invoke(fontManager);
            return replaceGlyphPlaceholders(text, mapObject, true);
        } catch (Throwable ignored) {
            return text;
        }
    }

    private boolean resolveOraxenGlyphMethods() {
        if (oraxenGlyphMethodsResolved) {
            return oraxenPluginGetMethod != null
                    && oraxenGetFontManagerMethod != null
                    && oraxenGetGlyphByPlaceholderMapMethod != null;
        }

        oraxenGlyphMethodsResolved = true;

        try {
            Class<?> oraxenPluginClass = Class.forName("io.th0rgal.oraxen.OraxenPlugin");
            oraxenPluginGetMethod = oraxenPluginClass.getMethod("get");
            oraxenGetFontManagerMethod = oraxenPluginClass.getMethod("getFontManager");

            Class<?> fontManagerClass = Class.forName("io.th0rgal.oraxen.font.FontManager");
            oraxenGetGlyphByPlaceholderMapMethod = fontManagerClass.getMethod("getGlyphByPlaceholderMap");

            Class<?> glyphClass = Class.forName("io.th0rgal.oraxen.font.Glyph");
            oraxenGlyphGetCharacterMethod = glyphClass.getMethod("getCharacter");
        } catch (Throwable ignored) {
            oraxenPluginGetMethod = null;
            oraxenGetFontManagerMethod = null;
            oraxenGetGlyphByPlaceholderMapMethod = null;
            oraxenGlyphGetCharacterMethod = null;
        }

        return oraxenPluginGetMethod != null
                && oraxenGetFontManagerMethod != null
                && oraxenGetGlyphByPlaceholderMapMethod != null;
    }

    private String applyNexoGlyphs(String text) {
        if (text == null || text.isEmpty() || !Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            return text;
        }

        if (!resolveNexoGlyphMethods()) {
            return text;
        }

        try {
            Object nexoPlugin = nexoPluginInstanceMethod.invoke(null);
            Object fontManager = nexoFontManagerMethod.invoke(nexoPlugin);
            Object mapObject = nexoGetPlaceholderGlyphMapMethod.invoke(fontManager);
            return replaceGlyphPlaceholders(text, mapObject, false);
        } catch (Throwable ignored) {
            return text;
        }
    }

    private boolean resolveNexoGlyphMethods() {
        if (nexoGlyphMethodsResolved) {
            return nexoPluginInstanceMethod != null
                    && nexoFontManagerMethod != null
                    && nexoGetPlaceholderGlyphMapMethod != null;
        }

        nexoGlyphMethodsResolved = true;

        try {
            Class<?> nexoPluginClass = Class.forName("com.nexomc.nexo.NexoPlugin");
            nexoPluginInstanceMethod = nexoPluginClass.getMethod("instance");
            nexoFontManagerMethod = nexoPluginClass.getMethod("fontManager");

            Class<?> fontManagerClass = Class.forName("com.nexomc.nexo.fonts.FontManager");
            nexoGetPlaceholderGlyphMapMethod = fontManagerClass.getMethod("getPlaceholderGlyphMap");

            Class<?> glyphClass = Class.forName("com.nexomc.nexo.fonts.Glyph");
            nexoGlyphCharacterMethod = glyphClass.getMethod("character");
        } catch (Throwable ignored) {
            nexoPluginInstanceMethod = null;
            nexoFontManagerMethod = null;
            nexoGetPlaceholderGlyphMapMethod = null;
            nexoGlyphCharacterMethod = null;
        }

        return nexoPluginInstanceMethod != null
                && nexoFontManagerMethod != null
                && nexoGetPlaceholderGlyphMapMethod != null;
    }

    private String replaceGlyphPlaceholders(String text, Object mapObject, boolean oraxenMap) {
        if (!(mapObject instanceof Map)) {
            return text;
        }

        @SuppressWarnings("unchecked")
        Map<Object, Object> placeholderMap = (Map<Object, Object>) mapObject;
        if (placeholderMap.isEmpty()) {
            return text;
        }

        String output = text;

        for (Map.Entry<Object, Object> entry : placeholderMap.entrySet()) {
            Object keyObject = entry.getKey();
            Object glyphObject = entry.getValue();

            if (!(keyObject instanceof String) || glyphObject == null) {
                continue;
            }

            String placeholder = (String) keyObject;
            if (placeholder.isEmpty() || !output.contains(placeholder)) {
                continue;
            }

            String glyphCharacter = resolveGlyphCharacter(glyphObject, oraxenMap);
            if (glyphCharacter == null || glyphCharacter.isEmpty()) {
                continue;
            }

            output = output.replace(placeholder, glyphCharacter);
        }

        return output;
    }

    private String resolveGlyphCharacter(Object glyphObject, boolean oraxenMap) {
        try {
            Method glyphMethod = oraxenMap ? oraxenGlyphGetCharacterMethod : nexoGlyphCharacterMethod;
            if (glyphMethod == null || !glyphMethod.getDeclaringClass().isInstance(glyphObject)) {
                glyphMethod = glyphObject.getClass().getMethod(oraxenMap ? "getCharacter" : "character");
                if (oraxenMap) {
                    oraxenGlyphGetCharacterMethod = glyphMethod;
                } else {
                    nexoGlyphCharacterMethod = glyphMethod;
                }
            }

            Object value = glyphMethod.invoke(glyphObject);
            return value instanceof String ? (String) value : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String format(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    private static String sc(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (Character.toLowerCase(c)) {
                case 'a': b.append('ᴀ'); break; case 'b': b.append('ʙ'); break;
                case 'c': b.append('ᴄ'); break; case 'd': b.append('ᴅ'); break;
                case 'e': b.append('ᴇ'); break; case 'f': b.append('ꜰ'); break;
                case 'g': b.append('ɢ'); break; case 'h': b.append('ʜ'); break;
                case 'i': b.append('ɪ'); break; case 'j': b.append('ᴊ'); break;
                case 'k': b.append('ᴋ'); break; case 'l': b.append('ʟ'); break;
                case 'm': b.append('ᴍ'); break; case 'n': b.append('ɴ'); break;
                case 'o': b.append('ᴏ'); break; case 'p': b.append('ᴘ'); break;
                case 'q': b.append('ǫ'); break; case 'r': b.append('ʀ'); break;
                case 's': b.append('ꜱ'); break; case 't': b.append('ᴛ'); break;
                case 'u': b.append('ᴜ'); break; case 'v': b.append('ᴠ'); break;
                case 'w': b.append('ᴡ'); break; case 'x': b.append('x'); break;
                case 'y': b.append('ʏ'); break; case 'z': b.append('ᴢ'); break;
                default: b.append(c);
            }
        }
        return b.toString();
    }

    private String formatPercent(double ratio) {
        return format(ratio * 100D) + "%";
    }

    private String buildGoalProgressBar(long progress, long goal) {
        if (goal <= 0L) {
            return buildGoalProgressBar(progress > 0L ? 1D : 0D);
        }

        return buildGoalProgressBar((double) progress / (double) goal);
    }

    private String buildGoalProgressBar(double ratio) {
        double clampedRatio = Math.max(0D, Math.min(1D, ratio));
        int filledSegments = (int) Math.round(clampedRatio * GOAL_PROGRESS_BAR_SEGMENTS);
        filledSegments = Math.max(0, Math.min(GOAL_PROGRESS_BAR_SEGMENTS, filledSegments));

        StringBuilder builder = new StringBuilder("&a");
        builder.append(repeatBarSegment('\u2588', filledSegments));
        builder.append("&8").append(repeatBarSegment('\u2591', GOAL_PROGRESS_BAR_SEGMENTS - filledSegments));
        builder.append(" &f").append(formatPercent(clampedRatio));
        return builder.toString();
    }

    private String repeatBarSegment(char symbol, int length) {
        if (length <= 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(symbol);
        }

        return builder.toString();
    }

    private String prettyMaterial(Material material) {
        String lower = material.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        StringBuilder output = new StringBuilder();
        for (String part : lower.split(" ")) {
            if (part.isEmpty()) {
                continue;
            }
            if (output.length() > 0) {
                output.append(' ');
            }
            output.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return output.toString();
    }

    private LocationKey getTargetBlockKey(Player player, int maxDistance) {
        try {
            @SuppressWarnings("deprecation")
            Block block = player.getTargetBlock((Set<Material>) null, maxDistance);
            if (block == null || block.getType() == AIR_MATERIAL) {
                return null;
            }
            return LocationKey.fromBlock(block);
        } catch (Exception ignored) {
            return null;
        }
    }

    private ItemStack createGeneratorItem(String trackKey, int amount) {
        String trackDisplay = trackKey.substring(0, 1).toUpperCase(Locale.ENGLISH) + trackKey.substring(1);
        Material mat = "farming".equalsIgnoreCase(trackKey) ? menuMaterial("HAY_BLOCK", "SPONGE") :
                       "slaying".equalsIgnoreCase(trackKey) ? menuMaterial("NETHER_BRICKS", "NETHER_BRICK") :
                       menuMaterial("IRON_BLOCK", "IRON_ORE");
        return createTaggedItem(mat, amount, "&e" + sc(trackDisplay) + " ɢᴇɴᴇʀᴀᴛᴏʀ",
            asList(
                color("&7Passively produces " + trackDisplay.toLowerCase() + " resources"),
                color("&7over time while you are online."),
                color("&7"),
                color("&bʀɪɢʜᴛ-ᴄʟɪᴄᴋ &7to open storage.")
            ),
            "GENERATOR", trackKey.toLowerCase(Locale.ENGLISH));
    }

    private ItemStack createNodeItem(String nodeType, int amount) {
        NodeTypeDefinition definition = config.nodeTypes.get(nodeType.toLowerCase(Locale.ENGLISH));
        String displayName = config.nodeItemName.replace("%type%", definition == null ? nodeType : definition.displayName);

        List<String> lore = new ArrayList<>();
        for (String loreLine : config.nodeItemLore) {
            lore.add(loreLine.replace("%type%", definition == null ? nodeType : definition.displayName));
        }

        String tagData = "NODE|" + nodeType.toLowerCase(Locale.ENGLISH);

        
        if (definition != null && definition.nexoItemId != null) {
            try {
                Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
                Object nexoItem = nexoItemsClass.getMethod("itemFromId", String.class).invoke(null, definition.nexoItemId);
                if (nexoItem != null) {
                    ItemStack nexoBase = (ItemStack) nexoItem.getClass().getMethod("build").invoke(nexoItem);
                    if (nexoBase != null && nexoBase.getType() != Material.AIR) {
                        nexoBase.setAmount(Math.max(1, amount));
                        ItemMeta meta = nexoBase.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(color(displayName));
                            List<String> coloredLore = new ArrayList<>();
                            for (String line : lore) coloredLore.add(color(line));
                            if (!writePdcTag(meta, tagData)) {
                                coloredLore.add(ITEM_TAG_PREFIX + tagData);
                            }
                            meta.setLore(coloredLore);
                            nexoBase.setItemMeta(meta);
                        }
                        return nexoBase;
                    }
                }
            } catch (Exception ignored) {}
        }

        
        Material material = (definition != null && definition.displayBlock != null && definition.displayBlock != AIR_MATERIAL)
                ? definition.displayBlock : config.nodeItemMaterial;
        return createTaggedItem(material, amount, displayName, lore, "NODE", nodeType.toLowerCase(Locale.ENGLISH));
    }

    private ItemStack createSpawnerItem(String spawnerType, int amount) {
        SpawnerTypeDefinition definition = config.spawnerTypes.get(spawnerType.toLowerCase(Locale.ENGLISH));
        String displayName = config.spawnerItemName.replace("%type%", definition == null ? spawnerType : definition.displayName);

        List<String> lore = new ArrayList<>();
        for (String loreLine : config.spawnerItemLore) {
            lore.add(loreLine.replace("%type%", definition == null ? spawnerType : definition.displayName));
        }

        return createTaggedItem(resolveSpawnerItemMaterial(definition), amount, displayName, lore,
                "SPAWNER", spawnerType.toLowerCase(Locale.ENGLISH));
    }

    private Material resolveSpawnerItemMaterial(SpawnerTypeDefinition definition) {
        Material spawner = parseMaterial("SPAWNER");
        return spawner != null ? spawner : menuMaterial("MOB_SPAWNER", "STONE");
    }

    private ItemStack createTreasureToken(String poolName, int amount) {
        TreasurePoolDefinition definition = config.treasurePools.get(poolName.toLowerCase(Locale.ENGLISH));
        String displayName = config.treasureItemName.replace("%pool%", definition == null ? poolName : definition.displayName);

        List<String> lore = new ArrayList<>();
        for (String loreLine : config.treasureItemLore) {
            lore.add(loreLine.replace("%pool%", definition == null ? poolName : definition.displayName));
        }

        return createTaggedItem(config.treasureItemMaterial, amount, displayName, lore, "TREASURE", poolName.toLowerCase(Locale.ENGLISH));
    }

    private boolean writePdcTag(ItemMeta meta, String tagData) {
        try {
            Class<?> nskClass = Class.forName("org.bukkit.NamespacedKey");
            Class<?> pdtClass = Class.forName("org.bukkit.persistence.PersistentDataType");
            Class<?> pdcClass = Class.forName("org.bukkit.persistence.PersistentDataContainer");
            Class<?> itemMetaApiClass = Class.forName("org.bukkit.inventory.meta.ItemMeta");
            Object pdc = itemMetaApiClass.getMethod("getPersistentDataContainer").invoke(meta);
            Object nsKey;
            try {
                nsKey = nskClass.getConstructor(String.class, String.class).newInstance("eskills", "tag");
            } catch (Exception e2) {
                nsKey = nskClass.getConstructor(org.bukkit.plugin.Plugin.class, String.class).newInstance(plugin, "eskill_tag");
            }
            Object stringType = pdtClass.getField("STRING").get(null);
            java.lang.reflect.Method setMethod = null;
            for (java.lang.reflect.Method m : pdcClass.getMethods()) {
                if ("set".equals(m.getName()) && m.getParameterCount() == 3) { setMethod = m; break; }
            }
            if (setMethod != null) {
                setMethod.invoke(pdc, nsKey, stringType, tagData);
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[EvolvedSkills] PDC write failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) plugin.getLogger().warning("[EvolvedSkills] PDC cause: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
        return false;
    }

    private ItemStack createTaggedItem(Material material, int amount, String name, List<String> lore,
                                       String tagType, String tagValue) {
        ItemStack itemStack = new ItemStack(material == null ? Material.STONE : material, Math.max(1, amount));
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(color(name));

            List<String> coloredLore = new ArrayList<>();
            for (String loreLine : lore) {
                coloredLore.add(color(loreLine));
            }
            itemMeta.setLore(coloredLore);

            String tagData = tagType + "|" + tagValue.toLowerCase(Locale.ENGLISH);
            if (!writePdcTag(itemMeta, tagData)) {
                List<String> withTag = new ArrayList<>(coloredLore);
                withTag.add(ITEM_TAG_PREFIX + tagData);
                itemMeta.setLore(withTag);
            }

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    private String getItemTag(ItemStack itemStack, String expectedType) {
        if (itemStack == null || itemStack.getType() == AIR_MATERIAL || !itemStack.hasItemMeta()) {
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        
        try {
            Class<?> nskClass = Class.forName("org.bukkit.NamespacedKey");
            Class<?> pdtClass = Class.forName("org.bukkit.persistence.PersistentDataType");
            Class<?> pdcClass = Class.forName("org.bukkit.persistence.PersistentDataContainer");
            Class<?> itemMetaApiClass = Class.forName("org.bukkit.inventory.meta.ItemMeta");
            java.lang.reflect.Method getpdcMethod = itemMetaApiClass.getMethod("getPersistentDataContainer");
            Object pdc = getpdcMethod.invoke(itemMeta);
            Object key;
            try {
                key = nskClass.getConstructor(String.class, String.class).newInstance("eskills", "tag");
            } catch (Exception e2) {
                key = nskClass.getConstructor(org.bukkit.plugin.Plugin.class, String.class).newInstance(plugin, "eskill_tag");
            }
            Object stringType = pdtClass.getField("STRING").get(null);
            java.lang.reflect.Method getMethod = null;
            for (java.lang.reflect.Method m : pdcClass.getMethods()) {
                if ("get".equals(m.getName()) && m.getParameterCount() == 2) {
                    getMethod = m;
                    break;
                }
            }
            if (getMethod != null) {
                Object raw = getMethod.invoke(pdc, key, stringType);
                if (raw instanceof String) {
                    String[] split = ((String) raw).split("\\|", 2);
                    if (split.length == 2 && split[0].equalsIgnoreCase(expectedType)) {
                        return split[1];
                    }
                    return null;
                }
            }
        } catch (Exception ignored) {}

        
        if (!itemMeta.hasLore()) return null;
        List<String> lore = itemMeta.getLore();
        if (lore == null) return null;

        for (String line : lore) {
            if (line == null || !line.startsWith(ITEM_TAG_PREFIX)) continue;
            String data = line.substring(ITEM_TAG_PREFIX.length());
            String[] split = data.split("\\|", 2);
            if (split.length != 2) continue;
            if (!split[0].equalsIgnoreCase(expectedType)) continue;
            return split[1];
        }

        return null;
    }

    private void consumeOneItem(ItemStack itemStack) {
        int amount = itemStack.getAmount();
        if (amount <= 1) {
            itemStack.setAmount(0);
        } else {
            itemStack.setAmount(amount - 1);
        }
    }

    private static void suppressBlockDrops(BlockBreakEvent event) {
        try {
            BlockBreakEvent.class.getMethod("setDropItems", boolean.class).invoke(event, false);
        } catch (Exception ignored) {}
    }

    private void giveItemToPlayer(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == AIR_MATERIAL || itemStack.getAmount() <= 0) {
            return;
        }

        if (itemStack.getAmount() <= itemStack.getMaxStackSize()) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            return;
        }

        int remaining = itemStack.getAmount();
        while (remaining > 0) {
            int amount = Math.min(itemStack.getMaxStackSize(), remaining);
            ItemStack splitStack = itemStack.clone();
            splitStack.setAmount(amount);

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(splitStack);
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }

            remaining -= amount;
        }
    }

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            names.add(onlinePlayer.getName());
        }
        return names;
    }

    private List<String> filterStartsWith(String input, List<String> candidates) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ENGLISH);
        List<String> output = new ArrayList<>();

        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ENGLISH).startsWith(normalized)) {
                output.add(candidate);
            }
        }

        return output;
    }

    private static void loadPerkLevelArray(ConfigurationSection section, String key, int[] target) {
        List<Integer> values = section.getIntegerList(key);
        for (int i = 0; i < target.length && i < values.size(); i++) {
            target[i] = Math.max(0, Math.min(10, values.get(i)));
        }
    }

    private static List<Integer> toIntList(int[] arr) {
        List<Integer> list = new ArrayList<>(arr.length);
        for (int v : arr) {
            list.add(v);
        }
        return list;
    }

    private int[] getPerkLevels(PlayerState state, SkillTrack track) {
        switch (track) {
            case FARMING:  return state.farmingPerkLevels;
            case SLAYING:  return state.slayingPerkLevels;
            default:       return state.miningPerkLevels;
        }
    }

    private List<String> asList(String... values) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private long parseLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private double parseDouble(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private Material parseMaterial(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Material material = EnumHelper.getEnum(Material.class, value.toUpperCase(Locale.ENGLISH));
        if (material != null) {
            return material;
        }

        material = Material.matchMaterial(value.toUpperCase(Locale.ENGLISH));
        if (material != null) {
            return material;
        }

        return null;
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Set<String> normalizeKeys(List<String> keys) {
        Set<String> output = new HashSet<>();
        for (String key : keys) {
            if (key != null && !key.trim().isEmpty()) {
                output.add(key.toLowerCase(Locale.ENGLISH));
            }
        }
        return output;
    }

    private Reward parseReward(ConfigurationSection section) {
        Reward reward = new Reward();

        if (section == null) {
            return reward;
        }

        reward.money = section.getDouble("reward-money", section.getDouble("money", 0D));
        reward.skillXp = section.getLong("reward-skill-xp", section.getLong("skill-xp", 0L));
        reward.playerXp = section.getLong("reward-player-xp", section.getLong("player-xp", 0L));

        List<String> items = section.getStringList("reward-items");
        if (items.isEmpty()) {
            items = section.getStringList("items");
        }

        for (String rawItem : items) {
            ItemStack itemStack = parseItemStack(rawItem);
            if (itemStack != null) {
                reward.items.add(itemStack);
            }
        }

        List<String> fragmentList = section.getStringList("reward-fragments");
        for (String rawFrag : fragmentList) {
            String[] fParts = rawFrag.split(":");
            if (fParts.length < 1 || fParts[0].trim().isEmpty()) continue;
            String fKey = fParts[0].trim().toLowerCase(Locale.ENGLISH);
            int fAmount = fParts.length >= 2 ? Math.max(1, parseInt(fParts[1].trim(), 1)) : 1;
            String fDisplayName = resolveFragmentDisplayName(fKey);
            reward.items.add(createFragmentItem(fKey, "&f" + fDisplayName, fAmount));
        }

        List<String> generatorList = section.getStringList("reward-generators");
        for (String rawGen : generatorList) {
            String[] gParts = rawGen.split(":");
            if (gParts.length < 1 || gParts[0].trim().isEmpty()) continue;
            String gKey = gParts[0].trim().toLowerCase(Locale.ENGLISH);
            int gAmount = gParts.length >= 2 ? Math.max(1, parseInt(gParts[1].trim(), 1)) : 1;
            reward.items.add(createGeneratorItem(gKey, gAmount));
        }

        List<String> commands = section.getStringList("reward-commands");
        if (commands.isEmpty()) {
            commands = section.getStringList("commands");
        }

        reward.commands.addAll(commands);
        return reward;
    }

    private ItemStack parseItemStack(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] parts = value.split(":");
        Material material = parseMaterial(parts[0]);
        if (material == null) {
            return null;
        }

        int amount = 1;
        if (parts.length >= 2) {
            amount = Math.max(1, parseInt(parts[1], 1));
        }

        return new ItemStack(material, amount);
    }

    private YamlConfiguration loadModuleYaml(File folder, String name) {
        return YamlConfiguration.loadConfiguration(new File(folder, name));
    }

    private void ensureDefaultConfigFiles() {
        String[] extras = {"items.yml", "milestones.yml", "nodes.yml", "spawners.yml", "oneblock.yml", "journey.yml"};
        for (String name : extras) {
            File f = new File(module.getModuleFolder(), name);
            if (!f.exists()) {
                try {
                    com.bgsoftware.superiorskyblock.core.io.Resources.saveResource("modules/evolvedskills/" + name);
                } catch (Exception e) {
                    plugin.getLogger().warning("[EvolvedSkills] Could not save default " + name + ": " + e.getMessage());
                }
            }
        }
        
        File treasureDir = new File(module.getModuleFolder(), "treasure");
        if (!treasureDir.exists()) {
            
            treasureDir.mkdirs();
        }
        for (String name : new String[]{"treasure/pools.yml", "treasure/links.yml"}) {
            File dest = new File(module.getModuleFolder(), name);
            if (!dest.exists()) {
                String resourcePath = "modules/evolvedskills/" + name;
                try (java.io.InputStream in = plugin.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (in != null) {
                        java.nio.file.Files.copy(in, dest.toPath());
                    } else {
                        plugin.getLogger().warning("[EvolvedSkills] Default resource not found: " + resourcePath);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("[EvolvedSkills] Could not save default " + name + ": " + e.getMessage());
                }
            }
        }
    }

    private EvolvedConfig loadConfiguration(File moduleFolder) {
        YamlConfiguration yaml = loadModuleYaml(moduleFolder, "config.yml");
        YamlConfiguration itemsYaml = loadModuleYaml(moduleFolder, "items.yml");
        YamlConfiguration milestonesYaml = loadModuleYaml(moduleFolder, "milestones.yml");
        YamlConfiguration nodesYaml = loadModuleYaml(moduleFolder, "nodes.yml");
        YamlConfiguration spawnersYaml = loadModuleYaml(moduleFolder, "spawners.yml");
        YamlConfiguration oneblockYaml = loadModuleYaml(moduleFolder, "oneblock.yml");
        YamlConfiguration journeyYaml = loadModuleYaml(moduleFolder, "journey.yml");
        YamlConfiguration treasureYaml = loadModuleYaml(new File(moduleFolder, "treasure"), "pools.yml");
        YamlConfiguration treasureLinksYaml = loadModuleYaml(new File(moduleFolder, "treasure"), "links.yml");

        EvolvedConfig result = new EvolvedConfig();

        result.oneBlockFallbackMaterial = parseMaterial(oneblockYaml.getString("oneblock.fallback-material", "STONE"));
        if (result.oneBlockFallbackMaterial == null) {
            result.oneBlockFallbackMaterial = Material.STONE;
        }

        result.xpPerNaturalBlockBreak = Math.max(0L, yaml.getLong("xp.block-break", 1L));
        result.xpPerOneBlockBreak = Math.max(0L, yaml.getLong("xp.oneblock-break", 2L));
        result.xpPerMobKill = Math.max(0L, yaml.getLong("xp.mob-kill", 3L));
        result.xpPerCraft = Math.max(0L, yaml.getLong("xp.craft", 2L));

        result.perkMaxLevel = Math.max(1, yaml.getInt("perks.max-level", 20));
        List<?> unlockLevels = yaml.getList("perks.unlock-levels", java.util.Arrays.asList(1, 5, 10, 15, 20));
        for (int i = 0; i < 5 && i < unlockLevels.size(); i++) {
            result.perkUnlockLevels[i] = ((Number) unlockLevels.get(i)).intValue();
        }
        result.perkCropGrowthBonusPerLevel = Math.max(0D, yaml.getDouble("perks.effects.crop-growth-bonus-per-level", 0.01D));
        result.perkNodeYieldBonusPerLevel = Math.max(0D, yaml.getDouble("perks.effects.node-yield-bonus-per-level", 0.03D));
        result.perkSpawnerSpeedBonusPerLevel = Math.max(0D, yaml.getDouble("perks.effects.spawner-speed-bonus-per-level", 0.02D));
        result.perkGeneratorYieldBonusPerLevel = Math.max(0D, yaml.getDouble("perks.effects.generator-yield-bonus-per-level", 0.02D));
        result.perkOneBlockBonusDropPerLevel = Math.max(0D, yaml.getDouble("perks.effects.oneblock-drop-bonus-per-level", 0.01D));

        result.skillLevelStepXp = Math.max(1L, yaml.getLong("skill.default-level-step-xp", 100L));
        result.playerLevelStepXp = Math.max(1L, yaml.getLong("player-level.default-level-step-xp", 120L));

        result.skillLevelThresholds = parseLevelThresholds(yaml.getConfigurationSection("skill.levels"));
        result.playerLevelThresholds = parseLevelThresholds(yaml.getConfigurationSection("player-level.levels"));

        result.dailyOneBlockTargetBreaks = Math.max(1, yaml.getInt("daily.oneblock-break-target", 64));
        result.dailyOneBlockReward = parseReward(yaml.getConfigurationSection("daily.oneblock-reward"));
        result.dailyTasks = parseDailyTasks(yaml.getConfigurationSection("daily.tasks"));
        result.dailyChallengePool = parseDailyChallenges(yaml.getConfigurationSection("daily.challenges.pool"));
        result.dailyChallengeCount = Math.max(1, yaml.getInt("daily.challenges.count", 10));

        result.oneBlockStages = parseOneBlockStages(oneblockYaml.getMapList("oneblock.stages"), result.oneBlockFallbackMaterial);

        result.milestones = parseMilestones(milestonesYaml.getConfigurationSection("mining"), null);
        result.miningMilestones = parseMilestones(milestonesYaml.getConfigurationSection("mining"), SkillTrack.MINING);
        result.farmingMilestones = parseMilestones(milestonesYaml.getConfigurationSection("farming"), SkillTrack.FARMING);
        result.slayingMilestones = parseMilestones(milestonesYaml.getConfigurationSection("slaying"), SkillTrack.SLAYING);

        result.fragmentItemMaterial = parseMaterial(itemsYaml.getString("fragment.material", "PRISMARINE_SHARD"));
        if (result.fragmentItemMaterial == null) result.fragmentItemMaterial = Material.PRISMARINE_SHARD;
        result.fragmentItemName = itemsYaml.getString("fragment.name", "&b%type% Fragment");
        result.fragmentItemLore = itemsYaml.getStringList("fragment.lore");

        result.nodeItemMaterial = parseMaterial(itemsYaml.getString("nodes.material", "NETHER_STAR"));
        result.nodeItemName = itemsYaml.getString("nodes.name", "&e%type% Node");
        result.nodeItemLore = itemsYaml.getStringList("nodes.lore");

        result.nodeTypes = parseNodeTypes(nodesYaml.getConfigurationSection("types"));

        ConfigurationSection nodeLimitsSection = nodesYaml.getConfigurationSection("limits");
        result.defaultNodeLimitRule = parseLimitRule(
            nodeLimitsSection == null ? null : nodeLimitsSection.getConfigurationSection("defaults"),
            new LimitRule(5, 2, 10, Collections.<Double>emptyList(), Collections.<Integer>emptyList(), Collections.<Integer>emptyList())
        );
        result.nodeLimitRules = parseLimitRules(
            nodeLimitsSection == null ? null : nodeLimitsSection.getConfigurationSection("types"),
            result.defaultNodeLimitRule
        );

        result.spawnerItemMaterial = parseMaterial(itemsYaml.getString("spawners.material", "ZOMBIE_SPAWN_EGG"));
        result.spawnerItemName = itemsYaml.getString("spawners.name", "&e%type% Spawner");
        result.spawnerItemLore = itemsYaml.getStringList("spawners.lore");

        result.spawnerTypes = parseSpawnerTypes(spawnersYaml.getConfigurationSection("types"));

        ConfigurationSection spawnerLimitsSection = spawnersYaml.getConfigurationSection("limits");
        result.defaultSpawnerLimitRule = parseLimitRule(
            spawnerLimitsSection == null ? null : spawnerLimitsSection.getConfigurationSection("defaults"),
            new LimitRule(4, 1, 10, Collections.<Double>emptyList(), Collections.<Integer>emptyList(), Collections.<Integer>emptyList())
        );
        result.spawnerLimitRules = parseLimitRules(
            spawnerLimitsSection == null ? null : spawnerLimitsSection.getConfigurationSection("types"),
            result.defaultSpawnerLimitRule
        );

        ConfigurationSection generatorsSection = oneblockYaml.getConfigurationSection("generators");
        if (generatorsSection != null) {
            for (String trackKey : generatorsSection.getKeys(false)) {
                GeneratorSettings settings = parseGeneratorSettings(generatorsSection.getConfigurationSection(trackKey));
                result.generators.put(trackKey.toLowerCase(Locale.ENGLISH), settings);
            }
        }

        
        
        this.config = result;
        result.journeyTasks = parseJourneyTasks(journeyYaml.getConfigurationSection("tasks"));
        List<String> journeyHeaderLoreYml = journeyYaml.getStringList("header.lore");
        result.journeyHeaderLore = journeyHeaderLoreYml.isEmpty() ? null : journeyHeaderLoreYml;
        String journeyHeaderNameYml = journeyYaml.getString("header.name", "");
        result.journeyHeaderName = journeyHeaderNameYml.isEmpty() ? null : journeyHeaderNameYml;

        result.treasureItemMaterial = parseMaterial(itemsYaml.getString("treasure.material", "TRIPWIRE_HOOK"));
        result.treasureItemName = itemsYaml.getString("treasure.name", "&e%pool% Treasure Token");
        result.treasureItemLore = itemsYaml.getStringList("treasure.lore");

        result.treasurePools = parseTreasurePools(treasureYaml.getConfigurationSection("pools"));

        result.perkTreasureChanceBonusPerLevel = yaml.getDouble("perks.treasure-chance-bonus-per-level", 0.1);
        result.fragmentChancePerLevel = Math.max(0D, yaml.getDouble("perks.fragment-chance-per-level", 0.05D));
        result.fragmentBaseChance = Math.max(0D, yaml.getDouble("perks.fragment-base-chance", 0.03D));

        List<Double> perkCosts = new ArrayList<>();
        List<?> rawPerkCosts = yaml.getList("perks.upgrade-costs");
        if (rawPerkCosts != null) {
            for (Object val : rawPerkCosts) {
                if (val instanceof Number) perkCosts.add(((Number) val).doubleValue());
            }
        }
        result.perkUpgradeCosts = perkCosts;

        Map<String, String> msgs = new HashMap<>();
        ConfigurationSection messagesSection = yaml.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                String val = messagesSection.getString(key);
                if (val != null) msgs.put(key, val);
            }
        }
        result.messages = msgs;
        ConfigurationSection linksSection = treasureLinksYaml.getConfigurationSection("links");
        if (linksSection != null) {
            for (String resourceKey : linksSection.getKeys(false)) {
                String lowerKey = resourceKey.toLowerCase(Locale.ENGLISH);
                List<TreasureDropLink> linkList = new ArrayList<>();
                List<?> rawList = linksSection.getList(resourceKey);
                if (rawList != null) {
                    for (Object obj : rawList) {
                        if (!(obj instanceof java.util.Map)) continue;
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> map = (java.util.Map<String, Object>) obj;
                        String pool = map.containsKey("pool") ? String.valueOf(map.get("pool")) : "default";
                        double chance = map.containsKey("chance") ? ((Number) map.get("chance")).doubleValue() : 0.05;
                        linkList.add(new TreasureDropLink(pool, chance));
                    }
                }
                if (!linkList.isEmpty()) {
                    result.treasureLinks.put(lowerKey, linkList);
                }
            }
        }

        result.prestigeCost = oneblockYaml.getDouble("prestige.cost", 100000.0);

        ConfigurationSection growthSection = oneblockYaml.getConfigurationSection("daily-growth-limits");
        if (growthSection != null) {
            result.growthLimitUpgradeBaseCost = growthSection.getDouble("upgrade-base-cost", 1000.0);
            result.growthLimitUpgradeCostMultiplier = growthSection.getDouble("upgrade-cost-multiplier", 1.5);
            result.growthLimitUpgradeAmount = growthSection.getInt("upgrade-amount", 50);
            result.growthLimitMaxTier = Math.max(1, growthSection.getInt("max-tier", 20));
            result.growthLimitFragmentCostBase = Math.max(0, growthSection.getInt("fragment-cost-base", 5));
            ConfigurationSection defaults = growthSection.getConfigurationSection("defaults");
            if (defaults != null) {
                for (String mat : defaults.getKeys(false)) {
                    result.dailyGrowthLimits.put(mat.toLowerCase(Locale.ENGLISH), defaults.getInt(mat, 100));
                }
            }
        }

        result.guiSection = yaml.getConfigurationSection("gui");

        return result;
    }

    private TreeMap<Integer, Long> parseLevelThresholds(ConfigurationSection section) {
        TreeMap<Integer, Long> thresholds = new TreeMap<>();

        if (section == null) {
            thresholds.put(1, 0L);
            thresholds.put(2, 100L);
            return thresholds;
        }

        for (String key : section.getKeys(false)) {
            int level;
            try {
                level = Integer.parseInt(key);
            } catch (NumberFormatException ignored) {
                continue;
            }

            thresholds.put(Math.max(1, level), Math.max(0L, section.getLong(key, 0L)));
        }

        if (!thresholds.containsKey(1)) {
            thresholds.put(1, 0L);
        }

        return thresholds;
    }

    private List<OneBlockStage> parseOneBlockStages(List<Map<?, ?>> stageMaps, Material fallbackMaterial) {
        List<OneBlockStage> stages = new ArrayList<>();

        int index = 0;
        for (Map<?, ?> map : stageMaps) {
            if (map == null || map.isEmpty()) {
                continue;
            }

            OneBlockStage stage = new OneBlockStage();
            Object stageNameValue = map.containsKey("name") ? map.get("name") : "stage-" + index;
            stage.key = String.valueOf(stageNameValue);
            stage.requiredBreaks = Math.max(0L, parseLong(map.get("required-breaks"), index == 0 ? 0L : index * 250L));

            List<String> blocks = toStringList(map.get("blocks"));
            stage.weightedMaterials = parseWeightedMaterialList(blocks);
            if (stage.weightedMaterials.isEmpty()) {
                stage.weightedMaterials.add(new WeightedMaterial(fallbackMaterial, 1));
            }

            stages.add(stage);
            index++;
        }

        if (stages.isEmpty()) {
            OneBlockStage defaultStage = new OneBlockStage();
            defaultStage.key = "default";
            defaultStage.requiredBreaks = 0L;
            defaultStage.weightedMaterials = new ArrayList<>();
            defaultStage.weightedMaterials.add(new WeightedMaterial(fallbackMaterial, 1));
            stages.add(defaultStage);
        }

        stages.sort(Comparator.comparingLong(stage -> stage.requiredBreaks));
        return stages;
    }

    private List<MilestoneDefinition> parseMilestones(ConfigurationSection section, SkillTrack track) {
        List<MilestoneDefinition> milestones = new ArrayList<>();

        if (section == null) {
            return milestones;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection milestoneSection = section.getConfigurationSection(key);
            if (milestoneSection == null) {
                continue;
            }

            MilestoneDefinition milestone = new MilestoneDefinition();
            milestone.key = key.toLowerCase(Locale.ENGLISH);
            milestone.displayName = milestoneSection.getString("display-name", key);
            milestone.description = milestoneSection.getString("description", "");
            milestone.track = track;
            milestone.displayOrder = milestoneSection.getInt("sort-order", 0);

            List<String> blockNames = milestoneSection.getStringList("target-blocks");
            for (String blockName : blockNames) {
                Material mat = parseMaterial(blockName.trim().toUpperCase(Locale.ENGLISH));
                if (mat != null) {
                    milestone.targetBlocks.add(mat);
                }
            }

            List<String> entityNames = milestoneSection.getStringList("target-entities");
            for (String entityName : entityNames) {
                try {
                    EntityType et = EntityType.valueOf(entityName.trim().toUpperCase(Locale.ENGLISH));
                    milestone.targetEntities.add(et);
                } catch (IllegalArgumentException ignored) {}
            }

            ConfigurationSection tiersSection = milestoneSection.getConfigurationSection("tiers");
            if (tiersSection != null) {
                for (String tierNumStr : tiersSection.getKeys(false)) {
                    try {
                        int tierNum = Integer.parseInt(tierNumStr);
                        ConfigurationSection tierSec = tiersSection.getConfigurationSection(tierNumStr);
                        if (tierSec == null) continue;
                        MilestoneTier tier = new MilestoneTier();
                        tier.requiredCount = Math.max(1L, tierSec.getLong("required-count", 1L));
                        tier.skillXp = Math.max(0L, tierSec.getLong("skill-xp", 0L));
                        tier.playerXp = Math.max(0L, tierSec.getLong("player-xp", 0L));
                        milestone.tiers.put(tierNum, tier);
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (milestone.tiers.isEmpty()) {
                MilestoneTier tier1 = new MilestoneTier();
                tier1.requiredCount = Math.max(1L, milestoneSection.getLong("required-count",
                        milestoneSection.getLong("required-breaks", 1L)));
                tier1.skillXp = Math.max(0L, milestoneSection.getLong("reward-skill-xp",
                        milestoneSection.getLong("skill-xp", 0L)));
                tier1.playerXp = Math.max(0L, milestoneSection.getLong("reward-player-xp",
                        milestoneSection.getLong("player-xp", 0L)));
                milestone.tiers.put(1, tier1);
            }

            milestones.add(milestone);
        }

        Collections.sort(milestones, new Comparator<MilestoneDefinition>() {
            @Override
            public int compare(MilestoneDefinition a, MilestoneDefinition b) {
                boolean aHasOrder = a.displayOrder > 0;
                boolean bHasOrder = b.displayOrder > 0;
                if (aHasOrder && bHasOrder) {
                    return Integer.compare(a.displayOrder, b.displayOrder);
                }
                if (aHasOrder) return -1;
                if (bHasOrder) return 1;
                MilestoneTier ta = a.tiers.isEmpty() ? null : a.tiers.firstEntry().getValue();
                MilestoneTier tb = b.tiers.isEmpty() ? null : b.tiers.firstEntry().getValue();
                long ra = ta == null ? 0L : ta.requiredCount;
                long rb = tb == null ? 0L : tb.requiredCount;
                return Long.compare(ra, rb);
            }
        });
        return milestones;
    }

    private Map<String, NodeTypeDefinition> parseNodeTypes(ConfigurationSection section) {
        Map<String, NodeTypeDefinition> types = new LinkedHashMap<>();

        if (section == null) {
            NodeTypeDefinition fallback = new NodeTypeDefinition();
            fallback.key = "wheat";
            fallback.displayName = "Wheat Node";
            fallback.displayBlock = Material.HAY_BLOCK;
            fallback.outputMaterial = Material.WHEAT;
            fallback.outputPerCycle = 2;
            fallback.growthSeconds = 30;
            fallback.maxLevel = 20;
            fallback.upgradeBaseCost = 500D;
            fallback.upgradeCostMultiplier = 1.3D;
            fallback.maxStorageBase = 64;
            types.put(fallback.key, fallback);
            return types;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection typeSection = section.getConfigurationSection(key);
            if (typeSection == null) {
                continue;
            }

            NodeTypeDefinition definition = new NodeTypeDefinition();
            definition.key = key.toLowerCase(Locale.ENGLISH);
            definition.displayName = typeSection.getString("display-name", key);
            definition.displayBlock = parseMaterial(typeSection.getString("block", "HAY_BLOCK"));

            ItemStack output = parseItemStack(typeSection.getString("output", "WHEAT:2"));
            if (output == null) {
                output = new ItemStack(Material.WHEAT, 2);
            }

            definition.outputMaterial = output.getType();
            definition.outputPerCycle = Math.max(1, output.getAmount());
            definition.growthSeconds = Math.max(1, typeSection.getInt("growth-seconds", 30));
            definition.cooldownTicks = Math.max(20L, typeSection.getLong("cooldown-ticks", 1200L));
            definition.fragmentChance = Math.max(0D, typeSection.getDouble("fragment-chance", 0.0D));
            definition.maxLevel = Math.max(1, typeSection.getInt("max-level", 20));
            definition.upgradeBaseCost = Math.max(0D, typeSection.getDouble("upgrade-base-cost", 500D));
            definition.upgradeCostMultiplier = Math.max(1D, typeSection.getDouble("upgrade-cost-multiplier", 1.3D));
            definition.maxStorageBase = Math.max(8, typeSection.getInt("max-storage-base", 64));
            definition.skillTrack = parseSkillTrack(typeSection.getString("track"));
            definition.customModelData = typeSection.getInt("custom-model-data", 0);
            definition.nexoItemId = typeSection.getString("nexo-item-id", null);

            types.put(definition.key, definition);
        }

        return types;
    }

    private SkillTrack parseSkillTrack(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return SkillTrack.valueOf(value.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Map<String, SpawnerTypeDefinition> parseSpawnerTypes(ConfigurationSection section) {
        Map<String, SpawnerTypeDefinition> types = new LinkedHashMap<>();

        if (section == null) {
            SpawnerTypeDefinition fallback = new SpawnerTypeDefinition();
            fallback.key = "zombie";
            fallback.displayName = "Zombie Spawner";
            fallback.entityType = EntityType.ZOMBIE;
            fallback.outputMaterial = Material.ROTTEN_FLESH;
            fallback.outputPerCycle = 1;
            fallback.spawnSeconds = 25;
            fallback.maxLevel = 20;
            fallback.upgradeBaseCost = 1000D;
            fallback.upgradeCostMultiplier = 1.35D;
            fallback.maxStorageBase = 64;
            types.put(fallback.key, fallback);
            return types;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection typeSection = section.getConfigurationSection(key);
            if (typeSection == null) {
                continue;
            }

            SpawnerTypeDefinition definition = new SpawnerTypeDefinition();
            definition.key = key.toLowerCase(Locale.ENGLISH);
            definition.displayName = typeSection.getString("display-name", key);

            EntityType entityType = EnumHelper.getEnum(EntityType.class, key.toUpperCase(Locale.ENGLISH));
            if (entityType == null) {
                entityType = EnumHelper.getEnum(EntityType.class,
                        typeSection.getString("entity-type", "ZOMBIE").toUpperCase(Locale.ENGLISH));
            }
            if (entityType == null) {
                entityType = EntityType.ZOMBIE;
            }
            definition.entityType = entityType;

            ItemStack output = parseItemStack(typeSection.getString("drop", "ROTTEN_FLESH:1"));
            if (output == null) {
                output = new ItemStack(Material.ROTTEN_FLESH, 1);
            }

            definition.outputMaterial = output.getType();
            definition.outputPerCycle = Math.max(1, output.getAmount());
            definition.spawnSeconds = Math.max(1, typeSection.getInt("spawn-seconds", 25));
            definition.fragmentChance = Math.max(0D, typeSection.getDouble("fragment-chance", 0.0D));
            definition.maxLevel = Math.max(1, typeSection.getInt("max-level", 20));
            definition.upgradeBaseCost = Math.max(0D, typeSection.getDouble("upgrade-base-cost", 1000D));
            definition.upgradeCostMultiplier = Math.max(1D, typeSection.getDouble("upgrade-cost-multiplier", 1.35D));
            definition.maxStorageBase = Math.max(8, typeSection.getInt("max-storage-base", 64));

            types.put(definition.key, definition);
        }

        return types;
    }

    private GeneratorSettings parseGeneratorSettings(ConfigurationSection section) {
        GeneratorSettings settings = new GeneratorSettings();

        if (section == null) {
            settings.enabled = true;
            settings.baseYield = 4;
            settings.yieldPerLevel = 2;
            settings.maxLevel = 20;
            settings.expansionBaseCost = 5000D;
            settings.expansionCostMultiplier = 1.25D;
            settings.weightedMaterials.add(new WeightedMaterial(Material.COBBLESTONE, 70));
            settings.weightedMaterials.add(new WeightedMaterial(Material.COAL, 20));
            settings.weightedMaterials.add(new WeightedMaterial(Material.IRON_INGOT, 8));
            settings.weightedMaterials.add(new WeightedMaterial(Material.DIAMOND, 2));
            return settings;
        }

        settings.enabled = section.getBoolean("enabled", true);
        settings.baseYield = Math.max(1, section.getInt("base-yield", 4));
        settings.yieldPerLevel = Math.max(0, section.getInt("yield-per-level", 2));
        settings.maxLevel = Math.max(1, section.getInt("max-level", 20));
        settings.expansionBaseCost = Math.max(0D, section.getDouble("expansion-base-cost", 5000D));
        settings.expansionCostMultiplier = Math.max(1D, section.getDouble("expansion-cost-multiplier", 1.25D));
        settings.maxStorage = Math.max(1, section.getInt("max-storage", 3456));
        settings.baseMaxPlacements = Math.max(1, section.getInt("base-max-placements", 1));
        settings.maxPlacementsPerPerkLevel = Math.max(0, section.getInt("max-placements-per-perk-level", 1));
        settings.productionRateSeconds = Math.max(1, section.getInt("production-rate-seconds", 60));

        ConfigurationSection materialsSection = section.getConfigurationSection("materials");
        if (materialsSection != null) {
            for (String materialName : materialsSection.getKeys(false)) {
                Material material = parseMaterial(materialName);
                if (material == null) {
                    continue;
                }

                int weight;
                int accessTier;
                ConfigurationSection entrySection = materialsSection.getConfigurationSection(materialName);
                if (entrySection != null) {
                    weight = Math.max(1, entrySection.getInt("weight", 1));
                    accessTier = Math.max(0, entrySection.getInt("access-tier", 0));
                } else {
                    weight = Math.max(1, materialsSection.getInt(materialName, 1));
                    accessTier = 0;
                }
                settings.weightedMaterials.add(new WeightedMaterial(material, weight, accessTier));
            }
        }

        if (settings.weightedMaterials.isEmpty()) {
            settings.weightedMaterials.add(new WeightedMaterial(Material.COBBLESTONE, 100));
        }

        return settings;
    }

    private Map<String, DailyTaskDefinition> parseDailyTasks(ConfigurationSection section) {
        Map<String, DailyTaskDefinition> tasks = new LinkedHashMap<>();

        if (section == null) {
            tasks.put(DAILY_TASK_KILL_MOBS,
                    createDefaultDailyTask(DAILY_TASK_KILL_MOBS, "Kill Mobs", menuMaterial("IRON_SWORD", "STONE_SWORD"),
                            asList("15", "35", "60", "90", "125", "170", "220", "285", "360", "450"),
                            550D, 20L, 20L));

            tasks.put(DAILY_TASK_MINE_RESOURCES,
                    createDefaultDailyTask(DAILY_TASK_MINE_RESOURCES, "Mine Resources", menuMaterial("IRON_PICKAXE", "STONE_PICKAXE"),
                            asList("30", "70", "120", "180", "250", "330", "420", "520", "640", "780"),
                            600D, 24L, 24L));

            tasks.put(DAILY_TASK_HARVEST_CROPS,
                    createDefaultDailyTask(DAILY_TASK_HARVEST_CROPS, "Harvest Crops", menuMaterial("WHEAT"),
                            asList("12", "28", "48", "72", "100", "132", "168", "210", "258", "312"),
                            520D, 18L, 18L));

            return tasks;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection taskSection = section.getConfigurationSection(key);
            if (taskSection == null) {
                continue;
            }

            DailyTaskDefinition definition = new DailyTaskDefinition();
            definition.key = key.toLowerCase(Locale.ENGLISH);
            definition.displayName = taskSection.getString("display-name", key);
            definition.iconMaterial = parseMaterial(taskSection.getString("icon", "YELLOW_DYE"));

            List<String> stageTargetValues = taskSection.getStringList("stage-targets");
            if (stageTargetValues.isEmpty()) {
                stageTargetValues = taskSection.getStringList("stages");
            }

            for (String stageTargetValue : stageTargetValues) {
                int target = Math.max(1, parseInt(stageTargetValue, 0));
                if (target > 0) {
                    definition.stageTargets.add((long) target);
                }
            }

            if (definition.stageTargets.isEmpty()) {
                definition.stageTargets.add(10L);
                definition.stageTargets.add(25L);
                definition.stageTargets.add(45L);
                definition.stageTargets.add(70L);
                definition.stageTargets.add(100L);
                definition.stageTargets.add(135L);
                definition.stageTargets.add(175L);
                definition.stageTargets.add(220L);
                definition.stageTargets.add(270L);
                definition.stageTargets.add(325L);
            }

            Collections.sort(definition.stageTargets);

            Reward stageReward = parseReward(taskSection.getConfigurationSection("stage-reward"));
            if (stageReward.isEmpty()) {
                stageReward.money = Math.max(0D, taskSection.getDouble("stage-reward-money", 500D));
                stageReward.skillXp = Math.max(0L, taskSection.getLong("stage-reward-skill-xp", 16L));
                stageReward.playerXp = Math.max(0L, taskSection.getLong("stage-reward-player-xp", 16L));

                List<String> rewardItems = taskSection.getStringList("stage-reward-items");
                for (String rewardItemRaw : rewardItems) {
                    ItemStack rewardItem = parseItemStack(rewardItemRaw);
                    if (rewardItem != null) {
                        stageReward.items.add(rewardItem);
                    }
                }
            }
            definition.baseStageReward = stageReward;

            tasks.put(definition.key, definition);
        }

        if (!tasks.containsKey(DAILY_TASK_KILL_MOBS)) {
            tasks.put(DAILY_TASK_KILL_MOBS,
                    createDefaultDailyTask(DAILY_TASK_KILL_MOBS, "Kill Mobs", menuMaterial("IRON_SWORD", "STONE_SWORD"),
                            asList("15", "35", "60", "90", "125", "170", "220", "285", "360", "450"),
                            550D, 20L, 20L));
        }

        if (!tasks.containsKey(DAILY_TASK_MINE_RESOURCES)) {
            tasks.put(DAILY_TASK_MINE_RESOURCES,
                    createDefaultDailyTask(DAILY_TASK_MINE_RESOURCES, "Mine Resources", menuMaterial("IRON_PICKAXE", "STONE_PICKAXE"),
                            asList("30", "70", "120", "180", "250", "330", "420", "520", "640", "780"),
                            600D, 24L, 24L));
        }

        if (!tasks.containsKey(DAILY_TASK_HARVEST_CROPS)) {
            tasks.put(DAILY_TASK_HARVEST_CROPS,
                    createDefaultDailyTask(DAILY_TASK_HARVEST_CROPS, "Harvest Crops", menuMaterial("WHEAT"),
                            asList("12", "28", "48", "72", "100", "132", "168", "210", "258", "312"),
                            520D, 18L, 18L));
        }

        return tasks;
    }

    private List<DailyChallengeDefinition> parseDailyChallenges(ConfigurationSection section) {
        List<DailyChallengeDefinition> pool = new ArrayList<>();
        if (section == null) return pool;
        for (String key : section.getKeys(false)) {
            ConfigurationSection cs = section.getConfigurationSection(key);
            if (cs == null) continue;
            DailyChallengeDefinition def = new DailyChallengeDefinition();
            def.key = key.toLowerCase(Locale.ENGLISH);
            def.displayName = cs.getString("display-name", key);
            def.description = cs.getString("description", "");
            def.iconMaterial = parseMaterial(cs.getString("icon", "PAPER"));
            def.matchKey = cs.getString("match", "").toLowerCase(Locale.ENGLISH);
            def.target = Math.max(1L, cs.getLong("amount", 10L));
            def.reward = parseReward(cs.getConfigurationSection("reward"));
            if (def.reward.isEmpty()) {
                def.reward.money = cs.getDouble("reward-money", 0D);
                def.reward.playerXp = cs.getLong("reward-player-xp", 0L);
            }
            if (!def.matchKey.isEmpty()) pool.add(def);
        }
        return pool;
    }

    private DailyTaskDefinition createDefaultDailyTask(String key, String displayName, Material icon,
                                                       List<String> stageTargets,
                                                       double rewardMoney, long rewardSkillXp, long rewardPlayerXp) {
        DailyTaskDefinition definition = new DailyTaskDefinition();
        definition.key = key;
        definition.displayName = displayName;
        definition.iconMaterial = icon;

        for (String stageTarget : stageTargets) {
            int parsedTarget = Math.max(1, parseInt(stageTarget, 1));
            definition.stageTargets.add((long) parsedTarget);
        }

        definition.baseStageReward.money = rewardMoney;
        definition.baseStageReward.skillXp = rewardSkillXp;
        definition.baseStageReward.playerXp = rewardPlayerXp;

        return definition;
    }

    private LimitRule parseLimitRule(ConfigurationSection section, LimitRule fallback) {
        if (section == null) {
            return fallback;
        }

        List<Integer> tierLimits = section.getIntegerList("tier-limits");
        List<Integer> normalizedTierLimits = new ArrayList<>();
        for (Integer tierLimit : tierLimits) {
            if (tierLimit == null) {
                continue;
            }
            normalizedTierLimits.add(Math.max(1, tierLimit));
        }

        int maxUpgradeTier = Math.max(0, section.getInt("max-upgrade-tier", fallback.maxUpgradeTier));
        if (!normalizedTierLimits.isEmpty()) {
            int derivedMaxTier = normalizedTierLimits.size() - 1;
            if (maxUpgradeTier <= 0 || maxUpgradeTier > derivedMaxTier) {
                maxUpgradeTier = derivedMaxTier;
            }
        }

        int initialLimit = Math.max(1, section.getInt("initial-limit", fallback.initialLimit));
        int addPerTier = Math.max(1, section.getInt("add-per-tier", fallback.addLimitPerTier));
        if (normalizedTierLimits.size() >= 2) {
            initialLimit = normalizedTierLimits.get(0);
            addPerTier = Math.max(1, normalizedTierLimits.get(1) - normalizedTierLimits.get(0));
        }

        List<Double> moneyCosts = new ArrayList<>();
        List<?> rawMoneyCosts = section.getList("money-costs");
        if (rawMoneyCosts != null) {
            for (Object val : rawMoneyCosts) {
                if (val instanceof Number) moneyCosts.add(((Number) val).doubleValue());
            }
        }
        if (moneyCosts.isEmpty()) {
            moneyCosts.addAll(fallback.moneyCostsPerTier);
        }

        List<Integer> fragmentCosts = new ArrayList<>();
        for (Integer fc : section.getIntegerList("fragment-costs")) {
            fragmentCosts.add(fc == null ? 0 : Math.max(0, fc));
        }
        if (fragmentCosts.isEmpty()) {
            fragmentCosts.addAll(fallback.fragmentCostsPerTier);
        }

        return new LimitRule(
                initialLimit,
                addPerTier,
                maxUpgradeTier,
                moneyCosts,
                fragmentCosts,
                normalizedTierLimits
        );
    }

    private Map<String, LimitRule> parseLimitRules(ConfigurationSection section, LimitRule fallback) {
        Map<String, LimitRule> rules = new HashMap<>();
        if (section == null) {
            return rules;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection ruleSection = section.getConfigurationSection(key);
            if (ruleSection == null) {
                continue;
            }

            rules.put(key.toLowerCase(Locale.ENGLISH), parseLimitRule(ruleSection, fallback));
        }

        return rules;
    }

    private Map<String, JourneyTaskDefinition> parseJourneyTasks(ConfigurationSection section) {
        Map<String, JourneyTaskDefinition> tasks = new LinkedHashMap<>();

        if (section == null) {
            return tasks;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection taskSection = section.getConfigurationSection(key);
            if (taskSection == null) {
                continue;
            }

            JourneyTaskDefinition task = new JourneyTaskDefinition();
            task.key = key.toLowerCase(Locale.ENGLISH);
            task.displayName = taskSection.getString("display-name", key);
            task.description = taskSection.getString("description", "");
            task.icon = parseMaterial(taskSection.getString("icon", null));
            task.type = JourneyType.fromName(taskSection.getString("type", "BREAK_BLOCK"));
            task.target = Math.max(1L, taskSection.getLong("target", 1L));
            task.daily = taskSection.getBoolean("daily", false);
            task.reward = parseReward(taskSection);

            List<String> matches = taskSection.getStringList("match");
            if (matches.isEmpty()) {
                String single = taskSection.getString("match");
                if (single != null && !single.isEmpty()) {
                    matches.add(single);
                }
            }

            for (String value : matches) {
                if (value == null || value.trim().isEmpty()) {
                    continue;
                }
                task.matchValues.add(value.toLowerCase(Locale.ENGLISH));
            }

            tasks.put(task.key, task);
        }

        return tasks;
    }

    private Map<String, TreasurePoolDefinition> parseTreasurePools(ConfigurationSection section) {
        Map<String, TreasurePoolDefinition> pools = new LinkedHashMap<>();

        if (section == null) {
            return pools;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection poolSection = section.getConfigurationSection(key);
            if (poolSection == null) {
                continue;
            }

            TreasurePoolDefinition pool = new TreasurePoolDefinition();
            pool.key = key.toLowerCase(Locale.ENGLISH);
            pool.displayName = poolSection.getString("display-name", key);

            List<Map<?, ?>> entryMaps = poolSection.getMapList("entries");
            for (Map<?, ?> entryMap : entryMaps) {
                if (entryMap == null || entryMap.isEmpty()) {
                    continue;
                }

                int weight = Math.max(1, (int) parseLong(entryMap.get("weight"), 1L));
                Reward reward = new Reward();

                reward.money = Math.max(0D, parseDouble(entryMap.get("money"), 0D));
                reward.skillXp = Math.max(0L, parseLong(entryMap.get("skill-xp"), 0L));
                reward.playerXp = Math.max(0L, parseLong(entryMap.get("player-xp"), 0L));

                String itemRaw = entryMap.get("item") == null ? null : String.valueOf(entryMap.get("item"));
                ItemStack itemStack = parseItemStack(itemRaw);
                if (itemStack != null) {
                    reward.items.add(itemStack);
                }

                String generatorRaw = entryMap.get("generator") == null ? null : String.valueOf(entryMap.get("generator")).trim().toLowerCase(Locale.ENGLISH);
                if (generatorRaw != null && !generatorRaw.isEmpty()) {
                    reward.items.add(createGeneratorItem(generatorRaw, 1));
                }

                List<String> commands = toStringList(entryMap.get("commands"));
                reward.commands.addAll(commands);

                if (reward.isEmpty()) {
                    continue;
                }

                pool.entries.add(new WeightedReward(weight, reward));
            }

            pools.put(pool.key, pool);
        }

        return pools;
    }

    private List<WeightedMaterial> parseWeightedMaterialList(List<String> rawValues) {
        List<WeightedMaterial> output = new ArrayList<>();

        for (String value : rawValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            
            String[] parts = value.split(":");
            Material material = parseMaterial(parts[0]);
            if (material == null) {
                continue;
            }

            int weight = 1;
            if (parts.length >= 2) {
                weight = Math.max(1, parseInt(parts[1], 1));
            }

            int minPerkLevel = 0;
            if (parts.length >= 3) {
                minPerkLevel = Math.max(0, parseInt(parts[2], 0));
            }

            Material dropOverride = null;
            if (parts.length >= 4 && !parts[3].isEmpty()) {
                dropOverride = parseMaterial(parts[3]);
            }

            int dropOverrideAmount = 1;
            if (parts.length >= 5) {
                dropOverrideAmount = Math.max(1, parseInt(parts[4], 1));
            }

            String mobSpawn = null;
            if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                mobSpawn = parts[5].trim().toUpperCase(Locale.ENGLISH);
            }

            output.add(new WeightedMaterial(material, weight, minPerkLevel, dropOverride, dropOverrideAmount, mobSpawn));
        }

        return output;
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }

        if (value instanceof List<?>) {
            List<String> output = new ArrayList<>();
            for (Object part : (List<?>) value) {
                output.add(String.valueOf(part));
            }
            return output;
        }

        return Collections.singletonList(String.valueOf(value));
    }

    private static class PlayerState {
        
        private long miningXp = 0L;
        private int miningLevel = 1;
        private long farmingXp = 0L;
        private int farmingLevel = 1;
        private long slayingXp = 0L;
        private int slayingLevel = 1;

        
        private int[] miningPerkLevels = new int[5];
        private int[] farmingPerkLevels = new int[5];
        private int[] slayingPerkLevels = new int[5];

        private long playerXp = 0L;
        private int playerLevel = 1;
        private int prestigeCount = 0;

        private int dailyOneblockBroken = 0;
        private boolean dailyOneblockClaimed = false;
        private final Map<String, Long> dailyTaskProgress = new HashMap<>();
        private final Map<String, Integer> claimedDailyTaskStages = new HashMap<>();
        private final Map<String, Long> challengeProgress = new HashMap<>();
        private final Set<String> claimedChallenges = new HashSet<>();
        private final Set<Integer> claimedPlayerLevelRewards = new HashSet<>();

        private final Map<String, Long> journeyProgress = new HashMap<>();
        private final Set<String> completedJourney = new HashSet<>();
        private final Set<String> claimedJourney = new HashSet<>();

        private final Map<String, Long> milestoneProgress = new HashMap<>();
        private final Map<String, Integer> milestoneTiersClaimed = new HashMap<>();
        private final Set<String> notifiedMilestones = new HashSet<>();
    }

    private static class IslandState {
        private long oneblockBrokenTotal = 0L;
        private LocationKey oneblockAnchor;

        
        private final Map<String, Integer> generatorLevels = new HashMap<>();
        private final Map<String, Map<Material, Integer>> generatorStorages = new HashMap<>();

        private final Set<String> unlockedMilestones = new HashSet<>();
        private final Set<LocationKey> grownTreeLogLocations = new HashSet<>();
        private static final int GROWN_LOCATION_CAP = 5000;
        private final Map<String, Integer> nodeLimitTiers = new HashMap<>();
        private final Map<String, Integer> spawnerLimitTiers = new HashMap<>();
        private final Map<String, Integer> dailyGrowthCounts = new HashMap<>();
        private final Map<String, Integer> growthLimitTiers = new HashMap<>();
        ItemStack pendingDropOverride = null;
        String pendingMobSpawn = null;
        UUID lastOneBlockMobEntityId = null;
    }

    private static class NodeState {
        private UUID islandId;
        private String typeKey;
        private int level;
        private double progress;
        private int stored;
        private boolean onCooldown;
        private long cooldownEndMillis;
    }

    private static class SpawnerState {
        private UUID islandId;
        private String typeKey;
        private int level;
        private double progress;
        private int stored;
    }

    private static class GeneratorState {
        private UUID islandId;
        private String trackKey;
        private UUID hologramUUID;
        private int level = 1;
        private final Map<Material, Integer> storage = new HashMap<>();
        private double tickProgress = 0.0;
    }

    private enum JourneyType {
        BREAK_BLOCK,
        KILL_MOB,
        CRAFT_ITEM,
        USE_COMMAND,
        ONEBLOCK_BREAK,
        REACH_SKILL_LEVEL,
        REACH_MILESTONE_TIER,
        COMPLETE_DAILY,
        REACH_PERK_LEVEL,
        EXPAND_GROWTH_LIMIT;

        private static JourneyType fromName(String name) {
            JourneyType type = EnumHelper.getEnum(JourneyType.class,
                    name == null ? "BREAK_BLOCK" : name.toUpperCase(Locale.ENGLISH));
            return type == null ? BREAK_BLOCK : type;
        }
    }

    private static class JourneyTaskDefinition {
        private String key;
        private String displayName;
        private String description = "";
        private JourneyType type;
        private long target;
        private boolean daily;
        private final Set<String> matchValues = new HashSet<>();
        private Reward reward = new Reward();
        private Material icon = null;

        private boolean matches(String value) {
            if (matchValues.isEmpty()) {
                return true;
            }

            if (value == null) {
                return false;
            }

            for (String match : matchValues) {
                if (type == JourneyType.USE_COMMAND) {
                    if (value.startsWith(match)) {
                        return true;
                    }
                } else {
                    if (value.equals(match)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static class OneBlockStage {
        private String key;
        private long requiredBreaks;
        private List<WeightedMaterial> weightedMaterials = new ArrayList<>();
    }

    private static class MilestoneDefinition {
        private String key;
        private String displayName;
        private String description = "";
        private List<Material> targetBlocks = new ArrayList<>();
        private List<EntityType> targetEntities = new ArrayList<>();
        private SkillTrack track;
        private int displayOrder = 0;
        private TreeMap<Integer, MilestoneTier> tiers = new TreeMap<>();
    }

    private static class MilestoneTier {
        private long requiredCount;
        private long skillXp;
        private long playerXp;
    }

    private static class NodeTypeDefinition {
        private String key;
        private String displayName;
        private Material displayBlock;
        private Material outputMaterial;
        private int outputPerCycle;
        private int growthSeconds;
        private long cooldownTicks = 1200L;
        private double fragmentChance = 0.0;
        private int maxLevel;
        private double upgradeBaseCost;
        private double upgradeCostMultiplier;
        private int maxStorageBase;
        private SkillTrack skillTrack;
        private int customModelData = 0;
        private String nexoItemId = null;
    }

    private static class SpawnerTypeDefinition {
        private String key;
        private String displayName;
        private EntityType entityType;
        private Material outputMaterial;
        private int outputPerCycle;
        private int spawnSeconds;
        private double fragmentChance = 0.0;
        private int maxLevel;
        private double upgradeBaseCost;
        private double upgradeCostMultiplier;
        private int maxStorageBase;
    }

    private static class GeneratorSettings {
        private boolean enabled;
        private int baseYield;
        private int yieldPerLevel;
        private int maxLevel;
        private double expansionBaseCost;
        private double expansionCostMultiplier;
        private int maxStorage = 3456;
        private int baseMaxPlacements = 1;
        private int maxPlacementsPerPerkLevel = 1;
        private int productionRateSeconds = 60;
        private final List<WeightedMaterial> weightedMaterials = new ArrayList<>();
    }

    private static class DailyTaskDefinition {
        private String key;
        private String displayName;
        private Material iconMaterial;
        private final List<Long> stageTargets = new ArrayList<>();
        private Reward baseStageReward = new Reward();
    }

    private static class DailyChallengeDefinition {
        String key;
        String displayName;
        String description = "";
        Material iconMaterial;
        String matchKey;
        long target;
        Reward reward = new Reward();
    }

    private static class LimitRule {
        private final int initialLimit;
        private final int addLimitPerTier;
        private final int maxUpgradeTier;
        private final List<Double> moneyCostsPerTier;
        private final List<Integer> fragmentCostsPerTier;
        private final List<Integer> tierLimits;

        private LimitRule(int initialLimit, int addLimitPerTier, int maxUpgradeTier,
                          List<Double> moneyCostsPerTier, List<Integer> fragmentCostsPerTier,
                          List<Integer> tierLimits) {
            this.initialLimit = initialLimit;
            this.addLimitPerTier = addLimitPerTier;
            this.maxUpgradeTier = maxUpgradeTier;
            this.moneyCostsPerTier = moneyCostsPerTier == null ? Collections.<Double>emptyList() : new ArrayList<>(moneyCostsPerTier);
            this.fragmentCostsPerTier = fragmentCostsPerTier == null ? Collections.<Integer>emptyList() : new ArrayList<>(fragmentCostsPerTier);
            this.tierLimits = tierLimits == null ? Collections.<Integer>emptyList() : new ArrayList<>(tierLimits);
        }

        private double getMoneyCostForTier(int tier) {
            if (moneyCostsPerTier.isEmpty()) return 0D;
            return moneyCostsPerTier.get(Math.max(0, Math.min(tier, moneyCostsPerTier.size() - 1)));
        }

        private int getFragmentCostForTier(int tier) {
            if (fragmentCostsPerTier.isEmpty()) return 0;
            return fragmentCostsPerTier.get(Math.max(0, Math.min(tier, fragmentCostsPerTier.size() - 1)));
        }

        private int getLimitForTier(int tier) {
            int clampedTier = Math.max(0, Math.min(maxUpgradeTier, tier));
            if (!tierLimits.isEmpty()) {
                int index = Math.max(0, Math.min(tierLimits.size() - 1, clampedTier));
                return Math.max(1, tierLimits.get(index));
            }

            return Math.max(1, initialLimit + clampedTier * addLimitPerTier);
        }

        private int getNextTierIncrease(int currentTier) {
            int clampedTier = Math.max(0, Math.min(maxUpgradeTier, currentTier));
            if (clampedTier >= maxUpgradeTier) {
                return 0;
            }

            int currentLimit = getLimitForTier(clampedTier);
            int nextLimit = getLimitForTier(clampedTier + 1);
            return Math.max(0, nextLimit - currentLimit);
        }
    }

    private static class TreasurePoolDefinition {
        private String key;
        private String displayName;
        private final List<WeightedReward> entries = new ArrayList<>();
    }

    private static class TreasureDropLink {
        final String pool;
        final double chance;
        TreasureDropLink(String pool, double chance) {
            this.pool = pool;
            this.chance = chance;
        }
    }

    private static class WeightedReward {
        private final int weight;
        private final Reward reward;

        private WeightedReward(int weight, Reward reward) {
            this.weight = weight;
            this.reward = reward;
        }
    }

    private static class WeightedMaterial {
        private final Material material;
        private final int weight;
        private final int minPerkLevel;
        private final Material dropOverride;
        private final int dropOverrideAmount;
        private final String mobSpawn;

        private WeightedMaterial(Material material, int weight) {
            this(material, weight, 0, null, 1, null);
        }

        private WeightedMaterial(Material material, int weight, int minPerkLevel) {
            this(material, weight, minPerkLevel, null, 1, null);
        }

        private WeightedMaterial(Material material, int weight, int minPerkLevel, Material dropOverride, int dropOverrideAmount) {
            this(material, weight, minPerkLevel, dropOverride, dropOverrideAmount, null);
        }

        private WeightedMaterial(Material material, int weight, int minPerkLevel, Material dropOverride, int dropOverrideAmount, String mobSpawn) {
            this.material = material;
            this.weight = weight;
            this.minPerkLevel = minPerkLevel;
            this.dropOverride = dropOverride;
            this.dropOverrideAmount = dropOverrideAmount;
            this.mobSpawn = mobSpawn;
        }
    }

    private static class Reward {
        private double money = 0D;
        private long skillXp = 0L;
        private long playerXp = 0L;
        private final List<ItemStack> items = new ArrayList<>();
        private final List<String> commands = new ArrayList<>();

        private Reward copy() {
            Reward reward = new Reward();
            reward.money = money;
            reward.skillXp = skillXp;
            reward.playerXp = playerXp;
            for (ItemStack item : items) {
                reward.items.add(item.clone());
            }
            reward.commands.addAll(commands);
            return reward;
        }

        private boolean isEmpty() {
            return money <= 0D && skillXp <= 0L && playerXp <= 0L && items.isEmpty() && commands.isEmpty();
        }
    }

    private static class EvolvedConfig {
        private ConfigurationSection guiSection;
        private Material oneBlockFallbackMaterial;

        private long xpPerNaturalBlockBreak;
        private long xpPerOneBlockBreak;
        private long xpPerMobKill;
        private long xpPerCraft;

        private int perkMaxLevel;
        private int[] perkUnlockLevels = new int[]{1, 5, 10, 15, 20};
        private double perkCropGrowthBonusPerLevel;
        private double perkNodeYieldBonusPerLevel;
        private double perkSpawnerSpeedBonusPerLevel;
        private double perkGeneratorYieldBonusPerLevel;
        private double perkOneBlockBonusDropPerLevel;

        private long skillLevelStepXp;
        private long playerLevelStepXp;
        private TreeMap<Integer, Long> skillLevelThresholds = new TreeMap<>();
        private TreeMap<Integer, Long> playerLevelThresholds = new TreeMap<>();

        private int dailyOneBlockTargetBreaks;
        private Reward dailyOneBlockReward = new Reward();
        private Map<String, DailyTaskDefinition> dailyTasks = new LinkedHashMap<>();
        private List<DailyChallengeDefinition> dailyChallengePool = new ArrayList<>();
        private int dailyChallengeCount = 10;

        private List<OneBlockStage> oneBlockStages = new ArrayList<>();
        private List<MilestoneDefinition> milestones = new ArrayList<>();
        private List<MilestoneDefinition> miningMilestones = new ArrayList<>();
        private List<MilestoneDefinition> farmingMilestones = new ArrayList<>();
        private List<MilestoneDefinition> slayingMilestones = new ArrayList<>();

        private Material fragmentItemMaterial = Material.PRISMARINE_SHARD;
        private String fragmentItemName = "&b%type% Fragment";
        private List<String> fragmentItemLore = new ArrayList<>();

        private Material nodeItemMaterial;
        private String nodeItemName;
        private List<String> nodeItemLore = new ArrayList<>();
        private Map<String, NodeTypeDefinition> nodeTypes = new LinkedHashMap<>();
        private LimitRule defaultNodeLimitRule = new LimitRule(5, 2, 10, Collections.<Double>emptyList(), Collections.<Integer>emptyList(), Collections.<Integer>emptyList());
        private Map<String, LimitRule> nodeLimitRules = new HashMap<>();

        private Material spawnerItemMaterial;
        private String spawnerItemName;
        private List<String> spawnerItemLore = new ArrayList<>();
        private Map<String, SpawnerTypeDefinition> spawnerTypes = new LinkedHashMap<>();
        private LimitRule defaultSpawnerLimitRule = new LimitRule(4, 1, 10, Collections.<Double>emptyList(), Collections.<Integer>emptyList(), Collections.<Integer>emptyList());
        private Map<String, LimitRule> spawnerLimitRules = new HashMap<>();

        private Map<String, GeneratorSettings> generators = new LinkedHashMap<>();
        private Map<String, JourneyTaskDefinition> journeyTasks = new LinkedHashMap<>();
        private List<String> journeyHeaderLore = null;
        private String journeyHeaderName = null;
        private Map<String, Integer> dailyGrowthLimits = new HashMap<>();
        private double growthLimitUpgradeBaseCost = 1000.0;
        private double growthLimitUpgradeCostMultiplier = 1.5;
        private int growthLimitUpgradeAmount = 50;
        private int growthLimitMaxTier = 20;
        private int growthLimitFragmentCostBase = 5;

        private Material treasureItemMaterial;
        private String treasureItemName;
        private List<String> treasureItemLore = new ArrayList<>();
        private Map<String, TreasurePoolDefinition> treasurePools = new LinkedHashMap<>();
        private double prestigeCost = 100000.0;
        private Map<String, List<TreasureDropLink>> treasureLinks = new HashMap<>();
        private double perkTreasureChanceBonusPerLevel = 0.1;
        private double fragmentChancePerLevel = 0.05;
        private double fragmentBaseChance = 0.03;
        private List<Double> perkUpgradeCosts = new ArrayList<>();
        private Map<String, String> messages = new HashMap<>();
    }

    private enum GuiMenuType {
        SKILL_HUB,
        MILESTONES,
        PERKS,
        LIMITS,
        GROWTH_LIMITS,
        FARMING_LIMITS,
        JOURNEY,
        DAILIES,
        DAILY_CHALLENGES,
        PLAYER_LEVELS,
        GENERATOR,
        TREASURE_POOL_VIEW
    }

    private enum SkillTrack {
        MINING("Mining"),
        SLAYING("Slaying"),
        FARMING("Farming");

        private final String displayName;

        SkillTrack(String displayName) {
            this.displayName = displayName;
        }
    }

    private static class EvolvedGuiHolder implements InventoryHolder {
        private final GuiMenuType menuType;
        private final SkillTrack skillTrack;
        private final UUID viewerId;
        private final int page;

        private EvolvedGuiHolder(GuiMenuType menuType, SkillTrack skillTrack, UUID viewerId) {
            this(menuType, skillTrack, viewerId, 0);
        }

        private EvolvedGuiHolder(GuiMenuType menuType, SkillTrack skillTrack, UUID viewerId, int page) {
            this.menuType = menuType;
            this.skillTrack = skillTrack;
            this.viewerId = viewerId;
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private static class LocationKey {
        private final String world;
        private final int x;
        private final int y;
        private final int z;

        private LocationKey(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static LocationKey fromBlock(Block block) {
            Location location = block.getLocation();
            return new LocationKey(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        private static LocationKey fromLocation(Location location) {
            return new LocationKey(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        private static LocationKey deserialize(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }

            String[] split = value.split(";");
            if (split.length != 4) {
                return null;
            }

            try {
                return new LocationKey(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        private String serialize() {
            return world + ";" + x + ";" + y + ";" + z;
        }

        private boolean matches(Location location) {
            if (location == null || location.getWorld() == null) {
                return false;
            }

            return world.equals(location.getWorld().getName()) && x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ();
        }

        private Location toLocation() {
            World targetWorld = Bukkit.getWorld(world);
            if (targetWorld == null) {
                return null;
            }
            return new Location(targetWorld, x, y, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LocationKey)) {
                return false;
            }
            LocationKey that = (LocationKey) o;
            return x == that.x && y == that.y && z == that.z && world.equals(that.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, y, z);
        }
    }

    private class EvolvedSkillsPapi extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "evolvedskills";
        }

        @Override
        public String getAuthor() {
            return "JXK";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onRequest(org.bukkit.OfflinePlayer player, String params) {
            if (player == null) return "";
            if ("player_level".equals(params)) {
                PlayerState ps = playerStates.get(player.getUniqueId());
                return ps == null ? "0" : String.valueOf(ps.playerLevel);
            }
            return null;
        }
    }

}
