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

        return text("");
    }

    public static DomContent mappingToBadge(MappingType mappingType) {
        var capitalized = mappingType.name().substring(0, 1).toUpperCase() + mappingType.name().substring(1).toLowerCase();
        switch (mappingType) {
            case MOJANG:
                return span(capitalized).withClass("badge bg-success me-2");
            case SPIGOT:
                return span(capitalized).withClass("badge bg-warning me-2");
            case SEARGE:
                return span(capitalized).withClass("badge bg-danger me-2");
            case OBFUSCATED:
                return span(capitalized).withClass("badge bg-primary me-2");
            default:
                return span(capitalized).withClass("badge bg-secondary me-2");
        }
    }
}
