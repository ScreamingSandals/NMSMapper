package org.screamingsandals.nms.mapper.utils;

import org.apache.commons.io.IOUtils;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JavadocIndexer {
    private static final Pattern ARRAY_MATCHER = Pattern.compile("\\[.*]");
    // package - module
    private final Map<String, String> packageIndex = new HashMap<>();

    public JavadocIndexer() {
        try {
            var matcher = ARRAY_MATCHER.matcher(IOUtils.toString(new URL("https://docs.oracle.com/en/java/javase/17/docs/api/package-search-index.js"), StandardCharsets.UTF_8));
            if (!matcher.find()) {
                throw new RuntimeException("Couldn't find array!");
            }
            var node = GsonConfigurationLoader.builder().buildAndLoadString(matcher.group());
            for (ConfigurationNode def : node.childrenList()) {
                packageIndex.put(def.node("l").getString(), def.node("m").getString());
            }
        } catch (Exception ignored) {
            // ignored
        }
    }

    public String linkFor(String clazz) {
        var result = packageIndex.get(clazz.substring(0, clazz.lastIndexOf('.')));
        if (result != null) {
            return "https://docs.oracle.com/en/java/javase/17/docs/api/" + result + "/" + clazz.replace('.', '/') + ".html";
        }
        return null;
    }
}
