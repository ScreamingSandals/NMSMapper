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

package org.screamingsandals.nms.mapper;

import lombok.SneakyThrows;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.screamingsandals.nms.mapper.extension.Version;
import org.screamingsandals.nms.mapper.tasks.*;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.screamingsandals.nms.mapper.workspace.Workspace;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NMSMapper implements Plugin<Project> {
    @SneakyThrows
    @Override
    public void apply(Project project) {
        project.getTasks().create("generateNmsConfig", ConfigGenerationTask.class, task ->
                task.getConfigFolder().set(project.file("config"))
        );

        var utilsHolder = new UtilsHolder(
                project.file("common/src/main/resources/nms-mappings")
        );

        var configFolder = project.file("config");

        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        var mainWorkspaceDirectory = project.file("work");

        if (!mainWorkspaceDirectory.exists()) {
            mainWorkspaceDirectory.mkdirs();
        }

        try (var stream = Files.walk(configFolder.toPath().toAbsolutePath())) {
            final var versions = stream.filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().toLowerCase().endsWith(".disabled"))
                    .map(path -> path.resolve("info.json"))
                    .filter(Files::exists)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                                try {
                                    return GsonConfigurationLoader.builder()
                                        .path(path)
                                            .build()
                                            .load()
                                            .get(Version.class);
                                } catch (ConfigurateException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!versions.isEmpty()) {
                versions.forEach(version -> {
                    version.setWorkspace(new Workspace(version.getVersion(), mainWorkspaceDirectory));
                    project.getTasks().create("remapVersion" + version.getVersion(), RemappingTask.class, remappingTask -> {
                        remappingTask.getVersion().set(version);

                        remappingTask.getUtils().set(utilsHolder);
                    });
                });

                project.getTasks().create("createJoinedMappings", JoinedMappingTask.class, joinedMappingTask -> {
                    joinedMappingTask.getUtils().set(utilsHolder);

                    joinedMappingTask.dependsOn(versions.stream().map(s -> "remapVersion" + s.getVersion()).toArray());
                });

                project.getTasks().create("saveNmsMappings", SaveMappingsTask.class, saveMappingsTask -> {
                    saveMappingsTask.getUtils().set(utilsHolder);

                    saveMappingsTask.dependsOn(Stream.concat(versions.stream().map(s -> "remapVersion" + s.getVersion()), Stream.of("createJoinedMappings")).toArray());
                });

                project.getTasks().create("generateNmsDocs", DocsGenerationTask.class, docsGenerationTask -> {
                    docsGenerationTask.getUtils().set(utilsHolder);
                    docsGenerationTask.getOutputFolder().set(project.file("build/docs"));

                    docsGenerationTask.dependsOn(Stream.concat(versions.stream().map(s -> "remapVersion" + s.getVersion()), Stream.of("createJoinedMappings")).toArray());
                });

                project.getTasks().create("uploadNmsDocs", UploadNmsDocsTask.class, uploadNmsDocsTask -> {
                    uploadNmsDocsTask.getDocsFolder().set(project.file("build/docs"));

                    uploadNmsDocsTask.dependsOn("generateNmsDocs");
                });
            }
        }
    }
}
