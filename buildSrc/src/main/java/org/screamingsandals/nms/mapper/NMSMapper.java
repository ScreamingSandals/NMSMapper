package org.screamingsandals.nms.mapper;

import lombok.SneakyThrows;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.screamingsandals.nms.mapper.extension.NMSExtension;
import org.screamingsandals.nms.mapper.utils.Caching;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;

public class NMSMapper implements Plugin<Project> {
    @SneakyThrows
    @Override
    public void apply(Project project) {
        project.getExtensions().create("nms", NMSExtension.class, project, new UtilsHolder(
                new Caching(project.file("cache").toPath()),
                project.file("src/main/resources/nms-mappings")
        ));
    }
}
