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

import java.util.Objects;

@Data
public class ClassNameLink implements Comparable<ClassNameLink> {
    private final String name;
    @Nullable
    private final String link;
    @Nullable
    private final String title;
    @Nullable
    private final String suffix;

    @Override
    public int compareTo(@NotNull ClassNameLink o) {
        int last = name.compareTo(o.name);
        if (last != 0) {
            return last;
        }
        if (suffix == null && o.suffix == null) {
            return 0;
        } else if (suffix == null) {
            return o.suffix.compareTo("");
        } else {
            return suffix.compareTo(Objects.requireNonNullElse(o.suffix, ""));
        }
    }
}
