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
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class MainPage extends AbstractPage {
    private final List<Map.Entry<String, List<MappingType>>> versions;

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
                        List.of("Version", "Mappings"),
                        versions.stream()
                                .map(key -> {
                                    var span = span();

                                    for (var k : key.getValue()) {
                                        span.with(MiscUtils.mappingToBadge(k, "ms-2"));
                                    }

                                    return Map.of(
                                            "Version", (DomContent) a(key.getKey()).withHref(key.getKey() + "/"),
                                            "Mappings", span
                                    );
                                })
                                .collect(Collectors.toList())
                ).generate()
        );
    }
}
