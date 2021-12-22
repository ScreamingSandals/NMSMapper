package org.screamingsandals.nms.mapper.newweb.pages;

import lombok.Getter;
import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.screamingsandals.nms.mapper.newweb.components.VersionRecord;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends AbstractPage {
    @Getter
    private final List<VersionRecord> versions = new ArrayList<>();

    public MainPage() {
        super(
                "index",
                "index.html",
                "NMS mapping browser",
                List.of(
                        new NavbarLink("Main page", null, true),
                        new NavbarLink("Overview", null, false),
                        new NavbarLink("Package", null, false),
                        new NavbarLink("Class", null, false),
                        new NavbarLink("History", null, false)
                ),
                false
        );
    }

    @Override
    public void fillContext(Context context) {
        context.setVariable("versions", versions);
    }
}
