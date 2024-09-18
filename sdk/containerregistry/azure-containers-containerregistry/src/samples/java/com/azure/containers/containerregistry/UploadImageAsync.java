// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.models.AcrErrorInfo;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.SetManifestOptions;
import com.azure.containers.containerregistry.models.SetManifestResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Random;

public class UploadImageAsync {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "hello/world";
    private static final DefaultAzureCredential CREDENTIAL = new DefaultAzureCredentialBuilder().build();
    private static final ManifestMediaType DOCKER_MANIFEST_LIST_TYPE = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
    private static final int CHUNK_SIZE = 4 * 1024 * 1024; // content will be uploaded in chunks of up to 4MB size
    public static void main(String[] args) {

        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        // BEGIN: readme-sample-uploadImageAsync
        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        Mono<OciDescriptor> uploadConfig = contentClient
            .uploadBlob(configContent)
            .map(result -> new OciDescriptor()
                .setMediaType("application/vnd.unknown.config.v1+json")
                .setDigest(result.getDigest())
                .setSizeInBytes(result.getSizeInBytes()));

        long dataLength = 1024 * 1024 * 1024;
        Mono<BinaryData> layerContent = BinaryData.fromFlux(getData(dataLength), dataLength, false); // 1 GB
        Mono<OciDescriptor> uploadLayer =
            layerContent.flatMap(content -> contentClient.uploadBlob(content))
            .map(result -> new OciDescriptor()
                .setDigest(result.getDigest())
                .setSizeInBytes(result.getSizeInBytes())
                .setMediaType("application/octet-stream"));

        Mono.zip(uploadConfig, uploadLayer)
            .map(tuple -> new OciImageManifest()
                .setConfiguration(tuple.getT1())
                .setSchemaVersion(2)
                .setLayers(Collections.singletonList(tuple.getT2())))
            .flatMap(manifest -> contentClient.setManifest(manifest, "latest"))
            .doOnSuccess(manifestResult -> System.out.printf("Uploaded manifest: digest - %s\n", manifestResult.getDigest()))
            .block();
        // END: readme-sample-uploadImageAsync

        System.out.println("Done");
    }

    private static Flux<ByteBuffer> getData(long size) {
        Random rand = new Random(42);
        byte[] data = new byte[12 * 1024 * 1024];
        rand.nextBytes(data);
        return Flux.generate(() -> 0L, (pos, sink) -> {
            long remaining = size - pos;
            if (remaining <= 0) {
                sink.complete();
                return size;
            }

            ByteBuffer buffer = ByteBuffer.wrap(data);
            if (remaining < data.length) {
                buffer.limit((int) remaining);
            }
            sink.next(buffer);

            return pos + data.length;
        });
    }

    private void setManifest() {
        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        Mono<OciDescriptor> config = contentClient
            .uploadBlob(configContent)
            .map(configUploadResult -> new OciDescriptor()
                .setMediaType("application/vnd.unknown.config.v1+json")
                .setDigest(configUploadResult.getDigest())
                .setSizeInBytes(configContent.getLength()));

        long dataLength = 1024 * 1024 * 1024;
        Mono<BinaryData> layerContent = BinaryData.fromFlux(getData(dataLength), dataLength, false); // 1 GB
        Mono<OciDescriptor> layer = layerContent
            .flatMap(content -> contentClient.uploadBlob(content))
            .map(result -> new OciDescriptor()
                .setDigest(result.getDigest())
                .setSizeInBytes(result.getSizeInBytes())
                .setMediaType("application/octet-stream"));

        config
            .flatMap(configDescriptor ->
                layer.flatMap(layerDescriptor -> {
                    // BEGIN: com.azure.containers.containerregistry.setManifestAsync
                    OciImageManifest manifest = new OciImageManifest()
                            .setConfiguration(configDescriptor)
                            .setSchemaVersion(2)
                            .setLayers(Collections.singletonList(layerDescriptor));
                    Mono<SetManifestResult> result = contentClient.setManifest(manifest, "latest");
                    // END: com.azure.containers.containerregistry.setManifestAsync
                    return result;
                }))
            .subscribe(result -> System.out.println("Manifest uploaded, digest - " + result.getDigest()));
    }

    private void uploadCustomManifestMediaType() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(credential)
            .buildAsyncClient();

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

        // BEGIN: com.azure.containers.containerregistry.uploadCustomManifestAsync
        SetManifestOptions options = new SetManifestOptions(manifestList, DOCKER_MANIFEST_LIST_TYPE)
            .setTag("v2");

        contentClient.setManifestWithResponse(options)
            .subscribe(response ->
                System.out.println("Manifest uploaded, digest - " + response.getValue().getDigest()));
        // END: com.azure.containers.containerregistry.uploadCustomManifestAsync
    }

    private void uploadBlob() {
        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.uploadBlobAsync
        BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

        contentClient
            .uploadBlob(configContent)
            .subscribe(uploadResult -> System.out.printf("Uploaded blob: digest - '%s', size - %s\n",
                    uploadResult.getDigest(), uploadResult.getSizeInBytes()));
        // END: com.azure.containers.containerregistry.uploadBlobAsync

        // BEGIN: com.azure.containers.containerregistry.uploadFileAsync
        contentClient.uploadBlob(BinaryData.fromFile(Paths.get("artifact.tar.gz"), CHUNK_SIZE))
            .subscribe(uploadResult ->
                System.out.printf("Uploaded blob: digest - '%s', size - %s\n",
                    uploadResult.getDigest(), uploadResult.getSizeInBytes()));
        // END: com.azure.containers.containerregistry.uploadFileAsync
    }

    private void uploadBlobFails() {
        ContainerRegistryContentAsyncClient contentClient = new ContainerRegistryContentClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        long dataLength = 1024 * 1024 * 1024;
        Mono<BinaryData> layerContent = BinaryData.fromFlux(getData(dataLength), dataLength, false); // 1 GB

        // BEGIN: com.azure.containers.containerregistry.uploadBlobAsyncErrorHandling
        layerContent
            .flatMap(content -> contentClient.uploadBlob(content))
            .doOnError(HttpResponseException.class, (ex) -> {
                if (ex.getCause() instanceof AcrErrorsException) {
                    AcrErrorsException acrErrors = (AcrErrorsException) ex.getCause();
                    for (AcrErrorInfo info : acrErrors.getValue().getErrors()) {
                        System.out.printf("Uploaded blob failed: code '%s'\n", info.getCode());
                    }
                }
            });
        // END: com.azure.containers.containerregistry.uploadBlobAsyncErrorHandling
    }
}
