package cc.eumc.eusminerhat;

import cc.eumc.eusminerhat.command.bukkit.AdminCommandExecutor;
import cc.eumc.eusminerhat.listener.PlayerListener;
import cc.eumc.eusminerhat.miner.MinerManager;
import cc.eumc.eusminerhat.miner.MinerPolicy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class MinerHat extends JavaPlugin {
    private MinerManager minerManager;
    private String MinerPath;
    private String LanguagePath;
    private MinerHatConfig config;
    private LocaleManager localeManager;

    public MinerManager getMinerManager() {
        return minerManager;
    }
    public String getMinerPath() {
        return MinerPath;
    }
    public String getLanguagePath() {
        return LanguagePath;
    }
    public MinerHatConfig getMinerHatConfig() {
        return config;
    }
    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    @Override
    public void onEnable() {
        this.MinerPath = getDataFolder() + "/miner";
        this.LanguagePath = getDataFolder() + "/language";

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

        File languageDir = new File(LanguagePath);
        if (!languageDir.exists()) {
            languageDir.mkdir();

            // Export default language pack
            try {
                exportDefaultLanguage();
            } catch (Exception e) {
                e.printStackTrace();
                sendSevere("§cFailed exporting default language pack.");
            }
        }

        this.config = new MinerHatConfig(this);

        try {
            this.localeManager = LocaleManager.createLocaleManager(this, config.getLanguage());
        } catch (Exception e) {
            e.printStackTrace();
            sendSevere(String.format("§cFailed loading language pack: %s.json", config.getLanguage()));
        }

        getCommand("minerhat").setExecutor(new AdminCommandExecutor(this));


        // Failable operation
        // If the policy failed to load, plugin enabling would stop here.
        try {
            MinerPolicy policy = MinerPolicy.loadPolicy(Paths.get(getMinerPath() + "/" + config.getMiner() + ".json"));
            this.minerManager = new MinerManager(this, config.getMiner(), policy);
            printMinerInformation();
        } catch (Exception e) {
            e.printStackTrace();
            sendSevere(l("policy.failure.loading"));
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
            minerManager.stopMining();
        }
    }

    void printMinerInformation() {
        sendInfo(String.format(l("miner.info.name"), config.getMiner()));
        sendInfo(String.format(l("miner.info.checkInterval"), config.getCheckInterval()));
    }

    public String l(String stringToken) {
        return localeManager.getLocalized(stringToken);
    }

    public void sendSevere(String message) {
        Bukkit.getServer().getLogger().severe(l("message.header") + message);
    }

    public void sendWarn(String message) {
        Bukkit.getServer().getLogger().warning(l("message.header") + message);
    }

    public void sendInfo(String message) {
        Bukkit.getServer().getLogger().info(l("message.header") + message);
    }

    private void createExamplePolicy() throws Exception {
        InputStream in = getResource("example-xmrig-policy.json");
        Files.copy(Objects.requireNonNull(in), new File(MinerPath + "/xmrig.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
        File minerDir = new File(MinerPath + "/xmrig");
        minerDir.mkdir();
    }

    private void exportDefaultLanguage() throws Exception {
        InputStream in = getResource("en.json");
        Files.copy(Objects.requireNonNull(in), new File(LanguagePath + "/en.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
