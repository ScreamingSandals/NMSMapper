package org.screamingsandals.nms.mapper.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.screamingsandals.nms.mapper.web.DescriptionPage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class DocsGenerationTask extends DefaultTask {
    @Input
    public abstract Property<File> getOutputFolder();
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @TaskAction
    public void run() {
        System.out.println("Generating web docs");
        var outputFolder = getOutputFolder().get();
        var versions = getUtils().get().getNewlyGeneratedMappings();
        var mappings = getUtils().get().getMappings();

        outputFolder.mkdirs();

        versions.forEach(version -> {
            System.out.println("Generating docs for version " + version);

            var versionDirectory = new File(outputFolder, version);
            versionDirectory.mkdirs();

            mappings.get(version).forEach((key, classDefinition) -> {
                var pathKey = key
                        .replace(".", "/")
                        .replace("${V}", "VVV");

                var finalHtml = new File(versionDirectory, pathKey + ".html");
                finalHtml.getParentFile().mkdirs();

                var page = new DescriptionPage(key, classDefinition, mappings.get(version));
                try (var fileWriter = new FileWriter(finalHtml)) {
                    page.generate().render(fileWriter);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });
        });
    }
}
