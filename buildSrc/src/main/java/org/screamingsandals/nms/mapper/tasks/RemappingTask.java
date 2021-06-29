package org.screamingsandals.nms.mapper.tasks;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.extension.Version;
import org.screamingsandals.nms.mapper.parser.MojangMappingParser;
import org.screamingsandals.nms.mapper.parser.SeargeMappingParser;
import org.screamingsandals.nms.mapper.parser.SpigotMappingParser;
import org.screamingsandals.nms.mapper.parser.VanillaJarParser;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;

import java.io.FileInputStream;

public abstract class RemappingTask extends DefaultTask {
    @Input
    public abstract Property<Version> getVersion();

    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        var version = getVersion().get();
        var utils = getUtils().get();

        var mappings = utils.getMappings();
        var newlyGeneratedMappings = utils.getNewlyGeneratedMappings();
        var workspace = version.getWorkspace();

        System.out.println("======= Mapping " + version.getVersion() + " =======");

        System.out.println("Getting base data from vanilla jar ....");

        var entry = VanillaJarParser.map(workspace.getFile(version.getVanillaJar(), "minecraft_server.jar"));

        var mapping = entry.getKey();
        var excluded = entry.getValue();

        System.out.println(excluded.size() + " symbols (fields, methods) excluded from final mapping: synthetic");
        System.out.println(mapping.size() + " classes mapped");

        mappings.put(version.getVersion(), mapping);

        var defaultMappings = MappingType.SPIGOT;

        if (version.getMojangMappings() != null && version.getMojangMappings().isPresent()) {
            System.out.println("Applying Mojang mapping ....");
            defaultMappings = MappingType.MOJANG;

            var errors = MojangMappingParser.map(
                    mapping,
                    new FileInputStream(workspace.getFile(version.getMojangMappings(), "mojang.mapping")),
                    excluded
            );

            if (errors > 0) {
                System.out.println(errors + " symbols (fields, methods) not found but they are not excluded");
            }
        }

        if (version.getSeargeMappings() != null && version.getSeargeMappings().isPresent()) {
            System.out.println("Applying Searge (Forge) mapping ....");

            var errors = SeargeMappingParser.map(mapping, version, excluded);

            if (errors > 0) {
                System.out.println(errors + " symbols (fields, methods) not found but they are not excluded");
            }
        }

        if (version.getSpigotClassMappings() != null && version.getSpigotClassMappings().isPresent() && version.getSpigotMemberMappings() != null && version.getSpigotMemberMappings().isPresent()) {
            System.out.println("Applying Spigot mapping ....");
            var errors = SpigotMappingParser.mapTo(version, mapping, excluded);

            if (errors > 0) {
                System.out.println(errors + " symbols (fields, methods) not found but they are not excluded");
            }
        }

        // TODO: Yarn

        newlyGeneratedMappings.put(version.getVersion(), defaultMappings);
    }
}
