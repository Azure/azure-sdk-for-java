// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.OciBlobDescriptor;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
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

        Mono<OciBlobDescriptor> uploadConfig = blobClient
            .uploadBlob(configContent)
            .doOnSuccess(configUploadResult -> System.out.printf("Uploaded config: digest - %s, size - %s\n", configUploadResult.getDigest(), configContent.getLength()))
            .map(configUploadResult -> new OciBlobDescriptor()
                .setMediaType("application/vnd.unknown.config.v1+json")
                .setDigest(configUploadResult.getDigest())
                .setSize(configContent.getLength()));

        BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
        Mono<OciBlobDescriptor> uploadLayer = blobClient
            .uploadBlob(layerContent)
            .doOnSuccess(layerUploadResult -> System.out.printf("Uploaded layer: digest - %s, size - %s\n", layerUploadResult.getDigest(), layerContent.getLength()))
            .map(layerUploadResult -> new OciBlobDescriptor()
                .setDigest(layerUploadResult.getDigest())
                .setSize(layerContent.getLength())
                .setMediaType("application/octet-stream"));

        Mono.zip(uploadConfig, uploadLayer)
            .map(tuple -> new OciManifest()
                .setConfig(tuple.getT1())
                .setSchemaVersion(2)
                .setLayers(Collections.singletonList(tuple.getT2())))
            .flatMap(manifest -> blobClient.uploadManifest(new UploadManifestOptions(manifest).setTag("latest")))
            .doOnSuccess(manifestResult -> System.out.printf("Uploaded manifest: digest - %s\n", manifestResult.getDigest()))
            .block();
        // END: readme-sample-uploadImageAsync

        System.out.println("Done");
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
