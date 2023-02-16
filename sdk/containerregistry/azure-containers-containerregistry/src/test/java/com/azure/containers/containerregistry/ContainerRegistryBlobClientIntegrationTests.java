// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.models.ManifestList;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.V2Manifest;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciAnnotations;
import com.azure.containers.containerregistry.models.OciBlobDescriptor;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobAsyncClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

import static com.azure.containers.containerregistry.TestUtils.CONFIG_DATA;
import static com.azure.containers.containerregistry.TestUtils.CONFIG_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.containers.containerregistry.TestUtils.HELLO_WORLD_REPOSITORY_NAME;
import static com.azure.containers.containerregistry.TestUtils.LAYER_DATA;
import static com.azure.containers.containerregistry.TestUtils.LAYER_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST;
import static com.azure.containers.containerregistry.TestUtils.MANIFEST_DIGEST;
import static com.azure.containers.containerregistry.TestUtils.SKIP_AUTH_TOKEN_REQUEST_FUNCTION;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.computeDigest;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.SAME_THREAD)
public class ContainerRegistryBlobClientIntegrationTests extends ContainerRegistryClientsTestBase {
    private ContainerRegistryBlobClient client;
    private ContainerRegistryBlobAsyncClient asyncClient;

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest(SKIP_AUTH_TOKEN_REQUEST_FUNCTION)
            .assertSync()
            .build();
    }
    private ContainerRegistryBlobClient getBlobClient(String repositoryName, HttpClient httpClient) {
        return getBlobClientBuilder(repositoryName, buildSyncAssertingClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)).buildClient();
    }

    private ContainerRegistryBlobAsyncClient getBlobAsyncClient(String repositoryName, HttpClient httpClient) {
        return getBlobClientBuilder(repositoryName, httpClient).buildAsyncClient();
    }

    @AfterEach
    void afterEach() {
        cleanupResources();
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
            .setConfig(new OciBlobDescriptor()
                .setMediaType("application/vnd.docker.container.image.v1+json")
                .setDigest(CONFIG_DIGEST)
                .setSize(171L));

        List<OciBlobDescriptor> layers = new ArrayList<>();

        layers.add(new OciBlobDescriptor()
            .setMediaType("application/vnd.docker.image.rootfs.diff.tar.gzip")
            .setSize(28L)
            .setDigest(LAYER_DIGEST));

        manifest.setLayers(layers);
        return manifest;
    }

    private void validateManifest(OciManifest originalManifest, BinaryData returnedManifestData) {
        OciManifest returnedManifest = returnedManifestData.toObject(OciManifest.class);
        validateManifest(originalManifest, returnedManifest);
    }

    private void validateManifest(OciManifest originalManifest, OciManifest returnedManifest) {
        assertNotNull(originalManifest);
        assertNotNull(returnedManifest);
        assertNotNull(returnedManifest.getConfig());
        assertEquals(originalManifest.getConfig().getMediaType(), returnedManifest.getConfig().getMediaType());
        assertEquals(originalManifest.getConfig().getSize(), returnedManifest.getConfig().getSize());
        assertNotNull(returnedManifest.getLayers());
        assertEquals(originalManifest.getLayers().size(), returnedManifest.getLayers().size());
        for (int i = 0; i < originalManifest.getLayers().size(); i++) {
            assertEquals(originalManifest.getLayers().get(i).getMediaType(), returnedManifest.getLayers().get(i).getMediaType());
            assertEquals(originalManifest.getLayers().get(i).getDigest(), returnedManifest.getLayers().get(i).getDigest());
            assertEquals(originalManifest.getLayers().get(i).getSize(), returnedManifest.getLayers().get(i).getSize());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifest(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);

        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifest(MANIFEST);
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

        BinaryData manifestData = BinaryData.fromObject(MANIFEST);

        UploadManifestResult result = client.uploadManifest(new UploadManifestOptions(manifestData, ManifestMediaType.OCI_MANIFEST));
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

        UploadManifestResult result = client.uploadManifest(new UploadManifestOptions(MANIFEST));
        assertNotNull(result);
        assertNotNull(result.getDigest());

        DownloadManifestResult downloadManifestResult = client.downloadManifest(MANIFEST_DIGEST);
        assertEquals(MANIFEST_DIGEST, downloadManifestResult.getDigest());
        validateManifest(MANIFEST, downloadManifestResult.getContent());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestWithTag(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        String tag = "v1";
        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifest(new UploadManifestOptions(MANIFEST).setTag(tag));
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

        UploadManifestResult result = client.uploadManifest(new UploadManifestOptions(dockerV2Manifest, ManifestMediaType.DOCKER_MANIFEST).setTag(tag));
        assertNotNull(result);
        assertNotNull(result.getDigest());

        DownloadManifestResult downloadManifestResult = client.downloadManifest(result.getDigest());
        assertEquals(result.getDigest(), downloadManifestResult.getDigest());

        validateTag("oci-artifact", result.getDigest(), tag, httpClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestAsync(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);

        uploadManifestPrerequisites();

        StepVerifier.create(asyncClient.uploadManifest(MANIFEST)
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
        StepVerifier.create(asyncClient.uploadManifest(new UploadManifestOptions(dockerV2Manifest, ManifestMediaType.DOCKER_MANIFEST).setTag(tag))
                .flatMap(result -> {
                    assertNotNull(result);
                    assertNotNull(result.getDigest());

                    return asyncClient.downloadManifest(result.getDigest());
                }))
                .assertNext(downloadManifestResult -> assertEquals(digest, downloadManifestResult.getDigest()))
                .verifyComplete();

        validateTag("oci-artifact", digest, tag, httpClient);
    }

    private void validateTag(String repoName, String digest, String tag, HttpClient httpClient) {
        ContainerRegistryClient acrClient = getContainerRegistryBuilder(httpClient).buildClient();

        RegistryArtifact artifact = acrClient.getArtifact(repoName, digest);
        PagedIterable<ArtifactTagProperties> tags = artifact.listTagProperties();
        assertEquals(1, tags.stream().count());
        assertEquals(tag, tags.stream().findFirst().get().getName());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadBlob(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);

        UploadBlobResult result = client.uploadBlob(CONFIG_DATA);
        assertEquals(CONFIG_DIGEST, result.getDigest());
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

        BinaryData content = generateStream((int) (CHUNK_SIZE * 2.5d));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient
                .uploadBlob(content)
                .flatMap(uploadResult ->
                    asyncClient.downloadStream(uploadResult.getDigest()))
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
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StepVerifier.create(asyncClient
                .uploadBlob(content)
                .flatMap(uploadResult ->
                    asyncClient.downloadStream(uploadResult.getDigest()))
                .flatMap(r -> r.writeValueTo(Channels.newChannel(stream))))
            .verifyComplete();

        stream.flush();
        assertArrayEquals(content.toBytes(), stream.toByteArray());
    }

    private static BinaryData generateStream(int size) {
        byte[] content = new byte[size];
        for (int i = 0; i < size; i++) {
            content[i] = (byte) (i % 127);
        }
        return BinaryData.fromBytes(content);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifest(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();

        UploadManifestResult result = client.uploadManifest(MANIFEST);
        DownloadManifestResult downloadResult = client.downloadManifest(result.getDigest());
        OciManifest returnedManifest = downloadResult.asOciManifest();
        assertNotNull(returnedManifest);
        validateManifest(MANIFEST, returnedManifest);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestAsync(HttpClient httpClient) {
        asyncClient = getBlobAsyncClient("oci-artifact", httpClient);
        StepVerifier.create(
                uploadManifestPrerequisitesAsync()
                    .then(asyncClient.uploadManifest(MANIFEST))
                    .flatMap(result -> asyncClient.downloadManifest(result.getDigest())))
            .assertNext(downloadResult -> {
                OciManifest returnedManifest = downloadResult.asOciManifest();
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
        Response<DownloadManifestResult> manifestResult = client.downloadManifestWithResponse("latest", dockerListType, Context.NONE);
        assertNotNull(manifestResult.getValue());
        assertEquals(dockerListType, manifestResult.getValue().getMediaType());
        // does not throw
        ManifestList list = manifestResult.getValue().getContent().toObject(ManifestList.class);
        assertEquals(2, list.getSchemaVersion());
        assertEquals(dockerListType.toString(), list.getMediaType());
        assertEquals(11, list.getManifests().size());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifestListManifestAsync(HttpClient httpClient) {
        asyncClient = getBlobAsyncClient(HELLO_WORLD_REPOSITORY_NAME, httpClient);
        ManifestMediaType dockerListType = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");

        StepVerifier.create(asyncClient.downloadManifestWithResponse("latest", dockerListType))
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
}
