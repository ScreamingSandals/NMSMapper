package org.screamingsandals.nms.generator.standalone;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.screamingsandals.nms.generator.AccessorClassGenerator;
import org.screamingsandals.nms.generator.configuration.NMSMapperConfiguration;
import org.screamingsandals.nms.generator.configuration.NewNMSMapperConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Arrays.asList;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting NMSMapper...");

        OptionParser parser = new OptionParser() {
            {
                acceptsAll(asList("?", "help"), "Show the help");
                acceptsAll(asList("b", "build-file"), "Sets the build file")
                        .withRequiredArg()
                        .ofType(File.class);
            }
        };

        OptionSet options = null;

        try {
            options = parser.parse(args);
        } catch (OptionException ex) {
            ex.printStackTrace();
        }
        if (options != null && options.has("b")) {
            File buildFile = ((File) options.valueOf("b")).getAbsoluteFile();
            if (!buildFile.exists() || !buildFile.isFile()) {
                System.out.println("ERR: The build file does not exist or is not a file!");
                return;
            }

            System.out.println("Source file: " + buildFile.getAbsolutePath());
            System.out.println("Working directory: " + buildFile.getParentFile().getAbsolutePath());

            //NewNMSMapperConfiguration configuration = new NewNMSMapperConfiguration();
            NMSMapperConfiguration configuration = new NMSMapperConfiguration();
            Binding binding = new Binding();
            binding.setProperty("nmsGen", configuration);

            try {
                GroovyScriptEngine engine = new GroovyScriptEngine(new URL[] {buildFile.toURI().toURL()});
                engine.run(buildFile.getName(), binding);
            } catch (MalformedURLException | ScriptException | ResourceException e) {
                e.printStackTrace();
                System.out.println("ERR: Can't load the build file!");
            }

            try {
                AccessorClassGenerator.run(configuration, buildFile.getParentFile());
                System.out.println("Generation finished!");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("ERR: Generation failed!");
            }
        } else {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
