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
import org.screamingsandals.nms.mapper.web.parts.FooterPart;
import org.screamingsandals.nms.mapper.web.parts.HeadPart;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import static j2html.TagCreator.*;

public abstract class AbstractPage implements WebsiteComponent {
    protected String title;
    protected String basePath = null;
    protected NavbarPart navbarPart;

    @Override
    public final ContainerTag generate() {
        configure();

        var div = div(
                h1(title).withClass("my-3")
        ).withClass("container");

        constructContent(div);

        div.with(new FooterPart().generate());

        return html(
                new HeadPart(title, basePath).generate(),
                body(
                        navbarPart.generate(),
                        div
                )
        );
    }

    protected abstract void configure();

    protected abstract void constructContent(ContainerTag div);
}
