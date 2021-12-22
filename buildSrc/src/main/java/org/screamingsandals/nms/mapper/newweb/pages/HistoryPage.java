package org.screamingsandals.nms.mapper.newweb.pages;

import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.thymeleaf.context.Context;

import java.util.List;

public class HistoryPage extends AbstractPage {
    public HistoryPage(String classHashName, String title) {
        super(
                "history",
                "history/" + classHashName + ".html",
                title,
                List.of(
                        new NavbarLink("Main page", "../", false),
                        new NavbarLink("Overview", null, false),
                        new NavbarLink("Package", null, false),
                        new NavbarLink("Class", null, false),
                        new NavbarLink("History", null, true)
                ),
                false
        );
    }

    @Override
    public void fillContext(Context context) {

    }
}
