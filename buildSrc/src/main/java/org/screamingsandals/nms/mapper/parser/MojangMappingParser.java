package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class MojangMappingParser {
    public static Map<String, ClassDefinition> map(String text) {
        var map = new HashMap<String, ClassDefinition>();
        var currentClass = new AtomicReference<ClassDefinition>();

        var secondIteration = new ArrayList<Map.Entry<ClassDefinition, String>>();

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

                currentClass.set(new ClassDefinition());
                map.put(matcher.group("unobfuscated"), currentClass.get());
                currentClass.get().getMapping().put(MappingType.OBFUSCATED, matcher.group("obfuscated"));
                currentClass.get().getMapping().put(MappingType.MOJANG, matcher.group("unobfuscated"));
            } else {
                secondIteration.add(Map.entry(currentClass.get(), line));
            }

        });

        secondIteration.forEach(entry -> {
            var theClass = entry.getKey();
            var line = entry.getValue();
            if (line.matches(".*\\([^)]*\\)\\s+->.*")) {
                // Method
                if (line.contains("<clinit>")) { // static initializers are ignored for now
                    return;
                }

                var matcher = Pattern.compile("\\s+(\\d+:\\d+:)?(?<type>[^\\s])+\\s(?<unobfuscated>[^\\s(]+)\\((?<parameters>[^)]*)\\)\\s->\\s(?<obfuscated>[^\\s]+)").matcher(line);

                if (!matcher.find()) {
                    return;
                }

                var matcherI = Pattern.compile("[^\\s,]+").matcher(matcher.group("parameters"));

                var parameters = new ArrayList<ClassDefinition.Link>();

                while (matcherI.find()) {
                    var clazz = matcherI.group();

                    if (map.containsKey(clazz)) {
                        parameters.add(ClassDefinition.Link.nmsLink(clazz));
                    } else {
                        parameters.add(ClassDefinition.Link.casualLink(clazz));
                    }
                }

                if (line.contains("<init>")) {
                    // Constructor
                    var constructor = new ClassDefinition.ConstructorDefinition();
                    constructor.getParameters().addAll(parameters);
                    theClass.getConstructors().add(constructor);
                } else {
                    var type = matcher.group("type");
                    ClassDefinition.Link link;
                    if (map.containsKey(type)) {
                        link = ClassDefinition.Link.nmsLink(type);
                    } else {
                        link = ClassDefinition.Link.casualLink(type);
                    }


                    ClassDefinition.MethodDefinition method = new ClassDefinition.MethodDefinition(link);
                    method.getMapping().put(MappingType.OBFUSCATED, matcher.group("obfuscated"));
                    method.getMapping().put(MappingType.MOJANG, matcher.group("unobfuscated"));
                    method.getParameters().addAll(parameters);
                    theClass.getMethods().add(method);
                }

            } else {
                // Field
                var matcher = Pattern.compile("\\s+(?<type>[^\\s]+)\\s(?<unobfuscated>[^\\s]+)\\s->\\s(?<obfuscated>[^\\s]+)").matcher(line);

                if (!matcher.find()) {
                    return;
                }

                var type = matcher.group("type");
                ClassDefinition.Link link;
                if (map.containsKey(type)) {
                    link = ClassDefinition.Link.nmsLink(type);
                } else {
                    link = ClassDefinition.Link.casualLink(type);
                }

                ClassDefinition.FieldDefinition field = new ClassDefinition.FieldDefinition(link);
                field.getMapping().put(MappingType.OBFUSCATED, matcher.group("obfuscated"));
                field.getMapping().put(MappingType.MOJANG, matcher.group("unobfuscated"));
                theClass.getFields().put(matcher.group("unobfuscated"), field);
            }
        });

        map.values().removeIf(c -> c.getMapping().get(MappingType.MOJANG).matches(".*\\$[0-9]+")); // we don't need anonymous classes in the mapping

        map.values().forEach(c -> {
            c.getMethods().removeIf(m -> m.getMapping().get(MappingType.MOJANG).matches("lambda\\$.*\\$[0-9]+")
                    || m.getMapping().get(MappingType.MOJANG).matches("access\\$[0-9]+")); // we don't need lambdas and black magic in the mapping

            c.getFields().values().removeIf(f -> f.getMapping().get(MappingType.MOJANG).equals("this$0")); // we don't need black magic in the mapping
        });

        return map;
    }
}
