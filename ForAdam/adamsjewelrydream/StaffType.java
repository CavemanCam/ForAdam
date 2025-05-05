package net.runelite.client.plugins.microbot.adamsjewelrydream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing different types of magic staves that can be used for enchanting.
 * Each type provides different elemental runes.
 */
@Getter
@RequiredArgsConstructor
public enum StaffType {
    ANY("Any Available Staff", null),
    
    // Air Staves
    STAFF_OF_AIR("Staff of Air", Arrays.asList(ItemID.STAFF_OF_AIR)),
    AIR_BATTLESTAFF("Air Battlestaff", Arrays.asList(ItemID.AIR_BATTLESTAFF)),
    MYSTIC_AIR_STAFF("Mystic Air Staff", Arrays.asList(ItemID.MYSTIC_AIR_STAFF)),
    
    // Water Staves
    STAFF_OF_WATER("Staff of Water", Arrays.asList(ItemID.STAFF_OF_WATER)),
    WATER_BATTLESTAFF("Water Battlestaff", Arrays.asList(ItemID.WATER_BATTLESTAFF)),
    MYSTIC_WATER_STAFF("Mystic Water Staff", Arrays.asList(ItemID.MYSTIC_WATER_STAFF)),
    
    // Earth Staves
    STAFF_OF_EARTH("Staff of Earth", Arrays.asList(ItemID.STAFF_OF_EARTH)),
    EARTH_BATTLESTAFF("Earth Battlestaff", Arrays.asList(ItemID.EARTH_BATTLESTAFF)),
    MYSTIC_EARTH_STAFF("Mystic Earth Staff", Arrays.asList(ItemID.MYSTIC_EARTH_STAFF)),
    
    // Fire Staves
    STAFF_OF_FIRE("Staff of Fire", Arrays.asList(ItemID.STAFF_OF_FIRE)),
    FIRE_BATTLESTAFF("Fire Battlestaff", Arrays.asList(ItemID.FIRE_BATTLESTAFF)),
    MYSTIC_FIRE_STAFF("Mystic Fire Staff", Arrays.asList(ItemID.MYSTIC_FIRE_STAFF)),
    
    // Combination Staves
    MYSTIC_STEAM_STAFF("Mystic Steam Staff (Water + Fire)", Arrays.asList(ItemID.MYSTIC_STEAM_STAFF)),
    MYSTIC_MIST_STAFF("Mystic Mist Staff (Air + Water)", Arrays.asList(ItemID.MYSTIC_MIST_STAFF)),
    MYSTIC_DUST_STAFF("Mystic Dust Staff (Air + Earth)", Arrays.asList(ItemID.MYSTIC_DUST_STAFF)),
    MYSTIC_MUD_STAFF("Mystic Mud Staff (Water + Earth)", Arrays.asList(ItemID.MYSTIC_MUD_STAFF)),
    MYSTIC_LAVA_STAFF("Mystic Lava Staff (Fire + Earth)", Arrays.asList(ItemID.MYSTIC_LAVA_STAFF)),
    MYSTIC_SMOKE_STAFF("Mystic Smoke Staff (Air + Fire)", Arrays.asList(ItemID.MYSTIC_SMOKE_STAFF)),
    
    // Special Staves
    STAFF_OF_LIGHT("Staff of Light (Air)", Arrays.asList(ItemID.STAFF_OF_LIGHT)),
    KODAI_WAND("Kodai Wand (All Elements)", Arrays.asList(ItemID.KODAI_WAND));
    
    private final String displayName;
    private final List<Integer> staffItemIds;
    
    /**
     * Gets all staff item IDs including those from combination staves.
     *
     * @return List of all staff item IDs.
     */
    public static List<Integer> getAllStaffItemIds() {
        return Arrays.asList(
            ItemID.STAFF_OF_AIR,
            ItemID.AIR_BATTLESTAFF,
            ItemID.MYSTIC_AIR_STAFF,
            ItemID.STAFF_OF_WATER,
            ItemID.WATER_BATTLESTAFF,
            ItemID.MYSTIC_WATER_STAFF,
            ItemID.STAFF_OF_EARTH,
            ItemID.EARTH_BATTLESTAFF,
            ItemID.MYSTIC_EARTH_STAFF,
            ItemID.STAFF_OF_FIRE,
            ItemID.FIRE_BATTLESTAFF,
            ItemID.MYSTIC_FIRE_STAFF,
            ItemID.MYSTIC_STEAM_STAFF,
            ItemID.MYSTIC_MIST_STAFF,
            ItemID.MYSTIC_DUST_STAFF,
            ItemID.MYSTIC_MUD_STAFF,
            ItemID.MYSTIC_LAVA_STAFF,
            ItemID.MYSTIC_SMOKE_STAFF,
            ItemID.STAFF_OF_LIGHT,
            ItemID.KODAI_WAND
        );
    }
} 