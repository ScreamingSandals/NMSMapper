package org.screamingsandals.nms.generator.configuration;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public abstract class RequiredSymbol implements Required {
    private final String mapping;
    private final String name;
    @Nullable
    private final String forcedVersion;
}
