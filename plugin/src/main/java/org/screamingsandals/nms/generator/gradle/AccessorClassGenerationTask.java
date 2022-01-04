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

package org.screamingsandals.nms.generator.gradle;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;
import org.screamingsandals.nms.generator.configuration.NMSMapperConfiguration;

public abstract class AccessorClassGenerationTask extends DefaultTask {
    @SneakyThrows
    @TaskAction
    public void run() {
        var extension = getProject().getExtensions().getByType(NMSMapperConfiguration.class);
        var projectFolder = getProject().getProjectDir();

        var generator = new AccessorClassGenerator(extension, projectFolder);
        generator.run();
    }
}
