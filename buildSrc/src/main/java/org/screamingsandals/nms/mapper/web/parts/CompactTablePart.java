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

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import org.screamingsandals.nms.mapper.web.WebsiteComponent;

import java.util.List;
import java.util.Map;

import static j2html.TagCreator.*;

@Data
public class CompactTablePart implements WebsiteComponent {
    private final String title;
    private final List<String> columns;
    private final List<Map<String, DomContent>> rows;

    @Override
    public ContainerTag generate() {
        return div(
                b(title).withClass("badge bg-info m-1"),
                table(
                        thead(
                                tr(
                                        columns.stream()
                                                .map(TagCreator::th)
                                                .peek(t -> t.withClass("py-1"))
                                                .toArray(DomContent[]::new)
                                )
                        ),
                        tbody(
                                rows.stream()
                                        .map(map -> {
                                            var tr = tr();
                                            columns.forEach(s ->
                                                    tr.with(td(map.get(s))).withClass("py-1")
                                            );
                                            return tr;
                                        })
                                        .toArray(DomContent[]::new)
                        )
                ).withClass("table table-stripped")
        );
    }
}
