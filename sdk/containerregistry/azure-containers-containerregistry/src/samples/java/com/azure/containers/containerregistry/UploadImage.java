// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class UploadImage {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "hello/world";
    private static final DefaultAzureCredential CREDENTIAL = new DefaultAzureCredentialBuilder().build();
    private static final ManifestMediaType DOCKER_MANIFEST_LIST_TYPE = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
    public static void main(String[] args) {

        ContainerRepository repository = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

        // BEGIN: readme-sample-uploadImage
        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        UploadBlobResult configUploadResult = repository.uploadBlob(configContent);
        System.out.printf("Uploaded config: digest - %s, size - %s\n", configUploadResult.getDigest(), configContent.getLength());

        OciDescriptor configDescriptor = new OciDescriptor()
            .setMediaType("application/vnd.unknown.config.v1+json")
            .setDigest(configUploadResult.getDigest())
            .setSizeInBytes(configContent.getLength());

        BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
        UploadBlobResult layerUploadResult = repository.uploadBlob(layerContent);
        System.out.printf("Uploaded layer: digest - %s, size - %s\n", layerUploadResult.getDigest(), layerContent.getLength());

        OciImageManifest manifest = new OciImageManifest()
            .setConfig(configDescriptor)
            .setSchemaVersion(2)
            .setLayers(Collections.singletonList(
                new OciDescriptor()
                    .setDigest(layerUploadResult.getDigest())
                    .setSizeInBytes(layerContent.getLength())
                    .setMediaType("application/octet-stream")));

        UploadManifestResult manifestResult = repository.setManifest(manifest, "latest");
        System.out.printf("Uploaded manifest: digest - %s\n", manifestResult.getDigest());
        // END: readme-sample-uploadImage

        System.out.println("Done");
    }

    private void uploadBlobBinaryData() {
        ContainerRepository blobClient = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

        // BEGIN: com.azure.containers.containerregistry.uploadBlob
        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        UploadBlobResult uploadResult = blobClient.uploadBlob(configContent);
        System.out.printf("Uploaded blob: digest - '%s', size - %s\n", uploadResult.getDigest(), uploadResult.getSizeInBytes());
        // END: com.azure.containers.containerregistry.uploadBlob
    }

    private void uploadStream() throws IOException {
        ContainerRepository blobClient = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

        // BEGIN: com.azure.containers.containerregistry.uploadStream
        try (FileInputStream content = new FileInputStream("artifact.tar.gz")) {
            UploadBlobResult uploadResult = blobClient.uploadBlob(content.getChannel(), Context.NONE);
            System.out.printf("Uploaded blob: digest - '%s', size - %s\n",
                uploadResult.getDigest(), uploadResult.getSizeInBytes());
        }
        // END: com.azure.containers.containerregistry.uploadStream
    }

    private void uploadManifest() {
        ContainerRepository blobClient = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        UploadBlobResult configUploadResult = blobClient.uploadBlob(configContent);

        OciDescriptor configDescriptor = new OciDescriptor()
            .setMediaType("application/vnd.unknown.config.v1+json")
            .setDigest(configUploadResult.getDigest())
            .setSizeInBytes(configContent.getLength());

        BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
        UploadBlobResult layerUploadResult = blobClient.uploadBlob(layerContent);

        OciImageManifest manifest = new OciImageManifest()
            .setConfig(configDescriptor)
            .setSchemaVersion(2)
            .setLayers(Collections.singletonList(
                new OciDescriptor()
                    .setDigest(layerUploadResult.getDigest())
                    .setSizeInBytes(layerContent.getLength())
                    .setMediaType("application/octet-stream")));

        // BEGIN: com.azure.containers.containerregistry.uploadManifest
        blobClient.setManifest(manifest, "v1");
        // END: com.azure.containers.containerregistry.uploadManifest
    }

    private void uploadCustomManifestMediaType() {
        ContainerRepository blobClient = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

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
        UploadManifestOptions options = new UploadManifestOptions(manifestList, DOCKER_MANIFEST_LIST_TYPE);

        Response<UploadManifestResult> response = blobClient.setManifestWithResponse(options, Context.NONE);
        System.out.println("Manifest uploaded, digest - " + response.getValue().getDigest());
        // END: com.azure.containers.containerregistry.uploadCustomManifest
    }

    private static FileInputStream getFileStream(String name) {
        try {
            return new FileInputStream(name);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String getTempDirectory() {
        String outDir = null;
        try {
            outDir = Files.createTempDirectory(null).toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.printf("Writing content to %s\n", outDir);
        return outDir;
    }
}
