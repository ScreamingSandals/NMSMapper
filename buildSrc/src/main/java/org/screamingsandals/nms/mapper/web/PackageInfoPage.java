package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import org.screamingsandals.nms.mapper.single.ClassDefinition;

import java.util.Map;

import static j2html.TagCreator.*;

@Data
public class PackageInfoPage implements WebsiteComponent {
    private final String packageName;
    private final Map<String, ClassDefinition> mappings;

    @Override
    public ContainerTag generate() {
        return html(
                head(
                        title(packageName),
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
                                                                        )
                                                                                .withClass("nav-link")
                                                                                .withHref("../".repeat(packageName.split("\\.").length + 1))
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Overview"
                                                                        )
                                                                                .withClass("nav-link")
                                                                                .withHref("../".repeat(packageName.split("\\.").length))
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        //.withHref("index.html") There's no overview page yet
                                                                        a(
                                                                                "Package"
                                                                        ).withClass("nav-link active")
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
                                h1("Package " + packageName),
                                div(
                                        b("Class/Interface/Enum summary"),
                                        table(
                                                thead(
                                                        tr(
                                                                th("Class") // TODO: split classes, interfaces and enums
                                                        )
                                                ),
                                                tbody(
                                                        mappings.keySet()
                                                                .stream()
                                                                .sorted()
                                                                .filter(entry -> entry.substring(0, entry.lastIndexOf(".")).equals(packageName))
                                                                .map(entry ->
                                                                        tr(
                                                                                td(
                                                                                        a(entry.substring(entry.lastIndexOf(".") + 1))
                                                                                                .withHref(entry.substring(entry.lastIndexOf(".") + 1) + ".html")
                                                                                )
                                                                        )
                                                                )
                                                                .toArray(DomContent[]::new)
                                                )
                                        ).withClass("table table-stripped")
                                )
                        ).withClass("main")
                )
        );
    }
}
