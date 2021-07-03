package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.extension.Version;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.ErrorsLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipFile;

public class SeargeMappingParser {
    public static String map(Map<String, ClassDefinition> map, Version version, List<String> excluded, ErrorsLogger errorsLogger) throws IOException, InterruptedException, URISyntaxException {
        var mcpZip = version.getWorkspace().getFile(Objects.requireNonNull(version.getSeargeMappings()), "mcp.zip");

        try (var zip = new ZipFile(mcpZip)) {
            var entry = zip.stream()
                    .filter(zipEntry -> zipEntry.getName().equals("config/joined.tsrg") || zipEntry.getName().equals("joined.srg"))
                    .findFirst()
                    .orElseThrow();

            var inputStream = zip.getInputStream(entry);

            AnyMappingParser.map(map, inputStream, excluded, MappingType.SEARGE, false, errorsLogger);

            return Files.readString(version.getWorkspace()
                    .getFile("mcp-license.txt", "https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/LICENSE")// mcp.zip doesn't contain any license, so we read it from github
                    .toPath()
            );
        }
    }
}
