// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests verifying that, when not explicitly configured, the initial transfer of a partitioned upload is the
 * same size as the subsequent transfers (the default block size).
 */
public class DataLakePartitionedTransferDefaultsTests {
    private static final String ENDPOINT = "https://account.dfs.core.windows.net";
    private static final int DEFAULT_BLOCK_SIZE = 4 * Constants.MB;

    @Test
    public void defaultMaxSingleUploadSizeIsDefaultBlockSize() {
        assertEquals(DEFAULT_BLOCK_SIZE, ModelHelper.FILE_DEFAULT_MAX_SINGLE_UPLOAD_SIZE);
        ParallelTransferOptions defaults = ModelHelper.populateAndApplyDefaults(null);
        assertEquals(DEFAULT_BLOCK_SIZE, defaults.getBlockSizeLong());
        assertEquals(DEFAULT_BLOCK_SIZE, defaults.getMaxSingleUploadSizeLong());

        // Explicitly set values are respected.
        long explicit = 100L * Constants.MB;
        assertEquals(explicit,
            ModelHelper.populateAndApplyDefaults(new ParallelTransferOptions().setMaxSingleUploadSizeLong(explicit))
                .getMaxSingleUploadSizeLong());
    }

    @ParameterizedTest
    @MethodSource("uploadSupplier")
    public void upload(long dataSize, ParallelTransferOptions options, int expectedAppends) {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient)
            .uploadWithResponse(new FileParallelUploadOptions(BinaryData.fromBytes(new byte[(int) dataSize]))
                .setParallelTransferOptions(options), null, null);

        assertUploadRequests(httpClient, dataSize, expectedAppends);
    }

    @ParameterizedTest
    @MethodSource("uploadSupplier")
    public void uploadFromFile(long dataSize, ParallelTransferOptions options, int expectedAppends,
        @TempDir Path tempDir) throws IOException {
        Path file = Files.write(tempDir.resolve("upload"), new byte[(int) dataSize]);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadFromFile(file.toString(), options, null, null, null, null);

        assertUploadRequests(httpClient, dataSize, expectedAppends);
    }

    private static Stream<Arguments> uploadSupplier() {
        long maxSingleUploadSize = 100L * Constants.MB;
        return Stream.of(
            // Default options: content up to the default block size is uploaded in a single append.
            Arguments.of(DEFAULT_BLOCK_SIZE - 1, null, 1), Arguments.of(DEFAULT_BLOCK_SIZE, null, 1),
            // Default options: content larger than the default block size is partitioned.
            Arguments.of(DEFAULT_BLOCK_SIZE + 1, null, 2),
            Arguments.of(DEFAULT_BLOCK_SIZE + 1, new ParallelTransferOptions(), 2),
            // An explicitly set max single upload size is still respected.
            Arguments.of(DEFAULT_BLOCK_SIZE + 1,
                new ParallelTransferOptions().setMaxSingleUploadSizeLong(maxSingleUploadSize), 1));
    }

    @Test
    public void uploadFromFileNoOverwriteChecksExistenceOnlyWhenPartitioned(@TempDir Path tempDir) throws IOException {
        Path smallFile = Files.write(tempDir.resolve("small"), new byte[DEFAULT_BLOCK_SIZE]);
        RequestCapturingHttpClient smallClient = new RequestCapturingHttpClient();
        fileClient(smallClient).uploadFromFile(smallFile.toString(), false);
        assertFalse(smallClient.hasRequest(HttpMethod.HEAD));
        assertUploadRequests(smallClient, DEFAULT_BLOCK_SIZE, 1);

        Path largeFile = Files.write(tempDir.resolve("large"), new byte[DEFAULT_BLOCK_SIZE + 1]);
        RequestCapturingHttpClient largeClient = new RequestCapturingHttpClient();
        fileClient(largeClient).uploadFromFile(largeFile.toString(), false);
        assertTrue(largeClient.hasRequest(HttpMethod.HEAD));
        assertUploadRequests(largeClient, DEFAULT_BLOCK_SIZE + 1, 2);
    }

    private static void assertUploadRequests(RequestCapturingHttpClient httpClient, long dataSize,
        int expectedAppends) {
        List<HttpRequest> requests = httpClient.getRequests();
        assertEquals(1, requests.stream().filter(r -> r.getHttpMethod() == HttpMethod.PUT).count());
        assertEquals(expectedAppends, requests.stream().filter(r -> queryContains(r, "action=append")).count());
        assertEquals(1, requests.stream().filter(r -> queryContains(r, "action=flush")).count());
        assertEquals(dataSize, httpClient.getUploadedBytes());
    }

    private static boolean queryContains(HttpRequest request, String value) {
        String query = request.getUrl().getQuery();
        return query != null && query.contains(value);
    }

    private static DataLakeFileClient fileClient(HttpClient httpClient) {
        return new DataLakeFileSystemClientBuilder().endpoint(ENDPOINT)
            .fileSystemName("filesystem")
            .credential(new MockTokenCredential())
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .buildClient()
            .getFileClient("file");
    }

    private static final class RequestCapturingHttpClient implements HttpClient {
        private final List<HttpRequest> requests = new ArrayList<>();
        private long uploadedBytes;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            synchronized (this) {
                requests.add(request);
            }

            if (request.getHttpMethod() == HttpMethod.HEAD) {
                HttpHeaders headers
                    = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-error-code"), "BlobNotFound");
                return Mono.just(new MockHttpResponse(request, 404, headers));
            }

            int status
                = request.getHttpMethod() == HttpMethod.PUT ? 201 : queryContains(request, "action=append") ? 202 : 200;
            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody());
            return body.map(bytes -> {
                synchronized (this) {
                    if (queryContains(request, "action=append")) {
                        uploadedBytes += bytes.length;
                    }
                }
                return new MockHttpResponse(request, status);
            });
        }

        synchronized List<HttpRequest> getRequests() {
            return new ArrayList<>(requests);
        }

        synchronized long getUploadedBytes() {
            return uploadedBytes;
        }

        synchronized boolean hasRequest(HttpMethod method) {
            return requests.stream().anyMatch(r -> r.getHttpMethod() == method);
        }
    }
}
