package noramu.fluxtrader;

import noramu.fluxtrader.config.ConfigManager;
import noramu.fluxtrader.config.MessageManager;
import noramu.fluxtrader.data.DataManager;
import noramu.fluxtrader.listener.PlayerListener;
import noramu.fluxtrader.listener.InventoryClickListener;
import noramu.fluxtrader.command.FluxTraderCommand;
import noramu.fluxtrader.command.FluxTraderTabCompleter;
import noramu.fluxtrader.trader.TradingSystem;
import noramu.fluxtrader.gui.GUIManager;
import noramu.fluxtrader.placeholder.FluxTraderPlaceholder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;

public class FluxTrader extends JavaPlugin {
    private static FluxTrader instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DataManager dataManager;
    private TradingSystem tradingSystem;
    private GUIManager guiManager;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("Starting FluxTrader...");
        
        // Initialize config and data managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        dataManager = new DataManager(this);
        
        // Setup economy system
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize GUI manager
        guiManager = new GUIManager(this);
        
        // Initialize trading system
        tradingSystem = new TradingSystem(this);
        tradingSystem.initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        
        // Register commands
        getCommand("fluxtrader").setExecutor(new FluxTraderCommand(this));
        getCommand("fluxtrader").setTabCompleter(new FluxTraderTabCompleter());
        
        // PlaceholderAPI support
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FluxTraderPlaceholder(this).register();
            getLogger().info("PlaceholderAPI integration successful.");
        }
        
        getLogger().info("FluxTrader loaded successfully!");
    }

    @Override
    public void onDisable() {
        if (tradingSystem != null) {
            tradingSystem.shutdown();
        }
        if (dataManager != null) {
            dataManager.save();
        }
        getLogger().info("FluxTrader disabled.");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static FluxTrader getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public TradingSystem getTradingSystem() {
        return tradingSystem;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
