package cc.eumc.eusminerhat.contribution;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.ContributionException;
import cc.eumc.eusminerhat.util.Timestamp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
public class PlayerContribution {
    private transient MinerHat plugin;
    private transient String filePath;

    private String playerName;
    private double revenue;
    private double accumulativeRevenue;
    private Map<Long, Integer> hashrateHistory;
    private Map<Long, Double> revenueChangeHistory;

    public String getPlayerName() {
        return playerName;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getAccumulativeRevenue() {
        return accumulativeRevenue;
    }

    public Map<Long, Integer> getHashrateHistoryCopy() {
        return new HashMap<>(hashrateHistory);
    }

    public Map<Long, Double> getRevenueChangeHistoryCopy() {
        return new HashMap<>(revenueChangeHistory);
    }

    private void setJsonFilePath(String filePath) {
        this.filePath = filePath;
    }

    private void setPlayerName(String name) {
        this.playerName = name;
    }

    private void setPlugin(MinerHat plugin) {
        this.plugin = plugin;
    }

    private PlayerContribution(String filePath, String playerName) {
        this.filePath = filePath;
        this.playerName = playerName;
        this.revenue = 0;
        this.accumulativeRevenue = 0;
        this.hashrateHistory = new HashMap<>();
        this.revenueChangeHistory = new HashMap<>();
    }

    /**
     * Deposit revenue
     * @param amount amount to deposit
     * @return total available revenue
     */
    protected double deposit(double amount) {
        this.revenue += amount;
        this.accumulativeRevenue += amount;
        revenueChangeHistory.put(Timestamp.getSecondsSince1970(), amount);
        save();
        return this.revenue;
    }

    /**
     * Withdraw revenue
     * @param amount amount to withdraw
     * @return
     * @throws ContributionException
     */
    protected double withdraw(double amount) throws ContributionException {
        if (this.revenue < amount) {
            throw new ContributionException(ContributionException.ContributionExceptionType.NOT_ENOUGH_REVENUE, "Not enough revenue for player " + playerName);
        }
        this.revenue -= amount;
        revenueChangeHistory.put(Timestamp.getSecondsSince1970(), -amount);
        save();
        return this.revenue;
    }

    protected void setRevenue(double amount) {
        this.revenue = amount;
    }

    protected void appendHashrate(int hashrate) {
        hashrateHistory.put(Timestamp.getSecondsSince1970(), hashrate);
        save();
    }

    protected void save() {
        File contributionDir = new File(plugin.getPlayerContributionPath());
        if (!contributionDir.exists()) {
            contributionDir.mkdir();
        }

        try (Writer writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(this, writer);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.sendSevere(String.format(plugin.l("contribution.failedSavingPlayerData"), playerName));
        }
    }

    protected static PlayerContribution getPlayerContribution(MinerHat plugin, Player player) {
        Gson gson = new Gson();
        PlayerContribution instance;
        String path = plugin.getPlayerContributionPath() + "/" + player.getUniqueId() + ".json";
        try {
            instance = gson.fromJson(new JsonReader(new FileReader(path)),
                    PlayerContribution.class);
            instance.setJsonFilePath(path);
            instance.setPlayerName(player.getName());
        } catch (Exception e) {
            instance = new PlayerContribution(path, player.getName());
            plugin.sendInfo(String.format(plugin.l("contribution.playerDataNotExist"), player.getName()));
        }
        instance.setPlugin(plugin);
        instance.save();
        return instance;
    }
}
