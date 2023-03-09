// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactArchitecture;
import com.azure.containers.containerregistry.models.ArtifactOperatingSystem;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobAsyncClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class UploadImageAsync {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "hello/world";

    public static void main(String[] args) {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        // BEGIN: readme-sample-uploadImageAsync
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(credential)
            .buildAsyncClient();

        BinaryData configContent = BinaryData.fromObject(new ManifestConfig().setProperty("async client"));

        Mono<OciDescriptor> uploadConfig = blobClient
            .uploadBlob(configContent)
            .doOnSuccess(configUploadResult -> System.out.printf("Uploaded config: digest - %s, size - %s\n", configUploadResult.getDigest(), configContent.getLength()))
            .map(configUploadResult -> new OciDescriptor()
                .setMediaType("application/vnd.unknown.config.v1+json")
                .setDigest(configUploadResult.getDigest())
                .setSizeInBytes(configContent.getLength()));

        BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
        Mono<OciDescriptor> uploadLayer = blobClient
            .uploadBlob(layerContent)
            .doOnSuccess(layerUploadResult -> System.out.printf("Uploaded layer: digest - %s, size - %s\n", layerUploadResult.getDigest(), layerContent.getLength()))
            .map(layerUploadResult -> new OciDescriptor()
                .setDigest(layerUploadResult.getDigest())
                .setSizeInBytes(layerContent.getLength())
                .setMediaType("application/octet-stream"));

        Mono.zip(uploadConfig, uploadLayer)
            .map(tuple -> new OciImageManifest()
                .setConfig(tuple.getT1())
                .setSchemaVersion(2)
                .setLayers(Collections.singletonList(tuple.getT2())))
            .flatMap(manifest -> blobClient.uploadManifest(new UploadManifestOptions(manifest).setTag("latest")))
            .doOnSuccess(manifestResult -> System.out.printf("Uploaded manifest: digest - %s\n", manifestResult.getDigest()))
            .block();
        // END: readme-sample-uploadImageAsync

        System.out.println("Done");
    }

    private void uploadCustomManifestMediaType() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(credential)
            .buildAsyncClient();

        ManifestMediaType manifestListType = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
        DockerV2ManifestList manifestList = new DockerV2ManifestList()
            .setSchemaVersion(2)
            .setMediaType(manifestListType.toString())
            .setManifests(Collections.singletonList(new DockerV2ManifestList.DockerV2ManifestListAttributes()
                .setDigest("sha256:f54a58bc1aac5ea1a25d796ae155dc228b3f0e11d046ae276b39c4bf2f13d8c4")
                .setMediaType(ManifestMediaType.DOCKER_MANIFEST.toString())
                .setPlatform(new DockerV2ManifestList.Platform()
                    .setArchitecture(ArtifactArchitecture.AMD64.toString())
                    .setOs(ArtifactOperatingSystem.LINUX.toString())))
            );

        UploadManifestResult result = blobClient.uploadManifest(new UploadManifestOptions(BinaryData.fromObject(manifestList), manifestListType))
            .block();

        System.out.println("Manifest uploaded, digest - " + result.getDigest());
    }

    private static class ManifestConfig {
        @JsonProperty("property")
        private String property;

        public String getProperty() {
            return property;
        }

        public ManifestConfig setProperty(String property) {
            this.property = property;
            return this;
        }
    }
}
