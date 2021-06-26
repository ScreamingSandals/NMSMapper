package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MojangMappingParser {
    public static int map(Map<String, ClassDefinition> map, String text, List<String> excluded) {
        var currentClass = new AtomicReference<ClassDefinition>();
        var mojangToObfuscated = new HashMap<String, String>();

        var secondIteration = new ArrayList<Map.Entry<ClassDefinition, String>>();

        var errors = new AtomicInteger();

        text.lines().forEach(line -> {
            if (line.startsWith("#") || line.contains("package-info")) {
                return;
            }

            if (!line.startsWith(" ")) {
                // Class
                var matcher = Pattern.compile("(?<unobfuscated>[^\\s]+) -> (?<obfuscated>[^\\s]+):").matcher(line);

                if (!matcher.find()) {
                    return;
                }
                var obfuscated = matcher.group("obfuscated");
                var mojang = matcher.group("unobfuscated");

                var definition = map.get(obfuscated);
                currentClass.set(definition);
                if (definition != null) {
                    definition.getMapping().put(MappingType.MOJANG,mojang);
                    mojangToObfuscated.put(mojang, obfuscated);
                }
            } else {
                if (currentClass.get() != null) {
                    secondIteration.add(Map.entry(currentClass.get(), line));
                }
            }
        });


        secondIteration.forEach(entry -> {
            var definition = entry.getKey();
            var line = entry.getValue();
            if (line.matches(".*\\([^)]*\\)\\s+->.*")) {
                // Method
                if (line.contains("<clinit>")) { // static initializers are ignored for now
                    return;
                }

                var matcher = Pattern.compile("\\s+(\\d+:\\d+:)?(?<type>[^\\s]+)\\s(?<unobfuscated>[^\\s(]+)\\((?<parameters>[^)]*)\\)\\s->\\s(?<obfuscated>[^\\s]+)").matcher(line);

                if (!matcher.find()) {
                    return;
                }

                if (!line.contains("<init>")) {
                    var obfuscated = matcher.group("obfuscated");
                    var mojang = matcher.group("unobfuscated");

                    var matcherI = Pattern.compile("[^\\s,]+").matcher(matcher.group("parameters"));

                    var parameters = new ArrayList<ClassDefinition.Link>();

                    while (matcherI.find()) {
                        var clazz = matcherI.group();
                        var type = clazz;
                        var suffix = new StringBuilder();

                        while (type.endsWith("[]")) {
                            suffix.append("[]");
                            type = type.substring(0, type.length() - 2);
                        }
                        if (type.matches(".*\\$\\d+")) { // WTF? How
                            suffix.insert(0, type.substring(type.lastIndexOf("$")));
                            type = type.substring(0, type.lastIndexOf("$"));
                        }

                        if (mojangToObfuscated.containsKey(type)) {
                            parameters.add(ClassDefinition.Link.nmsLink(mojangToObfuscated.get(type) + suffix));
                        } else {
                            parameters.add(ClassDefinition.Link.casualLink(type + suffix));
                        }
                    }

                    definition.getMethods()
                            .stream()
                            .filter(methodDefinition ->
                                    methodDefinition.getMapping().get(MappingType.OBFUSCATED).equals(obfuscated)
                                    && parameters.equals(methodDefinition.getParameters())
                            )
                            .findFirst()
                            .ifPresentOrElse(methodDefinition -> {
                                methodDefinition.getMapping().put(MappingType.MOJANG, mojang);
                            }, () -> {
                                var s = parameters.stream().map(ClassDefinition.Link::getType).collect(Collectors.joining(","));
                                if (!excluded.contains(definition.getMapping().get(MappingType.OBFUSCATED) + " method " + obfuscated + "(" + s + ")")) {
                                    System.out.println(definition.getMapping().get(MappingType.OBFUSCATED) + ": missing " + obfuscated + "(" + s + ") -> " + mojang);
                                    errors.incrementAndGet();
                                }
                            });
                } // else constructors can't have custom name mappings cuz they don't have name
            } else {
                // Field
                var matcher = Pattern.compile("\\s+(?<type>[^\\s]+)\\s(?<unobfuscated>[^\\s]+)\\s->\\s(?<obfuscated>[^\\s]+)").matcher(line);

                if (!matcher.find()) {
                    return;
                }

                var obfuscated = matcher.group("obfuscated");
                var mojang = matcher.group("unobfuscated");

                if (definition.getFields().containsKey(obfuscated)) {
                    definition.getFields().get(obfuscated).getMapping().put(MappingType.MOJANG, mojang);
                } else if (!excluded.contains(definition.getMapping().get(MappingType.OBFUSCATED) + " field " + obfuscated)) {
                    System.out.println(definition.getMapping().get(MappingType.OBFUSCATED) + ": Missing " + obfuscated + " -> " + mojang);
                    errors.incrementAndGet();
                }
            }
        });

        return errors.get();
    }
}
