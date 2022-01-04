/*
 * Copyright 2022 ScreamingSandals
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class IntermediaryMappingParser {
    public static String map(Map<String, ClassDefinition> map, Version version, List<String> excluded, ErrorsLogger errorsLogger) throws IOException, URISyntaxException, InterruptedException {
        var file = version.getWorkspace().getFile(Objects.requireNonNull(version.getIntermediaryMappings()), "fabric.tiny");
        AnyMappingParser.map(map, new ByteArrayInputStream(Files.readString(file.toPath()).getBytes(StandardCharsets.UTF_8)), excluded, MappingType.INTERMEDIARY, false, errorsLogger);

        new GenericMethodOverridingFix(MappingType.INTERMEDIARY).fix(map);

        return Files.readAllLines(
                version.getWorkspace()
                        .getFile("fabric-license.txt", "https://raw.githubusercontent.com/FabricMC/intermediary/master/LICENSE")
                        .toPath()
        ).stream().limit(12).collect(Collectors.joining("\n"));  // capping to 12 lines, don't want the entire CC0 license lol
    }
}
