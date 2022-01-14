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

package org.screamingsandals.nms.mapper.tasks;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.mapper.newweb.WebGenerator;
import org.screamingsandals.nms.mapper.newweb.components.VersionRecord;
import org.screamingsandals.nms.mapper.newweb.pages.MainPage;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.screamingsandals.nms.mapper.utils.UtilsHolder;

import java.io.File;
import java.util.Arrays;

public abstract class NewDocsGenerationTask extends DefaultTask {
    @Input
    public abstract Property<File> getTemplatesFolder();
    @Input
    public abstract Property<File> getOutputFolder();
    @Input
    public abstract Property<UtilsHolder> getUtils();

    @SneakyThrows
    @TaskAction
    public void run() {
        var outputFolder = getOutputFolder().get();

        outputFolder.mkdirs();

        var generator = new WebGenerator(getTemplatesFolder().get(), outputFolder);

        // TODO: other parts of generation

        var mainPage = new MainPage();
        mainPage.getVersions().add(new VersionRecord("1.18.1", Arrays.asList(MappingType.values()), "1.18.1/"));
        generator.generate(mainPage);
    }

}
