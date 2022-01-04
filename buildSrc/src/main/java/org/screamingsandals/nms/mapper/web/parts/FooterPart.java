/*
 * Copyright 2022 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.screamingsandals.nms.mapper.web.parts;

import j2html.tags.ContainerTag;
import j2html.tags.UnescapedText;
import org.screamingsandals.nms.mapper.web.WebsiteComponent;

import java.util.Calendar;

import static j2html.TagCreator.*;

public class FooterPart implements WebsiteComponent {
    @Override
    public ContainerTag generate() {
        return footer(
                div(
                        span("Copyright © " + Calendar.getInstance().get(Calendar.YEAR) + " ScreamingSandals").withClass("text-muted")
                ).withClass("col-md-4 d-flex align-items-center"),

                ul(
                        footerLink("https://screamingsandals.org", "ScreamingSandals.org",
                                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" fill=\"currentColor\" class=\"bi bi-globe\" viewBox=\"0 0 16 16\">" +
                                        "<path d=\"M0 8a8 8 0 1 1 16 0A8 8 0 0 1 0 8zm7.5-6.923c-.67.204-1.335.82-1.887 1.855A7.97 7.97 0 0 0 5.145 4H7.5V1.077zM4.09 4a9.267 9.267 0 0 1 .64-1.539 6.7 6.7 0 0 1 .597-.933A7.025 7.025 0 0 0 2.255 4H4.09zm-.582 3.5c.03-.877.138-1.718.312-2.5H1.674a6.958 6.958 0 0 0-.656 2.5h2.49zM4.847 5a12.5 12.5 0 0 0-.338 2.5H7.5V5H4.847zM8.5 5v2.5h2.99a12.495 12.495 0 0 0-.337-2.5H8.5zM4.51 8.5a12.5 12.5 0 0 0 .337 2.5H7.5V8.5H4.51zm3.99 0V11h2.653c.187-.765.306-1.608.338-2.5H8.5zM5.145 12c.138.386.295.744.468 1.068.552 1.035 1.218 1.65 1.887 1.855V12H5.145zm.182 2.472a6.696 6.696 0 0 1-.597-.933A9.268 9.268 0 0 1 4.09 12H2.255a7.024 7.024 0 0 0 3.072 2.472zM3.82 11a13.652 13.652 0 0 1-.312-2.5h-2.49c.062.89.291 1.733.656 2.5H3.82zm6.853 3.472A7.024 7.024 0 0 0 13.745 12H11.91a9.27 9.27 0 0 1-.64 1.539 6.688 6.688 0 0 1-.597.933zM8.5 12v2.923c.67-.204 1.335-.82 1.887-1.855.173-.324.33-.682.468-1.068H8.5zm3.68-1h2.146c.365-.767.594-1.61.656-2.5h-2.49a13.65 13.65 0 0 1-.312 2.5zm2.802-3.5a6.959 6.959 0 0 0-.656-2.5H12.18c.174.782.282 1.623.312 2.5h2.49zM11.27 2.461c.247.464.462.98.64 1.539h1.835a7.024 7.024 0 0 0-3.072-2.472c.218.284.418.598.597.933zM10.855 4a7.966 7.966 0 0 0-.468-1.068C9.835 1.897 9.17 1.282 8.5 1.077V4h2.355z\"></path>" +
                                        "</svg>"
                        ),
                        footerLink("https://github.com/ScreamingSandals/NMSMapper", "NMSMapper on GitHub",
                                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" fill=\"currentColor\" class=\"bi bi-github\" viewBox=\"0 0 16 16\">" +
                                        "<path d=\"M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.012 8.012 0 0 0 16 8c0-4.42-3.58-8-8-8z\"></path>" +
                                        "</svg>"
                        ),
                        footerLink("https://discord.gg/4xB54Ts", "Join us on Discord",
                                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" fill=\"currentColor\" class=\"bi bi-discord\" viewBox=\"0 0 16 16\">" +
                                        "<path d=\"M13.545 2.907a13.227 13.227 0 0 0-3.257-1.011.05.05 0 0 0-.052.025c-.141.25-.297.577-.406.833a12.19 12.19 0 0 0-3.658 0 8.258 8.258 0 0 0-.412-.833.051.051 0 0 0-.052-.025c-1.125.194-2.22.534-3.257 1.011a.041.041 0 0 0-.021.018C.356 6.024-.213 9.047.066 12.032c.001.014.01.028.021.037a13.276 13.276 0 0 0 3.995 2.02.05.05 0 0 0 .056-.019c.308-.42.582-.863.818-1.329a.05.05 0 0 0-.01-.059.051.051 0 0 0-.018-.011 8.875 8.875 0 0 1-1.248-.595.05.05 0 0 1-.02-.066.051.051 0 0 1 .015-.019c.084-.063.168-.129.248-.195a.05.05 0 0 1 .051-.007c2.619 1.196 5.454 1.196 8.041 0a.052.052 0 0 1 .053.007c.08.066.164.132.248.195a.051.051 0 0 1-.004.085 8.254 8.254 0 0 1-1.249.594.05.05 0 0 0-.03.03.052.052 0 0 0 .003.041c.24.465.515.909.817 1.329a.05.05 0 0 0 .056.019 13.235 13.235 0 0 0 4.001-2.02.049.049 0 0 0 .021-.037c.334-3.451-.559-6.449-2.366-9.106a.034.034 0 0 0-.02-.019Zm-8.198 7.307c-.789 0-1.438-.724-1.438-1.612 0-.889.637-1.613 1.438-1.613.807 0 1.45.73 1.438 1.613 0 .888-.637 1.612-1.438 1.612Zm5.316 0c-.788 0-1.438-.724-1.438-1.612 0-.889.637-1.613 1.438-1.613.807 0 1.451.73 1.438 1.613 0 .888-.631 1.612-1.438 1.612Z\"></path>" +
                                        "</svg>"
                        )
                ).withClass("nav col-md-4 justify-content-end list-unstyled d-flex"),
                script("let darkMode=localStorage.getItem(\"nms-dark-mode\");function toggleDarkMode(){let e=localStorage.getItem(\"nms-dark-mode\");null===e&&(e=window.matchMedia&&window.matchMedia(\"(prefers-color-scheme: dark)\").matches),\"false\"===e?(window.matchMedia&&window.matchMedia(\"(prefers-color-scheme: dark)\").matches||$(\"#darkModeCss\").removeAttr(\"media\"),$(\"#darkModeCss\")[0].disabled=!1,$(\"nav\").removeClass(\"bg-light\"),localStorage.setItem(\"nms-dark-mode\",!0)):($(\"#darkModeCss\")[0].disabled=!0,$(\"nav\").addClass(\"bg-light\"),localStorage.setItem(\"nms-dark-mode\",!1))}console.log(darkMode),null!==darkMode&&(\"true\"===darkMode?(window.matchMedia&&window.matchMedia(\"(prefers-color-scheme: dark)\").matches||$(\"#darkModeCss\").removeAttr(\"media\"),$(\"#darkModeCss\")[0].disabled=!1,$(\"nav\").removeClass(\"bg-light\")):($(\"#darkModeCss\")[0].disabled=!0,$(\"nav\").addClass(\"bg-light\")));")
        ).withClass("d-flex flex-wrap justify-content-between align-items-center py-3 my-4 border-top");
    }

    private ContainerTag footerLink(String link, String title, String svg) {
        return li(
                a()
                        .with(new UnescapedText(svg))
                        .withTitle(title)
                        .withClass("text-muted")
                        .withHref(link)
        ).withClass("ms-3");
    }
}
