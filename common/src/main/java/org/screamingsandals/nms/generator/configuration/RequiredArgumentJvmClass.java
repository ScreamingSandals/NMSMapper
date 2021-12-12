package org.screamingsandals.nms.generator.configuration;

import lombok.Data;
import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

import java.util.List;

@Data
public class RequiredArgumentJvmClass implements RequiredArgumentType {
    private final Class<?> theClass;

    @Override
    @ApiStatus.Internal
    public void generateClassGetter(AccessorClassGenerator generator, Accessor accessor, StringBuilder expression, List<Object> params) {
        expression.append("$T.class");
        params.add(theClass);
    }
}
