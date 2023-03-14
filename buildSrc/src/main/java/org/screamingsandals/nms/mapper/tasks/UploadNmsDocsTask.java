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

package org.screamingsandals.nms.mapper.tasks;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.SftpException;
import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public abstract class UploadNmsDocsTask extends DefaultTask {
    @Input
    public abstract Property<File> getDocsFolder();

    @SneakyThrows
    @TaskAction
    public void run() {
        if (System.getenv("NMSDOCS_USER") == null || System.getenv("NMSDOCS_HOST") == null || System.getenv("NMSDOCS_SECRET") == null) {
            return;
        }

        var jsch = new JSch();
        var jschSession = jsch.getSession(System.getenv("NMSDOCS_USER"), System.getenv("NMSDOCS_HOST"));
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setPassword(System.getenv("NMSDOCS_SECRET"));
        jschSession.connect();
        var sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
        sftpChannel.connect();

        sftpChannel.cd("www");

        recursiveClear(sftpChannel);

        recursiveFolderUpload(sftpChannel, getDocsFolder().get());

        sftpChannel.disconnect();

        jschSession.disconnect();
    }


    public static void recursiveClear(ChannelSftp sftpChannel) throws SftpException {
        sftpChannel.ls(".").forEach(entry -> {
            var file = (ChannelSftp.LsEntry) entry;

            if (file.getFilename().equals(".") || file.getFilename().equals("..")) {
                return;
            }

            try {
                if (file.getAttrs().isDir()) {
                    sftpChannel.cd(file.getFilename());
                    recursiveClear(sftpChannel);
                    sftpChannel.cd("..");
                    sftpChannel.rmdir(file.getFilename());
                } else {
                    sftpChannel.rm(file.getFilename());
                }
            } catch (SftpException e) {
                e.printStackTrace();
            }
        });
    }

    public static void recursiveFolderUpload(ChannelSftp channelSftp, File sourceFolder) throws SftpException {
        for (var file : sourceFolder.listFiles()) {
            if (file.isDirectory()) {
                channelSftp.mkdir(file.getName());
                channelSftp.cd(file.getName());
                recursiveFolderUpload(channelSftp, file);
                channelSftp.cd("..");
            } else {
                channelSftp.put(file.getAbsolutePath(), file.getName());
            }
        }
    }
}
