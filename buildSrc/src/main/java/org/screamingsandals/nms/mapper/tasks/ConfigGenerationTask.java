/*
 * Copyright 2022 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            throw new GradleException("Minecraft version not specified! ./gradlew generateNmsConfig -PminecraftVersion=<version> [-PcustomVersionString=<customVersion>]");
        }

        var customName = getProject().getProperties().get("customVersionString");
        if (customName == null) {
            customName = versionObj;
        }

        var customVersion = customName.toString();

        var version = versionObj.toString();

        System.out.println("Generating new config for version " + version + "... (will be saved as " + customName + ")");

        var httpClient = HttpClient.newHttpClient();

        System.out.println("Retrieving https://launchermeta.mojang.com/mc/game/version_manifest_v2.json...");

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
                throw new RuntimeException("Can't generate config without a valid vanilla JAR!");
            }
            if (vanillaJarSha1.empty()) {
                System.out.println("Warning: Vanilla JAR doesn't have sha1!");
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
                System.out.println("Mojang mappings found: " + mojangMappings.getString());
                if (mojangMappingsSha1.empty()) {
                    System.out.println("Warning: Mojang mappings don't have sha1!");
                }
                versionBuilder
                        .mojangMappings(Version.DownloadableContent.builder()
                                .url(mojangMappings.getString())
                                .sha1(mojangMappingsSha1.getString())
                                .build()
                        );
            }

            var intermediaryUrl = new URI("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + version + ".tiny");

            var intermediaryMappings = httpClient.send(HttpRequest.newBuilder().uri(intermediaryUrl).build(), HttpResponse.BodyHandlers.ofString());

            if (intermediaryMappings.statusCode() < 400 && intermediaryMappings.statusCode() >= 200) {
                System.out.println("Intermediary mappings found: https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + version + ".tiny");
                versionBuilder
                        .intermediaryMappings(Version.DownloadableContent.builder()
                                .url("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + version + ".tiny")
                                .sha1(null)
                                .build()
                        );

                var yarnMappingsVersions = GsonConfigurationLoader.builder()
                        .url(new URL("https://maven.fabricmc.net/net/fabricmc/yarn/versions.json"))
                        .build()
                        .load();

                var node = yarnMappingsVersions.node(version);
                if (!node.empty() && node.isList()) {
                    var list = node.childrenList();
                    var yarnBuild = list.get(list.size() - 1).getInt();
                    var yarnUrl = "https://maven.fabricmc.net/net/fabricmc/yarn/" + version + "%2Bbuild." + yarnBuild + "/yarn-" + version + "%2Bbuild." + yarnBuild + "-v2.jar";
                    String sha1 = null;

                    var yarnSha1 = httpClient.send(HttpRequest.newBuilder().uri(new URI(yarnUrl + ".sha1")).build(), HttpResponse.BodyHandlers.ofString());
                    if (yarnSha1.statusCode() < 400 && yarnSha1.statusCode() >= 200) {
                        sha1 = yarnSha1.body();
                    }

                    System.out.println("Yarn mappings found: " + yarnUrl);
                    versionBuilder
                            .yarnMappings(Version.DownloadableContent.builder()
                                    .url(yarnUrl)
                                    .sha1(sha1)
                                    .build()
                            );
                } else {
                    System.out.println("No Yarn mappings found!");
                }

            } else {
                System.out.println("No Intermediary mappings found! Also skipping Yarn!");
            }

            var seargeUrl = new URI("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/" + version + "/mcp_config-" + version + ".zip.sha1");

            var seargeSha1 = httpClient.send(HttpRequest.newBuilder().uri(seargeUrl).build(), HttpResponse.BodyHandlers.ofString());

            if (seargeSha1.statusCode() < 400 && seargeSha1.statusCode() >= 200) {
                System.out.println("MCP mappings found: https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/" + version + "/mcp_config-" + version + ".zip");
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
                    System.out.println("MCP mappings found: https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/" + version + "/mcp-" + version + "-srg.zip");
                    versionBuilder
                            .seargeMappings(Version.DownloadableContent.builder()
                                    .url("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/" + version + "/mcp-" + version + "-srg.zip")
                                    .sha1(seargeSha1.body())
                                    .build()
                            );
                } else {
                    System.out.println("No MCP mappings found!");
                }
            }

            try {
                var loader = GsonConfigurationLoader.builder()
                        .url(new URL("https://hub.spigotmc.org/versions/" + version + ".json"))
                        .build();

                var node = loader.load();
                var buildDataRevision = node.node("refs", "BuildData").getString();
                var craftBukkitRevision = node.node("refs", "CraftBukkit").getString();

                var info = GsonConfigurationLoader.builder()
                        .url(new URL("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/info.json?at=" + buildDataRevision))
                        .build()
                        .load();

                if (!info.node("classMappings").getString("").isEmpty()) {
                    System.out.println("Spigot class mappings found: https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("classMappings").getString() + "?at=" + buildDataRevision);

                    versionBuilder
                            .spigotClassMappings(Version.DownloadableContent.builder()
                                    .url("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("classMappings").getString() + "?at=" + buildDataRevision)
                                    .build());

                    if (!info.node("memberMappings").getString("").isEmpty()) {
                        System.out.println("Spigot member mappings found: https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("memberMappings").getString() + "?at=" + buildDataRevision);

                        versionBuilder
                                .spigotMemberMappings(Version.DownloadableContent.builder()
                                        .url("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/" + info.node("memberMappings").getString() + "?at=" + buildDataRevision)
                                        .build());
                    } else {
                        System.out.println("No Spigot member mappings found!");
                    }
                } else {
                    System.out.println("No Spigot class mappings found! (not released yet or discontinued)");
                }

                try {
                    var cbPomUrl = new URL("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/raw/pom.xml?at=" + craftBukkitRevision).toURI();

                    var cbResult = httpClient.send(HttpRequest.newBuilder().uri(cbPomUrl).build(), HttpResponse.BodyHandlers.ofString());

                    cbResult.body().lines()
                            .filter(s -> s.contains("minecraft_version"))
                            .map(s -> s.split("[><]")[2])
                            .findFirst()
                            .ifPresent(s -> {
                                versionBuilder.spigotNmsVersion(s);
                                System.out.println("CraftBukkit/Spigot NMS version: " + s);
                            });
                } catch (Throwable ignored) {
                }
            } catch (IOException exception) {
                System.out.println("No Spigot mappings found!");
            }

            var saver = GsonConfigurationLoader
                    .builder()
                    .path(getConfigFolder().get().toPath().resolve(customName + "/info.json"))
                    .build();

            var node = saver.createNode();

            node.set(versionBuilder.build());

            saver.save(node);
        } catch (Throwable e) {
            throw new GradleException("An error occurred while retrieving version information.", e);
        }
    }
}
