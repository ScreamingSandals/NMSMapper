package org.screamingsandals.nms.mapper.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.extension.Version;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public abstract class ConfigGenerationTask extends DefaultTask {
    @Input
    public abstract Property<File> getConfigFolder();

    @TaskAction
    public void run() {
        var versionObj = getProject().getProperties().get("minecraftVersion");

        if (versionObj == null) {
            throw new GradleException("Minecraft version is not specified! Use ./gradlew generateNmsConfig -PminecraftVersion=<version> [-PcustomVersionString=<customVersion>]");
        }

        var customName = getProject().getProperties().get("customVersionString");
        if (customName == null) {
            customName = versionObj;
        }

        var customVersion = customName.toString();

        var version = versionObj.toString();

        System.out.println("Generating new config for version " + version + " (will be saved as " + customName + ")");

        var httpClient = HttpClient.newHttpClient();

        System.out.println("Obtaining https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");

        try {
            var versionManifest = GsonConfigurationLoader.builder()
                    .url(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"))
                    .build()
                    .load();

            var url = new URL(Objects.requireNonNull(
                    versionManifest
                            .node("versions")
                            .childrenList()
                            .stream()
                            .filter(node -> version.equals(node.node("id").getString()))
                            .findFirst()
                            .orElseThrow()
                            .node("url")
                            .getString()
            ));


            var n = GsonConfigurationLoader.builder()
                    .url(url)
                    .build()
                    .load();

            var vanillaJar = n.node("downloads", "server", "url");
            var vanillaJarSha1 = n.node("downloads", "server", "sha1");
            if (vanillaJar.empty()) {
                throw new RuntimeException("Can't generate config without valid vanillaJar");
            }
            if (vanillaJarSha1.empty()) {
                System.out.println("WARN: Vanilla jar doesn't have sha1!");
            }

            var versionBuilder = Version.builder()
                    .version(version)
                    .vanillaJar(Version.DownloadableContent.builder()
                            .url(vanillaJar.getString())
                            .sha1(vanillaJarSha1.getString())
                            .build()
                    );

            if (!customVersion.equals(version)) {
                versionBuilder.version(customVersion)
                        .realVersion(version);
            }

            var mojangMappings = n.node("downloads", "server_mappings", "url");
            var mojangMappingsSha1 = n.node("downloads", "server_mappings", "sha1");
            if (!mojangMappings.empty()) {
                System.out.println("Mojang Mappings found: " + mojangMappings.getString());
                if (mojangMappingsSha1.empty()) {
                    System.out.println("WARN: Mojang Mappings don't have sha1!");
                }
                versionBuilder
                        .mojangMappings(Version.DownloadableContent.builder()
                                .url(mojangMappings.getString())
                                .sha1(mojangMappingsSha1.getString())
                                .build()
                        );
            }

            var seargeUrl = new URI("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/" + version + "/mcp_config-" + version + ".zip.sha1");

            var seargeSha1 = httpClient.send(HttpRequest.newBuilder().uri(seargeUrl).build(), HttpResponse.BodyHandlers.ofString());

            if (seargeSha1.statusCode() < 400 && seargeSha1.statusCode() >= 200) {
                System.out.println("MCP Mappings found: https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/" + version + "/mcp_config-" + version + ".zip");
                versionBuilder
                        .seargeMappings(Version.DownloadableContent.builder()
                                .url("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/" + version + "/mcp_config-" + version + ".zip")
                                .sha1(seargeSha1.body())
                                .build()
                        );
            } else {
                seargeUrl = new URI("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/" + version + "/mcp-" + version + "-srg.zip.sha1");

                seargeSha1 = httpClient.send(HttpRequest.newBuilder().uri(seargeUrl).build(), HttpResponse.BodyHandlers.ofString());

                if (seargeSha1.statusCode() < 400 && seargeSha1.statusCode() >= 200) {
                    System.out.println("MCP Mappings found: https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/" + version + "/mcp-" + version + "-srg.zip");
                    versionBuilder
                            .seargeMappings(Version.DownloadableContent.builder()
                                    .url("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/" + version + "/mcp-" + version + "-srg.zip")
                                    .sha1(seargeSha1.body())
                                    .build()
                            );
                } else {
                    System.out.println("No MCP mappings found");
                }
            }

            try {
                var loader = GsonConfigurationLoader
                        .builder()
                        .url(new URL("https://hub.spigotmc.org/versions/" + version + ".json"))
                        .build();

                var node = loader.load();
                var buildDataRevision = node.node("refs", "BuildData").getString();

                var info = GsonConfigurationLoader.builder()
                        .url(new URL("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/info.json?at=" + buildDataRevision))
                        .build()
                        .load();

                if (!info.node("classMappings").getString("").isEmpty()) {
                    System.out.println("Spigot Class Mappings found: https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("classMappings").getString() + "?at=" + buildDataRevision);

                    versionBuilder
                            .spigotClassMappings(Version.DownloadableContent.builder()
                                    .url("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("classMappings").getString() + "?at=" + buildDataRevision)
                                    .build());

                    if (!info.node("memberMappings").getString("").isEmpty()) {
                        System.out.println("Spigot Member Mappings found: https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("memberMappings").getString() + "?at=" + buildDataRevision);

                        versionBuilder
                                .spigotMemberMappings(Version.DownloadableContent.builder()
                                        .url("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("memberMappings").getString() + "?at=" + buildDataRevision)
                                        .build());
                    } else {
                        System.out.println("No Spigot Member Mappings found");
                    }
                } else {
                    System.out.println("No Spigot Class Mappings found (they are not released yet or they are discontinued)");
                }
            } catch (IOException exception) {
                System.out.println("No spigot mappings found");
            }

            var saver = GsonConfigurationLoader
                    .builder()
                    .path(getConfigFolder().get().toPath().resolve(customName + "/info.json"))
                    .build();

            var node = saver.createNode();

            node.set(versionBuilder.build());

            saver.save(node);
        } catch (Throwable e) {
            throw new GradleException("An error occurred while retrieving version information", e);
        }
    }
}
