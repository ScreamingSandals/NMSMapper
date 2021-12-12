package org.screamingsandals.nms.generator.configuration;

import lombok.Data;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

import java.lang.reflect.Array;
import java.util.List;

@Data
public class RequiredArgumentArrayClass implements RequiredArgumentType {
    private final RequiredArgumentType type;
    @Range(from = 1, to = Integer.MAX_VALUE)
    private final int dimensions;

    @Override
    @ApiStatus.Internal
    public void generateClassGetter(AccessorClassGenerator generator, Accessor accessor, StringBuilder expression, List<Object> params) {
        expression.append("$T.newInstance(");
        params.add(Array.class);
        type.generateClassGetter(generator, accessor, expression, params);
        expression.append(", 0".repeat(dimensions));
        expression.append(").getClass()");
    }
}
