package cc.eumc.eusminerhat.contribution.source;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.ContributionException;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class PoolSource {
    MinerHat plugin;
    protected String walletAddress;
    Consumer<PoolSource> walletInfoRefreshCallback;

    public String getWalletAddress() {
        return walletAddress;
    }

    PoolSource(MinerHat plugin, String walletAddress, Consumer<PoolSource> walletInfoRefreshCallback) {
        this.plugin = plugin;
        this.walletAddress = walletAddress;
        this.walletInfoRefreshCallback = walletInfoRefreshCallback;
    }

    /**
     * Fetch wallet information. It is intended to be called right after a PoolSource instance was constructed.
     * @param onComplete called with self
     * @param onError called on error
     */
    public abstract void refreshWalletInformation(Consumer<PoolSource> onComplete, Consumer<Exception> onError);

    /**
     * Fetch worker hashrate for the past 24 hours.
     * @param workerName name for the worker
     * @param onComplete called with two arguments: workerName & total hashrate
     * @param onError called on error
     */
    public abstract void getWorkerHashrate24h(String workerName, BiConsumer<String, Integer> onComplete, Consumer<Exception> onError);

    /**
     * Get total hashrate for the past 24 hours.
     * refreshWalletInformation() MUST be called before this can be used.
     * @return total hashrate for the past 24h
     * @throws ContributionException thrown if WalletInformation has not been fetched
     */
    public abstract int getWalletHashrate24h() throws ContributionException;

    /**
     * Get total revenue for the past 24 hours.
     * @return total revenue for the past 24h
     * @throws ContributionException thrown if WalletInformation has not been fetched
     */
    public abstract double getWalletRevenue24h() throws ContributionException;
}
