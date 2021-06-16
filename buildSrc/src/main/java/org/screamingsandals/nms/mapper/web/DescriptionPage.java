package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import lombok.SneakyThrows;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static j2html.TagCreator.*;

@Data
public class DescriptionPage implements WebsiteComponent {
    private final String keyName;
    private final ClassDefinition definition;
    private final Map<String, ClassDefinition> mappings;

    @Override
    public ContainerTag generate() {
        return html(
                head(
                        title(keyName),
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
                                                                                .withHref("../".repeat(keyName.split("\\.").length))
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Overview"
                                                                        )
                                                                                .withClass("nav-link")
                                                                                .withHref("../".repeat(keyName.split("\\.").length - 1))
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Package"
                                                                        ).withClass("nav-link")
                                                                        .withHref("index.html")
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "Class"
                                                                        ).withClass("nav-link active")
                                                                ).withClass("nav-item")
                                                        ).withClass("navbar-nav")

                                                ).withClass("collapse navbar-collapse")
                                        ).withClass("container-fluid")
                                ).withClass("navbar navbar-light bg-light navbar-expand"),
                                div("Package " + keyName.substring(0, keyName.lastIndexOf("."))),
                                h1(keyName.substring(keyName.lastIndexOf(".") + 1)),
                                ul(
                                        getMappings()
                                ),
                                div(
                                        b("Field summary"),
                                        table(
                                                thead(
                                                        tr(
                                                                th(/*"Modifier and Type"*/ "Type"),
                                                                th("Field")
                                                        )
                                                ),
                                                tbody(
                                                        getFields()
                                                )
                                        ).withClass("table table-stripped")
                                ),
                                div(
                                        b("Method summary"),
                                        table(
                                                thead(
                                                        tr(
                                                                th(/*"Modifier and Type"*/ "Type"),
                                                                th("Method")
                                                        )
                                                ),
                                                tbody(
                                                        getMethods()
                                                )
                                        ).withClass("table table-stripped")
                                ).withClass("methods")
                        ).withClass("main")
                )
        );
    }

    public DomContent[] getMappings() {
        return definition.getMapping()
                .entrySet()
                .stream()
                .map(entry -> li(entry.getKey() + ": " + entry.getValue()))
                .toArray(DomContent[]::new);
    }

    public DomContent[] getFields() {
        return definition.getFields()
                .values()
                .stream()
                .map(field -> tr(
                        td(linkIfNms(field.getType())),
                        td(
                                ul(
                                        field
                                                .getMapping()
                                                .entrySet()
                                                .stream()
                                                .map(entry ->
                                                        li(entry.getKey() + ": " + entry.getValue())
                                                )
                                                .toArray(DomContent[]::new)
                                )
                        )
                ))
                .toArray(DomContent[]::new);
    }

    public DomContent[] getMethods() {
        return definition.getMethods()
                .stream()
                .map(method -> tr(
                        td(linkIfNms(method.getReturnType())),
                        td(
                                ul(
                                        method
                                                .getMapping()
                                                .entrySet()
                                                .stream()
                                                .map(entry ->
                                                        li(text(entry.getKey() + ": " + entry.getValue()), generateMethodDescriptor(method, entry.getKey()))
                                                )
                                                .toArray(DomContent[]::new)
                                )
                        )
                ))
                .toArray(DomContent[]::new);
    }

    public DomContent generateMethodDescriptor(ClassDefinition.MethodDefinition method, MappingType mappingType) {
        var list = new ArrayList<DomContent>();
        list.add(text("("));
        AtomicInteger counter = new AtomicInteger();
        method.getParameters().forEach(link -> {
            if (list.size() > 1) {
                list.add(text(", "));
            }
            list.add(convertToMapping(link, mappingType));
            list.add(text(" arg" + counter.getAndIncrement()));
        });
        list.add(text(")"));
        return span(list.toArray(DomContent[]::new));
    }

    public DomContent convertToMapping(ClassDefinition.Link link, MappingType mappingType) {
        if (link.isNms()) {
            return a(mappings.get(link.getType()).getMapping().getOrDefault(mappingType, mappings.get(link.getType()).getMapping().get(MappingType.OBFUSCATED)))
                    .withHref(generateLink(link.getType()));
        } else {
            return text(link.getType());
        }
    }

    public DomContent linkIfNms(ClassDefinition.Link link) {
        if (link.isNms()) {
            return a(link.getType())
                    .withHref(generateLink(link.getType()));
        } else {
            return text(link.getType());
        }
    }

    @SneakyThrows
    public String generateLink(String clazz) {
        // TODO: Generate better links
        return "../".repeat(keyName.split("\\.").length - 1) + clazz
                .replace(".", "/")
                .replace("${V}", "VVV")
                .replace("[]", "") + ".html";
    }
}
