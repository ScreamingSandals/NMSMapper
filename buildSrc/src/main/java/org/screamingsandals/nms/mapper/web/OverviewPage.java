package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.MiscUtils;
import org.screamingsandals.nms.mapper.web.parts.CompactTablePart;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class OverviewPage extends AbstractPage {
    private final String docsName;
    private final Set<String> packages;
    private final MappingType defaultMapping;
    private final Map<MappingType, String> licenses;

    @Override
    protected void configure() {
        title = docsName;
        basePath = "../";
        navbarPart = NavbarPart.builder()
                .mainPageUrl("../")
                .currentPage(NavbarPart.CurrentPage.OVERVIEW)
                .build();
    }

    @Override
    protected void constructContent(ContainerTag div) {
        div.with(MiscUtils.descriptions(defaultMapping));

        licenses.forEach((mappingType, s) ->
                div.with(div(
                        div(MiscUtils.capitalizeFirst(mappingType.name()) + " license").withClass("card-header"),
                        div(s.replace("\n\n", "\n")).withClass("card-body").withStyle("white-space: pre-wrap;")
                ).withClass("card text-white mb-1 bg-" + MiscUtils.chooseBootstrapColor(mappingType)))
        );

        div.with(
                new CompactTablePart(
                        "Packages",
                        List.of("Package"),
                        packages.stream()
                                .sorted()
                                .map(key -> {
                                            var pathKey = key
                                                    .replace(".", "/")
                                                    .replace("${V}", "VVV");

                                            return Map.of(
                                                    "Package", (DomContent) a(key)
                                                            .withHref(pathKey + "/index.html")
                                            );
                                        }
                                )
                                .collect(Collectors.toList())
                ).generate()
        );
    }
}
