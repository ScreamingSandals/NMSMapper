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

package org.screamingsandals.nms.mapper.web.parts;

import j2html.tags.ContainerTag;
import lombok.Data;
import org.screamingsandals.nms.mapper.web.WebsiteComponent;

import static j2html.TagCreator.*;

@Data
public class HeadPart implements WebsiteComponent {
    private final String title;
    private final String basePath;

    @Override
    public ContainerTag generate() {
        return head(
                title(title),
                meta().withCharset("utf-8"),
                meta().withName("viewport").withContent("width=device-width, initial-scale=1.0"),
                link().withRel("stylesheet")
                        .withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css")
                        .attr("integrity", "sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3")
                        .attr("crossorigin", "anonymous"),
                link().withRel("stylesheet")
                        .withHref("https://code.jquery.com/ui/1.12.1/themes/smoothness/jquery-ui.css"),
                iff(basePath != null,
                    link().withRel("stylesheet")
                            .withHref(basePath + "static/highlight/styles/default.min.css")
                ),
                script()
                        .withSrc("https://code.jquery.com/jquery-3.6.0.min.js")
                        .attr("integrity", "sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=")
                        .attr("crossorigin", "anonymous"),
                script()
                        .withSrc("https://code.jquery.com/ui/1.12.1/jquery-ui.js"),
                script()
                        .withSrc("https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js")
                        .attr("integrity", "sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p")
                        .attr("crossorigin", "anonymous"),
                iff(basePath != null,
                    script()
                            .withSrc(basePath + "static/highlight/highlight.min.js")
                ),
                iff(basePath != null,
                        link().withRel("stylesheet")
                                .withHref(basePath + "static/css/search.css")
                ),
                iff(basePath != null,
                        script()
                                .withSrc(basePath + "static/js/search.js")
                ),
                link().withRel("stylesheet")
                        .withHref("https://cdn.jsdelivr.net/npm/bootstrap-dark-5@1.1.3/dist/css/bootstrap-nightfall.min.css")
                        .withId("darkModeCss")
                        .attr("media", "(prefers-color-scheme: dark)")
        );
    }
}
