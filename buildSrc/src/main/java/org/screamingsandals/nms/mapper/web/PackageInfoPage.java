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

package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.MiscUtils;
import org.screamingsandals.nms.mapper.web.parts.CompactTablePart;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class PackageInfoPage extends AbstractPage {
    private final String packageName;
    private final List<Map.Entry<ClassDefinition.Type, String>> paths;
    private final MappingType defaultMapping;

    @Override
    protected void configure() {
        title = packageName;
        basePath = "../".repeat(packageName.split("\\.").length + 1);
        navbarPart = NavbarPart.builder()
                .mainPageUrl("../".repeat(packageName.split("\\.").length + 1))
                .overviewUrl("../".repeat(packageName.split("\\.").length))
                .currentPage(NavbarPart.CurrentPage.PACKAGE)
                .build();
    }

    @Override
    protected void constructContent(ContainerTag div) {
        div.with(MiscUtils.descriptions(defaultMapping));

        var interfaces = paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.INTERFACE)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> Map.of(
                        "Interface", (DomContent) a(entry.substring(0, entry.length() - 5))
                                .withHref(entry)
                        )
                )
                .collect(Collectors.toList());

        if (!interfaces.isEmpty()) {
            div.with(
                    new CompactTablePart(
                            "Interface summary",
                            List.of("Interface"),
                            interfaces
                    ).generate()
            );
        }

        var annotations = paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.ANNOTATION)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> Map.of(
                        "Annotation", (DomContent) a(entry.substring(0, entry.length() - 5))
                                .withHref(entry)
                        )
                )
                .collect(Collectors.toList());

        if (!annotations.isEmpty()) {
            div.with(
                    new CompactTablePart(
                            "Annotation summary",
                            List.of("Annotation"),
                            annotations
                    ).generate()
            );
        }

        var classes = paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.CLASS)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> Map.of(
                        "Class", (DomContent) a(entry.substring(0, entry.length() - 5))
                                .withHref(entry)
                        )
                )
                .collect(Collectors.toList());

        if (!classes.isEmpty()) {
            div.with(
                    new CompactTablePart(
                            "Class summary",
                            List.of("Class"),
                            classes
                    ).generate()
            );
        }

        var enums = paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.ENUM)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> Map.of(
                        "Enum", (DomContent) a(entry.substring(0, entry.length() - 5))
                                .withHref(entry)
                        )
                )
                .collect(Collectors.toList());

        if (!enums.isEmpty()) {
            div.with(
                    new CompactTablePart(
                            "Enum summary",
                            List.of("Enum"),
                            enums
                    ).generate()
            );
        }
    }
}
