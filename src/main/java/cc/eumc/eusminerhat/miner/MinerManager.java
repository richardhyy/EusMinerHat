package cc.eumc.eusminerhat.miner;

import cc.eumc.eusminerhat.MinerHat;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class MinerManager {
    MinerHat plugin;
    String name;
    MinerPolicy policy;

    Process minerProcess = null;
    InputStream out = null;
    OutputStream in = null;

    public MinerManager(MinerHat plugin, String name, MinerPolicy policy) {
        this.plugin = plugin;
        this.name = name;
        this.policy = policy;

        validate();
    }

    void validate() {
        String[] args = getCommandArguments();
        if (args.length == 0) {
            plugin.sendSevere(String.format("§cPolicy for miner %s does not contain valid launch commands.", name));
            return;
        }
        File miner = new File(plugin.getMinerPath() + "/" + name + "/" + args[0]);
        if (miner.exists()) {
            plugin.sendInfo(String.format("§aMiner %s: executable file exists.", name));
        } else {
            plugin.sendSevere(String.format("§cMiner %s cannot be found. (%s)", name, miner));

            File minerDir = new File(plugin.getMinerPath() + "/" + name);
            if (!minerDir.exists()) {
                minerDir.mkdir();
                plugin.sendInfo(String.format("Created folder for miner %s at %s.", name, minerDir));
            }
        }
    }

    /**
     * Get running status of miner
     * @return true: running; false: stopped
     */
    public boolean getMinerStatus() {
        if (minerProcess == null) {
            return false;
        }
        return minerProcess.isAlive();
    }

    /**
     * Whether the server workload satisfy the policy to start mining
     * @return true: satisfied; false: unsatisfied
     */
    public boolean meetMiningCondition() {
        return plugin.getServer().getOnlinePlayers().size() <= policy.getMiningPlayerCountCeiling();
    }

    /**
     * Check if the server meets the policy for mining, and then automatically start or stop the miner.
     * @return true: miner status changed; false: miner status holds
     */
    public boolean checkAndToggleMining() {
        boolean isRunning = getMinerStatus();
        if (meetMiningCondition()) {
            // No need for checking getMinerStatus() here as the method will check for it.
            startMining();
            return !isRunning;
        } else {
            stopMining();
            return isRunning;
        }
    }

    String[] getCommandArguments() {
        // TODO: Support quotes
        return policy.getStartCommand().split(" ");
    }

    /**
     * Start mining if the miner has not been started.
     */
    public void startMining() {
        if (getMinerStatus()) { // check if the miner has already been started
            return;
        }
        try {
            String[] args = getCommandArguments();
            args[0] = plugin.getMinerPath() + "/" + name + "/" + args[0]; // to absolute path
            ProcessBuilder pb = new ProcessBuilder(args);

            pb.directory(new File(plugin.getMinerPath() + "/" + name));
            this.minerProcess = pb.start();
            this.out = minerProcess.getInputStream();
            this.in = minerProcess.getOutputStream();

            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); // TODO: Remove
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            plugin.sendInfo("§aStarted mining.");
        } catch (Exception e) {
            e.printStackTrace();
            plugin.sendSevere("§cError: " + e.getLocalizedMessage());
        }
    }

    /**
     * Stop mining if the miner is running.
     */
    public void stopMining() {
        if (getMinerStatus()) {
            minerProcess.destroy();
        }
    }
}
