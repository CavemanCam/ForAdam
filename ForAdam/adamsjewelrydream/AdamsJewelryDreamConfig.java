package net.runelite.client.plugins.microbot.adamsjewelrydream;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("adamsJewelryDream")
public interface AdamsJewelryDreamConfig extends Config {
    
    @ConfigSection(
            name = "General",
            description = "Configure general settings for jewelry enchanting",
            position = 0
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "jewelryType",
            name = "Jewelry Type",
            description = "Select the type of jewelry to enchant. Different types require different magic levels and runes.",
            position = 0,
            section = generalSection
    )
    default JewelryType jewelryType() {
        return JewelryType.SAPPHIRE_RING;
    }

    @ConfigItem(
            keyName = "enchantMode",
            name = "Enchant Mode",
            description = "QUICK: Faster enchanting with minimal pauses. AFK: Slower enchanting with natural breaks to appear more human-like.",
            position = 1,
            section = generalSection
    )
    default EnchantMode enchantMode() {
        return EnchantMode.QUICK;
    }
    
    @ConfigItem(
            keyName = "useStaff",
            name = "Use Magic Staff",
            description = "When enabled, the bot will equip a magic staff from your bank that provides the required elemental runes, saving inventory space.",
            position = 2,
            section = generalSection
    )
    default boolean useStaff() {
        return true;
    }
    
    @ConfigItem(
            keyName = "useRunePouch",
            name = "Use Rune Pouch",
            description = "When enabled, the bot will consider runes in your rune pouch when calculating required runes. This saves inventory space for more jewelry.",
            position = 3,
            section = generalSection
    )
    default boolean useRunePouch() {
        return true;
    }
} 