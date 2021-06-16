package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;

import java.util.List;

import static j2html.TagCreator.*;

@Data
public class OverviewPage implements WebsiteComponent {
    private final String docsName;
    private final List<String> packages;

    @Override
    public ContainerTag generate() {
        return html(
                head(
                        title(docsName),
                        link().withRel("stylesheet")
                                .withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/css/bootstrap.min.css")
                                .attr("integrity", "sha384-+0n0xVW2eSR5OomGNYDnhzAbDsOXxcvSN1TPprVMTNDbiYZCxYbOOl7+AMvyTG2x")
                                .attr("crossorigin", "anonymous")
                ),
                body(
                        div(
                                nav(
                                        div(
                                                div(
                                                        ul(
                                                                li(
                                                                        a(
                                                                                "Main page"
                                                                        ).withClass("nav-link")
                                                                        .withHref("../")
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Overview"
                                                                        ).withClass("nav-link active")
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Package"
                                                                        ).withClass("nav-link disabled")
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Class"
                                                                        ).withClass("nav-link disabled")
                                                                ).withClass("nav-item")
                                                        ).withClass("navbar-nav")
                                                ).withClass("collapse navbar-collapse")
                                        ).withClass("container-fluid")
                                ).withClass("navbar navbar-light bg-light navbar-expand"),
                                h1(docsName),
                                div(
                                        b("Packages"),
                                        table(
                                                thead(
                                                        tr(
                                                                th("Package")
                                                        )
                                                ),
                                                tbody(
                                                        packages.stream()
                                                                .sorted()
                                                                .map(key -> {
                                                                    var pathKey = key
                                                                            .replace(".", "/")
                                                                            .replace("${V}", "VVV");

                                                                    return tr(
                                                                            td(
                                                                                    a(key)
                                                                                            .withHref(pathKey + "/index.html")
                                                                            )
                                                                    );
                                                                }).toArray(DomContent[]::new)
                                                )
                                        ).withClass("table table-stripped")
                                )
                        ).withClass("main")
                )
        );
    }
}
