package org.screamingsandals.nms.mapper.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.VersionNumber;
import org.screamingsandals.nms.mapper.joined.JoinedClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class JoinedMappingTask extends DefaultTask {
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @TaskAction
    public void run() {
        System.out.println("Generating joined mapping...");

        var versions = getUtils().get().getNewlyGeneratedMappings().stream().sorted(Comparator.comparing(VersionNumber::parse).reversed()).collect(Collectors.toList());

        var mappings = getUtils().get().getMappings();

        var finalMapping = new HashMap<String, JoinedClassDefinition>();
        versions.forEach(version ->
                mappings.get(version).forEach((key, classDefinition) -> {
                    if (!finalMapping.containsKey(key)) {
                        finalMapping.put(key, new JoinedClassDefinition());
                    }
                    var definition = finalMapping.get(key);
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
                            .filter(joinedConstructor -> constructorDefinition.getParameters().equals(joinedConstructor.getParameters()))
                            .findFirst()
                            .ifPresentOrElse(joinedConstructor -> joinedConstructor.getSupportedVersions().add(version), () -> {
                                var constructor = new JoinedClassDefinition.JoinedConstructor();
                                constructor.getSupportedVersions().add(version);
                                constructor.getParameters().addAll(constructorDefinition.getParameters());
                                definition.getConstructors().add(constructor);
                            }));

                    classDefinition.getFields().forEach((s, fieldDefinition) -> {
                        definition.getFields()
                                .stream()
                                .filter(joinedField -> joinedField.getType().equals(fieldDefinition.getType()) && joinedField.getMapping()
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getKey().getValue() == MappingType.MOJANG)
                                        .map(Map.Entry::getValue)
                                        .findFirst()
                                        .orElse("")
                                        .equals(fieldDefinition.getMapping().get(MappingType.MOJANG)))
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
                                    var joinedField = new JoinedClassDefinition.JoinedField(fieldDefinition.getType());
                                    fieldDefinition.getMapping()
                                            .forEach((mappingType, s1) -> joinedField.getMapping().put(Map.entry(version, mappingType), s1));

                                    definition.getFields().add(joinedField);
                                });
                    });


                    classDefinition.getMethods().forEach(methodDefinition -> {
                        definition.getMethods()
                                .stream()
                                .filter(joinedMethod -> joinedMethod.getReturnType().equals(methodDefinition.getReturnType())
                                        && joinedMethod.getMapping()
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getKey().getValue() == MappingType.MOJANG)
                                        .map(Map.Entry::getValue)
                                        .findFirst()
                                        .orElse("")
                                        .equals(methodDefinition.getMapping().get(MappingType.MOJANG))
                                        && methodDefinition.getParameters().equals(joinedMethod.getParameters()))
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
                                    var joinedMethod = new JoinedClassDefinition.JoinedMethod(methodDefinition.getReturnType());
                                    methodDefinition.getMapping()
                                            .forEach((mappingType, s1) -> joinedMethod.getMapping().put(Map.entry(version, mappingType), s1));
                                    joinedMethod.getParameters().addAll(methodDefinition.getParameters());

                                    definition.getMethods().add(joinedMethod);
                                });
                    });
                })
        );

        System.out.println("Saving joined mappings");

        var saver = GsonConfigurationLoader.builder()
                .file(new File(getUtils().get().getResourceDir(), "joined.json"))
                .build();

        var mainNode = saver.createNode();

        finalMapping.forEach((k, v) -> {
            try {
                v.asNode(mainNode.node(k));
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });

        try {
            saver.save(mainNode);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }

    }
}
