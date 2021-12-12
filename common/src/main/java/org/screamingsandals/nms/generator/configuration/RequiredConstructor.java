package org.screamingsandals.nms.generator.configuration;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Constructor;
import java.util.*;

@Data
public class RequiredConstructor implements Required, RequiredClassMember {
    @ToString.Exclude // TODO: fix recursion
    @EqualsAndHashCode.Exclude // TODO: fix recursion
    private final RequiredArgumentType[] params;

    @Override
    @ApiStatus.Internal
    public MethodSpec generateSymbolAccessor(Accessor accessor, AccessorClassGenerator generator) {
        // TODO: check existence of constructor

        var id = accessor.getConstructorCounter().getAndIncrement();
        var args = new ArrayList<Object>(List.of(generator.getAccessorUtils(), "getConstructor", ClassName.get(generator.getBasePackage(), accessor.getClassName()), id));

        var strBuilder = new StringBuilder();

        for (var param : params) {
            strBuilder.append(", ");
            param.generateClassGetter(generator, accessor, strBuilder, args);
        }

        return MethodSpec.methodBuilder("getConstructor" + id)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Constructor.class), ClassName.get("", "?")))
                .addStatement("return $T.$N($T.class, $L" + strBuilder + ")", args.toArray())
                .build();
    }
}
