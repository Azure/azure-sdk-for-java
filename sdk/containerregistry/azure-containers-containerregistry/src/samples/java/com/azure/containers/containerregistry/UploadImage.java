// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.SetManifestOptions;
import com.azure.containers.containerregistry.models.SetManifestResult;
import com.azure.containers.containerregistry.models.UploadRegistryBlobResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Paths;
import java.util.Collections;

import static reactor.netty.Metrics.CHUNK_SIZE;

public class UploadImage {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "hello/world";
    private static final DefaultAzureCredential CREDENTIAL = new DefaultAzureCredentialBuilder().build();
    private static final ManifestMediaType DOCKER_MANIFEST_LIST_TYPE = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
    public static void main(String[] args) {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        // BEGIN: readme-sample-uploadImage
        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        UploadRegistryBlobResult configUploadResult = contentClient.uploadBlob(configContent);
        System.out.printf("Uploaded config: digest - %s, size - %s\n", configUploadResult.getDigest(), configContent.getLength());

        OciDescriptor configDescriptor = new OciDescriptor()
            .setMediaType("application/vnd.unknown.config.v1+json")
            .setDigest(configUploadResult.getDigest())
            .setSizeInBytes(configContent.getLength());

        BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
        UploadRegistryBlobResult layerUploadResult = contentClient.uploadBlob(layerContent);
        System.out.printf("Uploaded layer: digest - %s, size - %s\n", layerUploadResult.getDigest(), layerContent.getLength());

        OciImageManifest manifest = new OciImageManifest()
            .setConfiguration(configDescriptor)
            .setSchemaVersion(2)
            .setLayers(Collections.singletonList(
                new OciDescriptor()
                    .setDigest(layerUploadResult.getDigest())
                    .setSizeInBytes(layerContent.getLength())
                    .setMediaType("application/octet-stream")));

        SetManifestResult manifestResult = contentClient.setManifest(manifest, "latest");
        System.out.printf("Uploaded manifest: digest - %s\n", manifestResult.getDigest());
        // END: readme-sample-uploadImage

        System.out.println("Done");
    }

    private void uploadBlobBinaryData() {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        // BEGIN: com.azure.containers.containerregistry.uploadBlob
        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        UploadRegistryBlobResult uploadResult = contentClient.uploadBlob(configContent);
        System.out.printf("Uploaded blob: digest - '%s', size - %s\n", uploadResult.getDigest(), uploadResult.getSizeInBytes());
        // END: com.azure.containers.containerregistry.uploadBlob
    }

    private void uploadBlobFails() {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        // check out https://github.com/Azure/azure-sdk-for-java/issues/34276 for more details

        // BEGIN: com.azure.containers.containerregistry.uploadBlobErrorHandling
        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        try {
            UploadRegistryBlobResult uploadResult = contentClient.uploadBlob(configContent);
            System.out.printf("Uploaded blob: digest - '%s', size - %s\n", uploadResult.getDigest(),
                uploadResult.getSizeInBytes());
        } catch (HttpResponseException ex) {
            if (ex.getValue() instanceof ResponseError) {
                ResponseError error = (ResponseError) ex.getValue();
                System.out.printf("Upload failed: code '%s'\n", error.getCode());
                if ("BLOB_UPLOAD_INVALID".equals(error.getCode())) {
                    System.out.println("Transient upload issue, starting upload over");
                    // retry upload
                }
            }
        }
        // END: com.azure.containers.containerregistry.uploadBlobErrorHandling
    }

    private void uploadStream() {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        // BEGIN: com.azure.containers.containerregistry.uploadFile
        BinaryData content = BinaryData.fromFile(Paths.get("artifact.tar.gz", CHUNK_SIZE));
        UploadRegistryBlobResult uploadResult = contentClient.uploadBlob(content, Context.NONE);
        System.out.printf("Uploaded blob: digest - '%s', size - %s\n",
            uploadResult.getDigest(), uploadResult.getSizeInBytes());
        // END: com.azure.containers.containerregistry.uploadFile
    }

    private void setManifest() {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        UploadRegistryBlobResult configUploadResult = contentClient.uploadBlob(configContent);

        OciDescriptor configDescriptor = new OciDescriptor()
            .setMediaType("application/vnd.unknown.config.v1+json")
            .setDigest(configUploadResult.getDigest())
            .setSizeInBytes(configContent.getLength());

        BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
        UploadRegistryBlobResult layerUploadResult = contentClient.uploadBlob(layerContent);

        OciImageManifest manifest = new OciImageManifest()
            .setConfiguration(configDescriptor)
            .setSchemaVersion(2)
            .setLayers(Collections.singletonList(
                new OciDescriptor()
                    .setDigest(layerUploadResult.getDigest())
                    .setSizeInBytes(layerContent.getLength())
                    .setMediaType("application/octet-stream")));

        // BEGIN: com.azure.containers.containerregistry.setManifest
        contentClient.setManifest(manifest, "v1");
        // END: com.azure.containers.containerregistry.setManifest
    }

    private void uploadCustomManifestMediaType() {
        ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        // get manifest in custom format as string or an object of your custom type
        String manifest = "{"
            + "\"schemaVersion\": 2,"
            + "\"mediaType\": \"application/vnd.docker.distribution.manifest.list.v2+json\","
            + "\"manifests\": ["
            + "{"
            + "\"mediaType\": \"application/vnd.docker.distribution.manifest.v2+json\","
            +   "\"digest\": \"sha256:e692418e4cbaf90ca69d05a66403747baa33ee08806650b51fab815ad7fc331f\","
            +   "\"size\": 7143,"
            +   "\"platform\": { \"architecture\": \"ppc64le\", \"os\": \"linux\" }"
            + "},{"
            +   "\"mediaType\": \"application/vnd.docker.distribution.manifest.v2+json\","
            +   "\"digest\": \"sha256:5b0bcabd1ed22e9fb1310cf6c2dec7cdef19f0ad69efa1f392e94a4333501270\","
            +   "\"size\": 7682,"
            +   "\"platform\": { \"architecture\": \"amd64\", \"os\": \"linux\", \"features\": [\"sse4\"]}"
            + "}]}";
        // then create a binary data from it
        BinaryData manifestList = BinaryData.fromString(manifest);

        // BEGIN: com.azure.containers.containerregistry.uploadCustomManifest
        SetManifestOptions options = new SetManifestOptions(manifestList, DOCKER_MANIFEST_LIST_TYPE);

        Response<SetManifestResult> response = contentClient.setManifestWithResponse(options, Context.NONE);
        System.out.println("Manifest uploaded, digest - " + response.getValue().getDigest());
        // END: com.azure.containers.containerregistry.uploadCustomManifest
    }
}
