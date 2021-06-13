package org.screamingsandals.nms.mapper.tasks;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;

public abstract class SaveMappingsTask extends DefaultTask {
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        getUtils().get().getNewlyGeneratedMappings().forEach(version -> {
            var saver = GsonConfigurationLoader.builder()
                    .file(new File(getUtils().get().getResourceDir(), version + ".json"))
                    .build();

            var mainNode = saver.createNode();

            getUtils().get().getMappings().get(version).forEach((k, v) -> {
                try {
                    v.asNode(mainNode.node(k));
                } catch (SerializationException e) {
                    e.printStackTrace();
                }
            });

            try {
                saver.save(mainNode);
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        });
    }
}
