// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.FileUploadOptions;
import com.azure.ai.projects.models.ModelUploadOptions;
import com.azure.ai.projects.models.ModelVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUploadTests {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void uploadsForwardOptionsAndDoNotRegisterFailures(boolean async, @TempDir Path folder) throws IOException {
        Files.write(folder.resolve("weights.bin"), new byte[] { 1, 2 });
        Files.write(folder.resolve("excluded.txt"), new byte[] { 3 });
        for (boolean model : new boolean[] { false, true }) {
            for (boolean failUpload : new boolean[] { false, true }) {
                AtomicInteger projectCalls = new AtomicInteger();
                AtomicInteger uploadCalls = new AtomicInteger();
                HttpClient blob = request -> {
                    uploadCalls.incrementAndGet();
                    request.getBodyAsBinaryData().toBytes();
                    assertTrue(request.getUrl().getPath().endsWith("weights.bin"));
                    assertEquals("review", request.getHeaders().getValue("x-ms-meta-purpose"));
                    assertEquals("*", request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH));
                    return failUpload
                        ? Mono.error(new IllegalArgumentException("upload failed"))
                        : Mono.just(new MockHttpResponse(request, 201,
                            new HttpHeaders().set(HttpHeaderName.ETAG, "\"etag\""), new byte[0]));
                };
                AIProjectClientBuilder builder
                    = new AIProjectClientBuilder().endpoint("https://localhost/projects/test")
                        .pipeline(new HttpPipelineBuilder().httpClient(request -> {
                            assertTrue(request.getHttpMethod() != HttpMethod.GET, "Waiting must be disabled");
                            int call = projectCalls.incrementAndGet();
                            if (call == 1) {
                                return Mono.just(jsonResponse(request, 200, pendingResponse()));
                            }
                            assertEquals(2, call);
                            assertEquals(1, uploadCalls.get());
                            assertTrue(!request.getBodyAsBinaryData().toString().contains("sig="));
                            return Mono.just(jsonResponse(request, model ? 202 : 201,
                                model ? "{}" : request.getBodyAsBinaryData().toString()));
                        }).build());
                FileUploadOptions upload = new FileUploadOptions().setFilePattern(Pattern.compile("\\.bin$"))
                    .setBlobClientConfiguration(client -> client.httpClient(blob))
                    .setBlobUploadConfiguration(
                        options -> options.setMetadata(Collections.singletonMap("purpose", "review"))
                            .setRequestConditions(
                                new com.azure.storage.blob.models.BlobRequestConditions().setIfNoneMatch("*")));
                Runnable action = () -> {
                    if (model) {
                        ModelUploadOptions options
                            = new ModelUploadOptions().setFileUploadOptions(upload).setWaitForCompletion(false);
                        Path file = folder.resolve("weights.bin");
                        ModelVersion submitted = async
                            ? builder.beta()
                                .buildBetaModelsAsyncClient()
                                .createModel("model", "1", file, options)
                                .block(Duration.ofSeconds(5))
                            : builder.beta().buildBetaModelsClient().createModel("model", "1", file, options);
                        assertNotNull(submitted);
                        assertEquals("https://storage.example/container", submitted.getBlobUrl());
                    } else if (async) {
                        assertNotNull(builder.buildDatasetsAsyncClient()
                            .createDatasetWithFolder("dataset", "1", folder, null, upload)
                            .block(Duration.ofSeconds(5)));
                    } else {
                        assertNotNull(builder.buildDatasetsClient()
                            .createDatasetWithFolder("dataset", "1", folder, null, upload));
                    }
                };
                if (failUpload) {
                    assertThrows(IllegalArgumentException.class, action::run);
                } else {
                    action.run();
                }
                assertEquals(failUpload ? 1 : 2, projectCalls.get());
                assertEquals(1, uploadCalls.get());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void modelUploadRegistersMetadataAndWaits(boolean async, @TempDir Path folder) throws IOException {
        Files.createDirectories(folder.resolve("nested"));
        Files.write(folder.resolve("nested/model.bin"), new byte[] { 1, 2, 3 });
        Files.write(folder.resolve("excluded.txt"), new byte[] { 4 });
        List<HttpRequest> uploads = new ArrayList<>();
        AtomicReference<Map<?, ?>> registered = new AtomicReference<>();
        AtomicInteger polls = new AtomicInteger();
        AtomicInteger calls = new AtomicInteger();
        HttpClient blobClient = request -> {
            request.getBodyAsBinaryData().toBytes();
            uploads.add(request);
            return Mono.just(new MockHttpResponse(request, 201, new HttpHeaders().set(HttpHeaderName.ETAG, "\"etag\""),
                new byte[0]));
        };
        HttpClient projectClient = request -> {
            int call = calls.incrementAndGet();
            if (call == 1) {
                String pending = pendingResponse();
                return Mono.just(jsonResponse(request, 200,
                    async
                        ? pending.replace("blobReference", "blobReferenceForConsumption")
                            .replace("pendingUploadId", "temporaryDataReferenceId")
                        : pending));
            }
            if (request.getHttpMethod() != HttpMethod.GET) {
                assertEquals(1, uploads.size());
                registered.set(request.getBodyAsBinaryData().toObject(Map.class));
                return Mono.just(jsonResponse(request, 202, "{}"));
            }
            if (polls.incrementAndGet() == 1) {
                return Mono.just(jsonResponse(request, 404, "{\"error\":{\"code\":\"NotFound\"}}"));
            }
            return Mono.just(jsonResponse(request, 200,
                "{\"blobUri\":\"https://storage.example/container\",\"name\":\"model\",\"version\":\"1\"}"));
        };
        FileUploadOptions upload = new FileUploadOptions().setFilePattern(Pattern.compile("\\.bin$"))
            .setBlobClientConfiguration(builder -> builder.httpClient(blobClient))
            .setBlobUploadConfiguration(options -> options.setMetadata(Collections.singletonMap("purpose", "model")));
        ModelUploadOptions options = new ModelUploadOptions().setFileUploadOptions(upload)
            .setDescription("description")
            .setBaseModel("base")
            .setTags(Collections.singletonMap("tag", "value"))
            .setPollInterval(Duration.ofMillis(1))
            .setTimeout(Duration.ofSeconds(5));
        AIProjectClientBuilder builder = new AIProjectClientBuilder().endpoint("https://localhost/projects/test")
            .pipeline(new HttpPipelineBuilder().httpClient(projectClient).build());
        ModelVersion model = async
            ? builder.beta()
                .buildBetaModelsAsyncClient()
                .createModel("model", "1", folder, options)
                .block(Duration.ofSeconds(10))
            : builder.beta().buildBetaModelsClient().createModel("model", "1", folder, options);
        assertNotNull(model);
        assertEquals("model", model.getName());
        assertEquals(2, polls.get());
        assertEquals("/container/nested/model.bin", java.net.URI.create(uploads.get(0).getUrl().toString()).getPath());
        assertEquals("model", uploads.get(0).getHeaders().getValue("x-ms-meta-purpose"));
        assertTrue(uploads.get(0).getUrl().getQuery().contains("sig="));
        assertEquals("https://storage.example/container", registered.get().get("blobUri"));
        assertEquals("description", registered.get().get("description"));
        assertEquals("base", registered.get().get("baseModel"));
        assertEquals(Collections.singletonMap("tag", "value"), registered.get().get("tags"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void invalidModelSourceNeverRequestsStorage(boolean async, @TempDir Path folder) throws IOException {
        AIProjectClientBuilder builder = new AIProjectClientBuilder().endpoint("https://localhost")
            .httpClient(request -> Mono.error(new AssertionError("Unexpected HTTP request")));
        Path emptyFile = Files.createFile(folder.resolve("empty.bin"));
        for (Path source : new Path[] {
            folder.resolve("missing"),
            emptyFile,
            Files.createDirectory(folder.resolve("empty")) }) {
            assertThrows(IllegalArgumentException.class, () -> {
                if (async) {
                    builder.beta()
                        .buildBetaModelsAsyncClient()
                        .createModel("model", "1", source, null)
                        .block(Duration.ofSeconds(5));
                } else {
                    builder.beta().buildBetaModelsClient().createModel("model", "1", source, null);
                }
            });
        }
        assertThrows(IllegalArgumentException.class, () -> new ModelUploadOptions().setTimeout(Duration.ZERO));
    }

    private static String pendingResponse() {
        return "{\"pendingUploadId\":\"upload\",\"blobReference\":{\"blobUri\":\"https://storage.example/container\","
            + "\"storageAccountArmId\":\"storage\",\"credential\":{\"type\":\"SAS\","
            + "\"sasUri\":\"https://storage.example/container?sv=2024-11-04&sr=c&sig=fake\"}}}";
    }

    private static MockHttpResponse jsonResponse(HttpRequest request, int status, String body) {
        return new MockHttpResponse(request, status,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
            body.getBytes(StandardCharsets.UTF_8));
    }
}
