package org.screamingsandals.nms.generator.configuration;

import org.jetbrains.annotations.Range;

public interface RequiredArgumentType {
    default RequiredArgumentArrayClass array() {
        return new RequiredArgumentArrayClass(this, 1);
    }

    default RequiredArgumentArrayClass array(@Range(from = 1, to = Integer.MAX_VALUE) int dimensions) {
        return new RequiredArgumentArrayClass(this, dimensions);
    }
}
