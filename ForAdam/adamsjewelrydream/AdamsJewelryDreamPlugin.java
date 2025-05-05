package net.runelite.client.plugins.microbot.adamsjewelrydream;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

/**
 * AdamsJewelryDream - An automated jewelry enchanting plugin
 * 
 * Features:
 * - Automatically enchants all types of jewelry (sapphire through onyx)
 * - Handles banking, rune management, and staff equipping
 * - Support for rune pouch to save inventory space
 * - Multiple enchanting modes for different play styles
 * - Anti-ban measures to make gameplay more natural
 * 
 * Requirements:
 * - Must be standing near the Grand Exchange bank
 * - Must have unenchanted jewelry in your bank
 * - Must have cosmic runes and required elemental runes in bank or inventory
 * - Must have the magic level required for the selected jewelry type
 * - Optionally, a magic staff in your bank that provides the needed elemental runes
 */
@PluginDescriptor(
        name = "AdamsJewelryDream",
        description = "Automatically enchants jewelry with efficient banking and staff support. Stand at the Grand Exchange with jewelry and runes in your bank to use.",
        tags = {"enchant", "jewelry", "magic", "microbot", "profit"},
        enabledByDefault = false
)
@Slf4j
public class AdamsJewelryDreamPlugin extends Plugin {
    
    @Inject
    @Getter
    private AdamsJewelryDreamConfig config;
    
    @Provides
    AdamsJewelryDreamConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AdamsJewelryDreamConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    
    @Inject
    private AdamsJewelryDreamOverlay enchantOverlay;
    
    @Inject
    private AdamsJewelryDreamScript enchantScript;

    @Getter
    private JewelryType selectedJewelry;
    
    @Getter
    private EnchantMode enchantMode;

    @Override
    protected void startUp() throws AWTException {
        // Initialize config values
        selectedJewelry = config.jewelryType();
        enchantMode = config.enchantMode();
        
        // Add overlay to display script status
        if (overlayManager != null) {
            overlayManager.add(enchantOverlay);
        }
        
        // Start the enchanting script
        enchantScript.run();
    }

    protected void shutDown() {
        // Clean shutdown of the script
        enchantScript.shutdown();
        // Remove overlay
        overlayManager.remove(enchantOverlay);
    }
    
    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        // Only process events for this plugin's config group
        if (!event.getGroup().equals("adamsJewelryDream")) {
            return;
        }
        
        // Update local config values when changed in the UI
        if (event.getKey().equals("jewelryType")) {
            selectedJewelry = config.jewelryType();
        } else if (event.getKey().equals("enchantMode")) {
            enchantMode = config.enchantMode();
        }
    }
} 