// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.OciBlobDescriptor;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;

public class UploadImage {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "hello/world";

    public static void main(String[] args) {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        // BEGIN: readme-sample-uploadImage
        ContainerRegistryBlobClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(credential)
            .buildClient();

        BinaryData configContent = BinaryData.fromObject(new ManifestConfig().setProperty("sync client"));

        UploadBlobResult configUploadResult = blobClient.uploadBlob(configContent);
        System.out.printf("Uploaded config: digest - %s, size - %s\n", configUploadResult.getDigest(), configContent.getLength());

        OciBlobDescriptor configDescriptor = new OciBlobDescriptor()
            .setMediaType("application/vnd.unknown.config.v1+json")
            .setDigest(configUploadResult.getDigest())
            .setSize(configContent.getLength());

        BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
        UploadBlobResult layerUploadResult = blobClient.uploadBlob(layerContent);
        System.out.printf("Uploaded layer: digest - %s, size - %s\n", layerUploadResult.getDigest(), layerContent.getLength());

        OciManifest manifest = new OciManifest()
            .setConfig(configDescriptor)
            .setSchemaVersion(2)
            .setLayers(Collections.singletonList(
                new OciBlobDescriptor()
                    .setDigest(layerUploadResult.getDigest())
                    .setSize(layerContent.getLength())
                    .setMediaType("application/octet-stream")));

        UploadManifestResult manifestResult = blobClient.uploadManifest(new UploadManifestOptions(manifest).setTag("latest"));
        System.out.printf("Uploaded manifest: digest - %s\n", manifestResult.getDigest());
        // END: readme-sample-uploadImage

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
