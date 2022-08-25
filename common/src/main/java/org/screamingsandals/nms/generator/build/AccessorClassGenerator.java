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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Data
public class AccessorClassGenerator {
    private final NMSMapperConfiguration configuration;
    private final File projectFolder;
    private final List<Accessor> accessors = new ArrayList<>();
    private final Map<RequiredClass, Accessor> requiredClassAccessorMap = new HashMap<>();
    private List<String> normalizedRequestedPlatforms;
    private final List<String> platformsRequiringInitializers = new ArrayList<>();
    private ClassName accessorUtils;
    private String basePackage;

    public void run() throws IOException {
        var platforms = configuration.getMapForPlatforms();
        if (platforms.isEmpty()) {
            throw new RuntimeException("Please specify at least one platform for which accessors should be generated");
        }
        normalizedRequestedPlatforms = platforms.stream().map(s -> {
            var split = s.toUpperCase(Locale.ROOT).split(":", 2);
            if (split.length == 2) {
                if ("INIT".equals(split[1])) {
                    platformsRequiringInitializers.add(split[0]);
                }
            }
            return split[0].toUpperCase(Locale.ROOT);
        }).distinct().collect(Collectors.toList());
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
            String mapping = requiredClass.getMapping().toLowerCase(Locale.ROOT);
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
                }).node(mapping.toUpperCase(Locale.ROOT), className).getString();
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

            if (configuration.isAddInformationJavadoc()) {
                builder.addJavadoc("Class generated by NMS Mapper.\n<p>\nThis class is a reflection accessor for "
                        + requiredClass.getName()
                        + "\n\n@since " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(Instant.now())));
            }
            var accessor = new Accessor(name, builder, typeMapping, requiredClass, classHash);
            accessors.add(accessor);
            requiredClassAccessorMap.put(requiredClass, accessor);

            var typeMethod = MethodSpec.methodBuilder("getType")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get("", "?")))
                    .addStatement("return $T.$N($T.class, $L)", accessorUtils, "getType", ClassName.get(basePackage, name), generateMappings(typeMapping, configuration.isAddInformationJavadoc()));
            var nullable = configuration.getNullableAnnotation();
            if (nullable != null) {
                typeMethod.addAnnotation(ClassName.get(nullable.substring(0, nullable.lastIndexOf('.')), nullable.substring(nullable.lastIndexOf('.') + 1)));
            }
            if (configuration.isAddInformationJavadoc()) {
                typeMethod.addJavadoc("This method returns the {@link Class} object of the requested NMS class.\n<p>\nThis method is safe to call: exception is handler and null is returned in case of failure.\n\n@return the resolved class object or null if the class does not exist");
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

        // Constructing accessor utils
        var str = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils.java"))
                ))
                .lines()
                .map(s -> {
                    // TODO: probably involve javapoet into this somehow
                    if (s.contains("{/*=Generate=fields*/}")) {
                        var str2 = new StringBuilder();
                        for (var platform : normalizedRequestedPlatforms) {
                            str2.append("private static boolean availableMapping_")
                                    .append(platform)
                                    .append(" = false;\n")
                                    .append("public static boolean isAvailableMapping_")
                                    .append(platform)
                                    .append("() {\nreturn availableMapping_")
                                    .append(platform)
                                    .append(";\n}\n");
                            var stream = AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_" + platform + "_fields.txt");
                            if (stream != null) {
                                str2.append(new BufferedReader(new InputStreamReader(stream))
                                        .lines()
                                        .collect(Collectors.joining("\n")));
                                str2.append("\n");
                            }
                        }
                        s = s.replace("{/*=Generate=fields*/}", str2.toString());
                    }
                    if (s.contains("{/*=Generate=static*/}")) {
                        var str2 = new StringBuilder();
                        for (var platform : normalizedRequestedPlatforms) {
                            if (platformsRequiringInitializers.contains(platform)) {
                                continue;
                            }

                            var stream = AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_" + platform + "_static.txt");
                            if (stream != null) {
                                str2.append(new BufferedReader(new InputStreamReader(stream))
                                        .lines()
                                        .collect(Collectors.joining("\n")));
                                str2.append("\n");
                            }
                        }
                        s = s.replace("{/*=Generate=static*/}", str2.toString());
                    }
                    if (s.contains("{/*=Generate=reflection=class*/}")) {
                        var baseString = new BufferedReader(
                                    new InputStreamReader(
                                        Objects.requireNonNull(AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_class.txt"))
                                    )
                                )
                                .lines()
                                .collect(Collectors.joining("\n"));
                        var str2 = new StringBuilder();
                        for (var platform : normalizedRequestedPlatforms) {
                            str2.append(baseString.replaceAll("\\{PLATFORM_NAME}", platform));
                            str2.append("\n");
                        }
                        s = s.replace("{/*=Generate=reflection=class*/}", str2.toString());
                    }
                    if (s.contains("{/*=Generate=reflection=fields*/}")) {
                        var baseString = new BufferedReader(
                                new InputStreamReader(
                                        Objects.requireNonNull(AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_fields.txt"))
                                )
                        )
                                .lines()
                                .collect(Collectors.joining("\n"));
                        var str2 = new StringBuilder();
                        for (var platform : normalizedRequestedPlatforms) {
                            str2.append(baseString.replaceAll("\\{PLATFORM_NAME}", platform));
                            str2.append("\n");
                        }
                        s = s.replace("{/*=Generate=reflection=fields*/}", str2.toString());
                    }
                    if (s.contains("{/*=Generate=reflection=enums*/}")) {
                        var baseString = new BufferedReader(
                                new InputStreamReader(
                                        Objects.requireNonNull(AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_enums.txt"))
                                )
                        )
                                .lines()
                                .collect(Collectors.joining("\n"));

                        var str2 = new StringBuilder();
                        for (var platform : normalizedRequestedPlatforms) {
                            str2.append(baseString.replaceAll("\\{PLATFORM_NAME}", platform));
                            str2.append("\n");
                        }
                        s = s.replace("{/*=Generate=reflection=enums*/}", str2.toString());
                    }
                    if (s.contains("{/*=Generate=reflection=methods*/}")) {
                        var baseString = new BufferedReader(
                                new InputStreamReader(
                                        Objects.requireNonNull(AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_methods.txt"))
                                )
                        )
                                .lines()
                                .collect(Collectors.joining("\n"));

                        var str2 = new StringBuilder();
                        for (var platform : normalizedRequestedPlatforms) {
                            str2.append(baseString.replaceAll("\\{PLATFORM_NAME}", platform));
                            str2.append("\n");
                        }
                        s = s.replace("{/*=Generate=reflection=methods*/}", str2.toString());
                    }
                    if (s.contains("{/*=Generate=mapper*/}")) {
                        var str2 = new StringBuilder();
                        for (var platform : normalizedRequestedPlatforms) {
                            var stream = AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_" + platform + "_mapper.txt");
                            if (stream != null) {
                                str2.append(new BufferedReader(new InputStreamReader(stream))
                                        .lines()
                                        .collect(Collectors.joining("\n")));
                                str2.append("\n");
                            }
                        }
                        s = s.replace("{/*=Generate=mapper*/}", str2.toString());
                    }
                    if (s.contains("{/*=Generate=initializers*/}")) {
                        var str2 = new StringBuilder();
                        var baseString = new BufferedReader(
                                new InputStreamReader(
                                        Objects.requireNonNull(AccessorClassGenerator.class.getResourceAsStream("/templates/AccessorUtils_init.txt"))
                                )
                        )
                                .lines()
                                .collect(Collectors.joining("\n"));

                        for (var platform : normalizedRequestedPlatforms) {
                            if (platformsRequiringInitializers.contains(platform) || AccessorClassGenerator.class.getResource("/templates/AccessorUtils_" + platform + "_static.txt") == null) {
                                str2.append(baseString.replaceAll("\\{PLATFORM_NAME}", platform));
                                str2.append("\n");
                                System.out.println("No static-initializer for " + platform + " platform is present. Please call AccessorUtils#initMapping_" + platform + " method as soon as possible (before using any nms accessors)");
                            }
                        }

                        s = s.replace("{/*=Generate=initializers*/}", str2.toString());
                    }
                    return s;
                })
                .collect(Collectors.joining("\n"));

        var ac = new File(projectFolder, configuration.getSourceSet() + "/" + basePackage.replace(".", "/") + "/AccessorUtils.java");
        Files.writeString(ac.toPath(), "package " + basePackage + ";\n\n" + str, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public CodeBlock generateMappings(ConfigurationNode node) {
        return generateMappings(List.of(node), false);
    }

    public CodeBlock generateMappings(ConfigurationNode node, boolean addComments) {
        return generateMappings(List.of(node), addComments);
    }

    public CodeBlock generateMappings(List<ConfigurationNode> nodes) {
        return generateMappings(nodes, false);
    }

    public CodeBlock generateMappings(List<ConfigurationNode> nodes, boolean addComments) {
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

        for (var platform : normalizedRequestedPlatforms) {
            generateSpecificMappingsIntoCodeBlock(obfuscatedFallback, usedNode, platform, codeBlock, addComments);
        }

        if (addComments) {
            codeBlock.add("\n");
        }

        return codeBlock
                .unindent()
                .add("}")
                .build();
    }

    private void generateSpecificMappingsIntoCodeBlock(List<Map.Entry<String, String>> obfuscatedFallback, Map<String, ConfigurationNode> usedNode, String mappings, CodeBlock.Builder codeBlock, boolean addComments) {
        var latest = new AtomicReference<String>();
        var latestUpToDateVersion = new AtomicReference<String>();
        var needsToEndLine = new AtomicBoolean();

        if (addComments) {
            codeBlock.add("\n/* " + mappings + " */\n");
        }

        obfuscatedFallback.forEach(entry -> {
            var value = entry.getValue();
            var seargeValue = usedNode.get(entry.getKey())
                    .node(mappings)
                    .childrenMap()
                    .entrySet()
                    .stream()
                    .filter(e -> Arrays.asList(e.getKey().toString().split(",")).contains(entry.getKey()))
                    .findFirst()
                    .map(e -> e.getValue().getString());

            if (seargeValue.isPresent()) {
                value = seargeValue.get();
            }

            if (latest.get() == null || !latest.get().equals(value)) {
                if (addComments && latestUpToDateVersion.get() != null) {
                    codeBlock.add(" - " + latestUpToDateVersion.get());
                    latestUpToDateVersion.set(null);
                }
                if (needsToEndLine.get()) {
                    codeBlock.add("\n");
                    needsToEndLine.set(false);
                }
                codeBlock.add("$N.$N($S, $S, $S);", "mapper", "map", mappings, entry.getKey(), value);
                latest.set(value);
                if (addComments) {
                    codeBlock.add(" // " + entry.getKey());
                    needsToEndLine.set(true);
                } else {
                    codeBlock.add("\n");
                }
            } else if (addComments) {
                latestUpToDateVersion.set(entry.getKey());
            }
        });

        if (addComments && latestUpToDateVersion.get() != null) {
            codeBlock.add(" - " + latestUpToDateVersion.get());
        }
        if (needsToEndLine.get()) {
            codeBlock.add("\n");
        }
    }
}
