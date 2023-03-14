// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.models.ManifestList;
import com.azure.containers.containerregistry.implementation.models.V2Manifest;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobAsyncClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClient;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.azure.containers.containerregistry.TestUtils.CONFIG_DATA;
import static com.azure.containers.containerregistry.TestUtils.CONFIG_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.containers.containerregistry.TestUtils.HELLO_WORLD_REPOSITORY_NAME;
import static com.azure.containers.containerregistry.TestUtils.LAYER_DATA;
import static com.azure.containers.containerregistry.TestUtils.LAYER_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.SKIP_AUTH_TOKEN_REQUEST_FUNCTION;
import static com.azure.containers.containerregistry.TestUtils.importImage;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.computeDigest;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class ContainerRegistryBlobClientIntegrationTests extends ContainerRegistryClientsTestBase {
    private ContainerRegistryBlobClient client;
    private ContainerRegistryBlobAsyncClient asyncClient;
    private static final Random RANDOM = new Random(42);
    private static final byte[] CHUNK = new byte[CHUNK_SIZE];

    static {
        RANDOM.nextBytes(CHUNK);
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest(SKIP_AUTH_TOKEN_REQUEST_FUNCTION)
            .assertSync()
            .build();
    }

    @BeforeAll
    static void beforeAll() {
        importImage(TestingHelpers.getTestMode(), HELLO_WORLD_REPOSITORY_NAME, Collections.singletonList("latest"));
    }

    @BeforeEach
    void beforeEach() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterEach
    void afterEach() {
        StepVerifier.resetDefaultTimeout();
        cleanupResources();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifest(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);

        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifest(MANIFEST, null);
        assertNotNull(result);
        assertNotNull(result.getDigest());

        DownloadManifestResult downloadManifestResult = client.downloadManifest(result.getDigest());
        assertEquals(result.getDigest(), downloadManifestResult.getDigest());
        validateManifest(MANIFEST, downloadManifestResult.asOciManifest());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestBinaryData(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();

        UploadManifestOptions options  = new UploadManifestOptions(BinaryData.fromObject(MANIFEST), ManifestMediaType.OCI_MANIFEST);
        UploadManifestResult result = client.uploadManifestWithResponse(options, Context.NONE).getValue();
        assertNotNull(result);
        assertNotNull(result.getDigest());

        DownloadManifestResult downloadManifestResult = client.downloadManifest(MANIFEST_DIGEST);
        assertEquals(MANIFEST_DIGEST, downloadManifestResult.getDigest());
        validateManifest(MANIFEST, downloadManifestResult.getContent());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestViaOptions(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifestWithResponse(new UploadManifestOptions(MANIFEST), Context.NONE).getValue();
        assertNotNull(result);
        assertNotNull(result.getDigest());

        DownloadManifestResult downloadManifestResult = client.downloadManifest(MANIFEST_DIGEST);
        assertEquals(MANIFEST_DIGEST, downloadManifestResult.getDigest());
        validateManifest(MANIFEST, downloadManifestResult.getContent());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canDownloadManifestWithListOfTypes(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifest(MANIFEST, null);
        assertNotNull(result);
        assertNotNull(result.getDigest());

        List<ManifestMediaType> manifestTypes = Arrays.asList(ManifestMediaType.DOCKER_MANIFEST,
            ManifestMediaType.OCI_MANIFEST,
            ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json"),
            ManifestMediaType.fromString("application/vnd.oci.image.index.v1+json"));

        DownloadManifestResult downloadManifestResult = client.downloadManifestWithResponse(MANIFEST_DIGEST, manifestTypes, Context.NONE).getValue();
        assertEquals(MANIFEST_DIGEST, downloadManifestResult.getDigest());
        assertEquals(ManifestMediaType.OCI_MANIFEST, downloadManifestResult.getMediaType());
        validateManifest(MANIFEST, downloadManifestResult.getContent());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestWithTag(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        String tag = "v1";
        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifest(MANIFEST, tag);
        assertNotNull(result);
        assertNotNull(result.getDigest());

        DownloadManifestResult downloadManifestResult = client.downloadManifest(MANIFEST_DIGEST);
        assertEquals(MANIFEST_DIGEST, downloadManifestResult.getDigest());
        validateManifest(MANIFEST, downloadManifestResult.asOciManifest());

        validateTag("oci-artifact", MANIFEST_DIGEST, tag, httpClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadDockerManifestWithTag(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        String tag = "v2";
        uploadManifestPrerequisites();
        BinaryData dockerV2Manifest = BinaryData.fromObject(createDockerV2Manifest());

        UploadManifestOptions options = new UploadManifestOptions(dockerV2Manifest, ManifestMediaType.DOCKER_MANIFEST)
            .setTag(tag);
        UploadManifestResult result = client.uploadManifestWithResponse(options, Context.NONE).getValue();
        assertNotNull(result);
        assertNotNull(result.getDigest());

        DownloadManifestResult downloadManifestResult = client.downloadManifest(tag);
        assertEquals(result.getDigest(), downloadManifestResult.getDigest());

        validateTag("oci-artifact", result.getDigest(), tag, httpClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestAsync(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);

        uploadManifestPrerequisites();
        StepVerifier.create(asyncClient.uploadManifest(MANIFEST, null)
                .flatMap(result -> {
                    assertNotNull(result);
                    assertNotNull(result.getDigest());

                    return asyncClient.downloadManifest(MANIFEST_DIGEST);
                }))
            .assertNext(downloadManifestResult -> {
                assertEquals(MANIFEST_DIGEST, downloadManifestResult.getDigest());
                validateManifest(MANIFEST, downloadManifestResult.asOciManifest());
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadDockerManifestWithTagAsync(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);
        String tag = "v2";
        uploadManifestPrerequisites();
        BinaryData dockerV2Manifest = BinaryData.fromObject(createDockerV2Manifest());
        String digest = computeDigest(dockerV2Manifest.toByteBuffer().asReadOnlyBuffer());

        UploadManifestOptions options = new UploadManifestOptions(dockerV2Manifest, ManifestMediaType.DOCKER_MANIFEST).setTag(tag);
        StepVerifier.create(asyncClient.uploadManifestWithResponse(options)
                .flatMap(result -> {
                    assertNotNull(result);
                    assertNotNull(result.getValue().getDigest());

                    return asyncClient.downloadManifest(tag);
                }))
                .assertNext(downloadManifestResult -> assertEquals(digest, downloadManifestResult.getDigest()))
                .verifyComplete();

        validateTag("oci-artifact", digest, tag, httpClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadBlob(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);

        UploadBlobResult result = client.uploadBlob(CONFIG_DATA);

        assertEquals(CONFIG_DIGEST, result.getDigest());
        assertEquals(CONFIG_DATA.getLength(), result.getSizeInBytes());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadHugeBlobInChunks(HttpClient httpClient) throws IOException {
        assumeTrue(super.getTestMode() == TestMode.LIVE);

        client = getBlobClient("oci-artifact", httpClient);

        long size = CHUNK_SIZE * 50;
        TestInputStream input = new TestInputStream(size);
        UploadBlobResult result = client.uploadBlob(Channels.newChannel(input), Context.NONE);

        TestOutputStream output = new TestOutputStream();
        client.downloadStream(result.getDigest(), Channels.newChannel(output));
        output.flush();
        assertEquals(size, output.getPosition());
        assertEquals(size, result.getSizeInBytes());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadHugeBlobInChunksAsync(HttpClient httpClient) {
        assumeTrue(super.getTestMode() == TestMode.LIVE);

        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);

        long size = CHUNK_SIZE * 50;
        final TestOutputStream output = new TestOutputStream();
        StepVerifier.setDefaultTimeout(Duration.ofMinutes(30));
        StepVerifier.create(BinaryData.fromFlux(generateAsyncStream(size))
                .flatMap(data -> asyncClient.uploadBlob(data))
                .flatMap(uploadBlobResult -> {
                    assertEquals(size, uploadBlobResult.getSizeInBytes());
                    return asyncClient.downloadStream(uploadBlobResult.getDigest());
                })
                .flatMap(downloadResult -> downloadResult.writeValueTo(Channels.newChannel(output))))
            .verifyComplete();

        assertEquals(size, output.getPosition());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadBlob(HttpClient httpClient) throws IOException {
        BinaryData content = generateStream((int) (CHUNK_SIZE * 2.1d));
        client = getBlobClient("oci-artifact", httpClient);
        UploadBlobResult uploadResult = client.uploadBlob(content);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        client.downloadStream(uploadResult.getDigest(), Channels.newChannel(stream));

        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadSmallBlob(HttpClient httpClient) throws IOException {
        BinaryData content = generateStream(CHUNK_SIZE - 1);
        client = getBlobClient("oci-artifact", httpClient);
        UploadBlobResult uploadResult = client.uploadBlob(content);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        client.downloadStream(uploadResult.getDigest(), Channels.newChannel(stream));

        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadBlobAsync(HttpClient httpClient) throws IOException {
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);

        BinaryData content = generateStream((int) (CHUNK_SIZE * 2.5));
        String digest = computeDigest(content.toByteBuffer());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient
                .uploadBlob(content)
                .flatMap(uploadResult -> {
                    assertEquals(digest, uploadResult.getDigest());
                    return asyncClient.downloadStream(uploadResult.getDigest());
                })
                .flatMap(r -> r.writeValueTo(Channels.newChannel(stream))))
            .verifyComplete();

        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadSmallBlobAsync(HttpClient httpClient) throws IOException {
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);

        BinaryData content = generateStream(CHUNK_SIZE / 2);
        String digest = computeDigest(content.toByteBuffer());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient
                .uploadBlob(content)
                .flatMap(uploadResult -> {
                    assertEquals(digest, uploadResult.getDigest());
                    return asyncClient.downloadStream(uploadResult.getDigest());
                })
                .flatMap(r -> r.writeValueTo(Channels.newChannel(stream))))
            .verifyComplete();

        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifest(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifest(MANIFEST, "latest");
        DownloadManifestResult downloadResult = client.downloadManifest(result.getDigest());
        assertNotNull(downloadResult.asOciManifest());
        validateManifest(MANIFEST, downloadResult.asOciManifest());

        downloadResult = client.downloadManifest("latest");
        assertNotNull(downloadResult.asOciManifest());
        validateManifest(MANIFEST, downloadResult.asOciManifest());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestAsync(HttpClient httpClient) {
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);
        StepVerifier.create(
                uploadManifestPrerequisitesAsync()
                    .then(asyncClient.uploadManifest(MANIFEST, null))
                    .flatMap(result -> asyncClient.downloadManifest(result.getDigest())))
            .assertNext(downloadResult -> {
                OciImageManifest returnedManifest = downloadResult.asOciManifest();
                assertNotNull(returnedManifest);
                validateManifest(MANIFEST, returnedManifest);
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestWithListOfTypesAsync(HttpClient httpClient) {
        List<ManifestMediaType> manifestTypes = Arrays.asList(ManifestMediaType.DOCKER_MANIFEST,
            ManifestMediaType.OCI_MANIFEST,
            ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json"),
            ManifestMediaType.fromString("application/vnd.oci.image.index.v1+json"));

        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);
        StepVerifier.create(
                uploadManifestPrerequisitesAsync()
                    .then(asyncClient.uploadManifest(MANIFEST, null))
                    .flatMap(result -> asyncClient.downloadManifestWithResponse(result.getDigest(), manifestTypes))
                    .map(response -> response.getValue()))
            .assertNext(downloadResult -> {
                OciImageManifest returnedManifest = downloadResult.asOciManifest();
                assertNotNull(returnedManifest);
                validateManifest(MANIFEST, returnedManifest);
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestListManifest(HttpClient httpClient) {
        client = getBlobClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);
        ManifestMediaType dockerListType = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
        Response<DownloadManifestResult> manifestResult = client.downloadManifestWithResponse("latest", Collections.singletonList(dockerListType), Context.NONE);
        assertNotNull(manifestResult.getValue());
        assertEquals(dockerListType, manifestResult.getValue().getMediaType());

        assertThrows(IllegalStateException.class, () -> manifestResult.getValue().asOciManifest());

        // does not throw
        ManifestList list = manifestResult.getValue().getContent().toObject(ManifestList.class);
        assertEquals(2, list.getSchemaVersion());
        assertEquals(dockerListType.toString(), list.getMediaType());
        assertEquals(11, list.getManifests().size());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestIncompatibleType(HttpClient httpClient) {
        client = getBlobClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);
        ManifestMediaType dockerV1 = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.v1+json");

        assertThrows(ServiceResponseException.class, () -> client.downloadManifestWithResponse("latest",
            Collections.singletonList(dockerV1), Context.NONE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestDifferentType(HttpClient httpClient) {
        client = getBlobClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);

        // the original content there is docker v2 manifest list
        Response<DownloadManifestResult> manifestResult = client.downloadManifestWithResponse("latest",
            Collections.singletonList(ManifestMediaType.DOCKER_MANIFEST), Context.NONE);

        // but service does the best effort to return what it supports
        OciImageManifest manifest = manifestResult.getValue().asOciManifest();
        assertEquals(1, manifest.getLayers().size());
        assertEquals("application/vnd.docker.image.rootfs.diff.tar.gzip", manifest.getLayers().get(0).getMediaType());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestListManifestAsync(HttpClient httpClient) {
        asyncClient = getBlobAsyncClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);
        ManifestMediaType dockerListType = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");

        StepVerifier.create(asyncClient.downloadManifestWithResponse("latest", Collections.singletonList(dockerListType)))
            .assertNext(manifestResult -> {
                assertNotNull(manifestResult.getValue());
                assertEquals(dockerListType, manifestResult.getValue().getMediaType());
                // does not throw
                ManifestList list = manifestResult.getValue().getContent().toObject(ManifestList.class);
                assertEquals(2, list.getSchemaVersion());
                assertEquals(dockerListType.toString(), list.getMediaType());
                assertEquals(11, list.getManifests().size());
            })
            .verifyComplete();
    }

    private void validateTag(String repoName, String digest, String tag, HttpClient httpClient) {
        ContainerRegistryClient acrClient = getContainerRegistryBuilder(httpClient).buildClient();
        RegistryArtifact artifact = acrClient.getArtifact(repoName, digest);
        PagedIterable<ArtifactTagProperties> tags = artifact.listTagProperties();
        assertEquals(1, tags.stream().count());
        assertEquals(tag, tags.stream().findFirst().get().getName());
    }

    private void cleanupResources() {
        if (interceptorManager.isPlaybackMode()) {
            return;
        }
        if (asyncClient != null) {
            asyncClient.deleteBlob(CONFIG_DIGEST).block();
            asyncClient.deleteBlob(LAYER_DIGEST).block();
            asyncClient.deleteManifest(MANIFEST_DIGEST).block();
        } else if (client != null) {
            client.deleteBlob(CONFIG_DIGEST);
            client.deleteBlob(LAYER_DIGEST);
            client.deleteManifest(MANIFEST_DIGEST);
        }
    }

    private void uploadManifestPrerequisites() {
        client.uploadBlob(CONFIG_DATA);
        client.uploadBlob(LAYER_DATA);
    }

    private Mono<Void> uploadManifestPrerequisitesAsync() {
        return asyncClient.uploadBlob(CONFIG_DATA)
            .then(asyncClient.uploadBlob(LAYER_DATA))
            .then();
    }

    private V2Manifest createDockerV2Manifest() {
        V2Manifest manifest = new V2Manifest()
            .setMediaType(ManifestMediaType.DOCKER_MANIFEST.toString())
            .setSchemaVersion(2)
            .setConfig(new OciDescriptor()
                .setMediaType("application/vnd.docker.container.image.v1+json")
                .setDigest(CONFIG_DIGEST)
                .setSizeInBytes(171L));

        List<OciDescriptor> layers = new ArrayList<>();

        layers.add(new OciDescriptor()
            .setMediaType("application/vnd.docker.image.rootfs.diff.tar.gzip")
            .setSizeInBytes(28L)
            .setDigest(LAYER_DIGEST));

        manifest.setLayers(layers);
        return manifest;
    }

    private void validateManifest(OciImageManifest originalManifest, BinaryData returnedManifestData) {
        OciImageManifest returnedManifest = returnedManifestData.toObject(OciImageManifest.class);
        validateManifest(originalManifest, returnedManifest);
    }

    private void validateManifest(OciImageManifest originalManifest, OciImageManifest returnedManifest) {
        assertNotNull(originalManifest);
        assertNotNull(returnedManifest);
        assertNotNull(returnedManifest.getConfig());
        assertEquals(originalManifest.getConfig().getMediaType(), returnedManifest.getConfig().getMediaType());
        assertEquals(originalManifest.getConfig().getSizeInBytes(), returnedManifest.getConfig().getSizeInBytes());
        assertNotNull(returnedManifest.getLayers());
        assertEquals(originalManifest.getLayers().size(), returnedManifest.getLayers().size());
        for (int i = 0; i < originalManifest.getLayers().size(); i++) {
            assertEquals(originalManifest.getLayers().get(i).getMediaType(), returnedManifest.getLayers().get(i).getMediaType());
            assertEquals(originalManifest.getLayers().get(i).getDigest(), returnedManifest.getLayers().get(i).getDigest());
            assertEquals(originalManifest.getLayers().get(i).getSizeInBytes(), returnedManifest.getLayers().get(i).getSizeInBytes());
        }
    }

    private ContainerRegistryBlobClient getBlobClient(String repositoryName, HttpClient httpClient) {
        return getBlobClientBuilder(repositoryName, buildSyncAssertingClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)).buildClient();
    }

    private ContainerRegistryBlobAsyncClient getBlobAsyncClient(String repositoryName, HttpClient httpClient) {
        return getBlobClientBuilder(repositoryName, httpClient).buildAsyncClient();
    }

    private static BinaryData generateStream(int size) {
        byte[] content = new byte[size];
        for (int i = 0; i < size; i++) {
            content[i] = (byte) (i % 127);
        }
        return BinaryData.fromBytes(content);
    }

    private static Flux<ByteBuffer> generateAsyncStream(long size) {
        return Flux.generate(() -> 0L, (pos, sink) -> {
            long remaining = size - pos;
            if (remaining <= 0) {
                sink.complete();
                return size;
            }

            ByteBuffer buffer = ByteBuffer.wrap(CHUNK);
            if (remaining < CHUNK.length) {
                buffer.limit((int) remaining);
            }
            sink.next(buffer);

            return pos + CHUNK.length;
        });
    }

    private class TestInputStream extends InputStream {
        private final long size;
        private long position = 0;

        TestInputStream(long size) {
            this.size = size;
        }

        @Override
        public int read() {
            position++;
            return position == size ? -1 : CHUNK[(int) (position % (long) CHUNK.length)];
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (len == 0) {
                return 0;
            }
            if (off >= size || position >= size) {
                return -1;
            }

            int offAfter = off + len;
            long startPos = position;
            for (; off < offAfter && position < size; position++, off++) {
                b[off] = CHUNK[(int) (position % (long) CHUNK.length)];
            }

            return (int) (position - startPos);
        }


        @Override
        public int available() {
            return (int) Math.min(Integer.MAX_VALUE, size - position);
        }
    }

    private class TestOutputStream extends OutputStream {
        private long position = 0;

        @Override
        public void write(int b) {
            assertEquals(b, CHUNK[(int) (position % (long) CHUNK.length)]);
            position++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            for (; off < len; off++, position++) {
                assertEquals(b[off], CHUNK[(int) (position % (long) CHUNK.length)]);
            }
        }

        public long getPosition() {
            return position;
        }
    }
}
