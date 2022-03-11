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

import lombok.Data;
import org.screamingsandals.nms.mapper.errors.MappingError;
import org.screamingsandals.nms.mapper.parser.SpigotMappingParser;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.single.ProblemLocation;

import java.util.Map;

public class StrangeSpigotMethodOverridingFix extends AbstractSingleFix {
    public StrangeSpigotMethodOverridingFix(MappingType mappingType) {
        super(mappingType);
    }

    @Override
    public void fix(Map<String, ClassDefinition> map) {
        // Try to find overridden methods
        map.forEach((s, classDefinition) -> {
            var selfLink = ClassDefinition.Link.nmsLink(classDefinition.getMapping().get(MappingType.OBFUSCATED));
            classDefinition.getMethods()
                    .stream()
                    .filter(methodDefinition -> methodDefinition.getMapping().containsKey(this.mappingType) && methodDefinition.getMapping().containsKey(MappingType.SEARGE))
                    .forEach(methodDefinition -> map.entrySet()
                            .stream()
                            .filter(entry -> SpigotMappingParser.isImplementing(map, entry.getValue(), selfLink))
                            .forEach(entry -> entry.getValue()
                                    .getMethods()
                                    .stream()
                                    .filter(m ->
                                            m.getMapping().containsKey(MappingType.SEARGE)
                                                    && !m.getMapping().containsKey(this.mappingType)
                                                    && m.getMapping().get(MappingType.SEARGE).equals(methodDefinition.getMapping().get(MappingType.SEARGE))
                                                    && m.getParameters().equals(methodDefinition.getParameters())
                                    )
                                    .findFirst()
                                    .ifPresent(md -> {
                                        md.getMapping().put(this.mappingType, methodDefinition.getMapping().get(this.mappingType));
                                        entry.getValue().getMappingErrors().add(new StrangeSpigotMethodOverridingMappingError(entry.getValue(), md, classDefinition, methodDefinition, this.mappingType));
                                        incrementFix();
                                    })));
        });

        System.out.println(getName() + " applied " + fixCount + " times");
    }

    @Data
    public static class StrangeSpigotMethodOverridingMappingError implements MappingError {
        // TODO: get rid of this "ProblemLocation" shit
        private final ProblemLocation classLocation;
        private final ProblemLocation methodLocation;
        private final ProblemLocation superClassLocation;
        private final ProblemLocation superMethodLocation;
        private final MappingType mappingType;

        @Override
        public String getErrorName() {
            return "Spigot overrides method with different name based on Searge mappings (fixed)";
        }

        @Override
        public String getDescription() {
            // TODO: better output, this is mess
            return classLocation.printProblemLocationWith(methodLocation, mappingType) + " overrides " + superClassLocation.printProblemLocationWith(superMethodLocation, mappingType) + ", but it's not in mappings";
        }

        @Override
        public Level getErrorLevel() {
            return Level.NOTICE;
        }
    }
}
