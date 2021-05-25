package cc.eumc.eusminerhat.contribution.source;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.ContributionException;
import cc.eumc.eusminerhat.util.HttpRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class F2PoolXMR extends PoolSource {
    private static final String WorkerInfoUrl = "https://api.f2pool.com/monero/%s/%s";
    private static final String WalletInfoUrl = "https://api.f2pool.com/monero/%s";

    JsonObject walletInfoObject = null;

    public F2PoolXMR(MinerHat plugin, String walletAddress) {
        super(plugin, walletAddress);
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
                walletInfoObject = jelement.getAsJsonObject();

                F2PoolXMR source = this;
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

    @Override
    public void getWorkerHashrate24h(String workerName, BiConsumer<String, Integer> onComplete, Consumer<Exception> onError) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try {
                String jsonText = HttpRequest.get(new URL(String.format(WorkerInfoUrl, workerName)))
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asString("UTF-8").trim();
                JsonElement jelement = new JsonParser().parse(jsonText);
                JsonObject workerInfoObject = jelement.getAsJsonObject();

                JsonArray hashrates = workerInfoObject.getAsJsonArray("hashes_last_day");
                int totalHashratePastDay = 0;
                for (JsonElement rate : hashrates) {
                    totalHashratePastDay += rate.getAsInt();
                }

                int finalTotalHashratePastDay = totalHashratePastDay;
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

        return walletInfoObject.getAsJsonObject("hashes_last_day").getAsInt();
    }

    @Override
    public double getWalletRevenue24h() throws ContributionException {
        checkWalletInfoNull();

        return walletInfoObject.getAsJsonObject("value_last_day").getAsDouble();
    }

    private void checkWalletInfoNull() throws ContributionException {
        if (walletInfoObject == null) {
            throw new ContributionException(ContributionException.ContributionExceptionType.WalletInformationNotReady, "Wallet information has not finished fetching.");
        }
    }
}
