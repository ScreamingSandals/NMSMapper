package org.screamingsandals.nms.mapper.web.parts;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
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
                                        button(
                                                new UnescapedText("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-moon\" viewBox=\"0 0 16 16\"><path d=\"M6 .278a.768.768 0 0 1 .08.858 7.208 7.208 0 0 0-.878 3.46c0 4.021 3.278 7.277 7.318 7.277.527 0 1.04-.055 1.533-.16a.787.787 0 0 1 .81.316.733.733 0 0 1-.031.893A8.349 8.349 0 0 1 8.344 16C3.734 16 0 12.286 0 7.71 0 4.266 2.114 1.312 5.124.06A.752.752 0 0 1 6 .278zM4.858 1.311A7.269 7.269 0 0 0 1.025 7.71c0 4.02 3.279 7.276 7.319 7.276a7.316 7.316 0 0 0 5.205-2.162c-.337.042-.68.063-1.029.063-4.61 0-8.343-3.714-8.343-8.29 0-1.167.242-2.278.681-3.286z\"/></svg>")
                                        ).withClass("btn btn-outline-dark m-1")
                                                        .attr("onClick", "toggleDarkMode();"),
                                        iff(currentPage != CurrentPage.MAIN && currentPage != CurrentPage.HISTORY,
                                                div(
                                                        div(
                                                                select().withClass("form-select").withId("searchBarMappingSelect"),
                                                                input().withType("search").withPlaceholder("Search").withClass("form-control w-50").withId("searchBar")
                                                        ).withClass("input-group")
                                                ).withClass("flex-d")
                                        )
                                ).withClass("collapse navbar-collapse")
                                        .withId("mainNavbarCollapse")
                        ).withClass("container")
                ).withClass("navbar navbar-light navbar-expand-lg");
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
        HISTORY
    }
}
