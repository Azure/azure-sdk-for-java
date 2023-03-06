// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciManifest;
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
import com.azure.core.http.rest.Response;
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

import static com.azure.containers.containerregistry.TestUtils.DIGEST_UNKNOWN;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.OCI_INDEX_MEDIA_TYPE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ContainerRegistryBlobClientTests {
    private static final BinaryData SMALL_CONTENT = BinaryData.fromString("foobar");
    private static final String SMALL_CONTENT_SHA256 = "sha256:c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2";
    private static final String DEFAULT_MANIFEST_CONTENT_TYPE = ManifestMediaType.OCI_MANIFEST + "," + ManifestMediaType.DOCKER_MANIFEST;
    private static final BinaryData MANIFEST_DATA = BinaryData.fromObject(MANIFEST);

    private static final BinaryData OCI_INDEX = BinaryData.fromString("{\"schemaVersion\":2,\"mediaType\":\"application/vnd.oci.image.index.v1+json\","
                                            + "\"manifests\":[{\"mediaType\":\"application/vnd.oci.image.manifest.v1+json\",\"size\":7143,\"digest\":\"sha256:e692418e4cbaf90ca69d05a66403747baa33ee08806650b51fab815ad7fc331f\","
                                            + "\"platform\":{\"architecture\":\"ppc64le\",\"os\":\"linux\"}}]}");

    private static final String OCI_INDEX_DIGEST = "sha256:226bb568af1e417421f2f8053cd2847bdb5fcc52c7ed93823abdb595881c2b02";
    @Test
    public void downloadBlobWrongDigestInHeaderSync() {
        ContainerRegistryBlobClient client = createSyncClient(createClientBlobs(SMALL_CONTENT, DIGEST_UNKNOWN));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertThrows(ServiceResponseException.class, () -> client.downloadStream("some-digest", Channels.newChannel(stream)));
    }

    @Test
    public void downloadManigestWrongDigestInHeaderSync() {
        ContainerRegistryBlobClient client = createSyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));
        assertThrows(ServiceResponseException.class, () -> client.downloadManifest("latest"));
    }

    @Test
    public void downloadBlobWrongDigestInHeaderAsync() {
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientBlobs(SMALL_CONTENT, DIGEST_UNKNOWN));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient.downloadStream("some-digest")
                .flatMap(response -> response.writeValueTo(Channels.newChannel(stream))))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @Test
    public void downloadManifestWrongDigestInHeaderAsync() {
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));

        StepVerifier.create(asyncClient.downloadManifest("latest"))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @Test
    public void downloadBlobWrongResponseSync() {
        ContainerRegistryBlobClient client = createSyncClient(createClientBlobs(SMALL_CONTENT, DIGEST_UNKNOWN));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertThrows(ServiceResponseException.class, () -> client.downloadStream(SMALL_CONTENT_SHA256, Channels.newChannel(stream)));
    }

    @Test
    public void downloadManifestWrongResponseSync() {
        ContainerRegistryBlobClient client = createSyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));
        assertThrows(ServiceResponseException.class, () -> client.downloadManifest("latest"));
    }

    @Test
    public void downloadBlobWrongResponseAsync() {
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientBlobs(SMALL_CONTENT, DIGEST_UNKNOWN));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient.downloadStream(SMALL_CONTENT_SHA256)
                .flatMap(response -> response.writeValueTo(Channels.newChannel(stream))))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @Test
    public void downloadManifestWrongResponseAsync() {
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, DIGEST_UNKNOWN, null));
        StepVerifier.create(asyncClient.downloadManifest("latest"))
            .expectError(ServiceResponseException.class)
            .verify();
    }

    @SyncAsyncTest
    public void downloadManifest() {
        ContainerRegistryBlobClient client = createSyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, null));
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, null));

        DownloadManifestResult result = SyncAsyncExtension.execute(
            () -> client.downloadManifest(MANIFEST_DIGEST),
            () -> asyncClient.downloadManifest(MANIFEST_DIGEST));

        assertArrayEquals(MANIFEST_DATA.toBytes(), result.getContent().toBytes());
        assertNotNull(result.asOciManifest());
        assertEquals(ManifestMediaType.OCI_MANIFEST, result.getMediaType());
    }

    @SyncAsyncTest
    public void downloadManifestWithDockerType() {
        ContainerRegistryBlobClient client = createSyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, ManifestMediaType.DOCKER_MANIFEST));
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientManifests(MANIFEST_DATA, MANIFEST_DIGEST, ManifestMediaType.DOCKER_MANIFEST));

        Response<DownloadManifestResult> result = SyncAsyncExtension.execute(
            () -> client.downloadManifestWithResponse(MANIFEST_DIGEST, ManifestMediaType.DOCKER_MANIFEST, Context.NONE),
            () -> asyncClient.downloadManifestWithResponse(MANIFEST_DIGEST, ManifestMediaType.DOCKER_MANIFEST));

        assertArrayEquals(MANIFEST_DATA.toBytes(), result.getValue().getContent().toBytes());
        assertNotNull(result.getValue().asOciManifest());
        assertEquals(ManifestMediaType.DOCKER_MANIFEST, result.getValue().getMediaType());
    }

    @SyncAsyncTest
    public void downloadManifestWithOciIndexType() {
        ContainerRegistryBlobClient client = createSyncClient(createClientManifests(OCI_INDEX, OCI_INDEX_DIGEST, OCI_INDEX_MEDIA_TYPE));
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientManifests(OCI_INDEX, OCI_INDEX_DIGEST, OCI_INDEX_MEDIA_TYPE));

        Response<DownloadManifestResult> result = SyncAsyncExtension.execute(
            () -> client.downloadManifestWithResponse(OCI_INDEX_DIGEST, OCI_INDEX_MEDIA_TYPE, Context.NONE),
            () -> asyncClient.downloadManifestWithResponse(OCI_INDEX_DIGEST, OCI_INDEX_MEDIA_TYPE));

        assertArrayEquals(OCI_INDEX.toBytes(), result.getValue().getContent().toBytes());
        assertEquals(OCI_INDEX_MEDIA_TYPE, result.getValue().getMediaType());
        OciManifest ociManifest = result.getValue().asOciManifest();
        assertEquals(2, ociManifest.getSchemaVersion());
        assertNull(ociManifest.getConfig());
        assertNull(ociManifest.getLayers());
        assertNull(ociManifest.getAnnotations());
    }

    @SyncAsyncTest
    public void downloadBlobOneChunk() throws IOException {
        ContainerRegistryBlobClient client = createSyncClient(createClientBlobs(SMALL_CONTENT, SMALL_CONTENT_SHA256));
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientBlobs(SMALL_CONTENT, SMALL_CONTENT_SHA256));

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

        ContainerRegistryBlobClient client = createSyncClient(createClientBlobs(content, expectedDigest));
        ContainerRegistryBlobAsyncClient asyncClient = createAsyncClient(createClientBlobs(content, expectedDigest));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        WritableByteChannel channel = Channels.newChannel(stream);
        SyncAsyncExtension.execute(
            () -> client.downloadStream(expectedDigest, Channels.newChannel(stream)),
            () -> asyncClient.downloadStream(expectedDigest)
                .flatMap(result -> result.writeValueTo(channel)));
        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    public static HttpClient createClientBlobs(BinaryData content, String digest) {
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

    public static HttpClient createClientManifests(BinaryData content, String digest,
                                                   ManifestMediaType returnContentType) {
        return new MockHttpClient(request -> {
            if (returnContentType == null) {
                assertEquals(DEFAULT_MANIFEST_CONTENT_TYPE, request.getHeaders().getValue(HttpHeaderName.ACCEPT));
            } else {
                assertEquals(returnContentType.toString(), request.getHeaders().getValue(HttpHeaderName.ACCEPT));
            }
            HttpHeaders headers = new HttpHeaders()
                .add(UtilsImpl.DOCKER_DIGEST_HEADER_NAME, digest)
                .add(HttpHeaderName.CONTENT_TYPE, returnContentType == null ? ManifestMediaType.OCI_MANIFEST.toString() : returnContentType.toString());
            return new MockHttpResponse(request, 200, headers, content.toBytes());
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
