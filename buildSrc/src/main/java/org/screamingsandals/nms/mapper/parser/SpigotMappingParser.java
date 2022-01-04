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

package org.screamingsandals.nms.mapper.parser;

import lombok.SneakyThrows;
import org.screamingsandals.nms.mapper.extension.Version;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.ErrorsLogger;

import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: convert this mess to AnyMappingParser (maybe it won't be needed because spigot is dropping its own mapping slowly)
public class SpigotMappingParser {
    @SneakyThrows
    public static String mapTo(Version version, Map<String, ClassDefinition> map, List<String> excluded, ErrorsLogger errorsLogger) {
        var workspace = version.getWorkspace();

        var cl = Files.readString(workspace.getFile(Objects.requireNonNull(version.getSpigotClassMappings()), "bukkit-cl.csrg").toPath());
        var mem = "";
        if (version.getSpigotMemberMappings() != null && version.getSpigotMemberMappings().isPresent()) {
            mem = Files.readString(workspace.getFile(Objects.requireNonNull(version.getSpigotMemberMappings()), "bukkit-members.csrg").toPath());
        }

        var spigotToValue = new HashMap<String, ClassDefinition>();

        var old = version.getVersion().matches("1\\.(1[0-6]|[0-9])(\\..*)?$");
        var weird1165version = version.getVersion().equals("1.16.5");

        cl.lines().filter(l -> !l.startsWith("#") && !l.isBlank()).forEach(s -> {
            var split = s.split(" ");
            if (old) {
                if (split[1].equals("net/minecraft/server/MinecraftServer")) {
                    split[1] = "net.minecraft.server.${V}.MinecraftServer"; // ${V} is placeholder
                } else if (split[1].equals("net/minecraft/server/Main")) {
                    split[1] = "net.minecraft.server.${V}.Main"; // ${V} is placeholder
                } else {
                    if (weird1165version) {
                        var pattern = Pattern.compile("(net/minecraft/|com/mojang/math/)(.+/)*(?<clazz>.*)");
                        var matcher = pattern.matcher(split[1]);
                        if (matcher.matches()) {
                            split[1] = matcher.group("clazz");
                        }
                    }
                    split[1] = "net.minecraft.server.${V}." + split[1]; // ${V} is placeholder
                }
            }
            split[1] = split[1].replace("/", ".");
            split[0] = split[0].replace("/", ".");

            // let's skip anonymous classes in very old spigot mappings
            if (version.getVersion().equals("1.8.8") && split[0].matches(".*\\$\\d+|.*\\$a") && map.get(split[0]) == null) {
                return;
            }

            var tempMapping = map.get(split[0]).getMapping().get(MappingType.SPIGOT);

            if (tempMapping != null) {
                // Nested class automatically got mapping, but it's not valid
                spigotToValue.remove(tempMapping);
            }

            map.get(split[0]).getMapping().put(MappingType.SPIGOT, split[1]);
            spigotToValue.put(split[1], map.get(split[0]));

            // Filtering nested classes
            map.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().startsWith(split[0] + "$") && !entry.getValue().getMapping().containsKey(MappingType.SPIGOT))
                    .forEach(entry -> {
                        var spigotName = split[1] + entry.getKey().substring(entry.getKey().indexOf("$"));
                        entry.getValue().getMapping().put(MappingType.SPIGOT, spigotName);
                        spigotToValue.put(spigotName, entry.getValue());
                    });
        });

        if (old) {
            if (!spigotToValue.containsKey("net.minecraft.server.Main") && !version.getVersion().matches("1\\.(1[0-5]|[0-9])(\\..*)?$")) {
                map.get("net.minecraft.server.Main").getMapping().put(MappingType.SPIGOT, "net.minecraft.server.${V}.Main");
                spigotToValue.put("net.minecraft.server.${V}.Main", map.get("net.minecraft.server.Main"));
            }
            if (!spigotToValue.containsKey("net.minecraft.server.MinecraftServer")) {
                map.get("net.minecraft.server.MinecraftServer").getMapping().put(MappingType.SPIGOT, "net.minecraft.server.${V}.MinecraftServer");
                spigotToValue.put("net.minecraft.server.${V}.MinecraftServer", map.get("net.minecraft.server.MinecraftServer"));
            }
        } else {
            if (!spigotToValue.containsKey("net.minecraft.server.Main")) {
                map.get("net.minecraft.server.Main").getMapping().put(MappingType.SPIGOT, "net.minecraft.server.Main");
                spigotToValue.put("net.minecraft.server.Main", map.get("net.minecraft.server.Main"));
            }
            if (!spigotToValue.containsKey("net.minecraft.server.MinecraftServer")) {
                map.get("net.minecraft.server.MinecraftServer").getMapping().put(MappingType.SPIGOT, "net.minecraft.server.MinecraftServer");
                spigotToValue.put("net.minecraft.server.MinecraftServer", map.get("net.minecraft.server.MinecraftServer"));
            }
        }

        if (!mem.isEmpty()) {
            mem.lines().filter(l -> !l.startsWith("#") && !l.isBlank()).forEach(s -> {
                var split = s.split(" ");
                if (old) {
                    if (split[0].equals("net/minecraft/server/MinecraftServer")) {
                        split[0] = "net.minecraft.server.${V}.MinecraftServer"; // ${V} is placeholder
                    } else if (split[0].equals("net/minecraft/server/Main")) {
                        split[0] = "net.minecraft.server.${V}.Main"; // ${V} is placeholder
                    } else {
                        if (weird1165version) {
                            var pattern = Pattern.compile("(net/minecraft/|com/mojang/math/)(.+/)*(?<clazz>.*)");
                            var matcher = pattern.matcher(split[0]);
                            if (matcher.matches()) {
                                split[0] = matcher.group("clazz");
                            }
                        }
                        split[0] = "net.minecraft.server.${V}." + split[0]; // ${V} is placeholder
                    }
                }
                split[0] = split[0].replace("/", ".");

                if (spigotToValue.get(split[0]) == null) {
                    if (version.getVersion().equals("1.8.8") && split[0].matches(".*\\$\\d+|.*\\$a")) {
                        return; // Silent
                    }
                    System.out.println("Can't get Spigot class " + split[0] + "!");
                    return;
                }

                var selfLink = ClassDefinition.Link.nmsLink(spigotToValue.get(split[0]).getMapping().get(MappingType.OBFUSCATED));

                if (split.length == 3) {
                    // field
                    var fields = spigotToValue.get(split[0]).getFields();
                    fields.values()
                            .stream()
                            .filter(f -> f.getMapping().get(MappingType.OBFUSCATED).equals(split[1]))
                            .findFirst()
                            .ifPresentOrElse(fieldDefinition -> {
                                fieldDefinition.getMapping().put(MappingType.SPIGOT, split[2]);
                            }, () -> {
                                if (!excluded.contains(spigotToValue.get(split[0]).getMapping().get(MappingType.OBFUSCATED) + " field " + split[1])) {
                                    errorsLogger.log(spigotToValue.get(split[0]).getMapping().get(MappingType.OBFUSCATED) + ": Missing " + split[1] + " -> " + split[2]);
                                }
                            });
                } else if (split.length == 4) {
                    // method
                    var methods = spigotToValue.get(split[0]).getMethods();
                    var pattern = Pattern.compile("\\[*(L[^;]+;|[A-Z])");
                    var allMatches = new ArrayList<String>();
                    var matcher = pattern.matcher(split[2].substring(1, split[2].lastIndexOf(")")));
                    while (matcher.find()) {
                        var matched = matcher.group();
                        if (old) {
                            if (matched.contains("net/minecraft/server/MinecraftServer")) {
                                matched = matched.replace("net/minecraft/server/MinecraftServer", "net.minecraft.server.${V}.MinecraftServer"); // ${V} is placeholder
                            } else if (matched.contains("net/minecraft/server/Main")) {
                                matched = matched.replace("net/minecraft/server/Main", "net.minecraft.server.${V}.Main"); // ${V} is placeholder
                            } else if (matched.replace("[", "").startsWith("L") && (!matched.contains("/") || matched.matches(".*(net/minecraft/|com/mojang/math/).*"))) {
                                var sp = matched.split("L", 2);

                                if (weird1165version) {
                                    var pattern2 = Pattern.compile("(net/minecraft/|com/mojang/math/)(.+/)*(?<clazz>.*)");
                                    var matcher2 = pattern2.matcher(sp[1]);
                                    if (matcher2.matches()) {
                                        sp[1] = matcher2.group("clazz");
                                    }
                                }
                                matched = sp[0] + "Lnet.minecraft.server.${V}." + sp[1];
                            }
                        }
                        matched = matched.replace("/", ".");
                        matched = SpigotMappingParser.convertInternal(matched);

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

                        if (spigotToValue.containsKey(type)) {
                            matched = spigotToValue.get(type).getMapping().get(MappingType.OBFUSCATED) + suffix;
                        }

                        allMatches.add(matched);
                    }

                    methods.stream()
                            .filter(m -> {
                                if (!m.getMapping().get(MappingType.OBFUSCATED).equals(split[1])) {
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
                                methodDefinition.getMapping().put(MappingType.SPIGOT, split[3]);

                                // Try to find overridden methods
                                map.entrySet()
                                        .stream()
                                        .filter(entry -> isImplementing(map, entry.getValue(), selfLink))
                                        .forEach(entry -> {
                                            entry.getValue()
                                                    .getMethods()
                                                    .stream()
                                                    .filter(m -> {
                                                        if (!m.getMapping().get(MappingType.OBFUSCATED).equals(split[1])) {
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
                                                    .ifPresent(md -> {
                                                        md.getMapping().put(MappingType.SPIGOT, split[3]);
                                                    });

                                        });

                                // Try to find strangely overridden methods
                                if (methodDefinition.getMapping().containsKey(MappingType.SEARGE)) {
                                    map.entrySet()
                                            .stream()
                                            .filter(entry -> isImplementing(map, entry.getValue(), selfLink))
                                            .forEach(entry -> {
                                                entry.getValue()
                                                        .getMethods()
                                                        .stream()
                                                        .filter(m -> {
                                                            if (!m.getMapping().containsKey(MappingType.SEARGE) || m.getMapping().containsKey(MappingType.SPIGOT)) {
                                                                return false;
                                                            }
                                                            if (!m.getMapping().get(MappingType.SEARGE).equals(methodDefinition.getMapping().get(MappingType.SEARGE))) {
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
                                                        .ifPresent(md -> {
                                                            System.out.println("Strange Spigot mapping fixed: "
                                                                    + entry.getValue().getMapping().get(MappingType.OBFUSCATED) + "#" + md.getMapping().get(MappingType.OBFUSCATED) + "(" + md.getParameters().stream().map(ClassDefinition.Link::getType).collect(Collectors.joining(", ")) + ")"
                                                                    + " overrides " + spigotToValue.get(split[0]).getMapping().get(MappingType.OBFUSCATED) + "#" + methodDefinition.getMapping().get(MappingType.OBFUSCATED) + "(" + methodDefinition.getParameters().stream().map(ClassDefinition.Link::getType).collect(Collectors.joining(", ")) + ")"
                                                                    + "\n\tSpigot name: " +  entry.getValue().getMapping().getOrDefault(MappingType.SPIGOT, entry.getValue().getMapping().get(MappingType.OBFUSCATED)) + "#" + split[3] + "\n\tSearge name: " + entry.getValue().getMapping().getOrDefault(MappingType.SEARGE, entry.getValue().getMapping().get(MappingType.OBFUSCATED)) + "#" + methodDefinition.getMapping().get(MappingType.SEARGE)
                                                            );
                                                            md.getMapping().put(MappingType.SPIGOT, split[3]);
                                                        });

                                            });
                                }
                            }, () -> {
                                var s2 = String.join(",", allMatches);
                                if (!excluded.contains(spigotToValue.get(split[0]).getMapping().get(MappingType.OBFUSCATED) + " method " + split[1] + "(" + s2 + ")")) {
                                    errorsLogger.log(spigotToValue.get(split[0]).getMapping().get(MappingType.OBFUSCATED) + ": missing " + split[1] + "(" + s2 + ") -> " + split[3]);
                                }
                            });
                }
            });
        }

        return cl.lines().findFirst().map(e -> e.substring(1).trim()).orElse(null);
    }

    public static boolean isImplementing(Map<String,ClassDefinition> map, ClassDefinition definition, ClassDefinition.Link self) {
        if (definition.getSuperclass().equals(self) || definition.getInterfaces().contains(self)) {
            return true;
        }

        for (var theInterface : definition.getInterfaces()) {
            if (theInterface.isNms() && isImplementing(map, map.get(theInterface.getType()), self)) {
                return true;
            }
        }

        if (definition.getSuperclass() != null && definition.getSuperclass().isNms()) {
            return isImplementing(map, map.get(definition.getSuperclass().getType()), self);
        }

        return false;
    }

    public static String convertInternal(String type) {
        switch (type) {
            case "B":
                return "byte";
            case "C":
                return "char";
            case "D":
                return "double";
            case "F":
                return "float";
            case "I":
                return "int";
            case "J":
                return "long";
            case "S":
                return "short";
            case "Z":
                return "boolean";
            case "V":
                return "void";
            default:
                if (type.startsWith("[")) {
                    return convertInternal(type.substring(1)) + "[]";
                } else if (type.endsWith(";")) {
                    return type.substring(1, type.length() - 1).replace("/", ".");
                } else {
                    return type.substring(1).replace("/", ".");
                }

        }
    }
}
