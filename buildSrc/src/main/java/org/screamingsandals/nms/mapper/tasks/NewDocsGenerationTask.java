package org.screamingsandals.nms.mapper.tasks;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.newweb.WebGenerator;
import org.screamingsandals.nms.mapper.newweb.components.VersionRecord;
import org.screamingsandals.nms.mapper.newweb.pages.MainPage;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class NewDocsGenerationTask extends DefaultTask {
    @Input
    public abstract Property<File> getTemplatesFolder();
    @Input
    public abstract Property<File> getOutputFolder();
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        var outputFolder = getOutputFolder().get();

        outputFolder.mkdirs();

        var generator = new WebGenerator(getTemplatesFolder().get(), outputFolder);

        // TODO: other parts of generation

        var mainPage = new MainPage();
        mainPage.getVersions().add(new VersionRecord("1.18.1", Arrays.asList(MappingType.values()), "1.18.1/"));
        generator.generate(mainPage);
    }

}
