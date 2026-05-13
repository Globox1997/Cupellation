# Cupellation
Cupellation is a mod based around a smelting mechanic.

### Installation
Cupellation is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) to be installed separately; all other dependencies are installed with the mod.

### License
Cupellation is licensed under GPLv3.

### Datapacks
Metals, their material and fuels are data driven.

If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.wiki/w/Data_Pack)
website and try to create your first one for the vanilla game. Each existing file can be overriden by setting replace = true.

#### Metal
Folder: `data/modid/smelter/metals`  
A metal requires the following:
- id: basically an identifier for the metal
- name: translation key, translate it then via lang file
- required temperature: minimum temperature to get smelted
- color: hex color of the molten material
- cooled_color: hex color of the cooled material
- texture: metal texture identifier including the path
- density: integer to determine which metal is heavier and floats above others
- grades: low, mid and high temperature ranges to determine the quality of casts

Extra fields for a metal (not required)
- ingot: ingot item id used in the casting table
- block: block id used in the casting basin
- flux item: item id which determines the correct flux item
- alloy_from: list of required metals to get mixed with ratio (max 2 currently)

Example:
```json
{
  "id": "cupellation:netherite",
  "name": "metal.cupellation.netherite",
  "required_temp": 1300,
  "color": "8E8A8E",
  "cooled_color": "484548",
  "texture": "cupellation:fluid/molten_netherite",
  "ingot": "minecraft:netherite_ingot",
  "block": "minecraft:netherite_block",
  "flux_item": "cupellation:quartz_powder",
  "density": 250,
  "alloy_from": [
    {
      "metal": "cupellation:gold",
      "parts": 1
    },
    {
      "metal": "cupellation:debris",
      "parts": 1
    }
  ],
  "grades": {
    "low": {
      "min": 1300,
      "max": 1400
    },
    "mid": {
      "min": 1400,
      "max": 1500
    },
    "high": {
      "min": 1500,
      "max": 1600
    }
  }
}
 ```

#### Material
Folder: `data/modid/smelter/items`  
A material requires the following
- item: item id or tag (starting with #)
- metal_type: metal id
- smelt_time: time in ticks to get smelted
- yield: how much metal is the output of this item

Example:
```json
{
  "item": "minecraft:gold_ore",
  "metal_type": "cupellation:gold",
  "smelt_time": 200,
  "yield": 144
}
```

#### Fuel
Folder: `data/modid/smelter/fuels`  
A fuel requires the following
- item: item id or tag (starting with #)
- max_temperature: the maximal temperature this item outputs as a fuel
- burn_time: duration of the fuel

Example:
```json
{
  "item": "minecraft:blaze_powder",
  "max_temperature": 1400,
  "burn_time": 1000
}
```

#### Type
Folder: `data/modid/smelter/types`  
A smelter type requires the following
- id: unique id
- max_temperature: max temperature the smelter type can have - optional!
- blocks: array of block ids or tags (starting with #)
- allowed_metals: Optional array list of metal ids - if empty all metals can be smelted in this smelter type

Example:
```json
{
  "id": "cupellation:deepslate_smelter",
  "max_temperature": 800,
  "blocks": [
    "minecraft:deepslate_bricks",
    "minecraft:deepslate_tiles",
    "minecraft:polished_deepslate",
    "cupellation:deepslate_brick_smelter",
    "cupellation:deepslate_brick_glass",
    "cupellation:deepslate_brick_drain"
  ],
  "allowed_metals": [
    "cupellation:iron",
    "cupellation:gold",
    "cupellation:copper"
  ]
}
```

### Mod Integration API
Since v1.0.3, Cupellation provides an API for other Fabric mods to register custom smelter blocks.

#### Dependency
Your mod must depend on Cupellation.  
Example `fabric.mod.json` dependency:

```json
"depends": {
  "cupellation": "*"
}
```

---

#### Registering a custom smelter
Create a class implementing `CupellationEntrypoint`:

```java
package com.example.test;

import net.cupellation.api.*;

public class TestCupellationPlugin implements CupellationEntrypoint {

    @Override
    public void registerSmelterTypes() {

        CupellationAPI.registerSmelterType(
                new SmelterType(
                        ModBlocks.TEST_SMELTER,
                        ModBlocks.TEST_FAUCET,
                        ModBlocks.TEST_BASIN,
                        ModBlocks.TEST_TABLE
                )
        );
    }
}
```

---

#### fabric.mod.json
Register your plugin entrypoint inside your `fabric.mod.json`:

```json
{
  "entrypoints": {
    "cupellation": [
      "com.example.test.TestCupellationPlugin"
    ]
  }
}
```

---

#### SmelterType
A `SmelterType` contains the following blocks:

- smelter
- faucet
- casting basin
- casting table

Example:

```java
new SmelterType(
    MY_SMELTER,
    MY_FAUCET,
    MY_CASTING_BASIN,
    MY_CASTING_TABLE
)
```