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
