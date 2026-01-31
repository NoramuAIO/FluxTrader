package noramu.fluxtrader.gui;

import noramu.fluxtrader.FluxTrader;
import noramu.fluxtrader.trader.TraderItem;
import noramu.fluxtrader.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GUIManager {
    private final FluxTrader plugin;
    private FileConfiguration inventoryConfig;
    private FileConfiguration itemsConfig;
    private Map<String, TraderItem> itemsCache;

    public GUIManager(FluxTrader plugin) {
        this.plugin = plugin;
        this.itemsCache = new HashMap<>();
        loadConfigs();
    }

    private void loadConfigs() {
        // Inventory config yükle
        File inventoryFile = new File(plugin.getDataFolder(), "inventory.yml");
        if (!inventoryFile.exists()) {
            plugin.saveResource("inventory.yml", false);
        }
        inventoryConfig = YamlConfiguration.loadConfiguration(inventoryFile);

        // Items config yükle
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        
        loadItemsCache();
    }

    private void loadItemsCache() {
        itemsCache.clear();
        ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("items");
        
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                Map<String, Object> itemData = itemsSection.getConfigurationSection(key).getValues(false);
                TraderItem item = new TraderItem(itemData);
                itemsCache.put(item.getId(), item);
            }
        }
    }

    public void reload() {
        loadConfigs();
        // Clear cache to force reload
        itemsCache.clear();
        loadItemsCache();
    }

    public void updateRefreshIndicator(Inventory inventory) {
        int slot = inventoryConfig.getInt("info-panel.refresh-indicator.slot", 10);
        if (slot < inventory.getSize()) {
            ItemStack refreshItem = createRefreshIndicator();
            inventory.setItem(slot, refreshItem);
        }
    }

    public Inventory createInventory(Player player) {
        String title = inventoryConfig.getString("inventory.title", "§6Gezgin Tüccar");
        // MiniMessage desteği
        title = ColorUtil.toLegacy(title);
        
        int size = inventoryConfig.getInt("inventory.size", 54);
        
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Arka plan öğelerini ekle
        addBackgroundItems(inventory);
        
        // Ürünleri ekle
        addItems(inventory);
        
        // Info panelini ekle
        addInfoPanel(inventory, player);
        
        return inventory;
    }

    private void addBackgroundItems(Inventory inventory) {
        if (!inventoryConfig.getBoolean("background.enabled", true)) {
            return;
        }
        
        String material = inventoryConfig.getString("background.material", "GRAY_STAINED_GLASS_PANE");
        String displayName = inventoryConfig.getString("background.display-name", " ");
        List<Integer> slots = inventoryConfig.getIntegerList("background.slots");
        
        // Renk desteği
        displayName = ColorUtil.toLegacy(displayName);
        
        ItemStack bgItem = createItem(Material.valueOf(material), displayName, new ArrayList<>());
        
        for (int slot : slots) {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, bgItem);
            }
        }
    }

    private void addItems(Inventory inventory) {
        ConfigurationSection itemsSection = inventoryConfig.getConfigurationSection("items");
        
        if (itemsSection == null) {
            return;
        }
        
        for (String slotKey : itemsSection.getKeys(false)) {
            ConfigurationSection slotConfig = itemsSection.getConfigurationSection(slotKey);
            int slot = Integer.parseInt(slotKey.replace("slot_", ""));
            String itemId = slotConfig.getString("item-id");
            
            if (itemsCache.containsKey(itemId)) {
                TraderItem traderItem = itemsCache.get(itemId);
                
                // Zaman kontrolü
                if (!traderItem.isAvailable()) {
                    continue;
                }
                
                ItemStack itemStack = createTraderItemStack(traderItem);
                
                if (slot < inventory.getSize()) {
                    inventory.setItem(slot, itemStack);
                }
            }
        }
    }

    private ItemStack createTraderItemStack(TraderItem item) {
        ItemStack stack = new ItemStack(item.getMaterial(), 1);
        ItemMeta meta = stack.getItemMeta();
        
        if (meta != null) {
            // Display name - MiniMessage desteği
            String displayName = item.getDisplayName();
            displayName = ColorUtil.toLegacy(displayName);
            meta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            
            // Temel bilgiler
            lore.add(ColorUtil.toLegacy("§7━━━━━━━━━━━━━━━━━━━━━━"));
            
            // Açıklama
            if (!item.getLore().isEmpty()) {
                String loreLine = item.getLore();
                loreLine = ColorUtil.toLegacy(loreLine);
                lore.add(loreLine);
            }
            
            lore.add(" ");
            
            // Fiyat - with small caps currency display
            String currencyDisplay = getCurrencyDisplay(item.getCurrencyType());
            lore.add(ColorUtil.toLegacy("§6💰 fiyat: §e" + (int)item.getPrice() + " " + currencyDisplay));
            
            // Miktar
            lore.add(ColorUtil.toLegacy("§6📦 miktar: §e" + item.getMinAmount() + "-" + item.getMaxAmount()));
            
            // Çıkma ihtimali
            lore.add(ColorUtil.toLegacy("§6🎲 sans: §e%" + (int)item.getChance()));
            
            // Günlük limit - dinamik olarak güncellenir
            int dailyUsed = plugin.getDataManager().getDailyStockUsed(item.getId());
            int remaining = Math.max(0, item.getDailyLimit() - dailyUsed);
            lore.add(ColorUtil.toLegacy("§6📊 gunluk limit: §e" + remaining + "/" + item.getDailyLimit()));
            
            lore.add(ColorUtil.toLegacy("§7━━━━━━━━━━━━━━━━━━━━━━"));
            
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        
        return stack;
    }

    private String getCurrencyDisplay(String currencyType) {
        switch (currencyType.toUpperCase()) {
            case "GOLD":
                return "gold";
            case "DIAMOND":
                return "diamond";
            case "EMERALD":
                return "emerald";
            case "VAULT":
            default:
                return "vault";
        }
    }

    private void addInfoPanel(Inventory inventory, Player player) {
        if (!inventoryConfig.getBoolean("info-panel.enabled", true)) {
            return;
        }
        
        // Refresh Indicator
        ConfigurationSection refreshConfig = inventoryConfig.getConfigurationSection("info-panel.refresh-indicator");
        if (refreshConfig != null) {
            int slot = refreshConfig.getInt("slot", 37);
            ItemStack refreshItem = createRefreshIndicator();
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, refreshItem);
            }
        }
        
        // Profile Button - Oyuncunun başı
        ConfigurationSection profileConfig = inventoryConfig.getConfigurationSection("info-panel.profile-button");
        if (profileConfig != null) {
            int slot = profileConfig.getInt("slot", 22);
            String displayName = profileConfig.getString("display-name", "§6👤 ᴘʀᴏꜰɪʟ");
            
            // Renk desteği
            displayName = ColorUtil.toLegacy(displayName);
            
            // Oyuncunun para bilgilerini al
            org.bukkit.entity.Player p = player;
            int goldAmount = countItemInInventory(p, org.bukkit.Material.GOLD_INGOT);
            int diamondAmount = countItemInInventory(p, org.bukkit.Material.DIAMOND);
            int emeraldAmount = countItemInInventory(p, org.bukkit.Material.EMERALD);
            double vaultAmount = plugin.getEconomy().getBalance(p);
            
            // Profile description'ı al ve placeholder'ları değiştir
            List<String> profileDescription = profileConfig.getStringList("profile-description");
            List<String> lore = new ArrayList<>();
            
            for (String line : profileDescription) {
                line = line.replace("%vault%", String.valueOf((int)vaultAmount));
                line = line.replace("%gold%", String.valueOf(goldAmount));
                line = line.replace("%diamond%", String.valueOf(diamondAmount));
                line = line.replace("%emerald%", String.valueOf(emeraldAmount));
                lore.add(ColorUtil.toLegacy(line));
            }
            
            // Oyuncunun başını oluştur
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(displayName);
                if (!lore.isEmpty()) {
                    meta.setLore(lore);
                }
                playerHead.setItemMeta(meta);
            }
            
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, playerHead);
            }
        }
        
        // Info Button
        ConfigurationSection infoConfig = inventoryConfig.getConfigurationSection("info-panel.info-button");
        if (infoConfig != null) {
            int slot = infoConfig.getInt("slot", 38);
            String material = infoConfig.getString("material", "BOOK");
            String displayName = infoConfig.getString("display-name", "§6ℹ Bilgi");
            List<String> lore = infoConfig.getStringList("lore");
            
            // Renk desteği
            displayName = ColorUtil.toLegacy(displayName);
            lore = lore.stream().map(ColorUtil::toLegacy).toList();
            
            ItemStack infoItem = createItem(Material.valueOf(material), displayName, lore);
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, infoItem);
            }
        }
        
        // Close Button
        ConfigurationSection closeConfig = inventoryConfig.getConfigurationSection("info-panel.close-button");
        if (closeConfig != null) {
            int slot = closeConfig.getInt("slot", 43);
            String material = closeConfig.getString("material", "BARRIER");
            String displayName = closeConfig.getString("display-name", "§c✕ Kapat");
            List<String> lore = closeConfig.getStringList("lore");
            
            // Renk desteği
            displayName = ColorUtil.toLegacy(displayName);
            lore = lore.stream().map(ColorUtil::toLegacy).toList();
            
            ItemStack closeItem = createItem(Material.valueOf(material), displayName, lore);
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, closeItem);
            }
        }
    }

    private ItemStack createRefreshIndicator() {
        ConfigurationSection config = inventoryConfig.getConfigurationSection("info-panel.refresh-indicator");
        String material = config.getString("material", "CLOCK");
        String displayName = config.getString("display-name", "§6⏱ ꜱᴏɴʀᴀᴋɪ ʏᴇɴɪʟᴇɴᴍᴇ");
        
        // Renk desteği
        displayName = ColorUtil.toLegacy(displayName);
        
        ItemStack indicator = new ItemStack(Material.valueOf(material));
        ItemMeta meta = indicator.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            
            long timeUntil = plugin.getTradingSystem().getTimeUntilRefresh();
            long hours = TimeUnit.MILLISECONDS.toHours(timeUntil);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeUntil) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeUntil) % 60;
            
            // Count only available items
            int availableItems = countAvailableItems();
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.toLegacy("§7ᴋᴀʟᴀɴ ꜱᴜ́ʀᴇ: §e" + hours + "ꜱ " + minutes + "ᴅ " + seconds + "ꜱɴ"));
            lore.add(ColorUtil.toLegacy("§7ᴛᴏᴘʟᴀᴍ ᴜ́ʀᴜ́ɴ: §e" + availableItems));
            
            meta.setLore(lore);
            indicator.setItemMeta(meta);
        }
        
        return indicator;
    }

    private int countAvailableItems() {
        int count = 0;
        for (TraderItem item : itemsCache.values()) {
            if (item.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    private ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public TraderItem getTraderItem(String itemId) {
        return itemsCache.get(itemId);
    }

    public TraderItem getTraderItemByMaterial(String material) {
        for (TraderItem item : itemsCache.values()) {
            if (item.getMaterial().toString().equals(material)) {
                return item;
            }
        }
        return null;
    }

    public Map<String, TraderItem> getAllItems() {
        return new HashMap<>(itemsCache);
    }

    public int getInventorySize() {
        return inventoryConfig.getInt("inventory.size", 54);
    }

    public String getInventoryTitle() {
        return inventoryConfig.getString("inventory.title", "§6Gezgin Tüccar");
    }

    private int countItemInInventory(org.bukkit.entity.Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
}
