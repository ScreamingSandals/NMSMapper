package org.screamingsandals.nms.mapper.utils;

import lombok.Data;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class UtilsHolder {
    private final Caching caching;
    private final Map<String, Map<String, ClassDefinition>> mappings;
    private final File resourceDir;
    private final List<String> newlyGeneratedMappings;
    private final AtomicReference<ConfigurationNode> versionManifest;
}
