package org.screamingsandals.nms.mapper.newweb.components;

import lombok.Data;

@Data
public class NavbarLink {
    private final String label;
    private final String link;
    private final boolean active;
}
