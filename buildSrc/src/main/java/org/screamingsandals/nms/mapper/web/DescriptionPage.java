package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
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
                div(
                        div(
                                resolveImplementations()
                        ).withClass("col-sm-11"),
                        div(
                                div(
                                        button(
                                                new UnescapedText("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-code-slash\" viewBox=\"0 0 16 16\">" +
                                                        "  <path d=\"M10.478 1.647a.5.5 0 1 0-.956-.294l-4 13a.5.5 0 0 0 .956.294l4-13zM4.854 4.146a.5.5 0 0 1 0 .708L1.707 8l3.147 3.146a.5.5 0 0 1-.708.708l-3.5-3.5a.5.5 0 0 1 0-.708l3.5-3.5a.5.5 0 0 1 .708 0zm6.292 0a.5.5 0 0 0 0 .708L14.293 8l-3.147 3.146a.5.5 0 0 0 .708.708l3.5-3.5a.5.5 0 0 0 0-.708l-3.5-3.5a.5.5 0 0 0-.708 0z\"/>" +
                                                        "</svg>")
                                        )
                                                .withClass("btn btn-outline-secondary")
                                                .withData("bs-toggle", "dropdown")
                                                .withData("bs-auto-close", "false")
                                                .attr("aria-expanded", false),
                                        div(
                                                generateGradleCodeExample()
                                        ).withClass("dropdown-menu dropdown-menu-end")
                                ).withClass("dropdown")
                        ).withClass("col-sm-1")
                ).withClass("row"),
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

    private DomContent generateGradleCodeExample() {
        return div(
                h5("Get the class using our Gradle plugin").withClass("card-title"),
                div(
                        text("Add our repository to your settings.gradle:"),
                        button("Spoiler")
                                .withClass("btn btn-link dropdown-toggle d-block")
                                .withData("bs-toggle", "collapse")
                                .attr("aria-expanded", false)
                                .withData("bs-target", "#collapseSettingsGradle")
                                .withType("button"),
                        div(
                                pre(
                                        code(
                                                "pluginManagement {\n" +
                                                        "    repositories {\n" +
                                                        "        maven {\n" +
                                                        "            url = \"https://repo.screamingsandals.org/public/\"\n" +
                                                        "        }\n" +
                                                        "\n" +
                                                        "        gradlePluginPortal()\n" +
                                                        "    }\n" +
                                                        "}\n" +
                                                        "\n" +
                                                        "rootProject.name = 'YourProject'"
                                        ).withClass("language-groovy")
                                )
                        )
                                .withClass("collapse")
                                .withId("collapseSettingsGradle"),
                        text("Configure the plugin:"),
                        button("Spoiler")
                                .withClass("btn btn-link dropdown-toggle d-block")
                                .withData("bs-toggle", "collapse")
                                .attr("aria-expanded", false)
                                .withData("bs-target", "#collapseBuildGradle")
                                .withType("button"),
                        div(
                                pre(
                                        code(
                                                "plugins {\n" +
                                                        "\t// Other plugins\n" +
                                                        "\tid 'org.screamingsandals.nms-mapper'\n" +
                                                        "}\n\n" +
                                                        "\n/* ... Other stuff ... */\n\n" +
                                                        "sourceSets.main.java.srcDirs = ['src/generated/java', 'src/main/java']\n\n" +
                                                        "nmsGen {\n" +
                                                        "\tbasePackage = \"com.example.nms.accessors\"\n" +
                                                        "\tsourceSet = \"src/generated/java\"\n" +
                                                        "\tcleanOnRebuild = true\n\n" +
                                                        "\treqClass('" + getRightReqClass() + "')\n" +
                                                        "\t/* ... Other classes ... */\n" +
                                                        "}\n\n" +
                                                        "tasks.getByName('compileJava').dependsOn('generateNmsComponents')"
                                        ).withClass("language-groovy")
                                )
                        )
                                .withClass("collapse")
                                .withId("collapseBuildGradle"),
                        text("Then you can use it in your code:"),
                        button("Spoiler")
                                .withClass("btn btn-link dropdown-toggle d-block")
                                .withData("bs-toggle", "collapse")
                                .attr("aria-expanded", false)
                                .withData("bs-target", "#collapseJavaExample")
                                .withType("button"),
                        div(
                                pre(
                                        code(
                                                "import com.example.nms.accessors." + getAccessorName() + ";\n" +
                                                        "\n// ...\n\n" +
                                                        "Class<?> theClass = " + getAccessorName() + ".getType();"
                                        ).withClass("language-java")
                                )
                        )
                                .withClass("collapse")
                                .withId("collapseJavaExample"),
                        text("Learn more "), a("here").withHref("https://github.com/ScreamingSandals/NMSMapper/blob/master/README.md")
                )
        ).withClass("card-body").withStyle("width:min(500px, 95vw);");
    }

    private String getRightReqClass() {
        switch (defaultMapping) {
            case MOJANG:
                return keyName;
            case SPIGOT:
                return "spigot:" + keyName.substring(keyName.lastIndexOf(".") + 1);
            default:
                return defaultMapping.name().toLowerCase() + ":" + keyName;
        }
    }

    private String getAccessorName() {
        var clazz = getRightReqClass();
        var li = clazz.lastIndexOf(".");
        return (clazz.substring(li < 0 ? (clazz.startsWith("spigot:") ? 7 : 0) : (li + 1)) + "Accessor").replace("$", "_i_");
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
