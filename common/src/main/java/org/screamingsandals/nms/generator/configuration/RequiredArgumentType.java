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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

import java.util.List;

public interface RequiredArgumentType {
    default RequiredArgumentArrayClass array() {
        if (this instanceof RequiredArgumentArrayClass) {
            throw new UnsupportedOperationException("Can't create an array of another array!");
        }
        return new RequiredArgumentArrayClass(this, 1);
    }

    default RequiredArgumentArrayClass array(@Range(from = 1, to = Integer.MAX_VALUE) int dimensions) {
        if (this instanceof RequiredArgumentArrayClass) {
            throw new UnsupportedOperationException("Can't create an array of another array!");
        }
        return new RequiredArgumentArrayClass(this, dimensions);
    }

    @ApiStatus.Internal
    void generateClassGetter(AccessorClassGenerator generator, Accessor accessor, StringBuilder expression, List<Object> params);
}
