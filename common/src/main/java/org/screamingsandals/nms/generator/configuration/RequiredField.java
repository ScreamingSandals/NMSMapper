package org.screamingsandals.nms.generator.configuration;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredField extends RequiredSymbol implements RequiredClassMember {
    public RequiredField(String mapping, String name, @Nullable String forcedVersion) {
        super(mapping, name, forcedVersion);
    }
}
