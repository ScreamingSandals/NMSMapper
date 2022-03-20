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

import lombok.Getter;
import org.gradle.util.VersionNumber;
import org.screamingsandals.nms.mapper.web.WebGenerator;
import org.screamingsandals.nms.mapper.web.components.NavbarLink;
import org.screamingsandals.nms.mapper.web.components.VersionRecord;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainPage extends AbstractPage {
    @Getter
    private final List<VersionRecord> versions = new ArrayList<>();

    public MainPage() {
        super(
                "index",
                "index.html",
                "NMS mapping browser",
                List.of(
                        new NavbarLink("Version overview", null, false),
                        new NavbarLink("Documentation", WebGenerator.DOC_LINK, false)
                ),
                false,
                false,
                false,
                false
        );
    }

    @Override
    public void fillContext(Context context) {
        context.setVariable("versions", versions
                .stream()
                .sorted(Comparator.comparing((VersionRecord o) -> VersionNumber.parse(o.getVersion())).reversed())
                .collect(Collectors.toList())
        );
    }
}
