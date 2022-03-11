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

package org.screamingsandals.nms.mapper.utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

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

    public static String classNameToUrl(String name) {
        return (name.split("\\.").length == 1 ? "default-pkg/" : "") + name.replace(".", "/").replace("${V}", "VVV") + ".html";
    }
}
