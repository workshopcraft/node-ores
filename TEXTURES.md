# Textures

Every texture is a plain 16x16 PNG. Replace the file and restart the game. They live in `src/main/resources/assets/nodeores/textures/block/`.

| File | Use |
| --- | --- |
| `node_base.png` | Stone outcrop. All four corners, every face. |
| `cluster_iron.png` | Iron clusters, all three growth stages. |
| `cluster_copper.png` | Copper clusters, all three growth stages. |
| `cluster_rocky.png` | Other rocky ores, such as coal, gold, redstone, and lapis. |
| `cluster_crystal.png` | Diamond, emerald, amethyst, and quartz clusters. |

The models are simple boxes. Painting the PNG is enough. Edit the JSON only if you want a new shape. A node still stores the real block id (`minecraft:iron_ore` and so on). These five files are only the prototype look.
