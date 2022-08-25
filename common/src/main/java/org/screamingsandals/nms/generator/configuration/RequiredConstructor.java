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
import org.screamingsandals.nms.generator.utils.MiscUtils;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class RequiredConstructor implements Required, RequiredClassMember {
    @ToString.Exclude // TODO: fix recursion
    @EqualsAndHashCode.Exclude // TODO: fix recursion
    private final RequiredArgumentType[] params;

    @Override
    @ApiStatus.Internal
    public MethodSpec generateSymbolAccessor(Accessor accessor, AccessorClassGenerator generator) {
        var mappingParams = Arrays.stream(params)
                .map(s -> {
                    var s2 = s;
                    if (s instanceof RequiredArgumentArrayClass) {
                        s2 = ((RequiredArgumentArrayClass) s).getType();
                    }
                    String build;

                    if (s2 instanceof RequiredClass) {
                        build = "&" + generator.getRequiredClassAccessorMap().get(s2).getClassHash();
                    } else if (s2 instanceof RequiredArgumentStringClass) {
                        build = ((RequiredArgumentStringClass) s2).getClassName();
                    } else if (s2 instanceof RequiredArgumentJvmClass) {
                        build = MiscUtils.convertWeirdResultOfClassName(((RequiredArgumentJvmClass) s2).getTheClass().getName());
                    } else {
                        throw new UnsupportedOperationException("Don't know what " + s.getClass() + " is!");
                    }
                    if (s instanceof RequiredArgumentArrayClass) {
                        build += "[]".repeat(((RequiredArgumentArrayClass) s).getDimensions());
                    }
                    return build;
                })
                .collect(Collectors.toList());

        if (
                accessor.getMapping().node("constructors").childrenList()
                        .stream()
                        .noneMatch(n -> {
                            try {
                                return Objects.equals(n.node("parameters").getList(String.class), mappingParams);
                            } catch (SerializationException e) {
                                e.printStackTrace();
                                return false;
                            }
                        })
        ) {
            throw new IllegalArgumentException("Constructor (" + String.join(", ", mappingParams) + ") does not exist in any version");
        }

        System.out.println("Generating accessor method for constructor (" + String.join(", ", mappingParams) + ")");

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
