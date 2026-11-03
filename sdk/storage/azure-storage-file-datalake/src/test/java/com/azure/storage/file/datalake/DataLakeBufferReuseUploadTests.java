// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.test.shared.ByteBufferState;
import com.azure.storage.common.test.shared.TestByteBufferVariant;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Offline tests verifying that a Data Lake file upload split into multiple appends doesn't modify the caller's
 * buffers, not even their position or limit, so the same buffers can be uploaded again.
 */
public class DataLakeBufferReuseUploadTests {
    private static final String ENDPOINT = "https://account.dfs.core.windows.net";
    // Small sizes keep the tests fast; the staging logic doesn't depend on the absolute sizes.
    private static final int BLOCK_SIZE = 1024;
    // Smaller than a block, so appends span several buffers and buffers straddle append boundaries.
    private static final int PIECE_LENGTH = 700;

    @Test
    public void partitionedUploadDoesNotModifyCallerBuffers() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        List<ByteBufferState> sources = TestByteBufferVariant.mixed(content, PIECE_LENGTH);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadWithResponse(new FileParallelUploadOptions(Flux.fromIterable(buffersOf(sources)))
            .setParallelTransferOptions(blockOptions()), null, null);

        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        sources.forEach(ByteBufferState::assertUnchanged);
    }

    @Test
    public void uploadingSameBuffersAgainSendsSameContent() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        List<ByteBufferState> sources = TestByteBufferVariant.mixed(content, PIECE_LENGTH);
        List<ByteBuffer> buffers = buffersOf(sources);

        for (int upload = 1; upload <= 2; upload++) {
            RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

            fileClient(httpClient).uploadWithResponse(
                new FileParallelUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(blockOptions()),
                null, null);

            TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        }
        sources.forEach(ByteBufferState::assertUnchanged);
    }

    private static List<ByteBuffer> buffersOf(List<ByteBufferState> sources) {
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        return buffers;
    }

    private static ParallelTransferOptions blockOptions() {
        return new ParallelTransferOptions().setBlockSizeLong((long) BLOCK_SIZE)
            .setMaxSingleUploadSizeLong((long) BLOCK_SIZE);
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random(length).nextBytes(bytes);
        return bytes;
    }

    private static DataLakeFileClient fileClient(HttpClient httpClient) {
        return new DataLakeFileSystemClientBuilder().endpoint(ENDPOINT)
            .fileSystemName("filesystem")
            .credential(new MockTokenCredential())
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .buildClient()
            .getFileClient("file");
    }

    /*
     * Captures the content of each append by its position, answering create and flush requests. Reads request bodies
     * through duplicates, as HTTP clients do, so the mock itself never moves the caller's buffers.
     */
    private static final class RequestCapturingHttpClient implements HttpClient {
        private final Map<Long, byte[]> appends = new TreeMap<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String action = getQueryParameter(request, "action");
            int status = request.getHttpMethod() == HttpMethod.PUT ? 201 : "append".equals(action) ? 202 : 200;

            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody().map(ByteBuffer::duplicate));
            return body.map(bytes -> {
                if ("append".equals(action)) {
                    synchronized (this) {
                        appends.put(Long.parseLong(getQueryParameter(request, "position")), bytes);
                    }
                }
                return new MockHttpResponse(request, status);
            });
        }

        /*
         * Gets the content of the file as it would be after the upload, from the appends ordered by their position.
         */
        synchronized byte[] getFileContent() {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            for (Map.Entry<Long, byte[]> append : appends.entrySet()) {
                assertEquals(content.size(), append.getKey().longValue());
                content.write(append.getValue(), 0, append.getValue().length);
            }
            return content.toByteArray();
        }

        private static String getQueryParameter(HttpRequest request, String name) {
            String query = request.getUrl().getQuery();
            if (query == null) {
                return null;
            }
            for (String parameter : query.split("&")) {
                if (parameter.startsWith(name + "=")) {
                    return parameter.substring(name.length() + 1);
                }
            }
            return null;
        }
    }
}
