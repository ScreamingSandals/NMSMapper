package org.screamingsandals.nms.generator.configuration;

import lombok.Data;
import lombok.ToString;

@Data
public class RequiredConstructor implements Required, RequiredClassMember {
    @ToString.Exclude // TODO: fix recursion
    private final RequiredArgumentType[] params;
}
