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
