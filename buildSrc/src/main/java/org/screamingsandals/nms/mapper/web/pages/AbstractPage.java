/*
 * Copyright 2023 ScreamingSandals
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

import lombok.Data;
import org.screamingsandals.nms.mapper.web.components.NavbarLink;
import org.thymeleaf.context.Context;

import java.util.List;

@Data
public abstract class AbstractPage {
    private final String templateName;
    private final String finalLocation;
    private final String pageTitle;
    private final List<NavbarLink> links;
    private final boolean searchAllowed;
    private final boolean containerFluid;
    private final boolean highlightJs;
    private final boolean disableScripts;

    public abstract void fillContext(Context context);
}
