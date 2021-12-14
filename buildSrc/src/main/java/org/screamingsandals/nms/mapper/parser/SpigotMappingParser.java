package org.screamingsandals.nms.mapper.parser;

import lombok.SneakyThrows;
import org.screamingsandals.nms.mapper.extension.Version;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.ErrorsLogger;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpigotMappingParser {
    @SneakyThrows
    public static String mapTo(Version version, Map<String, ClassDefinition> map, List<String> excluded, ErrorsLogger errorsLogger) {
        var workspace = version.getWorkspace();

        var cl = Files.readString(workspace.getFile(Objects.requireNonNull(version.getSpigotClassMappings()), "bukkit-cl.csrg").toPath());
        if (version.getSpigotMemberMappings() != null && version.getSpigotMemberMappings().isPresent()) {
            cl = cl + "\n" + Files.readString(workspace.getFile(Objects.requireNonNull(version.getSpigotMemberMappings()), "bukkit-members.csrg").toPath());
        }

        AnyMappingParser.map(map, new ByteArrayInputStream(cl.getBytes(StandardCharsets.UTF_8)), excluded, MappingType.SPIGOT, false, errorsLogger);

        return cl.lines().filter(e -> e.startsWith("#")).findFirst().orElse(null);
    }
}
