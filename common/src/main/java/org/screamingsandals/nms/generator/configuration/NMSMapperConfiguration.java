package org.screamingsandals.nms.generator.configuration;

import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.generator.utils.GroovyUtils;

import java.util.function.Consumer;

@Data
public class NMSMapperConfiguration {
    public static final String DEFAULT_MAPPING = "mojang";

    private final ClassContext classContext = new ClassContext();

    private String sourceSet = "src/main/java";
    private String basePackage = "com.example.nms";
    private boolean cleanOnRebuild = false;
    private String minMinecraftVersion = "1.9.4";
    @Nullable
    private String maxMinecraftVersion = null;

    public RequiredClass reqClass(String unifiedString) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqClass(split[0], classContext.getDefaultMapping(), classContext.getDefaultForcedVersion());
        } else if (split.length == 2) {
            return reqClass(split[1], split[0], classContext.getDefaultForcedVersion());
        } else if (split.length == 3) {
            return reqClass(split[1], split[0], split[2]);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqClass(String unifiedString, Consumer<RequiredClass> consumer) {
        var split = unifiedString.split(":");
        if (split.length == 1) {
            return reqClass(split[0], classContext.getDefaultMapping(), classContext.getDefaultForcedVersion(), consumer);
        } else if (split.length == 2) {
            return reqClass(split[1], split[0], classContext.getDefaultForcedVersion(), consumer);
        } else if (split.length == 3) {
            return reqClass(split[1], split[0], split[2], consumer);
        } else {
            throw new RuntimeException("Invalid configuration: Can't parse " + unifiedString);
        }
    }

    public RequiredClass reqClass(String className, String mapping, @Nullable String forcedVersion) {
        return new RequiredClass(mapping, className, forcedVersion, classContext);
    }

    public RequiredClass reqClass(String className, String mapping, @Nullable String forcedVersion, Consumer<RequiredClass> consumer) {
        var required = reqClass(className, mapping, forcedVersion);
        GroovyUtils.hackClosure(consumer, required);
        consumer.accept(required);
        return required;
    }

    @SneakyThrows
    @ApiStatus.Internal
    public NMSMapperConfiguration call(Consumer<NMSMapperConfiguration> closure) {
        GroovyUtils.hackClosure(closure, this);
        closure.accept(this);
        return this;
    }

}
