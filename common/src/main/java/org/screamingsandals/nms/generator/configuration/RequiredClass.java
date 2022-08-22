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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;
import org.screamingsandals.nms.generator.utils.GroovyUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ToString(callSuper = true)
public class RequiredClass extends RequiredSymbol implements RequiredArgumentType {
    @Nullable
    private static Class<?> kotlinJvmInternalClassBasedDeclarationContainer;
    @Nullable
    private static Method cBDCJClassMethod;

    static {
        // Please Jetbrains, don't rename it in the future :)
        try {
            kotlinJvmInternalClassBasedDeclarationContainer = Class.forName("kotlin.jvm.internal.ClassBasedDeclarationContainer"); // DeclarationContainerImpl
        } catch (Throwable ignored) {
            try {
                kotlinJvmInternalClassBasedDeclarationContainer = Class.forName("kotlin.jvm.internal.DeclarationContainerImpl");
            } catch (Throwable ignored2) {
            }
        }
        try {
            if (kotlinJvmInternalClassBasedDeclarationContainer != null) {
                cBDCJClassMethod = kotlinJvmInternalClassBasedDeclarationContainer.getMethod("getJClass");
            }
        } catch (Throwable ignored) {
        }
    }

    @Getter
    private final List<RequiredClassMember> requiredSymbols = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final ClassContext context;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable
    @Getter
    private String forcedEnumFieldsLoadVersion;

    public RequiredClass(String mapping, String theClass, @Nullable String forcedVersion, ClassContext context) {
        super(mapping, theClass, forcedVersion);
        this.context = context;
        context.addClass(this);
    }

    public RequiredClass reqField(String unifiedString) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqField(split[0], context.getDefaultMapping(), context.getDefaultForcedVersion());
        } else if (split.length == 2) {
            return reqField(split[1], split[0].isBlank() ? context.getDefaultMapping() : split[0], context.getDefaultForcedVersion());
        } else if (split.length == 3) {
            return reqField(split[1], split[0].isBlank() ? context.getDefaultMapping() : split[0], split[2].isBlank() ? context.getDefaultForcedVersion() : split[2]);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqField(RequiredNameChain chain) {
        requiredSymbols.add(new RequiredField(chain));
        return this;
    }

    public RequiredClass reqField(String name, String mapping, @Nullable String forcedVersion) {
        requiredSymbols.add(new RequiredField(mapping, name, forcedVersion));
        return this;
    }

    public RequiredClass reqEnumField(String unifiedString) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqEnumField(split[0], context.getDefaultMapping(), context.getDefaultForcedVersion());
        } else if (split.length == 2) {
            return reqEnumField(split[1], split[0].isBlank() ? context.getDefaultMapping() : split[0], context.getDefaultForcedVersion());
        } else if (split.length == 3) {
            return reqEnumField(split[1], split[0].isBlank() ? context.getDefaultMapping() : split[0], split[2].isBlank() ? context.getDefaultForcedVersion() : split[2]);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqEnumField(RequiredNameChain chain) {
        requiredSymbols.add(new RequiredEnumField(chain));
        return this;
    }

    public RequiredClass reqEnumField(String name, String mapping, @Nullable String forcedVersion) {
        requiredSymbols.add(new RequiredEnumField(mapping, name, forcedVersion));
        return this;
    }

    @ApiStatus.Experimental
    public RequiredClass reqAllEnumFieldsOfVersion(String version) {
        this.forcedEnumFieldsLoadVersion = version;
        return this;
    }

    public RequiredClass reqConstructor(Object... params) {
        requiredSymbols.add(new RequiredConstructor(parseParams(params)));
        return this;
    }

    public RequiredClass reqMethod(String unifiedString, Object... params) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqMethod(split[0], context.getDefaultMapping(), context.getDefaultForcedVersion(), params);
        } else if (split.length == 2) {
            return reqMethod(split[1], split[0].isBlank() ? context.getDefaultMapping() : split[0], context.getDefaultForcedVersion(), params);
        } else if (split.length == 3) {
            return reqMethod(split[1], split[0].isBlank() ? context.getDefaultMapping() : split[0], split[2].isBlank() ? context.getDefaultForcedVersion() : split[2], params);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqMethod(RequiredNameChain chain, Object... params) {
        requiredSymbols.add(new RequiredMethod(chain, parseParams(params)));
        return this;
    }

    public RequiredClass reqMethod(String name, String mapping, String forcedVersion, Object[] params) {
        requiredSymbols.add(new RequiredMethod(mapping, name, forcedVersion, parseParams(params)));
        return this;
    }

    private static boolean NAGGED_AUTHOR_ABOUT_INLINE_CLASS_DECLARATION = false;

    private RequiredArgumentType[] parseParams(Object[] params) {
        var list = new ArrayList<RequiredArgumentType>();
        for (Object p : params) {
            if (p instanceof RequiredArgumentType) {
                list.add((RequiredArgumentType) p);
            } else if (p instanceof Class) {
                list.add(new RequiredArgumentJvmClass((Class<?>) p));
            } else if (kotlinJvmInternalClassBasedDeclarationContainer != null && cBDCJClassMethod != null && kotlinJvmInternalClassBasedDeclarationContainer.isInstance(p)) {
                try {
                    var javaClass = cBDCJClassMethod.invoke(p);
                    list.add(new RequiredArgumentJvmClass((Class<?>) javaClass));
                } catch (Throwable throwable) {
                    throw new RuntimeException("Invalid configuration: Can't convert kotlin KClass to java Class", throwable);
                }
            } else if (p instanceof CharSequence) {
                var unifiedString = p.toString();
                if (unifiedString.startsWith("@")) {
                    String copy = unifiedString.substring(1);
                    int arrayDimensions = 0;

                    while (copy.endsWith("[]")) {
                        copy = copy.substring(0, copy.length() - 2);
                        arrayDimensions++;
                    }
                    var contextClass = context.findClassInContext(copy);
                    if (arrayDimensions > 0) {
                        list.add(contextClass.array(arrayDimensions));
                    } else {
                        list.add(contextClass);
                    }
                } else if (unifiedString.startsWith("&")) {
                    if (!NAGGED_AUTHOR_ABOUT_INLINE_CLASS_DECLARATION) {
                        System.out.println("WARN: You have used inline class declaration (in method or constructor declaration) instead of reqClass() method! Save the return value of reqClass or use context instead!");
                        NAGGED_AUTHOR_ABOUT_INLINE_CLASS_DECLARATION = true;
                    }
                    unifiedString = unifiedString.substring(1);

                    var split = unifiedString.split(":");
                    // TODO: check if the class already exists in the context to avoid duplication
                    if (split.length == 1) {
                        String copy = split[0];
                        int arrayDimensions = 0;

                        while (copy.endsWith("[]")) {
                            copy = copy.substring(0, copy.length() - 2);
                            arrayDimensions++;
                        }
                        if (arrayDimensions > 0) {
                            list.add(new RequiredClass(context.getDefaultMapping(), copy, context.getDefaultForcedVersion(), context).array(arrayDimensions));
                        } else {
                            list.add(new RequiredClass(context.getDefaultMapping(), copy, context.getDefaultForcedVersion(), context));
                        }
                    } else if (split.length == 2) {
                        String copy = split[1];
                        int arrayDimensions = 0;

                        while (copy.endsWith("[]")) {
                            copy = copy.substring(0, copy.length() - 2);
                            arrayDimensions++;
                        }
                        if (arrayDimensions > 0) {
                            list.add(new RequiredClass(split[0], copy, context.getDefaultForcedVersion(), context).array(arrayDimensions));
                        } else {
                            list.add(new RequiredClass(split[0], copy, context.getDefaultForcedVersion(), context));
                        }
                    } else if (split.length == 3) {
                        String copy = split[1];
                        int arrayDimensions = 0;

                        while (copy.endsWith("[]")) {
                            copy = copy.substring(0, copy.length() - 2);
                            arrayDimensions++;
                        }
                        if (arrayDimensions > 0) {
                            list.add(new RequiredClass(split[0], copy, split[2], context).array(arrayDimensions));
                        } else {
                            list.add(new RequiredClass(split[0], copy, split[2], context));
                        }
                    } else {
                        throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
                    }
                } else {
                    String copy = unifiedString;
                    int arrayDimensions = 0;

                    while (copy.endsWith("[]")) {
                        copy = copy.substring(0, copy.length() - 2);
                        arrayDimensions++;
                    }
                    if (arrayDimensions > 0) {
                        list.add(new RequiredArgumentStringClass(copy).array(arrayDimensions));
                    } else {
                        list.add(new RequiredArgumentStringClass(copy));
                    }
                }
            }
        }
        return list.toArray(RequiredArgumentType[]::new);
    }

    @SneakyThrows
    @ApiStatus.Internal
    public RequiredClass call(Consumer<RequiredClass> closure) {
        GroovyUtils.hackClosure(closure, this);
        closure.accept(this);
        return this;
    }

    @Override
    @ApiStatus.Internal
    public void generateClassGetter(AccessorClassGenerator generator, Accessor accessor, StringBuilder expression, List<Object> params) {
        expression.append("$T.getType()");
        params.add(ClassName.get(generator.getBasePackage(), generator.getRequiredClassAccessorMap().get(this).getClassName()));
    }
}
