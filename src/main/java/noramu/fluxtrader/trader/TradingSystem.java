package noramu.fluxtrader.trader;

import noramu.fluxtrader.FluxTrader;
import noramu.fluxtrader.config.ConfigManager;
import noramu.fluxtrader.data.DataManager;
import noramu.fluxtrader.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TradingSystem {
    private final FluxTrader plugin;
    private final ConfigManager configManager;
    private final DataManager dataManager;
    private BukkitTask refreshTask;
    private List<TraderItem> currentItems;
    private long nextRefreshTime;
    private FileConfiguration itemsConfig;

    public TradingSystem(FluxTrader plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.dataManager = plugin.getDataManager();
        this.currentItems = new ArrayList<>();
    }

    public void initialize() {
        loadItemsConfig();
        loadItemsFromConfig();
        calculateNextRefresh();
        startRefreshTask();
        startReminderTask();
        
        // Eğer yenilenme zamanı gelmişse hemen yenile
        if (System.currentTimeMillis() >= nextRefreshTime) {
            refreshItems();
        }
    }

    private void loadItemsConfig() {
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    private void loadItemsFromConfig() {
        currentItems.clear();
        ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("items");
        
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                Map<String, Object> itemConfig = itemsSection.getConfigurationSection(key).getValues(false);
                TraderItem item = new TraderItem(itemConfig);
                currentItems.add(item);
            }
        }
    }

    private void calculateNextRefresh() {
        long lastRefresh = dataManager.getLastRefreshTime();
        
        if (configManager.isRefreshByHours()) {
            List<Integer> refreshHours = configManager.getRefreshHours();
            if (refreshHours.isEmpty()) {
                nextRefreshTime = System.currentTimeMillis() + 12 * 60 * 60 * 1000;
                return;
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextTime = now.withHour(refreshHours.get(0)).withMinute(0).withSecond(0);
            
            if (nextTime.isBefore(now)) {
                nextTime = nextTime.plusDays(1);
            }
            
            nextRefreshTime = nextTime.atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
        } else {
            long interval = configManager.getRefreshIntervalHours() * 60 * 60 * 1000;
            nextRefreshTime = lastRefresh + interval;
            
            if (nextRefreshTime < System.currentTimeMillis()) {
                nextRefreshTime = System.currentTimeMillis() + interval;
            }
        }
    }

    private void startRefreshTask() {
        refreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (System.currentTimeMillis() >= nextRefreshTime) {
                refreshItems();
            }
        }, 0, 20 * 60); // Her dakika kontrol et
    }

    public void refreshItems() {
        loadItemsConfig();
        loadItemsFromConfig();
        dataManager.setLastRefreshTime(System.currentTimeMillis());
        dataManager.resetDailyStock();
        calculateNextRefresh();
        
        // Reload GUI cache
        plugin.getGUIManager().reload();
        
        // Close all open GUIs and notify players
        String guiTitle = ColorUtil.toLegacy(plugin.getConfigManager().getConfig().getString("inventory.title", "&6Tüccar"));
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().equals(guiTitle)) {
                player.closeInventory();
                player.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getRefreshMessage()));
            }
        }
        
        plugin.getLogger().info("Trader inventory refreshed!");
        announceRefresh();
    }

    private void selectRandomItems() {
        // This method is no longer needed - items are loaded from config
    }

    public List<TraderItem> getCurrentItems() {
        return new ArrayList<>(currentItems);
    }

    public long getNextRefreshTime() {
        return nextRefreshTime;
    }

    public long getTimeUntilRefresh() {
        return Math.max(0, nextRefreshTime - System.currentTimeMillis());
    }

    public void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
    }

    public void forceRefresh() {
        refreshItems();
    }

    private void startReminderTask() {
        if (!configManager.isAnnouncementsEnabled()) {
            return;
        }
        
        // Check every 10 seconds for reminders
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkReminders, 0, 200);
    }

    private void checkReminders() {
        long timeUntilRefresh = getTimeUntilRefresh();
        long minutesLeft = timeUntilRefresh / (60 * 1000);
        long secondsLeft = (timeUntilRefresh % (60 * 1000)) / 1000;
        
        List<Integer> reminderMinutes = configManager.getReminderMinutes();
        
        for (Integer reminderMin : reminderMinutes) {
            if (reminderMin == 0) {
                // Last 10 seconds - only countdown message
                if (timeUntilRefresh <= 10000 && timeUntilRefresh > 0) {
                    announceCountdown(timeUntilRefresh);
                    break;
                }
            } else {
                // Specific minutes - only countdown message
                if (minutesLeft == reminderMin && secondsLeft < 10) {
                    announceCountdown(timeUntilRefresh);
                    break;
                }
            }
        }
    }

    private void announceCountdown(long timeUntilRefresh) {
        if (!configManager.isAnnouncementsEnabled()) {
            return;
        }
        
        long hours = timeUntilRefresh / (60 * 60 * 1000);
        long minutes = (timeUntilRefresh % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (timeUntilRefresh % (60 * 1000)) / 1000;
        
        String timeStr;
        if (hours > 0) {
            timeStr = hours + "s " + minutes + "d " + seconds + "sn";
        } else if (minutes > 0) {
            timeStr = minutes + "d " + seconds + "sn";
        } else {
            timeStr = seconds + "sn";
        }
        
        String message = plugin.getMessageManager().getCountdownMessage().replace("%time%", timeStr);
        if (configManager.isAnnouncementBroadcast()) {
            Bukkit.broadcastMessage(ColorUtil.toLegacy(message));
        }
    }

    private void announceRefresh() {
        if (!configManager.isAnnouncementsEnabled()) {
            return;
        }
        
        // Only show refresh message when items are refreshed
        String message = plugin.getMessageManager().getRefreshMessage();
        if (configManager.isAnnouncementBroadcast()) {
            Bukkit.broadcastMessage(ColorUtil.toLegacy(message));
        }
    }

}
