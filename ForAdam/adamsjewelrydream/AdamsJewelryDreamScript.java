package net.runelite.client.plugins.microbot.adamsjewelrydream;

import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

@Slf4j
public class AdamsJewelryDreamScript extends Script {

    private final AdamsJewelryDreamPlugin plugin;
    private JewelryType selectedJewelry;
    private EnchantMode enchantMode;
    private boolean useStaff;
    private boolean useRunePouch;
    private StaffType selectedStaffType;
    
    // Maximum number of consecutive errors allowed before script resets
    private static final int MAX_ERROR_COUNT = 5;
    
    private enum State {
        BANKING,
        ENCHANTING,
        FINISHED
    }
    
    private State currentState;
    private List<Integer> staffItemIds;
    private int totalJewelryProcessed = 0;
    private int errorCount = 0;
    private long lastStateChangeTime = System.currentTimeMillis();
    
    @Inject
    public AdamsJewelryDreamScript(AdamsJewelryDreamPlugin plugin) {
        this.plugin = plugin;
        this.staffItemIds = new ArrayList<>();
        // Common staff IDs that may provide rune savings for enchantment spells
        staffItemIds.add(ItemID.STAFF_OF_AIR);
        staffItemIds.add(ItemID.AIR_BATTLESTAFF);
        staffItemIds.add(ItemID.MYSTIC_AIR_STAFF);
        staffItemIds.add(ItemID.STAFF_OF_WATER);
        staffItemIds.add(ItemID.WATER_BATTLESTAFF);
        staffItemIds.add(ItemID.MYSTIC_WATER_STAFF);
        staffItemIds.add(ItemID.STAFF_OF_EARTH);
        staffItemIds.add(ItemID.EARTH_BATTLESTAFF);
        staffItemIds.add(ItemID.MYSTIC_EARTH_STAFF);
        staffItemIds.add(ItemID.STAFF_OF_FIRE);
        staffItemIds.add(ItemID.FIRE_BATTLESTAFF);
        staffItemIds.add(ItemID.MYSTIC_FIRE_STAFF);
        staffItemIds.add(ItemID.MYSTIC_STEAM_STAFF);
        staffItemIds.add(ItemID.MYSTIC_MIST_STAFF);
        staffItemIds.add(ItemID.MYSTIC_DUST_STAFF);
        staffItemIds.add(ItemID.MYSTIC_MUD_STAFF);
        staffItemIds.add(ItemID.MYSTIC_LAVA_STAFF);
        staffItemIds.add(ItemID.MYSTIC_SMOKE_STAFF);
        staffItemIds.add(ItemID.STAFF_OF_LIGHT);
        staffItemIds.add(ItemID.KODAI_WAND);
    }
    
    @Override
    public boolean run() {
        if (plugin == null) {
            log.error("Plugin reference is null!");
            return false;
        }
        
        AdamsJewelryDreamConfig config = plugin.getConfig();
        if (config == null) {
            log.error("Config is null!");
            return false;
        }
        
        return run(config);
    }

    public boolean run(AdamsJewelryDreamConfig config) {
        if (config == null) {
            log.error("Config is null in run method!");
            return false;
        }
        
        Microbot.enableAutoRunOn = false;
        this.selectedJewelry = config.jewelryType();
        this.enchantMode = config.enchantMode();
        this.useStaff = config.useStaff();
        this.useRunePouch = config.useRunePouch();
        this.selectedStaffType = config.staffType();
        
        // Log initial config values
        log.info("Starting AdamsJewelryDream script with config:");
        log.info("  Jewelry: {}", this.selectedJewelry);
        log.info("  Mode: {}", this.enchantMode);
        log.info("  Use Staff: {}", this.useStaff);
        log.info("  Staff Type: {}", this.selectedStaffType);
        log.info("  Use Rune Pouch: {}", this.useRunePouch);

        if (this.selectedJewelry == null) {
            log.error("Selected Jewelry Type is null! Please check configuration.");
            Microbot.showMessage("Error: Selected Jewelry Type is null in config.");
            return false; // Stop the script
        }
        
        // Verify player is ready to run the script
        if (!performSetupCheck()) {
            return false;
        }
        
        // Setup spellbook filters to ensure all spells are visible
        setupSpellbookFilters();
        
        // Setup anti-ban measures
        Rs2Antiban.resetAntibanSettings();
        Rs2AntibanSettings.dynamicActivity = true;
        
        currentState = State.BANKING;
        errorCount = 0;
        lastStateChangeTime = System.currentTimeMillis();
        
        if (mainScheduledFuture != null && !mainScheduledFuture.isDone()) {
            mainScheduledFuture.cancel(true);
        }
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                
                // Save a reference to the current jewelry to avoid any race conditions
                final JewelryType currentJewelry = this.selectedJewelry;
                
                // Extra safety check for null config or jewelry
                if (currentJewelry == null) {
                    log.error("selectedJewelry is null inside the main loop!");
                    Microbot.status = "Error: Config issue";
                    errorCount++;
                    return;
                }
                
                // Check for too many consecutive errors and restart if needed
                if (errorCount >= MAX_ERROR_COUNT) {
                    log.warn("Too many errors ({}), resetting script state", errorCount);
                    Microbot.status = "Recovering from errors...";
                    errorCount = 0;
                    currentState = State.BANKING;
                    // Close bank if open to reset state
                    if (Rs2Bank.isOpen()) {
                        Rs2Bank.closeBank();
                    }
                    return;
                }
                
                // Check for being stuck in same state too long (3 minutes)
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastStateChangeTime > 180000) {
                    log.warn("Stuck in state {} for too long, resetting", currentState);
                    Microbot.status = "Resetting from stuck state";
                    currentState = State.BANKING;
                    lastStateChangeTime = currentTime;
                    // Close bank if open to reset state
                    if (Rs2Bank.isOpen()) {
                        Rs2Bank.closeBank();
                    }
                    return;
                }
                
                if (Rs2Player.isAnimating() || Rs2Player.isMoving() || 
                    (Rs2Antiban.getCategory() != null && Rs2Antiban.getCategory().isBusy()) || 
                    Microbot.pauseAllScripts) return;
                    
                if (Rs2AntibanSettings.actionCooldownActive) return;
                
                // Check if we have the required magic level
                if (Microbot.getClient() != null && currentJewelry != null &&
                    Microbot.getClient().getRealSkillLevel(Skill.MAGIC) < currentJewelry.getMagicLevel()) {
                    Microbot.showMessage("You need at least level " + currentJewelry.getMagicLevel() + " Magic to enchant " + currentJewelry.getName());
                    shutdown();
                    return;
                }

                // Update state safely
                State newState = updateState();
                if (newState != currentState) {
                    lastStateChangeTime = System.currentTimeMillis();
                }
                currentState = newState;
                
                switch (currentState) {
                    case BANKING:
                        handleBanking();
                        break;
                    case ENCHANTING:
                        handleEnchanting();
                        break;
                    case FINISHED:
                        Microbot.showMessage("Finished enchanting all available jewelry. Total processed: " + totalJewelryProcessed);
                        shutdown();
                        break;
                }
                
                // Reset error count on successful execution
                errorCount = 0;
                
            } catch (Exception ex) {
                log.error("Error in AdamsJewelryDreamScript: {}", ex.getMessage(), ex);
                errorCount++;
            }
        }, 0, enchantMode == EnchantMode.AFK ? 1000 : 600, TimeUnit.MILLISECONDS);
        
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        Microbot.status = "Plugin stopped";
    }
    
    private State updateState() {
        try {
            // Add null check before accessing selectedJewelry
            final JewelryType jewelry = this.selectedJewelry;
            if (jewelry == null) {
                log.error("selectedJewelry is null in updateState!");
                return State.FINISHED; // Treat as finished if config is broken
            }

            // Check if we already have enchanted jewelry in inventory
            if (Rs2Inventory.hasItem(jewelry.getEnchantedId())) {
                // We have some enchanted jewelry, may need to check bank
                return State.BANKING;
            }

            // If we have jewelry to enchant and the required runes, we're in enchanting state
            if (Rs2Inventory.hasItem(jewelry.getUnenchantedId()) && hasRequiredRunes()) {
                return State.ENCHANTING;
            }
            
            // Check if we're in bank and there's no more jewelry
            if (Rs2Bank.isOpen()) {
                // Scan bank for jewelry
                if (!Rs2Bank.hasItem(jewelry.getUnenchantedId())) {
                    return State.FINISHED;
                }
            }
            
            // Default to banking state
            return State.BANKING;
        } catch (Exception e) {
            log.error("Error in updateState: {}", e.getMessage(), e);
            errorCount++;
            return State.BANKING; // Default to banking as safer option
        }
    }
    
    private void handleBanking() {
        Microbot.status = "Banking";
        
        try {
            final JewelryType jewelry = this.selectedJewelry;
            if (jewelry == null) {
                log.error("selectedJewelry is null in handleBanking!");
                errorCount++;
                return;
            }
            
            // First, check if we already have what we need without banking
            if (hasRequiredRunesAndJewelry()) {
                currentState = State.ENCHANTING;
                return;
            }
            
            // Open bank if not already open
            if (!Rs2Bank.isOpen()) {
                if (!Rs2Bank.isNearBank(BankLocation.GRAND_EXCHANGE, 15)) {
                    Rs2Bank.walkToBank(BankLocation.GRAND_EXCHANGE);
                    Global.sleepUntil(() -> Rs2Bank.isNearBank(BankLocation.GRAND_EXCHANGE, 15), 10000);
                    return;
                }
                
                if (!Rs2Bank.useBank()) {
                    return;
                }
                
                Global.sleepUntil(Rs2Bank::isOpen, 5000);
                return;
            }
            
            // Check for enchanted jewelry in inventory and deposit it
            if (Rs2Inventory.hasItem(jewelry.getEnchantedId())) {
                Rs2Bank.depositAll(jewelry.getEnchantedId());
                // Reduced wait time for inventory changes
                Global.sleepUntil(() -> !Rs2Inventory.hasItem(jewelry.getEnchantedId()), 1200);
                return;
            }
            
            // Deposit everything except cosmic runes and staff (if equipped)
            if (!Rs2Inventory.isEmpty()) {
                // Keep cosmic runes
                Rs2Bank.depositAllExcept(ItemID.COSMIC_RUNE);
                // Reduced wait time
                Global.sleepUntil(() -> Rs2Inventory.isEmpty() || Rs2Inventory.hasItem(ItemID.COSMIC_RUNE), 1200);
            }
            
            // Equip staff if needed
            if (useStaff) {
                boolean hasStaff = false;
                
                // Check if already equipped with appropriate staff
                if (selectedStaffType != StaffType.ANY) {
                    // User selected a specific staff
                    List<Integer> selectedStaffIds = selectedStaffType.getStaffItemIds();
                    if (selectedStaffIds != null) {
                        for (int staffId : selectedStaffIds) {
                            if (Rs2Equipment.isWearing(staffId)) {
                                hasStaff = true;
                                break;
                            }
                        }
                        
                        // If not wearing the selected staff, try to get it from bank
                        if (!hasStaff) {
                            for (int staffId : selectedStaffIds) {
                                if (Rs2Bank.hasItem(staffId)) {
                                    Rs2Bank.withdrawOne(staffId);
                                    Global.sleepUntil(() -> Rs2Inventory.hasItem(staffId), 800);
                                    Rs2Inventory.interact(staffId, "Wield");
                                    Global.sleepUntil(() -> Rs2Equipment.isWearing(staffId), 800);
                                    hasStaff = true;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // Check if wearing any staff
                    for (int staffId : staffItemIds) {
                        if (Rs2Equipment.isWearing(staffId)) {
                            hasStaff = true;
                            break;
                        }
                    }
                    
                    // If not wearing any staff, get the first available one
                    if (!hasStaff) {
                        for (int staffId : staffItemIds) {
                            if (Rs2Bank.hasItem(staffId)) {
                                Rs2Bank.withdrawOne(staffId);
                                Global.sleepUntil(() -> Rs2Inventory.hasItem(staffId), 800);
                                Rs2Inventory.interact(staffId, "Wield");
                                Global.sleepUntil(() -> Rs2Equipment.isWearing(staffId), 800);
                                hasStaff = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            // Optimization: Gather all items needed to withdraw before starting
            Map<Integer, Integer> itemsToWithdraw = new HashMap<>();
            
            // Get required runes
            Map<Integer, Integer> requiredRunes = getRequiredRunes();
            if (requiredRunes == null) {
                log.error("Required runes map is null!");
                errorCount++;
                return;
            }
            
            // Add required runes to withdrawal list
            for (Map.Entry<Integer, Integer> entry : requiredRunes.entrySet()) {
                int runeId = entry.getKey();
                int requiredAmount = entry.getValue();
                
                // Skip runes that would be provided by equipped staff
                if (isRuneProvidedByStaff(runeId)) {
                    continue;
                }
                
                // Calculate how many we need to withdraw
                if (!Rs2Inventory.hasItemAmount(runeId, requiredAmount)) {
                    if (!Rs2Bank.hasItem(runeId)) {
                        Microbot.showMessage("Missing required rune: " + runeId);
                        shutdown();
                        return;
                    }
                    
                    int amountToWithdraw = requiredAmount - Rs2Inventory.count(runeId);
                    if (amountToWithdraw > 0) {
                        itemsToWithdraw.put(runeId, amountToWithdraw);
                    }
                }
            }
            
            // Calculate free slots for jewelry
            int freeSlots = 28 - Rs2Inventory.count(0);
            int jewelrySlots = freeSlots - itemsToWithdraw.size();
            
            // Check if we have jewelry to withdraw
            if (jewelrySlots > 0 && Rs2Bank.hasItem(jewelry.getUnenchantedId())) {
                itemsToWithdraw.put(jewelry.getUnenchantedId(), jewelrySlots);
            }
            
            // Now withdraw all items quickly with minimal delays
            boolean madeChanges = false;
            for (Map.Entry<Integer, Integer> entry : itemsToWithdraw.entrySet()) {
                Rs2Bank.withdrawX(entry.getKey(), entry.getValue());
                madeChanges = true;
                // Brief pause between withdrawals
                Global.sleep(150, 250);
            }
            
            // Wait for all inventory changes to complete, but only once after all withdrawals
            if (madeChanges) {
                Global.sleepUntil(() -> {
                    // Check if we have the jewelry
                    boolean hasJewelry = !itemsToWithdraw.containsKey(jewelry.getUnenchantedId()) || 
                                        Rs2Inventory.hasItem(jewelry.getUnenchantedId());
                    
                    // Check if we have all the runes
                    boolean hasAllRunes = true;
                    for (Map.Entry<Integer, Integer> entry : requiredRunes.entrySet()) {
                        int runeId = entry.getKey();
                        int requiredAmount = entry.getValue();
                        
                        if (!isRuneProvidedByStaff(runeId) && !Rs2Inventory.hasItemAmount(runeId, requiredAmount)) {
                            hasAllRunes = false;
                            break;
                        }
                    }
                    
                    return hasJewelry && hasAllRunes;
                }, 1500);
            }
            
            Rs2Bank.closeBank();
            Global.sleepUntil(() -> !Rs2Bank.isOpen(), 1500);
        } catch (Exception e) {
            log.error("Error in handleBanking: {}", e.getMessage(), e);
            errorCount++;
        }
    }
    
    private void handleEnchanting() {
        try {
            // Add null check before accessing selectedJewelry
            final JewelryType jewelry = this.selectedJewelry;
            if (jewelry == null) {
                log.error("selectedJewelry is null in handleEnchanting!");
                errorCount++;
                currentState = State.BANKING; // Try to recover by banking
                return;
            }

            Microbot.status = "Enchanting " + jewelry.getName();
            
            int jewelryCount = Rs2Inventory.count(jewelry.getUnenchantedId());
            if (jewelryCount == 0) {
                currentState = State.BANKING;
                return;
            }
            
            // Check if we have the right runes
            if (!hasRequiredRunes()) {
                log.info("Missing required runes for enchanting");
                currentState = State.BANKING;
                return;
            }
            
            // First ensure we're on the magic tab
            Rs2Tab.switchToMagicTab();
            Global.sleep(300, 500);
            
            // Get enchant spell name and unenchanted jewelry ID
            MagicAction enchantSpell = jewelry.getEnchantSpell();
            int unenchantedId = jewelry.getUnenchantedId();
            
            if (enchantSpell == null) {
                log.error("Enchant spell is null for selected jewelry: {}", jewelry.getName());
                errorCount++;
                shutdown();
                return;
            }
            
            String spellName = enchantSpell.getName();
            
            // Check for the "Jewellery Enchantments" button
            Widget viewButton = Rs2Widget.findWidget("Jewellery Enchantments");
            if (viewButton != null) {
                // Click the "View" button to open the Jewellery Enchantments submenu
                Microbot.click(viewButton.getBounds());
                Global.sleep(300, 500);
            } else {
                // If we can't find the "View" button, look for the submenu directly
                Widget enchantButton = Rs2Widget.findWidget("Enchant");
                if (enchantButton != null) {
                    Microbot.click(enchantButton.getBounds());
                    Global.sleep(300, 500);
                }
            }
            
            // Now find and click the specific enchant spell
            Widget enchantWidget = Rs2Widget.findWidget(spellName);
            if (enchantWidget == null) {
                // Try alternative approaches to find the spell
                log.info("Trying alternative method to find spell: {}", spellName);
                
                // Check for direct widget ID from logs - widget target param1=14286861
                Widget spellWidget = Rs2Widget.getWidget(218, 21);
                if (spellWidget != null) {
                    Microbot.click(spellWidget.getBounds());
                    Global.sleep(300, 500);
                    
                    // Now click on the jewelry with Cast option
                    Rs2Inventory.interact(unenchantedId, "Cast");
                    
                    // Wait until animation starts
                    Global.sleepUntil(Rs2Player::isAnimating, 2000);
                    
                    // Wait until animation completes and jewelry is enchanted
                    Global.sleepUntil(() -> Rs2Inventory.count(unenchantedId) < jewelryCount, 5000);
                    
                    totalJewelryProcessed++;
                    return;
                } else {
                    log.error("Could not find widget for spell: {}", spellName);
                    errorCount++;
                    return;
                }
            }
            
            // If we found the spell widget directly, click it
            Microbot.click(enchantWidget.getBounds());
            Global.sleep(300, 500);
            
            // Click on the jewelry item
            Rs2Inventory.interact(unenchantedId, "Cast");
            
            // Wait until animation starts
            Global.sleepUntil(Rs2Player::isAnimating, 2000);
            
            // Wait until animation completes and jewelry is enchanted
            Global.sleepUntil(() -> Rs2Inventory.count(unenchantedId) < jewelryCount, 5000);
            
            totalJewelryProcessed++;
            
            // Apply anti-ban measures based on mode
            if (enchantMode == EnchantMode.AFK) {
                Rs2Antiban.actionCooldown();
                Rs2Antiban.takeMicroBreakByChance();
            } else {
                Rs2Antiban.actionCooldown();
            }
        } catch (Exception e) {
            log.error("Error in handleEnchanting: {}", e.getMessage(), e);
            errorCount++;
        }
    }
    
    private boolean hasRequiredRunes() {
        try {
            Map<Integer, Integer> requiredRunes = getRequiredRunes();
            if (requiredRunes == null) {
                log.error("Required runes map is null in hasRequiredRunes!");
                return false;
            }

            for (Map.Entry<Integer, Integer> entry : requiredRunes.entrySet()) {
                int runeId = entry.getKey();
                int requiredAmount = entry.getValue();
                
                // Skip check for runes provided by staff
                if (isRuneProvidedByStaff(runeId)) {
                    continue;
                }
                
                if (!Rs2Inventory.hasItemAmount(runeId, requiredAmount)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error in hasRequiredRunes: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean hasRequiredRunesAndJewelry() {
        try {
            final JewelryType jewelry = this.selectedJewelry;
            if (jewelry == null) {
                return false;
            }
            
            // Check if we have jewelry to enchant
            if (!Rs2Inventory.hasItem(jewelry.getUnenchantedId())) {
                return false;
            }
            
            // Check if we have required runes
            return hasRequiredRunes();
        } catch (Exception e) {
            log.error("Error in hasRequiredRunesAndJewelry: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private Map<Integer, Integer> getRequiredRunes() {
        // Add null check before accessing selectedJewelry
        final JewelryType jewelry = this.selectedJewelry;
        if (jewelry == null) {
            log.error("selectedJewelry is null in getRequiredRunes!");
            return null; // Indicate an error state
        }

        Map<Integer, Integer> requiredRunes = new HashMap<>();
        
        // Always need cosmic runes for enchanting
        requiredRunes.put(ItemID.COSMIC_RUNE, 1);
        
        // Add elemental runes based on jewelry type
        switch (jewelry.getEnchantLevel()) {
            case 1: // Sapphire - Water rune
                requiredRunes.put(ItemID.WATER_RUNE, 1);
                break;
            case 2: // Emerald - Air rune
                requiredRunes.put(ItemID.AIR_RUNE, 3);
                break;
            case 3: // Ruby - Fire rune
                requiredRunes.put(ItemID.FIRE_RUNE, 5);
                break;
            case 4: // Diamond - Earth rune
                requiredRunes.put(ItemID.EARTH_RUNE, 10);
                break;
            case 5: // Dragonstone - Water + Earth runes
                requiredRunes.put(ItemID.WATER_RUNE, 15);
                requiredRunes.put(ItemID.EARTH_RUNE, 15);
                break;
            case 6: // Onyx - Fire + Earth runes
                requiredRunes.put(ItemID.FIRE_RUNE, 20);
                requiredRunes.put(ItemID.EARTH_RUNE, 20);
                break;
            case 7: // Zenyte - Blood + Soul runes
                requiredRunes.put(ItemID.BLOOD_RUNE, 20);
                requiredRunes.put(ItemID.SOUL_RUNE, 20);
                break;
        }
        
        return requiredRunes;
    }
    
    private boolean isRuneProvidedByStaff(int runeId) {
        if (!useStaff) {
            return false;
        }
        
        // Check if player is wearing a staff that provides this rune
        switch (runeId) {
            case ItemID.AIR_RUNE:
                return Rs2Equipment.isWearing(ItemID.STAFF_OF_AIR) || 
                       Rs2Equipment.isWearing(ItemID.AIR_BATTLESTAFF) || 
                       Rs2Equipment.isWearing(ItemID.MYSTIC_AIR_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_DUST_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_SMOKE_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_MIST_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.STAFF_OF_LIGHT) ||
                       Rs2Equipment.isWearing(ItemID.KODAI_WAND);
            case ItemID.WATER_RUNE:
                return Rs2Equipment.isWearing(ItemID.STAFF_OF_WATER) || 
                       Rs2Equipment.isWearing(ItemID.WATER_BATTLESTAFF) || 
                       Rs2Equipment.isWearing(ItemID.MYSTIC_WATER_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_STEAM_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_MUD_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_MIST_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.KODAI_WAND);
            case ItemID.EARTH_RUNE:
                return Rs2Equipment.isWearing(ItemID.STAFF_OF_EARTH) || 
                       Rs2Equipment.isWearing(ItemID.EARTH_BATTLESTAFF) || 
                       Rs2Equipment.isWearing(ItemID.MYSTIC_EARTH_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_DUST_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_MUD_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_LAVA_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.KODAI_WAND);
            case ItemID.FIRE_RUNE:
                return Rs2Equipment.isWearing(ItemID.STAFF_OF_FIRE) || 
                       Rs2Equipment.isWearing(ItemID.FIRE_BATTLESTAFF) || 
                       Rs2Equipment.isWearing(ItemID.MYSTIC_FIRE_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_STEAM_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_SMOKE_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.MYSTIC_LAVA_STAFF) ||
                       Rs2Equipment.isWearing(ItemID.KODAI_WAND);
            default:
                return false;
        }
    }
    
    /**
     * Sets up spellbook filters to ensure all spells are visible
     */
    private void setupSpellbookFilters() {
        if (!Microbot.isLoggedIn()) return;
        
        try {
            // Switch to magic tab
            Rs2Tab.switchToMagicTab();
            Global.sleep(300, 500);
            
            // First, check if we have access to Magic Filters button
            Widget filtersWidget = Rs2Widget.findWidget("Filters");
            if (filtersWidget != null) {
                // Click the filters button
                Microbot.click(filtersWidget.getBounds());
                Global.sleep(600, 800);
                
                // Try to ensure all three spell types are enabled
                Widget combatWidget = Rs2Widget.findWidget("Show Combat spells");
                if (combatWidget != null) {
                    // Check if the widget is selected
                    boolean isSelected = combatWidget.getSpriteId() == 299; // Selected sprite
                    if (!isSelected) {
                        Microbot.click(combatWidget.getBounds());
                        Global.sleep(200, 300);
                    }
                }
                
                Widget teleportWidget = Rs2Widget.findWidget("Show Teleport spells");
                if (teleportWidget != null) {
                    boolean isSelected = teleportWidget.getSpriteId() == 299; // Selected sprite
                    if (!isSelected) {
                        Microbot.click(teleportWidget.getBounds());
                        Global.sleep(200, 300);
                    }
                }
                
                Widget utilityWidget = Rs2Widget.findWidget("Show Utility spells");
                if (utilityWidget != null) {
                    boolean isSelected = utilityWidget.getSpriteId() == 299; // Selected sprite
                    if (!isSelected) {
                        Microbot.click(utilityWidget.getBounds());
                        Global.sleep(200, 300);
                    }
                }
                
                // Go back to main spellbook
                Microbot.click(filtersWidget.getBounds());
                Global.sleep(300, 500);
            } else {
                // If we can't find the filters button, log this but continue
                log.info("Filters button not found in spellbook. Continuing without filter setup.");
            }
            
            // Double check if we're on the main spellbook view, not filter view
            if (Rs2Widget.hasWidget("Jewellery Enchantments")) {
                log.info("Successfully identified Jewellery Enchantments in spellbook");
            } else if (Rs2Widget.hasWidget("Enchant")) {
                log.info("Successfully identified Enchant button in spellbook");
            } else {
                log.warn("Could not find Enchant or Jewellery Enchantments in spellbook");
            }
            
        } catch (Exception e) {
            log.error("Error setting up spellbook filters: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Performs initial checks to ensure the player is ready to run the script
     * @return true if all checks pass, false otherwise
     */
    private boolean performSetupCheck() {
        try {
            if (!Microbot.isLoggedIn()) {
                Microbot.status = "Please log in first";
                return false;
            }
            
            // Safety check for the thread issue reported in logs
            if (SwingUtilities.isEventDispatchThread()) {
                log.warn("Setup check running on Event Dispatch Thread, deferring checks");
                // We shouldn't show a message here as it could cause the AWT invokeAndWait error
                Microbot.status = "Setup deferred, please wait...";
                return true; // Allow starting but checks will run in main loop
            }
            
            // Get the jewelry type safely
            final JewelryType jewelry = this.selectedJewelry;
            if (jewelry == null) {
                Microbot.status = "Invalid jewelry configuration";
                return false;
            }
            
            // Check magic level
            if (Microbot.getClient().getRealSkillLevel(Skill.MAGIC) < jewelry.getMagicLevel()) {
                Microbot.status = "Magic level too low";
                return false;
            }
            
            // Check if near bank
            if (!Rs2Bank.isNearBank(BankLocation.GRAND_EXCHANGE, 15)) {
                Microbot.status = "Please go to GE bank";
                return false;
            }
            
            // Check if we already have what we need in inventory
            if (Rs2Inventory.hasItem(jewelry.getUnenchantedId()) && hasRequiredRunes()) {
                Microbot.status = "Ready to enchant";
                return true;
            }
            
            // Check for enchanted jewelry that needs depositing
            if (Rs2Inventory.hasItem(jewelry.getEnchantedId())) {
                Microbot.status = "Will deposit enchanted jewelry";
                return true;
            }
            
            // If we get here, we need to check bank contents
            boolean bankOpened = false;
            if (!Rs2Bank.isOpen()) {
                bankOpened = Rs2Bank.useBank();
                if (!bankOpened) {
                    Microbot.status = "Could not open bank";
                    return false;
                }
                Global.sleepUntil(Rs2Bank::isOpen, 5000);
                if (!Rs2Bank.isOpen()) {
                    Microbot.status = "Bank failed to open";
                    return false;
                }
            }
            
            // Check if player has required jewelry in bank
            if (!Rs2Bank.hasItem(jewelry.getUnenchantedId())) {
                Microbot.status = "No jewelry in bank";
                if (bankOpened) Rs2Bank.closeBank();
                return false;
            }
            
            // Check for cosmic runes
            if (!Rs2Bank.hasItem(ItemID.COSMIC_RUNE) && !Rs2Inventory.hasItem(ItemID.COSMIC_RUNE)) {
                Microbot.status = "No cosmic runes";
                if (bankOpened) Rs2Bank.closeBank();
                return false;
            }
            
            // Check for required elemental runes
            Map<Integer, Integer> requiredRunes = getRequiredRunes();
            if (requiredRunes != null) {
                boolean hasMissingRunes = false;
                for (Map.Entry<Integer, Integer> entry : requiredRunes.entrySet()) {
                    int runeId = entry.getKey();
                    // Skip cosmic rune (already checked)
                    if (runeId == ItemID.COSMIC_RUNE) continue;
                    
                    // Skip runes provided by staff if using staff
                    if (useStaff && isRuneProvidedByStaff(runeId)) {
                        // Check if player has staff in bank
                        boolean hasStaffInBank = false;
                        for (int staffId : staffItemIds) {
                            if (Rs2Equipment.isWearing(staffId) || Rs2Bank.hasItem(staffId)) {
                                hasStaffInBank = true;
                                break;
                            }
                        }
                        
                        if (hasStaffInBank) {
                            continue; // Staff will provide this rune
                        }
                    }
                    
                    // If player doesn't have enough in inventory and none in bank
                    if (!Rs2Inventory.hasItemAmount(runeId, entry.getValue()) && !Rs2Bank.hasItem(runeId)) {
                        Microbot.status = "Missing rune: " + runeId;
                        hasMissingRunes = true;
                        break;
                    }
                }
                
                if (hasMissingRunes) {
                    if (bankOpened) Rs2Bank.closeBank();
                    return false;
                }
            }
            
            // Close bank if we opened it
            if (bankOpened) {
                Rs2Bank.closeBank();
                Global.sleepUntil(() -> !Rs2Bank.isOpen(), 3000);
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error in performSetupCheck: {}", e.getMessage(), e);
            Microbot.status = "Setup check error";
            return false;
        }
    }
} 