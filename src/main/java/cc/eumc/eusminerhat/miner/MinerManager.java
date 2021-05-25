package cc.eumc.eusminerhat.miner;

import cc.eumc.eusminerhat.MinerHat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MinerManager {
    MinerHat plugin;
    String name;
    MinerPolicy policy;

    Runnable restartTimer = null;
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
            plugin.sendSevere(String.format(plugin.l("policy.failure.noValidCommands"), name));
            return;
        }
        File miner = new File(plugin.getMinerPath() + "/" + name + "/" + args[0]);
        if (miner.exists()) {
            plugin.sendInfo(String.format(plugin.l("policy.executableExists"), name));
        } else {
            plugin.sendSevere(String.format(plugin.l("policy.failure.minerNotFound"), name, miner));

            File minerDir = new File(plugin.getMinerPath() + "/" + name);
            if (!minerDir.exists()) {
                minerDir.mkdir();
                plugin.sendInfo(String.format(plugin.l("policy.createdFolder"), name, minerDir));
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
     * Get the latest output from the miner.
     * @return output. empty string if the miner has stopped
     */
    public String fetchMinerOutput() {
        if (!getMinerStatus()) {
            return "";
        }

        try {
            int no = out.available();
            if (no > 0) {
                byte[] buffer = new byte[4000];
                int n = out.read(buffer, 0, Math.min(no, buffer.length));
                return new String(buffer, 0, n);
            }
        } catch (IOException ignored) { }
        return "";
    }

    /**
     * Write to miner input stream.
     * @param input input string
     * @return true: success; false: failed
     */
    public boolean writeInputToMiner(String input) {
        if (!getMinerStatus()) {
            return false;
        }

        try {
            in.write(input.getBytes(StandardCharsets.UTF_8));
            in.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
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

            if (plugin.getMinerHatConfig().getRestartMinerIntervalMinutes() > 0) {
                MinerManager currentMinerManager = this;

                this.restartTimer = new Runnable() {
                    final long startTime = System.currentTimeMillis() / 1000;
                    final MinerManager minerManager = currentMinerManager;
                    boolean cancelled = false;

                    @Override
                    public void run() {
                        if (cancelled) { return; }
                        if (minerManager.getMinerStatus() && System.currentTimeMillis() / 1000 - startTime >= plugin.getMinerHatConfig().getRestartMinerIntervalMinutes()) {
                            minerManager.stopMining();
                            minerManager.startMining();
                            cancelled = true;
                        }
                    }
                };
                long interval = plugin.getMinerHatConfig().getRestartMinerIntervalMinutes() * 60L * 20L;
                plugin.getServer().getScheduler().runTaskTimer(plugin, restartTimer, interval, interval);

                plugin.sendInfo(String.format(plugin.l("miner.autoRestart"), plugin.getMinerHatConfig().getRestartMinerIntervalMinutes()));
            }

            plugin.sendInfo(plugin.l("miner.started"));
        } catch (Exception e) {
            e.printStackTrace();
            plugin.sendSevere(String.format(plugin.l("miner.failedStarting"), e.getLocalizedMessage()));
        }
    }

    /**
     * Stop mining if the miner is running.
     */
    public void stopMining() {
        if (getMinerStatus()) {
            minerProcess.destroy();
            minerProcess = null;

            restartTimer = null;

            plugin.sendInfo(plugin.l("miner.stopped"));
        }
    }
}
