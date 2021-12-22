package org.screamingsandals.nms.mapper.newweb.components;

import lombok.Data;
import org.screamingsandals.nms.mapper.single.MappingType;

import java.util.List;

@Data
public class VersionRecord {
    private final String version;
    private final List<MappingType> mappingTypes;
    private final String link;
}
