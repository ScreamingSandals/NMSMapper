package org.screamingsandals.nms.mapper.newweb.pages;

import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.utils.MiscUtils;
import org.thymeleaf.context.Context;

import java.util.List;

public class DescriptionPage extends AbstractPage {
    public DescriptionPage(String className, ClassDefinition definition, String title) {
        super(
                "description",
                MiscUtils.classNameToUrl(className),
                title,
                List.of(
                        new NavbarLink("Main page", "../".repeat(className.split("\\.").length + (className.split("\\.").length == 1 ? 1 : 0)), true),
                        new NavbarLink("Overview", "../".repeat(className.split("\\.").length + (className.split("\\.").length == 1 ? 1 : 0) - 1), false),
                        new NavbarLink("Package", "index.html", false),
                        new NavbarLink("Class", null, true),
                        new NavbarLink("History", "../".repeat(className.split("\\.").length + (className.split("\\.").length == 1 ? 1 : 0)) + "history/" + definition.getJoinedKey() + ".html", false)
                ),
                true
        );
    }

    @Override
    public void fillContext(Context context) {

    }
}
