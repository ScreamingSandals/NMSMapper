package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.ErrorsLogger;

import java.io.*;
import java.util.List;
import java.util.Map;

public class MojangMappingParser {
    public static String map(Map<String, ClassDefinition> map, File file, List<String> excluded, ErrorsLogger errorsLogger) throws IOException {
        AnyMappingParser.map(map, new FileInputStream(file), excluded, MappingType.MOJANG, true, errorsLogger);

        try (var br = new BufferedReader(new FileReader(file))) {
            return br.readLine();
        } catch (Throwable ignored) {}

        return null;
    }
}
