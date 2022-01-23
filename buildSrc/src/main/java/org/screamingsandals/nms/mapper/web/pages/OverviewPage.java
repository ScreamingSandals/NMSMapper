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

import org.screamingsandals.nms.mapper.web.WebGenerator;
import org.screamingsandals.nms.mapper.web.components.NavbarLink;
import org.screamingsandals.nms.mapper.single.Mapping;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Set;

public class OverviewPage extends AbstractPage {
    private final Mapping mapping;
    private final Set<String> packages;

    public OverviewPage(Mapping mapping, String version, Set<String> packages) {
        super(
                "overview",
                version + "/index.html",
                "NMS mapping - v" + version,
                List.of(
                        new NavbarLink("Version Overview", null, true),
                        new NavbarLink("Documentation", WebGenerator.DOC_LINK, false)
                ),
                true
        );
        this.mapping = mapping;
        this.packages = packages;
    }

    @Override
    public void fillContext(Context context) {
        context.setVariable("defaultMapping", mapping.getDefaultMapping());
        context.setVariable("licenses", mapping.getLicenses());
        context.setVariable("packages", packages);
    }
}
