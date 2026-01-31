package noramu.fluxtrader.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    /**
     * MiniMessage formatını Component'e dönüştür
     * Örnek: "<red>Merhaba</red>", "<gold>Altın</gold>"
     */
    public static Component parseMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        try {
            return MINI_MESSAGE.deserialize(text);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    /**
     * Legacy renk kodlarını Component'e dönüştür
     * Örnek: "§cKırmızı", "&cKırmızı"
     */
    public static Component parseLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        try {
            return LEGACY.deserialize(text);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    /**
     * Hem MiniMessage hem Legacy formatını destekle
     * MiniMessage: <red>Metin</red>
     * Legacy: &cMetin veya §cMetin
     */
    public static Component parseText(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // MiniMessage formatı kontrol et
        if (text.contains("<") && text.contains(">")) {
            return parseMiniMessage(text);
        }

        // Legacy formatı kontrol et
        if (text.contains("&") || text.contains("§")) {
            return parseLegacy(text);
        }

        return Component.text(text);
    }

    /**
     * Component'i legacy string'e dönüştür (uyumluluk için)
     */
    public static String componentToLegacy(Component component) {
        try {
            return LEGACY.serialize(component);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * String'i legacy formatına dönüştür
     */
    public static String toLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * MiniMessage renk kodları
     */
    public static class Colors {
        // Temel renkler
        public static final String BLACK = "<black>";
        public static final String DARK_BLUE = "<dark_blue>";
        public static final String DARK_GREEN = "<dark_green>";
        public static final String DARK_AQUA = "<dark_aqua>";
        public static final String DARK_RED = "<dark_red>";
        public static final String DARK_PURPLE = "<dark_purple>";
        public static final String GOLD = "<gold>";
        public static final String GRAY = "<gray>";
        public static final String DARK_GRAY = "<dark_gray>";
        public static final String BLUE = "<blue>";
        public static final String GREEN = "<green>";
        public static final String AQUA = "<aqua>";
        public static final String RED = "<red>";
        public static final String LIGHT_PURPLE = "<light_purple>";
        public static final String YELLOW = "<yellow>";
        public static final String WHITE = "<white>";

        // Özel renkler
        public static final String RESET = "<reset>";
        public static final String BOLD = "<bold>";
        public static final String ITALIC = "<italic>";
        public static final String UNDERLINED = "<underlined>";
        public static final String STRIKETHROUGH = "<strikethrough>";
        public static final String OBFUSCATED = "<obfuscated>";

        // Hex renkler
        public static String hex(String hexColor) {
            return "<color:" + hexColor + ">";
        }

        // RGB renkler
        public static String rgb(int r, int g, int b) {
            return String.format("<color:#%02x%02x%02x>", r, g, b);
        }
    }

    /**
     * Legacy renk kodları
     */
    public static class LegacyColors {
        public static final String BLACK = "&0";
        public static final String DARK_BLUE = "&1";
        public static final String DARK_GREEN = "&2";
        public static final String DARK_AQUA = "&3";
        public static final String DARK_RED = "&4";
        public static final String DARK_PURPLE = "&5";
        public static final String GOLD = "&6";
        public static final String GRAY = "&7";
        public static final String DARK_GRAY = "&8";
        public static final String BLUE = "&9";
        public static final String GREEN = "&a";
        public static final String AQUA = "&b";
        public static final String RED = "&c";
        public static final String LIGHT_PURPLE = "&d";
        public static final String YELLOW = "&e";
        public static final String WHITE = "&f";

        // Formatlar
        public static final String BOLD = "&l";
        public static final String STRIKETHROUGH = "&m";
        public static final String UNDERLINED = "&n";
        public static final String ITALIC = "&o";
        public static final String OBFUSCATED = "&k";
        public static final String RESET = "&r";
    }
}
