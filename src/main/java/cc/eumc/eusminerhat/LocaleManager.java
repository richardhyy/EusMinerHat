package cc.eumc.eusminerhat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;

public class LocaleManager {
    MinerHat plugin;
    JsonObject languageObject;

    public static LocaleManager createLocaleManager(MinerHat plugin, String language) throws Exception {
        JsonElement jelement = new JsonParser().parse(Files.readString(Path.of(plugin.getLanguagePath() + "/" + language + ".json")));
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("translations");
        return new LocaleManager(plugin, jobject);
    }

    private LocaleManager(MinerHat plugin, JsonObject languageObject) {
        this.plugin = plugin;
        this.languageObject = languageObject;
    }

    public String getLocalized(String token) {
        JsonElement element = languageObject.get(token);
        if (element == null) {
            return String.format("NO TRANSLATION: %s", token);
        }
        return languageObject.get(token).getAsString();
    }
}
