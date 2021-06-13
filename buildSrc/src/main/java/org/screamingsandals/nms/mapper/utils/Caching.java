package org.screamingsandals.nms.mapper.utils;

import lombok.Data;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

@Data
public class Caching {
    private final Path cacheDirectory;
    private final HttpClient client = HttpClient.newHttpClient();

    public String loadData(URI uri, String cache) throws IOException, InterruptedException {
        return loadData(() -> uri, cache);
    }

    public String loadData(Supplier<URI> uri, String cache) throws IOException, InterruptedException {
        if (cache != null && Files.exists(cacheDirectory.resolve(cache))) {
            return Files.readString(cacheDirectory.resolve(cache));
        }

        var c = client.send(HttpRequest.newBuilder().uri(uri.get()).build(), HttpResponse.BodyHandlers.ofString()).body();
        if (cache != null) {
            if (!Files.exists(cacheDirectory)) {
                cacheDirectory.toFile().mkdirs();
            }

            Files.writeString(cacheDirectory.resolve(cache), c);
        }
        return c;
    }
}
