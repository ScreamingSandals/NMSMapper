package org.screamingsandals.nms.generator.configuration;

import lombok.Data;

@Data
public class RequiredArgumentJvmClass implements RequiredArgumentType {
    private final Class<?> theClass;
}
