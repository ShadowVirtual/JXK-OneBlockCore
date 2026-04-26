# Evolved Skills — Build Plan
**Spec:** EVOLVED_SKILLS_SPEC.pdf  
**Custom code:** `src/main/java/.../module/evolvedskills/EvolvedSkillsService.java` (~5,500 lines)

---

## What Shadow Built

| System | State |
|--------|-------|
| One Block Core (break/regenerate) | Working |
| Skill XP tracking (basic increment) | Working |
| Milestone framework | Partial |
| Resource Nodes (Wheat/Melon/Cactus) | Built — wrong types |
| Spawners (Zombie/Skeleton/Creeper) | Built — matches spec |
| Generator (cobblestone, single type) | Built — wrong (should be 3 per skill) |
| Player Journey | Unverified |
| Player Leveling | Unverified |
| Daily Challenges & Tasks | Unverified |
| Treasure system | Code exists — unverified |
| Data persistence (YAML) | Working |

---

## Phase 0 — Performance & 1.21.11 Compatibility

> Fix these before any feature work. Lag at high player counts is a client-visible problem and will kill retention on launch day.

### 1.21.11 Compatibility

- [x] **NMS confirmed** — `NMS/v1_21_11/` exists; `nms.compile_v1_21=true` in gradle.properties. SSB2 already targets 1.21.11 natively.
- [ ] **Material/EntityType audit** — Scan all `EvolvedSkillsService.java` Material and EntityType references. Any hardcoded string name that was renamed in 1.21.x will silently return `null` at runtime. Every `Material.valueOf()` must be guarded with `EnumHelper.getEnum()` or `Material.matchMaterial()` fallback — the existing codebase does this in spots but needs a full pass.
- [ ] **Deprecated API pass** — Check for any use of deprecated Bukkit/Paper methods that were removed between 1.20 and 1.21. Run a compile against Paper 1.21.11 API and resolve all warnings/errors.

### Performance Fixes (Root Causes of Lag)

- [x] **Async YAML save** — YAML is built on main thread (state is read safely), then `yaml.save()` dispatched via `runTaskAsynchronously()`. Eliminates disk I/O lag spikes.
- [x] **Fix O(n²) in `tickGenerators()`** — Replaced `getIslandIdByState()` reverse scan with direct `islandStates.entrySet()` iteration. Now O(n).
- [ ] **Move tick tasks off main thread** — `nodeTickTask`, `spawnerTickTask`, `generatorTickTask`, `dailyTickTask` all use `runTaskTimer()` (main thread). Pure state-update logic (timer decrement, reward calculation) should run on `runTaskTimerAsynchronously()`. Any Bukkit API calls (block spawn, particle, sound) must be scheduled back to main thread via `runTask()`.
- [x] **Remove unnecessary `ArrayList` copy in tick loops** — Removed `new ArrayList<>(nodeStates.entrySet())` copies in `tickNodes()` and `tickSpawners()`. Iterate the map directly.
- [x] **Cache per-island perk lookups** — Added `islandPerkCache` map in `tickNodes()` and `tickSpawners()`. One `getPlayerState()` call per island, not per node/spawner.
- [ ] **Per-player state files (long-term)** — Single YAML file for all players/islands/nodes is a bottleneck at scale. Split into per-player files (`playerdata/<uuid>.yml`) so saves are smaller and partial writes don't block the full dataset. Can be deferred to Phase 4 but keep in mind during Phase 3 rebuilds — don't make the monolith bigger.

---

## Phase 1 — Fix Known Bugs

- [x] **Per-skill level-ups** — `PlayerState` now has `miningXp/Level`, `farmingXp/Level`, `slayingXp/Level`. Block breaks → Mining, crop harvests → Farming, mob kills → Slaying. `addSkillXp()` takes `SkillTrack`. YAML migrates old saves automatically.
- [x] **Menus audit** — all 7 menus audited (Skill Hub, Milestones, Perks, Limits, Journey, Dailies, Player Levels). Hub header now shows per-skill levels. Milestones click now shows meaningful progress feedback. All back buttons, routing, and click handlers verified correct.
- [x] **Resource Access perk** — `WeightedMaterial` now has `minPerkLevel`. Config format: `MATERIAL:WEIGHT:MIN_PERK_LEVEL`. `chooseOneBlockMaterial()` filters pool by player's perk level; falls back to full pool if all entries are gated.

---

## Phase 2 — Verify What's Partially Built

Go through each system, test it, and confirm it matches spec exactly:

- [ ] **One Block Core** — verify bonus block on top (sugar cane, rose etc.), entity spawning on top, entry weight system, entry requirement gating by Resource Access perk tier
- [ ] **Milestone tracking** — verify anti-exploit rules: ores/stones only from Resource Nodes or One Block (not player-placed); crops only grown (not placed in grown state); logs only from grown trees
- [ ] **Milestone overflow** — extra progress before claiming should carry into the next tier on claim
- [ ] **Milestone chat notification** — clickable chat message when new tier reached, opens Milestone Menu
- [ ] **Player Journey** — test all task types (run command, craft item, harvest resource, reach milestone tier, reach skill level, complete daily challenge); verify sequential unlock (must complete previous)
- [ ] **Player Leveling** — verify Player XP sources (milestones, journey tasks, daily challenges, daily bonuses); verify chat level display; test prestige at 100
- [ ] **Daily Challenges & Tasks** — verify 3 core tasks (Kill Mobs, Mine Resources, Harvest Crops) with 10 stages each; verify daily reset; verify challenge count gated by player rank via permissions
- [ ] **Treasure system** — test /treasurepool save and /treasurepool view; verify treasure drops from One Block, nodes, spawner mobs, crops, trees; verify Treasure Chance perk applies

---

## Phase 3 — Rebuild What's Wrong

- [x] **Mining Resource Nodes** — Added `track: FARMING` to wheat/melon/cactus; added Stone, Iron, Gold Mining nodes with `track: MINING`. Config-driven; admin can give via `/admin evolvedskills givenode <player> stone|iron|gold`.
- [x] **Generator system** — Replaced single `generator:` with `generators: {mining, farming, slaying}`. Per-track levels (`IslandState.generatorLevels`) and storages (`generatorStorages`). Command: `/generator <mining|farming|slaying> [status|claim [mat]|expand]`. Legacy state auto-migrates to mining track.
- [x] **Shift+mine to reclaim nodes** — Shift+break on a node/spawner returns the seed/core to inventory instead of destroying it.
- [ ] **Online-only node timer** — respawn timer must only count down while the player is online and loading the island; verify this is actually implemented correctly

---

## Phase 4 — Polish & Config

- [ ] **Skills menu layout** — custom art background, 3×3 layout: Mining/Farming/Slaying headers (decorative, not buttons), 3 sub-buttons each (Milestones, Limits, Perks)
- [ ] **Locked milestone icons** — locked milestones should show gray dye (or custom locked icon), not their actual block icon
- [ ] **Player XP multipliers** — 1.2x, 1.3x, 1.5x, 2x, 2.5x, 3x multipliers applied via permission nodes on player rank
- [ ] **Per-skill config files** — verify each skill has its own config file for milestones (20 tiers each: requirement, skill XP, player XP) and skill levels (50 levels each: XP requirement, perk unlock)
- [ ] **Node limit expand menus** — identical design to milestone menus; triggered by clickable chat message on placement when at limit
- [ ] **Daily Growth Limits** — verify per-crop and per-sapling daily cap works; crops/saplings freeze when limit hit until next day or expanded; expandable via money + fragments

---

## Order of Attack

1. Phase 1 (bugs) — fix these before anything else or testing is meaningless
2. Phase 2 (verify) — test everything that exists; note anything else that's broken
3. Phase 3 (rebuild) — node types and generator system are the biggest chunks
4. Phase 4 (polish) — menus and config last

---

## Notes

- Do NOT touch sound effects yet — Jake is still updating the spec doc
- Jake is sending an updated version of EVOLVED_SKILLS_SPEC.pdf — replace this file when it arrives
- Shadow's payout: ~$150 for partial work completed
