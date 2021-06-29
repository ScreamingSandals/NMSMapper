package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.extension.Version;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipFile;

public class SeargeMappingParser {
    public static int map(Map<String, ClassDefinition> map, Version version, List<String> excluded) throws IOException, InterruptedException, URISyntaxException {
        var mcpZip = version.getWorkspace().getFile(Objects.requireNonNull(version.getSeargeMappings()), "mcp.zip");

        try (var zip = new ZipFile(mcpZip)) {
            var entry = zip.stream()
                    .filter(zipEntry -> zipEntry.getName().equals("config/joined.tsrg") || zipEntry.getName().equals("joined.srg"))
                    .findFirst()
                    .orElseThrow();

            var inputStream = zip.getInputStream(entry);

            return AnyMappingParser.map(map, inputStream, excluded, MappingType.SEARGE, false);
        }
    }
}
