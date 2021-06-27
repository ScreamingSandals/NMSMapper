package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.Caching;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class SeargeMappingParser {
    public static int map(Map<String, ClassDefinition> map, String version, String mcpBuild, Caching caching, List<String> excluded) throws IOException, InterruptedException {
        String url;

        if (!mcpBuild.matches("[a-z]+://.*")) {
            url = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/" + version + "-" + mcpBuild + "/mcp_config-" + version + "-" + mcpBuild + ".zip";
        } else {
            url = mcpBuild;
        }

        System.out.println("Downloading MCP config...");

        var mcpZip = caching.getFile(URI.create(url), url.substring(url.lastIndexOf("/") + 1));

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
