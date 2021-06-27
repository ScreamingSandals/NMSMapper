package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MojangMappingParser {
    public static int map(Map<String, ClassDefinition> map, InputStream text, List<String> excluded) throws IOException {
        return AnyMappingParser.map(map, text, excluded, MappingType.MOJANG, true);
    }
}
