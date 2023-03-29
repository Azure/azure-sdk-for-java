// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.models.GetManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.containers.containerregistry.TestUtils.DIGEST_UNKNOWN;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.OCI_INDEX_MEDIA_TYPE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOCKER_DIGEST_HEADER_NAME;
import static com.azure.core.util.CoreUtils.bytesToHexString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ContainerRegistryContentClientTests {
    private static final BinaryData SMALL_CONTENT = BinaryData.fromString("foobar");
    private static final String SMALL_CONTENT_SHA256 = "sha256:c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2";
    private static final String DEFAULT_MANIFEST_CONTENT_TYPE = "*/*," + ManifestMediaType.OCI_MANIFEST + "," + ManifestMediaType.DOCKER_MANIFEST + ",application/vnd.oci.image.index.v1+json"
        + ",application/vnd.docker.distribution.manifest.list.v2+json,application/vnd.cncf.oras.artifact.manifest.v1+json";
    private static final BinaryData MANIFEST_DATA = BinaryData.fromObject(MANIFEST);

    private static final BinaryData OCI_INDEX = BinaryData.fromString("{\"schemaVersion\":2,\"mediaType\":\"application/vnd.oci.image.index.v1+json\","
                                            + "\"manifests\":[{\"mediaType\":\"application/vnd.oci.image.manifest.v1+json\",\"size\":7143,\"digest\":\"sha256:e692418e4cbaf90ca69d05a66403747baa33ee08806650b51fab815ad7fc331f\","
                                            + "\"platform\":{\"architecture\":\"ppc64le\",\"os\":\"linux\"}}]}");

    private static final String OCI_INDEX_DIGEST = "sha256:226bb568af1e417421f2f8053cd2847bdb5fcc52c7ed93823abdb595881c2b02";

    private static final Random RANDOM = new Random(42);
    private static final byte[] CHUNK = new byte[CHUNK_SIZE];

    static {
        RANDOM.nextBytes(CHUNK);
    }

    private MessageDigest sha256;
    private Supplier<String> calculateDigest = () -> "sha256:" + bytesToHexString(sha256.digest());

    @BeforeEach
    void beforeEach() {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            fail(e);
        }
    }

    @Test
    public void downloadBlobWrongDigestInHeaderSync() {
        ContainerRegistryContentClient client = createSyncClient(createDownloadContentClient(SMALL_CONTENT, DIGEST_UNKNOWN));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertThrows(ServiceResponseException.class, () -> client.downloadStream("some-digest", Channels.newChannel(stream)));
    }

    @Test
    public void downloadManigestWrongDigestInHeaderSync() {
        ContainerRegistryContentClient client = createSyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));
        assertThrows(ServiceResponseException.class, () -> client.getManifest("latest"));
    }

    @Test
    public void downloadBlobWrongDigestInHeaderAsync() {
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createDownloadContentClient(SMALL_CONTENT, DIGEST_UNKNOWN));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient.downloadStream("some-digest")
                .flatMap(response -> FluxUtil.writeToOutputStream(response.toFluxByteBuffer(), stream)))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @Test
    public void getManifestWrongDigestInHeaderAsync() {
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));

        StepVerifier.create(asyncClient.getManifest("latest"))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @Test
    public void downloadBlobWrongResponseSync() {
        ContainerRegistryContentClient client = createSyncClient(createDownloadContentClient(SMALL_CONTENT, DIGEST_UNKNOWN));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertThrows(ServiceResponseException.class, () -> client.downloadStream(SMALL_CONTENT_SHA256, Channels.newChannel(stream)));
    }

    @Test
    public void getManifestWrongResponseSync() {
        ContainerRegistryContentClient client = createSyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));
        assertThrows(ServiceResponseException.class, () -> client.getManifest("latest"));
    }

    @Test
    public void downloadBlobWrongResponseAsync() {
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createDownloadContentClient(SMALL_CONTENT, DIGEST_UNKNOWN));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient.downloadStream(SMALL_CONTENT_SHA256)
                .flatMap(response -> FluxUtil.writeToOutputStream(response.toFluxByteBuffer(), stream)))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @Test
    public void getManifestWrongResponseAsync() {
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));
        StepVerifier.create(asyncClient.getManifest("latest"))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @SyncAsyncTest
    public void getManifest() {
        ContainerRegistryContentClient client = createSyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, null));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, null));

        GetManifestResult result = SyncAsyncExtension.execute(
            () -> client.getManifest(MANIFEST_DIGEST),
            () -> asyncClient.getManifest(MANIFEST_DIGEST));

        assertArrayEquals(MANIFEST_DATA.toBytes(), result.getManifest().toBytes());
        assertNotNull(result.getManifest().toObject(ManifestMediaType.class));
        assertEquals(ManifestMediaType.OCI_MANIFEST, result.getManifestMediaType());
    }

    @SyncAsyncTest
    public void getManifestWithDockerType() {
        ContainerRegistryContentClient client = createSyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, ManifestMediaType.DOCKER_MANIFEST));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, ManifestMediaType.DOCKER_MANIFEST));

        Response<GetManifestResult> result = SyncAsyncExtension.execute(
            () -> client.getManifestWithResponse(MANIFEST_DIGEST, Context.NONE),
            () -> asyncClient.getManifestWithResponse(MANIFEST_DIGEST));

        assertArrayEquals(MANIFEST_DATA.toBytes(), result.getValue().getManifest().toBytes());
        assertNotNull(result.getValue().getManifest().toObject(OciImageManifest.class));
        assertEquals(ManifestMediaType.DOCKER_MANIFEST, result.getValue().getManifestMediaType());
    }

    @SyncAsyncTest
    public void getManifestWithOciIndexType() {
        ContainerRegistryContentClient client = createSyncClient(createClientManifests(OCI_INDEX, OCI_INDEX_DIGEST, OCI_INDEX_MEDIA_TYPE));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createClientManifests(OCI_INDEX, OCI_INDEX_DIGEST, OCI_INDEX_MEDIA_TYPE));

        Response<GetManifestResult> result = SyncAsyncExtension.execute(
            () -> client.getManifestWithResponse(OCI_INDEX_DIGEST, Context.NONE),
            () -> asyncClient.getManifestWithResponse(OCI_INDEX_DIGEST));

        assertArrayEquals(OCI_INDEX.toBytes(), result.getValue().getManifest().toBytes());
        assertEquals(OCI_INDEX_MEDIA_TYPE, result.getValue().getManifestMediaType());
    }

    @SyncAsyncTest
    public void downloadBlobOneChunk() throws IOException {
        ContainerRegistryContentClient client = createSyncClient(createDownloadContentClient(SMALL_CONTENT, SMALL_CONTENT_SHA256));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createDownloadContentClient(SMALL_CONTENT, SMALL_CONTENT_SHA256));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        SyncAsyncExtension.execute(
            () -> client.downloadStream(SMALL_CONTENT_SHA256, Channels.newChannel(stream)),
            () -> asyncClient.downloadStream(SMALL_CONTENT_SHA256)
                .flatMap(result ->  FluxUtil.writeToOutputStream(result.toFluxByteBuffer(), stream)));
        stream.flush();
        assertArrayEquals(SMALL_CONTENT.toBytes(), stream.toByteArray());
    }

    @SyncAsyncTest
    public void downloadBlobMultipleChunks() throws IOException {
        BinaryData content = getDataSync((int) (CHUNK_SIZE * 2.3), sha256);
        String expectedDigest = "sha256:" + bytesToHexString(sha256.digest());

        ContainerRegistryContentClient client = createSyncClient(createDownloadContentClient(content, expectedDigest));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createDownloadContentClient(content, expectedDigest));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        WritableByteChannel channel = Channels.newChannel(stream);
        SyncAsyncExtension.execute(
            () -> client.downloadStream(expectedDigest, Channels.newChannel(stream)),
            () -> asyncClient.downloadStream(expectedDigest)
                .flatMap(result -> FluxUtil.writeToWritableByteChannel(result.toFluxByteBuffer(), channel)));
        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    @SyncAsyncTest
    public void uploadBlobSmall() {
        BinaryData content = getDataSync((int) (CHUNK_SIZE * 0.1), sha256);

        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));
        SyncAsyncExtension.execute(
            () -> client.uploadBlob(content),
            () -> asyncClient.uploadBlob(content));
    }

    @SyncAsyncTest
    public void uploadBlobSmallChunks() {
        long length = CHUNK_SIZE * 2;
        Flux<ByteBuffer> content = getDataAsync(length, CHUNK_SIZE / 10, sha256);
        BinaryData data = BinaryData.fromFlux(content, length, true).block();
        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));
        SyncAsyncExtension.execute(
            () -> client.uploadBlob(data),
            () -> asyncClient.uploadBlob(data));
    }

    @SyncAsyncTest
    public void uploadBlobBigChunks() {
        long length = CHUNK_SIZE * 2;
        Flux<ByteBuffer> content = getDataAsync(CHUNK_SIZE * 2, (int) (CHUNK_SIZE * 1.5), sha256);
        BinaryData data = BinaryData.fromFlux(content, length, true).block();

        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));
        SyncAsyncExtension.execute(
            () -> client.uploadBlob(data),
            () -> asyncClient.uploadBlob(data));
    }

    private ByteBuffer slice(ByteBuffer buffer, int offset, int length) {
        // make Java8 happy
        Buffer limited = buffer.duplicate().position(offset).limit(offset + length);
        return (ByteBuffer) limited;
    }

    @Test
    public void uploadVariableChunkSize() {
        ByteBuffer buf = ByteBuffer.wrap(CHUNK);
        Flux<ByteBuffer> content = Flux.create(sink -> {
            sink.next(slice(buf, 0, 1));
            sink.next(slice(buf, 1, 100));
            sink.next(slice(buf, 101, 1000));
            sink.complete();
        });

        ByteBuffer full = slice(buf, 0, 1101);
        sha256.update(full.asReadOnlyBuffer());

        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));
        StepVerifier.create(BinaryData.fromFlux(content).flatMap(c -> asyncClient.uploadBlob(c)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void uploadFailWhileReadingInput() {
        Flux<ByteBuffer> content = Flux.create(sink -> {
            byte[] data = new byte[100];
            new Random().nextBytes(data);

            sink.next(ByteBuffer.wrap(data));
            sink.error(new IllegalStateException("foo"));
        });

        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(() -> "foo"));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(() -> "foo"));
        assertThrows(IllegalStateException.class, () -> client.uploadBlob(BinaryData.fromFlux(content).block()));

        StepVerifier.create(BinaryData.fromFlux(content).flatMap(c -> asyncClient.uploadBlob(c)))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    public void uploadFromFile() throws IOException {
        File input = File.createTempFile("temp", "in");
        Files.write(input.toPath(), CHUNK, StandardOpenOption.APPEND);
        sha256.update(CHUNK);

        byte[] secondChunk = Arrays.copyOfRange(CHUNK, 0, 100);
        Files.write(input.toPath(), secondChunk, StandardOpenOption.APPEND);
        sha256.update(secondChunk);

        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));

        StepVerifier.create(asyncClient.uploadBlob(BinaryData.fromFile(input.toPath())))
            .expectNextCount(1)
            .verifyComplete();
    }

    @SyncAsyncTest
    public void uploadFromStream() throws IOException {
        byte[] data = Arrays.copyOfRange(CHUNK, 0, 1024 * 100);
        sha256.update(data);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            BinaryData content = BinaryData.fromStream(stream, (long) data.length).toReplayableBinaryData();

            ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest));
            ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));
            SyncAsyncExtension.execute(
                () -> client.uploadBlob(content),
                () -> asyncClient.uploadBlob(content));
        }
    }

    @SyncAsyncTest
    public void uploadFails() {
        Flux<ByteBuffer> flux = Flux.create(sink -> {
            sha256.update(CHUNK);
            sink.next(ByteBuffer.wrap(CHUNK));

            sha256.update(CHUNK);
            sink.next(ByteBuffer.wrap(CHUNK));

            sink.complete();
        });

        BiFunction<HttpRequest, Integer, HttpResponse> onChunk = (r, c) -> {
            if (c == 3) {
                HttpHeaders responseHeaders = new HttpHeaders().add("Content-Type", String.valueOf("application/json"));
                String error = "{\"errors\":[{\"code\":\"BLOB_UPLOAD_INVALID\",\"message\":\"blob upload invalid\"}]}";
                return new MockHttpResponse(r, 404, responseHeaders, error.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        };
        Supplier<String> calculateDigest = () -> "sha256:" + bytesToHexString(sha256.digest());
        BinaryData content = BinaryData.fromFlux(flux, (long) CHUNK_SIZE * 2, false).block();

        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest, onChunk));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest, onChunk));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> SyncAsyncExtension.execute(
            () -> client.uploadBlob(content),
            () -> asyncClient.uploadBlob(content)));

        assertAcrException(ex, "BLOB_UPLOAD_INVALID");
    }

    private void assertAcrException(Exception ex, String code) {
        assertInstanceOf(AcrErrorsException.class, ex.getCause());
        AcrErrorsException acrErrors = (AcrErrorsException) ex.getCause();
        assertEquals(1, acrErrors.getValue().getErrors().size());
        assertEquals(code, acrErrors.getValue().getErrors().get(0).getCode());
    }

    public void uploadFromFlux() {
        byte[] data = Arrays.copyOfRange(CHUNK, 0, 1024 * 100);
        sha256.update(data);
        Flux<ByteBuffer> flux = Flux.create(sink -> {
            sink.next(ByteBuffer.wrap(data));
            sink.complete();
        });

        BinaryData content = BinaryData.fromFlux(flux).block();

        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));
        SyncAsyncExtension.execute(
            () -> client.uploadBlob(content),
            () -> asyncClient.uploadBlob(content));
    }

    @Test
    public void uploadFromStreamNotReplayableThrows() throws IOException {
        byte[] data = Arrays.copyOfRange(CHUNK, 0, 1024 * 100);
        Supplier<String> calculateDigest = () -> "";
        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            BinaryData content = BinaryData.fromStream(stream, (long) data.length);

            assertThrows(IllegalArgumentException.class, () -> client.uploadBlob(content));
            StepVerifier.create(asyncClient.uploadBlob(content))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    @Test
    public void uploadFromFluxNotReplayable() throws IOException {
        Flux<ByteBuffer> flux = Flux.create(sink -> {
            byte[] data = new byte[100];
            new Random().nextBytes(data);
            sink.next(ByteBuffer.wrap(data));
            sink.complete();
        });

        ContainerRegistryContentClient client = createSyncClient(createUploadContentClient(calculateDigest));
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createUploadContentClient(calculateDigest));

        BinaryData content = BinaryData.fromFlux(flux, null, false).block();
        assertThrows(IllegalArgumentException.class, () -> client.uploadBlob(content));

        StepVerifier.create(asyncClient.uploadBlob(content))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    public void downloadToFile() throws IOException {
        File output = File.createTempFile("temp", "in");
        ContainerRegistryContentAsyncClient asyncClient = createAsyncClient(createDownloadContentClient(SMALL_CONTENT, SMALL_CONTENT_SHA256));
        try (FileOutputStream outputStream = new FileOutputStream(output)) {
            StepVerifier.create(asyncClient.downloadStream(SMALL_CONTENT_SHA256)
                    .flatMap(result -> FluxUtil.writeToOutputStream(result.toFluxByteBuffer(), outputStream)))
                .verifyComplete();

            outputStream.flush();
            assertArrayEquals(SMALL_CONTENT.toBytes(), Files.readAllBytes(output.toPath()));
        }
    }

    public static HttpClient createDownloadContentClient(BinaryData content, String digest) {
        try (InputStream contentStream = content.toStream()) {
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
        } catch (IOException e) {
            fail(e);
            throw new RuntimeException(e);
        }
    }

    public static HttpClient createClientManifests(BinaryData content, String digest,
                                                   ManifestMediaType returnContentType) {
        return new MockHttpClient(request -> {
            assertEquals(DEFAULT_MANIFEST_CONTENT_TYPE, request.getHeaders().getValue(HttpHeaderName.ACCEPT));
            HttpHeaders headers = new HttpHeaders()
                .add(UtilsImpl.DOCKER_DIGEST_HEADER_NAME, digest)
                .add(HttpHeaderName.CONTENT_TYPE, returnContentType == null ? ManifestMediaType.OCI_MANIFEST.toString() : returnContentType.toString());
            return new MockHttpResponse(request, 200, headers, content.toBytes());
        });
    }

    public static HttpClient createUploadContentClient(Supplier<String> calculateDigest, BiFunction<HttpRequest, Integer, HttpResponse> onChunk) {
        AtomicInteger callNumber = new AtomicInteger();
        return new MockHttpClient(request -> {
            String expectedReceivedLocation =  String.valueOf(callNumber.getAndIncrement());
            HttpHeaders responseHeaders = new HttpHeaders().add("Location", String.valueOf(callNumber.get()));
            if (request.getHttpMethod() == HttpMethod.POST) { // start upload
                assertEquals(0, callNumber.get() - 1);
                return new MockHttpResponse(request, 202, responseHeaders);
            } else if (request.getHttpMethod() == HttpMethod.PATCH) { // upload chunk
                assertEquals("/" + expectedReceivedLocation, request.getUrl().getPath());
                HttpResponse response = onChunk.apply(request, callNumber.get());
                if (response == null) {
                    response = new MockHttpResponse(request, 202, responseHeaders);
                }

                return response;
            } else if (request.getHttpMethod() == HttpMethod.PUT) { // complete upload
                String expectedDigest = calculateDigest.get();
                try {
                    // make Java 8 happy
                    assertEquals(request.getUrl().getQuery(), "digest=" + URLEncoder.encode(expectedDigest, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    fail(e);
                }
                assertEquals("/" + expectedReceivedLocation, request.getUrl().getPath());

                responseHeaders.add(DOCKER_DIGEST_HEADER_NAME, expectedDigest);
                return new MockHttpResponse(request, 201, responseHeaders);
            }
            return new MockHttpResponse(request, 404);
        });
    }

    public static HttpClient createUploadContentClient(Supplier<String> calculateDigest) {
        return createUploadContentClient(calculateDigest, (r, c) -> null);
    }

    private BinaryData getDataSync(int size, MessageDigest sha256) {
        return BinaryData.fromFlux(getDataAsync(size, CHUNK_SIZE, sha256)).block();
    }

    private Flux<ByteBuffer> getDataAsync(long size, int chunkSize, MessageDigest sha256) {
        return Flux.create(sink -> {
            long sent = 0;
            while (sent < size) {
                ByteBuffer buffer = ByteBuffer.wrap(CHUNK);
                if (sent + chunkSize > size) {
                    buffer.limit((int) (size - sent));
                }

                sha256.update(buffer.asReadOnlyBuffer());
                sink.next(buffer);
                sent += chunkSize;
            }

            sink.complete();
        });
    }

    private ContainerRegistryContentClient createSyncClient(HttpClient httpClient) {
        return new ContainerRegistryContentClientBuilder()
            .endpoint("https://endpoint.com")
            .repositoryName("foo")
            .httpClient(httpClient)
            .buildClient();
    }

    private ContainerRegistryContentAsyncClient createAsyncClient(HttpClient httpClient) {
        return new ContainerRegistryContentClientBuilder()
            .endpoint("https://endpoint.com")
            .repositoryName("foo")
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
