package org.screamingsandals.nms.mapper.tasks;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.parser.MojangMappingParser;
import org.screamingsandals.nms.mapper.parser.SeargeMappingParser;
import org.screamingsandals.nms.mapper.parser.SpigotMappingParser;
import org.screamingsandals.nms.mapper.parser.VanillaJarParser;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public abstract class RemappingTask extends DefaultTask {
    @Input
    public abstract Property<String> getVersion();

    @Input
    public abstract Property<UtilsHolder> getUtils();

    @Input
    @Optional
    public abstract Property<String> getMcpBuild();

    @SneakyThrows
    @TaskAction
    public void run() {
        var version = getVersion().get();
        var utils = getUtils().get();

        var caching = utils.getCaching();
        var mappings = utils.getMappings();
        var versionManifestAtomic = utils.getVersionManifest();
        var newlyGeneratedMappings = utils.getNewlyGeneratedMappings();

        System.out.println("======= Mapping " + version + " =======");

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

        System.out.println("Getting base data from vanilla jar ....");

        var entry = VanillaJarParser.map(
            caching.getFile(
                            new URI(Objects.requireNonNull(n.node("downloads", "server", "url").getString())),
                                    "minecraft_server." + version + ".jar")
        );

        var mapping = entry.getKey();
        var excluded = entry.getValue();

        System.out.println(excluded.size() + " symbols (fields, methods) excluded from final mapping: synthetic");
        System.out.println(mapping.size() + " classes mapped");

        mappings.put(version, mapping);

        var defaultMappings = MappingType.SPIGOT;

        if (!n.node("downloads", "server_mappings", "url").empty()) {
            System.out.println("Applying Mojang mapping ....");
            defaultMappings = MappingType.MOJANG;

            var errors = MojangMappingParser.map(
                    mapping,
                    new FileInputStream(caching.getFile(
                            new URI(Objects.requireNonNull(n.node("downloads", "server_mappings", "url").getString())),
                            "mojang-" + version + ".mapping")),
                    excluded
            );

            if (errors > 0) {
                System.out.println(errors + " symbols (fields, methods) not found but they are not excluded");
            }
        }

        if (getMcpBuild().isPresent()) {
            System.out.println("Applying Searge (Forge) mapping ....");

            var errors = SeargeMappingParser.map(mapping, version, getMcpBuild().get(), caching, excluded);

            if (errors > 0) {
                System.out.println(errors + " symbols (fields, methods) not found but they are not excluded");
            }
        }

        System.out.println("Applying Spigot mapping ....");
        var errors = SpigotMappingParser.mapTo(version, mapping, caching, excluded);

        if (errors > 0) {
            System.out.println(errors + " symbols (fields, methods) not found but they are not excluded");
        }

        // TODO: Yarn

        newlyGeneratedMappings.put(version, defaultMappings);
    }
}
