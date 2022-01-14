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
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.License;
import org.screamingsandals.nms.mapper.utils.MiscUtils;
import org.screamingsandals.nms.mapper.web.parts.CompactTablePart;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class OverviewPage extends AbstractPage {
    private final String docsName;
    private final Set<String> packages;
    private final MappingType defaultMapping;
    private final List<License> licenses;

    @Override
    protected void configure() {
        title = docsName;
        basePath = "../";
        navbarPart = NavbarPart.builder()
                .mainPageUrl("../")
                .currentPage(NavbarPart.CurrentPage.OVERVIEW)
                .build();
    }

    @Override
    protected void constructContent(ContainerTag div) {
        div.with(MiscUtils.descriptions(defaultMapping));

        div.with(
                button("Show/hide licenses")
                        .withClass("btn btn-primary btn-sm btn-block mb-2 w-100")
                        .attr("onClick", "document.getElementById('licenses').classList.toggle('d-none')")
        );

        div.with(div(
                licenses.stream().map(entry -> {
                    var mappingType = entry.getMappingType();
                    var s = entry;
                    return div(
                            div(MiscUtils.capitalizeFirst(mappingType.name()) + " license").withClass("card-header"),
                            div(s.getLicense().replace("\n\n", "\n")).withClass("card-body").withStyle("white-space: pre-wrap;"),
                            div(s.getLinks().stream().map(s1 -> a(s1).withHref(s1).withClass("d-block text-white")).toArray(DomContent[]::new)).withClass("card-footer")
                    ).withClass("card text-white mb-1 bg-" + MiscUtils.chooseBootstrapColor(mappingType));
                }).toArray(DomContent[]::new)
        ).withClass("d-none").withId("licenses"));

        div.with(
                new CompactTablePart(
                        "Packages",
                        List.of("Package"),
                        packages.stream()
                                .sorted()
                                .map(key -> {
                                            var pathKey = key
                                                    .replace(".", "/")
                                                    .replace("${V}", "VVV");

                                            return Map.of(
                                                    "Package", (DomContent) a(key)
                                                            .withHref(pathKey + "/index.html")
                                            );
                                        }
                                )
                                .collect(Collectors.toList())
                ).generate()
        );
    }
}
