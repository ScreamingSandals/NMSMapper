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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassContext {
    private final List<RequiredClass> requiredClasses = new ArrayList<>();
    private String defaultMapping = NMSMapperConfiguration.DEFAULT_MAPPING;
    @Nullable
    private String defaultForcedVersion = null;

    public void addClass(RequiredClass requiredClass) {
        this.requiredClasses.add(requiredClass);
    }

    public RequiredClass findClassInContext(String contextName) {
        return requiredClasses.stream()
                .filter(requiredClass -> {
                    if (requiredClass.getName().equals(contextName)) {
                        return true;
                    }
                    var dot = requiredClass.getName().lastIndexOf(".");
                    if (dot > -1) {
                        return requiredClass.getName().substring(dot + 1).equals(contextName);
                    }
                    return false;
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't find class " + contextName + " in the context!"));
    }

    public List<RequiredClass> getAllClasses() {
        return List.copyOf(requiredClasses);
    }

}
