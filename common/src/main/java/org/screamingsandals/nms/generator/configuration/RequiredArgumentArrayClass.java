package org.screamingsandals.nms.generator.configuration;

import lombok.Data;
import org.jetbrains.annotations.Range;

@Data
public class RequiredArgumentArrayClass implements RequiredArgumentType {
    private final RequiredArgumentType type;
    @Range(from = 1, to = Integer.MAX_VALUE)
    private final int dimensions;
}
