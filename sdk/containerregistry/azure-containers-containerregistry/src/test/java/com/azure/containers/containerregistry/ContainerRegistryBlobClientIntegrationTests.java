// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.DownloadBlobResult;
import com.azure.containers.containerregistry.models.DownloadManifestOptions;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.OciAnnotations;
import com.azure.containers.containerregistry.models.OciBlobDescriptor;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;

import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.azure.containers.containerregistry.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.SAME_THREAD)
public class ContainerRegistryBlobClientIntegrationTests extends ContainerRegistryClientsTestBase {
    private ContainerRegistryBlobClient client;

    private ContainerRegistryBlobClient getBlobClient(String repositoryName, HttpClient httpClient) {
        return getBlobClientBuilder(repositoryName, httpClient).buildClient();
    }

    private static String configDigest;
    private static String layerDigest;
    private static String manifestDigest;
    private static BinaryData configData;
    private static BinaryData layerData;
    private static Boolean isWindowsOS;

    @BeforeAll
    static void beforeAll() {
        String layer = "654b93f61054e4ce90ed203bb8d556a6200d5f906cf3eca0620738d6dc18cbed";
        String config = "config.json";
        Path configPath = Paths.get("src", "test", "resources", "oci-artifact", config);
        Path layerPath = Paths.get("src", "test", "resources", "oci-artifact", layer);
        configData = BinaryData.fromFile(configPath);
        layerData = BinaryData.fromFile(layerPath);
        configDigest = UtilsImpl.computeDigest(configData.toByteBuffer());
        layerDigest = UtilsImpl.computeDigest(layerData.toByteBuffer());
        isWindowsOS = System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    @AfterEach
    void afterEach() {
        cleanupResources();
    }

    private void cleanupResources() {
        if (client == null) {
            return;
        }

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
        Assumptions.assumeTrue(interceptorManager.isLiveMode() || isWindowsOS);
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
        Assumptions.assumeTrue(interceptorManager.isLiveMode() || isWindowsOS);
        client = getBlobClient("oci-artifact", httpClient);
        uploadManifestPrerequisites();
        OciManifest manifest = createManifest();
        BinaryData manifestData = BinaryData.fromObject(manifest);

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
        Assumptions.assumeTrue(interceptorManager.isLiveMode() || isWindowsOS);
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
        Assumptions.assumeTrue(interceptorManager.isLiveMode() || isWindowsOS);
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
        Assumptions.assumeTrue(interceptorManager.isLiveMode() || isWindowsOS);
        client = getBlobClient("oci-artifact", httpClient);

        UploadBlobResult result = client.uploadBlob(configData);
        assertEquals(configDigest, result.getDigest());
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadBlob(HttpClient httpClient) {
        Assumptions.assumeTrue(interceptorManager.isLiveMode());
        client = getBlobClient("oci-artifact", httpClient);
        UploadBlobResult uploadResult = client.uploadBlob(configData);
        DownloadBlobResult downloadResult = client.downloadBlob(uploadResult.getDigest());

        assertEquals(uploadResult.getDigest(), downloadResult.getDigest());
        assertEquals(downloadResult.getContent().toString(), configData.toString());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void downloadManifest(HttpClient httpClient) {
        Assumptions.assumeTrue(interceptorManager.isLiveMode() || isWindowsOS);
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
