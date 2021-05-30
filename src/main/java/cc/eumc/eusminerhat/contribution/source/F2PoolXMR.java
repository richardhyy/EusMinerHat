package cc.eumc.eusminerhat.contribution.source;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.ContributionException;
import cc.eumc.eusminerhat.util.HttpRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class F2PoolXMR extends PoolSource {
    private static final String WorkerInfoUrl = "https://api.f2pool.com/monero/%s/%s";
    private static final String WalletInfoUrl = "https://api.f2pool.com/monero/%s";

    long walletInfoLastUpdateSecondsSince1970 = 0;
    JsonObject walletInfoObject = null;

    public F2PoolXMR(MinerHat plugin, String walletAddress, Consumer<PoolSource> walletInfoRefreshCallback) {
        super(plugin, walletAddress, "XMR", walletInfoRefreshCallback);
    }

    @Override
    public void refreshWalletInformation(Consumer<PoolSource> onComplete, Consumer<Exception> onError) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try {
                String jsonText = HttpRequest.get(new URL(String.format(WalletInfoUrl, this.walletAddress)))
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asString("UTF-8").trim();
                JsonElement jelement = new JsonParser().parse(jsonText);
                this.walletInfoObject = jelement.getAsJsonObject();
                this.walletInfoLastUpdateSecondsSince1970 = System.currentTimeMillis() / 1000;

                F2PoolXMR source = this; // For future multiple pools support
                if (this.walletInfoRefreshCallback != null) {
                    this.walletInfoRefreshCallback.accept(source);
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        onComplete.accept(source);
                    }
                }.runTask(plugin);

            } catch (Exception e) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        onError.accept(e);
                    }
                }.runTask(plugin);
            }
        }, 1);
    }

    private boolean checkWalletInfoExpires() {
        if (plugin.getMinerHatConfig().getWalletInfoExpireSeconds() <= 0) { // Never expires
            return false;
        }

        return System.currentTimeMillis() / 1000 - walletInfoLastUpdateSecondsSince1970 > plugin.getMinerHatConfig().getWalletInfoExpireSeconds();
    }

    @Override
    public void getWorkerHashrate24h(String workerName, BiConsumer<String, Integer> onComplete, Consumer<Exception> onError) {
        if (checkWalletInfoExpires()) { // refresh wallet info if expired
            refreshWalletInformation(poolSource -> {
                runGetWorkerHashrateTask(workerName, onComplete, onError);
            }, exception -> {
                onError.accept(new ContributionException(ContributionException.ContributionExceptionType.WALLET_INFORMATION_NOT_READY, "Error fetching wallet information"));
            });
        } else {
            runGetWorkerHashrateTask(workerName, onComplete, onError);
        }
    }

    private void runGetWorkerHashrateTask(String workerName, BiConsumer<String, Integer> onComplete, Consumer<Exception> onError) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try {
                String jsonText = HttpRequest.get(new URL(String.format(WorkerInfoUrl, walletAddress, workerName)))
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asString("UTF-8").trim();
                JsonElement jelement = new JsonParser().parse(jsonText);
                JsonObject workerInfoObject = jelement.getAsJsonObject();

                Set<Map.Entry<String, JsonElement>> hashrates = workerInfoObject.getAsJsonObject("hashrate_history").entrySet();
                int totalHashratePastDay = 0;
                for (Map.Entry<String, JsonElement> hashrateKV : hashrates) {
                    totalHashratePastDay += hashrateKV.getValue().getAsInt();
                }

                Set<Map.Entry<String, JsonElement>> hashrateStale = workerInfoObject.getAsJsonObject("hashrate_history_stale").entrySet();
                for (Map.Entry<String, JsonElement> staleKV : hashrateStale) {
                    totalHashratePastDay -= staleKV.getValue().getAsInt();
                }

                int finalTotalHashratePastDay = Math.max(totalHashratePastDay, 0);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        onComplete.accept(workerName, finalTotalHashratePastDay);
                    }
                }.runTask(plugin);

            } catch (Exception e) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        onError.accept(e);
                    }
                }.runTask(plugin);
            }
        }, 1);
    }

    @Override
    public int getWalletHashrate24h() throws ContributionException {
        checkWalletInfoNull();

        return walletInfoObject.get("hashes_last_day").getAsInt();
    }

    @Override
    public double getWalletRevenue24h() throws ContributionException {
        checkWalletInfoNull();

        return walletInfoObject.get("value_last_day").getAsDouble();
    }

    private void checkWalletInfoNull() throws ContributionException {
        if (walletInfoObject == null) {
            throw new ContributionException(ContributionException.ContributionExceptionType.WALLET_INFORMATION_NOT_READY, "Wallet information has not finished fetching.");
        }
    }
}
