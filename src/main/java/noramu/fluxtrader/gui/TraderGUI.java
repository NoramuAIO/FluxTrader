package noramu.fluxtrader.gui;

import noramu.fluxtrader.FluxTrader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

public class TraderGUI {
    private final FluxTrader plugin;
    private final GUIManager guiManager;
    private final Inventory inventory;
    private final Player player;
    private BukkitTask updateTask;

    public TraderGUI(FluxTrader plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.guiManager = plugin.getGUIManager();
        this.inventory = guiManager.createInventory(player);
    }

    public void open(Player player) {
        player.openInventory(inventory);
        
        // Start update task for refresh indicator
        startUpdateTask();
    }

    private void startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Check if player still has the GUI open
            if (player.getOpenInventory().getTopInventory() != inventory) {
                if (updateTask != null) {
                    updateTask.cancel();
                }
                return;
            }
            
            // Update refresh indicator
            guiManager.updateRefreshIndicator(inventory);
        }, 0, 20); // Update every second (20 ticks)
    }

    public Inventory getInventory() {
        return inventory;
    }
}
