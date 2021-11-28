package org.screamingsandals.nms.mapper.web;

import j2html.tags.ContainerTag;
import org.screamingsandals.nms.mapper.web.parts.FooterPart;
import org.screamingsandals.nms.mapper.web.parts.HeadPart;
import org.screamingsandals.nms.mapper.web.parts.NavbarPart;

import static j2html.TagCreator.*;

public abstract class AbstractPage implements WebsiteComponent {
    protected String title;
    protected String basePath = null;
    protected NavbarPart navbarPart;

    @Override
    public final ContainerTag generate() {
        configure();

        var div = div(
                h1(title).withClass("my-3")
        ).withClass("container");

        constructContent(div);

        div.with(new FooterPart().generate());

        return html(
                new HeadPart(title, basePath).generate(),
                body(
                        navbarPart.generate(),
                        div
                )
        );
    }

    protected abstract void configure();

    protected abstract void constructContent(ContainerTag div);
}
