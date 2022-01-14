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
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.single.ProblemLocation;

import java.util.Map;

@Data
public abstract class AbstractSingleFix {
    protected final MappingType mappingType;
    protected int fixCount;

    public boolean isMappingSupported(MappingType type) {
        return true;
    }

    public String getName() {
        return getClass().getSimpleName() + "(" + mappingType.name() + ")";
    }

    public abstract void fix(Map<String, ClassDefinition> map);

    protected void logNewFix(ProblemLocation problemLocation, String fixMessage) {
        // TODO: save logged fix
        System.out.println("Applied " + getName() + ": " + fixMessage.replace("{location}", problemLocation.printProblemLocationWith(mappingType)));
        fixCount++;
    }

    protected void logNewFix(ProblemLocation problemLocation, ProblemLocation childProblem, String fixMessage) {
        // TODO: save logged fix
        System.out.println("Applied " + getName() + ": " + fixMessage.replace("{location}", problemLocation.printProblemLocationWith(childProblem, mappingType)));
        fixCount++;
    }
}
