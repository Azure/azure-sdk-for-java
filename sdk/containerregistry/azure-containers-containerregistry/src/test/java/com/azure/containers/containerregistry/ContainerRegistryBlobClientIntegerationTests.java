// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.OciAnnotations;
import com.azure.containers.containerregistry.models.OciBlobDescriptor;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobAsyncClient;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.azure.containers.containerregistry.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class ContainerRegistryBlobClientIntegerationTests extends ContainerRegistryClientsTestBase {
    private ContainerRegistryBlobAsyncClient asyncClient;

    private ContainerRegistryBlobAsyncClient getBlobClientAsync(String repositoryName, HttpClient httpClient) {
        return getBlobClientBuilder(repositoryName, httpClient).buildAsyncClient();
    }

    private static OciManifest createManifest() {
        OciManifest manifest = new OciManifest()
            .setSchemaVersion(2)
            .setConfig(new OciBlobDescriptor()
                .setMediaType("application/vnd.acme.rocket.config.v1+json")
                .setDigest("sha256:ed4ae817c4ca4527071a5e7485ab22dee42091a868ae996429f4129a0bb5fb14")
                .setSize(41L));


        List<OciBlobDescriptor> layers = new ArrayList<>();

        layers.add(new OciBlobDescriptor()
            .setMediaType("text/plain")
            .setSize(16L)
            .setDigest("sha256:9780097f396c718281149a598c96484ed34fea9213cfe7f2f0e0a4832b5ae14b")
            .setAnnotations(new OciAnnotations()
                .setName("artifact.txt")));

        manifest.setLayers(layers);

        return manifest;
    }

    private void uploadManifestPrerequisites() {
        String layer = "artifact.txt";
        String config = "config.json";
        Path configPath = Paths.get("src", "test", "resources", "oci-artifact", config);
        Path layerPath = Paths.get("src", "test", "resources", "oci-artifact", layer);

        asyncClient.uploadBlob(BinaryData.fromFile(configPath)).block();
        asyncClient.uploadBlob(BinaryData.fromFile(layerPath)).block();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    @EnabledOnOs(value = {OS.WINDOWS}, disabledReason = "OCI manifest are OS specific. Will need another test for Linux and Mac.")
    public void uploadManifest(HttpClient httpClient) {
        asyncClient = getBlobClientAsync("oci-artifact", httpClient);
        uploadManifestPrerequisites();
        OciManifest manifest = createManifest();

        Mono<Void> validateUpload = asyncClient.uploadManifest(manifest)
            .flatMap(uploadManifestResult -> {
                if (uploadManifestResult == null || uploadManifestResult.getDigest() == null) {
                    return Mono.error(new NullPointerException("digest is null"));
                }

                Assertions.assertEquals("sha256:0b4d72678898b0ce0b3ebc06c4309f64ebaf6d8f13fa5aa968dc82c217795a82", uploadManifestResult.getDigest());
                return Mono.just(uploadManifestResult.getDigest());
            }).flatMap(digest -> asyncClient.deleteManifest(digest));

        StepVerifier.create(validateUpload)
            .verifyComplete();
    }
}
