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

package org.screamingsandals.nms.mapper.newweb.components;

import lombok.Data;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Changelog {
    private final String version;
    private final String link;
    private final boolean first;
    private final Map<MappingType, Map.Entry<String, String>> nameMappings = new HashMap<>();
    private final List<ChangedSymbol> constructorChanges = new ArrayList<>();
    private final List<ChangedSymbol> fieldChanges = new ArrayList<>();
    private final List<ChangedSymbol> methodChanges = new ArrayList<>();
}
