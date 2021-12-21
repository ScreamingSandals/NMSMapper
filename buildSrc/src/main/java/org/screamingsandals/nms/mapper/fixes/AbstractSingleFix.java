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
