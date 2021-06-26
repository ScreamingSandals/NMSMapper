package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import lombok.SneakyThrows;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static j2html.TagCreator.*;

@Data
public class DescriptionPage implements WebsiteComponent {
    private final String keyName;
    private final ClassDefinition definition;
    private final Map<String, ClassDefinition> mappings;
    private final MappingType defaultMapping;

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
                                                                ).withClass("nav-item"),
                                                                li(
                                                                        a(
                                                                                "History"
                                                                        ).withClass("nav-link")
                                                                        .withHref("../".repeat(keyName.split("\\.").length) + "history/" + definition.getJoinedKey() + ".html")
                                                                ).withClass("nav-item")
                                                        ).withClass("navbar-nav")

                                                ).withClass("collapse navbar-collapse")
                                        ).withClass("container-fluid")
                                ).withClass("navbar navbar-light bg-light navbar-expand"),
                                div(keyName.lastIndexOf(".") != -1 ? ("Package " + keyName.substring(0, keyName.lastIndexOf("."))) : "(default)"),
                                h1(text(MiscUtils.getModifierString(definition.getModifier())), text(definition.getType().name().toLowerCase() + " " + keyName.substring(keyName.lastIndexOf(".") + 1))),
                                div(definition.getType() != ClassDefinition.Type.INTERFACE ? span(text("extends "), linkIfNms(definition.getSuperclass())) : text("")),
                                resolveImplementations(),
                                hr(),
                                MiscUtils.descriptions(defaultMapping),
                                ul(
                                        getMappings()
                                ),
                                div(
                                        b("Field summary"),
                                        table(
                                                thead(
                                                        tr(
                                                                th("Modifier and Type"),
                                                                th("Field")
                                                        )
                                                ),
                                                tbody(
                                                        getFields()
                                                )
                                        ).withClass("table table-stripped")
                                ),
                                div(
                                        b("Constructor Summary"),
                                        table(
                                                thead(
                                                        tr(
                                                                th("Modifier"),
                                                                th("Constructor")
                                                        )
                                                ),
                                                tbody(
                                                        getConstructors()
                                                )
                                        ).withClass("table table-stripped")
                                ).withClass("methods"),
                                div(
                                        b("Method summary"),
                                        table(
                                                thead(
                                                        tr(
                                                                th("Modifier and Type"),
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
                        td(text(MiscUtils.getModifierString(field.getModifier())), linkIfNms(field.getType())),
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
                        td(text(MiscUtils.getModifierString(method.getModifier())), linkIfNms(method.getReturnType())),
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

    public DomContent[] getConstructors() {
        return definition.getConstructors()
                .stream()
                .map(constructor -> tr(
                        td(text(MiscUtils.getModifierString(constructor.getModifier()))),
                        td(generateMethodDescriptor(constructor.getParameters(), defaultMapping))
                ))
                .toArray(DomContent[]::new);
    }

    public DomContent generateMethodDescriptor(ClassDefinition.MethodDefinition method, MappingType mappingType) {
        return generateMethodDescriptor(method.getParameters(), mappingType);
    }

    public DomContent generateMethodDescriptor(List<ClassDefinition.Link> parameters, MappingType mappingType) {
        var list = new ArrayList<DomContent>();
        list.add(text("("));
        AtomicInteger counter = new AtomicInteger();
        parameters.forEach(link -> {
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
            var type = link.getType();
            var suffix = new StringBuilder();
            while (type.endsWith("[]")) {
                suffix.append("[]");
                type = type.substring(0, type.length() - 2);
            }
            if (type.matches(".*\\$\\d+")) { // WTF? How
                suffix.insert(0, type.substring(type.lastIndexOf("$")));
                type = type.substring(0, type.lastIndexOf("$"));
            }
            return span(a(mappings.get(type).getMapping().getOrDefault(mappingType, type))
                    .withHref(generateLink(type)), text(suffix.toString()));
        } else {
            return text(link.getType());
        }
    }

    public DomContent linkIfNms(ClassDefinition.Link link) {
       return convertToMapping(link, defaultMapping);
    }

    @SneakyThrows
    public String generateLink(String clazz) {
        // TODO: Generate better links
        var moj = clazz.replaceAll("\\[]", "");

        return "../".repeat(keyName.split("\\.").length - 1) + mappings
                .get(moj)
                .getMapping()
                .getOrDefault(defaultMapping, moj)
                .replace(".", "/")
                .replace("${V}", "VVV")
                + ".html";
    }

    public DomContent resolveImplementations() {
        var d = div();

        var c = definition;

        do {
            c.getInterfaces().forEach(link -> {
                if (d.getNumChildren() > 0) {
                    d.with(text(", "));
                }

                d.with(linkIfNms(link));
            });

            c = c.getSuperclass().isNms() ? mappings.get(c.getSuperclass().getType()) : null;
        } while (c != null);

        if (d.getNumChildren() > 0) {
            return div(
                    div(b("All known superinterfaces:")),
                    d
            );
        }

        return text("");
    }
}
