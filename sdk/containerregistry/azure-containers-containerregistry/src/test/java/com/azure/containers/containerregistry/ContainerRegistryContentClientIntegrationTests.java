// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.models.ManifestList;
import com.azure.containers.containerregistry.implementation.models.V2Manifest;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.GetManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.SetManifestOptions;
import com.azure.containers.containerregistry.models.SetManifestResult;
import com.azure.containers.containerregistry.models.UploadRegistryBlobResult;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

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
public class ContainerRegistryContentClientIntegrationTests extends ContainerRegistryClientsTestBase {
    private ContainerRegistryContentClient client;
    private ContainerRegistryContentAsyncClient asyncClient;
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
        importImage(TestUtils.getTestMode(), HELLO_WORLD_REPOSITORY_NAME, Collections.singletonList("latest"));
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
        client = getContentClient("oci-artifact", httpClient);

        setManifestPrerequisites();

        SetManifestResult result = client.setManifest(MANIFEST, null);
        assertNotNull(result);
        assertNotNull(result.getDigest());

        GetManifestResult getManifestResult = client.getManifest(result.getDigest());
        assertEquals(result.getDigest(), getManifestResult.getDigest());
        validateManifest(MANIFEST, getManifestResult.getManifest().toObject(OciImageManifest.class));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestBinaryData(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);
        setManifestPrerequisites();

        SetManifestOptions options  = new SetManifestOptions(BinaryData.fromObject(MANIFEST), ManifestMediaType.OCI_IMAGE_MANIFEST);
        SetManifestResult result = client.setManifestWithResponse(options, Context.NONE).getValue();
        assertNotNull(result);
        assertNotNull(result.getDigest());

        GetManifestResult getManifestResult = client.getManifest(MANIFEST_DIGEST);
        assertEquals(MANIFEST_DIGEST, getManifestResult.getDigest());
        validateManifest(MANIFEST, getManifestResult.getManifest());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestViaOptions(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);
        setManifestPrerequisites();

        SetManifestResult result = client.setManifestWithResponse(new SetManifestOptions(MANIFEST), Context.NONE).getValue();
        assertNotNull(result);
        assertNotNull(result.getDigest());

        GetManifestResult getManifestResult = client.getManifest(MANIFEST_DIGEST);
        assertEquals(MANIFEST_DIGEST, getManifestResult.getDigest());
        validateManifest(MANIFEST, getManifestResult.getManifest());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestWithTag(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);
        String tag = "v1";
        setManifestPrerequisites();

        SetManifestResult result = client.setManifest(MANIFEST, tag);
        assertNotNull(result);
        assertNotNull(result.getDigest());

        GetManifestResult getManifestResult = client.getManifest(MANIFEST_DIGEST);
        assertEquals(MANIFEST_DIGEST, getManifestResult.getDigest());
        validateManifest(MANIFEST, getManifestResult.getManifest().toObject(OciImageManifest.class));

        validateTag("oci-artifact", MANIFEST_DIGEST, tag, httpClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadDockerManifestWithTag(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);
        String tag = "v2";
        setManifestPrerequisites();
        BinaryData dockerV2Manifest = BinaryData.fromObject(createDockerV2Manifest());

        SetManifestOptions options = new SetManifestOptions(dockerV2Manifest, ManifestMediaType.DOCKER_MANIFEST)
            .setTag(tag);
        SetManifestResult result = client.setManifestWithResponse(options, Context.NONE).getValue();
        assertNotNull(result);
        assertNotNull(result.getDigest());

        GetManifestResult getManifestResult = client.getManifest(tag);
        assertEquals(result.getDigest(), getManifestResult.getDigest());

        validateTag("oci-artifact", result.getDigest(), tag, httpClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestAsync(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);

        setManifestPrerequisites();
        StepVerifier.create(asyncClient.setManifest(MANIFEST, null)
                .flatMap(result -> {
                    assertNotNull(result);
                    assertNotNull(result.getDigest());

                    return asyncClient.getManifest(MANIFEST_DIGEST);
                }))
            .assertNext(getManifestResult -> {
                assertEquals(MANIFEST_DIGEST, getManifestResult.getDigest());
                validateManifest(MANIFEST, getManifestResult.getManifest().toObject(OciImageManifest.class));
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadDockerManifestWithTagAsync(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);
        String tag = "v2";
        setManifestPrerequisites();
        BinaryData dockerV2Manifest = BinaryData.fromObject(createDockerV2Manifest());
        String digest = computeDigest(dockerV2Manifest.toByteBuffer().asReadOnlyBuffer());

        SetManifestOptions options = new SetManifestOptions(dockerV2Manifest, ManifestMediaType.DOCKER_MANIFEST).setTag(tag);
        StepVerifier.create(asyncClient.setManifestWithResponse(options)
                .flatMap(result -> {
                    assertNotNull(result);
                    assertNotNull(result.getValue().getDigest());

                    return asyncClient.getManifest(tag);
                }))
                .assertNext(getManifestResult -> assertEquals(digest, getManifestResult.getDigest()))
                .verifyComplete();

        validateTag("oci-artifact", digest, tag, httpClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadBlob(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);

        UploadRegistryBlobResult result = client.uploadBlob(CONFIG_DATA);

        assertEquals(CONFIG_DIGEST, result.getDigest());
        assertEquals(CONFIG_DATA.getLength(), result.getSizeInBytes());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadHugeBlobInChunks(HttpClient httpClient) throws IOException, InterruptedException {
        // test is too long for innerloop
        assumeTrue(super.getTestMode() == TestMode.LIVE);

        client = getContentClient("oci-artifact", httpClient);

        long size = CHUNK_SIZE * 50;
        BinaryData data = BinaryData.fromStream(new TestInputStream(size), size);
        UploadRegistryBlobResult result = client.uploadBlob(data, Context.NONE);

        TestOutputStream output = new TestOutputStream();
        client.downloadStream(result.getDigest(), Channels.newChannel(output));
        output.flush();
        assertEquals(size, output.getPosition());
        assertEquals(size, result.getSizeInBytes());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadHugeBlobInChunksAsync(HttpClient httpClient) {
        // test is too long for innerloop
        assumeTrue(super.getTestMode() == TestMode.LIVE);

        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);

        long size = CHUNK_SIZE * 50;
        Mono<BinaryData> data = BinaryData.fromFlux(generateAsyncStream(size), size, false);
        AtomicLong download = new AtomicLong(0);
        StepVerifier.setDefaultTimeout(Duration.ofMinutes(30));
        StepVerifier.create(data
                .flatMap(content -> asyncClient.uploadBlob(content))
                .flatMap(r -> asyncClient.downloadStream(r.getDigest()))
                .flatMapMany(BinaryData::toFluxByteBuffer)
                .doOnNext(bb -> download.addAndGet(bb.remaining()))
                .then())
            .verifyComplete();

        assertEquals(size, download.get());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadBlob(HttpClient httpClient) throws IOException {
        BinaryData content = generateStream((int) (CHUNK_SIZE * 2.1d));
        client = getContentClient("oci-artifact", httpClient);
        UploadRegistryBlobResult uploadResult = client.uploadBlob(content);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        client.downloadStream(uploadResult.getDigest(), Channels.newChannel(stream));

        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadSmallBlob(HttpClient httpClient) throws IOException {
        BinaryData content = generateStream(CHUNK_SIZE - 1);
        client = getContentClient("oci-artifact", httpClient);
        UploadRegistryBlobResult uploadResult = client.uploadBlob(content);
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
                .flatMap(r -> FluxUtil.writeToOutputStream(r.toFluxByteBuffer(), stream)))
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
                .flatMap(r -> FluxUtil.writeToOutputStream(r.toFluxByteBuffer(), stream)))
            .verifyComplete();

        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getManifest(HttpClient httpClient) {
        client = getContentClient("oci-artifact", httpClient);
        setManifestPrerequisites();

        SetManifestResult result = client.setManifest(MANIFEST, "latest");
        GetManifestResult downloadResult = client.getManifest(result.getDigest());
        assertNotNull(downloadResult.getManifest().toObject(OciImageManifest.class));
        validateManifest(MANIFEST, downloadResult.getManifest().toObject(OciImageManifest.class));

        downloadResult = client.getManifest("latest");
        assertNotNull(downloadResult.getManifest().toObject(OciImageManifest.class));
        validateManifest(MANIFEST, downloadResult.getManifest().toObject(OciImageManifest.class));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getManifestAsync(HttpClient httpClient) {
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);
        StepVerifier.create(
                setManifestPrerequisitesAsync()
                    .then(asyncClient.setManifest(MANIFEST, null))
                    .flatMap(result -> asyncClient.getManifest(result.getDigest())))
            .assertNext(downloadResult -> {
                OciImageManifest returnedManifest = downloadResult.getManifest().toObject(OciImageManifest.class);
                assertNotNull(returnedManifest);
                validateManifest(MANIFEST, returnedManifest);
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getManifestListManifest(HttpClient httpClient) {
        // TODO (limolkova) enable other modes after https://github.com/Azure/azure-sdk-tools/issues/6194 is released
        assumeTrue(super.getTestMode() == TestMode.LIVE);
        client = getContentClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);
        ManifestMediaType dockerListType = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
        Response<GetManifestResult> manifestResult = client.getManifestWithResponse("latest", Context.NONE);
        assertNotNull(manifestResult.getValue());
        assertEquals(dockerListType, manifestResult.getValue().getManifestMediaType());

        ManifestList list = manifestResult.getValue().getManifest().toObject(ManifestList.class);
        assertEquals(2, list.getSchemaVersion());
        assertEquals(dockerListType.toString(), list.getMediaType());
        assertEquals(11, list.getManifests().size());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getManifestIncompatibleType(HttpClient httpClient) {
        client = getContentClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);
        ManifestMediaType ociArtifactType = ManifestMediaType.fromString("application/vnd.oci.artifact.manifest.v1+json");

        BinaryData ociArtifact = BinaryData.fromString("{\"mediaType\": \"application/vnd.oci.artifact.manifest.v1+json\",\"artifactType\": \"application/vnd.example.sbom.v1\"}");
        SetManifestResult result = client.setManifestWithResponse(new SetManifestOptions(ociArtifact, ociArtifactType), Context.NONE)
            .getValue();
        assertThrows(ResourceNotFoundException.class, () -> client.getManifestWithResponse(result.getDigest(), Context.NONE));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getManifestDifferentType(HttpClient httpClient) {
        // TODO (limolkova) enable other modes after https://github.com/Azure/azure-sdk-tools/issues/6194 is released
        assumeTrue(super.getTestMode() == TestMode.LIVE);
        client = getContentClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);

        // the original content there is docker v2 manifest list
        GetManifestResult manifestResult = client.getManifest("latest");

        // but service does the best effort to return what it supports
        assertEquals("application/vnd.docker.distribution.manifest.list.v2+json", manifestResult.getManifestMediaType().toString());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getManifestListManifestAsync(HttpClient httpClient) {
        // TODO (limolkova) enable other modes after https://github.com/Azure/azure-sdk-tools/issues/6194 is released
        assumeTrue(super.getTestMode() == TestMode.LIVE);
        asyncClient = getBlobAsyncClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);
        ManifestMediaType dockerListType = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");

        StepVerifier.create(asyncClient.getManifestWithResponse("latest"))
            .assertNext(manifestResult -> {
                assertNotNull(manifestResult.getValue());
                assertEquals(dockerListType, manifestResult.getValue().getManifestMediaType());
                // does not throw
                ManifestList list = manifestResult.getValue().getManifest().toObject(ManifestList.class);
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

    private void setManifestPrerequisites() {
        client.uploadBlob(CONFIG_DATA);
        client.uploadBlob(LAYER_DATA);
    }

    private Mono<Void> setManifestPrerequisitesAsync() {
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
        assertNotNull(returnedManifest.getConfiguration());
        assertEquals(originalManifest.getConfiguration().getMediaType(), returnedManifest.getConfiguration().getMediaType());
        assertEquals(originalManifest.getConfiguration().getSizeInBytes(), returnedManifest.getConfiguration().getSizeInBytes());
        assertNotNull(returnedManifest.getLayers());
        assertEquals(originalManifest.getLayers().size(), returnedManifest.getLayers().size());
        for (int i = 0; i < originalManifest.getLayers().size(); i++) {
            assertEquals(originalManifest.getLayers().get(i).getMediaType(), returnedManifest.getLayers().get(i).getMediaType());
            assertEquals(originalManifest.getLayers().get(i).getDigest(), returnedManifest.getLayers().get(i).getDigest());
            assertEquals(originalManifest.getLayers().get(i).getSizeInBytes(), returnedManifest.getLayers().get(i).getSizeInBytes());
        }
    }

    private ContainerRegistryContentClient getContentClient(String repositoryName, HttpClient httpClient) {
        return getContentClientBuilder(repositoryName, buildSyncAssertingClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)).buildClient();
    }

    private ContainerRegistryContentAsyncClient getBlobAsyncClient(String repositoryName, HttpClient httpClient) {
        return getContentClientBuilder(repositoryName, httpClient).buildAsyncClient();
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

    private static class TestInputStream extends InputStream {
        private final long size;
        private long position = 0;
        private long mark = 0;

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

        @Override
        public synchronized void mark(int readlimit) {
            mark = position;
        }

        @Override
        public synchronized void reset() {
            position = mark;
        }

        @Override
        public boolean markSupported() {
            return true;
        }
    }

    private static class TestOutputStream extends OutputStream {
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
