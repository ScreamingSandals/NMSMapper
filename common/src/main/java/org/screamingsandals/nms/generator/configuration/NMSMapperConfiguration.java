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

import groovy.lang.Closure;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.generator.utils.Action;
import org.screamingsandals.nms.generator.utils.GroovyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
public class NMSMapperConfiguration {
    public static final String DEFAULT_MAPPING = "mojang";

    private final ClassContext classContext = new ClassContext();

    private String sourceSet = "src/main/java";
    private String basePackage = "com.example.nms";
    private boolean cleanOnRebuild = false;
    private String minMinecraftVersion = "1.9.4";
    @Nullable
    private String maxMinecraftVersion = null;

    public RequiredClass reqClass(String unifiedString) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqClass(split[0], classContext.getDefaultMapping(), classContext.getDefaultForcedVersion());
        } else if (split.length == 2) {
            return reqClass(split[1], split[0].isBlank() ? classContext.getDefaultMapping() : split[0], classContext.getDefaultForcedVersion());
        } else if (split.length == 3) {
            return reqClass(split[1], split[0].isBlank() ? classContext.getDefaultMapping() : split[0], split[2].isBlank() ? classContext.getDefaultForcedVersion() : split[2]);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqClass(String unifiedString, Action<RequiredClass> consumer) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqClass(split[0], classContext.getDefaultMapping(), classContext.getDefaultForcedVersion(), consumer);
        } else if (split.length == 2) {
            return reqClass(split[1], split[0].isBlank() ? classContext.getDefaultMapping() : split[0], classContext.getDefaultForcedVersion(), consumer);
        } else if (split.length == 3) {
            return reqClass(split[1], split[0].isBlank() ? classContext.getDefaultMapping() : split[0], split[2].isBlank() ? classContext.getDefaultForcedVersion() : split[2], consumer);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqClass(String unifiedString, Closure<RequiredClass> consumer) {
        return reqClass(unifiedString, GroovyUtils.convertToAction(consumer));
    }

    public RequiredClass reqClass(String className, String mapping, @Nullable String forcedVersion) {
        return new RequiredClass(mapping, className, forcedVersion, classContext);
    }

    public RequiredClass reqClass(String className, String mapping, @Nullable String forcedVersion, Action<RequiredClass> consumer) {
        var required = reqClass(className, mapping, forcedVersion);
        consumer.execute(required);
        return required;
    }

    public RequiredClass reqClass(String className, String mapping, @Nullable String forcedVersion, Closure<RequiredClass> consumer) {
        return reqClass(className, mapping, forcedVersion, GroovyUtils.convertToAction(consumer));
    }

    public RequiredNameChain chain(String... unifiedStrings) {
        if (unifiedStrings.length == 0) {
            throw new RuntimeException("Invalid configuration: name chain needs at least one name");
        }
        var list = new ArrayList<RequiredName>();
        for (var unifiedString : unifiedStrings) {
            var split = unifiedString.split(":");
            if (split.length == 1) {
                list.add(name(split[0], classContext.getDefaultMapping(), classContext.getDefaultForcedVersion()));
            } else if (split.length == 2) {
                list.add(name(split[1], split[0].isBlank() ? classContext.getDefaultMapping() : split[0], classContext.getDefaultForcedVersion()));
            } else if (split.length == 3) {
                list.add(name(split[1], split[0].isBlank() ? classContext.getDefaultMapping() : split[0], split[2].isBlank() ? classContext.getDefaultForcedVersion() : split[2]));
            } else {
                throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
            }
        }
        return new RequiredNameChain(list);
    }

    public RequiredNameChain chain(RequiredName... names) {
        if (names.length == 0) {
            throw new RuntimeException("Invalid configuration: name chain needs at least one name");
        }
        return new RequiredNameChain(List.of(names));
    }

    public RequiredName name(String name, String mapping, @Nullable String forcedVersion) {
        return new RequiredName(mapping, name, forcedVersion != null ? forcedVersion : classContext.getDefaultForcedVersion());
    }

    @SneakyThrows
    @ApiStatus.Internal
    public NMSMapperConfiguration call(Consumer<NMSMapperConfiguration> closure) {
        GroovyUtils.hackClosure(closure, this);
        closure.accept(this);
        return this;
    }

}
