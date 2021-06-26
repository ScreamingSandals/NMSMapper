package org.screamingsandals.nms.mapper.parser;

import lombok.SneakyThrows;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.Caching;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpigotMappingParser {
    @SneakyThrows
    public static void mapTo(String version, Map<String, ClassDefinition> map, Caching caching) {
        var loader = GsonConfigurationLoader
                .builder()
                .url(new URI("https://hub.spigotmc.org/versions/" + version + ".json").toURL())
                .build();

        var node = loader.load();
        var buildDataRevision = node.node("refs", "BuildData").getString();

        var classFileUrl = new URI("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/bukkit-" + version + "-cl.csrg?at=" + buildDataRevision);
        var membersFileUrl = new URI("https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/bukkit-" + version + "-members.csrg?at=" + buildDataRevision);

        var cl = caching.loadData(classFileUrl, "bukkit-" + version + "-cl.csrg");
        var mem = caching.loadData(membersFileUrl, "bukkit-" + version + "-members.csrg");

        var spigotToValue = new HashMap<String, ClassDefinition>();

        var old = version.matches("1\\.(1[0-6]|[0-9])(\\..*)?$");
        var weird1165version = version.equals("1.16.5");

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

            var tempMapping = map.get(split[0]).getMapping().get(MappingType.SPIGOT);

            if (tempMapping != null) {
                // Nested class automatically got mapping but it's not valid
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
            if (!spigotToValue.containsKey("net.minecraft.server.Main") && !version.matches("1\\.(1[0-5]|[0-9])(\\..*)?$")) {
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
                System.out.println("can't get spigot class " + split[0]);
                return;
            }

            if (split.length == 3) {
                // field
                var fields = spigotToValue.get(split[0]).getFields();
                fields.values()
                        .stream()
                        .filter(f -> f.getMapping().get(MappingType.OBFUSCATED).equals(split[1]))
                        .findFirst()
                        .ifPresent(fieldDefinition -> {
                            fieldDefinition.getMapping().put(MappingType.SPIGOT, split[2]);
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
                        } else if (matched.replace("[", "").startsWith("L") && !matched.contains("/")) {
                            var sp = matched.split("L");

                            if (weird1165version) {
                                var matcher2 = pattern.matcher(split[1]);
                                if (matcher2.matches()) {
                                    sp[1] = matcher2.group("clazz");
                                }
                            }
                            matched = sp[0] + "Lnet.minecraft.server.${V}." + sp[1] + ";";
                        }
                        matched = matched.replace("/", ".");
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
                        .ifPresent(methodDefinition -> {
                            methodDefinition.getMapping().put(MappingType.SPIGOT, split[3]);
                        });
            }
        });
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
