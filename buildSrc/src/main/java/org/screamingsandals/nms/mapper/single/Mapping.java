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

package org.screamingsandals.nms.mapper.single;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.mapper.utils.License;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Mapping {
    private final String version;
    private final Map<String, ClassDefinition> mappings = new HashMap<>();
    private final List<MappingType> supportedMappings = new ArrayList<>();
    private final List<License> licenses = new ArrayList<>();
    private MappingType defaultMapping = MappingType.OBFUSCATED;
    @Nullable
    private final String spigotNms;

}
