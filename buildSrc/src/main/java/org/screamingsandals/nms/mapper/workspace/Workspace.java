package org.screamingsandals.nms.mapper.workspace;

import lombok.Data;
import org.gradle.api.GradleException;
import org.screamingsandals.nms.mapper.extension.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Data
public class Workspace {
    private final String version;
    private final File baseFolder;
    private final HttpClient client = HttpClient.newHttpClient();
    private final MessageDigest checksumAlgorithm;

    public Workspace(String version, File baseFolder) {
        this.version = version;
        this.baseFolder = baseFolder;

        try {
            this.checksumAlgorithm = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new GradleException("An error occurred while creating new workspace", e);
        }
    }

    public File getFile(Version.DownloadableContent downloadableContent, String localName) throws IOException, URISyntaxException, InterruptedException {
        if (downloadableContent.getSha1() != null) {
            return getFile(localName, downloadableContent.getUrl(), downloadableContent.getSha1());
        } else {
            return getFile(localName, downloadableContent.getUrl());
        }
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        var fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        fis.close();

        byte[] bytes = digest.digest();

        var sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)  {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public File getFile(String name, String url, String checksum) throws IOException, URISyntaxException, InterruptedException {
        var file = new File(baseFolder, version + "/" + name);

        if (file.exists()) {
            var actualChecksum = getFileChecksum(checksumAlgorithm, file);
            if (!checksum.equals(actualChecksum)) {
                System.out.println("Checksum mismatch! Updating file " + version + "/" + name + " with new one from " + url);
                client.send(HttpRequest.newBuilder().uri(new URI(url)).build(), HttpResponse.BodyHandlers.ofFile(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            }
        } else {
            file.getParentFile().mkdirs();
            System.out.println("Downloading new file " + url + " to " + version + "/" + name);
            client.send(HttpRequest.newBuilder().uri(new URI(url)).build(), HttpResponse.BodyHandlers.ofFile(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
        }

        var actualChecksum = getFileChecksum(checksumAlgorithm, file);
        if (!checksum.equals(actualChecksum)) {
            throw new GradleException("Can't download file! Checksum mismatch");
        }

        return file;
    }

    public File getFile(String name, String url) throws IOException, URISyntaxException, InterruptedException {
        var file = new File(baseFolder, version + "/" + name);
        var origin = new File(baseFolder, version + "/" + name + ".origin");

        if (file.exists() && origin.exists()) {
            var url2 = Files.readString(origin.toPath());
            if (!url.equals(url2)) {
                System.out.println("Updating file " + version + "/" + name + " with new one from " + url);
                client.send(HttpRequest.newBuilder().uri(new URI(url)).build(), HttpResponse.BodyHandlers.ofFile(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
                Files.write(origin.toPath(), url.getBytes());
            }
        } else {
            file.getParentFile().mkdirs();
            System.out.println("Downloading new file " + url + " to " + version + "/" + name);
            client.send(HttpRequest.newBuilder().uri(new URI(url)).build(), HttpResponse.BodyHandlers.ofFile(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            Files.write(origin.toPath(), url.getBytes());
        }

        return file;
    }
}
