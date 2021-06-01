package cc.eumc.eusminerhat.command.bukkit;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.ContributionException;
import cc.eumc.eusminerhat.util.Timestamp;
import net.md_5.bungee.api.chat.*;
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
    private final String[] commands = { "help", "check", "revenue", "history", "mining", "exchange" };

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
            Player player = (Player) sender;
            if (args.length == 1) {

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
                                    StringBuilder entry = new StringBuilder();
                                    entry.append(Timestamp.toFormattedTime(timeInterval));
                                    entry.append("§7 | ");
                                    entry.append(amount >= 0 ? "§2+§r" : "§4-§r");
                                    entry.append(Math.abs(amount));
                                    entry.append("\n");
                                    historyMessage.insert(0, entry);
                                });
                        sendMessage(sender, String.format(plugin.l("message.command.contribution.revenueHistory"), historyMessage));
                        break;

                    case "mining":
                        sendMessage(sender, plugin.l("message.command.contribution.miningInformation.header"));

                        String workerName = plugin.getContributorManager().getWorkerNameFor(player);

                        if (!plugin.getMinerHatConfig().getPlayerMiningNoteMessage().isEmpty()) {
                            sendCopyableMessage(player, plugin.parseFormatCode(plugin.getMinerHatConfig().getPlayerMiningNoteMessage()
                                    .replace("{address}", plugin.getContributorManager().getPoolSource().getWalletAddress())
                                    .replace("{worker}", workerName)),
                                    plugin.getMinerHatConfig().getPlayerMiningNoteValue());
                        }

                        sendCopyableMessage(player, String.format(plugin.l("message.command.contribution.miningInformation.address"),
                                plugin.getContributorManager().getPoolSource().getCryptocurrencyName(),
                                plugin.getContributorManager().getPoolSource().getWalletAddress()),
                                plugin.getContributorManager().getPoolSource().getWalletAddress());

                        sendCopyableMessage(player, String.format(plugin.l("message.command.contribution.miningInformation.worker"),
                                plugin.getContributorManager().getPoolSource().getCryptocurrencyName(),
                                workerName),
                                workerName);

                        break;

                    case "exchange": // without the second arg, show exchange help
                        if (!plugin.getMinerHatConfig().isEconomyIntegrationEnabled() || plugin.getEconomy() == null) {
                            sendMessage(player, plugin.l("message.command.contribution.exchange.notEnabled"));
                        } else {
                            sendMessage(player, plugin.l("message.command.contribution.exchange.argumentSuggestion"));
                        }
                        break;

                    case "help":
                        sendMessage(sender, plugin.l("message.command.contribution.help"));
                        break;

                    default:
                        sendMessage(sender, plugin.l("message.command.notFound"));
                }

            } else if (args.length == 2) {

                switch (args[0].toLowerCase()) {
                    case "exchange": // exchange command with amount
                        if (!plugin.getMinerHatConfig().isEconomyIntegrationEnabled() || plugin.getEconomy() == null) {
                            sendMessage(player, plugin.l("message.command.contribution.exchange.notEnabled"));
                        } else {
                            try {
                                double amount = Double.parseDouble(args[1]);
                                if (amount <= 0) {
                                    throw new NumberFormatException("Exchange amount must be greater than zero.");
                                }

                                // withdraw from revenue account
                                // if there's not enough revenue, an exception would be thrown
                                plugin.getContributorManager().withdrawPlayerRevenue(player, amount);

                                double convertedAmount = amount * plugin.getMinerHatConfig().getExchangeRateToServerMoney();
                                plugin.getEconomy().depositPlayer(player, convertedAmount);

                                sendMessage(player, String.format(plugin.l("message.command.contribution.exchange.success"),
                                                    amount, plugin.getEconomy().format(convertedAmount)));
                            } catch (NumberFormatException ex) {
                                sendMessage(player, plugin.l("message.command.contribution.exchange.illegalAmount"));
                            } catch (ContributionException ex) {
                                if (ex.getType() == ContributionException.ContributionExceptionType.NOT_ENOUGH_REVENUE) {
                                    sendMessage(player, plugin.l("message.command.contribution.exchange.notEnoughRevenue"));
                                } else {
                                    sendMessage(player, String.format(plugin.l("message.command.contribution.exchange.serverError"), ex.getMessage()));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace(); // log the unexpected exception
                                sendMessage(player, String.format(plugin.l("message.command.contribution.exchange.serverError"), ex.getMessage()));
                            }
                        }
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

    private void sendMessage(Player player, TextComponent component) {
        player.spigot().sendMessage(component);
    }

    private void sendCopyableMessage(Player player, String message, String value) {
        BaseComponent[] clickToCopy =  new ComponentBuilder(plugin.l("message.command.contribution.miningInformation.clickToCopy")).create();
        TextComponent workerMessage = new TextComponent(message);
        workerMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value));
        workerMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, clickToCopy));
        sendMessage(player, workerMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(playerPermissionNode)) return new ArrayList<>();

        if (args.length > 1)
            return new ArrayList<>();
        else if (args.length == 1)
            return Arrays.stream(commands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else
            return Arrays.asList(commands);
    }
}
