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

        var constructorBuilder = MethodSpec.methodBuilder("getConstructor" + id)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Constructor.class), ClassName.get("", "?")))
                .addStatement("return $T.$N($T.class, $L" + strBuilder + ")", args.toArray());
        var nullable = generator.getConfiguration().getNullableAnnotation();
        if (nullable != null) {
            constructorBuilder.addAnnotation(ClassName.get(nullable.substring(0, nullable.lastIndexOf('.')), nullable.substring(nullable.lastIndexOf('.') + 1)));
        }
        return constructorBuilder.build();
    }
}
