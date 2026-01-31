package noramu.fluxtrader.command;

import noramu.fluxtrader.FluxTrader;
import noramu.fluxtrader.gui.TraderGUI;
import noramu.fluxtrader.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.concurrent.TimeUnit;

public class FluxTraderCommand implements CommandExecutor {
    private final FluxTrader plugin;

    public FluxTraderCommand(FluxTrader plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getCommandUsage()));
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                if (!sender.hasPermission("fluxtrader.admin")) {
                    sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getPermissionDenied()));
                    return true;
                }
                plugin.getConfigManager().reload();
                plugin.getMessageManager().reload();
                plugin.getGUIManager().reload();
                sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getConfigReloaded()));
                return true;

            case "info":
                sendInfo(sender);
                return true;

            case "next":
                if (!sender.hasPermission("fluxtrader.admin")) {
                    sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getPermissionDenied()));
                    return true;
                }
                plugin.getTradingSystem().forceRefresh();
                sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getRefreshForced()));
                return true;

            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getPlayerOnly()));
                    return true;
                }
                Player player = (Player) sender;
                TraderGUI gui = new TraderGUI(plugin, player);
                gui.open(player);
                return true;

            default:
                sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getCommandUsage()));
                return true;
        }
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(ColorUtil.toLegacy(plugin.getMessageManager().getPluginInfoHeader()));
        String version = plugin.getMessageManager().getPluginVersion()
                .replace("%version%", "1.0.0");
        sender.sendMessage(ColorUtil.toLegacy(version));
        
        String items = plugin.getMessageManager().getPluginTotalItems()
                .replace("%items%", String.valueOf(plugin.getGUIManager().getAllItems().size()));
        sender.sendMessage(ColorUtil.toLegacy(items));
        
        String mode = plugin.getMessageManager().getPluginRefreshMode()
                .replace("%mode%", plugin.getConfigManager().isRefreshByHours() ? "belirli saatler" : "sabit aralik");
        sender.sendMessage(ColorUtil.toLegacy(mode));
    }
}
