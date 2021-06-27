package org.screamingsandals.nms.mapper.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.VersionNumber;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.screamingsandals.nms.mapper.web.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

        versions.forEach((version, defaultMapping) -> {
            System.out.println("Generating docs for version " + version);

            var versionDirectory = new File(outputFolder, version);
            versionDirectory.mkdirs();

            var packages = new HashMap<String, List<String>>();

            mappings.get(version).forEach((key, classDefinition) -> {
                var key2 = classDefinition.getMapping().getOrDefault(defaultMapping, key);

                var pathKey = key2
                        .replace(".", "/")
                        .replace("${V}", "VVV");

                var packageStr = key2.lastIndexOf(".") == -1 ? "default-pkg" : key2.substring(0, key2.lastIndexOf("."));

                if (!packages.containsKey(packageStr)) {
                    packages.put(packageStr, new ArrayList<>());
                }

                if (packageStr.equals("default-pkg")) {
                    pathKey = "default-pkg/" + pathKey;
                }

                packages.get(packageStr).add(pathKey.substring(pathKey.lastIndexOf("/") + 1) + ".html");

                classDefinition.setPathKey(pathKey + ".html");

                getUtils().get().getJoinedMappings().get(classDefinition.getJoinedKey()).getPathKeys().put(version, pathKey + ".html");

                var finalHtml = new File(versionDirectory, pathKey + ".html");
                finalHtml.getParentFile().mkdirs();

                var page = new DescriptionPage(key2, classDefinition, mappings.get(version), defaultMapping);
                try (var fileWriter = new FileWriter(finalHtml)) {
                    page.generate().render(fileWriter);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            packages.forEach((key, paths) -> {
                var pathKey = key
                        .replace(".", "/")
                        .replace("${V}", "VVV");

                var finalHtml = new File(versionDirectory, pathKey + "/index.html");
                finalHtml.getParentFile().mkdirs();

                var page = new PackageInfoPage(key, paths, defaultMapping);
                try (var fileWriter = new FileWriter(finalHtml)) {
                    page.generate().render(fileWriter);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            var finalHtml = new File(versionDirectory, "index.html");
            finalHtml.getParentFile().mkdirs();

            var page = new OverviewPage("NMS mapping - v" + version, packages.keySet(), defaultMapping);
            try (var fileWriter = new FileWriter(finalHtml)) {
                page.generate().render(fileWriter);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        System.out.println("Generating classes history");

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

        var page = new MainPage(versions.keySet().stream().sorted(Comparator.comparing(VersionNumber::parse)).collect(Collectors.toList()));
        try (var fileWriter = new FileWriter(finalHtml)) {
            page.generate().render(fileWriter);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
