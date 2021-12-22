package org.screamingsandals.nms.mapper.newweb.pages;

import lombok.Data;
import org.screamingsandals.nms.mapper.newweb.components.NavbarLink;
import org.thymeleaf.context.Context;

import java.util.List;

@Data
public abstract class AbstractPage {
    private final String templateName;
    private final String finalLocation;
    private final String pageTitle;
    private final List<NavbarLink> links;
    private final boolean searchAllowed;

    public abstract void fillContext(Context context);
}
