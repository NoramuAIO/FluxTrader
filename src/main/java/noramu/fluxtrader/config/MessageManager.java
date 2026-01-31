package noramu.fluxtrader.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class MessageManager {
    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        loadMessages();
    }

    // Satın alma mesajları
    public String getPurchaseSuccess() {
        return messages.getString("messages.purchase-success", "&a✓ satin alindi: &e%amount%x %item%");
    }

    public String getPurchasePaid() {
        return messages.getString("messages.purchase-paid", "&7odenen: &e%price% %currency%");
    }

    // Hata mesajları
    public String getItemUnavailable() {
        return messages.getString("messages.item-unavailable", "&c✗ %reason%");
    }

    public String getDailyLimitReached() {
        return messages.getString("messages.daily-limit-reached", "&c✗ bu urnun gunluk limiti doldu!");
    }

    public String getDropChanceFailed() {
        return messages.getString("messages.drop-chance-failed", "&c✗ bu sefer urun dusmedi! (&e%chance%&c)");
    }

    public String getInsufficientVaultMoney() {
        return messages.getString("messages.insufficient-vault-money", "&c✗ yeterli para yok!");
    }

    public String getVaultMoneyDetails() {
        return messages.getString("messages.vault-money-details", "&7gerekli: &e%needed% %currency% &7mevcut: &e%balance%");
    }

    public String getInsufficientInventoryCurrency() {
        return messages.getString("messages.insufficient-inventory-currency", "&c✗ yeterli %currency% yok!");
    }

    public String getInventoryCurrencyDetails() {
        return messages.getString("messages.inventory-currency-details", "&7gerekli: &e%needed% %currency% &7mevcut: &e%available%");
    }

    // Duyurular
    public String getRefreshMessage() {
        return messages.getString("messages.refresh-message", "&6&l[Tüccar] &r&eurünler yenilendi! &7/fluxtrader gui");
    }

    public String getCountdownMessage() {
        return messages.getString("messages.countdown-message", "&6&l[Tüccar] &r&etüccar &e%time% &esonra yenileniyor!");
    }

    // Komut mesajları
    public String getCommandUsage() {
        return messages.getString("messages.command-usage", "&c/fluxtrader <reload|info|next|gui>");
    }

    public String getPermissionDenied() {
        return messages.getString("messages.permission-denied", "&cyetkiniz!");
    }

    public String getPlayerOnly() {
        return messages.getString("messages.player-only", "&csadece oyuncu komutu!");
    }

    public String getConfigReloaded() {
        return messages.getString("messages.config-reloaded", "&akonfigürasyon ve gui yenilendi!");
    }

    public String getRefreshForced() {
        return messages.getString("messages.refresh-forced", "&ayenilenme zorla tetiklendi!");
    }

    public String getPluginInfoHeader() {
        return messages.getString("messages.plugin-info-header", "&6&l=== tüccar bilgisi ===");
    }

    public String getPluginVersion() {
        return messages.getString("messages.plugin-version", "&7surum: &e%version%");
    }

    public String getPluginTotalItems() {
        return messages.getString("messages.plugin-total-items", "&7toplam urun: &e%items%");
    }

    public String getPluginRefreshMode() {
        return messages.getString("messages.plugin-refresh-mode", "&7yenilenme modu: &e%mode%");
    }

    public FileConfiguration getMessages() {
        return messages;
    }
}
