package org.screamingsandals.nms.generator.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.screamingsandals.nms.generator.configuration.NMSMapperConfiguration;

public class NMSMapperGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("nmsGen", NMSMapperConfiguration.class);

        project.getTasks().create("generateNmsComponents", AccessorClassGenerationTask.class);
    }
}
