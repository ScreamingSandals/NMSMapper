/*
 * Copyright 2023 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    @Nullable
    private String spigotNmsVersion;
    private DownloadableContent vanillaJar;
    @Nullable
    private DownloadableContent mojangMappings;
    @Nullable
    private DownloadableContent seargeMappings;
    @Nullable
    private DownloadableContent spigotClassMappings;
    @Nullable
    private DownloadableContent spigotMemberMappings;
    @Nullable
    private DownloadableContent intermediaryMappings;
    @Nullable
    private DownloadableContent yarnMappings;
    @Nullable
    private DownloadableContent hashedQuiltMappings;
    @Nullable
    private DownloadableContent quiltMappings;

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
