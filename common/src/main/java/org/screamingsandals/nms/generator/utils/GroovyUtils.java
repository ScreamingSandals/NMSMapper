package org.screamingsandals.nms.generator.utils;

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
}
