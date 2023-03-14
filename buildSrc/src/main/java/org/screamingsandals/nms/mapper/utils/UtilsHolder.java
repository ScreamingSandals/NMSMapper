/*
 * Copyright 2023 ScreamingSandals
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
import org.screamingsandals.nms.mapper.joined.JoinedClassDefinition;
import org.screamingsandals.nms.mapper.single.Mapping;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class UtilsHolder {
    private final Map<String, Mapping> mappings = new HashMap<>();

    //private final Map<String, Map<String, ClassDefinition>> mappings = new HashMap<>();
    private final Map<String, Map<MappingType, Map<String, String>>> mappingTypeLinks = new HashMap<>();
    private final File resourceDir;
    //private final Map<String, MappingType> newlyGeneratedMappings = new HashMap<>();
    private final AtomicReference<ConfigurationNode> versionManifest = new AtomicReference<>();
    private final Map<String, String> joinedMappingsClassLinks = new HashMap<>();
    private final Map<String, JoinedClassDefinition> joinedMappings = new HashMap<>();
    private final Map<String, String> spigotJoinedMappingsClassLinks = new HashMap<>();
    private final Map<String, String> seargeJoinedMappingsClassLinks = new HashMap<>();
    private final Map<String, String> intermediaryJoinedMappingsClassLinks = new HashMap<>();
    private final List<String> undefinedClassLinks = new ArrayList<>();
    //private final Map<Map.Entry<String, MappingType>, License> licenses = new HashMap<>();
    //private final Map<String, List<MappingType>> allMappingsByVersion = new HashMap<>();
}
