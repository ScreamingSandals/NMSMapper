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
import lombok.ToString;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredField extends RequiredChainedSymbol implements RequiredClassMember {
    public RequiredField(String mapping, String name, @Nullable String forcedVersion) {
        super(new RequiredNameChain(List.of(new RequiredName(mapping, name, forcedVersion))));
    }

    public RequiredField(RequiredNameChain chain) {
        super(chain);
    }

    @Override
    @ApiStatus.Internal
    public MethodSpec generateSymbolAccessor(Accessor accessor, AccessorClassGenerator generator) {
        var chain = getChain();
        if (chain.getRequiredNames().isEmpty()) {
            throw new UnsupportedOperationException("Provided chain has no names!");
        }

        System.out.println("Generating accessor method for field " + chain.getRequiredNames().toString());
        var chainedNodes = chain.getRequiredNames().stream()
                .flatMap(chainedName -> accessor.getMapping().node("fields")
                        .childrenList()
                        .stream()
                        .filter(n -> n.node(chainedName.getMapping().toUpperCase()).childrenMap().entrySet().stream().anyMatch(n1 ->
                                n1.getValue().getString("").equals(chainedName.getName())
                                        && (chainedName.getForcedVersion() == null || Arrays.asList(n1.getKey().toString().split(",")).contains(chainedName.getForcedVersion()))
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
                        ))
                        .map(n -> (ConfigurationNode) n)
                        .findFirst()
                        .stream())
                .collect(Collectors.toList());

        if (!chainedNodes.isEmpty()) {
            var firstName = chain.getRequiredNames().get(0).getName();

            var capitalized = firstName.substring(0, 1).toUpperCase();
            if (firstName.length() > 1) {
                capitalized += firstName.substring(1);
            }

            int count;
            if (!accessor.getFieldNameCounter().containsKey(firstName)) {
                accessor.getFieldNameCounter().put(firstName, 1);
                count = 1;
            } else {
                count = accessor.getFieldNameCounter().get(firstName) + 1;
                accessor.getFieldNameCounter().put(firstName, count);
            }

            var fieldBuilder = MethodSpec.methodBuilder("getField" + capitalized + (count != 1 ? count : ""))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Field.class)
                    .addStatement("return $T.$N($T.class, $S, $L)", generator.getAccessorUtils(), "getField", ClassName.get(generator.getBasePackage(), accessor.getClassName()), firstName + count, generator.generateMappings(chainedNodes));
            var nullable = generator.getConfiguration().getNullableAnnotation();
            if (nullable != null) {
                fieldBuilder.addAnnotation(ClassName.get(nullable.substring(0, nullable.lastIndexOf('.')), nullable.substring(nullable.lastIndexOf('.') + 1)));
            }
            return fieldBuilder.build();
        } else {
            throw new IllegalArgumentException("Can't find field: " + chain.getRequiredNames());
        }
    }
}
