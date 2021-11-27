package org.screamingsandals.nms.mapper.web.parts;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.Builder;
import lombok.Data;
import org.screamingsandals.nms.mapper.web.WebsiteComponent;

import static j2html.TagCreator.*;

@Data
@Builder
public class NavbarPart implements WebsiteComponent {
    private final String mainPageUrl;
    private final String overviewUrl;
    private final String packageUrl;
    private final String classUrl;
    private final String historyUrl;
    private final CurrentPage currentPage;

    @Override
    public ContainerTag generate() {
        return
                nav(
                        div(
                                button(
                                    span().withClass("navbar-toggler-icon")
                                )
                                        .attr("aria-expanded", false)
                                        .attr("aria-label", "Toggle navigation")
                                        .withClass("navbar-toggler")
                                        .withType("button")
                                        .withData("bs-toggle", "collapse")
                                        .withData("bs-target", "#mainNavbarCollapse"),
                                div(
                                        ul(
                                                createLink("Main page", mainPageUrl, currentPage == CurrentPage.MAIN),
                                                createLink("Overview", overviewUrl, currentPage == CurrentPage.OVERVIEW),
                                                createLink("Package", packageUrl, currentPage == CurrentPage.PACKAGE),
                                                createLink("Class", classUrl, currentPage == CurrentPage.CLASS),
                                                createLink("History", historyUrl, currentPage == CurrentPage.HISTORY)
                                        ).withClass("navbar-nav me-auto"),
                                        iff(currentPage != CurrentPage.MAIN && currentPage != CurrentPage.HISTORY,
                                                div(
                                                        input().withType("search").withPlaceholder("Search").withClass("form-control").withId("searchBar")
                                                ).withClass("flex-d")
                                        )
                                ).withClass("collapse navbar-collapse")
                                        .withId("mainNavbarCollapse")
                        ).withClass("container")
                ).withClass("navbar navbar-light bg-light navbar-expand-lg");
    }

    private DomContent createLink(String title, String link, boolean active) {
        var a = a(title);

        if (active) {
            a.withClass("nav-link active");
        } else {
            a.withClass("nav-link");
        }

        if (link != null) {
            a.withHref(link);
        } else if (!active) {
            a.withClass("nav-link disabled");
        }

        return li(a);
    }

    public enum CurrentPage {
        MAIN,
        OVERVIEW,
        PACKAGE,
        CLASS,
        HISTORY;
    }
}
