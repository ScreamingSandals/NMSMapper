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
