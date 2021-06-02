package cc.eumc.eusminerhat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class LocaleManager {
    MinerHat plugin;
    JsonObject languageObject;

    public static LocaleManager createLocaleManager(MinerHat plugin, String language) throws Exception {
        JsonElement jelement = new JsonParser().parse(new JsonReader(new FileReader(plugin.getLanguagePath() + "/" + language + ".json")));
        JsonObject jobject = jelement.getAsJsonObject();

        if (!compareVersion(jobject)) {
            plugin.sendWarn(String.format("§eLanguage pack %s is not compatible with the current version of plugin.", language + ".json"));
        }

        jobject = jobject.getAsJsonObject("translations");
        return new LocaleManager(plugin, jobject);
    }

    public static boolean checkCompatibility(File languageFile) throws FileNotFoundException {
        JsonElement jelement = new JsonParser().parse(new JsonReader(new FileReader(languageFile)));
        JsonObject jobject = jelement.getAsJsonObject();
        return compareVersion(jobject);
    }

    private static boolean compareVersion(JsonObject languageObject) {
        String packVersion = languageObject.get("version").getAsString();
        return MinerHat.LanguagePackVersion.equals(packVersion);
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
