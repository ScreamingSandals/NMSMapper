package org.screamingsandals.nms.mapper.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.screamingsandals.nms.mapper.web.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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

            var packages = new ArrayList<String>();

            mappings.get(version).forEach((key, classDefinition) -> {
                var pathKey = key
                        .replace(".", "/")
                        .replace("${V}", "VVV");

                var packageStr = key.substring(0, key.lastIndexOf("."));

                if (!packages.contains(packageStr)) {
                    packages.add(packageStr);
                }

                var finalHtml = new File(versionDirectory, pathKey + ".html");
                finalHtml.getParentFile().mkdirs();

                var page = new DescriptionPage(key, classDefinition, mappings.get(version));
                try (var fileWriter = new FileWriter(finalHtml)) {
                    page.generate().render(fileWriter);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            packages.forEach(key -> {
                var pathKey = key
                        .replace(".", "/")
                        .replace("${V}", "VVV");

                var finalHtml = new File(versionDirectory, pathKey + "/index.html");
                finalHtml.getParentFile().mkdirs();

                var page = new PackageInfoPage(key, mappings.get(version));
                try (var fileWriter = new FileWriter(finalHtml)) {
                    page.generate().render(fileWriter);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            var finalHtml = new File(versionDirectory, "index.html");
            finalHtml.getParentFile().mkdirs();

            var page = new OverviewPage("NMS mapping - v" + version, packages);
            try (var fileWriter = new FileWriter(finalHtml)) {
                page.generate().render(fileWriter);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        getUtils().get().getJoinedMappingsClassLinks().forEach((s, s2) -> {
            var finalHtml = new File(outputFolder, "history/" + s2 + ".html");
            finalHtml.getParentFile().mkdirs();

            var page = new HistoryPage(s, getUtils().get().getJoinedMappingsClassLinks(), getUtils().get().getJoinedMappings());
            try (var fileWriter = new FileWriter(finalHtml)) {
                page.generate().render(fileWriter);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        var finalHtml = new File(outputFolder, "index.html");
        finalHtml.getParentFile().mkdirs();

        var page = new MainPage(versions);
        try (var fileWriter = new FileWriter(finalHtml)) {
            page.generate().render(fileWriter);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
