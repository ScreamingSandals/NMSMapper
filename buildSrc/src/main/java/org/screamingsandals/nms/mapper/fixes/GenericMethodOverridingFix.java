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

package org.screamingsandals.nms.mapper.fixes;

import org.screamingsandals.nms.mapper.parser.SpigotMappingParser;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.Map;

public class GenericMethodOverridingFix extends AbstractSingleFix {
    public GenericMethodOverridingFix(MappingType mappingType) {
        super(mappingType);
    }

    @Override
    public void fix(Map<String, ClassDefinition> map) {
        // Try to find overridden methods
        map.forEach((s, classDefinition) -> {
            var selfLink = ClassDefinition.Link.nmsLink(classDefinition.getMapping().get(MappingType.OBFUSCATED));
            classDefinition.getMethods()
                    .stream()
                    .filter(methodDefinition -> methodDefinition.getMapping().containsKey(MappingType.INTERMEDIARY))
                    .forEach(methodDefinition -> map.entrySet()
                    .stream()
                    .filter(entry -> SpigotMappingParser.isImplementing(map, entry.getValue(), selfLink))
                    .forEach(entry -> entry.getValue()
                            .getMethods()
                            .stream()
                            .filter(m -> !m.getMapping().containsKey(this.mappingType)
                                    && m.getMapping().get(MappingType.OBFUSCATED).equals(methodDefinition.getMapping().get(MappingType.OBFUSCATED))
                                    && m.getParameters().equals(methodDefinition.getParameters()))
                            .findFirst()
                            .ifPresent(md -> {
                                logNewFix(entry.getValue(), md, "Missing mapping for overridden method fixed: {location} overrides " + classDefinition.printProblemLocationWith(methodDefinition, this.mappingType));
                                md.getMapping().put(this.mappingType, methodDefinition.getMapping().get(this.mappingType));
                            })));
        });

        System.out.println(getName() + " applied " + fixCount + " times");
    }
}
