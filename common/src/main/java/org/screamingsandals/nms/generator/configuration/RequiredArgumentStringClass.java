package org.screamingsandals.nms.generator.configuration;

import lombok.Data;
import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

import java.util.List;

@Data
public class RequiredArgumentStringClass implements RequiredArgumentType {
    private final String className;

    @Override
    @ApiStatus.Internal
    public void generateClassGetter(AccessorClassGenerator generator, Accessor accessor, StringBuilder expression, List<Object> params) {
        switch (className) {
            case "int":
            case "byte":
            case "short":
            case "long":
            case "char":
            case "float":
            case "double":
            case "boolean":
                expression.append(className).append(".class");
                break;
            default:
                expression.append("$T.getOrCatch($S)");
                params.add(generator.getAccessorUtils());
                params.add(className);
        }
    }
}
