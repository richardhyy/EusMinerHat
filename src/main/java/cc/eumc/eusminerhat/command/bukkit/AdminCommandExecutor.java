package cc.eumc.eusminerhat.command.bukkit;

import cc.eumc.eusminerhat.MinerHat;
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
                        break;
                    case "log":
                        break;
                    case "start":
                        break;
                    case "stop":
                        break;
                    case "reload":
                        break;
                    case "help":
                        // TODO: Help message
                    default:
                        // TODO: Command not found message.
                }
            }
        } else {
            sender.sendMessage(plugin.l("message.header") + plugin.l("message.permissionDenied"));
        }
        return true;
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
