package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.screamingsandals.nms.mapper.web.parts.CompactTablePart;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static j2html.TagCreator.a;

@EqualsAndHashCode(callSuper = true)
@Data
public class MainPage extends AbstractPage {
    private final List<String> versions;

    @Override
    protected void configure() {
        title = "NMS Mapping browser";
        navbarPart = NavbarPart.builder()
                .currentPage(NavbarPart.CurrentPage.MAIN)
                .build();
    }

    @Override
    protected void constructContent(ContainerTag div) {
        div.with(
                new CompactTablePart(
                        "Versions",
                        List.of("Version"),
                        versions.stream()
                                .map(key -> Map.of("Version", (DomContent) a(key).withHref(key + "/")))
                                .collect(Collectors.toList())
                ).generate()
        );
    }
}
