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

package org.screamingsandals.nms.mapper.web.components;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.List;
import java.util.Map;

@Data
public class ChangedSymbol {
    private final List<Map.Entry<MappingType, String>> mappings;
    private final boolean addition;
    private final boolean canHaveArguments;
    @Nullable
    private final ClassNameLink returnType;
    private final List<ClassNameLink> arguments;

    public static ChangedSymbol addition(List<Map.Entry<MappingType, String>> mappings, @Nullable ClassNameLink link) {
        return new ChangedSymbol(mappings,true, false, link, null);
    }

    public static ChangedSymbol addition(List<Map.Entry<MappingType, String>> mappings, @Nullable ClassNameLink link, List<ClassNameLink> arguments) {
        return new ChangedSymbol(mappings,true, true, link, arguments == null ? List.of() : arguments);
    }

    public static ChangedSymbol removal(List<Map.Entry<MappingType, String>> mappings, @Nullable ClassNameLink link) {
        return new ChangedSymbol(mappings,false, false, link, null);
    }

    public static ChangedSymbol removal(List<Map.Entry<MappingType, String>> mappings, @Nullable ClassNameLink link, List<ClassNameLink> arguments) {
        return new ChangedSymbol(mappings,false, true, link, arguments == null ? List.of() : arguments);
    }
}
