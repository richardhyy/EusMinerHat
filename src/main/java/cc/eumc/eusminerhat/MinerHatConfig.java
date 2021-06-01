package cc.eumc.eusminerhat;

import cc.eumc.eusminerhat.contribution.source.PoolSourceType;

@SuppressWarnings("FieldMayBeFinal")
public class MinerHatConfig {
    private String language;

    private boolean localMiningEnabled;
    private String miner;
    private int checkIntervalSeconds;

    private int restartMinerIntervalMinutes;

    private boolean playerContributionEnabled;
    private PoolSourceType poolSourceType;
    private String walletAddress;
    private String playerMiningNoteMessage;
    private String playerMiningNoteValue;
    private String workerPrefix;
    private int walletInfoExpireSeconds;
    private double revenueFactor;
    private int revenueDecimalPlaces;
//    private PayoutInterval payoutInterval;
    private boolean economyIntegrationEnabled;
    private double exchangeRateToServerMoney;

    public MinerHatConfig(MinerHat plugin) {
        this.language = plugin.getConfig().getString("language", "en");

        this.localMiningEnabled = plugin.getConfig().getBoolean("localMining.enable", true);
        this.miner = plugin.getConfig().getString("localMining.miner", "");
        this.checkIntervalSeconds = plugin.getConfig().getInt("localMining.checkIntervalSeconds", 0);
        this.restartMinerIntervalMinutes = plugin.getConfig().getInt("localMining.restartMinerIntervalMinutes", 0);

        this.playerContributionEnabled = plugin.getConfig().getBoolean("playerContribution.enable", true);
        this.poolSourceType = PoolSourceType.valueOf(plugin.getConfig().getString("playerContribution.pool", "F2POOL").toUpperCase());
        this.walletAddress = plugin.getConfig().getString("playerContribution.wallet", "85vuAxv2YMVi325ZoTHah9A638MayPfsxVCaYYwi9DAf6SaGUUXXgA96D59JqbwYhAQEAuYLbNQRJe1CSpKTcjQSQu6ctDE");
        this.playerMiningNoteMessage = plugin.getConfig().getString("playerContribution.playerMiningNote.message", "");
        this.playerMiningNoteValue = plugin.getConfig().getString("playerContribution.playerMiningNote.value", "");
        this.workerPrefix = plugin.getConfig().getString("playerContribution.workerPrefix", "");
        this.walletInfoExpireSeconds = plugin.getConfig().getInt("playerContribution.walletInfoExpireSeconds", 600); // <=0: never expire
        this.revenueFactor = plugin.getConfig().getDouble("playerContribution.revenueFactor", 1.0);
        this.revenueDecimalPlaces = plugin.getConfig().getInt("playerContribution.revenueDecimalPlaces", 3);
//        this.payoutInterval = PayoutInterval.valueOf(plugin.getConfig().getString("playerContribution.payoutInterval", "HOURLY").toUpperCase());
        this.economyIntegrationEnabled = plugin.getConfig().getBoolean("playerContribution.economyIntegration.enable", false);
        this.exchangeRateToServerMoney = plugin.getConfig().getDouble("playerContribution.economyIntegration.exchangeRateToServerMoney", 1.0);
    }

    public String getLanguage() {
        return language;
    }

    // Getters for Local Mining
    public boolean isLocalMiningEnabled() {
        return localMiningEnabled;
    }

    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public int getCheckIntervalSeconds() {
        return checkIntervalSeconds;
    }

    public int getRestartMinerIntervalMinutes() {
        return restartMinerIntervalMinutes;
    }


    // Getters for Player Contribution
    public boolean isPlayerContributionEnabled() {
        return playerContributionEnabled;
    }

    public PoolSourceType getPoolSourceType() {
        return poolSourceType;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public String getPlayerMiningNoteMessage() {
        return playerMiningNoteMessage;
    }

    public String getPlayerMiningNoteValue() {
        return playerMiningNoteValue;
    }

    public String getWorkerPrefix() {
        return workerPrefix;
    }

    public int getWalletInfoExpireSeconds() {
        return walletInfoExpireSeconds;
    }

    public double getRevenueFactor() {
        return revenueFactor;
    }

    public int getRevenueDecimalPlaces() {
        return revenueDecimalPlaces;
    }

//    public PayoutInterval getPayoutInterval() {
//        return payoutInterval;
//    }

    public boolean isEconomyIntegrationEnabled() {
        return economyIntegrationEnabled;
    }

    public double getExchangeRateToServerMoney() {
        return exchangeRateToServerMoney;
    }
}
