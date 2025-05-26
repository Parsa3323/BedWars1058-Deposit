<div align="center">

# Bedwars1058-Deposit

`1.8, 1.9`

![GitHub Sponsors](https://img.shields.io/github/sponsors/Parsa3323?label=Sponsor&logo=GitHub)
![GitHub contributors](https://img.shields.io/github/contributors/Parsa3323/BedWars1058-Deposit?label=Contributors&logo=GitHub)
![GitHub Releases](https://img.shields.io/github/downloads/Parsa3323/BedWars1058-Deposit/total?label=Downloads&logo=GitHub)
![GitHub last commit](https://img.shields.io/github/last-commit/Parsa3323/BedWars1058-Deposit?label=Last%20Commit&logo=GitHub)
![GitHub issues](https://img.shields.io/github/issues/Parsa3323/BedWars1058-Deposit?label=Open%20Issues&logo=GitHub)
![GitHub repo size](https://img.shields.io/github/repo-size/Parsa3323/BedWars1058-Deposit?color=yellow&logo=github)
![GitHub license](https://img.shields.io/github/license/Parsa3323/BedWars1058-Deposit?color=purple&logo=github)
![Forks](https://img.shields.io/github/forks/Parsa3323/BedWars1058-Deposit?style=)
</div>

[//]: # (![Spigot Downloads]&#40;https://img.shields.io/spiget/downloads/PLUGIN_ID?color=blue&logo=spigot&#41;)
[//]: # (![GitHub Activity Graph]&#40;https://github-readme-activity-graph.vercel.app/graph?username=Parsa3323&theme=github-dark&#41;)


<div align="center">

[//]: # ([![S]&#40;https://img.shields.io/badge/Go_to-Spigot-yellow?style=for-the-badge&#41;]&#40;https://www.spigotmc.org/resources/advancedarmorstands.121022/&#41;)

[//]: # ([![S]&#40;https://img.shields.io/badge/Go_to-Wiki-orange?style=for-the-badge&#41;]&#40;https://docs.advancedarmorstands.ir/&#41;)

[//]: # ([![S]&#40;https://img.shields.io/badge/Go_to-PolyMart-green?style=for-the-badge&#41;]&#40;https://www.polymart.org/product/7829/advancedarmorstands&#41;)


[![Repo Stats](https://github-readme-stats.vercel.app/api/pin/?username=Parsa3323&repo=BedWars1058-Deposit&theme=dark)
](https://github.com/Parsa3323/BedWars1058-Deposit)



</div>

[//]: # (![FirstImg]&#40;https://biaupload.com/do.php?imgf=org-3b039f0f3c191.png&#41;)

[//]: # ()
[//]: # (![2Img]&#40;https://biaupload.com/do.php?imgf=org-02a4d92ff3c92.png&#41;)
<div align="center">

# Table of Content

</div>

- [Requirements](#requirements)

- [Spigot](https://www.spigotmc.org/resources/advancedarmorstands.121022/)
- [Configs](#Configs)
    - [Main config](#Main-Configuration)


<div align="center">

# Configs

This document contains the configuration details for BedWars1058-Deposit.

## Main Configuration

The `main.yml` configuration file allows you to enable or disable debugging.


</div>

```yaml
# ──────────────────────────────────────────────────────────────
#   Punch to deposit - By Parsa3323
# ──────────────────────────────────────────────────────────────
#   This configuration file controls various aspects of the addon.
#   Make sure to read the comments carefully before changing any settings.
# ──────────────────────────────────────────────────────────────

# LOG LEVEL:
# Determines the level of logging that will be shown in the console.
# Available options:
# - SEVERE   → Shows only critical errors.
# - WARNING  → Displays warnings and serious issues.
# - INFO     → Standard logging (recommended for most cases).
# - CONFIG   → Shows additional configuration details.
# - FINE     → Provides debugging information (useful for developers).
# - FINER    → Even more detailed debugging logs.
# - FINEST   → Maximum debugging details (may spam the console).
# Default is INFO. Change only if needed for debugging purposes.
log-level: FINE

data-saving-method: SQLITE

# ──────────────────────────────────────────────────────────────

# DISABLE HOLOGRAM AFTER DEATH:
# If enabled (true), the deposit hologram will be removed after the player dies.
disable-hologram-after-death: false

# ──────────────────────────────────────────────────────────────

# HOLOGRAM REGISTER EVENT:
# Determines when the deposit hologram should be registered in the game.
hologram-register-event: GameStateChangeEvent

# ──────────────────────────────────────────────────────────────

# DEPOSIT WHOLE ITEMSTACK:
# If enabled (true), depositing an item will move all matching item stacks
# (same type as the item in hand) from the player's inventory to the Ender Chest.
deposit-whole-itemstack: false

# ──────────────────────────────────────────────────────────────

# SHIFT-CLICK ON CHEST TO SET:
# If enabled (true), while in BedWars1058 setup mode,
# players can shift-click on an Ender Chest or Chest to register it
# as a valid deposit chest for holograms.
shift-click-on-chest-to-set: true

# ──────────────────────────────────────────────────────────────

# SET CHEST LOCATIONS ON PLAYER JOIN:
# If enabled (true), all chest locations will be saved when the a player joins the server.
set-chest-locations-on-join: true

```


<div align="center">



</div>

<div align="center">

# Requirements
</div>

- [Bedwars1058](https://github.com/andrei1058/BedWars1058)

<div align="center">

# Contributors

<a href="https://github.com/Parsa3323/BedWars1058-Deposit/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Parsa3323/BedWars1058-Deposit" />
</a>

</div>







