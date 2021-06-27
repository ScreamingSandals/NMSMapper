package org.screamingsandals.nms.mapper.parser;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.Type;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class VanillaJarParser {
    public static Map.Entry<Map<String, ClassDefinition>, List<String>> map(File vanillaJar) throws IOException {
        var map = new HashMap<String, ClassDefinition>();
        var excludedSynthetic = new ArrayList<String>();

        try (var zip = new ZipFile(vanillaJar)) {
            var entries = zip.stream()
                    .filter(e -> e.getName().matches("net/minecraft/.*\\.class|[^/]+\\.class") && !e.isDirectory())
                    .map(e -> {
                        try (var i = zip.getInputStream(e)) {
                            return new ClassParser(i, e.getName()).parse();
                        } catch (IOException exception) {
                            exception.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(e -> !e.isAnonymous() && !e.isSynthetic())
                    .filter(e -> !e.getClassName().equals("net.minecraft.data.Main")) // net.minecraft.data.Main is excluded from custom servers
                    .collect(Collectors.toList());

            entries.forEach(javaClass -> {
                var definition = new ClassDefinition();
                map.put(javaClass.getClassName(), definition);
                definition.setModifier(javaClass.getModifiers());
                if (javaClass.isInterface()) {
                    definition.setType(ClassDefinition.Type.INTERFACE);
                } else if (javaClass.isAnnotation()) {
                    definition.setType(ClassDefinition.Type.ANNOTATION);
                } else if (javaClass.isEnum()) {
                    definition.setType(ClassDefinition.Type.ENUM);
                }
                definition.getMapping().put(MappingType.OBFUSCATED, javaClass.getClassName());
            });

            entries.forEach(javaClass -> {
                var definition = map.get(javaClass.getClassName());

                var superType = javaClass.getSuperclassName().replaceAll("<[^>]>", "");
                definition.setSuperclass(map.containsKey(superType) ? ClassDefinition.Link.nmsLink(superType) : ClassDefinition.Link.casualLink(superType));

                Arrays.stream(javaClass.getInterfaceNames())
                        .map(e -> e.replaceAll("<[^>]>", ""))
                        .map(s -> {
                            if (map.containsKey(s)) {
                                return ClassDefinition.Link.nmsLink(s);
                            } else {
                                return ClassDefinition.Link.casualLink(s);
                            }
                        })
                        .forEach(definition.getInterfaces()::add);

                for (var field : javaClass.getFields()) {
                    if (field.isSynthetic()) {
                        excludedSynthetic.add(javaClass.getClassName() + " field " + field.getName());
                        continue;
                    }

                    var type = SpigotMappingParser.convertInternal(field.getType().getSignature().replaceAll("<[^>]>", ""));

                    ClassDefinition.Link link;
                    if (map.containsKey(type.replaceAll("\\[]", ""))) {
                        link = ClassDefinition.Link.nmsLink(type);
                    } else {
                        link = ClassDefinition.Link.casualLink(type);
                    }

                    var fieldDef = new ClassDefinition.FieldDefinition(link);
                    fieldDef.getMapping().put(MappingType.OBFUSCATED, field.getName());
                    fieldDef.setModifier(field.getModifiers());

                    definition.getFields().put(field.getName(), fieldDef);
                }

                for (var method : javaClass.getMethods()) {
                    if (method.getName().equals("<clinit>")) // we don't need static initializers and black magic
                        continue;

                    if (method.getName().equals("<init>")) {
                        // Constructor
                        var constrcutorDef = new ClassDefinition.ConstructorDefinition();
                        constrcutorDef.setModifier(method.getModifiers());
                        Arrays.stream(method.getArgumentTypes()).map(type -> {
                            var rType = SpigotMappingParser.convertInternal(type.getSignature().replaceAll("<[^>]>", ""));
                            if (map.containsKey(rType.replaceAll("\\[]", ""))) {
                                return ClassDefinition.Link.nmsLink(rType);
                            } else {
                                return ClassDefinition.Link.casualLink(rType);
                            }
                        }).forEach(constrcutorDef.getParameters()::add);
                        definition.getConstructors().add(constrcutorDef);
                    } else {

                        if (method.isSynthetic()) {
                            excludedSynthetic.add(javaClass.getClassName() + " method " + method.getName() + "(" + Arrays.stream(method.getArgumentTypes()).map(Type::getSignature).map(SpigotMappingParser::convertInternal).collect(Collectors.joining(",")) + ")");
                            continue;
                        }

                        // Method
                        var returnType = SpigotMappingParser.convertInternal(method.getReturnType().getSignature().replaceAll("<[^>]>", ""));

                        ClassDefinition.Link link;
                        if (map.containsKey(returnType.replaceAll("\\[]", ""))) {
                            link = ClassDefinition.Link.nmsLink(returnType);
                        } else {
                            link = ClassDefinition.Link.casualLink(returnType);
                        }

                        var methodDef = new ClassDefinition.MethodDefinition(link);
                        methodDef.getMapping().put(MappingType.OBFUSCATED, method.getName());
                        methodDef.setModifier(method.getModifiers());
                        Arrays.stream(method.getArgumentTypes()).map(type -> {
                            var rType = SpigotMappingParser.convertInternal(type.getSignature().replaceAll("<[^>]>", ""));
                            if (map.containsKey(rType.replaceAll("\\[]", ""))) {
                                return ClassDefinition.Link.nmsLink(rType);
                            } else {
                                return ClassDefinition.Link.casualLink(rType);
                            }
                        }).forEach(methodDef.getParameters()::add);
                        definition.getMethods().add(methodDef);
                    }
                }
            });
        }

        return Map.entry(map, excludedSynthetic);
    }
}
