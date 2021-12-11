package org.screamingsandals.nms.generator.configuration;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassContext {
    private final List<RequiredClass> requiredClasses = new ArrayList<>();

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
