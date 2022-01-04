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

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.util.Arrays;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredField extends RequiredSymbol implements RequiredClassMember {
    public RequiredField(String mapping, String name, @Nullable String forcedVersion) {
        super(mapping, name, forcedVersion);
    }

    @Override
    @ApiStatus.Internal
    public MethodSpec generateSymbolAccessor(Accessor accessor, AccessorClassGenerator generator) {
        System.out.println("Generating accessor method for field " + getName());
        var optional = accessor.getMapping().node("fields")
                .childrenList()
                .stream()
                .filter(n -> n.node(getMapping().toUpperCase()).childrenMap().entrySet().stream().anyMatch(n1 ->
                        n1.getValue().getString("").equals(getName())
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
                ))
                .findFirst();

        if (optional.isPresent()) {
            var n = optional.get();

            var capitalized = getName().substring(0, 1).toUpperCase();
            if (getName().length() > 1) {
                capitalized += getName().substring(1);
            }

            int count;
            if (!accessor.getFieldNameCounter().containsKey(getName())) {
                accessor.getFieldNameCounter().put(getName(), 1);
                count = 1;
            } else {
                count = accessor.getFieldNameCounter().get(getName()) + 1;
                accessor.getFieldNameCounter().put(getName(), count);
            }

            return MethodSpec.methodBuilder("getField" + capitalized + (count != 1 ? count : ""))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Field.class)
                    .addStatement("return $T.$N($T.class, $S, $L)", generator.getAccessorUtils(), "getField", ClassName.get(generator.getBasePackage(), accessor.getClassName()), getName() + count, generator.generateMappings(n))
                    .build();
        } else {
            throw new IllegalArgumentException("Can't find field: " + getName());
        }
    }
}
