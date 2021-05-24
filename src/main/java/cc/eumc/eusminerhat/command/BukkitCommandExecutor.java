package cc.eumc.eusminerhat.command;

import cc.eumc.eusminerhat.MinerHat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BukkitCommandExecutor implements CommandExecutor, TabExecutor {
    MinerHat plugin;
    private String adminPermissionNode = "minerhat.admin";
    private String[] commands = {"help", "status", "start", "stop", "policy", "reload"};
    private String[] policySubCommands = {"list", "set"};

    public BukkitCommandExecutor(MinerHat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] strings) {
        if (sender.hasPermission(adminPermissionNode)) {

            // TODO: Admin commands

        } else {
            sender.sendMessage("[MinerHat] Sorry.");
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
