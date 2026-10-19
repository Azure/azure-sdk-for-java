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
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Offline tests verifying that uploading a {@link ByteBuffer} whose position is not zero uploads only the bytes between
 * its position and limit, both when uploaded in a single append and when split into multiple appends.
 */
public class DataLakePositionedBufferUploadTests {
    private static final String ENDPOINT = "https://account.dfs.core.windows.net";
    private static final long BLOCK_SIZE = 4L * Constants.MB;

    @ParameterizedTest
    @MethodSource("positionedBufferSupplier")
    public void uploadPositionedBuffer(int position, int remaining, int expectedAppends) {
        byte[] backing = new byte[position + remaining + 3];
        new Random(position + remaining).nextBytes(backing);
        ByteBuffer buffer = ByteBuffer.wrap(backing, position, remaining);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        // Content larger than the block size is split into multiple appends.
        ParallelTransferOptions options
            = new ParallelTransferOptions().setBlockSizeLong(BLOCK_SIZE).setMaxSingleUploadSizeLong(BLOCK_SIZE);
        fileClient(httpClient).uploadWithResponse(
            new FileParallelUploadOptions(Flux.just(buffer)).setParallelTransferOptions(options), null, null);

        assertEquals(expectedAppends, httpClient.getAppendCount());
        TestUtils.assertArraysEqual(Arrays.copyOfRange(backing, position, position + remaining),
            httpClient.getUploadedContent());
    }

    private static Stream<Arguments> positionedBufferSupplier() {
        return Stream.of(
            // position | remaining | expected appends
            Arguments.of(1, (int) BLOCK_SIZE, 1), Arguments.of(1, (int) BLOCK_SIZE + Constants.MB, 2),
            Arguments.of(Constants.MB + 7, 2 * (int) BLOCK_SIZE + 5, 3),
            // An exact multiple of the block size.
            Arguments.of(1, 2 * (int) BLOCK_SIZE, 2));
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
        private final Map<Long, byte[]> appends = new TreeMap<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String action = getQueryParameter(request, "action");
            int status = request.getHttpMethod() == HttpMethod.PUT ? 201 : "append".equals(action) ? 202 : 200;

            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody());
            return body.map(bytes -> {
                if ("append".equals(action)) {
                    synchronized (this) {
                        appends.put(Long.parseLong(getQueryParameter(request, "position")), bytes);
                    }
                }
                return new MockHttpResponse(request, status);
            });
        }

        synchronized int getAppendCount() {
            return appends.size();
        }

        /*
         * Gets the content of the file as it would be after the upload, from the appends ordered by their position.
         */
        synchronized byte[] getUploadedContent() {
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
