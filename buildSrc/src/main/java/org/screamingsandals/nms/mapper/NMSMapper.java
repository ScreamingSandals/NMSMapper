package org.screamingsandals.nms.mapper;

import lombok.SneakyThrows;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.screamingsandals.nms.mapper.extension.NMSExtension;
import org.screamingsandals.nms.mapper.utils.Caching;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class NMSMapper implements Plugin<Project> {
    @SneakyThrows
    @Override
    public void apply(Project project) {
        project.getExtensions().create("nms", NMSExtension.class, project, new UtilsHolder(
                new Caching(project.file("cache").toPath()),
                new HashMap<>(),
                project.file("src/main/resources/nms-mappings"),
                new ArrayList<>(),
                new AtomicReference<>()
        ));
    }
}
