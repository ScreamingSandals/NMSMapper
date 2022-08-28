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
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredMethod extends RequiredChainedSymbol implements RequiredClassMember {
    @Getter
    @ToString.Exclude // TODO: fix recursion
    @EqualsAndHashCode.Exclude // TODO: fix recursion
    private final RequiredArgumentType[] params;

    public RequiredMethod(String mapping, String name, @Nullable String forcedVersion, RequiredArgumentType[] params) {
        super(new RequiredNameChain(List.of(new RequiredName(mapping, name, forcedVersion))));
        this.params = params;
    }

    public RequiredMethod(RequiredNameChain chain, RequiredArgumentType[] params) {
        super(chain);
        this.params = params;
    }

    @Override
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
        var chain = getChain();
        if (chain.getRequiredNames().isEmpty()) {
            throw new UnsupportedOperationException("Provided chain has no names!");
        }
        var paramsString = mappingParams.stream().map(s -> {
                            var s2 = s.replace("[]", "");
                            if (hashToName.containsKey(s2)) {
                                var s3 = hashToName.get(s2);
                                s = s.replace(s2, s3);
                            }
                            return s;
                        })
                        .collect(Collectors.joining(", "));
        System.out.println("[" + accessor.getClassName() + "] Generating accessor method for method " + chain.getRequiredNames().toString() + "(" + paramsString + ")");

        var chainedNodes = chain.getRequiredNames().stream()
                .flatMap(chainedName -> accessor.getMapping().node("methods")
                        .childrenList()
                        .stream()
                        .filter(n -> {
                            try {
                                return n.node(chainedName.getMapping().toUpperCase(Locale.ROOT))
                                        .childrenMap()
                                        .entrySet()
                                        .stream()
                                        .anyMatch(n1 -> n1.getValue().getString("").equals(chainedName.getName())
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
                                        )
                                        &&
                                        Objects.equals(n.node("parameters").getList(String.class), mappingParams);
                            } catch (SerializationException e) {
                                e.printStackTrace();
                            }
                            return false;
                        })
                        .map(n -> (ConfigurationNode) n)
                        .findFirst()
                        .stream())
                .collect(Collectors.toList());

        if (!chainedNodes.isEmpty()) {
            var firstName = chain.getRequiredNames().get(0).getName();

            var capitalized = firstName.substring(0, 1).toUpperCase(Locale.ROOT);
            if (firstName.length() > 1) {
                capitalized += firstName.substring(1);
            }

            int count;
            if (!accessor.getMethodNameCounter().containsKey(firstName)) {
                accessor.getMethodNameCounter().put(firstName, 1);
                count = 1;
            } else {
                count = accessor.getMethodNameCounter().get(firstName) + 1;
                accessor.getMethodNameCounter().put(firstName, count);
            }

            var args = new ArrayList<>(List.of(generator.getAccessorUtils(), "getMethod", ClassName.get(generator.getBasePackage(), accessor.getClassName()), firstName + count, generator.generateMappings(chainedNodes, generator.getConfiguration().isAddInformationJavadoc())));

            var strBuilder = new StringBuilder();

            for (var param : params) {
                strBuilder.append(", ");
                param.generateClassGetter(generator, accessor, strBuilder, args);
            }

            var methodBuilder = MethodSpec.methodBuilder("getMethod" + capitalized + count)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Method.class)
                    .addStatement("return $T.$N($T.class, $S, $L" + strBuilder + ")", args.toArray());
            var nullable = generator.getConfiguration().getNullableAnnotation();
            if (nullable != null) {
                methodBuilder.addAnnotation(ClassName.get(nullable.substring(0, nullable.lastIndexOf('.')), nullable.substring(nullable.lastIndexOf('.') + 1)));
            }
            if (generator.getConfiguration().isAddInformationJavadoc()) {
                var requestedMethod = new StringBuilder();
                if (chain.getRequiredNames().size() == 1) {
                    var name = chain.getRequiredNames().get(0);
                    requestedMethod.append("Requested method: ").append(name.getName()).append(!name.getMapping().isBlank() ? ", mapping: " + name.getMapping() : "").append(name.getForcedVersion() != null && !name.getForcedVersion().isBlank() ? ", version: " + name.getForcedVersion() : "");
                } else {
                    requestedMethod.append("The method was requested using the following chain of names:\n<ul>");
                    for (var name : chain.getRequiredNames()) {
                        requestedMethod.append("\n<li>").append(name.getName()).append(!name.getMapping().isBlank() ? ", mapping: " + name.getMapping() : "").append(name.getForcedVersion() != null && !name.getForcedVersion().isBlank() ? ", version: " + name.getForcedVersion() : "");
                    }
                    requestedMethod.append("\n</ul>");
                }
                requestedMethod.append("\nParameters of requested method: (").append(paramsString.replace("$", "$$")).append(")");
                methodBuilder.addJavadoc("This method returns the {@link Method} object of the requested NMS method.\n<p>\n" +
                        requestedMethod +
                        "\n<p>\nThis method is safe to call: exception is handled and null is returned in case of failure.\n\n@return the method object or null if either class does not exist or it does not have this field in the specific environment");
            }
            return methodBuilder.build();
        } else {
            throw new IllegalArgumentException("[" + accessor.getClassName() + "] Can't find method: " + chain.getRequiredNames().toString() + "(" + paramsString + ")");
        }
    }
}
