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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;
import org.screamingsandals.nms.generator.utils.MiscUtils;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredMethod extends RequiredSymbol implements RequiredClassMember {
    @Getter
    @ToString.Exclude // TODO: fix recursion
    @EqualsAndHashCode.Exclude // TODO: fix recursion
    private final RequiredArgumentType[] params;

    public RequiredMethod(String mapping, String name, @Nullable String forcedVersion, RequiredArgumentType[] params) {
        super(mapping, name, forcedVersion);
        this.params = params;
    }

    @Override
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

        var optional = accessor.getMapping().node("methods")
                .childrenList()
                .stream()
                .filter(n -> {
                            try {
                                return n.node(getMapping().toUpperCase())
                                        .childrenMap()
                                        .entrySet()
                                        .stream()
                                        .anyMatch(n1 -> n1.getValue().getString("").equals(getName())
                                                && (getForcedVersion() == null || Arrays.asList(n1.getKey().toString().split(",")).contains(getForcedVersion()))
                                                        && (
                                                        generator.getConfiguration().getMinMinecraftVersion() == null
                                                                || generator.getConfiguration().getMinMinecraftVersion().isEmpty()
                                                                || Arrays.stream(n1.getKey().toString().split(","))
                                                                .anyMatch(s -> new ComparableVersion(s).compareTo(new ComparableVersion(generator.getConfiguration().getMinMinecraftVersion())) >= 0)
                                                )
                                                        && (
                                                        generator.getConfiguration().getMaxMinecraftVersion() == null
                                                                || generator.getConfiguration().getMaxMinecraftVersion().isEmpty()
                                                                || Arrays.stream(n1.getKey().toString().split(","))
                                                                .anyMatch(s -> new ComparableVersion(s).compareTo(new ComparableVersion(generator.getConfiguration().getMaxMinecraftVersion())) <= 0)
                                                )
                                        )
                                        &&
                                        Objects.equals(n.node("parameters").getList(String.class), mappingParams);
                            } catch (SerializationException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                )
                .findFirst();

        if (optional.isPresent()) {
            var n = optional.get();
            var capitalized = getName().substring(0, 1).toUpperCase();
            if (getName().length() > 1) {
                capitalized += getName().substring(1);
            }

            int count;
            if (!accessor.getMethodNameCounter().containsKey(getName())) {
                accessor.getMethodNameCounter().put(getName(), 1);
                count = 1;
            } else {
                count = accessor.getMethodNameCounter().get(getName()) + 1;
                accessor.getMethodNameCounter().put(getName(), count);
            }

            var args = new ArrayList<>(List.of(generator.getAccessorUtils(), "getMethod", ClassName.get(generator.getBasePackage(), accessor.getClassName()), getName() + count, generator.generateMappings(n)));

            var strBuilder = new StringBuilder();

            for (var param : params) {
                strBuilder.append(", ");
                param.generateClassGetter(generator, accessor, strBuilder, args);
            }

            return MethodSpec.methodBuilder("getMethod" + capitalized + count)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Method.class)
                    .addStatement("return $T.$N($T.class, $S, $L" + strBuilder + ")", args.toArray())
                    .build();
        } else {
            throw new IllegalArgumentException("Can't find method: " + getName() + "(" + String.join(", ", mappingParams) + ")");
        }
    }
}
