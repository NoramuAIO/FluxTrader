package noramu.fluxtrader.listener;

import noramu.fluxtrader.FluxTrader;
import noramu.fluxtrader.trader.TraderItem;
import noramu.fluxtrader.util.ColorUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import net.milkbowl.vault.economy.Economy;
import java.util.ArrayList;
import java.util.List;

public class InventoryClickListener implements Listener {
    private final FluxTrader plugin;

    public InventoryClickListener(FluxTrader plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        // Herhangi bir inventory drag event'ini engelle
        event.setCancelled(true);
        
        // Oyuncunun inventory'sini güncelle
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
            player.updateInventory();
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryPickupItem(org.bukkit.event.inventory.InventoryPickupItemEvent event) {
        // GUI başlığını kontrol et
        String title = event.getInventory().getHolder() != null ? 
            event.getInventory().getHolder().toString() : "";
        
        // Tüccar GUI'sinden item alınmasını engelle
        String guiTitle = ColorUtil.toLegacy(plugin.getConfigManager().getConfig()
                .getString("inventory.title", "&6Tüccar"));
        
        if (title.contains(guiTitle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // GUI başlığını kontrol et
        String title = event.getView().getTitle();
        String guiTitle = ColorUtil.toLegacy(plugin.getConfigManager().getConfig()
                .getString("inventory.title", "&6Tüccar"));
        
        if (!title.equals(guiTitle)) {
            return;
        }

        // Tüm tıklamaları iptal et (drag ve shift-click engelle)
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            player.updateInventory();
            return;
        }

        int slot = event.getRawSlot();

        // Kapat butonu (slot 26)
        if (slot == 26) {
            player.closeInventory();
            return;
        }

        // Profile butonu (slot 22) - sadece görüntüleme, tıklama yapılmaz
        if (slot == 22) {
            player.updateInventory();
            return;
        }

        // Ürün slotları: 11, 12, 14 - sadece LEFT_CLICK
        if (slot == 11 || slot == 12 || slot == 14) {
            // Sadece sol tıklama ile satın alma
            if (event.getClick().isLeftClick()) {
                handleItemPurchase(player, clickedItem);
            }
            player.updateInventory();
            return;
        }
        
        player.updateInventory();
    }

    private void handleItemPurchase(Player player, ItemStack item) {
        // Find item by material
        TraderItem traderItem = plugin.getGUIManager().getTraderItemByMaterial(item.getType().toString());
        
        if (traderItem == null) {
            return;
        }

        // Check if item is available (time-based)
        if (!traderItem.isAvailable()) {
            player.sendMessage(ColorUtil.toLegacy("&c✗ " + traderItem.getUnavailableReason()));
            return;
        }

        // Check daily limit
        int dailyUsed = plugin.getDataManager().getDailyStockUsed(traderItem.getId());
        if (dailyUsed >= traderItem.getDailyLimit()) {
            player.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getDailyLimitReached()));
            return;
        }

        // Check drop chance
        if (Math.random() * 100 > traderItem.getChance()) {
            String msg = plugin.getMessageManager().getDropChanceFailed()
                    .replace("%chance%", String.valueOf((int)traderItem.getChance()));
            player.sendMessage(ColorUtil.toLegacy(msg));
            return;
        }

        // Random amount
        int amount = traderItem.getMinAmount() + 
                (int) (Math.random() * (traderItem.getMaxAmount() - traderItem.getMinAmount() + 1));

        // Check currency based on type
        String currencyType = traderItem.getCurrencyType().toUpperCase();
        
        if (currencyType.equals("VAULT")) {
            // Check Vault money
            Economy economy = plugin.getEconomy();
            double totalPrice = traderItem.getPrice() * amount;

            if (!economy.has(player, totalPrice)) {
                String currencyDisplay = getCurrencyDisplay(traderItem.getCurrencyType());
                player.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getInsufficientVaultMoney()));
                String details = plugin.getMessageManager().getVaultMoneyDetails()
                        .replace("%needed%", String.valueOf((int)totalPrice))
                        .replace("%currency%", currencyDisplay)
                        .replace("%balance%", String.valueOf((int)economy.getBalance(player)));
                player.sendMessage(ColorUtil.toLegacy(details));
                return;
            }

            // Withdraw money
            economy.withdrawPlayer(player, totalPrice);
        } else {
            // Check inventory for material
            Material currencyMaterial = getCurrencyMaterial(currencyType);
            int needed = (int) traderItem.getPrice() * amount;
            int hasAmount = countItemInInventory(player, currencyMaterial);

            if (hasAmount < needed) {
                String currencyDisplay = getCurrencyDisplay(traderItem.getCurrencyType());
                String msg = plugin.getMessageManager().getInsufficientInventoryCurrency()
                        .replace("%currency%", currencyDisplay);
                player.sendMessage(ColorUtil.toLegacy(msg));
                String details = plugin.getMessageManager().getInventoryCurrencyDetails()
                        .replace("%needed%", String.valueOf(needed))
                        .replace("%currency%", currencyDisplay)
                        .replace("%available%", String.valueOf(hasAmount));
                player.sendMessage(ColorUtil.toLegacy(details));
                return;
            }

            // Remove currency from inventory
            removeItemFromInventory(player, currencyMaterial, needed);
        }

        // Add item to inventory
        ItemStack purchasedItem = new ItemStack(traderItem.getMaterial(), amount);
        player.getInventory().addItem(purchasedItem);

        // Update daily stock
        plugin.getDataManager().addDailyStockUsed(traderItem.getId(), amount);

        // Success message
        String currencyDisplay = getCurrencyDisplay(traderItem.getCurrencyType());
        String success = plugin.getMessageManager().getPurchaseSuccess()
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", traderItem.getDisplayName());
        player.sendMessage(ColorUtil.toLegacy(success));
        
        String paid = plugin.getMessageManager().getPurchasePaid()
                .replace("%price%", String.valueOf((int)traderItem.getPrice() * amount))
                .replace("%currency%", currencyDisplay);
        player.sendMessage(ColorUtil.toLegacy(paid));
        
        // GUI'yi refresh et - günlük limit güncellensin
        player.updateInventory();
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

    private Material getCurrencyMaterial(String currencyType) {
        switch (currencyType.toUpperCase()) {
            case "GOLD":
                return Material.GOLD_INGOT;
            case "DIAMOND":
                return Material.DIAMOND;
            case "EMERALD":
                return Material.EMERALD;
            default:
                return Material.GOLD_INGOT;
        }
    }

    private int countItemInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItemFromInventory(Player player, Material material, int amount) {
        int toRemove = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && toRemove > 0) {
                if (item.getAmount() <= toRemove) {
                    toRemove -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                    toRemove = 0;
                }
            }
        }
    }
}

