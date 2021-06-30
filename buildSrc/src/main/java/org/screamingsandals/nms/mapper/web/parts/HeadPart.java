package org.screamingsandals.nms.mapper.web.parts;

import j2html.tags.ContainerTag;
import lombok.Data;
import org.screamingsandals.nms.mapper.web.WebsiteComponent;

import static j2html.TagCreator.*;

@Data
public class HeadPart implements WebsiteComponent {
    private final String title;
    private final String basePath;

    @Override
    public ContainerTag generate() {
        return head(
                title(title),
                link().withRel("stylesheet")
                        .withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css")
                        .attr("integrity", "sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC")
                        .attr("crossorigin", "anonymous"),
                link().withRel("stylesheet")
                        .withHref("https://code.jquery.com/ui/1.12.1/themes/smoothness/jquery-ui.css"),
                script()
                        .withSrc("https://code.jquery.com/jquery-3.6.0.min.js")
                        .attr("integrity", "sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=")
                        .attr("crossorigin", "anonymous"),
                script()
                        .withSrc("https://code.jquery.com/ui/1.12.1/jquery-ui.js"),
                script()
                        .withSrc("https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js")
                        .attr("integrity", "sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM")
                        .attr("crossorigin", "anonymous"),
                iff(basePath != null,
                        link().withRel("stylesheet")
                                .withHref(basePath + "static/css/search.css")
                ),
                iff(basePath != null,
                        script()
                                .withSrc(basePath + "static/js/search.js")
                )
        );
    }
}
