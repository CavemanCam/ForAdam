package net.runelite.client.plugins.microbot.adamsjewelrydream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mode settings for jewelry enchanting that affect speed and anti-ban measures.
 * Different modes offer different balances between efficiency and human-like behavior.
 */
@Getter
@RequiredArgsConstructor
public enum EnchantMode {
    /**
     * Fast enchanting with minimal pauses.
     * More efficient but potentially more detectable.
     */
    QUICK("Quick", "Fast enchanting with minimal pauses"),
    
    /**
     * Slower enchanting with natural breaks to simulate human behavior.
     * Less efficient but appears more natural during long sessions.
     */
    AFK("AFK", "Slower enchanting with natural breaks");
    
    private final String name;
    private final String description;
} 