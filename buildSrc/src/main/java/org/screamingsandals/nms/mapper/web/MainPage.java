package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;

import java.util.List;

import static j2html.TagCreator.*;
import static j2html.TagCreator.a;

@Data
public class MainPage implements WebsiteComponent {
    private final List<String> versions;

    @Override
    public ContainerTag generate() {

        return html(
                head(
                        title("NMS Mapping browser"),
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
                                                                        ).withClass("nav-link active")
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Overview"
                                                                        ).withClass("nav-link disabled")
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
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "History"
                                                                        ).withClass("nav-link disabled")
                                                                ).withClass("nav-item")
                                                        ).withClass("navbar-nav")
                                                ).withClass("collapse navbar-collapse")
                                        ).withClass("container-fluid")
                                ).withClass("navbar navbar-light bg-light navbar-expand"),
                                h1("NMS Mapping browser"),
                                div(
                                        b("Packages"),
                                        table(
                                                thead(
                                                        tr(
                                                                th("Version")
                                                        )
                                                ),
                                                tbody(
                                                        versions.stream()
                                                                .map(key -> tr(
                                                                            td(
                                                                                    a(key).withHref(key + "/")
                                                                            )
                                                                    )
                                                                ).toArray(DomContent[]::new)
                                                )
                                        ).withClass("table table-stripped")
                                )
                        ).withClass("main")
                )
        );
    }
}
