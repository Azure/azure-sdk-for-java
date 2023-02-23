// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.DownloadManifestOptions;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
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
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

import static com.azure.containers.containerregistry.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.containers.containerregistry.TestUtils.SKIP_AUTH_TOKEN_REQUEST_FUNCTION;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.convertToJson;
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

    private static String configDigest;
    private static String layerDigest;
    private static String manifestDigest;
    private static BinaryData configData;
    private static BinaryData layerData;

    @BeforeAll
    static void beforeAll() {
        configData = BinaryData.fromString("{}");
        layerData = BinaryData.fromString("hello world");
        configDigest = UtilsImpl.computeDigest(configData.toByteBuffer());
        assertEquals("sha256:44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a", configDigest);
        layerDigest = UtilsImpl.computeDigest(layerData.toByteBuffer());
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
            if (configDigest != null) {
                asyncClient.deleteBlob(configDigest).block();
            }
            if (layerDigest != null) {
                asyncClient.deleteBlob(layerDigest).block();
            }

            if (manifestDigest != null) {
                asyncClient.deleteManifest(manifestDigest).block();
            }
        } else if (client != null) {
            if (configDigest != null) {
                client.deleteBlob(configDigest);
            }
            if (layerDigest != null) {
                client.deleteBlob(layerDigest);
            }

            if (manifestDigest != null) {
                client.deleteManifest(manifestDigest);
            }
        }
    }

    private OciManifest createManifest() {
        OciManifest manifest = new OciManifest()
            .setSchemaVersion(2)
            .setConfig(new OciBlobDescriptor()
                .setMediaType("application/vnd.acme.rocket.config.v1+json")
                .setDigest(configDigest)
                .setSize(171L));


        List<OciBlobDescriptor> layers = new ArrayList<>();

        layers.add(new OciBlobDescriptor()
            .setMediaType("application/vnd.oci.image.layer.v1.tar")
            .setSize(28L)
            .setDigest(layerDigest)
            .setAnnotations(new OciAnnotations()
                .setName("654b93f61054e4ce90ed203bb8d556a6200d5f906cf3eca0620738d6dc18cbed")));

        manifest.setLayers(layers);
        return manifest;
    }

    private void uploadManifestPrerequisites() {
        UploadBlobResult result = client.uploadBlob(configData);
        assertEquals(configDigest, result.getDigest());
        result = client.uploadBlob(layerData);
        assertEquals(layerDigest, result.getDigest());
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
        OciManifest manifest = createManifest();

        UploadManifestResult result = client.uploadManifest(manifest);
        assertNotNull(result);
        assertNotNull(result.getDigest());
        manifestDigest = result.getDigest();

        DownloadManifestOptions options = DownloadManifestOptions.fromDigest(manifestDigest);
        DownloadManifestResult downloadManifestResult = client.downloadManifest(options);
        assertEquals(manifestDigest, downloadManifestResult.getDigest());
        validateManifest(manifest, downloadManifestResult.getManifest());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestBinaryData(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();
        OciManifest manifest = createManifest();
        BinaryData manifestData = BinaryData.fromString(convertToJson(manifest));

        UploadManifestResult result = client.uploadManifest(new UploadManifestOptions(manifestData));
        assertNotNull(result);
        assertNotNull(result.getDigest());
        manifestDigest = result.getDigest();

        DownloadManifestOptions options = DownloadManifestOptions.fromDigest(manifestDigest);
        DownloadManifestResult downloadManifestResult = client.downloadManifest(options);
        assertEquals(manifestDigest, downloadManifestResult.getDigest());
        validateManifest(manifest, downloadManifestResult.getManifestStream());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestViaOptions(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();
        OciManifest manifest = createManifest();

        UploadManifestResult result = client.uploadManifest(new UploadManifestOptions(manifest));
        assertNotNull(result);
        assertNotNull(result.getDigest());
        manifestDigest = result.getDigest();

        DownloadManifestOptions options = DownloadManifestOptions.fromDigest(manifestDigest);
        DownloadManifestResult downloadManifestResult = client.downloadManifest(options);
        assertEquals(manifestDigest, downloadManifestResult.getDigest());
        validateManifest(manifest, downloadManifestResult.getManifest());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadOciManifestWithTag(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);
        String tag = "v1";
        uploadManifestPrerequisites();
        OciManifest manifest = createManifest();

        UploadManifestResult result = client.uploadManifest(new UploadManifestOptions(manifest).setTag(tag));
        assertNotNull(result);
        assertNotNull(result.getDigest());
        manifestDigest = result.getDigest();

        DownloadManifestOptions options = DownloadManifestOptions.fromDigest(manifestDigest);
        DownloadManifestResult downloadManifestResult = client.downloadManifest(options);
        assertEquals(manifestDigest, downloadManifestResult.getDigest());
        validateManifest(manifest, downloadManifestResult.getManifest());

        ContainerRegistryClient acrClient = getContainerRegistryBuilder(httpClient).buildClient();
        RegistryArtifact artifact = acrClient.getArtifact("oci-artifact", manifestDigest);
        PagedIterable<ArtifactTagProperties> tags = artifact.listTagProperties();
        assertEquals(1, tags.stream().count());
        assertEquals(tag, tags.stream().findFirst().get().getName());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void canUploadBlob(HttpClient httpClient) {
        client = getBlobClient("oci-artifact", httpClient);

        UploadBlobResult result = client.uploadBlob(configData);
        assertEquals(configDigest, result.getDigest());
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
        OciManifest manifest = createManifest();
        UploadManifestResult result = client.uploadManifest(manifest);
        DownloadManifestResult downloadResult = client.downloadManifest(DownloadManifestOptions.fromDigest(result.getDigest()));
        OciManifest returnedManifest = downloadResult.getManifest();
        assertNotNull(returnedManifest);
        validateManifest(manifest, returnedManifest);
    }
}
