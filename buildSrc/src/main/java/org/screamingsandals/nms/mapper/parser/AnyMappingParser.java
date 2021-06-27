package org.screamingsandals.nms.mapper.parser;

import net.minecraftforge.srgutils.IMappingFile;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class AnyMappingParser {
    public static int map(Map<String, ClassDefinition> map, InputStream inputStream, List<String> excluded, MappingType mappingType, boolean inverted) throws IOException {
        var errors = new AtomicInteger();

        var mappingFile = IMappingFile.load(inputStream);

        var invertedBuffer = new HashMap<String, String>();
        if (inverted) {
            mappingFile.getClasses().forEach(iClass ->
                    invertedBuffer.put(iClass.getOriginal().replace("/", "."), iClass.getMapped().replace("/", "."))
            );
        }

        mappingFile.getClasses()
                .forEach(iClass -> {
                    var original = (inverted ? iClass.getMapped() : iClass.getOriginal()).replace("/", ".");
                    var mapped = (inverted ? iClass.getOriginal() : iClass.getMapped()).replace("/", ".");

                    var definition = map.get(original);
                    if (definition != null) { // we have only server classes
                        definition.getMapping().put(mappingType, mapped);

                        iClass.getFields().forEach(iField -> {
                            var iFieldOriginal = inverted ? iField.getMapped() : iField.getOriginal();
                            var iFieldMapped = inverted ? iField.getOriginal() : iField.getMapped();

                            if (definition.getFields().containsKey(iFieldOriginal)) {
                                definition.getFields().get(iFieldOriginal).getMapping().put(mappingType, iFieldMapped);
                            } else if (!excluded.contains(definition.getMapping().get(MappingType.OBFUSCATED) + " field " + iFieldOriginal)) {
                                System.out.println(definition.getMapping().get(MappingType.OBFUSCATED) + ": Missing " + iFieldOriginal + " -> " + iFieldMapped);
                                errors.incrementAndGet();
                            }
                        });

                        iClass.getMethods().forEach(iMethod -> {
                            if (iMethod.getOriginal().equals("<init>") || iMethod.getOriginal().equals("<clinit>")) {
                                return; // Ignoring constructors
                            }

                            // for some reason srgutils can't parse arguments and I don't know why
                            var iMethodOriginal = inverted ? iMethod.getMapped() : iMethod.getOriginal();
                            var iMethodMapped = inverted ? iMethod.getOriginal() : iMethod.getMapped();

                            var descriptor = iMethod.getDescriptor();

                            var pattern = Pattern.compile("\\[*(L[^;]+;|[A-Z])");
                            var allMatches = new ArrayList<String>();
                            var matcher = pattern.matcher(descriptor.substring(descriptor.indexOf("(") + 1, descriptor.lastIndexOf(")")));
                            while (matcher.find()) {
                                var matched = matcher.group();
                                matched = matched.replace("/", ".");
                                matched = SpigotMappingParser.convertInternal(matched);

                                if (inverted) {
                                    var type = matched;
                                    var suffix = new StringBuilder();
                                    while (type.endsWith("[]")) {
                                        suffix.append("[]");
                                        type = type.substring(0, type.length() - 2);
                                    }
                                    if (type.matches(".*\\$\\d+")) { // WTF? How
                                        suffix.insert(0, type.substring(type.lastIndexOf("$")));
                                        type = type.substring(0, type.lastIndexOf("$"));
                                    }

                                    matched = invertedBuffer.getOrDefault(type, type) + suffix;
                                }

                                allMatches.add(matched);
                            }

                            definition.getMethods().stream()
                                    .filter(m -> {
                                        if (!m.getMapping().get(MappingType.OBFUSCATED).equals(iMethodOriginal)) {
                                            return false;
                                        }
                                        if (m.getParameters().size() != allMatches.size()) {
                                            return false;
                                        }

                                        for (var i = 0; i < m.getParameters().size(); i++) {
                                            var par = m.getParameters().get(i);
                                            var spar = allMatches.get(i);

                                            if (!par.getType().equals(spar)) {
                                                return false;
                                            }
                                        }

                                        return true;
                                    })
                                    .findFirst()
                                    .ifPresentOrElse(methodDefinition -> {
                                        methodDefinition.getMapping().put(mappingType, iMethodMapped);
                                    }, () -> {
                                        var s = String.join(",", allMatches);
                                        if (!excluded.contains(definition.getMapping().get(MappingType.OBFUSCATED) + " method " + iMethodOriginal + "(" + s + ")")) {
                                            System.out.println(definition.getMapping().get(MappingType.OBFUSCATED) + ": missing " + iMethodOriginal + "(" + s + ") -> " + iMethodMapped);
                                            errors.incrementAndGet();
                                        }
                                    });
                        });
                    }
                });

        return errors.get();
    }
}
