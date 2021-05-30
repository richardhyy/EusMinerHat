package cc.eumc.eusminerhat.contribution;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.contribution.source.F2PoolXMR;
import cc.eumc.eusminerhat.contribution.source.PoolSource;
import cc.eumc.eusminerhat.contribution.source.PoolSourceType;
import cc.eumc.eusminerhat.exception.ContributionException;
import cc.eumc.eusminerhat.util.Timestamp;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ContributorManager {
    MinerHat plugin;
    PoolSource poolSource;

    private static final String ServerErrorMessageToken = "message.contribution.checkoutServerError";

    private HashMap<String, Boolean> checkoutWorkingInProgress = new HashMap<>();
    private HashMap<UUID, PlayerContribution> playerContributionCache = new HashMap<>();

    public ContributorManager(MinerHat plugin, PoolSourceType poolSourceType) {
        this.plugin = plugin;

        switch (poolSourceType) {
            case F2POOL:
                this.poolSource = new F2PoolXMR(plugin, plugin.getMinerHatConfig().getWalletAddress(), this::walletInfoRefreshCallback);
                break;
            // TODO: more mining pool supports
        }
    }

    private void walletInfoRefreshCallback(PoolSource poolSource) {
        try {
            plugin.sendInfo(String.format(plugin.l("contribution.wallet.information"),
                    poolSource.getWalletAddress(),
                    poolSource.getWalletHashrate24h(),
                    poolSource.getWalletRevenue24h()));
        } catch (ContributionException e) {
            e.printStackTrace();
        }
    }

    public void checkoutRevenue24h(Player player, BiConsumer<Double, Double> onSuccess, Consumer<Exception> onError) {
        UUID playerUUID = player.getUniqueId();
        String uuidStr = playerUUID.toString();
        String trimmed = plugin.getMinerHatConfig().getWorkerPrefix() + uuidStr.substring(0, 6) + uuidStr.substring(26, 32);

        if (checkoutWorkingInProgress.getOrDefault(trimmed, false)) {
            onError.accept(new ContributionException(ContributionException.ContributionExceptionType.CHECKOUT_WORKING_IN_PROGRESS, plugin.l("message.contribution.checkoutWorkingInProgress")));
            return;
        }

        if (this.poolSource == null) {
            onError.accept(new ContributionException(ContributionException.ContributionExceptionType.WALLET_INFORMATION_NOT_READY, plugin.l("message.contribution.serverNotReady")));
            return;
        }

        checkoutWorkingInProgress.put(trimmed, true);
        this.poolSource.getWorkerHashrate24h(trimmed, (worker, workerHashrate) -> {
            checkoutWorkingInProgress.put(trimmed, false);

            // Validate time interval between last checkout
            // TODO: Checkout that does not require halt
            Map<Long, Integer> hashrateHistory = getPlayerContribution(player).getHashrateHistoryCopy();
            if (hashrateHistory.size() > 0) {
                final long[] lastTimestamp = {0};
                hashrateHistory.forEach((secondsSince1970, hashrate) -> {
                    if (secondsSince1970 > lastTimestamp[0]) {
                        lastTimestamp[0] = secondsSince1970;
                    }
                });
                // If time interval is less than one day
                if (Timestamp.getSecondsSince1970() - lastTimestamp[0] < 3600 * 24) {
                    onError.accept(new ContributionException(ContributionException.ContributionExceptionType.CHECKOUT_FAILED,
                            "Checkout interval is less than one day"));
                    return;
                }
            }

            // Checkout player contribution
            try {
                int walletHashrate = this.poolSource.getWalletHashrate24h();
                double walletRevenue = this.poolSource.getWalletRevenue24h();
                double playerPercentage = (double)workerHashrate / (double)walletHashrate;
                double playerShare = walletRevenue * playerPercentage;
                depositPlayerRevenue(player, playerShare);
                recordPlayerHashrate(player, walletHashrate);
                onSuccess.accept(getPlayerRevenue(player), playerShare);
            } catch (ContributionException ex) {
                this.poolSource.refreshWalletInformation((ps) -> {

                }, exception -> {
                    exception.printStackTrace();
                });
                onError.accept(ex);
            }
        }, (exception -> {
            checkoutWorkingInProgress.put(trimmed, false);
            onError.accept(exception);
        }));
    }

    public PlayerContribution getPlayerContribution(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerContributionCache.containsKey(uuid)) {
            return playerContributionCache.get(uuid);
        } else {
            PlayerContribution contribution = PlayerContribution.getPlayerContribution(plugin, player);
            playerContributionCache.put(uuid, contribution);
            return contribution;
        }
    }

    private void depositPlayerRevenue(Player player, double amount) {
        getPlayerContribution(player).deposit(amount);
    }

    private void setPlayerRevenue(Player player, double amount) {
        getPlayerContribution(player).setRevenue(amount);
    }

    private void recordPlayerHashrate(Player player, int hashrate) {
        getPlayerContribution(player).appendHashrate(hashrate);
    }

    /**
     * Get revenue in a player's wallet
     * @param player
     * @return amount of revenue
     */
    public double getPlayerRevenue(Player player) {
        return getPlayerContribution(player).getRevenue();
    }

    /**
     * Get accumulative revenue
     * @param player
     * @return amount of accumulative revenue
     */
    public double getAccumulativeRevenue(Player player) {
        return getPlayerContribution(player).getAccumulativeRevenue();
    }

    /**
     * Withdraw revenue from a player's wallet
     * @param player
     * @param amount amount of revenue
     * @return amount of revenue remaining after withdrawing
     * @throws ContributionException
     */
    public double withdrawPlayerRevenue(Player player, double amount) throws ContributionException {
        return getPlayerContribution(player).withdraw(amount);
    }
}
