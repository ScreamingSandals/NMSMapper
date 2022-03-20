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

package org.screamingsandals.nms.mapper.web.pages;

import org.screamingsandals.nms.mapper.joined.JoinedClassDefinition;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.Mapping;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.JavadocIndexer;
import org.screamingsandals.nms.mapper.web.WebGenerator;
import org.screamingsandals.nms.mapper.web.components.*;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ComparePage extends AbstractPage {
    private final List<String> versions;
    private final Map<String, String> spigotReplacements;
    private final MappingType baseMapping;
    private final MappingType secondMapping;
    private final Map<String, JoinedClassDefinition> joinedMappings;
    private final Predicate<JoinedClassDefinition> additionalFilter;
    private final String extraText;
    private final boolean showObfuscated;
    private final List<MappingType> mappingTypes;

    public ComparePage(Map<String, JoinedClassDefinition> joinedMappings, List<Mapping> mappings, String name, String title, MappingType baseMapping, MappingType secondMapping, Predicate<JoinedClassDefinition> additionalFilter, String extraText, boolean showObfuscated) {
        super(
                "compare",
                "comparison/" + name + ".html",
                title + " comparison",
                List.of(
                        new NavbarLink("Version overview", null, false),
                        new NavbarLink("Documentation", WebGenerator.DOC_LINK, false)
                ),
                false,
                true,
                false,
                true
        );
        this.versions = mappings.stream().map(Mapping::getVersion).collect(Collectors.toList());
        this.spigotReplacements = mappings.stream().map(mapping -> new AbstractMap.SimpleEntry<>(mapping.getVersion(), mapping.getSpigotNms())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.baseMapping = baseMapping;
        this.secondMapping = secondMapping;
        this.joinedMappings = joinedMappings;
        this.additionalFilter = additionalFilter != null ? additionalFilter : joinedClassDefinition -> true;
        this.extraText = extraText;
        this.showObfuscated = showObfuscated;
        this.mappingTypes = showObfuscated ? List.of(baseMapping, secondMapping, MappingType.OBFUSCATED) : List.of(baseMapping, secondMapping);
    }

    @Override
    public void fillContext(Context context) {
        context.setVariable("baseMappingName", baseMapping.getWebName());
        context.setVariable("secondMappingName", secondMapping.getWebName());
        context.setVariable("baseMappingColor", baseMapping.getBootstrapColor());
        context.setVariable("secondMappingColor", secondMapping.getBootstrapColor());
        context.setVariable("versions", versions);
        context.setVariable("extraText", extraText != null ? extraText : "");
        context.setVariable("showObfuscated", showObfuscated);

        context.setVariable("comparisons", joinedMappings.entrySet()
                .stream()
                .filter(entry ->
                        entry.getValue().getMapping().keySet().stream().anyMatch(e ->
                                versions.stream().anyMatch(v -> Arrays.asList(e.getKey().split(",")).contains(v))
                                        && e.getValue() == baseMapping
                        )
                                &&
                                entry.getValue().getMapping().keySet().stream().anyMatch(e ->
                                        versions.stream().anyMatch(v -> Arrays.asList(e.getKey().split(",")).contains(v))
                                                && e.getValue() == secondMapping
                                )
                                && additionalFilter.test(entry.getValue())
                )
                .sorted(Comparator.comparing(entry -> entry.getValue().getMapping().entrySet().stream()
                        .filter(e -> Arrays.asList(e.getKey().getKey().split(",")).contains(versions.get(0)) && e.getKey().getValue() == baseMapping)
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElseGet(() -> entry.getValue().getMapping().values().stream().findFirst().orElseThrow())
                ))
                .map(entry -> new CompareClassMultiVersion(entry.getKey(), versions.stream().map(v -> {
                            var neededMappings = entry.getValue().getMapping()
                                    .entrySet()
                                    .stream()
                                    .filter(
                                            e -> Arrays.asList(e.getKey().getKey().split(",")).contains(v)
                                                    && this.mappingTypes.contains(e.getKey().getValue())
                                    )
                                    .map(e -> Map.entry(e.getKey().getValue(), e.getKey().getValue() == MappingType.SPIGOT
                                            && spigotReplacements.get(v) != null ? e.getValue().replace("${V}", spigotReplacements.get(v)) : e.getValue()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                            if (neededMappings.isEmpty()) {
                                return null;
                            }
                            return Map.entry(v, new CompareClass(
                                    "../" + entry.getValue().getPathKeys().get(v),
                                    neededMappings.get(baseMapping),
                                    neededMappings.get(MappingType.OBFUSCATED),
                                    neededMappings.get(secondMapping)
                            ));
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                        entry.getValue().getFields()
                                .stream()
                                .filter(joinedField ->
                                        joinedField.getMapping().keySet().stream().anyMatch(e ->
                                                versions.stream().anyMatch(v -> Arrays.asList(e.getKey().split(",")).contains(v))
                                                        && e.getValue() == baseMapping
                                        )
                                                &&
                                                joinedField.getMapping().keySet().stream().anyMatch(e ->
                                                        versions.stream().anyMatch(v -> Arrays.asList(e.getKey().split(",")).contains(v))
                                                                && e.getValue() == secondMapping
                                                ))
                                .map(joinedField -> new CompareFieldMultiVersion(versions.stream().map(v -> {
                                            var neededMappings = joinedField.getMapping()
                                                    .entrySet()
                                                    .stream()
                                                    .filter(
                                                            e -> Arrays.asList(e.getKey().getKey().split(",")).contains(v)
                                                                    && this.mappingTypes.contains(e.getKey().getValue())
                                                    )
                                                    .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                            if (neededMappings.isEmpty()) {
                                                return null;
                                            }
                                            return Map.entry(v, new CompareField(
                                                    neededMappings.get(baseMapping),
                                                    neededMappings.get(MappingType.OBFUSCATED),
                                                    neededMappings.get(secondMapping)
                                            ));
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))))
                                .collect(Collectors.toList()),
                        entry.getValue().getMethods()
                                .stream()
                                .filter(joinedMethod -> joinedMethod.getMapping().keySet().stream().anyMatch(e ->
                                        versions.stream().anyMatch(v -> Arrays.asList(e.getKey().split(",")).contains(v))
                                                && e.getValue() == baseMapping
                                )
                                        &&
                                        joinedMethod.getMapping().keySet().stream().anyMatch(e ->
                                                versions.stream().anyMatch(v -> Arrays.asList(e.getKey().split(",")).contains(v))
                                                        && e.getValue() == secondMapping
                                        ))
                                .map(joinedMethod -> new CompareMethodMultiVersion(versions.stream().map(v -> {
                                                    var neededMappings = joinedMethod.getMapping()
                                                            .entrySet()
                                                            .stream()
                                                            .filter(
                                                                    e -> Arrays.asList(e.getKey().getKey().split(",")).contains(v)
                                                                            && this.mappingTypes.contains(e.getKey().getValue())
                                                            )
                                                            .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                                    if (neededMappings.isEmpty()) {
                                                        return null;
                                                    }
                                                    return Map.entry(v, new CompareMethod(
                                                            neededMappings.get(baseMapping),
                                                            neededMappings.get(MappingType.OBFUSCATED),
                                                            neededMappings.get(secondMapping),
                                                            joinedMethod.getParameters()
                                                                    .stream()
                                                                    .map(link -> convertToMapping(v, link, baseMapping))
                                                                    .collect(Collectors.toList())
                                                    ));
                                                })
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                                )
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList())
        );
    }

    public ClassNameLink convertToMapping(String version, ClassDefinition.Link link, MappingType mappingType) {
        var type = link.getType();
        var suffix = new StringBuilder();
        while (type.endsWith("[]")) {
            suffix.append("[]");
            type = type.substring(0, type.length() - 2);
        }
        if (link.isNms()) {
            if (type.matches(".*\\$\\d+")) { // WTF? How
                suffix.insert(0, type.substring(type.lastIndexOf("$")));
                type = type.substring(0, type.lastIndexOf("$"));
            }
            var joinedMap = joinedMappings.get(type);
            var mappingName = joinedMap.getMapping().entrySet().stream()
                    .filter(entry -> Arrays.asList(entry.getKey().getKey().split(",")).contains(version) && entry.getKey().getValue() == mappingType)
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .or(() ->
                            joinedMap.getMapping().entrySet().stream()
                                    .filter(entry -> Arrays.asList(entry.getKey().getKey().split(",")).contains(version) && entry.getKey().getValue() == MappingType.OBFUSCATED)
                                    .map(Map.Entry::getValue)
                                    .findFirst()
                    )
                    .orElse(type);
            if (mappingType == MappingType.SPIGOT && spigotReplacements.get(version) != null) {
                mappingName = mappingName.replace("${V}", spigotReplacements.get(version));
            }
            return new ClassNameLink(mappingName.substring(mappingName.lastIndexOf(".") + 1), "../" + joinedMap.getPathKeys().get(version), mappingName, suffix.toString());
        } else {
            // not a primitive type
            if (type.contains(".")) {
                var result = JavadocIndexer.INSTANCE.linkFor(type);
                if (result != null) {
                    return new ClassNameLink(type.substring(type.lastIndexOf(".") + 1), result, type, suffix.toString());
                }
            }
            return new ClassNameLink(link.getType(), null, null, null);
        }
    }
}
