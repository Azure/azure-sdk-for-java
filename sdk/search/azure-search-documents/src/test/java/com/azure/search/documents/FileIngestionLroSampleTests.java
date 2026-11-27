// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileIngestionLroSampleTests {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void lifecycleReplacesFileAndCleansOwnedSourceEvenWhenIngestionFails(boolean fail) {
        AtomicInteger ingestions = new AtomicInteger();
        AtomicBoolean fileDeleted = new AtomicBoolean();
        AtomicBoolean sourceDeleted = new AtomicBoolean();
        SearchIndexClient client = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                String path = request.getUrl().getPath();
                if (request.getHttpMethod() == HttpMethod.DELETE) {
                    if (path.contains("/files(")) {
                        fileDeleted.set(true);
                    } else {
                        sourceDeleted.set(true);
                    }
                    return Mono.just(response(request, 204, ""));
                }
                if (request.getHttpMethod() == HttpMethod.POST && path.endsWith(":ingest")) {
                    int number = ingestions.incrementAndGet();
                    String body = request.getBodyAsBinaryData().toString();
                    assertTrue(body.contains("\"version\":\"" + number + "\""));
                    assertEquals(number == 1
                        ? "/knowledgesources('owned-source')/files:ingest"
                        : "/knowledgesources('owned-source')/files('file-1'):ingest", path);
                    return Mono.just(new MockHttpResponse(request, 202,
                        new HttpHeaders().set("Operation-Location",
                            "https://example.search.windows.net/knowledgesources('owned-source')/files/operations/op-"
                                + number + "?api-version=2026-11-01-preview"),
                        operation(number, false).getBytes(StandardCharsets.UTF_8)));
                }
                if (request.getHttpMethod() == HttpMethod.POST) {
                    return Mono.just(response(request, 201, request.getBodyAsBinaryData().toString()));
                }
                if (path.endsWith("/files/operations")) {
                    return Mono.just(response(request, 200, "{\"value\":[" + operation(2, false) + "]}"));
                }
                if (path.contains("/operations/")) {
                    return Mono.just(response(request, 200, operation(ingestions.get(), fail)));
                }
                String files = fileDeleted.get() ? "" : file(2);
                return Mono.just(response(request, 200, "{\"value\":[" + files + "]}"));
            })
            .buildClient();
        if (fail) {
            IllegalStateException error = assertThrows(IllegalStateException.class, () -> FileIngestionLroPreviewExample
                .run(client, "owned-source", Duration.ofSeconds(5), Duration.ofMillis(1)));
            assertTrue(error.getMessage().contains("ExtractionFailed"));
            assertEquals(1, ingestions.get());
        } else {
            FileIngestionLroPreviewExample.run(client, "owned-source", Duration.ofSeconds(5), Duration.ofMillis(1));
            assertEquals(2, ingestions.get());
            assertTrue(fileDeleted.get());
        }
        assertTrue(sourceDeleted.get(), "Only a successfully created source is registered for cleanup.");
    }

    private static String operation(int version, boolean fail) {
        return "{\"id\":\"op-" + version + "\",\"kind\":\"knowledgeSourceFileIngestion\",\"status\":\""
            + (fail ? "failed" : "succeeded") + "\","
            + (fail
                ? "\"error\":{\"code\":\"ExtractionFailed\",\"message\":\"failed\"}"
                : "\"result\":" + file(version))
            + "}";
    }

    private static String file(int version) {
        return "{\"fileId\":\"file-1\",\"fileName\":\"notes/hotel.txt\",\"metadata\":{\"version\":\"" + version
            + "\"}}";
    }

    private static MockHttpResponse response(HttpRequest request, int status, String body) {
        return new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8));
    }
}
