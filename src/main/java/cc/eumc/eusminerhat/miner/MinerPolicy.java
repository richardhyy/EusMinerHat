package cc.eumc.eusminerhat.miner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;

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


    public static MinerPolicy loadPolicy(String filePath) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(new JsonReader(new FileReader(filePath)), MinerPolicy.class);
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
