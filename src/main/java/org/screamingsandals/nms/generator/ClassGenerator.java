package org.screamingsandals.nms.generator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ClassGenerator implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("nmsGen", ClassGeneratorExtension.class);

        project.getTasks().create("generateNmsComponents", GenerateClassesTask.class);
    }
}
