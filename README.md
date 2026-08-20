![INFABRIC](https://gist.githubusercontent.com/nxfx21/77a4d90b250f0cd8567b736259f296df/raw/584572cb2a97a1b574ed89d455c27f5bc6e27597/infabric.png)

---

## What is it?

A port of the original Infuse plugin to Fabric, for use in singleplayer, and modded servers.

## Changes

The Brewing Stand UI has been replaced with a Crafting Table! Use it to craft your effects.

> **Admin Note:** Admins can re-enable brewing by adjusting `config.yml`.

## How Effects & Rituals Work

#### Crafting Broadcast

* When an effect is crafted, its crafting coordinates are broadcast to the entire server.

#### Augmented Effects

* The first effect of each type to be crafted becomes an **augmented effect**.
* Augmented effects have **half the cooldown** of regular effects.

#### Ritual Requirement

* Crafting an augmented effect triggers a ritual.
* Rituals last for **10 minutes** before the augmented effect is finally produced.
* Players don't need to do anything during the ritual except be the one to claim the effect once it finishes!

## Effect Abiltities

In-game, run `/infuses` to see what they do!

---

## Commands

### `/controls`

This changes between using [Command Keys](https://modrinth.com/mod/commandkeys) and using offhand to activate abilities

### `/lspark`, `/rspark`

This activates your ability in your first and second slot

### `/cleareffect <player>`

This clears the players effects

### `/cooldown <player>`

This resets the cooldown of the player

### `/ldrain`, `/rdrain`

This turns your effect back into a bottle that can be used for trading

### `/swap`

This swaps the order of your effects around

### `/infuse gui`

This can be allowed to preview/take effects

### Optional

You can use [Command Keys](https://modrinth.com/mod/commandkeys) for custom keybinds (bind /rspark to something and /lspark) but its optional

---

## Recipes

The first effect crafted starts a ritual and gives an augmented effect, next 3 give a regular one, and then its uncraftable

---

## Crafting Notes

* **Speed Effect:** Crafting requires **1 Dune Armor Trim** and **1 Eye Armor Trim**.
* **Heart Effect:** Can be crafted using **any type of potion**.
* **Frost & Regeneration Effects:** Both require **Pearlescent Froglights**.

---

### Recipe Reference Cards

---

#### Recipe Overviews

![Default Recipes](https://cdn.modrinth.com/data/cached_images/cd619d2c9eb42d4368405aba345063aa01d7f5fb.png)
![Extra Recipes](https://cdn.modrinth.com/data/cached_images/3c54969c686f29ffcef9fb43d5b1455d4d3d4bba.png)
![Thief Recipe](https://cdn.modrinth.com/data/cached_images/59eef76248b09bd0d7f1fd0e6310c439bdf3b9ff.png)

---

#### Individual Effects

![Strength](https://cdn.modrinth.com/data/cached_images/f8d3e628a5cecbe35a06f14a704de471af057eea_0.webp)
![Emerald](https://cdn.modrinth.com/data/cached_images/2293614a520fca5f7509ce350c09ae9c05d90d66_0.webp)
![Feather](https://cdn.modrinth.com/data/cached_images/e0b9605a3c88eb8a5a47e2757fa93bc41a586e23_0.webp)
![Fire](https://cdn.modrinth.com/data/cached_images/c184439f1a18b76b87124d397c91af8d87c175bd_0.webp)
![Frost](https://cdn.modrinth.com/data/cached_images/443ff03065ca13345938dd3019e4e6d59c2fadc2_0.webp)
![Haste](https://cdn.modrinth.com/data/cached_images/8fdf33da37da86407227730fe23b287e271bb5e3_0.webp)
![Heart](https://cdn.modrinth.com/data/cached_images/53438a8052f78c1e3a0bd6b1ce5d066440f0d6de_0.webp)
![Ocean](https://cdn.modrinth.com/data/cached_images/72ab1cedc189b45675ea46c6bf4c09b385fc7b0a_0.webp)
![Invisibility](https://cdn.modrinth.com/data/cached_images/ce40aab7cd7fbeb5da6b066feecfc1ba25eb54c6_0.webp)
![Speed](https://cdn.modrinth.com/data/cached_images/abaaf7501999a808014d6576adceb00f9a947bad_0.webp)
![Regeneration](https://cdn.modrinth.com/data/cached_images/dbded7b5cee9c4d6db425716a3ed8611ce85fb32_0.webp)
![Thunder](https://cdn.modrinth.com/data/cached_images/6f3181d1829d9e5fcca07725fa20b9223f0b6c19_0.webp)
![Ender](https://cdn.modrinth.com/data/cached_images/21a561d047f1fd3339103200a4e58f6e77855588_0.webp)
![Apophis](https://cdn.modrinth.com/data/cached_images/4a4ba4921556d68bdcf2f28e9f4e53361456e6b4.png)

---

## Credits - For making the Original InfuseSMP plugin

* @TurboJax
* @CatAdmirer
* @duffmansjnr-hub
* @amhunter1

## Ported by nxfx21
