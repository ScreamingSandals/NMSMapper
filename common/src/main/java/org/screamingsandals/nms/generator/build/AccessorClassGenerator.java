/*
 * Copyright 2022 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.screamingsandals.nms.generator.build;

import com.squareup.javapoet.*;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.generator.configuration.NMSMapperConfiguration;
import org.screamingsandals.nms.generator.configuration.RequiredClass;
import org.screamingsandals.nms.generator.configuration.RequiredField;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import javax.lang.model.element.Modifier;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Data
public class AccessorClassGenerator {
    private final NMSMapperConfiguration configuration;
    private final File projectFolder;
    private final List<Accessor> accessors = new ArrayList<>();
    private final Map<RequiredClass, Accessor> requiredClassAccessorMap = new HashMap<>();
    private ClassName accessorUtils;
    private String basePackage;

    public void run() throws IOException {
        basePackage = configuration.getBasePackage();
        System.out.println("Generated accessors will be saved in: " + projectFolder.toPath().resolve(configuration.getSourceSet() + "/" + basePackage.replace(".", "/")).toAbsolutePath().toString());

        if (configuration.isCleanOnRebuild()) {
            System.out.println("Cleaning workspace...");
            var file = new File(projectFolder, configuration.getSourceSet() + "/" + basePackage.replace(".", "/"));
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
        }

        System.out.println("Loading joined mappings...");
        var joinedMappings = GsonConfigurationLoader
                .builder()
                .source(() -> new BufferedReader(
                        new InputStreamReader(
                                Objects.requireNonNull(
                                        AccessorClassGenerator.class.getResourceAsStream("/nms-mappings/joined.json")
                                )
                        )
                ))
                .build()
                .load();

        var forcedVersions = new HashMap<String, ConfigurationNode>();
        var versionSpecificData = new HashMap<String, ConfigurationNode>();

        System.out.println("Copying AccessorUtils...");
        accessorUtils = ClassName.get(basePackage, "AccessorUtils");

        var takenAccessorNames = new ArrayList<>();

        System.out.println("Generating accessors for classes...");
        configuration.getClassContext().getAllClasses().forEach(requiredClass -> {
            String mapping = requiredClass.getMapping().toLowerCase();
            String className = requiredClass.getName();
            @Nullable
            String forcedVersion = requiredClass.getForcedVersion();

            String classHash;
            if ("hash".equals(mapping)) {
                classHash = mapping;
            } else if (forcedVersion != null) {
                classHash = forcedVersions.computeIfAbsent(forcedVersion, s -> {
                    try {
                        return GsonConfigurationLoader
                                .builder()
                                .source(() -> new BufferedReader(
                                        new InputStreamReader(
                                                Objects.requireNonNull(
                                                        AccessorClassGenerator.class.getResourceAsStream("/nms-mappings/" + s + "-joined-class-links.json")
                                                )
                                        )
                                ))
                                .build()
                                .load();
                    } catch (ConfigurateException e) {
                        throw new RuntimeException(e);
                    }
                }).node(mapping.toUpperCase(), className).getString();
            } else if ("obfuscated".equals(mapping)) {
                throw new UnsupportedOperationException("Can't use an obfuscated name when the forced version is not specified!");
            } else {
                classHash = joinedMappings.node((mapping.equals("mojang") ? "class" : mapping) + "Names", className).getString();
            }
            System.out.println("Generating accessor for " + className + "...");

            if (classHash == null) {
                throw new IllegalArgumentException("Can't find class: " + className);
            }

            var li = className.lastIndexOf(".");
            String name;
            int counter = 0;
            do {
                name = (className.substring(li < 0 ? 0 : (li + 1)) + "Accessor").replace("$", "_i_");
                if (counter > 0) {
                    name += "_" + counter;
                }
                counter++;
            } while (takenAccessorNames.contains(name));
            takenAccessorNames.add(name);

            var builder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC);

            var typeMapping = joinedMappings.node(classHash);

            if (typeMapping.empty()) {
                throw new IllegalArgumentException("Can't find class: " + className);
            }

            if (requiredClass.getForcedEnumFieldsLoadVersion() != null && !requiredClass.getForcedEnumFieldsLoadVersion().isEmpty()) {
                var version = requiredClass.getForcedEnumFieldsLoadVersion();
                var versionObfuscatedName = typeMapping.node("OBFUSCATED")
                        .childrenMap()
                        .entrySet()
                        .stream()
                        .filter(entry -> Arrays.asList(entry.getKey().toString().split(",")).contains(version))
                        .map(entry -> entry.getValue().getString())
                        .findFirst()
                        .orElseThrow();
                versionSpecificData.computeIfAbsent(version, s -> {
                            try {
                                return GsonConfigurationLoader
                                        .builder()
                                        .source(() -> new BufferedReader(
                                                new InputStreamReader(
                                                        Objects.requireNonNull(
                                                                AccessorClassGenerator.class.getResourceAsStream("/nms-mappings/" + s + ".json")
                                                        )
                                                )
                                        ))
                                        .build()
                                        .load();
                            } catch (ConfigurateException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .node(versionObfuscatedName, "fields")
                        .childrenList()
                        .stream()
                        .filter(node1 -> {
                            var modifier = node1.node("modifier").getInt();
                            return java.lang.reflect.Modifier.isFinal(modifier)
                                    && java.lang.reflect.Modifier.isStatic(modifier)
                                    && ("&" + versionObfuscatedName).equals(node1.node("type").getString());
                        })
                        .map(node1 -> {
                            var fieldName = node1.node("mapping", "MOJANG").getString();
                            var type = "mojang";
                            if (fieldName == null) {
                                fieldName = node1.node("mapping", "SPIGOT").getString();
                                type = "spigot";
                                if (fieldName == null) {
                                    fieldName = node1.node("mapping", "OBFUSCATED").getString();
                                    type = "obfuscated";
                                }
                            }

                            return new RequiredField(type, fieldName, version);
                        })
                        .forEach(requiredClass.getRequiredSymbols()::add);
            }

            var accessor = new Accessor(name, builder, typeMapping, requiredClass, classHash);
            accessors.add(accessor);
            requiredClassAccessorMap.put(requiredClass, accessor);

            var typeMethod = MethodSpec.methodBuilder("getType")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get("", "?")))
                    .addStatement("return $T.$N($T.class, $L)", accessorUtils, "getType", ClassName.get(basePackage, name), generateMappings(typeMapping));
            var nullable = configuration.getNullableAnnotation();
            if (nullable != null) {
                typeMethod.addAnnotation(ClassName.get(nullable.substring(0, nullable.lastIndexOf('.')), nullable.substring(nullable.lastIndexOf('.') + 1)));
            }
            builder.addMethod(typeMethod.build());
        });


        System.out.println("Generating accessors for fields, enum fields, methods and constructors...");

        accessors.forEach(accessor ->
                accessor.getRequiredClass().getRequiredSymbols().forEach(requiredClassMember ->
                        accessor.getBuilder().addMethod(requiredClassMember.generateSymbolAccessor(accessor, this))
                )
        );

        System.out.println("Saving accessors...");

        accessors.forEach(accessor -> {
            try {
                JavaFile.builder(basePackage, accessor.getBuilder().build())
                        .build()
                        .writeTo(new File(projectFolder, configuration.getSourceSet()));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        // Adding AccessorUtils to the generated folder
        var str = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils.java"))
                ))
                .lines()
                .collect(Collectors.joining("\n"));

        var ac = new File(projectFolder, configuration.getSourceSet() + "/" + basePackage.replace(".", "/") + "/AccessorUtils.java");
        Files.writeString(ac.toPath(), "package " + basePackage + ";\n\n" + str, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public CodeBlock generateMappings(ConfigurationNode node) {
        return generateMappings(List.of(node));
    }

    public CodeBlock generateMappings(List<ConfigurationNode> nodes) {
        var codeBlock = CodeBlock.builder()
                .add("mapper -> {\n")
                .indent();

        var usedNode = new HashMap<String, ConfigurationNode>();
        var rawObfuscatedFallback = new HashMap<String, String>();
        nodes.forEach(node -> {
            node.node("OBFUSCATED")
                    .childrenMap()
                    .entrySet()
                    .stream()
                    .flatMap(entry -> Arrays.stream(entry.getKey().toString().split(",")).map(s1 -> Map.entry(s1, entry.getValue().getString(""))))
                    .filter(stringStringEntry -> {
                        if (rawObfuscatedFallback.containsKey(stringStringEntry.getKey())) {
                            return false; // don't overwrite it
                        }

                        if (
                                (configuration.getMinMinecraftVersion() != null && !configuration.getMinMinecraftVersion().isEmpty())
                                || (configuration.getMaxMinecraftVersion() != null && !configuration.getMaxMinecraftVersion().isEmpty())
                        ) {
                            var version = new ComparableVersion(stringStringEntry.getKey());
                            if (configuration.getMinMinecraftVersion() != null && !configuration.getMinMinecraftVersion().isEmpty()) {
                                var min = new ComparableVersion(configuration.getMinMinecraftVersion());
                                if (version.compareTo(min) < 0) {
                                    return false;
                                }
                            }
                            if (configuration.getMaxMinecraftVersion() != null && !configuration.getMaxMinecraftVersion().isEmpty()) {
                                var max = new ComparableVersion(configuration.getMaxMinecraftVersion());
                                if (version.compareTo(max) > 0) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    })
                    .forEach(e -> {
                        rawObfuscatedFallback.put(e.getKey(), e.getValue());
                        usedNode.put(e.getKey(), node);
                    });
        });
        var obfuscatedFallback = rawObfuscatedFallback.entrySet()
                .stream()
                .sorted(Comparator.comparing(o -> new ComparableVersion(o.getKey())))
                .collect(Collectors.toList());


        // currently support spigot and searge. vanilla servers are not supported
        var spigotLatest = new AtomicReference<String>();

        obfuscatedFallback.forEach(entry -> {
            var value = usedNode.get(entry.getKey())
                    .node("SPIGOT")
                    .childrenMap()
                    .entrySet()
                    .stream()
                    .filter(e -> Arrays.asList(e.getKey().toString().split(",")).contains(entry.getKey()))
                    .findFirst()
                    .map(e -> e.getValue().getString())
                    .orElse(entry.getValue());

            if (spigotLatest.get() == null || !spigotLatest.get().equals(value)) {
                codeBlock.add("$N.$N($S, $S, $S);\n", "mapper", "map", "spigot", entry.getKey(), value);
                spigotLatest.set(value);
            }
        });


        var seargeLatest = new AtomicReference<String>();

        obfuscatedFallback.forEach(entry -> {
            var value = entry.getValue();
            var seargeValue = usedNode.get(entry.getKey())
                    .node("SEARGE")
                    .childrenMap()
                    .entrySet()
                    .stream()
                    .filter(e -> Arrays.asList(e.getKey().toString().split(",")).contains(entry.getKey()))
                    .findFirst()
                    .map(e -> e.getValue().getString());

            if (seargeValue.isPresent()) {
                value = seargeValue.get();
            } else if (seargeLatest.get() != null) {
                // Searge mappings are usually consistent across versions, so skip the mapping if there's no value in mappings but there's value in the seargeLatest variable
                return;
            }

            if (seargeLatest.get() == null || !seargeLatest.get().equals(value)) {
                codeBlock.add("$N.$N($S, $S, $S);\n", "mapper", "map", "mcp", entry.getKey(), value);
                seargeLatest.set(value);
            }
        });

        return codeBlock
                .unindent()
                .add("}")
                .build();
    }
}
