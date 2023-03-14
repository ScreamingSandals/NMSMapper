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

import java.util.Map;

@Data
public class CompareFieldMultiVersion {
    private final Map<String, CompareField> map;

    public String getBaseName(String version) {
        var comp = this.map.get(version);
        if (comp != null) {
            var name = comp.getBaseName();
            return name != null ? name : "";
        }
        return "";
    }

    public String getObfuscatedName(String version) {
        var comp = this.map.get(version);
        if (comp != null) {
            var name = comp.getObfuscatedName();
            return name != null ? name : "";
        }
        return "";
    }

    public String getSecondName(String version) {
        var comp = this.map.get(version);
        if (comp != null) {
            var name = comp.getSecondName();
            return name != null ? name : "";
        }
        return "";
    }
}
