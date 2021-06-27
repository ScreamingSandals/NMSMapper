package org.screamingsandals.nms.mapper.extension;

import lombok.Data;
import org.gradle.api.Project;
import org.screamingsandals.nms.mapper.tasks.DocsGenerationTask;
import org.screamingsandals.nms.mapper.tasks.JoinedMappingTask;
import org.screamingsandals.nms.mapper.tasks.RemappingTask;
import org.screamingsandals.nms.mapper.tasks.SaveMappingsTask;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import java.util.List;
import java.util.stream.Stream;

@Data
public class NMSExtension {
    private final Project project;
    private final UtilsHolder utilsHolder;

    public Version version(String version) {
        return new Version(version, null);
    }

    public Version version(String version, String mcpBuild) {
        return new Version(version, mcpBuild);
    }

    public void buildTasks(List<Version> versions) {
        if (!versions.isEmpty()) {
            versions.forEach(s -> project.getTasks().create("remapVersion" + s.getVersion(), RemappingTask.class, remappingTask -> {
                remappingTask.getVersion().set(s.getVersion());
                remappingTask.getMcpBuild().set(s.getMcpBuild());

                remappingTask.getUtils().set(utilsHolder);
            }));

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
        }
    }
}
