package org.screamingsandals.nms.mapper.tasks;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.parser.MojangMappingParser;
import org.screamingsandals.nms.mapper.parser.SpigotMappingParser;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public abstract class RemappingTask extends DefaultTask {
    @Input
    public abstract Property<String> getVersion();

    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        var version = getVersion().get();
        var utils = getUtils().get();

        var caching = utils.getCaching();
        var mappings = utils.getMappings();
        var versionManifestAtomic = utils.getVersionManifest();
        var newlyGeneratedMappings = utils.getNewlyGeneratedMappings();

        System.out.println("Mapping for version " + version);

        var n = GsonConfigurationLoader.builder()
                .source(() -> new BufferedReader(new StringReader(
                        caching.loadData(() -> {
                            System.out.println("Obtaining https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");

                            try {
                                var versionManifest = versionManifestAtomic.get();
                                if (versionManifest == null) {
                                    versionManifest = GsonConfigurationLoader.builder()
                                            .url(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"))
                                            .build()
                                            .load();
                                    versionManifestAtomic.set(versionManifest);
                                }
                                return new URI(Objects.requireNonNull(
                                        versionManifest
                                                .node("versions")
                                                .childrenList()
                                                .stream()
                                                .filter(node -> node.node("id").getString().equals(version))
                                                .findFirst()
                                                .orElseThrow()
                                                .node("url")
                                                .getString()
                                ));
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        }, "client-" + version + ".json"))
                ))
                .build()
                .load();

        var mapping = MojangMappingParser.map(
                caching.loadData(
                        new URI(Objects.requireNonNull(n.node("downloads", "server_mappings", "url").getString())),
                        "mojang-" + version + ".mapping")
        );

        mappings.put(version, mapping);

        System.out.println("Processing version " + version + " ....");

        System.out.println("Getting spigot mapping for " + version);
        SpigotMappingParser.mapTo(version, mapping, caching);

        // TODO: MCP
        // TODO: Yarn

        newlyGeneratedMappings.add(version);
    }
}
