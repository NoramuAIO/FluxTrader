package noramu.fluxtrader.placeholder;

import noramu.fluxtrader.FluxTrader;
import noramu.fluxtrader.util.ColorUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import java.util.concurrent.TimeUnit;

public class FluxTraderPlaceholder extends PlaceholderExpansion {
    private final FluxTrader plugin;

    public FluxTraderPlaceholder(FluxTrader plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "fluxtrader";
    }

    @Override
    public String getAuthor() {
        return "noramu";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("next_refresh")) {
            long timeUntil = plugin.getTradingSystem().getTimeUntilRefresh();
            long hours = TimeUnit.MILLISECONDS.toHours(timeUntil);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeUntil) % 60;
            return hours + "s " + minutes + "d";
        }

        if (identifier.equals("next_refresh_seconds")) {
            long timeUntil = plugin.getTradingSystem().getTimeUntilRefresh();
            return String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timeUntil));
        }

        if (identifier.equals("items_count")) {
            return String.valueOf(plugin.getGUIManager().getAllItems().size());
        }

        return null;
    }
}
