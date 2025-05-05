# AdamsJewelryDream Plugin Installation Guide

This guide explains how to install the AdamsJewelryDream plugin into an existing Microbot installation.

## What is AdamsJewelryDream?

A plugin for RuneLite/Microbot that automates the process of enchanting various types of jewelry. 
The plugin supports all levels of enchantment spells and various jewelry types.

## Files Included

The following files are provided in the `adamsjewelrydream` folder:

- `AdamsJewelryDreamPlugin.java` - Main plugin class
- `AdamsJewelryDreamConfig.java` - Configuration interface
- `AdamsJewelryDreamScript.java` - Main script logic
- `AdamsJewelryDreamOverlay.java` - Status overlay
- `JewelryType.java` - Enum for jewelry types
- `EnchantMode.java` - Enum for enchant modes

## Installation Steps

1. **Copy Files**: 
   - Copy all the `.java` files from the `adamsjewelrydream` folder
   - Paste them into your Microbot project at: `runelite-client/src/main/java/net/runelite/client/plugins/microbot/adamsjewelrydream/`
   - Create the `adamsjewelrydream` directory if it doesn't exist

2. **Register Plugin**:
   - If you haven't added a custom plugin before, you'll need to register it in the Microbot system
   - Open `runelite-client/src/main/java/net/runelite/client/plugins/microbot/Microbot.java`
   - Find the section where plugins are registered (look for `injector.getInstance()` calls)
   - Add this line: `injector.getInstance(AdamsJewelryDreamPlugin.class)`

3. **Compile and Run**:
   - Compile your RuneLite/Microbot project
   - The plugin should appear in the plugin list with the name "Adams Jewelry Dream"

## Usage

1. Open the configuration panel for the "Adams Jewelry Dream" plugin
2. Select the jewelry type you want to enchant
3. Choose your enchant mode (Normal or AFK)
4. Select whether to use a staff for rune reduction
5. Click "Start" to begin enchanting

## Requirements

- The plugin is designed to be used at the Grand Exchange bank
- You must have the required magic level for the chosen enchantment
- Cosmic runes and appropriate elemental runes in your bank
- Unenchanted jewelry in your bank

## Troubleshooting

- If the plugin doesn't appear, check that you've registered it correctly
- Verify that all the files are in the correct directory
- Make sure your Microbot framework is up to date 