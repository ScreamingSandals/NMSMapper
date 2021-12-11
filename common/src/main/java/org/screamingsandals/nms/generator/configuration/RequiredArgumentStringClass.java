package org.screamingsandals.nms.generator.configuration;

import lombok.Data;

@Data
public class RequiredArgumentStringClass implements RequiredArgumentType {
    private final String className;
}
