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
import java.util.stream.Stream;

@Data
public class RequiredConstructor implements Required, RequiredClassMember {
    @ToString.Exclude // TODO: fix recursion
    @EqualsAndHashCode.Exclude // TODO: fix recursion
    private final RequiredArgumentType[] params;

    @Override
    @ApiStatus.Internal
    public MethodSpec generateSymbolAccessor(Accessor accessor, AccessorClassGenerator generator) {
        var hashToName = new HashMap<String, String>();
        var mappingParams = Arrays.stream(params)
                .map(s -> {
                    var s2 = s;
                    if (s instanceof RequiredArgumentArrayClass) {
                        s2 = ((RequiredArgumentArrayClass) s).getType();
                    }
                    String build;

                    if (s2 instanceof RequiredClass) {
                        var classAccessor = generator.getRequiredClassAccessorMap().get(s2);
                        build = "&" + classAccessor.getClassHash();
                        hashToName.put(build, classAccessor.getRequiredClass().getName());
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

        var foundInVersions = accessor.getMapping().node("constructors").childrenList()
                .stream()
                .filter(n -> {
                    try {
                        return Objects.equals(n.node("parameters").getList(String.class), mappingParams);
                    } catch (SerializationException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .flatMap(n -> {
                    try {
                        return n.node("versions").getList(String.class).stream();
                    } catch (SerializationException | NullPointerException e) {
                        e.printStackTrace();
                        return Stream.of();
                    }
                })
                .collect(Collectors.toList());

        var paramsString = mappingParams.stream().map(s -> {
                    var s2 = s.replace("[]", "");
                    if (hashToName.containsKey(s2)) {
                        var s3 = hashToName.get(s2);
                        s = s.replace(s2, s3);
                    }
                    return s;
                })
                .collect(Collectors.joining(", "));

        if (foundInVersions.isEmpty()) {
            throw new IllegalArgumentException("[" + accessor.getClassName() + "] Constructor (" + paramsString + ") does not exist in any version");
        }

        System.out.println("[" + accessor.getClassName() + "] Generating accessor method for constructor (" +  paramsString + ")");

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
        if (generator.getConfiguration().isAddInformationJavadoc()) {
            constructorBuilder.addJavadoc("This method returns the {@link Constructor} object of the requested NMS constructor.\n<p>\n" +
                    "Requested constructor: (" + paramsString.replace("$", "$$") + ")" +
                    "\n<p>\nPresent in versions: " + String.join(", ", foundInVersions).replace("$", "$$") +
                    "\n<p>\nThis method is safe to call: exception is handled and null is returned in case of failure.\n\n@return the resolved constructor object or null if either class does not exist or it does not have this constructor in the specific environment");
        }
        return constructorBuilder.build();
    }
}
