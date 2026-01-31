package noramu.fluxtrader.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class DataManager {
    private final JavaPlugin plugin;
    private FileConfiguration data;
    private File dataFile;
    private File dbFile;

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        // YAML file (old format)
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        
        // DB file (new format)
        dbFile = new File(plugin.getDataFolder(), "data.db");
        
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Use DB file if exists, otherwise load from YAML
        if (dbFile.exists()) {
            data = YamlConfiguration.loadConfiguration(dbFile);
        } else if (dataFile.exists()) {
            data = YamlConfiguration.loadConfiguration(dataFile);
        } else {
            try {
                dbFile.createNewFile();
                data = new YamlConfiguration();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create data file: " + e.getMessage());
                data = new YamlConfiguration();
            }
        }
    }

    public void save() {
        try {
            // Save to DB file
            data.save(dbFile);
            
            // Delete old YAML file (optional)
            if (dataFile.exists()) {
                dataFile.delete();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data file: " + e.getMessage());
        }
    }

    public void reload() {
        loadData();
    }

    public long getLastRefreshTime() {
        return data.getLong("last-refresh", 0);
    }

    public void setLastRefreshTime(long time) {
        data.set("last-refresh", time);
        save();
    }

    public int getDailyStockUsed(String itemId) {
        String today = LocalDate.now().toString();
        return data.getInt("daily-stock." + today + "." + itemId, 0);
    }

    public void addDailyStockUsed(String itemId, int amount) {
        String today = LocalDate.now().toString();
        String path = "daily-stock." + today + "." + itemId;
        int current = data.getInt(path, 0);
        data.set(path, current + amount);
        save();
    }

    public void resetDailyStock() {
        data.set("daily-stock", null);
        save();
    }

    public List<String> getTodayUsedItems() {
        String today = LocalDate.now().toString();
        List<String> used = new ArrayList<>();
        if (data.contains("daily-stock." + today)) {
            for (String key : data.getConfigurationSection("daily-stock." + today).getKeys(false)) {
                used.add(key);
            }
        }
        return used;
    }

    public FileConfiguration getData() {
        return data;
    }

    public File getDataFile() {
        return dbFile;
    }
}
