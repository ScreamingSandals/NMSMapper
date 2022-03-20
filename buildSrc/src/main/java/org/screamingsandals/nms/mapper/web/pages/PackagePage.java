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

import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.web.WebGenerator;
import org.screamingsandals.nms.mapper.web.components.ClassNameLink;
import org.screamingsandals.nms.mapper.web.components.NavbarLink;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.Mapping;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PackagePage extends AbstractPage {
    private final Mapping mapping;
    private final List<Map.Entry<ClassDefinition.Type, String>> paths;

    public PackagePage(Mapping mapping, String packageName, List<Map.Entry<ClassDefinition.Type, String>> paths) {
        super(
                "package",
                mapping.getVersion() + "/" + packageName.replace(".", "/").replace("${V}", "VVV") + "/index.html",
                mapping.getDefaultMapping() == MappingType.SPIGOT && mapping.getSpigotNms() != null ? packageName.replace("${V}", mapping.getSpigotNms()) : packageName,
                List.of(
                        new NavbarLink("Version Overview", "../".repeat(packageName.split("\\.").length), false),
                        new NavbarLink("Documentation", WebGenerator.DOC_LINK, false)
                ),
                true,
                false,
                false,
                false
        );
        this.mapping = mapping;
        this.paths = paths;
    }

    @Override
    public void fillContext(Context context) {
        context.setVariable("defaultMapping", mapping.getDefaultMapping());

        context.setVariable("interfaces", paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.INTERFACE)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> new ClassNameLink(entry.substring(0, entry.length() - 5), entry, null, null))
                .collect(Collectors.toList()));

        context.setVariable("annotations", paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.ANNOTATION)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> new ClassNameLink(entry.substring(0, entry.length() - 5), entry, null, null))
                .collect(Collectors.toList()));

        context.setVariable("classes", paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.CLASS)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> new ClassNameLink(entry.substring(0, entry.length() - 5), entry, null, null))
                .collect(Collectors.toList()));

        context.setVariable("enums", paths.stream()
                .filter(e -> e.getKey() == ClassDefinition.Type.ENUM)
                .map(Map.Entry::getValue)
                .sorted()
                .map(entry -> new ClassNameLink(entry.substring(0, entry.length() - 5), entry, null, null))
                .collect(Collectors.toList()));
    }
}
