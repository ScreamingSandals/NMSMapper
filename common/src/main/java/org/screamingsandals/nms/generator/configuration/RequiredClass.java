package org.screamingsandals.nms.generator.configuration;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.generator.utils.GroovyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredClass extends RequiredSymbol implements RequiredArgumentType {
    private final List<RequiredClassMember> requiredSymbols = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final ClassContext context;

    public RequiredClass(String mapping, String theClass, @Nullable String forcedVersion, ClassContext context) {
        super(mapping, theClass, forcedVersion);
        this.context = context;
        context.addClass(this);
    }

    public RequiredClass reqField(String unifiedString) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqField(split[0], NewNMSMapperConfiguration.DEFAULT_MAPPING, null);
        } else if (split.length == 2) {
            return reqField(split[1], split[0], null);
        } else if (split.length == 3) {
            return reqField(split[1], split[0], split[2]);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqField(String name, String mapping, @Nullable String forcedVersion) {
        requiredSymbols.add(new RequiredField(mapping, name, forcedVersion));
        return this;
    }

    public RequiredClass reqEnumField(String unifiedString) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqEnumField(split[0], NewNMSMapperConfiguration.DEFAULT_MAPPING, null);
        } else if (split.length == 2) {
            return reqEnumField(split[1], split[0], null);
        } else if (split.length == 3) {
            return reqEnumField(split[1], split[0], split[2]);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqEnumField(String name, String mapping, @Nullable String forcedVersion) {
        requiredSymbols.add(new RequiredEnumField(mapping, name, forcedVersion));
        return this;
    }

    public RequiredClass reqConstructor(Object... params) {
        requiredSymbols.add(new RequiredConstructor(parseParams(params)));
        return this;
    }

    public RequiredClass reqMethod(String unifiedString, Object... params) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqMethod(split[0], NewNMSMapperConfiguration.DEFAULT_MAPPING, null, params);
        } else if (split.length == 2) {
            return reqMethod(split[1], split[0], null, params);
        } else if (split.length == 3) {
            return reqMethod(split[1], split[0], split[2], params);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
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
                    if (split.length == 1) {
                        String copy = split[0];
                        int arrayDimensions = 0;

                        while (copy.endsWith("[]")) {
                            copy = copy.substring(0, copy.length() - 2);
                            arrayDimensions++;
                        }
                        if (arrayDimensions > 0) {
                            list.add(new RequiredClass(copy, NewNMSMapperConfiguration.DEFAULT_MAPPING, null, context).array(arrayDimensions));
                        } else {
                            list.add(new RequiredClass(copy, NewNMSMapperConfiguration.DEFAULT_MAPPING, null, context));
                        }
                    } else if (split.length == 2) {
                        String copy = split[1];
                        int arrayDimensions = 0;

                        while (copy.endsWith("[]")) {
                            copy = copy.substring(0, copy.length() - 2);
                            arrayDimensions++;
                        }
                        if (arrayDimensions > 0) {
                            list.add(new RequiredClass(copy, split[0], null, context).array(arrayDimensions));
                        } else {
                            list.add(new RequiredClass(copy, split[0], null, context));
                        }
                    } else if (split.length == 3) {
                        String copy = split[1];
                        int arrayDimensions = 0;

                        while (copy.endsWith("[]")) {
                            copy = copy.substring(0, copy.length() - 2);
                            arrayDimensions++;
                        }
                        if (arrayDimensions > 0) {
                            list.add(new RequiredClass(copy, split[0], split[2], context).array(arrayDimensions));
                        } else {
                            list.add(new RequiredClass(copy, split[0], split[2], context));
                        }
                    } else {
                        throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
                    }
                } else {
                    list.add(new RequiredArgumentStringClass(unifiedString));
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
}
