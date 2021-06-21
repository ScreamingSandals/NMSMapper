package org.screamingsandals.nms.mapper.extension;

import lombok.Data;
import org.gradle.api.Project;
import org.screamingsandals.nms.mapper.tasks.DocsGenerationTask;
import org.screamingsandals.nms.mapper.tasks.JoinedMappingTask;
import org.screamingsandals.nms.mapper.tasks.RemappingTask;
import org.screamingsandals.nms.mapper.tasks.SaveMappingsTask;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import java.util.List;

@Data
public class NMSExtension {
    private final Project project;
    private final UtilsHolder utilsHolder;

    public void buildTasks(List<String> versions) {
        if (!versions.isEmpty()) {
            versions.forEach(s -> project.getTasks().create("remapVersion" + s, RemappingTask.class, remappingTask -> {
                remappingTask.getVersion().set(s);

                remappingTask.getUtils().set(utilsHolder);
            }));

            project.getTasks().create("saveNmsMappings", SaveMappingsTask.class, saveMappingsTask -> {
                saveMappingsTask.getUtils().set(utilsHolder);

                saveMappingsTask.dependsOn(versions.stream().map(s -> "remapVersion" + s).toArray());
            });

            project.getTasks().create("createAndSaveJoinedMappings", JoinedMappingTask.class, joinedMappingTask -> {
                joinedMappingTask.getUtils().set(utilsHolder);

                joinedMappingTask.dependsOn(versions.stream().map(s -> "remapVersion" + s).toArray());
            });

            project.getTasks().create("generateNmsDocs", DocsGenerationTask.class, docsGenerationTask -> {
                docsGenerationTask.getUtils().set(utilsHolder);
                docsGenerationTask.getOutputFolder().set(project.file("build/docs"));

                docsGenerationTask.dependsOn(versions.stream().map(s -> "remapVersion" + s).toArray());
            });
        }
    }
}
