package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gradle.util.VersionNumber;
import org.screamingsandals.nms.mapper.joined.JoinedClassDefinition;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static j2html.TagCreator.*;
import static j2html.TagCreator.a;

@EqualsAndHashCode(callSuper = true)
@Data
public class HistoryPage extends AbstractPage {
    private final String name;
    private final JoinedClassDefinition definition;
    private final Map<String, String> joinedMappingsClassLinks;
    private final Map<String, JoinedClassDefinition> joinedMappings;

    @Override
    protected void configure() {
        title = "History of " + name;
        navbarPart = NavbarPart.builder()
                .mainPageUrl("../")
                .currentPage(NavbarPart.CurrentPage.HISTORY)
                .build();
    }

    @Override
    protected void constructContent(ContainerTag div) {
        div.with(generateChangelogs());
    }

    private DomContent generateChangelogs() {
        var versions = definition.getMapping()
                .keySet()
                .stream()
                .filter(s -> s.getValue() == MappingType.OBFUSCATED)
                .map(s -> s.getKey().split(","))
                .flatMap(Stream::of)
                .sorted(Comparator.comparing(VersionNumber::parse).reversed())
                .collect(Collectors.toList());

        var mainContent = div();

        versions.forEach(s -> {
            var d = div(
                    h5(a(s).withHref("../" + s + "/" + definition.getPathKeys().get(s))).withClass("card-title")
            ).withClass("card card-body m-5");

            var changes = false;

            var previousVersionIndex = versions.indexOf(s) + 1;
            var previousVersion = versions.size() > previousVersionIndex ? versions.get(previousVersionIndex) : null;

            var classMapping = definition.getMapping()
                    .entrySet()
                    .stream()
                    .filter(e -> Arrays.asList(e.getKey().getKey().split(",")).contains(s))
                    .collect(Collectors.toList());

            var constructorsMapping = definition.getConstructors()
                    .stream()
                    .filter(e -> e.getSupportedVersions().contains(s))
                    .collect(Collectors.toList());

            var fieldsMapping = definition.getFields()
                    .stream()
                    .map(e -> Map.entry(e.getMapping()
                            .entrySet()
                            .stream()
                            .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(s))
                            .collect(Collectors.toList()), e))
                    .filter(l -> !l.getKey().isEmpty())
                    .collect(Collectors.toList());

            var methodsMapping = definition.getMethods()
                    .stream()
                    .map(e -> Map.entry(e.getMapping()
                            .entrySet()
                            .stream()
                            .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(s))
                            .collect(Collectors.toList()), e))
                    .filter(l -> !l.getKey().isEmpty())
                    .collect(Collectors.toList());

            if (previousVersion == null) { // Everything is change
                changes = true;
                d.with(p(i("First known occurrence")));

                d.with(h6("Name mapping").withClass("card-subtitle mb-2 text-muted"));
                classMapping.forEach(entry ->
                        d.with(div(text(entry.getKey().getValue().name()), text(": "), text(entry.getValue())).withClass("alert-info font-monospace"))
                );

                if (!constructorsMapping.isEmpty()) {
                    d.with(h6("Constructors").withClass("card-subtitle mb-2 text-muted"));
                    constructorsMapping.forEach(entry ->
                            d.with(div(renderMethodParameters(entry.getParameters())).withClass("alert-info font-monospace"))
                    );
                }

                if (!fieldsMapping.isEmpty()) {
                    d.with(h6("Fields").withClass("card-subtitle mb-2 text-muted"));
                    fieldsMapping.forEach(entry ->
                            d.with(div(nmsLink(entry.getValue().getType()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-info font-monospace"))
                    );
                }

                if (!methodsMapping.isEmpty()) {
                    d.with(h6("Methods").withClass("card-subtitle mb-2 text-muted"));
                    methodsMapping.forEach(entry ->
                            d.with(div(nmsLink(entry.getValue().getReturnType()), renderMethodParameters(entry.getValue().getParameters()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-info font-monospace"))
                    );
                }
            } else {
                var diffClassMapping = classMapping
                        .stream()
                        .map(entry -> {
                            var old = definition.getMapping().entrySet().stream()
                                    .filter(e -> Arrays.asList(e.getKey().getKey().split(",")).contains(previousVersion) && e.getKey().getValue() == entry.getKey().getValue())
                                    .findFirst();

                            if (old.isPresent()) {
                                if (!old.get().getValue().equals(entry.getValue())) {
                                    return div(
                                            div("- " + entry.getKey().getValue() + ": " + old.get().getValue()).withClass("alert-danger font-monospace"),
                                            div("+ " + entry.getKey().getValue() + ": " + entry.getValue()).withClass("alert-success font-monospace")
                                    );
                                }
                            } else {
                                return div("+ " + entry.getKey().getValue() + ": " + entry.getValue()).withClass("alert-success font-monospace");
                            }

                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!diffClassMapping.isEmpty()) {
                    changes = true;
                    d.with(h6("Name mapping").withClass("card-subtitle mb-2 text-muted"));
                    diffClassMapping.forEach(d::with);
                }

                var diffConstructors = Stream
                        .concat(
                                constructorsMapping
                                        .stream()
                                        .map(entry -> {
                                            if (!entry.getSupportedVersions().contains(previousVersion)) {
                                                return div(text("+ "), renderMethodParameters(entry.getParameters())).withClass("alert-success font-monospace");
                                            }

                                            return null;
                                        }),
                                definition.getConstructors()
                                        .stream()
                                        .filter(e -> e.getSupportedVersions().contains(previousVersion) && !e.getSupportedVersions().contains(s))
                                        .map(entry -> div(text("- "), renderMethodParameters(entry.getParameters())).withClass("alert-danger font-monospace"))
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!diffConstructors.isEmpty()) {
                    changes = true;
                    d.with(h6("Constructors").withClass("card-subtitle mb-2 text-muted"));
                    diffConstructors.forEach(d::with);
                }

                var oldFields = definition.getFields()
                        .stream()
                        .map(e -> Map.entry(e.getMapping()
                                .entrySet()
                                .stream()
                                .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(previousVersion))
                                .collect(Collectors.toList()), e))
                        .filter(l -> !l.getKey().isEmpty())
                        .collect(Collectors.toList());

                var diffFieldsMapping = Stream
                        .concat(
                                fieldsMapping
                                        .stream()
                                        .map(entry -> {
                                            if (!oldFields.contains(entry)) {
                                                var similar = oldFields.stream()
                                                        .filter(e -> e.getValue() == entry.getValue())
                                                        .findFirst();

                                                if (similar.isPresent()) {
                                                    return div(
                                                            div(text("- "), nmsLink(similar.get().getValue().getType()), text(" " + similar.get().getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-danger font-monospace"),
                                                            div(text("+ "), nmsLink(entry.getValue().getType()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-success font-monospace")
                                                    );
                                                }

                                                return div(text("+ "), nmsLink(entry.getValue().getType()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-success font-monospace");
                                            }

                                            return null;
                                        }),
                                oldFields
                                        .stream()
                                        .filter(e -> fieldsMapping.stream().noneMatch(e2 -> e2.getValue() == e.getValue()))
                                        .map(entry ->
                                                div(text("- "), nmsLink(entry.getValue().getType()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-danger font-monospace")
                                        )
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!diffFieldsMapping.isEmpty()) {
                    changes = true;
                    d.with(h6("Fields").withClass("card-subtitle mb-2 text-muted"));
                    diffFieldsMapping.forEach(d::with);
                }

                var oldMethods = definition.getMethods()
                        .stream()
                        .map(e -> Map.entry(e.getMapping()
                                .entrySet()
                                .stream()
                                .filter(e1 -> Arrays.asList(e1.getKey().getKey().split(",")).contains(previousVersion))
                                .collect(Collectors.toList()), e))
                        .filter(l -> !l.getKey().isEmpty())
                        .collect(Collectors.toList());

                var diffMethodsMapping = Stream
                        .concat(
                                methodsMapping
                                        .stream()
                                        .map(entry -> {
                                            if (!oldMethods.contains(entry)) {
                                                var similar = oldMethods.stream()
                                                        .filter(e -> e.getValue() == entry.getValue())
                                                        .findFirst();

                                                if (similar.isPresent()) {
                                                    return div(
                                                            div(text("- "), nmsLink(similar.get().getValue().getReturnType()), renderMethodParameters(similar.get().getValue().getParameters()), text(" " + similar.get().getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-danger font-monospace"),
                                                            div(text("+ "), nmsLink(entry.getValue().getReturnType()), renderMethodParameters(entry.getValue().getParameters()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-success font-monospace")
                                                    );
                                                }

                                                return div(text("+ "), nmsLink(entry.getValue().getReturnType()), renderMethodParameters(entry.getValue().getParameters()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-success font-monospace");
                                            }

                                            return null;
                                        }),
                                oldMethods
                                        .stream()
                                        .filter(e -> methodsMapping.stream().noneMatch(e2 -> e2.getValue() == e.getValue()))
                                        .map(entry ->
                                                div(text("- "), nmsLink(entry.getValue().getReturnType()), renderMethodParameters(entry.getValue().getParameters()), text(" " + entry.getKey().stream().map(e -> e.getKey().getValue() + ": " + e.getValue()).collect(Collectors.joining(", ")))).withClass("alert-danger font-monospace")
                                        )
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!diffMethodsMapping.isEmpty()) {
                    changes = true;
                    d.with(h6("Methods").withClass("card-subtitle mb-2 text-muted"));
                    diffMethodsMapping.forEach(d::with);
                }
            }

            if (!changes) {
                d.with(i("No changes so far"));
            }

            mainContent.with(d);
        });

        return mainContent;
    }

    public DomContent renderMethodParameters(List<ClassDefinition.Link> parameters) {
        var span = span(text("("));
        var first = new AtomicBoolean();

        parameters.stream()
                .map(this::nmsLink)
                .forEach(r -> {
                    if (first.get()) {
                        span.with(text(", "));
                    } else {
                        first.set(true);
                    }
                    span.with(r);
                });

        span.with(text(")"));
        return span;
    }

    public DomContent nmsLink(ClassDefinition.Link link) {
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
            var finalType = type;
            var name = joinedMappingsClassLinks.entrySet().stream().filter(e -> e.getValue().equals(finalType)).map(Map.Entry::getKey).findFirst().orElse(finalType);
            return span(a(name.substring(name.lastIndexOf(".") + 1))
                    .withHref(type + ".html"), text(suffix.toString()));
        } else {
            return text(link.getType());
        }
    }
}
