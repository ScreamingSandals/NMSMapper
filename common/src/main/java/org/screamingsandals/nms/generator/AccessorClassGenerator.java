package org.screamingsandals.nms.generator;

import com.squareup.javapoet.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.screamingsandals.nms.generator.configuration.NMSMapperConfiguration;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.lang.model.element.Modifier;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AccessorClassGenerator {
    @SuppressWarnings("unchecked")
    public static void run(NMSMapperConfiguration configuration, File projectFolder) throws IOException {
        var neeededClasses = configuration.getNeededClasses();
        var basePackage = configuration.getBasePackage();
        System.out.println("Generated accessors will be saved in: " + projectFolder.toPath().resolve(configuration.getSourceSet() + "/" + basePackage.replace(".", "/")).toAbsolutePath().toString());

        if (configuration.isCleanOnRebuild()) {
            System.out.println("Cleaning workspace");
            var file = new File(projectFolder, configuration.getSourceSet() + "/" + basePackage.replace(".", "/"));
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
        }

        System.out.println("Loading joined mappings");
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

        var mojangClassNameTranslate = joinedMappings.node("classNames");
        var spigotClassNameTranslate = joinedMappings.node("spigotNames");

        System.out.println("Copying AccessorUtils");
        var accessorUtils = ClassName.get(basePackage, "AccessorUtils");

        var classAccessors = new HashMap<String, Map.Entry<String, TypeSpec.Builder>>();

        System.out.println("Generating accessors for classes and their fields");

        // First iteration: adding required classes and their fields
        neeededClasses.forEach(requiredClass -> {
            String translated;
            if (requiredClass.getClazz().startsWith("spigot:")) {
                translated = spigotClassNameTranslate.node(requiredClass.getClazz().substring(7)).getString();
            } else if (requiredClass.getClazz().startsWith("hash:")) {
                translated = requiredClass.getClazz().substring(5);
            } else {
                translated = mojangClassNameTranslate.node(requiredClass.getClazz()).getString();
            }
            System.out.println("Generating accessor for " + requiredClass.getClazz());

            if (translated == null) {
                throw new IllegalArgumentException("Can't find class: " + requiredClass.getClazz());
            }

            var li = requiredClass.getClazz().lastIndexOf(".");
            var name = (requiredClass.getClazz().substring(li < 0 ? (requiredClass.getClazz().startsWith("spigot:") ? 7 : 0) : (li + 1)) + "Accessor").replace("$", "_i_");

            var builder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC);

            var typeMapping = joinedMappings.node(translated);

            var typeMethod = MethodSpec.methodBuilder("getType")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get("", "?")))
                    .addStatement("return $T.$N($T.class, $L)", accessorUtils, "getType", ClassName.get(basePackage, name), generateMappings(typeMapping))
                    .build();
            builder.addMethod(typeMethod);

            var nameCounter = new HashMap<String, Integer>();

            requiredClass.getFields().forEach(s1 -> {
                var split = s1.split(":");
                var type = "MOJANG";
                var fieldName = split[0];
                if (split.length > 1) {
                    type = split[0].toUpperCase();
                    fieldName = split[1];
                }
                var forceVersion = split.length > 2 ? split[2] : null;

                var finalType = type;
                var finalFieldName = fieldName;
                typeMapping.node("fields")
                        .childrenList()
                        .stream()
                        .filter(n -> n.node(finalType).childrenMap().entrySet().stream().anyMatch(n1 ->
                                n1.getValue().getString("").equals(finalFieldName) && (forceVersion == null || Arrays.asList(n1.getKey().toString().split(",")).contains(forceVersion))
                        ))
                        .findFirst()
                        .ifPresentOrElse(n -> {
                            var capitalized = finalFieldName.substring(0, 1).toUpperCase();
                            if (finalFieldName.length() > 1) {
                                capitalized += finalFieldName.substring(1);
                            }

                            int count;
                            if (!nameCounter.containsKey(finalFieldName)) {
                                nameCounter.put(finalFieldName, 1);
                                count = 1;
                            } else {
                                count = nameCounter.get(finalFieldName) + 1;
                                nameCounter.put(finalFieldName, count);
                            }

                            var method = MethodSpec.methodBuilder("getField" + capitalized + (count != 1 ? count : ""))
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .returns(Field.class)
                                    .addStatement("return $T.$N($T.class, $S, $L)", accessorUtils, "getField", ClassName.get(basePackage, name), finalFieldName + count, generateMappings(n))
                                    .build();
                            builder.addMethod(method);
                        }, () -> {
                            throw new IllegalArgumentException("Can't find field: " + finalFieldName);
                        });
            });

            requiredClass.getEnumFields().forEach(s1 -> {
                var split = s1.split(":");
                var type = "MOJANG";
                var fieldName = split[0];
                if (split.length > 1) {
                    type = split[0].toUpperCase();
                    fieldName = split[1];
                }
                var forceVersion = split.length > 2 ? split[2] : null;

                var finalType = type;
                var finalFieldName = fieldName;
                typeMapping.node("fields")
                        .childrenList()
                        .stream()
                        .filter(n -> n.node(finalType).childrenMap().entrySet().stream().anyMatch(
                                n1 -> n1.getValue().getString("").equals(finalFieldName) && (forceVersion == null || Arrays.asList(n1.getKey().toString().split(",")).contains(forceVersion))
                        ))
                        .findFirst()
                        .ifPresentOrElse(n -> {
                            var capitalized = finalFieldName.substring(0, 1).toUpperCase();
                            if (finalFieldName.length() > 1) {
                                capitalized += finalFieldName.substring(1);
                            }

                            int count;
                            if (!nameCounter.containsKey(finalFieldName)) {
                                nameCounter.put(finalFieldName, 1);
                                count = 1;
                            } else {
                                count = nameCounter.get(finalFieldName) + 1;
                                nameCounter.put(finalFieldName, count);
                            }

                            var method = MethodSpec.methodBuilder("getField" + capitalized + (count != 1 ? count : ""))
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .returns(Object.class)
                                    .addStatement("return $T.$N($T.class, $S, $L)", accessorUtils, "getEnumField", ClassName.get(basePackage, name), finalFieldName + count, generateMappings(n))
                                    .build();
                            builder.addMethod(method);
                        }, () -> {
                            throw new IllegalArgumentException("Can't find field: " + finalFieldName);
                        });
            });

            classAccessors.put(translated, Map.entry(name, builder));
        });

        // second iteration: adding missing classes required by constructors and methods
        neeededClasses.forEach(requiredClass -> {
            requiredClass.getConstructors().forEach(strings -> {
                for (var str : strings) {
                    addMissingClasses(basePackage, joinedMappings, mojangClassNameTranslate, spigotClassNameTranslate, accessorUtils, classAccessors, str);
                }
            });

            requiredClass.getMethods().forEach(entry -> {
                for (var str : entry.getValue()) {
                    addMissingClasses(basePackage, joinedMappings, mojangClassNameTranslate, spigotClassNameTranslate, accessorUtils, classAccessors, str);
                }
            });
        });

        System.out.println("Generating constructors and methods accessors");

        // third iteration: adding constructors and methods
        neeededClasses.forEach(requiredClass -> {
            System.out.println("Processing " + requiredClass.getClazz());

            String translated;
            if (requiredClass.getClazz().startsWith("spigot:")) {
                translated = spigotClassNameTranslate.node(requiredClass.getClazz().substring(7)).getString();
            } else {
                translated = mojangClassNameTranslate.node(requiredClass.getClazz()).getString();
            }

            var typeMapping = joinedMappings.node(translated);

            var entry = classAccessors.get(translated);
            var name = entry.getKey();
            var builder = entry.getValue();

            var constructorCounter = new AtomicInteger();

            requiredClass.getConstructors().forEach(strings -> {
                var classes = Arrays.stream(strings)
                        .map(s -> {
                            if (s.startsWith("&")) {
                                if (getArrayDimensionsCount(s) > 0) {
                                    Map.Entry<String, TypeSpec.Builder> data;
                                    var map = new HashMap<>();
                                    if (s.startsWith("&spigot:")) {
                                        data = classAccessors.get(spigotClassNameTranslate.node(removeArrayDimensionsFromArgument(s.substring(8))).getString());
                                    } else {
                                        data = classAccessors.get(mojangClassNameTranslate.node(removeArrayDimensionsFromArgument(s.substring(1))).getString());
                                    }
                                    map.put("data", data);
                                    map.put("dimensions", getArrayDimensionsCount(s));
                                    return map;
                                } else {
                                    if (s.startsWith("&spigot:")) {
                                        return classAccessors.get(spigotClassNameTranslate.node(s.substring(8)).getString());
                                    } else {
                                        return classAccessors.get(mojangClassNameTranslate.node(s.substring(1)).getString());
                                    }
                                }
                            } else {
                                return s;
                            }
                        })
                        .collect(Collectors.toList());

                // TODO: check existence of constructor

                var id = constructorCounter.getAndIncrement();
                var args = new ArrayList<Object>(List.of(accessorUtils, "getConstructor", ClassName.get(basePackage, name), id));

                var strBuilder = new StringBuilder();

                classes.forEach(s -> {
                    strBuilder.append(", ");
                    if (s instanceof Map) {
                        strBuilder.append("$T.$N($T.$N(), $L).$N()");
                        args.add(Array.class);
                        args.add("newInstance");
                        args.add(ClassName.get(basePackage, ((Map.Entry<String, ?>) ((Map<?, ?>) s).get("data")).getKey()));
                        args.add("getType");
                        args.add((int) ((Map<?, ?>) s).get("dimensions"));
                        args.add("getClass");
                    } else if (s instanceof Map.Entry) {
                        strBuilder.append("$T.$N()");
                        args.add(ClassName.get(basePackage, ((Map.Entry<String, ?>) s).getKey()));
                        args.add("getType");
                    } else {
                        strBuilder.append("$T.class");
                        var s2 = convertInternal(s.toString());
                        var l = s2.lastIndexOf(".");
                        var packageN = s2.substring(0, Math.max(l, 0));
                        var className = s2.substring(l + 1);
                        var split = className.split("\\$");
                        var fistClassName = split[0];
                        var rest = Arrays.stream(split).skip(1).toArray(String[]::new);

                        if (packageN.isEmpty()) { // fix primitive arrays

                            switch (fistClassName) {
                                case "I[]":
                                    fistClassName = "int[]";
                                    break;
                                case "Z[]":
                                    fistClassName = "boolean[]";
                                    break;
                                case "J[]":
                                    fistClassName = "long[]";
                                    break;
                                case "B[]":
                                    fistClassName = "byte[]";
                                    break;
                                case "D[]":
                                    fistClassName = "double[]";
                                    break;
                                case "F[]":
                                    fistClassName = "float[]";
                                    break;
                                case "C[]":
                                    fistClassName = "char[]";
                                    break;
                                case "S[]":
                                    fistClassName = "short[]";
                                    break;
                            }
                        }

                        args.add(ClassName.get(packageN, fistClassName, rest));
                    }
                });

                var method = MethodSpec.methodBuilder("getConstructor" + id)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Constructor.class), ClassName.get("", "?")))
                        .addStatement("return $T.$N($T.class, $L" + strBuilder + ")", args.toArray())
                        .build();
                builder.addMethod(method);
            });

            var nameCounter = new HashMap<String, Integer>();

            requiredClass.getMethods().forEach(method -> {
                var classes = Arrays.stream(method.getValue())
                        .map(s -> {
                            if (s.startsWith("&")) {
                                if (getArrayDimensionsCount(s) > 0) {
                                    Map.Entry<String, TypeSpec.Builder> data;
                                    var map = new HashMap<>();
                                    if (s.startsWith("&spigot:")) {
                                        data = classAccessors.get(spigotClassNameTranslate.node(removeArrayDimensionsFromArgument(s.substring(8))).getString());
                                    } else {
                                        data = classAccessors.get(mojangClassNameTranslate.node(removeArrayDimensionsFromArgument(s.substring(1))).getString());
                                    }
                                    map.put("data", data);
                                    map.put("dimensions", getArrayDimensionsCount(s));
                                    return map;
                                } else {
                                    if (s.startsWith("&spigot:")) {
                                        return classAccessors.get(spigotClassNameTranslate.node(s.substring(8)).getString());
                                    } else {
                                        return classAccessors.get(mojangClassNameTranslate.node(s.substring(1)).getString());
                                    }
                                }
                            } else {
                                return s;
                            }
                        })
                        .collect(Collectors.toList());

                var params = Arrays.stream(method.getValue())
                        .map(s -> {
                            if (s.startsWith("&")) {
                                if (s.startsWith("&spigot:")) {
                                    return "&" + spigotClassNameTranslate.node(removeArrayDimensionsFromArgument(s.substring(8))).getString() + "[]".repeat(getArrayDimensionsCount(s.substring(8)));
                                } else {
                                    return "&" + mojangClassNameTranslate.node(removeArrayDimensionsFromArgument(s.substring(1))).getString() + "[]".repeat(getArrayDimensionsCount(s.substring(1)));
                                }
                            } else {
                                return s;
                            }
                        })
                        .collect(Collectors.toList());

                var split = method.getKey().split(":");
                var type = "MOJANG";
                var methodName = split[0];
                if (split.length > 1) {
                    type = split[0].toUpperCase();
                    methodName = split[1];
                }
                var forceVersion = split.length > 2 ? split[2] : null;

                var finalType = type;
                var finalMethodName = methodName;
                typeMapping.node("methods")
                        .childrenList()
                        .stream()
                        .filter(n -> {
                                    try {
                                        return n.node(finalType)
                                                .childrenMap()
                                                .entrySet()
                                                .stream()
                                                .anyMatch(n1 -> n1.getValue().getString("").equals(finalMethodName)
                                                        && (forceVersion == null || Arrays.asList(n1.getKey().toString().split(",")).contains(forceVersion)))
                                                &&
                                                Objects.equals(n.node("parameters").getList(String.class), params);
                                    } catch (SerializationException e) {
                                        e.printStackTrace();
                                    }
                                    return false;
                                }
                        )
                        .findFirst()
                        .ifPresentOrElse(n -> {
                            var capitalized = finalMethodName.substring(0, 1).toUpperCase();
                            if (finalMethodName.length() > 1) {
                                capitalized += finalMethodName.substring(1);
                            }

                            int count;
                            if (!nameCounter.containsKey(finalMethodName)) {
                                nameCounter.put(finalMethodName, 1);
                                count = 1;
                            } else {
                                count = nameCounter.get(finalMethodName) + 1;
                                nameCounter.put(finalMethodName, count);
                            }

                            var args = new ArrayList<>(List.of(accessorUtils, "getMethod", ClassName.get(basePackage, name), finalMethodName + count, generateMappings(n))); // you know what gradle? fuck off

                            var strBuilder = new StringBuilder();

                            classes.forEach(s -> {
                                strBuilder.append(", ");
                                if (s instanceof Map) {
                                    strBuilder.append("$T.$N($T.$N(), $L).$N()");
                                    args.add(Array.class);
                                    args.add("newInstance");
                                    args.add(ClassName.get(basePackage, ((Map.Entry<String, ?>) ((Map<?, ?>) s).get("data")).getKey()));
                                    args.add("getType");
                                    args.add((int) ((Map<?, ?>) s).get("dimensions"));
                                    args.add("getClass");
                                } else  if (s instanceof Map.Entry) {
                                    strBuilder.append("$T.$N()");
                                    args.add(ClassName.get(basePackage, ((Map.Entry) s).getKey().toString()));
                                    args.add("getType");
                                } else {
                                    strBuilder.append("$T.class");
                                    var s2 = convertInternal(s.toString());
                                    var l = s2.lastIndexOf(".");
                                    var packageN = s2.substring(0, Math.max(l, 0));
                                    var className = s2.substring(l + 1);
                                    var sp = className.split("\\$");
                                    var fistClassName = sp[0];
                                    var rest = Arrays.stream(sp).skip(1).toArray(String[]::new);

                                    if (packageN.isEmpty()) { // fix primitive arrays

                                        switch (fistClassName) {
                                            case "I[]":
                                                fistClassName = "int[]";
                                                break;
                                            case "Z[]":
                                                fistClassName = "boolean[]";
                                                break;
                                            case "J[]":
                                                fistClassName = "long[]";
                                                break;
                                            case "B[]":
                                                fistClassName = "byte[]";
                                                break;
                                            case "D[]":
                                                fistClassName = "double[]";
                                                break;
                                            case "F[]":
                                                fistClassName = "float[]";
                                                break;
                                            case "C[]":
                                                fistClassName = "char[]";
                                                break;
                                            case "S[]":
                                                fistClassName = "short[]";
                                                break;
                                        }
                                    }

                                    args.add(ClassName.get(packageN, fistClassName, rest));
                                }
                            });

                            var methodSpec = MethodSpec.methodBuilder("getMethod" + capitalized + count)
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .returns(Method.class)
                                    .addStatement("return $T.$N($T.class, $S, $L" + strBuilder + ")", args.toArray())
                                    .build();
                            builder.addMethod(methodSpec);
                        }, () -> {
                            throw new IllegalArgumentException("Can't find method: " + finalMethodName + "(" + String.join(", ", params) + ")");
                        });
            });
        });

        classAccessors.forEach((s, builder) -> {
            try {
                JavaFile.builder(basePackage, builder.getValue().build())
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

    private static void addMissingClasses(String basePackage, BasicConfigurationNode joinedMappings, BasicConfigurationNode mojangClassNameTranslate, BasicConfigurationNode spigotClassNameTranslate, ClassName accessorUtils, HashMap<String, Map.Entry<String, TypeSpec.Builder>> classAccessors, String str) {
        if (str.startsWith("&")) {
            String translated2;
            str = removeArrayDimensionsFromArgument(str);

            if (str.startsWith("&spigot:")) {
                translated2 = spigotClassNameTranslate.node(str.substring(8)).getString();
            } else if (str.startsWith("&hash:")) {
                translated2 = str.substring(6);
            } else {
                translated2 = mojangClassNameTranslate.node(str.substring(1)).getString();
            }

            if (translated2 == null) {
                throw new IllegalArgumentException("Can't find class: " + str);
            }

            if (!classAccessors.containsKey(translated2)) {
                System.out.println("Generating accessor for " + translated2);
                var li = str.lastIndexOf(".");
                var name = (str.substring(li < 0 ? (str.startsWith("&spigot:") ? 8 : (str.startsWith("&hash:") ? 6 : 1)) : (li + 1)) + "Accessor").replace("$", "_i_");

                var builder = TypeSpec.classBuilder(name)
                        .addModifiers(Modifier.PUBLIC);

                var typeMapping = joinedMappings.node(translated2);

                var typeMethod = MethodSpec.methodBuilder("getType")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get("", "?")))
                        .addStatement("return $T.$N($T.class, $L)", accessorUtils, "getType", ClassName.get(basePackage, name), generateMappings(typeMapping))
                        .build();
                builder.addMethod(typeMethod);

                classAccessors.put(translated2, Map.entry(name, builder));
            }
        }
    }

    public static CodeBlock generateMappings(ConfigurationNode node) {
        var codeBlock = CodeBlock.builder()
                .add("mapper -> {\n")
                .indent();

        var obfuscatedFallback = node.node("OBFUSCATED")
                .childrenMap()
                .entrySet()
                .stream()
                .flatMap(entry -> Arrays.stream(entry.getKey().toString().split(",")).map(s1 -> Map.entry(s1, entry.getValue().getString(""))))
                .sorted(Comparator.comparing(o -> new ComparableVersion(o.getKey())))
                .collect(Collectors.toList());

        // currently support spigot and searge. vanilla servers are not supported
        var spigotLatest = new AtomicReference<String>();

        var allSpigotMappings = node.node("SPIGOT").childrenMap().entrySet()
                .stream()
                .flatMap(entry -> Arrays.stream(entry.getKey().toString().split(",")).map(s1 -> Map.entry(s1, entry.getValue().getString(""))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        obfuscatedFallback.forEach(entry -> {
            var value = entry.getValue();

            if (allSpigotMappings.containsKey(entry.getKey())) {
                value = allSpigotMappings.get(entry.getKey());
            }

            if (spigotLatest.get() == null || !spigotLatest.get().equals(value)) {
                codeBlock.add("$N.$N($S, $S, $S);\n", "mapper", "map", "spigot", entry.getKey(), value);
                spigotLatest.set(value);
            }
        });


        var seargeLatest = new AtomicReference<String>();

        var allSeargeMappings = node.node("SEARGE").childrenMap().entrySet()
                .stream()
                .flatMap(entry -> Arrays.stream(entry.getKey().toString().split(",")).map(s1 -> Map.entry(s1, entry.getValue().getString(""))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        obfuscatedFallback.forEach(entry -> {
            var value = entry.getValue();

            if (allSeargeMappings.containsKey(entry.getKey())) {
                value = allSeargeMappings.get(entry.getKey());
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

    public static String removeArrayDimensionsFromArgument(String string) {
        while (string.endsWith("[]")) {
            string = string.substring(0, string.length() - 2);
        }
        return string;
    }

    public static int getArrayDimensionsCount(String string) {
        int dimensions = 0;
        while (string.endsWith("[]")) {
            string = string.substring(0, string.length() - 2);
            dimensions++;
        }
        return dimensions;
    }


    public static String convertInternal(String type) {
        // here just arrays
        if (type.startsWith("[")) {
            return convertInternal(type.substring(1)) + "[]";
        } else {
            return type;
        }
    }
}
