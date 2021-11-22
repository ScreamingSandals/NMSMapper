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
import org.screamingsandals.nms.mapper.utils.ErrorsLogger;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;

import java.util.Map;

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

        var realVersion = version.getRealVersion() != null ? version.getRealVersion() : version.getVersion();

        System.out.println("======= Mapping " + realVersion + " =======");

        System.out.println("Getting base data from vanilla jar ....");

        var entry = VanillaJarParser.map(workspace.getFile(version.getVanillaJar(), "minecraft_server.jar"));

        var mapping = entry.getKey();
        var excluded = entry.getValue();

        System.out.println(excluded.size() + " symbols (fields, methods) excluded from final mapping: synthetic");
        System.out.println(mapping.size() + " classes mapped");

        mappings.put(version.getVersion(), mapping);

        var defaultMappings = MappingType.SPIGOT;

        var errors = new ErrorsLogger();

        if (version.getMojangMappings() != null && version.getMojangMappings().isPresent()) {
            System.out.println("Applying Mojang mappings ....");
            defaultMappings = MappingType.MOJANG;

            var license = MojangMappingParser.map(
                    mapping,
                    workspace.getFile(version.getMojangMappings(), "mojang.mapping"),
                    excluded,
                    errors
            );

            errors.printWarn();
            errors.reset();

            if (license != null) {
                getUtils().get().getLicenses().put(Map.entry(version.getVersion(), MappingType.MOJANG), license);
            }
        }

        if (version.getSeargeMappings() != null && version.getSeargeMappings().isPresent()) {
            System.out.println("Applying Searge (Forge) mappings ....");

            var license = SeargeMappingParser.map(mapping, version, excluded, errors);

            errors.printWarn();
            errors.reset();

            if (license != null) {
                getUtils().get().getLicenses().put(Map.entry(version.getVersion(), MappingType.SEARGE), license);
            }
        }

        if (version.getSpigotClassMappings() != null && version.getSpigotClassMappings().isPresent()) {
            System.out.println("Applying Spigot mappings ....");
            var license = SpigotMappingParser.mapTo(version, mapping, excluded, errors);

            errors.printWarn();
            errors.reset();

            if (license != null) {
                getUtils().get().getLicenses().put(Map.entry(version.getVersion(), MappingType.SPIGOT), license);
            }
        }

        // TODO: Yarn

        newlyGeneratedMappings.put(version.getVersion(), defaultMappings);
    }
}
