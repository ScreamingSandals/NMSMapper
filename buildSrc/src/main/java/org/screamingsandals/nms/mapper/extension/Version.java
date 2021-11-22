package org.screamingsandals.nms.mapper.extension;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.nms.mapper.workspace.Workspace;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Data
@ConfigSerializable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Version {
    private String version;
    private String realVersion;
    private DownloadableContent vanillaJar;
    @Nullable
    private DownloadableContent mojangMappings;
    @Nullable
    private DownloadableContent seargeMappings;
    @Nullable
    private DownloadableContent spigotClassMappings;
    @Nullable
    private DownloadableContent spigotMemberMappings;

    private transient Workspace workspace;

    @Data
    @ConfigSerializable
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadableContent {
        private String url;
        @Nullable
        private String sha1;

        // thanks configurate
        public boolean isPresent() {
            return url != null && !url.isBlank();
        }
    }
}
