# Node Ores

Surface stone nodes that grow mineable ore clusters. Mod id `nodeores`, package `com.workshopcraft.nodeores`, version 1.0.0.

## What a node is

A node is a 2x2 stone outcrop, one block tall, made of four `nodeores:node_base` blocks. Each base can hold one ore cluster in the block directly above it, so a node has four growth spots. The stone is a low pile of cuboids with a taller pad in the middle. A cluster is its own block, with its own outline and mining cracks. Mining one cluster does not reset the others.

Clusters are not full cubes and they do not glow. Iron and copper use rocky shapes. Diamond, emerald, amethyst, and quartz use a crystal shape. Every other ore uses a generic rocky shape. The exact block id is what controls drops; the shape is only the prototype look.

Growth has three shapes over the growth time: a nub, a larger partly exposed cluster, then the mature shape in the last third. The cluster can be mined only when the growth timer finishes. Until then, survival mining does not break it.

## Timing

Defaults are 10 minutes between spawn attempts and 10 minutes of growth. An attempt places 1 or 2 clusters into empty spots, capped at 4 clusters on the node. The first attempt is a random delay up to one interval so neighbouring nodes do not all fruit on the same tick. After that, attempts are one full interval apart.

Timers advance only while the node's chunks are loaded, in one-second steps. They do not use the wall clock, and they do not catch up to global game time while the chunk is unloaded. A node that sits on a chunk border only tries to spawn when all four base chunks are loaded. A cluster that is already placed keeps growing for as long as its own chunk stays loaded.

Breaking any stone block removes the whole node. That drops the configured base bonus once and does not drop cluster loot. Harvesting a finished cluster drops whatever the resolved ore block would drop for that tool, including Fortune and Silk Touch, and leaves the stone in place. A pick that is too weak breaks a finished cluster and drops nothing, the same as the ore it copies. The spot can grow a new cluster later.

## Worldgen and commands

Overworld surface generation attempts one node per chunk, then keeps about 1 in `worldgenOneIn` of those attempts (default 32). The 2x2 has to sit inside a single chunk, on solid ground, with two blocks of air. Oceans and blocked ground are skipped.

Operators can place and inspect without waiting:

- Stand in open air on solid ground and run `/nodeores place`. The node uses your feet as one corner and tries the four horizontal directions.
- `/nodeores grow` fills nearby nodes and marks every cluster harvestable.
- `/nodeores inspect` prints ore id, shape, and timer for nearby clusters.
- `/nodeores reload` reads `config/nodeores.json` again.

These commands need permission level 2.

## Config

`config/nodeores.json` is written the first time the game starts.

```json
{
  "ores": ["c:ores/iron", "c:ores/copper"],
  "spawnIntervalSeconds": 600,
  "growthSeconds": 600,
  "clustersPerSpawnMin": 1,
  "clustersPerSpawnMax": 2,
  "maxClusters": 4,
  "worldgenOneIn": 32,
  "baseBonus": ["minecraft:cobblestone|4"]
}
```

An ore entry can be a block id (`minecraft:iron_ore`) or a block tag (`c:ores/iron` or `#minecraft:iron_ores`). A tag uses the first non-deepslate block in the tag. If `c:ores/<material>` is missing or empty, the mod falls back to `minecraft:<material>_ore` (`quartz` falls back to `minecraft:nether_quartz_ore`). An empty `ores` list stops new clusters. Times below 10 seconds are raised to 10. The base bonus lines are `item_id` or `item_id|count`.

## Build

The version and loader set matches More Foods and Simple Golems:

| Minecraft | Loaders |
| --- | --- |
| 1.20.1 | Fabric, Forge |
| 1.21.1 | Fabric, NeoForge |
| 26.1.2 | Fabric, NeoForge |
| 26.2 | Fabric, NeoForge |
| 26.3 | Fabric, NeoForge |

```
./gradlew build
```

That builds every row above. One project at a time is `./gradlew :26.1.2-fabric:build`. Jars are named `nodeores-<loader>-<minecraft>-1.0.0.jar`. Each project runs the same seven unit tests, covering growth stages, config parsing, and ore shapes. Those tests and a successful compile are the checks this repository runs. They do not start a client or a dedicated server.

## Limitations

Biome modifiers, loot tables, and block outlines were not exercised in a client or dedicated server.

Explosions that destroy a finished cluster can drop that ore without Fortune. If the same blast also destroys the stone, the base bonus still drops only once, and cluster loot is skipped when the stone removal wins the race. The stone bonus does not follow Silk Touch. Pistons cannot push a node. There is no block item, so the node cannot be picked up.
