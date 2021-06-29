package org.screamingsandals.nms.mapper.utils;

import lombok.Data;
import org.screamingsandals.nms.mapper.joined.JoinedClassDefinition;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class UtilsHolder {
    private final Map<String, Map<String, ClassDefinition>> mappings = new HashMap<>();
    private final File resourceDir;
    private final Map<String, MappingType> newlyGeneratedMappings = new HashMap<>();
    private final AtomicReference<ConfigurationNode> versionManifest = new AtomicReference<>();
    private final Map<String, String> joinedMappingsClassLinks = new HashMap<>();
    private final Map<String, JoinedClassDefinition> joinedMappings = new HashMap<>();
    private final Map<String, String> spigotJoinedMappingsClassLinks = new HashMap<>();
    private final List<String> undefinedClassLinks = new ArrayList<>();
}
