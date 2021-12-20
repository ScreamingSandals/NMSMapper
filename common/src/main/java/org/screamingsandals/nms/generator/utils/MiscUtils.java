package org.screamingsandals.nms.generator.utils;

public class MiscUtils {
    public static String convertWeirdResultOfClassName(String type) {
        switch (type) {
            case "B":
                return "byte";
            case "C":
                return "char";
            case "D":
                return "double";
            case "F":
                return "float";
            case "I":
                return "int";
            case "J":
                return "long";
            case "S":
                return "short";
            case "Z":
                return "boolean";
            case "V":
                return "void";
            default:
                if (type.startsWith("[")) {
                    return convertWeirdResultOfClassName(type.substring(type.startsWith("[L") ? 2 : 1)) + "[]";
                } else if (type.endsWith(";")) {
                    return type.substring(0, type.length() - 1); // Class.forName is also weird shit
                } else {
                    return type; // Class.forName is also weird shit
                }

        }
    }
}
