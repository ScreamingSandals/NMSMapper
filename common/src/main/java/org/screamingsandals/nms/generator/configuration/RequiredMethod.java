package org.screamingsandals.nms.generator.configuration;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredMethod extends RequiredSymbol implements RequiredClassMember {
    @Getter
    @ToString.Exclude // TODO: fix recursion
    private final RequiredArgumentType[] params;

    public RequiredMethod(String mapping, String name, @Nullable String forcedVersion, RequiredArgumentType[] params) {
        super(mapping, name, forcedVersion);
        this.params = params;
    }
}
