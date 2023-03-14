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

package org.screamingsandals.nms.mapper.parser;

import org.screamingsandals.nms.mapper.extension.Version;
import org.screamingsandals.nms.mapper.fixes.GenericMethodOverridingFix;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.ErrorsLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class QuiltMCMappingParser {
    public static String map(Map<String, ClassDefinition> map, Version version, List<String> excluded, ErrorsLogger errorsLogger) throws IOException, InterruptedException, URISyntaxException {
        var mcpZip = version.getWorkspace().getFile(Objects.requireNonNull(version.getQuiltMappings()), "quilt.jar");

        try (var zip = new ZipFile(mcpZip)) {
            var entry = zip.stream()
                    .filter(zipEntry -> zipEntry.getName().equals("mappings/mappings.tiny"))
                    .findFirst()
                    .orElseThrow();

            var inputStream = zip.getInputStream(entry);

            AnyMappingParser.map(map, inputStream, excluded, MappingType.QUILTMC, false, errorsLogger, MappingType.HASHED);

            new GenericMethodOverridingFix(MappingType.QUILTMC).fix(map);

            return Files.readAllLines(
                    version.getWorkspace()
                            .getFile("quiltmc-license.txt", "https://github.com/QuiltMC/quilt-mappings/blob/HEAD/LICENSE")
                            .toPath()
            ).stream().limit(12).collect(Collectors.joining("\n"));  // capping to 12 lines, don't want the entire CC0 license lol
        }
    }
}
