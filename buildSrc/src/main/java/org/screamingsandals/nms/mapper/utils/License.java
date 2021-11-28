package org.screamingsandals.nms.mapper.utils;

import lombok.Data;

import java.util.List;

@Data
public class License {
    private final String license;
    private final List<String> links;
}
