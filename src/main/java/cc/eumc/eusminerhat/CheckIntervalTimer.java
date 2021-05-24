package cc.eumc.eusminerhat;

import cc.eumc.eusminerhat.miner.MinerManager;

public class CheckIntervalTimer implements Runnable {
    MinerHat plugin;

    public CheckIntervalTimer(MinerHat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        MinerManager minerManager = plugin.getMinerManager();

        if (minerManager == null) {
            return;
        }

        if (minerManager.checkAndToggleMining()) {
            plugin.sendInfo(String.format(plugin.l("miner.statusChanged"), minerManager.getMinerStatus() ?
                    plugin.l("miner.status.mining") : plugin.l("miner.status.stopped")));
        }
    }
}
