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
import org.screamingsandals.nms.mapper.newweb.WebGenerator;
import org.screamingsandals.nms.mapper.newweb.components.VersionRecord;
import org.screamingsandals.nms.mapper.newweb.pages.DescriptionPage;
import org.screamingsandals.nms.mapper.newweb.pages.MainPage;
import org.screamingsandals.nms.mapper.newweb.pages.OverviewPage;
import org.screamingsandals.nms.mapper.newweb.pages.PackagePage;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class NewDocsGenerationTask extends DefaultTask {
    @Input
    public abstract Property<File> getTemplatesFolder();
    @Input
    public abstract Property<File> getOutputFolder();
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        System.out.println("Generating docs...");
        var outputFolder = getOutputFolder().get();

        outputFolder.mkdirs();

        var generator = new WebGenerator(getTemplatesFolder().get(), outputFolder);
        var mainPage = new MainPage();
        generator.putPage(mainPage);

        getUtils().get().getMappings().forEach((version, mapping) -> {
            System.out.println("Generating docs for version " + version + "...");

            var searchIndex = new HashMap<MappingType, List<Map<String, String>>>();
            var packages = new HashMap<String, List<Map.Entry<ClassDefinition.Type, String>>>();

            mainPage.getVersions().add(new VersionRecord(version, mapping.getSupportedMappings(), version + "/"));

            mapping.getMappings().forEach((key, classDefinition) -> {
                var key2 = classDefinition.getMapping().getOrDefault(mapping.getDefaultMapping(), key);

                var page = new DescriptionPage(mapping, key2, classDefinition);

                var packageStr = key2.lastIndexOf(".") == -1 ? "default-pkg" : key2.substring(0, key2.lastIndexOf("."));

                if (!packages.containsKey(packageStr)) {
                    packages.put(packageStr, new ArrayList<>());
                }

                packages.get(packageStr).add(Map.entry(classDefinition.getType(), page.getFinalLocation().substring(page.getFinalLocation().lastIndexOf("/") + 1)));

                classDefinition.setPathKey(page.getFinalLocation());

                //getUtils().get().getJoinedMappings().get(classDefinition.getJoinedKey()).getPathKeys().put(version, page.getFinalLocation());

                classDefinition.getMapping().forEach((mappingType, s) -> {
                    if (!searchIndex.containsKey(mappingType)) {
                        searchIndex.put(mappingType, new ArrayList<>());
                    }
                    searchIndex.get(mappingType).add(Map.of(
                            "label", s,
                            "value",  page.getFinalLocation()
                    ));
                });

                generator.putPage(page);
            });

            packages.forEach((key, paths) -> {
                generator.putPage(new PackagePage(mapping, key, paths));
            });

            generator.putPage(new OverviewPage(mapping, version, packages.keySet()));

            try {
                var saver = GsonConfigurationLoader.builder()
                        .file(new File(generator.getFinalFolder(), version + "/search-index.json"))
                        .build();

                var node = saver.createNode();

                node.node("index").set(searchIndex);
                node.node("default-mapping").set(mapping.getDefaultMapping());
                saver.save(node);
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        });

        // TODO: history generation


        generator.generate();

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
