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
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;

public abstract class SaveMappingsTask extends DefaultTask {
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        getUtils().get().getMappings().keySet().forEach(version -> {
            System.out.println("Saving " + version + " mappings...");
            var saver = GsonConfigurationLoader.builder()
                    .file(new File(getUtils().get().getResourceDir(), version + ".json"))
                    .build();

            var mainNode = saver.createNode();

            getUtils().get().getMappings().get(version).getMappings().forEach((k, v) -> {
                try {
                    v.asNode(mainNode.node(k));
                } catch (SerializationException e) {
                    e.printStackTrace();
                }
            });

            try {
                saver.save(mainNode);
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }

            var classLinks = getUtils().get().getMappingTypeLinks().get(version);
            if (classLinks != null && !classLinks.isEmpty()) {
                var saver2 = GsonConfigurationLoader.builder()
                        .file(new File(getUtils().get().getResourceDir(), version + "-joined-class-links.json"))
                        .build();

                var mainNode2 = saver2.createNode();

                try {
                    mainNode2.set(classLinks);
                } catch (SerializationException e) {
                    e.printStackTrace();
                }

                try {
                    saver2.save(mainNode2);
                } catch (ConfigurateException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Saving joined mappings...");

        var saver = GsonConfigurationLoader.builder()
                .file(new File(getUtils().get().getResourceDir(), "joined.json"))
                .build();

        var mainNode = saver.createNode();

        getUtils().get().getJoinedMappings().forEach((k, v) -> {
            try {
                v.asNode(mainNode.node(k));
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });

        mainNode.node("classNames").set(getUtils().get().getJoinedMappingsClassLinks());

        mainNode.node("spigotNames").set(getUtils().get().getSpigotJoinedMappingsClassLinks());

        mainNode.node("seargeNames").set(getUtils().get().getSeargeJoinedMappingsClassLinks());

        mainNode.node("intermediaryNames").set(getUtils().get().getIntermediaryJoinedMappingsClassLinks());

        try {
            saver.save(mainNode);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }
}
