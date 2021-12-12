package org.screamingsandals.nms.mapper.tasks;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.VersionNumber;
import org.screamingsandals.nms.mapper.joined.JoinedClassDefinition;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Rewrite this fucking mess from scratch
public abstract class JoinedMappingTask extends DefaultTask {
    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        System.out.println("Generating joined mapping...");

        var spigotForceMerge = Files.readAllLines(getProject().file("config/joined/spigot-class-force-merge.txt").toPath())
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank() && !s.startsWith("#"))
                    .collect(Collectors.toList());

        var versions = getUtils().get().getNewlyGeneratedMappings()
                .keySet()
                .stream()
                .sorted(Comparator.comparing(VersionNumber::parse).reversed())
                .collect(Collectors.toList());

        var mappings = getUtils().get().getMappings();

        var finalMapping = getUtils().get().getJoinedMappings();
        versions.forEach(version -> {
            var versionDefaultMapping = getUtils().get().getNewlyGeneratedMappings().get(version);

            var nextVersion = versions.stream()
                    .filter(a -> VersionNumber.parse(a).compareTo(VersionNumber.parse(version)) > 0)
                    .min(Comparator.comparing(VersionNumber::parse));

            System.out.println("Applying version " + version + "...");

            mappings.get(version).forEach((key, classDefinition) -> {
                try {
                    var finalClassName = getJoinedClassName(classDefinition, spigotForceMerge);
                    if (!finalMapping.containsKey(finalClassName)) {
                        finalMapping.put(finalClassName, new JoinedClassDefinition());
                    }
                    var definition = finalMapping.get(finalClassName);
                    classDefinition.getMapping().forEach((mappingType, s) -> definition.getMapping()
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().equals(s) && entry.getKey().getValue() == mappingType)
                            .findFirst()
                            .ifPresentOrElse(entry -> {
                                definition.getMapping().remove(entry.getKey());
                                definition.getMapping().put(Map.entry(entry.getKey().getKey() + "," + version, entry.getKey().getValue()), entry.getValue());
                            }, () -> definition.getMapping().put(Map.entry(version, mappingType), s)));

                    classDefinition.getConstructors().forEach(constructorDefinition -> definition.getConstructors()
                            .stream()
                            .filter(joinedConstructor -> constructorDefinition.getParameters()
                                    .stream()
                                    .map(link -> remapParameterType(version, link, spigotForceMerge))
                                    .collect(Collectors.toList())
                                    .equals(joinedConstructor.getParameters())
                            )
                            .findFirst()
                            .ifPresentOrElse(joinedConstructor -> joinedConstructor.getSupportedVersions().add(version), () -> {
                                var constructor = new JoinedClassDefinition.JoinedConstructor();
                                constructor.getSupportedVersions().add(version);
                                constructor.getParameters().addAll(constructorDefinition.getParameters()
                                        .stream()
                                        .map(link -> remapParameterType(version, link, spigotForceMerge))
                                        .collect(Collectors.toList()));
                                definition.getConstructors().add(constructor);
                            }));

                    classDefinition.getFields().forEach((s, fieldDefinition) -> {
                        definition.getFields()
                                .stream()
                                .filter(joinedField -> joinedField.getType().equals(remapParameterType(version, fieldDefinition.getType(), spigotForceMerge))
                                        && compareMappings(joinedField.getMapping(), nextVersion.orElse(""), versionDefaultMapping, fieldDefinition.getMapping())
                                )
                                .findFirst()
                                .ifPresentOrElse(joinedField -> fieldDefinition.getMapping()
                                        .forEach((mappingType, s3) -> joinedField.getMapping()
                                                .entrySet()
                                                .stream()
                                                .filter(entry -> entry.getValue().equals(s3) && entry.getKey().getValue() == mappingType)
                                                .findFirst()
                                                .ifPresentOrElse(entry -> {
                                                            joinedField.getMapping().remove(entry.getKey());
                                                            joinedField.getMapping().put(Map.entry(entry.getKey().getKey() + "," + version, entry.getKey().getValue()), entry.getValue());
                                                        },
                                                        () -> joinedField.getMapping().put(Map.entry(version, mappingType), s3))), () -> {
                                    var joinedField = new JoinedClassDefinition.JoinedField(remapParameterType(version, fieldDefinition.getType(), spigotForceMerge));
                                    fieldDefinition.getMapping()
                                            .forEach((mappingType, s1) -> joinedField.getMapping().put(Map.entry(version, mappingType), s1));

                                    definition.getFields().add(joinedField);
                                });
                    });


                    classDefinition.getMethods().forEach(methodDefinition -> {
                        definition.getMethods()
                                .stream()
                                .filter(joinedMethod -> joinedMethod.getReturnType().equals(remapParameterType(version, methodDefinition.getReturnType(), spigotForceMerge))
                                        && compareMappings(joinedMethod.getMapping(), nextVersion.orElse(""), versionDefaultMapping, methodDefinition.getMapping())
                                        && methodDefinition.getParameters()
                                        .stream()
                                        .map(link -> remapParameterType(version, link, spigotForceMerge))
                                        .collect(Collectors.toList())
                                        .equals(joinedMethod.getParameters()))
                                .findFirst()
                                .ifPresentOrElse(joinedMethod -> methodDefinition.getMapping()
                                        .forEach((mappingType, s3) -> joinedMethod.getMapping()
                                                .entrySet()
                                                .stream()
                                                .filter(entry -> entry.getValue().equals(s3) && entry.getKey().getValue() == mappingType)
                                                .findFirst()
                                                .ifPresentOrElse(entry -> {
                                                            joinedMethod.getMapping().remove(entry.getKey());
                                                            joinedMethod.getMapping().put(Map.entry(entry.getKey().getKey() + "," + version, entry.getKey().getValue()), entry.getValue());
                                                        },
                                                        () -> joinedMethod.getMapping().put(Map.entry(version, mappingType), s3))), () -> {
                                    var joinedMethod = new JoinedClassDefinition.JoinedMethod(remapParameterType(version, methodDefinition.getReturnType(), spigotForceMerge));
                                    methodDefinition.getMapping()
                                            .forEach((mappingType, s1) -> joinedMethod.getMapping().put(Map.entry(version, mappingType), s1));
                                    joinedMethod.getParameters().addAll(methodDefinition.getParameters()
                                            .stream()
                                            .map(link -> remapParameterType(version, link, spigotForceMerge))
                                            .collect(Collectors.toList()));

                                    definition.getMethods().add(joinedMethod);
                                });
                    });
                } catch (DigestException e) {
                    e.printStackTrace();
                }
            });
        });

        finalMapping.forEach((s, joinedClassDefinition) -> {
            joinedClassDefinition.getMapping().forEach((stringMappingTypeEntry, s1) -> {
                if (stringMappingTypeEntry.getValue() == MappingType.SEARGE) {
                    getUtils().get().getSeargeJoinedMappingsClassLinks().put(s1, s);
                } else if (stringMappingTypeEntry.getValue() == MappingType.INTERMEDIARY) {
                    getUtils().get().getIntermediaryJoinedMappingsClassLinks().put(s1, s);
                }

                Arrays.stream(stringMappingTypeEntry.getKey().split(",")).forEach(s2 -> {
                    var map = getUtils().get().getMappingTypeLinks().computeIfAbsent(s2, k -> new HashMap<>());
                    var mappingType = map.computeIfAbsent(stringMappingTypeEntry.getValue(), k -> new HashMap<>());
                    if (stringMappingTypeEntry.getValue() == MappingType.SPIGOT) {
                        var dot = s1.lastIndexOf(".");
                        mappingType.put(dot < 0 ? s1 : s1.substring(dot + 1), s);
                    } else {
                        mappingType.put(s1, s);
                    }
                });
            });
        });
    }

    public String getJoinedClassName(ClassDefinition classDefinition, List<String> spigotForceMerge) throws DigestException {
        if (classDefinition.getJoinedKey() != null) {
            return classDefinition.getJoinedKey();
        }

        var mojMap = classDefinition.getMapping().get(MappingType.MOJANG);
        var spigotLinks = getUtils().get().getSpigotJoinedMappingsClassLinks();

        if (mojMap == null) {
            // YAY, we are on an older version
            var spigot = classDefinition.getMapping().get(MappingType.SPIGOT);

            if (spigot != null) {
                spigot = spigot.substring(spigot.lastIndexOf(".") + 1);
                var hash = spigotLinks.get(spigot);

                if (hash != null) {
                    classDefinition.setJoinedKey(hash);
                    return hash;
                } else {
                    var byteArray = digest.digest(spigot.getBytes(StandardCharsets.UTF_8));
                    var longHash = new StringBuilder();

                    for (var b : byteArray) {
                        longHash.append(Integer.toHexString(0xFF & b));
                    }

                    var length = 7;

                    while (true) {
                        hash = "c_old_" + longHash.substring(0, length);

                        if (spigotLinks.containsValue(hash)) {
                            length++;
                        } else {
                            classDefinition.setJoinedKey(hash);
                            spigotLinks.put(spigot, hash);
                            return hash;
                        }
                    }
                }
            } else {
                var obfuscated = classDefinition.getMapping().get(MappingType.OBFUSCATED);

                var byteArray = digest.digest(obfuscated.getBytes(StandardCharsets.UTF_8));
                var longHash = new StringBuilder();

                for (var b : byteArray) {
                    longHash.append(Integer.toHexString(0xFF & b));
                }

                var length = 7;

                while (true) {
                    var hash = "c_undefined_" + longHash.substring(0, length);

                    if (getUtils().get().getUndefinedClassLinks().contains(hash)) {
                        length++;
                    } else {
                        classDefinition.setJoinedKey(hash);
                        System.out.println("Class without any mapping: " + obfuscated + ", hash: " + hash);
                        getUtils().get().getUndefinedClassLinks().add(hash);
                        return hash;
                    }
                }
            }
        }

        var links = getUtils().get().getJoinedMappingsClassLinks();

        if (links.containsKey(mojMap)) {
            var hash = links.get(mojMap);
            classDefinition.setJoinedKey(hash);
            var spigot = classDefinition.getMapping().get(MappingType.SPIGOT);
            if (spigot != null) {
                spigot = spigot.substring(spigot.lastIndexOf(".") + 1);
                if (!spigotLinks.containsKey(spigot)) {
                    spigotLinks.put(spigot, hash);
                }
            }
            return hash;
        }

        // check if newer spigot thinks that the class is still the same
        var checkIfNewerSpigot = classDefinition.getMapping().get(MappingType.SPIGOT);
        if (checkIfNewerSpigot != null && spigotForceMerge.contains(checkIfNewerSpigot)) { // also check if merge is allowed
            checkIfNewerSpigot = checkIfNewerSpigot.substring(checkIfNewerSpigot.lastIndexOf(".") + 1);
            var hash = spigotLinks.get(checkIfNewerSpigot);

            if (hash != null) {
                // Yay, Mojang mappings are different, but Spigot mappings are the same, so we assume it's the same class
                links.put(mojMap, hash);
                classDefinition.setJoinedKey(hash);
                return hash;
            }
        }

        var byteArray = digest.digest(mojMap.getBytes(StandardCharsets.UTF_8));
        var longHash = new StringBuilder();

        for (var b : byteArray) {
            longHash.append(Integer.toHexString(0xFF & b));
        }

        var length = 7;

        while (true) {
            var hash = "c_" + longHash.substring(0, length);

            if (links.containsValue(hash)) {
                length++;
            } else {
                classDefinition.setJoinedKey(hash);
                links.put(mojMap, hash);
                if (classDefinition.getMapping().containsKey(MappingType.SPIGOT)) {
                    // current spigot 1.17 has still unique names like before just another packages
                    var spigot = classDefinition.getMapping().get(MappingType.SPIGOT);
                    spigot = spigot.substring(spigot.lastIndexOf(".") + 1);
                    getUtils().get().getSpigotJoinedMappingsClassLinks().put(spigot, hash);
                }
                return hash;
            }
        }
    }

    @SneakyThrows
    public ClassDefinition.Link remapParameterType(String version, ClassDefinition.Link link, List<String> spigotForceMerge) {
        if (link.isNms()) {
            var type = link.getType();
            var suffix = new StringBuilder();
            while (type.endsWith("[]")) {
                suffix.append("[]");
                type = type.substring(0, type.length() - 2);
            }
            if (type.matches(".*\\$\\d+")) { // WTF? How
                suffix.insert(0, type.substring(type.lastIndexOf("$")));
                type = type.substring(0, type.lastIndexOf("$"));
            }
            return ClassDefinition.Link.nmsLink(getJoinedClassName(getUtils().get().getMappings().get(version).get(type), spigotForceMerge) + suffix);
        }
        return link;
    }

    public boolean compareMappings(Map<Map.Entry<String, MappingType>, String> mapping, String anotherVersion, MappingType baseMappingType, Map<MappingType, String> versionSpecificMapping) {
        return mapping
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getValue() == baseMappingType && Arrays.asList(entry.getKey().getKey().split(",")).contains(anotherVersion) && versionSpecificMapping.containsKey(entry.getKey().getValue()))
                .findFirst()
                .or(() ->
                        mapping
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().getValue() == MappingType.INTERMEDIARY && Arrays.asList(entry.getKey().getKey().split(",")).contains(anotherVersion) && versionSpecificMapping.containsKey(entry.getKey().getValue()))
                                .findFirst()
                                .or(() -> mapping
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getKey().getValue() == MappingType.SEARGE && Arrays.asList(entry.getKey().getKey().split(",")).contains(anotherVersion) && versionSpecificMapping.containsKey(entry.getKey().getValue()))
                                        .findFirst()
                                        .or(() -> mapping
                                                .entrySet()
                                                .stream()
                                                .filter(entry -> entry.getKey().getValue() == MappingType.OBFUSCATED && Arrays.asList(entry.getKey().getKey().split(",")).contains(anotherVersion))
                                                .findFirst()
                                        )
                                )
                )
                .map(entry -> Map.entry(entry.getKey().getValue(), entry.getValue()))
                .map(entry -> entry.getValue().equals(versionSpecificMapping.get(entry.getKey())))
                .orElse(false);
    }
}
