package org.screamingsandals.nms.generator.configuration;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequiredEnumField extends RequiredSymbol implements RequiredClassMember {
    public RequiredEnumField(String mapping, String name, @Nullable String forcedVersion) {
        super(mapping, name, forcedVersion);
    }
}
