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

package org.screamingsandals.nms.mapper.newweb.pages;

import org.gradle.util.VersionNumber;
import org.screamingsandals.nms.mapper.joined.JoinedClassDefinition;
import org.screamingsandals.nms.mapper.newweb.components.ChangedSymbol;
import org.screamingsandals.nms.mapper.newweb.components.Changelog;
import org.screamingsandals.nms.mapper.newweb.components.ClassNameLink;
import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryPage extends AbstractPage {
    private final JoinedClassDefinition definition;
    private final Map<String, String> joinedMappingsClassLinks;

    public HistoryPage(String classHashName, String className, JoinedClassDefinition definition, Map<String, String> joinedMappingsClassLinks) {
        super(
                "history",
                "history/" + classHashName + ".html",
                "History of " + className,
                List.of(
                        new NavbarLink("Main page", "../", false),
                        new NavbarLink("Overview", null, false),
                        new NavbarLink("Package", null, false),
                        new NavbarLink("Class", null, false),
                        new NavbarLink("History", null, true)
                ),
                false
        );
        this.definition = definition;
        this.joinedMappingsClassLinks = joinedMappingsClassLinks;
    }

    @Override
    public void fillContext(Context context) {
        var versions = definition.getMapping()
                .keySet()
                .stream()
                .filter(s -> s.getValue() == MappingType.OBFUSCATED)
                .map(s -> s.getKey().split(","))
                .flatMap(Stream::of)
                .sorted(Comparator.comparing(VersionNumber::parse).reversed())
                .collect(Collectors.toList());

        var finalVersions = new ArrayList<Changelog>();

        versions.forEach(s -> {
            var previousVersionIndex = versions.indexOf(s) + 1;
            var previousVersion = versions.size() > previousVersionIndex ? versions.get(previousVersionIndex) : null;

            var classMapping = definition.getMapping()
                    .entrySet()
                    .stream()
                    .filter(e -> Arrays.asList(e.getKey().getKey().split(",")).contains(s))
                    .collect(Collectors.toList());

            var constructorsMapping = definition.getConstructors()
                    .stream()
                    .filter(e -> e.getSupportedVersions().contains(s))
                    .collect(Collectors.toList());

            var fieldsMapping = definition.getFields()
                    .stream()
                    .map(e -> Map.entry(e.getMapping()
                            .entrySet()
                            .stream()
                            .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(s))
                            .collect(Collectors.toList()), e))
                    .filter(l -> !l.getKey().isEmpty())
                    .collect(Collectors.toList());

            var methodsMapping = definition.getMethods()
                    .stream()
                    .map(e -> Map.entry(e.getMapping()
                            .entrySet()
                            .stream()
                            .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(s))
                            .collect(Collectors.toList()), e))
                    .filter(l -> !l.getKey().isEmpty())
                    .collect(Collectors.toList());

            var changelog = new Changelog(s, "../" + s + "/" + definition.getPathKeys().get(s), previousVersion == null);

            if (previousVersion == null) { // Everything is changed
                classMapping.forEach(entry ->
                        changelog.getNameMappings().put(entry.getKey().getValue(), new AbstractMap.SimpleEntry<>(null, entry.getValue()))
                );

                if (!constructorsMapping.isEmpty()) {
                    constructorsMapping.forEach(entry ->
                            changelog.getConstructorChanges().add(
                                    ChangedSymbol.addition(
                                            List.of(),
                                            null,
                                            entry.getParameters()
                                                    .stream()
                                                    .map(this::nmsLink)
                                                    .collect(Collectors.toList())
                                    )
                            )
                    );
                }

                if (!fieldsMapping.isEmpty()) {
                    fieldsMapping.forEach(entry ->
                            changelog.getFieldChanges().add(
                                    ChangedSymbol.addition(
                                            entry.getKey()
                                                    .stream()
                                                    .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                    .collect(Collectors.toList()),
                                            nmsLink(entry.getValue().getType())
                                    )
                            )
                    );
                }

                if (!methodsMapping.isEmpty()) {
                    methodsMapping.forEach(entry ->
                            changelog.getMethodChanges().add(
                                    ChangedSymbol.addition(
                                            entry.getKey()
                                                    .stream()
                                                    .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                    .collect(Collectors.toList()),
                                            nmsLink(entry.getValue().getReturnType()),
                                            entry.getValue()
                                                    .getParameters()
                                                    .stream()
                                                    .map(this::nmsLink)
                                                    .collect(Collectors.toList())
                                    )
                            )
                    );
                }
            } else {
                classMapping
                        .stream()
                        .map(entry -> {
                            var old = definition.getMapping().entrySet().stream()
                                    .filter(e -> Arrays.asList(e.getKey().getKey().split(",")).contains(previousVersion) && e.getKey().getValue() == entry.getKey().getValue())
                                    .findFirst();

                            if (old.isPresent()) {
                                if (!old.get().getValue().equals(entry.getValue())) {
                                    return Map.entry(entry.getKey().getValue(), Map.entry(old.get().getValue(), entry.getValue()));
                                }
                            } else {
                                return Map.entry(entry.getKey().getValue(), new AbstractMap.SimpleEntry<String, String>(null, entry.getValue()));
                            }

                            return null;
                        })
                        .filter(Objects::nonNull)
                        .forEach(o -> changelog.getNameMappings().put(o.getKey(), o.getValue()));

                Stream
                        .concat(
                                constructorsMapping
                                        .stream()
                                        .map(entry -> {
                                            if (!entry.getSupportedVersions().contains(previousVersion)) {
                                                return ChangedSymbol.addition(
                                                        List.of(),
                                                        null,
                                                        entry.getParameters()
                                                                .stream()
                                                                .map(this::nmsLink)
                                                                .collect(Collectors.toList())

                                                );
                                            }

                                            return null;
                                        }),
                                definition.getConstructors()
                                        .stream()
                                        .filter(e -> e.getSupportedVersions().contains(previousVersion) && !e.getSupportedVersions().contains(s))
                                        .map(entry -> ChangedSymbol.removal(
                                                List.of(),
                                                null,
                                                entry.getParameters()
                                                        .stream()
                                                        .map(this::nmsLink)
                                                        .collect(Collectors.toList())

                                        ))
                        )
                        .filter(Objects::nonNull)
                        .forEach(changelog.getConstructorChanges()::add);

                var oldFields = definition.getFields()
                        .stream()
                        .map(e -> Map.entry(e.getMapping()
                                .entrySet()
                                .stream()
                                .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(previousVersion))
                                .collect(Collectors.toList()), e))
                        .filter(l -> !l.getKey().isEmpty())
                        .collect(Collectors.toList());

                Stream
                        .concat(
                                fieldsMapping
                                        .stream()
                                        .map(entry -> {
                                            if (!oldFields.contains(entry)) {
                                                var similar = oldFields.stream()
                                                        .filter(e -> e.getValue() == entry.getValue())
                                                        .findFirst();

                                                if (similar.isPresent()) {
                                                    return List.of(
                                                        ChangedSymbol.removal(
                                                                similar.get().getKey()
                                                                        .stream()
                                                                        .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                        .collect(Collectors.toList()),
                                                                nmsLink(similar.get().getValue().getType())
                                                        ),
                                                        ChangedSymbol.addition(
                                                                entry.getKey()
                                                                        .stream()
                                                                        .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                        .collect(Collectors.toList()),
                                                                nmsLink(entry.getValue().getType())
                                                        )
                                                    );
                                                }

                                                return List.of(
                                                        ChangedSymbol.addition(
                                                                entry.getKey()
                                                                        .stream()
                                                                        .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                        .collect(Collectors.toList()),
                                                                nmsLink(entry.getValue().getType())
                                                        )
                                                );
                                            }

                                            return null;
                                        }),
                                oldFields
                                        .stream()
                                        .filter(e -> fieldsMapping.stream().noneMatch(e2 -> e2.getValue() == e.getValue()))
                                        .map(entry -> List.of(
                                                    ChangedSymbol.removal(
                                                            entry.getKey()
                                                                    .stream()
                                                                    .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                    .collect(Collectors.toList()),
                                                            nmsLink(entry.getValue().getType())
                                                    )
                                                )
                                        )
                        )
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .forEach(changelog.getFieldChanges()::add);

                var oldMethods = definition.getMethods()
                        .stream()
                        .map(e -> Map.entry(e.getMapping()
                                .entrySet()
                                .stream()
                                .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(previousVersion))
                                .collect(Collectors.toList()), e))
                        .filter(l -> !l.getKey().isEmpty())
                        .collect(Collectors.toList());

                Stream
                        .concat(
                                methodsMapping
                                        .stream()
                                        .map(entry -> {
                                            if (!oldMethods.contains(entry)) {
                                                var similar = oldMethods.stream()
                                                        .filter(e -> e.getValue() == entry.getValue())
                                                        .findFirst();

                                                if (similar.isPresent()) {
                                                    return List.of(
                                                            ChangedSymbol.removal(
                                                                    similar.get().getKey()
                                                                            .stream()
                                                                            .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                            .collect(Collectors.toList()),
                                                                    nmsLink(similar.get().getValue().getReturnType()),
                                                                    similar.get().getValue()
                                                                            .getParameters()
                                                                            .stream()
                                                                            .map(this::nmsLink)
                                                                            .collect(Collectors.toList())
                                                            ),
                                                            ChangedSymbol.addition(
                                                                    entry.getKey()
                                                                            .stream()
                                                                            .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                            .collect(Collectors.toList()),
                                                                    nmsLink(entry.getValue().getReturnType()),
                                                                    entry.getValue()
                                                                            .getParameters()
                                                                            .stream()
                                                                            .map(this::nmsLink)
                                                                            .collect(Collectors.toList())
                                                            )
                                                    );
                                                }

                                                return List.of(
                                                        ChangedSymbol.addition(
                                                                entry.getKey()
                                                                        .stream()
                                                                        .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                        .collect(Collectors.toList()),
                                                                nmsLink(entry.getValue().getReturnType()),
                                                                entry.getValue()
                                                                        .getParameters()
                                                                        .stream()
                                                                        .map(this::nmsLink)
                                                                        .collect(Collectors.toList())
                                                        )
                                                );
                                            }

                                            return null;
                                        }),
                                oldMethods
                                        .stream()
                                        .filter(e -> methodsMapping.stream().noneMatch(e2 -> e2.getValue() == e.getValue()))
                                        .map(entry ->
                                                List.of(
                                                        ChangedSymbol.removal(
                                                                entry.getKey()
                                                                        .stream()
                                                                        .map(e -> Map.entry(e.getKey().getValue(), e.getValue()))
                                                                        .collect(Collectors.toList()),
                                                                nmsLink(entry.getValue().getReturnType()),
                                                                entry.getValue()
                                                                        .getParameters()
                                                                        .stream()
                                                                        .map(this::nmsLink)
                                                                        .collect(Collectors.toList())
                                                        )
                                                )
                                        )
                        )
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .forEach(changelog.getMethodChanges()::add);
            }

            finalVersions.add(changelog);
        });

        context.setVariable("versions", finalVersions);
    }

    public ClassNameLink nmsLink(ClassDefinition.Link link) {
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
            var finalType = type;
            var name = joinedMappingsClassLinks.entrySet().stream().filter(e -> e.getValue().equals(finalType)).map(Map.Entry::getKey).findFirst().orElse(finalType);
            return new ClassNameLink(name.substring(name.lastIndexOf(".") + 1), type + ".html", name, suffix.toString());
        } else {
            return new ClassNameLink(link.getType(), null, null, null); // TODO: Indexer
        }
    }
}
