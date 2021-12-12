package org.screamingsandals.nms.generator.configuration;

import com.squareup.javapoet.MethodSpec;
import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.nms.generator.build.Accessor;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;

public interface RequiredClassMember {
    @ApiStatus.Internal
    MethodSpec generateSymbolAccessor(Accessor accessor, AccessorClassGenerator generator);
}
