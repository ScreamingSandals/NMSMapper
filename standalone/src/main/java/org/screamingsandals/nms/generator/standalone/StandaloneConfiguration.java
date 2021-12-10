package org.screamingsandals.nms.generator.standalone;

import groovy.lang.Closure;
import org.screamingsandals.nms.generator.configuration.NMSMapperConfiguration;

public class StandaloneConfiguration extends NMSMapperConfiguration {

    public StandaloneConfiguration call(Closure<StandaloneConfiguration> closure) {
        closure.setDelegate(this);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call(this);
        return this;
    }
}
