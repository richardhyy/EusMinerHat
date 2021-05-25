package cc.eumc.eusminerhat.contribution;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.contribution.source.F2PoolXMR;
import cc.eumc.eusminerhat.contribution.source.PoolSource;
import cc.eumc.eusminerhat.contribution.source.PoolSourceType;
import cc.eumc.eusminerhat.exception.ContributionException;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class ContributorManager {
    MinerHat plugin;
    PoolSource poolSource;

    HashMap<String, Boolean> checkoutWorkingInProgress = new HashMap<>();

    public ContributorManager(MinerHat plugin, PoolSourceType poolSourceType) {
        this.plugin = plugin;

        switch (poolSourceType) {
            case F2POOL:
                this.poolSource = new F2PoolXMR(plugin, plugin.getMinerHatConfig().getWalletAddress());
                break;
            // TODO: more mining pool supports
        }
    }

    public void checkoutRevenue24h(UUID playerUUID, Consumer<Integer> callback, Consumer<ContributionException> onError) {
        String uuidStr = playerUUID.toString();
        String trimmed = uuidStr.substring(0, 6) + uuidStr.substring(26, 32);

        if (checkoutWorkingInProgress.getOrDefault(trimmed, false)) {
            onError.accept(new ContributionException(ContributionException.ContributionExceptionType.CheckoutWorkingInProgress, plugin.l("message.contribution.checkoutWorkingInProgress")));
            return;
        }

        if (this.poolSource == null) {
            onError.accept(new ContributionException(ContributionException.ContributionExceptionType.WalletInformationNotReady, plugin.l("message.contribution.serverNotReady")));
            return;
        }

        checkoutWorkingInProgress.put(trimmed, true);
        this.poolSource.getWorkerHashrate24h(trimmed, (worker, revenue) -> {
            checkoutWorkingInProgress.put(trimmed, false);

            // TODO: checkout player contribution

        }, (exception -> {
            checkoutWorkingInProgress.put(trimmed, false);
        }));
    }
}
