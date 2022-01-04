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

import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.ErrorsLogger;

import java.io.*;
import java.util.List;
import java.util.Map;

public class MojangMappingParser {
    public static String map(Map<String, ClassDefinition> map, File file, List<String> excluded, ErrorsLogger errorsLogger) throws IOException {
        AnyMappingParser.map(map, new FileInputStream(file), excluded, MappingType.MOJANG, true, errorsLogger);

        try (var br = new BufferedReader(new FileReader(file))) {
            return br.readLine();
        } catch (Throwable ignored) {}

        return null;
    }
}
