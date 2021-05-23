package cc.eumc.eusminerhat;

import cc.eumc.eusminerhat.listener.PlayerListener;
import cc.eumc.eusminerhat.miner.MinerManager;
import cc.eumc.eusminerhat.miner.MinerPolicy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class MinerHat extends JavaPlugin {
    private MinerManager minerManager;
    private String MinerPath;
    private MinerHatConfig config;

    public MinerManager getMinerManager() {
        return minerManager;
    }

    public String getMinerPath() {
        return MinerPath;
    }

    public MinerHatConfig getMinerHatConfig() {
        return config;
    }

    @Override
    public void onEnable() {
        this.MinerPath = getDataFolder() + "/miner";
        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            saveDefaultConfig();
        }

        reloadConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File minerDir = new File(MinerPath);
        if (!minerDir.exists()) {
            minerDir.mkdir();

            // Create example policy on init
            try {
                createExamplePolicy();
            } catch (Exception e) {
                e.printStackTrace();
                sendWarn("§eFailed creating miner policy example.");
            }
        }

        this.config = new MinerHatConfig(this);

        try {
            MinerPolicy policy = MinerPolicy.loadPolicy(Paths.get(getMinerPath() + "/" + config.getMiner() + ".json"));
            this.minerManager = new MinerManager(this, config.getMiner(), policy);
            printMinerInformation();
        } catch (Exception e) {
            e.printStackTrace();
            sendSevere("§cFailed loading policy.");
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (config.getCheckInterval() > 0) { // check timer will be disabled if interval is less or equal than 0
            long interval = config.getCheckInterval() * 20L;
            getServer().getScheduler().runTaskTimer(this, new CheckIntervalTimer(this), interval, interval);
        }
    }

    @Override
    public void onDisable() {
        if (minerManager != null) {
            sendInfo("Stopping miner.");
            minerManager.stopMining();
        }
    }

    void printMinerInformation() {
        sendInfo(String.format("Name: %s", config.getMiner()));
        sendInfo(String.format("Check Interval: %s", config.getCheckInterval()));
    }

    public String l(String stringToken) {
        // TODO: localization
        return stringToken;
    }

    public void sendSevere(String message) {
        Bukkit.getServer().getLogger().severe("[EusMinerHat] " + message);
    }

    public void sendWarn(String message) {
        Bukkit.getServer().getLogger().warning("[EusMinerHat] " + message);
    }

    public void sendInfo(String message) {
        Bukkit.getServer().getLogger().info("[EusMinerHat] " + message);
    }

    private void createExamplePolicy() throws Exception {
        InputStream in = getResource("example-xmrig-policy.json");
        Files.copy(Objects.requireNonNull(in), new File(MinerPath + "/xmrig.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
        File minerDir = new File(MinerPath + "/xmrig");
        minerDir.mkdir();
    }
}
