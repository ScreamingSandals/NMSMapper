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

package org.screamingsandals.nms.mapper.utils;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.List;

@Data
public class License implements Comparable<License> {
    private final MappingType mappingType;
    private final String license;
    private final List<String> links;

    @Override
    public int compareTo(@NotNull License o) {
        return mappingType.compareTo(o.mappingType);
    }
}
