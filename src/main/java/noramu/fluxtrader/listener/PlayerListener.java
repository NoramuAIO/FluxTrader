package noramu.fluxtrader.listener;

import noramu.fluxtrader.FluxTrader;
import noramu.fluxtrader.gui.TraderGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.entity.ArmorStand;

public class PlayerListener implements Listener {
    private final FluxTrader plugin;

    public PlayerListener(FluxTrader plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) event.getRightClicked();
            
            // Tüccar armor stand'ı kontrol et
            if (stand.hasMetadata("fluxtrader")) {
                event.setCancelled(true);
                
                TraderGUI gui = new TraderGUI(plugin, event.getPlayer());
                gui.open(event.getPlayer());
            }
        }
    }
}
