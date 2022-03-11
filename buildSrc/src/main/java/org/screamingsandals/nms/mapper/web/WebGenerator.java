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

package org.screamingsandals.nms.mapper.web;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import lombok.Data;
import org.screamingsandals.nms.mapper.web.pages.AbstractPage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Data
public class WebGenerator {
    public static final String DOC_LINK = "https://docs.screamingsandals.org/ScreamingLib/2.0.1-SNAPSHOT/nmsmapper/";

    private final TemplateEngine templateEngine;
    private final FileTemplateResolver templateResolver;
    private final File finalFolder;
    private final HtmlCompressor compressor;

    private final List<AbstractPage> pageBuffer = new ArrayList<>();

    public WebGenerator(File templatesFolder, File finalFolder) {
        this.finalFolder = finalFolder;
        templateEngine = new TemplateEngine();
        templateResolver = new FileTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix(templatesFolder.getAbsolutePath() + File.separator);
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);
        templateEngine.setTemplateResolver(templateResolver);

        compressor = new HtmlCompressor();
    }

    public void putPage(AbstractPage page) {
        pageBuffer.add(page);
    }

    public void generate(AbstractPage page) throws IOException {
        var context = new Context();
        context.setVariable("pageTitle", page.getPageTitle());
        context.setVariable("searchAllowed", page.isSearchAllowed());
        context.setVariable("navLinks", page.getLinks());
        context.setVariable("basePath", "../".repeat(page.getFinalLocation().split("/").length - 1));
        page.fillContext(context);

        var file = new File(finalFolder, page.getFinalLocation());
        file.getParentFile().mkdirs();
        var stringWriter = new StringWriter();
        templateEngine.process(page.getTemplateName(), context, stringWriter);
        var fileWriter = new FileWriter(file);
        fileWriter.write(compressor.compress(stringWriter.toString()));
        fileWriter.close();
    }

    public void generate() throws IOException {
        for (var page : pageBuffer) {
            generate(page);
        }
        pageBuffer.clear();
    }
}
