package org.screamingsandals.nms.mapper.extension;

import lombok.Data;
import org.gradle.api.Project;
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
        }
    }
}
