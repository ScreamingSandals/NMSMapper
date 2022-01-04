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

import lombok.Data;
import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

import java.util.List;

@Data
public class RequiredArgumentStringClass implements RequiredArgumentType {
    private final String className;

    @Override
    @ApiStatus.Internal
    public void generateClassGetter(AccessorClassGenerator generator, Accessor accessor, StringBuilder expression, List<Object> params) {
        switch (className) {
            case "int":
            case "byte":
            case "short":
            case "long":
            case "char":
            case "float":
            case "double":
            case "boolean":
                expression.append(className).append(".class");
                break;
            default:
                expression.append("$T.getOrCatch($S)");
                params.add(generator.getAccessorUtils());
                params.add(className);
        }
    }
}
