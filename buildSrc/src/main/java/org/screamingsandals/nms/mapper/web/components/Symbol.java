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

package org.screamingsandals.nms.mapper.web.components;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.List;

@Data
public class Symbol implements Comparable<Symbol> {
    private final String modifier;
    @Nullable
    private final ClassNameLink type;
    private final List<SymbolMapping> mappings;

    @Override
    public int compareTo(@NotNull Symbol o) {
        // TODO: comparing based on arguments
        return mappings.stream()
                .filter(symbolMapping -> symbolMapping.getMappingType() == MappingType.OBFUSCATED)
                .findFirst()
                .flatMap(symbolMapping -> o.mappings.stream()
                        .filter(symbolMapping2 -> symbolMapping2.getMappingType() == MappingType.OBFUSCATED)
                        .findFirst()
                        .map(symbolMapping1 -> symbolMapping1.getName().compareTo(symbolMapping.getName())))
                .orElse(0);
    }
}
