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
            plugin.sendInfo(String.format("Miner status changed: %s", minerManager.getMinerStatus() ? "mining" : "stopped"));
        }
    }
}
