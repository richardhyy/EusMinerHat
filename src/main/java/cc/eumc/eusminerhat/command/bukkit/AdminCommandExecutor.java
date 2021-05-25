package cc.eumc.eusminerhat.command.bukkit;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.MinerException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommandExecutor implements CommandExecutor, TabExecutor {
    MinerHat plugin;
    private String adminPermissionNode = "minerhat.admin";
    private String[] commands = {"help", "status", "log", "start", "stop", "policy", "reload"};
    private String[] policySubCommands = {"list", "set"};

    public AdminCommandExecutor(MinerHat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission(adminPermissionNode)) {
            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "status":
                        sendMessage(sender, String.format(plugin.l("miner.info.name"), plugin.getMinerHatConfig().getMiner()));
                        sendMessage(sender, String.format(plugin.l("miner.info.checkInterval"), plugin.getMinerHatConfig().getCheckIntervalSeconds()));
                        if (plugin.getMinerManager() == null) {
                            sendMessage(sender, plugin.l("miner.managerNotCreated"));
                        } else {
                            sendMessage(sender, String.format(plugin.l("miner.info.status"),
                                    plugin.getMinerManager().getMinerStatus() ? plugin.l("miner.status.mining") : plugin.l("miner.status.stopped")));
                        }
                        break;

                    case "log":
                        if (plugin.getMinerManager() == null) {
                            sendMessage(sender, plugin.l("miner.managerNotCreated"));
                        } else {
                            if (plugin.getMinerManager().getMinerStatus()) {
                                sendMessage(sender, String.format(plugin.l("message.command.admin.log.logFollows"), plugin.getMinerManager().fetchMinerOutput()));
                            } else {
                                sendMessage(sender, plugin.l("message.command.admin.log.noLog"));
                            }
                        }
                        break;

                    case "start":
                        if (plugin.getMinerManager() == null) {
                            sendMessage(sender, plugin.l("miner.managerNotCreated"));
                        } else {
                            if (plugin.getMinerManager().getMinerStatus()) {
                                sendMessage(sender, plugin.l("message.command.admin.start.already"));
                            } else {
                                plugin.getMinerManager().startMining();
                                sendMessage(sender, plugin.l("message.command.admin.start.started"));
                            }
                        }
                        break;

                    case "stop":
                        // TODO: Disable auto mining control for time interval
                        if (plugin.getMinerManager() == null) {
                            sendMessage(sender, plugin.l("miner.managerNotCreated"));
                        } else {
                            if (plugin.getMinerManager().getMinerStatus()) {
                                plugin.getMinerManager().stopMining();
                                sendMessage(sender, plugin.l("message.command.admin.stop.stopped"));
                            } else {
                                sendMessage(sender, plugin.l("message.command.admin.stop.notRunning"));
                            }
                        }
                        break;

                    case "reload":
                        sendMessage(sender, plugin.l("message.command.admin.reload.reloading"));
                        plugin.loadMinerHatConfig();
                        try {
                            plugin.registerMiner();
                            sendMessage(sender, plugin.l("message.command.admin.reload.success"));
                        } catch (MinerException e) {
                            sendMessage(sender, String.format(plugin.l("message.command.admin.reload.failed"), e.getMessage()));
                        }
                        break;

                    case "help":
                        sendMessage(sender, plugin.l("message.command.admin.help"));
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
        if (!sender.hasPermission(adminPermissionNode)) return new ArrayList<>();

        if (args.length > 2)
            return new ArrayList<>();
        else if (args.length == 2)
            if (args[0].equalsIgnoreCase("policy"))
                // Fix: Tab complete won't work for sub-commands
                return Arrays.stream(policySubCommands).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
            else
                return new ArrayList<>();
        else if (args.length == 1)
            return Arrays.stream(commands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else
            return Arrays.asList(commands);
    }
}
