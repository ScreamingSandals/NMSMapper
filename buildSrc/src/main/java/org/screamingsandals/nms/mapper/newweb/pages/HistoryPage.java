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

import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.thymeleaf.context.Context;

import java.util.List;

public class HistoryPage extends AbstractPage {
    public HistoryPage(String classHashName, String title) {
        super(
                "history",
                "history/" + classHashName + ".html",
                title,
                List.of(
                        new NavbarLink("Main page", "../", false),
                        new NavbarLink("Overview", null, false),
                        new NavbarLink("Package", null, false),
                        new NavbarLink("Class", null, false),
                        new NavbarLink("History", null, true)
                ),
                false
        );
    }

    @Override
    public void fillContext(Context context) {

    }
}
