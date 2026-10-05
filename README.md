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


#### Reaction
Folder: `data/modid/smelter/reactions`  
Reactions let players throw items into the top of a smelter to convert the molten material inside.
They can be used for fluxes (slag to metal), for creating alloy-like conversions (metal to another metal)
or for adding impurities (metal to slag).

A reaction requires the following
- item: item id or tag (starting with #)
- from: the fluid which gets consumed
    - metal: metal id
    - state: `metal` (molten metal) or `slag`
- to: the fluid which is created
    - metal: metal id
    - state: `metal` or `slag`
- amount_per_item: how many mB get converted per consumed item

Extra fields for a reaction (not required)
- min_temperature: minimum temperature the smelter needs to have (default 0)
- smelter_types: array of smelter type ids in which this reaction works - if missing, it works in all smelter types
- replace: overrides an existing reaction with the same item, from metal and from state

Example (flux, converts slag back to metal):
```json
{
  "item": "cupellation:quartz_powder",
  "from": {
    "metal": "cupellation:netherite",
    "state": "slag"
  },
  "to": {
    "metal": "cupellation:netherite",
    "state": "metal"
  },
  "amount_per_item": 50
}
```

Example (metal to another metal, only in hot smelters):
```json
{
  "item": "minecraft:charcoal",
  "from": {
    "metal": "cupellation:iron",
    "state": "metal"
  },
  "to": {
    "metal": "cupellation:steel",
    "state": "metal"
  },
  "amount_per_item": 36,
  "min_temperature": 900,
  "smelter_types": [
    "cupellation:deepslate_smelter"
  ]
}
```

### Mod Integration API
Since v1.0.3, Cupellation provides an API for other mods to register custom smelter blocks and mold types.

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

#### Registering custom mold types

```java
package com.example.test;

import net.cupellation.api.*;
import net.minecraft.util.Identifier;

import java.util.Set;

public class TestCupellationPlugin implements CupellationEntrypoint {

    @Override
    public void registerMoldTypes() {

        CupellationAPI.registerMoldType(
                new MoldType(
                        "hammer_head",
                        576,
                        true,
                        Set.of(
                                Identifier.of("minecraft", "netherite")
                        )
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

---

#### MoldType

A `MoldType` contains the following properties:

- suffix: item suffix used for item registration
- mb: required molten metal amount
- extraOutput: generates extra casted tool part items for tool materials
- blacklist: set of blacklisted metal identifiers
- moldingMetalTypeId (optional): metal used to cast the mold itself from a stamp item
- strategy (optional): `MoldResultStrategy` which decides the result item for a metal (default: suffix strategy)
- craftableAsClayMold: if true, a clay mold of this type can be crafted with a stamp item
- stampItemIds: fixed stamp items for the clay mold (mutually exclusive with `extraOutput`)

Simple example (the short constructor sets `craftableAsClayMold` to the value of `extraOutput`):

```java
new MoldType("hammer_head", 576, true, Set.of())
```

Advanced example with the builder:

```java
MoldType.builder("diamond", 144)
        .moldingMetalTypeId(Identifier.of("cupellation", "gold"))
        .strategy(new MappedResultMoldStrategy(Map.of(
                Identifier.of("cupellation", "diamond"), Identifier.of("minecraft", "diamond"))))
        .craftableAsClayMold(Identifier.of("minecraft", "diamond"))
        .build();
```

Restrictions:
- `extraOutput = true` only works with the default suffix strategy.
- `extraOutput` and fixed `stampItemIds` can't be combined.