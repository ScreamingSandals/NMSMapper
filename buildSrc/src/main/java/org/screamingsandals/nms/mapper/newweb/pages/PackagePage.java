package org.screamingsandals.nms.mapper.newweb.pages;

import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.thymeleaf.context.Context;

import java.util.List;

public class PackagePage extends AbstractPage {
    public PackagePage(String version, String packageName, String title) {
        super(
                "package",
                version + "/" + packageName + "/index.html",
                title,
                List.of(
                        new NavbarLink("Main page", "../".repeat(packageName.split("\\.").length + 1), false),
                        new NavbarLink("Overview", "../".repeat(packageName.split("\\.").length), false),
                        new NavbarLink("Package", null, true),
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
