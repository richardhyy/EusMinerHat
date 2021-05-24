package cc.eumc.eusminerhat;

import cc.eumc.eusminerhat.command.bukkit.AdminCommandExecutor;
import cc.eumc.eusminerhat.exception.MinerException;
import cc.eumc.eusminerhat.listener.PlayerListener;
import cc.eumc.eusminerhat.miner.MinerManager;
import cc.eumc.eusminerhat.miner.MinerPolicy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
    private BukkitTask checkTask;

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

        loadMinerHatConfig();

        getCommand("minerhatadmin").setExecutor(new AdminCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        try {
            registerMiner();
        } catch (MinerException e) {
            sendSevere(l("policy.failure.loading"));
        }
    }

    @Override
    public void onDisable() {
        if (minerManager != null) {
            minerManager.stopMining();
        }
    }

    public void loadMinerHatConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
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

    }

    /**
     * Create a new MinerManager and RunningCheckTimer.
     * It will also try to stop the existing miner if needed.
     * @throws MinerException
     */
    public void registerMiner() throws MinerException {
        if (this.minerManager != null) { // Attempt to stop existing miner
            this.minerManager.stopMining();
            this.minerManager = null;
        }

        try {
            MinerPolicy policy = MinerPolicy.loadPolicy(Paths.get(getMinerPath() + "/" + config.getMiner() + ".json"));
            this.minerManager = new MinerManager(this, config.getMiner(), policy);
            printMinerInformation();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MinerException(MinerException.MinerExceptionType.FAILED_LOADING_POLICY, "Failed loading miner policy");
        }

        if (this.checkTask != null) { // Attempt to cancel existing timer
            this.checkTask.cancel();
            this.checkTask = null;
        }

        if (config.getCheckInterval() > 0) { // check timer will be disabled if interval is less or equal than 0
            long interval = config.getCheckInterval() * 20L;
            this.checkTask = getServer().getScheduler().runTaskTimer(this, new CheckIntervalTimer(this), interval, interval);
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
        Bukkit.getServer().getLogger().severe(prefixForEachLine(message));
    }

    public void sendWarn(String message) {
        Bukkit.getServer().getLogger().warning(prefixForEachLine(message));
    }

    public void sendInfo(String message) {
        Bukkit.getServer().getLogger().info(prefixForEachLine(message));
    }

    public String prefixForEachLine(String text) {
        String prefix = l("message.prefix");
        String[] lines = text.split("\n");
        for (int i=0; i<lines.length; i++) {
            lines[i] = prefix + lines[i];
        }
        return String.join("\n", lines);
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
