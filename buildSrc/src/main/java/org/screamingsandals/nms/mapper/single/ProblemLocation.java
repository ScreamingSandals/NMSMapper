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

package org.screamingsandals.nms.mapper.single;

import org.jetbrains.annotations.Nullable;

public interface ProblemLocation {
    String printProblemLocation();

    default String printProblemLocationWith(MappingType mappingType) {
        var mapLoc = printProblemLocationUsing(mappingType);
        return printProblemLocation() + (mapLoc != null ? (" " + mapLoc) : "");
    }

    @Nullable
    String printProblemLocationUsing(MappingType mappingType);

    default String printProblemLocationWith(ProblemLocation childProblem) {
        return printProblemLocation() + "#" + childProblem.printProblemLocation();
    }

    default String printProblemLocationWith(ProblemLocation childProblem, MappingType mappingType) {
        var mapLoc = printProblemLocationUsing(mappingType);
        var childLoc = childProblem.printProblemLocationUsing(mappingType);
        return printProblemLocation() + "#" + childProblem.printProblemLocation() + (mapLoc != null ? (" " + mapLoc + "#" + (childLoc != null ? childLoc : childProblem.printProblemLocation())) : "");
    }
}
