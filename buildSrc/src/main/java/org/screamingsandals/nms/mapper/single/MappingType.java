package org.screamingsandals.nms.mapper.single;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MappingType {
    MOJANG("Mojang", "success"),
    INTERMEDIARY("Intermediary", "info"),
    OBFUSCATED("Obfuscated", "primary"), // sometimes referred to as NOTCH
    SPIGOT("Spigot", "warning"),
    SEARGE("Searge", "danger"); // read from MCPConfig

    private final String webName;
    private final String bootstrapColor;
}
