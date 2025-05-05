package net.runelite.client.plugins.microbot.adamsjewelrydream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

/**
 * Enum representing different types of jewelry that can be enchanted.
 * Each type has an unenchanted ID (input), enchanted ID (output), required magic level,
 * experience gained, and enchant spell needed.
 */
@Getter
@RequiredArgsConstructor
public enum JewelryType {
    // Sapphire Jewelry - Level 1 Enchant (Level 7 Magic, Water Rune + Cosmic Rune)
    SAPPHIRE_RING("Sapphire ring", ItemID.SAPPHIRE_RING, ItemID.RING_OF_RECOIL, MagicAction.ENCHANT_SAPPHIRE_JEWELLERY, 7, 17, 1),
    SAPPHIRE_AMULET("Sapphire amulet", ItemID.SAPPHIRE_AMULET, ItemID.GAMES_NECKLACE8, MagicAction.ENCHANT_SAPPHIRE_JEWELLERY, 7, 17, 1),
    SAPPHIRE_NECKLACE("Sapphire necklace", ItemID.SAPPHIRE_NECKLACE, ItemID.GAMES_NECKLACE8, MagicAction.ENCHANT_SAPPHIRE_JEWELLERY, 7, 17, 1),
    SAPPHIRE_BRACELET("Sapphire bracelet", ItemID.SAPPHIRE_BRACELET_11072, ItemID.GAMES_NECKLACE8, MagicAction.ENCHANT_SAPPHIRE_JEWELLERY, 7, 17, 1),
    
    // Emerald Jewelry - Level 2 Enchant (Level 27 Magic, 3 Air Runes + Cosmic Rune)
    EMERALD_RING("Emerald ring", ItemID.EMERALD_RING, ItemID.RING_OF_DUELING8, MagicAction.ENCHANT_EMERALD_JEWELLERY, 27, 37, 2),
    EMERALD_AMULET("Emerald amulet", ItemID.EMERALD_AMULET, ItemID.BINDING_NECKLACE, MagicAction.ENCHANT_EMERALD_JEWELLERY, 27, 37, 2),
    EMERALD_NECKLACE("Emerald necklace", ItemID.EMERALD_NECKLACE, ItemID.BINDING_NECKLACE, MagicAction.ENCHANT_EMERALD_JEWELLERY, 27, 37, 2),
    EMERALD_BRACELET("Emerald bracelet", ItemID.EMERALD_BRACELET, ItemID.CASTLE_WARS_BRACELET3, MagicAction.ENCHANT_EMERALD_JEWELLERY, 27, 37, 2),
    
    // Ruby Jewelry - Level 3 Enchant (Level 49 Magic, 5 Fire Runes + Cosmic Rune)
    RUBY_RING("Ruby ring", ItemID.RUBY_RING, ItemID.RING_OF_FORGING, MagicAction.ENCHANT_RUBY_JEWELLERY, 49, 59, 3),
    RUBY_AMULET("Ruby amulet", ItemID.RUBY_AMULET, ItemID.DIGSITE_PENDANT_5, MagicAction.ENCHANT_RUBY_JEWELLERY, 49, 59, 3),
    RUBY_NECKLACE("Ruby necklace", ItemID.RUBY_NECKLACE, ItemID.DIGSITE_PENDANT_5, MagicAction.ENCHANT_RUBY_JEWELLERY, 49, 59, 3),
    RUBY_BRACELET("Ruby bracelet", ItemID.RUBY_BRACELET, ItemID.INOCULATION_BRACELET, MagicAction.ENCHANT_RUBY_JEWELLERY, 49, 59, 3),
    
    // Diamond Jewelry - Level 4 Enchant (Level 57 Magic, 10 Earth Runes + Cosmic Rune)
    DIAMOND_RING("Diamond ring", ItemID.DIAMOND_RING, ItemID.RING_OF_LIFE, MagicAction.ENCHANT_DIAMOND_JEWELLERY, 57, 67, 4),
    DIAMOND_AMULET("Diamond amulet", ItemID.DIAMOND_AMULET, ItemID.AMULET_OF_GLORY, MagicAction.ENCHANT_DIAMOND_JEWELLERY, 57, 67, 4),
    DIAMOND_NECKLACE("Diamond necklace", ItemID.DIAMOND_NECKLACE, ItemID.PHOENIX_NECKLACE, MagicAction.ENCHANT_DIAMOND_JEWELLERY, 57, 67, 4),
    DIAMOND_BRACELET("Diamond bracelet", ItemID.DIAMOND_BRACELET, ItemID.ABYSSAL_BRACELET5, MagicAction.ENCHANT_DIAMOND_JEWELLERY, 57, 67, 4),
    
    // Dragonstone Jewelry - Level 5 Enchant (Level 68 Magic, 15 Earth + 15 Water Runes + Cosmic Rune)
    DRAGONSTONE_RING("Dragonstone ring", ItemID.DRAGONSTONE_RING, ItemID.RING_OF_WEALTH, MagicAction.ENCHANT_DRAGONSTONE_JEWELLERY, 68, 78, 5),
    DRAGONSTONE_AMULET("Dragonstone amulet", ItemID.DRAGONSTONE_AMULET, ItemID.AMULET_OF_GLORY, MagicAction.ENCHANT_DRAGONSTONE_JEWELLERY, 68, 78, 5),
    DRAGONSTONE_NECKLACE("Dragonstone necklace", ItemID.DRAGON_NECKLACE, ItemID.SKILLS_NECKLACE, MagicAction.ENCHANT_DRAGONSTONE_JEWELLERY, 68, 78, 5),
    DRAGONSTONE_BRACELET("Dragonstone bracelet", ItemID.DRAGONSTONE_BRACELET, ItemID.COMBAT_BRACELET, MagicAction.ENCHANT_DRAGONSTONE_JEWELLERY, 68, 78, 5),
    
    // Onyx Jewelry - Level 6 Enchant (Level 87 Magic, 20 Fire + 20 Earth Runes + Cosmic Rune)
    ONYX_RING("Onyx ring", ItemID.ONYX_RING, ItemID.RING_OF_STONE, MagicAction.ENCHANT_ONYX_JEWELLERY, 87, 97, 6),
    ONYX_AMULET("Onyx amulet", ItemID.ONYX_AMULET, ItemID.AMULET_OF_FURY, MagicAction.ENCHANT_ONYX_JEWELLERY, 87, 97, 6),
    ONYX_NECKLACE("Onyx necklace", ItemID.ONYX_NECKLACE, ItemID.BERSERKER_NECKLACE, MagicAction.ENCHANT_ONYX_JEWELLERY, 87, 97, 6),
    ONYX_BRACELET("Onyx bracelet", ItemID.ONYX_BRACELET, ItemID.REGEN_BRACELET, MagicAction.ENCHANT_ONYX_JEWELLERY, 87, 97, 6);
    
    private final String name;
    private final int unenchantedId;  // The item ID of the unenchanted jewelry (input)
    private final int enchantedId;    // The item ID of the enchanted jewelry (output)
    private final MagicAction enchantSpell;  // The spell used to enchant this jewelry
    private final int magicLevel;     // Magic level required to enchant this jewelry
    private final int enchantXp;      // XP gained from enchanting this jewelry
    private final int enchantLevel;   // The level of enchantment (1-7)
} 