package cc.eumc.eusminerhat;

public class MinerHatConfig {
    private String language;
    private String miner;
    private int checkInterval;

    public MinerHatConfig(MinerHat plugin) {
        this.language = plugin.getConfig().getString("language", "en");
        this.miner = plugin.getConfig().getString("miner", "");
        this.checkInterval = plugin.getConfig().getInt("checkInterval", 0);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }
}
