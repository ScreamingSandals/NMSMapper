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

import groovy.lang.Closure;

import java.lang.reflect.Proxy;

public class GroovyUtils {
    public static void hackClosure(Object closure, Object delegate) {
        try {
            if (Proxy.isProxyClass(closure.getClass())) {
                var proxy = Proxy.getInvocationHandler(closure);
                var realClosure = proxy.getClass().getMethod("getDelegate").invoke(proxy);
                realClosure.getClass().getMethod("setDelegate", Object.class).invoke(realClosure, delegate);
                realClosure.getClass().getMethod("setResolveStrategy", int.class).invoke(realClosure, 1);
            } else {
                closure.getClass().getMethod("setDelegate", Object.class).invoke(closure, delegate);
                closure.getClass().getMethod("setResolveStrategy", int.class).invoke(closure, 1);
            }

        } catch (Throwable ignored) {
        }
    }

    public static <T> Action<T> convertToAction(Closure<T> closure) {
        return t -> {
            closure.setDelegate(t);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            closure.call(t);
        };
    }
}
