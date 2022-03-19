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

import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.mapper.errors.MappingError;
import org.screamingsandals.nms.mapper.web.WebGenerator;
import org.screamingsandals.nms.mapper.web.components.*;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.Mapping;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.JavadocIndexer;
import org.screamingsandals.nms.mapper.utils.MiscUtils;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DescriptionPage extends AbstractPage {
    private final Mapping mapping;
    private final ClassDefinition definition;
    private final String className;

    public DescriptionPage(Mapping mapping, String className, ClassDefinition definition) {
        super(
                "description",
                mapping.getVersion() + "/" + MiscUtils.classNameToUrl(className),
                MiscUtils.getModifierString(definition.getModifier()) + definition.getType().name().toLowerCase() + " " + className.substring(className.lastIndexOf(".") + 1),
                List.of(
                        new NavbarLink("Version Overview", "../".repeat(className.split("\\.").length + (className.split("\\.").length == 1 ? 1 : 0) - 1), false),
                        new NavbarLink("Documentation", WebGenerator.DOC_LINK, false)
                ),
                true
        );
        this.mapping = mapping;
        this.definition = definition;
        this.className = className;
    }

    @Override
    public void fillContext(Context context) {
        context.setVariable("defaultMapping", mapping.getDefaultMapping());
        if (mapping.getDefaultMapping() == MappingType.SPIGOT && mapping.getSpigotNms() != null) {
            context.setVariable("packageName", className.contains(".") ? className.substring(0, className.lastIndexOf(".")).replace("${V}", mapping.getSpigotNms()) : "(default package)");
        } else {
            context.setVariable("packageName", className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "(default package)");
        }
        context.setVariable("historyLink", "../".repeat(className.split("\\.").length + (className.split("\\.").length == 1 ? 1 : 0)) + "history/" + definition.getJoinedKey() + ".html");

        if (definition.getType() != ClassDefinition.Type.INTERFACE) {
            context.setVariable("extends", convertToMapping(definition.getSuperclass(), mapping.getDefaultMapping()));
        }
        context.setVariable("accessorName", getAccessorName());
        context.setVariable("accessorRequireClass", getRightReqClass());
        context.setVariable("knownSuperinterfaces", getAllKnownSuperinterfaces());
        context.setVariable("errors", definition.getMappingErrors());
        context.setVariable("totalErrorSeverity", Arrays.stream(MappingError.Level.values()).filter(level -> definition.getMappingErrors().stream().anyMatch(mappingError -> mappingError.getErrorLevel() == level)).findFirst().orElse(MappingError.Level.OKAY));

        context.setVariable("classMappings", definition.getMapping()
                .entrySet()
                .stream()
                .map(entry -> new SymbolMapping(entry.getKey(), entry.getKey() == MappingType.SPIGOT && mapping.getSpigotNms() != null ? entry.getValue().replace("${V}", mapping.getSpigotNms()) : entry.getValue()))
                .collect(Collectors.toList())
        );

        context.setVariable("classFields", definition.getFields()
                .values()
                .stream()
                .map(field -> new Symbol(
                        MiscUtils.getModifierString(field.getModifier()),
                        convertToMapping(field.getType(), mapping.getDefaultMapping()),
                        field
                                .getMapping()
                                .entrySet()
                                .stream()
                                .map(entry -> new SymbolMapping(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList())
                        )
                )
                .collect(Collectors.toList())
        );

        context.setVariable("classConstructors", definition.getConstructors()
                .stream()
                .map(constructor -> new Constructor(
                                MiscUtils.getModifierString(constructor.getModifier()),
                                generateMethodDescriptor(constructor.getParameters(), mapping.getDefaultMapping())
                        )
                )
                .collect(Collectors.toList())
        );

        context.setVariable("classMethods", definition.getMethods()
                .stream()
                .map(method -> new Symbol(
                        MiscUtils.getModifierString(method.getModifier()),
                        convertToMapping(method.getReturnType(), mapping.getDefaultMapping()),
                        method.getMapping()
                                .entrySet()
                                .stream()
                                .map(entry -> new SymbolMapping(entry.getKey(), entry.getValue(), generateMethodDescriptor(method.getParameters(), entry.getKey())))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList())
        );
    }

    private List<SymbolArgument> generateMethodDescriptor(List<ClassDefinition.Link> parameters, MappingType mappingType) {
        AtomicInteger counter = new AtomicInteger();
        return parameters
                .stream()
                .map(link -> convertToMapping(link, mappingType))
                .map(classNameLink -> new SymbolArgument(classNameLink, "arg" + counter.getAndIncrement()))
                .collect(Collectors.toList());
    }

    public List<ClassNameLink> getAllKnownSuperinterfaces() {
        var interfaces = new ArrayList<ClassNameLink>();

        var c = definition;

        do {
            c.getInterfaces().forEach(link -> interfaces.add(convertToMapping(link, mapping.getDefaultMapping())));

            c = c.getSuperclass().isNms() ? mapping.getMappings().get(c.getSuperclass().getType()) : null;
        } while (c != null);

        return interfaces;
    }

    private String getRightReqClass() {
        switch (mapping.getDefaultMapping()) {
            case MOJANG:
                return className;
            case SPIGOT:
                return "spigot:" + className.substring(className.lastIndexOf(".") + 1);
            default:
                return mapping.getDefaultMapping().name().toLowerCase() + ":" + className;
        }
    }

    private String getAccessorName() {
        var clazz = getRightReqClass();
        var li = clazz.lastIndexOf(".");
        return (clazz.substring(li < 0 ? (clazz.startsWith("spigot:") ? 7 : 0) : (li + 1)) + "Accessor").replace("$", "_i_");
    }

    public ClassNameLink convertToMapping(ClassDefinition.Link link, MappingType mappingType) {
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
            var mappingName = mapping.getMappings().get(type).getMapping().getOrDefault(mappingType, type);
            if (mappingType == MappingType.SPIGOT && mapping.getSpigotNms() != null) {
                mappingName = mappingName.replace("${V}", mapping.getSpigotNms());
            }
            return new ClassNameLink(mappingName.substring(mappingName.lastIndexOf(".") + 1), generateLink(type), mappingName, suffix.toString());
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


    @SneakyThrows
    public String generateLink(String clazz) {
        // TODO: Generate better links
        var moj = clazz.replaceAll("\\[]", "");
        var map = mapping
                .getMappings()
                .get(moj)
                .getMapping()
                .getOrDefault(mapping.getDefaultMapping(), moj);

        return "../".repeat(className.split("\\.").length - 1 + (className.split("\\.").length == 1 ? 1 : 0)) +
                (map.split("\\.").length == 1 ? "default-pkg/" : "") +
                map.replace(".", "/")
                        .replace("${V}", "VVV")
                + ".html";
    }
}
