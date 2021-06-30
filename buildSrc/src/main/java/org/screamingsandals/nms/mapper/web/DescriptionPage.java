package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.MiscUtils;
import org.screamingsandals.nms.mapper.web.parts.CompactTablePart;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class DescriptionPage extends AbstractPage {
    private final String keyName;
    private final ClassDefinition definition;
    private final Map<String, ClassDefinition> mappings;
    private final MappingType defaultMapping;

    @Override
    protected void configure() {
        title = MiscUtils.getModifierString(definition.getModifier()) + definition.getType().name().toLowerCase() + " " + keyName.substring(keyName.lastIndexOf(".") + 1);
        basePath = "../".repeat(keyName.split("\\.").length);
        navbarPart = NavbarPart.builder()
                .mainPageUrl("../".repeat(keyName.split("\\.").length))
                .overviewUrl("../".repeat(keyName.split("\\.").length - 1))
                .packageUrl("index.html")
                .historyUrl("../".repeat(keyName.split("\\.").length) + "history/" + definition.getJoinedKey() + ".html")
                .currentPage(NavbarPart.CurrentPage.CLASS)
                .build();
    }

    @Override
    protected void constructContent(ContainerTag div) {
        div.with(
                div(definition.getType() != ClassDefinition.Type.INTERFACE ? span(text("extends "), linkIfNms(definition.getSuperclass())) : text("")),
                resolveImplementations(),
                hr(),
                MiscUtils.descriptions(defaultMapping),
                ul(
                        getMappings()
                ).withClass("list-unstyled")
        );

        var fields = getFields();
        var constructors = getConstructors();
        var methods = getMethods();

        if (!fields.isEmpty()) {
            div.with(
                    new CompactTablePart(
                            "Field summary",
                            List.of("Modifier and Type", "Field"),
                            fields
                    ).generate()
            );
        }

        if (!constructors.isEmpty()) {
            div.with(
                    new CompactTablePart(
                            "Constructor summary",
                            List.of("Modifier", "Constructor"),
                            constructors
                    ).generate()
            );
        }

        if (!methods.isEmpty()) {
            div.with(
                    new CompactTablePart(
                            "Method summary",
                            List.of("Modifier and Type", "Method"),
                            methods
                    ).generate()
            );
        }
    }

    public DomContent[] getMappings() {
        return definition.getMapping()
                .entrySet()
                .stream()
                .map(entry -> li(MiscUtils.mappingToBadge(entry.getKey()), text(entry.getValue())))
                .toArray(DomContent[]::new);
    }

    public List<Map<String, DomContent>> getFields() {
        return definition.getFields()
                .values()
                .stream()
                .map(field -> Map.of(
                        "Modifier and Type", (DomContent) span(text(MiscUtils.getModifierString(field.getModifier())), linkIfNms(field.getType())),
                        "Field", ul(
                                field
                                        .getMapping()
                                        .entrySet()
                                        .stream()
                                        .map(entry ->
                                                li(MiscUtils.mappingToBadge(entry.getKey()), text(entry.getValue()))
                                        )
                                        .toArray(DomContent[]::new)
                        ).withClass("list-unstyled mb-0")
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, DomContent>> getMethods() {
        return definition.getMethods()
                .stream()
                .map(method -> Map.of(
                        "Modifier and Type", (DomContent) span(text(MiscUtils.getModifierString(method.getModifier())), linkIfNms(method.getReturnType())),
                        "Method", ul(
                                method
                                        .getMapping()
                                        .entrySet()
                                        .stream()
                                        .map(entry ->
                                                li(MiscUtils.mappingToBadge(entry.getKey()), text(entry.getValue()), generateMethodDescriptor(method, entry.getKey()))
                                        )
                                        .toArray(DomContent[]::new)
                        ).withClass("list-unstyled mb-0")
                        )
                )
                .collect(Collectors.toList());
    }

    public List<Map<String, DomContent>> getConstructors() {
        return definition.getConstructors()
                .stream()
                .map(constructor -> Map.of(
                        "Modifier", text(MiscUtils.getModifierString(constructor.getModifier())),
                        "Constructor", generateMethodDescriptor(constructor.getParameters(), defaultMapping)
                        )
                )
                .collect(Collectors.toList());
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
            var mappingName = mappings.get(type).getMapping().getOrDefault(mappingType, type);

            return span(a(mappingName.substring(mappingName.lastIndexOf(".") + 1))
                    .withHref(generateLink(type))
                    .withTitle(mappingName), text(suffix.toString()));
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
