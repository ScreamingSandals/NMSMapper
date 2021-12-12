package org.screamingsandals.nms.generator.configuration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

import java.util.List;

public interface RequiredArgumentType {
    default RequiredArgumentArrayClass array() {
        if (this instanceof RequiredArgumentArrayClass) {
            throw new UnsupportedOperationException("Can't create an array of another array!");
        }
        return new RequiredArgumentArrayClass(this, 1);
    }

    default RequiredArgumentArrayClass array(@Range(from = 1, to = Integer.MAX_VALUE) int dimensions) {
        if (this instanceof RequiredArgumentArrayClass) {
            throw new UnsupportedOperationException("Can't create an array of another array!");
        }
        return new RequiredArgumentArrayClass(this, dimensions);
    }

    @ApiStatus.Internal
    void generateClassGetter(AccessorClassGenerator generator, Accessor accessor, StringBuilder expression, List<Object> params);
}
