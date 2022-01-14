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

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.VersionNumber;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.screamingsandals.nms.mapper.web.*;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DocsGenerationTask extends DefaultTask {
    @Input
    public abstract Property<File> getOutputFolder();
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        System.out.println("Generating docs...");
        var outputFolder = getOutputFolder().get();
        var versions = getUtils().get().getMappings();
        var mappings = getUtils().get().getMappings();

        outputFolder.mkdirs();

        versions.forEach((version, mapping) -> {
            System.out.println("Generating docs for version " + version + "...");

            var searchIndex = new HashMap<MappingType, List<Map<String, String>>>();

            var versionDirectory = new File(outputFolder, version);
            versionDirectory.mkdirs();

            var packages = new HashMap<String, List<Map.Entry<ClassDefinition.Type, String>>>();

            mapping.getMappings().forEach((key, classDefinition) -> {
                var key2 = classDefinition.getMapping().getOrDefault(mapping.getDefaultMapping(), key);

                var pathKey = key2
                        .replace(".", "/")
                        .replace("${V}", "VVV");

                var packageStr = key2.lastIndexOf(".") == -1 ? "default-pkg" : key2.substring(0, key2.lastIndexOf("."));

                if (!packages.containsKey(packageStr)) {
                    packages.put(packageStr, new ArrayList<>());
                }

                if (packageStr.equals("default-pkg")) {
                    pathKey = "default-pkg/" + pathKey;
                }

                packages.get(packageStr).add(Map.entry(classDefinition.getType(), pathKey.substring(pathKey.lastIndexOf("/") + 1) + ".html"));

                classDefinition.setPathKey(pathKey + ".html");

                getUtils().get().getJoinedMappings().get(classDefinition.getJoinedKey()).getPathKeys().put(version, pathKey + ".html");

                var finalHtml = new File(versionDirectory, pathKey + ".html");
                finalHtml.getParentFile().mkdirs();

                final var finalPathKey = pathKey;
                classDefinition.getMapping().forEach((mappingType, s) -> {
                    if (!searchIndex.containsKey(mappingType)) {
                        searchIndex.put(mappingType, new ArrayList<>());
                    }
                    searchIndex.get(mappingType).add(Map.of(
                            "label", s,
                            "value",  finalPathKey + ".html"
                    ));
                });

                var page = new DescriptionPage(key2, classDefinition, mapping.getMappings(), mapping.getDefaultMapping());
                try (var fileWriter = new FileWriter(finalHtml)) {
                    page.generate().render(fileWriter);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            packages.forEach((key, paths) -> {
                var pathKey = key
                        .replace(".", "/")
                        .replace("${V}", "VVV");

                var finalHtml = new File(versionDirectory, pathKey + "/index.html");
                finalHtml.getParentFile().mkdirs();

                var page = new PackageInfoPage(key, paths, mapping.getDefaultMapping());
                try (var fileWriter = new FileWriter(finalHtml)) {
                    page.generate().render(fileWriter);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            var finalHtml = new File(versionDirectory, "index.html");
            finalHtml.getParentFile().mkdirs();

            var page = new OverviewPage("NMS mapping - v" + version, packages.keySet(), mapping.getDefaultMapping(), mapping.getLicenses());
            try (var fileWriter = new FileWriter(finalHtml)) {
                page.generate().render(fileWriter);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            try {
                var saver = GsonConfigurationLoader.builder()
                        .file(new File(versionDirectory, "search-index.json"))
                        .build();

                var node = saver.createNode();

                node.node("index").set(searchIndex);
                node.node("default-mapping").set(mapping);
                saver.save(node);
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Generating class history...");

        getUtils().get().getJoinedMappings().forEach((s, m) -> {
            var finalHtml = new File(outputFolder, "history/" + s + ".html");
            finalHtml.getParentFile().mkdirs();

            var l = getUtils().get()
                    .getJoinedMappingsClassLinks()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(s))
                    .findFirst()
                    .or(() -> getUtils().get()
                            .getSpigotJoinedMappingsClassLinks()
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().equals(s))
                            .findFirst()
                    )
                    .map(Map.Entry::getKey)
                    .orElse(s);
            var page = new HistoryPage(l, m, getUtils().get().getJoinedMappingsClassLinks(), getUtils().get().getJoinedMappings());
            try (var fileWriter = new FileWriter(finalHtml)) {
                page.generate().render(fileWriter);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        var finalHtml = new File(outputFolder, "index.html");
        finalHtml.getParentFile().mkdirs();

        var page = new MainPage(mappings
                .values()
                .stream()
                .sorted(Comparator.comparing(e -> VersionNumber.parse(e.getVersion())))
                .map(e -> Map.entry(e.getVersion(), e.getSupportedMappings()))
                .collect(Collectors.toList()));
        try (var fileWriter = new FileWriter(finalHtml)) {
            page.generate().render(fileWriter);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        System.out.println("Updating static contents...");
        var staticFolder = new File(outputFolder, "static");
        if (staticFolder.exists()) {
            FileUtils.deleteDirectory(staticFolder);
        }
        FileUtils.copyDirectory(getProject().file("static"), staticFolder);
        FileUtils.touch(new File(outputFolder, ".nojekyll"));
        FileUtils.write(new File(outputFolder, "robots.txt"), "User-agent: *\nDisallow: /", StandardCharsets.UTF_8);
    }
}
