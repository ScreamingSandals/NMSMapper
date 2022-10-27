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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessorUtils {
    private static final Map<Class<?>, Class<?>> classCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, String>, Field> fieldCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, String>, Object> enumCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, String>, Method> methodCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, Integer>, Constructor<?>> constructorCache = new HashMap<>();

    private static int MAJOR_VERSION;
    private static int MINOR_VERSION;
    private static int PATCH_VERSION;
    {/*=Generate=fields*/}

    static {
        {/*=Generate=static*/}
    }

    {/*=Generate=initializers*/}

    public static Class<?> getType(Class<?> accessor, Consumer<AccessorMapper> mapper) {
        Class<?> cache = classCache.get(accessor);
        if (cache != null) {
            return cache;
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        {/*=Generate=reflection=class*/}

        // cache that we don't have it
        classCache.put(accessor, null);
        return null;
    }

    public static Field getField(Class<?> accessor, String field, Consumer<AccessorMapper> mapper) {
        Map.Entry<Class<?>, String> kvholder = new AbstractMap.SimpleEntry<>(accessor, field);
        if (fieldCache.containsKey(kvholder)) {
            return fieldCache.get(kvholder);
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        {/*=Generate=reflection=fields*/}

        // cache that we don't have it
        fieldCache.put(kvholder, null);
        return null;
    }

    public static Object getEnumField(Class<?> accessor, String field, Consumer<AccessorMapper> mapper) {
        Map.Entry<Class<?>, String> kvholder = new AbstractMap.SimpleEntry<>(accessor, field);
        if (enumCache.containsKey(kvholder)) {
            return enumCache.get(kvholder);
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        {/*=Generate=reflection=enums*/}

        // cache that we don't have it
        enumCache.put(kvholder, null);
        return null;
    }

    public static Method getMethod(Class<?> accessor, String method, Consumer<AccessorMapper> mapper, Class<?>... params) {
        Map.Entry<Class<?>, String> kvholder = new AbstractMap.SimpleEntry<>(accessor, method);
        if (methodCache.containsKey(kvholder)) {
            return methodCache.get(kvholder);
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        {/*=Generate=reflection=methods*/}

        // cache that we don't have it
        methodCache.put(kvholder, null);
        return null;
    }

    public static Constructor<?> getConstructor(Class<?> accessor, int constructor, Class<?>... params) {
        Map.Entry<Class<?>, Integer> kvholder = new AbstractMap.SimpleEntry<>(accessor, constructor);
        if (constructorCache.containsKey(kvholder)) {
            return constructorCache.get(kvholder);
        }

        try {
            Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
            Constructor<?> constructorC = clazz.getDeclaredConstructor(params);
            constructorC.setAccessible(true);
            constructorCache.put(kvholder, constructorC);
            return constructorC;
        } catch (Throwable ignored) {
        }

        // cache that we don't have it
        constructorCache.put(kvholder, null);
        return null;
    }

    public static boolean isVersion(int[] ver) {
        return isVersion(ver[0], ver[1], ver[2]);
    }

    public static boolean isVersion(int major, int minor, int patch) {
        return MAJOR_VERSION > major || (MAJOR_VERSION >= major && (MINOR_VERSION > minor || (MINOR_VERSION >= minor && PATCH_VERSION >= patch)));
    }

    public static int compare(int[] ver, int[] ver2) {
        if (ver[0] != ver2[0]) {
            return ver[0] - ver2[0];
        }
        if (ver[1] != ver2[1]) {
            return ver[1] - ver2[1];
        }
        return ver[2] - ver2[2];
    }

    public static int[] convertVersion(String version) {
        int[] result = new int[3];
        if (version != null) {
            String[] split = version.split("\\.");
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    result[0] = Integer.parseInt(split[i]);
                } else if (i == 1) {
                    result[1] = Integer.parseInt(split[i]);
                } else if (i == 2) {
                    result[2] = Integer.parseInt(split[i]);
                }
            }
        }
        return result;
    }

    public static String reduceMapping(Map<String, Map<int[], String>> map, String mapping) {
        Optional<String> opt = Optional.ofNullable(map.get(mapping))
                .flatMap(m -> m.entrySet()
                        .stream()
                        .filter(entry -> isVersion(entry.getKey()))
                        .sorted((o1, o2) -> compare(o1.getKey(), o2.getKey()))
                        .reduce((first, second) -> second)
                        .map(Map.Entry::getValue)
                );
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return Optional.ofNullable(map.get(mapping))
                    .flatMap(m -> m.entrySet()
                            .stream()
                            .sorted((o1, o2) -> compare(o1.getKey(), o2.getKey()))
                            .reduce((first, second) -> second)
                            .map(Map.Entry::getValue)
                    )
                    .orElse(null);
        }
    }

    public static class AccessorMapper {
        private final Map<String, Map<int[], String>> map = new HashMap<>();

        public AccessorMapper map(String mappingType, String minVersion, String symbol) {
            if (!map.containsKey(mappingType)) {
                map.put(mappingType, new HashMap<>());
            }
            {/*=Generate=mapper*/}
            Map<int[], String> map2 = map.get(mappingType);
            map2.put(convertVersion(minVersion), symbol);
            return this;
        }
    }

    public static Class<?> getOrCatch(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}