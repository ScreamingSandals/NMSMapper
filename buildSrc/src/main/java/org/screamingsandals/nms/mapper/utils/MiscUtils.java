package org.screamingsandals.nms.mapper.utils;

import j2html.tags.DomContent;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static j2html.TagCreator.*;

public class MiscUtils {
    public static String getModifierString(int modifier) {
        var modifiers = new ArrayList<String>();

        if (Modifier.isPublic(modifier)) {
            modifiers.add("public");
        }

        if (Modifier.isPrivate(modifier)) {
            modifiers.add("private");
        }

        if (Modifier.isProtected(modifier)) {
            modifiers.add("protected");
        }

        if (Modifier.isStatic(modifier)) {
            modifiers.add("static");
        }

        if (Modifier.isAbstract(modifier) && !Modifier.isInterface(modifier)) { // we all know interfaces are abstract
            modifiers.add("abstract");
        }

        if (Modifier.isFinal(modifier)) {
            modifiers.add("final");
        }

        if (Modifier.isNative(modifier)) {
            modifiers.add("native");
        }

        if (Modifier.isStrict(modifier)) {
            modifiers.add("strict");
        }

        if (Modifier.isSynchronized(modifier)) {
            modifiers.add("synchronized");
        }

        if (Modifier.isTransient(modifier)) {
            modifiers.add("transient");
        }

        if (Modifier.isVolatile(modifier)) {
            modifiers.add("volatile");
        }

        if (!modifiers.isEmpty()) {
            modifiers.add("");
        }

        return String.join(" ", modifiers);
    }

    public static DomContent descriptions(MappingType defaultMapping) {
        if (defaultMapping != MappingType.MOJANG) {
            return div(
                    "This minecraft version doesn't have published official Mojang mappings. Other mappings are used as default instead: " + defaultMapping
            ).withClass("alert alert-danger");
        }

        return null;
    }

    public static String capitalizeFirst(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static DomContent mappingToBadge(MappingType mappingType) {
        return span(capitalizeFirst(mappingType.name())).withClass("badge me-2 bg-" + chooseBootstrapColor(mappingType));
    }

    public static DomContent mappingToBadge(MappingType mappingType, String additionalClass) {
        return span(capitalizeFirst(mappingType.name())).withClass("badge me-2 bg-" + chooseBootstrapColor(mappingType) + " " + additionalClass);
    }

    public static String chooseBootstrapColor(MappingType mappingType) {
        return mappingType.getBootstrapColor();
    }

    public static String classNameToUrl(String name) {
        return (name.split("\\.").length == 1 ? "default-pkg/" : "") + name.replace(".", "/").replace("${V}", "VVV") + ".html";
    }
}
