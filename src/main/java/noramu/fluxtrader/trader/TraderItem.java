package noramu.fluxtrader.trader;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TraderItem {
    private String id;
    private Material material;
    private double chance;
    private int minAmount;
    private int maxAmount;
    private double price;
    private String currencyType;  // GOLD, DIAMOND, EMERALD, VAULT
    private int dailyLimit;
    private String displayName;
    private String lore;
    private List<Integer> availableHours;
    private List<Integer> availableDays;
    private List<Integer> availableMonths;
    private String event;

    public TraderItem(Map<String, Object> config) {
        this.id = (String) config.get("id");
        this.material = Material.valueOf((String) config.get("material"));
        this.chance = ((Number) config.getOrDefault("chance", 100)).doubleValue();
        this.minAmount = ((Number) config.getOrDefault("min-amount", 1)).intValue();
        this.maxAmount = ((Number) config.getOrDefault("max-amount", 1)).intValue();
        this.price = ((Number) config.getOrDefault("price", 0)).doubleValue();
        this.currencyType = (String) config.getOrDefault("currency-type", "VAULT");
        this.dailyLimit = ((Number) config.getOrDefault("daily-limit", 64)).intValue();
        this.displayName = (String) config.getOrDefault("display-name", material.toString());
        
        // Lore - String or List
        Object loreObj = config.getOrDefault("lore", "");
        if (loreObj instanceof List) {
            List<?> loreList = (List<?>) loreObj;
            String[] loreArray = new String[loreList.size()];
            for (int i = 0; i < loreList.size(); i++) {
                loreArray[i] = loreList.get(i).toString();
            }
            this.lore = String.join("\n", loreArray);
        } else {
            this.lore = (String) loreObj;
        }
        
        // Time settings
        this.availableHours = new ArrayList<>();
        this.availableDays = new ArrayList<>();
        this.availableMonths = new ArrayList<>();
        
        if (config.get("available-hours") instanceof List) {
            for (Object hour : (List<?>) config.get("available-hours")) {
                if (hour instanceof Number) {
                    availableHours.add(((Number) hour).intValue());
                }
            }
        }
        
        if (config.get("available-days") instanceof List) {
            for (Object day : (List<?>) config.get("available-days")) {
                if (day instanceof Number) {
                    availableDays.add(((Number) day).intValue());
                }
            }
        }
        
        if (config.get("available-months") instanceof List) {
            for (Object month : (List<?>) config.get("available-months")) {
                if (month instanceof Number) {
                    availableMonths.add(((Number) month).intValue());
                }
            }
        }
        
        this.event = (String) config.getOrDefault("event", "");
    }

    /**
     * Ürünün şu anda mevcut olup olmadığını kontrol et
     */
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        
        // Saat kontrolü
        if (!availableHours.isEmpty() && !availableHours.contains(now.getHour())) {
            return false;
        }
        
        // Gün kontrolü (1=Pazartesi, 7=Pazar)
        if (!availableDays.isEmpty()) {
            int dayOfWeek = now.getDayOfWeek().getValue();
            if (!availableDays.contains(dayOfWeek)) {
                return false;
            }
        }
        
        // Ay kontrolü
        if (!availableMonths.isEmpty() && !availableMonths.contains(now.getMonthValue())) {
            return false;
        }
        
        return true;
    }

    /**
     * Get the reason why item is unavailable
     */
    public String getUnavailableReason() {
        LocalDateTime now = LocalDateTime.now();
        
        if (!availableHours.isEmpty() && !availableHours.contains(now.getHour())) {
            return "This item is only available at specific hours";
        }
        
        if (!availableDays.isEmpty()) {
            int dayOfWeek = now.getDayOfWeek().getValue();
            if (!availableDays.contains(dayOfWeek)) {
                return "This item is only available on specific days";
            }
        }
        
        if (!availableMonths.isEmpty() && !availableMonths.contains(now.getMonthValue())) {
            return "This item is only available in specific months";
        }
        
        return "This item is currently unavailable";
    }

    public ItemStack toItemStack(int amount) {
        ItemStack item = new ItemStack(material, amount);
        return item;
    }

    // Getters
    public String getId() { return id; }
    public Material getMaterial() { return material; }
    public double getChance() { return chance; }
    public int getMinAmount() { return minAmount; }
    public int getMaxAmount() { return maxAmount; }
    public double getPrice() { return price; }
    public String getCurrencyType() { return currencyType; }
    public int getDailyLimit() { return dailyLimit; }
    public String getDisplayName() { return displayName; }
    public String getLore() { return lore; }
    public List<Integer> getAvailableHours() { return availableHours; }
    public List<Integer> getAvailableDays() { return availableDays; }
    public List<Integer> getAvailableMonths() { return availableMonths; }
    public String getEvent() { return event; }
}
