package org.screamingsandals.nms.mapper.newweb.pages;

import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.thymeleaf.context.Context;

import java.util.List;

public class OverviewPage extends AbstractPage {
    public OverviewPage(String version, String title) {
        super(
                "overview",
                version + "/index.html",
                title,
                List.of(
                        new NavbarLink("Main page", "../", false),
                        new NavbarLink("Overview", null, true),
                        new NavbarLink("Package", null, false),
                        new NavbarLink("Class", null, false),
                        new NavbarLink("History", null, false)
                ),
                true
        );
    }

    @Override
    public void fillContext(Context context) {

    }
}
