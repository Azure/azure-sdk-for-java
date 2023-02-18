// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobAsyncClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClientBuilder;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ContainerRegistryBlobClientTests {

    private static final BinaryData SMALL_CONTENT = BinaryData.fromString("foobar");
    private static final String SMALL_CONTENT_SHA256 = "sha256:c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2";
    @Test
    public void downloadBlobWrongDigestInHeaderSync() {
        ContainerRegistryBlobClient client = createSyncClient(createClient(SMALL_CONTENT, "wrong-digest"));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertThrows(ServiceResponseException.class, () -> client.downloadStream("some-digest", Channels.newChannel(stream)));
    }

    @Test
    public void downloadBlobWrongDigestInHeaderAsync() {
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClient(SMALL_CONTENT, "wrong-digest"));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient.downloadStream("some-digest")
                .flatMap(response -> response.writeValueTo(Channels.newChannel(stream))))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @Test
    public void downloadBlobWrongResponseSync() {
        ContainerRegistryBlobClient client = createSyncClient(createClient(SMALL_CONTENT, "some-digest"));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertThrows(ServiceResponseException.class, () -> client.downloadStream("some-digest", Channels.newChannel(stream)));
    }

    @Test
    public void downloadBlobWrongResponseAsync() {
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClient(SMALL_CONTENT, "some-digest"));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient.downloadStream("some-digest")
                .flatMap(response -> response.writeValueTo(Channels.newChannel(stream))))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @SyncAsyncTest
    public void downloadBlobOneChunk() throws IOException {
        ContainerRegistryBlobClient client = createSyncClient(createClient(SMALL_CONTENT, SMALL_CONTENT_SHA256));
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClient(SMALL_CONTENT, SMALL_CONTENT_SHA256));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        WritableByteChannel channel = Channels.newChannel(stream);
        SyncAsyncExtension.execute(
            () -> client.downloadStream(SMALL_CONTENT_SHA256, Channels.newChannel(stream)),
            () -> asyncClient.downloadStream(SMALL_CONTENT_SHA256)
                .flatMap(result -> result.writeValueTo(channel)));
        stream.flush();
        assertArrayEquals(SMALL_CONTENT.toBytes(), stream.toByteArray());
    }

    @SyncAsyncTest
    public void downloadBlobMultipleChunks() throws IOException, NoSuchAlgorithmException {
        byte[] bytes = new byte[(int) (CHUNK_SIZE * 1.3)];
        new Random().nextBytes(bytes);
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(bytes);

        String expectedDigest = "sha256:" + UtilsImpl.byteArrayToHex(sha256.digest());
        BinaryData content = BinaryData.fromBytes(bytes);

        ContainerRegistryBlobClient client = createSyncClient(createClient(content, expectedDigest));
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClient(content, expectedDigest));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        WritableByteChannel channel = Channels.newChannel(stream);
        SyncAsyncExtension.execute(
            () -> client.downloadStream(expectedDigest, Channels.newChannel(stream)),
            () -> asyncClient.downloadStream(expectedDigest)
                .flatMap(result -> result.writeValueTo(channel)));
        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    public static HttpClient createClient(BinaryData content, String digest) {
        InputStream contentStream = content.toStream();
        AtomicLong expectedStartPosition = new AtomicLong(0);
        long contentLength = content.getLength();
        return new MockHttpClient(request -> {
            long start = expectedStartPosition.get();
            long end = Math.min(start + CHUNK_SIZE, contentLength);

            if (start >= contentLength) {
                fail("Got download chunk on completed blob");
            }

            HttpRange expectedRange = new HttpRange(start, (long) CHUNK_SIZE);
            assertEquals(expectedRange.toString(), request.getHeaders().getValue(HttpHeaderName.RANGE));

            byte[] response = new byte[(int) (end - start)];
            try {
                contentStream.read(response);
            } catch (IOException e) {
                fail(e);
            }

            HttpHeaders headers = new HttpHeaders()
                .add("Content-Range", String.format("bytes %s-%s/%s", start, end, contentLength))
                .add(UtilsImpl.DOCKER_DIGEST_HEADER_NAME, digest);

            expectedStartPosition.set(start + CHUNK_SIZE);
            return new MockHttpResponse(request, 206, headers, response);
        });
    }

    private ContainerRegistryBlobClient createSyncClient(HttpClient httpClient) {
        return new ContainerRegistryBlobClientBuilder()
            .endpoint("https://endpoint.com")
            .repository("foo")
            .httpClient(httpClient)
            .buildClient();
    }

    private ContainerRegistryBlobAsyncClient createAsyncClient(HttpClient httpClient) {
        return new ContainerRegistryBlobClientBuilder()
            .endpoint("https://endpoint.com")
            .repository("foo")
            .httpClient(httpClient)
            .buildAsyncClient();
    }

    static class MockHttpClient implements HttpClient {
        private final Function<HttpRequest, HttpResponse> requestToResponse;
        MockHttpClient(Function<HttpRequest, HttpResponse> requestToResponse) {
            this.requestToResponse = requestToResponse;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            return Mono.just(requestToResponse.apply(httpRequest));
        }

        @Override
        public HttpResponse sendSync(HttpRequest httpRequest, Context context) {
            return requestToResponse.apply(httpRequest);
        }
    }
}
