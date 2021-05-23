package cc.eumc.eusminerhat.miner;

import cc.eumc.eusminerhat.MinerHat;
import cc.eumc.eusminerhat.exception.MinerException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class MinerPolicy {
    private String description;
    private int miningPlayerCountCeiling;
    private String startCommand;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMiningPlayerCountCeiling() {
        return miningPlayerCountCeiling;
    }

    public void setMiningPlayerCountCeiling(int miningPlayerCountCeiling) {
        this.miningPlayerCountCeiling = miningPlayerCountCeiling;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }


    public static MinerPolicy loadPolicy(Path filePath) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(String.join( "\n", Files.readAllLines(filePath)), MinerPolicy.class);
    }

//    public MinerPolicy(MinerHat plugin, String minerName) throws Exception {
//        String policyFile = minerName + ".yml";
//        File configFile = new File(plugin.getMinerPath(), policyFile);
//        /*if (!configFile.exists()) {
//            save();
//        }*/
//        FileConfiguration policy = loadConfig(configFile);
//        if (policy == null) {
//            plugin.sendSevere("§c§lFailed loading miner policy: " + plugin.getMinerPath() + "/" + policyFile);
//            throw new MinerException(MinerException.MinerExceptionType.FAILED_LOADING_POLICY, "");
//        } else {
//
//        }
//    }
//
//    private FileConfiguration loadConfig(File file) {
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//
//        return YamlConfiguration.loadConfiguration(file);
//    }
}
