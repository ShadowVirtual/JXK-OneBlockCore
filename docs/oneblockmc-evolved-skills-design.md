# OneBlockMC Evolved Skills Technical Design

Version: 1.0 (Full)
Status: Ready for implementation
Date: 2026-04-08
Target server: Paper 1.21.8
Primary dependency: SuperiorSkyblock2

## 1. Purpose

This document defines the full technical design for the OneBlockMC Evolved Skills system, implemented as a custom progression core integrated with SuperiorSkyblock2.

The design is intended to be implementation-ready and covers:

- Gameplay systems and progression loops
- Runtime architecture and module boundaries
- Data model and persistence strategy
- Event tracking and anti-exploit policy
- Full config model and example schemas
- Menus, commands, permissions, and UX states
- Testing and rollout plan

## 2. Product Scope

## 2.1 Included Systems

1. Skills and Milestones
2. Skill Perks and perk upgrades
3. One Block island core
4. Resource Nodes
5. Upgradeable Spawners
6. Daily Growth Limits
7. Resource Generators
8. Treasure pools and bonus drops
9. Daily Tasks and Daily Challenges
10. Player Level and Prestige
11. Player Journey tutorial

## 2.2 Out of Scope for V1

- Cross-server sync and shared progression across proxies
- External managed databases as mandatory dependencies
- Public web API for third-party dashboards
- Custom client-side UI mods

## 3. Hard Requirements

1. Fully configurable progression values and content.
2. Trusted server-side tracking only; no client-driven progression.
3. Anti-exploit validation for crops, logs, and mined resources.
4. Strict island boundary and island permission checks using SuperiorSkyblock2.
5. Daily resets must be deterministic and idempotent.
6. System must remain performant under high player concurrency.

## 4. Definitions and IDs

## 4.1 Core Enums

- SkillType: MINING, FARMING, SLAYING
- SourceType: ONE_BLOCK, RESOURCE_NODE, GROWN_CROP, GROWN_TREE, SPAWNER_MOB, GENERATOR
- RequirementType:
  - RUN_COMMAND
  - CRAFT_ITEM
  - HARVEST_RESOURCE
  - REACH_MILESTONE_TIER
  - REACH_SKILL_LEVEL
  - COMPLETE_DAILY_CHALLENGE

## 4.2 Perk Keys

Shared (all skills):

- RESOURCE_ACCESS
- FRAGMENT_CHANCE
- TREASURE_CHANCE
- GENERATOR_LIMIT

Unique:

- MINING_NODE_SPEED
- FARMING_CROP_GROWTH_SPEED
- SLAYING_SPAWNER_SPEED

## 4.3 Resource Keys

All tracked resources use stable lowercase keys such as:

- iron_ore
- deepslate_iron_ore
- wheat
- oak_log
- zombie

These keys are used consistently across milestones, treasure sources, journey tasks, and daily challenges.

## 5. Architecture

## 5.1 Module Layout

1. Bootstrap Module
- Plugin startup/shutdown
- Service registration
- Config bootstrap

2. Island Integration Module
- SuperiorSkyblock bridge services
- Island resolver by player and location
- Island permission gateway

3. Tracking Module
- Event listeners
- Source attribution and trust validation
- Anti-exploit filters

4. Progression Module
- Skills, Milestones, Perks
- Player XP and Player Level
- Daily Tasks/Challenges
- Player Journey

5. World Objects Module
- One Block lifecycle
- Resource Nodes
- Spawners
- Generators
- Daily growth enforcement

6. Rewards Module
- Money handling (Vault)
- Fragment economy
- Treasure roll pipeline
- Reward dispatch and claim checks

7. UI Module
- Menu rendering and click handling
- Interactive chat messages
- Icon state generation

8. Persistence Module
- Data access layer
- Transaction boundaries
- Runtime cache flush scheduler

## 5.2 Threading and Scheduler Model

Main thread:

- Bukkit event handlers
- Block/entity world updates
- Menu interactions

Async thread pool:

- Database reads/writes
- Non-critical recalculations
- Daily challenge assignment precompute

Schedulers:

- 1-second loop: node timers, growth limit checks, tracked runtime cleanup
- 60-second loop: generator production cycle
- Daily reset loop: daily tasks/challenges reset and growth counter reset

## 5.3 Runtime Cache Strategy

- Player cache keyed by UUID
- Island runtime cache keyed by islandId
- Location indexes keyed by world+xyz for O(1) lookup of nodes/spawners/generators/oneblock

Write policy:

- Periodic async flush every configurable interval (default 30s)
- Immediate flush on critical operations (claims, purchases, prestige)
- Final synchronous flush during shutdown

## 6. Data Model

## 6.1 PlayerProgress Aggregate

- playerId
- skills[SkillType]
  - level (1-50)
  - xp
  - perkLevels[PerkKey] (1-10)
- milestones[milestoneId]
  - totalProgress
  - claimedTier
- playerLevel
  - level (1-100)
  - xp
  - prestigeCount
  - claimedRewards (set of level numbers)
- dailies
  - resetDate
  - taskStagesProgress
  - challengeAssignments
  - challengeCompletions
- journey
  - currentTaskIndex
  - taskProgressById
  - claimedTaskRewards

## 6.2 IslandProgress Aggregate

- islandId
- oneBlockAnchor
  - location
  - currentEntryId
  - breakCount
- placedNodes[location] => NodeInstance
- placedSpawners[location] => SpawnerInstance
- placedGenerators[location] => GeneratorInstance
- expansionLimits
  - nodeLimitsByType
  - spawnerLimitsByType
  - growthLimitsByCropType
- dailyGrowthCountersByCropType

## 6.3 World Object Records

NodeInstance:

- nodeType
- location
- state: BEDROCK_COOLDOWN or MATURE
- remainingSeconds
- ownerIslandId

SpawnerInstance:

- spawnerType
- location
- ownerIslandId
- baseDelayTicks

GeneratorInstance:

- generatorType (MINING/FARMING/SLAYING)
- location
- ownerIslandId
- inventoryContents
- lastProducedAtEpoch

## 6.4 Suggested Relational Tables

- ob_player
- ob_skill
- ob_perk
- ob_milestone
- ob_player_level
- ob_player_level_claim
- ob_daily_state
- ob_journey_state
- ob_island_runtime
- ob_node
- ob_spawner
- ob_generator
- ob_limit_expansion
- ob_treasure_pool
- ob_meta_reset

Indexes:

- ob_node(world, x, y, z)
- ob_spawner(world, x, y, z)
- ob_generator(world, x, y, z)
- ob_skill(player_id, skill_type)
- ob_milestone(player_id, milestone_id)

## 7. Feature Specifications

## 7.1 One Block Core

Each island owns one One Block anchor location.

Break cycle:

1. Validate breaker is on an island and allowed to interact.
2. Validate broken location is island one-block anchor.
3. Resolve previous entry metadata.
4. Emit trusted ResourceHarvestedEvent for previous block.
5. Roll next eligible entry by weight.
6. Place new base block at anchor.
7. Place optional bonus block above anchor.
8. Spawn optional entity above anchor.

Entry eligibility:

- Entry requirement is a perk gate in the form skill + tier.
- Player must meet required RESOURCE_ACCESS tier for that skill.

Weighted selection:

- Candidate set = all entries passing requirement gate.
- If candidate set is empty, fallback to configured default entry.
- Use weighted random over integer weights > 0.

Example entry schema:

```yaml
entries:
  dirt_base:
    skill: FARMING
    requiredAccessTier: 1
    blockType: DIRT
    bonusType: none
    entityType: none
    weight: 40
  wheat_patch:
    skill: FARMING
    requiredAccessTier: 4
    blockType: FARMLAND
    bonusType: wheat_full
    entityType: none
    weight: 10
  zombie_ore:
    skill: SLAYING
    requiredAccessTier: 5
    blockType: IRON_ORE
    bonusType: none
    entityType: ZOMBIE
    weight: 6
```

## 7.2 Skills, Milestones, and Overflow

Skills:

- Mining: valid mining resources only
- Farming: valid crop/log harvest only
- Slaying: valid kill events only

Milestones:

- Each milestone has 20 tiers.
- Tier unlock condition uses cumulative progress.
- Tier rewards are claim-based (not auto-claim).

Overflow rule:

- totalProgress is always incremented on valid events.
- claimedTier is separate from totalProgress.
- Player can overfill progress before claiming.
- Claiming tier N does not erase overflow toward tier N+1.

Tier reached notification:

- Chat message must include click action opening milestone menu.

Example:

- You have reached Tier 3 for your Iron Ore Milestone! Click here to view.

## 7.3 Skill Levels and Perk Unlock Routing

Each skill has 50 levels with configurable XP requirements.

Level-up:

- Recalculate level whenever skill XP changes.
- Multiple levels can be granted in one update if XP jumps.

Unlock routing:

- Skill levels unlock access to specific perk tiers.
- Unlock config maps level -> one or many perk tier unlocks.

## 7.4 Skill Perks

Per skill: 5 perks, each with 10 tiers.

Upgrade flow:

1. Verify target tier exists.
2. Verify target tier unlocked by skill level.
3. Verify player has money.
4. Deduct cost.
5. Apply new perk tier.
6. Persist and notify.

Effects:

- RESOURCE_ACCESS: unlocks One Block and Generator resource entries.
- FRAGMENT_CHANCE: chance to drop matching fragment on harvest.
- TREASURE_CHANCE: multiplier on base treasure chance.
- GENERATOR_LIMIT: max count of that skill generator per island.
- MINING_NODE_SPEED: reduces node respawn time.
- FARMING_CROP_GROWTH_SPEED: increases crop/sapling growth speed.
- SLAYING_SPAWNER_SPEED: increases spawner summon frequency.

Formula examples:

- nodeRespawn = baseRespawnSeconds * (1 - nodeSpeedPercent)
- cropGrowthMultiplier = 1 + cropGrowthPercent
- spawnerInterval = baseDelayTicks / (1 + spawnerSpeedPercent)
- finalTreasureChance = baseChance * treasureMultiplier

## 7.5 Resource Nodes

Behavior:

- Placeable custom items by node type.
- Placed node starts in BEDROCK_COOLDOWN state.
- After timer ends, node becomes mature block.
- Breaking mature node grants drops/progression and returns to cooldown.
- Sneak-break removes node and returns node item.

Timer gating:

- Countdown only while chunk loaded.
- Countdown only while at least one island member is online.

Limits:

- Per-island per-node-type max placement.
- Limit expansion menu allows tiered increases.
- Expansion cost = money + matching fragments.

Placement denial message:

- You have reached the limit for {nodeType}. Expand it in Mining Limits. Click to view.

## 7.6 Upgradeable Spawners

Behavior:

- Placeable custom spawner items by entity type.
- Vanilla-compatible spawn mechanics where practical.
- Spawn delay scales with SLAYING_SPAWNER_SPEED perk.
- Spawned mobs tagged for trusted kill attribution.

Limits:

- Per-island per-spawner-type limit.
- Expandable through tiered money + fragment upgrades.

## 7.7 Daily Growth Limits

Each crop and sapling type has:

- Daily growth cap per island
- Expandable cap via upgrades

When cap reached:

- Future growth stage updates are denied until reset or limit expansion.

Tracked by:

- BlockGrowEvent
- StructureGrowEvent

Daily reset:

- Clears all daily growth counters for each island.

## 7.8 Resource Generators

Generator model:

- Three generator families: MINING, FARMING, SLAYING.
- Per-island limit by skill perk GENERATOR_LIMIT.
- Generates exactly 1 item per minute while active.

Active condition:

- Owner island has online member and generator chunk is loaded.

Resource selection:

- Roll from same eligible resource pool as One Block entries filtered by skill.

Storage:

- Internal chest-like inventory.
- Production pauses when full.

UX:

- Physical block with hologram title:
  - Mining Generator
  - Farming Generator
  - Slaying Generator
- Right click opens generator menu.

## 7.9 Treasure System

Treasure source map:

- resourceKey maps to one or more pool rolls.
- each roll has pool name and base chance.

Roll sequence:

1. Resolve source resourceKey.
2. Read configured pool list.
3. For each pool roll, compute final chance using TREASURE_CHANCE multiplier.
4. On success, select item from saved pool inventory.
5. Deliver item to player or fallback to ground drop if inventory full.

Admin management:

- /treasurepool save <name>
  - Reads targeted chest inventory and stores snapshot.
  - Existing pool of same name is overwritten.
- /treasurepool view
  - Opens pool index menu.
- Click pool
  - Opens editable inventory view.

## 7.10 Player Journey

Journey is an ordered list of tasks.

Rules:

- Only the current task can progress.
- Next task stays locked until current task claimed.
- Completion and claim are separate states.

Task visuals:

- Green icon: completed/claimable
- Yellow icon: current in progress
- Red icon: locked

Supported task requirement types:

- RUN_COMMAND
- CRAFT_ITEM
- HARVEST_RESOURCE
- REACH_MILESTONE_TIER
- REACH_SKILL_LEVEL
- COMPLETE_DAILY_CHALLENGE

Task reward types:

- money
- fragments
- playerXp

## 7.11 Player Level and Prestige

Player XP sources:

- milestone claims
- journey claims
- daily task stage claims
- daily challenge claims

Level range:

- 1 to 100

Reward icon states:

- claimed => EMPTY_MINECART
- claimable => CHEST_MINECART
- locked => TNT_MINECART

Prestige:

- Available at level 100.
- Costs configurable money amount.
- Resets level progression and claim states.
- prestigeCount increments.

Recommendation:

- Reset to level 1 after prestige for cleaner UX.

Player XP rank multipliers by permission:

- oneblockmc.playerxp.multiplier.1_2
- oneblockmc.playerxp.multiplier.1_3
- oneblockmc.playerxp.multiplier.1_5
- oneblockmc.playerxp.multiplier.2_0
- oneblockmc.playerxp.multiplier.2_5
- oneblockmc.playerxp.multiplier.3_0

If multiple permissions are present, use highest multiplier.

## 7.12 Daily Tasks and Daily Challenges

Daily Tasks:

- Kill Mobs
- Mine Resources
- Harvest Crops

Each daily task has 10 stages with:

- requirement amount
- claim rewards (playerXp, money, fragments)

Daily Challenges:

- Up to 10 challenge slots per player.
- Slot availability based on permissions:
  - oneblockmc.dailychallenge.slot.1 ... slot.10
- Challenges use specific requirement and amount.
- Single claim reward per challenge.

## 8. Anti-Exploit and Trust Rules

## 8.1 Mining Trust Rules

Valid mining progression sources:

- One Block anchor block
- Mature Resource Node block

Invalid sources:

- Naturally generated world ore
- Player-placed ore blocks

## 8.2 Crop Trust Rules

Only count crops when both are true:

1. Crop is in valid grown state at harvest time.
2. Crop was not placed by player in fully grown state.

For stem-like and placeable crops, maintain placement metadata and growth lifecycle metadata.

## 8.3 Log Trust Rules

- Track all placed logs as playerPlaced.
- Mark natural logs generated from StructureGrowEvent as natural.
- Count only natural logs for farming progression.

## 8.4 Mob Trust Rules

- Count kills only for entities tagged from One Block or tracked custom spawner.
- Ignore natural world mob kills unless explicitly configured.

## 8.5 Island Scope Rules

All tracked actions must pass:

- location belongs to island
- actor has island interaction permission

## 9. Configuration Reference

Recommended plugin data directory:

- plugins/OneBlockMC/

## 9.1 File Layout

```text
plugins/OneBlockMC/
  config.yml
  messages.yml
  oneblock/
    entries.yml
    bonus-types.yml
  skills/
    mining-levels.yml
    farming-levels.yml
    slaying-levels.yml
  milestones/
    mining.yml
    farming.yml
    slaying.yml
  perks/
    mining.yml
    farming.yml
    slaying.yml
  limits/
    nodes.yml
    spawners.yml
    growth.yml
  generators.yml
  treasure/
    sources.yml
    pools.yml
  journey/
    tasks.yml
  dailies/
    tasks.yml
    challenges.yml
  player-levels.yml
  menus/
    skill-hub.yml
    milestones.yml
    perks.yml
    limits.yml
    dailies.yml
    journey.yml
    player-levels.yml
```

## 9.2 config.yml

```yaml
storage:
  type: sqlite
  sqliteFile: data.db
save:
  flushIntervalSeconds: 30
reset:
  timezone: UTC
  time: "00:00"
performance:
  generatorTickSeconds: 60
  nodeTickSeconds: 1
  runtimeCleanupSeconds: 60
economy:
  useVault: true
```

## 9.3 oneblock/entries.yml

```yaml
defaultEntryId: dirt_base
entries:
  dirt_base:
    skill: FARMING
    requiredAccessTier: 1
    blockType: DIRT
    bonusType: none
    entityType: none
    weight: 40
  stone_basic:
    skill: MINING
    requiredAccessTier: 1
    blockType: STONE
    bonusType: none
    entityType: none
    weight: 30
  zombie_ore:
    skill: SLAYING
    requiredAccessTier: 5
    blockType: IRON_ORE
    bonusType: none
    entityType: ZOMBIE
    weight: 6
```

## 9.4 oneblock/bonus-types.yml

```yaml
bonusTypes:
  none:
    kind: NONE
  wheat_full:
    kind: BLOCK
    blockType: WHEAT
    age: 7
    offsetY: 1
  sugar_cane_single:
    kind: BLOCK
    blockType: SUGAR_CANE
    height: 1
    offsetY: 1
```

## 9.5 milestones/<skill>.yml

```yaml
milestones:
  iron_ore:
    displayName: Iron Ore
    icon: IRON_ORE
    resourceKey: iron_ore
    tiers:
      1: { requirement: 50, skillXp: 20, playerXp: 10 }
      2: { requirement: 150, skillXp: 30, playerXp: 12 }
      3: { requirement: 350, skillXp: 45, playerXp: 18 }
      # ... up to 20
```

## 9.6 skills/<skill>-levels.yml

```yaml
levels:
  1:
    skillXpRequirement: 0
    unlocks: []
  2:
    skillXpRequirement: 100
    unlocks: ["RESOURCE_ACCESS:2"]
  3:
    skillXpRequirement: 220
    unlocks: ["TREASURE_CHANCE:2"]
  # ... up to 50
```

## 9.7 perks/<skill>.yml

```yaml
perks:
  RESOURCE_ACCESS:
    tiers:
      1: { unlockAtSkillLevel: 1, costMoney: 0, effect: { accessTier: 1 } }
      2: { unlockAtSkillLevel: 4, costMoney: 5000, effect: { accessTier: 2 } }
  FRAGMENT_CHANCE:
    tiers:
      1: { unlockAtSkillLevel: 1, costMoney: 0, effect: { chance: 0.01 } }
      2: { unlockAtSkillLevel: 5, costMoney: 8000, effect: { chance: 0.02 } }
  TREASURE_CHANCE:
    tiers:
      1: { unlockAtSkillLevel: 1, costMoney: 0, effect: { multiplier: 1.00 } }
      2: { unlockAtSkillLevel: 6, costMoney: 10000, effect: { multiplier: 1.15 } }
  GENERATOR_LIMIT:
    tiers:
      1: { unlockAtSkillLevel: 1, costMoney: 0, effect: { limit: 2 } }
      2: { unlockAtSkillLevel: 7, costMoney: 12000, effect: { limit: 4 } }
  MINING_NODE_SPEED:
    tiers:
      1: { unlockAtSkillLevel: 1, costMoney: 0, effect: { percent: 0.00 } }
      2: { unlockAtSkillLevel: 8, costMoney: 15000, effect: { percent: 0.08 } }
```

For farming and slaying files, replace unique perk key accordingly.

## 9.8 limits/nodes.yml

```yaml
nodeTypes:
  iron:
    itemId: node_iron
    matureBlock: IRON_ORE
    baseRespawnSeconds: 120
    milestoneResourceKey: iron_ore
    initialLimit: 5
    expansions:
      1: { addLimit: 2, costMoney: 5000, costFragments: 25 }
      2: { addLimit: 3, costMoney: 10000, costFragments: 40 }
```

## 9.9 limits/spawners.yml

```yaml
spawnerTypes:
  zombie:
    itemId: spawner_zombie
    entityType: ZOMBIE
    baseDelayTicks: 200
    initialLimit: 4
    expansions:
      1: { addLimit: 1, costMoney: 6000, costFragments: 30 }
      2: { addLimit: 2, costMoney: 12000, costFragments: 50 }
```

## 9.10 limits/growth.yml

```yaml
growthTypes:
  wheat:
    material: WHEAT
    initialDailyLimit: 300
    expansions:
      1: { addLimit: 100, costMoney: 4000, costFragments: 20 }
      2: { addLimit: 150, costMoney: 9000, costFragments: 35 }
  oak_sapling:
    material: OAK_SAPLING
    initialDailyLimit: 60
    expansions:
      1: { addLimit: 20, costMoney: 5000, costFragments: 25 }
```

## 9.11 generators.yml

```yaml
generators:
  MINING:
    itemId: gen_mining
    blockType: DROPPER
    inventorySize: 27
    produceEverySeconds: 60
  FARMING:
    itemId: gen_farming
    blockType: DROPPER
    inventorySize: 27
    produceEverySeconds: 60
  SLAYING:
    itemId: gen_slaying
    blockType: DROPPER
    inventorySize: 27
    produceEverySeconds: 60
```

## 9.12 treasure/sources.yml

```yaml
sources:
  iron_ore:
    - { pool: mining_common, chance: 0.08 }
    - { pool: mining_rare, chance: 0.02 }
  zombie:
    - { pool: slaying_common, chance: 0.12 }
```

## 9.13 treasure/pools.yml

```yaml
pools:
  mining_common:
    items: []
  mining_rare:
    items: []
  slaying_common:
    items: []
```

Note: Items are stored as serialized ItemStack data.

## 9.14 journey/tasks.yml

```yaml
tasks:
  1:
    id: run_skills
    requirementType: RUN_COMMAND
    requirementData: "skills"
    amount: 1
    rewards:
      money: 1000
      fragments:
        farming: 10
      playerXp: 20
  2:
    id: mine_iron
    requirementType: HARVEST_RESOURCE
    requirementData: "iron_ore"
    amount: 30
    rewards:
      money: 2000
      fragments:
        mining: 20
      playerXp: 30
```

## 9.15 player-levels.yml

```yaml
prestigeCostMoney: 500000
levels:
  1:
    requirementXp: 0
    rewards:
      money: 0
      fragments: {}
  2:
    requirementXp: 150
    rewards:
      money: 3000
      fragments:
        mining: 10
  # ... up to 100
```

## 9.16 dailies/tasks.yml

```yaml
dailyTasks:
  kill_mobs:
    displayName: Kill Mobs
    stages:
      1: { requirement: 25, rewards: { playerXp: 25, money: 1000, fragments: { slaying: 8 } } }
      2: { requirement: 60, rewards: { playerXp: 40, money: 1800, fragments: { slaying: 12 } } }
      # ... up to 10
  mine_resources:
    displayName: Mine Resources
    stages:
      1: { requirement: 30, rewards: { playerXp: 25, money: 1000, fragments: { mining: 8 } } }
  harvest_crops:
    displayName: Harvest Crops
    stages:
      1: { requirement: 30, rewards: { playerXp: 25, money: 1000, fragments: { farming: 8 } } }
```

## 9.17 dailies/challenges.yml

```yaml
slots:
  1: oneblockmc.dailychallenge.slot.1
  2: oneblockmc.dailychallenge.slot.2
  3: oneblockmc.dailychallenge.slot.3
  4: oneblockmc.dailychallenge.slot.4
  5: oneblockmc.dailychallenge.slot.5
  6: oneblockmc.dailychallenge.slot.6
  7: oneblockmc.dailychallenge.slot.7
  8: oneblockmc.dailychallenge.slot.8
  9: oneblockmc.dailychallenge.slot.9
  10: oneblockmc.dailychallenge.slot.10
challengePool:
  mine_deepslate_iron:
    requirementType: HARVEST_RESOURCE
    requirementData: deepslate_iron_ore
    amount: 120
    rewards: { money: 7000, playerXp: 80, fragments: { mining: 40 } }
  kill_zombies:
    requirementType: HARVEST_RESOURCE
    requirementData: zombie
    amount: 90
    rewards: { money: 7000, playerXp: 80, fragments: { slaying: 40 } }
```

## 10. Menus and UX

## 10.1 Skill Hub

Single custom-art menu with three sections:

- Mining header + three buttons
- Slaying header + three buttons
- Farming header + three buttons

Each section routes to:

- Milestones menu
- Perks menu
- Limits/feature menu

## 10.2 Locked Resource State

When a resource is not unlocked by RESOURCE_ACCESS:

- Display GRAY_DYE icon, or configured locked icon.
- Tooltip includes unlock hint with required tier.

## 10.3 Clickable Messages

Use clickable chat component actions for:

- Milestone tier reached notifications
- Limit reached notifications
- Task completion notifications

## 10.4 Icon Conventions

- Journey: green/yellow/red state icons
- Player level rewards: empty/chest/TNT minecart states
- Daily task stages: filled progress indicator and claim button state

## 11. Commands

Player:

- /skills
- /milestones <mining|farming|slaying>
- /perks <mining|farming|slaying>
- /journey
- /dailies
- /playerlevel
- /prestige

Admin:

- /oneblockmc reload
- /oneblockmc debug <player>
- /oneblockmc give <player> <customItemId> <amount>
- /treasurepool save <name>
- /treasurepool view

## 12. Permissions

Core:

- oneblockmc.use
- oneblockmc.admin

Daily challenge slots:

- oneblockmc.dailychallenge.slot.1 ... oneblockmc.dailychallenge.slot.10

Player XP multipliers:

- oneblockmc.playerxp.multiplier.1_2
- oneblockmc.playerxp.multiplier.1_3
- oneblockmc.playerxp.multiplier.1_5
- oneblockmc.playerxp.multiplier.2_0
- oneblockmc.playerxp.multiplier.2_5
- oneblockmc.playerxp.multiplier.3_0

## 13. SuperiorSkyblock2 Integration Contract

Mandatory checks before any tracked action:

1. Resolve island at action location.
2. Verify actor is island member (or permitted by island role).
3. Verify location is inside island boundary.
4. Verify island is not in restricted state.

Integration adapters:

- IslandResolver
- IslandMembershipService
- IslandPermissionService
- IslandOnlineService

## 14. Internal Event Contracts

- ResourceHarvestedEvent(playerId, islandId, skillType, resourceKey, sourceType, amount)
- MilestoneTierReachedEvent(playerId, skillType, milestoneId, tier)
- MilestoneTierClaimedEvent(playerId, skillType, milestoneId, tier, skillXp, playerXp)
- SkillLevelChangedEvent(playerId, skillType, oldLevel, newLevel)
- PlayerLevelChangedEvent(playerId, oldLevel, newLevel)
- DailyTaskStageCompletedEvent(playerId, taskKey, stage)
- DailyChallengeCompletedEvent(playerId, challengeId)
- DailyResetEvent(resetDate)

## 15. Daily Reset Design

Reset runs at configured timezone/time and performs:

1. Advance logical reset date.
2. Rebuild daily challenge assignments per player.
3. Reset daily task stage progress and claims.
4. Reset island daily growth counters.
5. Persist reset checkpoint.

Idempotence rule:

- If resetDate already processed, skip without side effects.

## 16. Error Handling and Edge Cases

1. Missing config references:
- Fail startup with clear error listing file and key.

2. Entry pool empty after perk filtering:
- Use defaultEntryId fallback.

3. Economy unavailable with money rewards enabled:
- Block money transactions and log warning.

4. Full inventories on reward grant:
- Drop items naturally at player location.

5. Player logs out mid-claim:
- Use transactional claim mark then reward dispatch with retry-safe markers.

## 17. Performance Requirements

Targets:

- No visible TPS impact from One Block or node tick loops under normal load.
- O(1) lookup for location-bound objects.
- Async persistence with bounded queue and backpressure logging.

Operational protections:

- Cap per-tick processed runtime objects per island if needed.
- Defer heavy recomputations to async worker.
- Purge stale placement metadata with TTL.

## 18. Testing Strategy

## 18.1 Unit Tests

- Weighted picker distribution and fallback behavior
- Milestone overflow and claim progression correctness
- Skill level recalculation for large XP jumps
- Perk unlock gate and cost validation
- XP multiplier resolution using highest permission

## 18.2 Integration Tests

- One Block full break cycle with bonus block and entity
- Node place/mature/harvest/sneak-reclaim lifecycle
- Spawner speed scaling by perk tier
- Growth update cancellation at daily cap
- Generator production and full-inventory pause
- Treasure roll from multiple pools and item delivery

## 18.3 Exploit Tests

- Placed fully-grown crop cannot be farmed for progress
- Placed logs cannot be farmed for progress
- World-generated ores outside One Block/Node do not grant mining progress
- Untagged mobs do not grant slaying progress

## 18.4 Load and Soak

- 100+ active islands with nodes/spawners/generators running
- Extended runtime with repeated daily resets
- Validate memory stability and no cache leaks

## 19. Delivery Plan

Phase 1: Core foundation

- Service architecture, persistence, config loader, SuperiorSkyblock adapters

Phase 2: Trusted tracking and progression core

- Harvest attribution, milestones, skills, perk unlock model

Phase 3: One Block + Resource Nodes

- One Block weighted generation and node lifecycle

Phase 4: Spawners + Growth Limits + Generators

- World object systems and limit expansions

Phase 5: Dailies + Journey + Player Level + Prestige

- Full progression UX and claim systems

Phase 6: Treasure tooling + polish

- Treasure pool admin commands, balancing, exploit hardening

Phase 7: Stabilization and release

- Performance pass, migration checks, release candidates

## 20. Acceptance Criteria

1. Every major system is fully driven by config.
2. Progression cannot be farmed via known placement exploits.
3. Milestone overflow works exactly as designed.
4. Perk unlocks require skill-level gates and upgrade purchase.
5. Island limits for nodes/spawners/growth are enforced and expandable.
6. Generator output is passive, weighted, and pauses when storage is full.
7. Daily task/challenge reset is deterministic and idempotent.
8. Player level and prestige loop functions with claim resets.
9. Treasure pools can be saved, viewed, edited, and rolled from source config.
10. System remains stable and performant during sustained runtime.

## 21. Outstanding Product Decisions

1. Prestige reset exact target: level 0 or level 1 (recommend level 1).
2. Timer activity gate: any island member online or owner-only online.
3. Exact list of trackable resource keys for launch.
4. Final UX copy and locale strategy for messages.
5. Balance pass values for costs, rates, and XP curves.

## 22. Implementation Notes for this Repository

This project is already multi-module and contains NMS/platform hooks.

Recommended placement strategy for OneBlockMC implementation:

- Add new internal package tree in the main plugin module under src/main.
- Keep all version-specific code isolated in existing NMS abstractions if required.
- Avoid direct hard-coding to a single minor version where stable API can be used.

This specification is the source of truth for implementation scope and behavior for OneBlockMC Evolved Skills on Paper 1.21.8 with SuperiorSkyblock2.
