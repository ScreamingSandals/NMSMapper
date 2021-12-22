package org.screamingsandals.nms.mapper.newweb;

import lombok.Data;
import org.screamingsandals.nms.mapper.newweb.pages.AbstractPage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Data
public class WebGenerator {
    private final TemplateEngine templateEngine;
    private final FileTemplateResolver templateResolver;
    private final File finalFolder;

    public WebGenerator(File templatesFolder, File finalFolder) {
        this.finalFolder = finalFolder;
        templateEngine = new TemplateEngine();
        templateResolver = new FileTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix(templatesFolder.getAbsolutePath() + File.separator);
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(false);
        templateEngine.setTemplateResolver(templateResolver);
    }

    public void generate(AbstractPage page) throws IOException {
        var context = new Context();
        context.setVariable("pageTitle", page.getPageTitle());
        context.setVariable("searchAllowed", page.isSearchAllowed());
        context.setVariable("navLinks", page.getLinks());
        page.fillContext(context);

        var stringWriter = new FileWriter(new File(finalFolder, page.getFinalLocation()));
        templateEngine.process(page.getTemplateName(), context, stringWriter);
        stringWriter.close();
    }
}
