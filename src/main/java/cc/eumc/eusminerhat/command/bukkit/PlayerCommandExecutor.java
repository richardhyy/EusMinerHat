package cc.eumc.eusminerhat.command.bukkit;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.ContributionException;
import cc.eumc.eusminerhat.util.Timestamp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerCommandExecutor implements CommandExecutor, TabExecutor {
    MinerHat plugin;
    private final String playerPermissionNode = "minerhat.contributor";
    private final String[] commands = {"help", "check", "revenue", "history"};

    public PlayerCommandExecutor(MinerHat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!plugin.getMinerHatConfig().isPlayerContributionEnabled()) {
            sendMessage(sender, plugin.l("message.command.contribution.notEnabled"));
            return true;
        }
        if (!(sender instanceof Player)) {
            sendMessage(sender, plugin.l("message.command.playerOnly"));
            return true;
        }
        if (sender.hasPermission(playerPermissionNode)) {
            if (args.length == 1) {
                Player player = (Player) sender;
                switch (args[0].toLowerCase()) {
                    case "check":
                        sendMessage(sender, plugin.l("message.command.contribution.checkoutStarted"));
                        plugin.getContributorManager().checkoutRevenue24h(player, (total, delta) -> {
                            sendMessage(sender, String.format(plugin.l("message.command.contribution.checkoutSuccess"), total, delta));
                        }, (exception) -> {
                            if (exception instanceof ContributionException
                                    && ((ContributionException)exception).getType() == ContributionException.ContributionExceptionType.CHECKOUT_WORKING_IN_PROGRESS
                            ) {
                                    sendMessage(sender, plugin.l("message.command.contribution.checkoutWorkingInProgress"));
                            } else {
                                sendMessage(sender, String.format(plugin.l("message.command.contribution.checkoutServerError"), exception.getMessage()));
                            }
                        });
                        break;
                    case "revenue":
                        sendMessage(sender, String.format(plugin.l("message.command.contribution.revenue"),
                                plugin.getContributorManager().getPlayerRevenue(player),
                                plugin.getContributorManager().getAccumulativeRevenue(player)));
                        break;

                    case "history":
                        StringBuilder historyMessage = new StringBuilder();
                        plugin.getContributorManager().getPlayerContribution(player).getRevenueChangeHistoryCopy()
                                .forEach((timeInterval, amount) -> {
                                    historyMessage.append(Timestamp.toFormattedTime(timeInterval));
                                    historyMessage.append("§7 | ");
                                    historyMessage.append(amount >= 0 ? "§2+§r" : "§4-§r");
                                    historyMessage.append(amount);
                                    historyMessage.append("\n");
                                });
                        sendMessage(sender, String.format(plugin.l("message.command.contribution.revenueHistory"), historyMessage));
                        break;

                    case "help":
                        sendMessage(sender, plugin.l("message.command.contribution.help"));
                        break;

                    default:
                        sendMessage(sender, plugin.l("message.command.notFound"));
                }
            }
        } else {
            sendMessage(sender, plugin.l("message.command.permissionDenied"));
        }
        return true;
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(plugin.prefixForEachLine(message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(playerPermissionNode)) return new ArrayList<>();

        if (args.length > 2)
            return new ArrayList<>();
        else if (args.length == 1)
            return Arrays.stream(commands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else
            return Arrays.asList(commands);
    }
}
