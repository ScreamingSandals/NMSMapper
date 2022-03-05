package org.screamingsandals.nms.generator.utils;

import org.gradle.api.HasImplicitReceiver;

@HasImplicitReceiver
public interface Action<T> {
    void execute(T t);
}
