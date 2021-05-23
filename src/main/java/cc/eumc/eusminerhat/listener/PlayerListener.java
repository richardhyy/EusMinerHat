package cc.eumc.eusminerhat.listener;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.miner.MinerManager;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    MinerHat plugin;

    public PlayerListener(MinerHat plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(PlayerJoinEvent e) {
        checkMiner();
    }

    public void onPlayerQuit(PlayerQuitEvent e) {
        checkMiner();
    }

    private void checkMiner() {
        MinerManager minerManager = plugin.getMinerManager();

        if (minerManager == null) {
            return;
        }

        if (minerManager.checkAndToggleMining()) {
            plugin.sendInfo(String.format("Miner status changed: %s", minerManager.getMinerStatus() ? "mining" : "stopped"));
        }
    }
}
