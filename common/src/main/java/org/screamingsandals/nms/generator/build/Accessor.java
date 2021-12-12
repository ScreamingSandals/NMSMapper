package org.screamingsandals.nms.generator.build;

import com.squareup.javapoet.TypeSpec;
import lombok.Data;
import org.screamingsandals.nms.generator.configuration.RequiredClass;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Accessor {
    private final String className;
    private final TypeSpec.Builder builder;
    private final ConfigurationNode mapping;
    private final RequiredClass requiredClass;
    private final String classHash;
    private final Map<String, Integer> fieldNameCounter = new HashMap<>();
    private final Map<String, Integer> methodNameCounter = new HashMap<>();
    private final AtomicInteger constructorCounter = new AtomicInteger();
}
