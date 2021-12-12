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

        /*
                    switch (fistClassName) {
                        case "I[]":
                            fistClassName = "int[]";
                            break;
                        case "Z[]":
                            fistClassName = "boolean[]";
                            break;
                        case "J[]":
                            fistClassName = "long[]";
                            break;
                        case "B[]":
                            fistClassName = "byte[]";
                            break;
                        case "D[]":
                            fistClassName = "double[]";
                            break;
                        case "F[]":
                            fistClassName = "float[]";
                            break;
                        case "C[]":
                            fistClassName = "char[]";
                            break;
                        case "S[]":
                            fistClassName = "short[]";
                            break;
                    }*/
    }
}
