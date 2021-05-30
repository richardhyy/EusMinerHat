package cc.eumc.eusminerhat;

import cc.eumc.eusminerhat.command.bukkit.AdminCommandExecutor;
import cc.eumc.eusminerhat.command.bukkit.PlayerCommandExecutor;
import cc.eumc.eusminerhat.contribution.ContributorManager;
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
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class MinerHat extends JavaPlugin {
    private MinerManager minerManager;
    private String MinerPath;
    private String LanguagePath;
    private String PlayerContributionPath;
    private MinerHatConfig config;
    private LocaleManager localeManager;
    private ContributorManager contributorManager;
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
    public String getPlayerContributionPath() { return PlayerContributionPath; }
    public MinerHatConfig getMinerHatConfig() {
        return config;
    }
    public LocaleManager getLocaleManager() {
        return localeManager;
    }
    public ContributorManager getContributorManager() { return contributorManager; }

    @Override
    public void onEnable() {
        this.MinerPath = getDataFolder() + "/miner";
        this.LanguagePath = getDataFolder() + "/language";
        this.PlayerContributionPath = getDataFolder() + "/contribution";

        loadMinerHatConfig();

        getCommand("minerhatadmin").setExecutor(new AdminCommandExecutor(this));
        getCommand("minerhat").setExecutor(new PlayerCommandExecutor(this));

        try {
            registerMiner();
        } catch (MinerException e) {
            sendSevere(l("policy.failure.loading"));
        }
        registerPlayerContribution();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
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

        File contributionDir = new File(PlayerContributionPath);
        if (!contributionDir.exists()) {
            contributionDir.mkdir();
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
     * Set up ContributionManager for player commands if player contribution is enabled.
     */
    public void registerPlayerContribution() {
//        if (this.contributorManager != null) {
//            this.contributorManager = null;
//        }

        if (!config.isPlayerContributionEnabled()) {
            // Do nothing here!
            // So that third-party plugins can still deal with the remaining player revenue after this function being disabled

            //return; // Player contribution disabled
        }

        this.contributorManager = new ContributorManager(this, config.getPoolSourceType());
    }

    /**
     * Create a new MinerManager and RunningCheckTimer if local mining is enabled.
     * It will also try to stop the existing miner if needed.
     * @throws MinerException
     */
    public void registerMiner() throws MinerException {
        if (this.minerManager != null) { // Attempt to stop existing miner
            this.minerManager.stopMining();
            this.minerManager = null;
        }

        if (this.checkTask != null) { // Attempt to cancel existing timer
            this.checkTask.cancel();
            this.checkTask = null;
        }

        if (!config.isLocalMiningEnabled()) {
            return; // Local mining disabled
        }

        try {
            MinerPolicy policy = MinerPolicy.loadPolicy(getMinerPath() + "/" + config.getMiner() + ".json");
            this.minerManager = new MinerManager(this, config.getMiner(), policy);
            printMinerInformation();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MinerException(MinerException.MinerExceptionType.FAILED_LOADING_POLICY, "Failed loading miner policy");
        }


        if (config.getCheckIntervalSeconds() > 0) { // check timer will be disabled if interval is less or equal than 0
            long interval = config.getCheckIntervalSeconds() * 20L;
            this.checkTask = getServer().getScheduler().runTaskTimer(this, new CheckIntervalTimer(this), interval, interval);
        }
    }

    void printMinerInformation() {
        sendInfo(String.format(l("miner.info.name"), config.getMiner()));
        sendInfo(String.format(l("miner.info.checkInterval"), config.getCheckIntervalSeconds()));
        sendInfo(String.format(l("miner.info.restartInterval"), getMinerHatConfig().getRestartMinerIntervalMinutes()));
    }

    public String l(String stringToken) {
        return localeManager.getLocalized(stringToken).replace("&", "§");
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
