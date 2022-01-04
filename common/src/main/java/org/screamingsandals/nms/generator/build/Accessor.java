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

package org.screamingsandals.nms.generator.build;

import com.squareup.javapoet.TypeSpec;
import lombok.Data;
import org.screamingsandals.nms.generator.configuration.RequiredClass;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Accessor {
    private final String className;
    private final TypeSpec.Builder builder;
    private final ConfigurationNode mapping;
    private final RequiredClass requiredClass;
    private final String classHash;
    private final Map<String, Integer> fieldNameCounter = new HashMap<>();
    private final Map<String, Integer> methodNameCounter = new HashMap<>();
    private final AtomicInteger constructorCounter = new AtomicInteger();
}
