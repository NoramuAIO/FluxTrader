package noramu.fluxtrader.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() {
        loadConfig();
    }

    public int getRefreshIntervalHours() {
        return config.getInt("refresh.interval-hours", 12);
    }

    public List<Integer> getRefreshHours() {
        List<?> list = config.getList("refresh.hours");
        List<Integer> result = new ArrayList<>();
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Number) {
                    result.add(((Number) obj).intValue());
                }
            }
        }
        return result;
    }

    public boolean isRefreshByHours() {
        return config.getBoolean("refresh.by-hours", false);
    }

    public boolean isAnnouncementsEnabled() {
        return config.getBoolean("announcements.enabled", true);
    }

    public List<Integer> getReminderMinutes() {
        List<?> list = config.getList("announcements.reminder-minutes");
        List<Integer> result = new ArrayList<>();
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Number) {
                    result.add(((Number) obj).intValue());
                }
            }
        }
        return result;
    }

    public boolean isAnnouncementBroadcast() {
        return config.getBoolean("announcements.broadcast", true);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
