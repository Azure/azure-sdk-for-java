// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobAsyncClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClientBuilder;
import com.azure.core.util.io.IOUtils;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class DownloadImageAsync {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "samples/nginx";
    private static final ObjectMapper PRETTY_PRINT = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final String OUT_DIRECTORY = getTempDirectory();
    private static final DefaultAzureCredential CREDENTIAL = new DefaultAzureCredentialBuilder().build();
    private static final ManifestMediaType DOCKER_MANIFEST_LIST_TYPE = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
    private static final ManifestMediaType OCI_INDEX_TYPE = ManifestMediaType.fromString("application/vnd.oci.image.index.v1+json");

    public static void main(String[] args) {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        // BEGIN: readme-sample-downloadImageAsync
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        blobClient
            .downloadManifest("latest")
            .map(manifestResult -> manifestResult.asOciManifest())
            .doOnSuccess(manifest -> System.out.printf("Got manifest:\n%s\n", prettyPrint(manifest)))
            .flatMapMany(manifest -> {
                String configFileName = manifest.getConfig().getDigest() + ".json";

                Mono<Void> downloadConfig = blobClient
                        .downloadStream(manifest.getConfig().getDigest())
                        .flatMap(downloadResponse -> downloadResponse.writeValueToAsync(createFileChannel(configFileName)))
                        .doOnSuccess(i -> System.out.printf("Got config: %s\n", configFileName));

                Flux<Void> downloadLayers = Flux.fromIterable(manifest.getLayers())
                    .flatMap(layer -> blobClient
                        .downloadStream(layer.getDigest())
                        .flatMap(downloadResponse -> downloadResponse.writeValueToAsync(createFileChannel(layer.getDigest())))
                        .doOnSuccess(i -> System.out.printf("Got layer: %s\n", layer.getDigest())));

                return Flux.concat(downloadConfig, downloadLayers);
            })
            .blockLast();
        // END: readme-sample-downloadImageAsync

        System.out.println("Done");
    }

    private void downloadManifest() {
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.downloadManifestAsync
        blobClient.downloadManifest("latest")
            .doOnNext(downloadResult -> {
                if (ManifestMediaType.OCI_MANIFEST.equals(downloadResult.getMediaType())
                    || ManifestMediaType.DOCKER_MANIFEST.equals(downloadResult.getMediaType())) {
                    OciImageManifest manifest = downloadResult.asOciManifest();
                    System.out.println("Got OCI manifest");
                } else {
                    throw new IllegalArgumentException("Unexpected manifest type: " + downloadResult.getMediaType());
                }
            })
            .block();
        // END: com.azure.containers.containerregistry.downloadManifestAsync
    }

    private void downloadCustomManifestMediaType() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(credential)
            .buildAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.downloadCustomManifestAsync
        blobClient.downloadManifestWithResponse("latest", Arrays.asList(DOCKER_MANIFEST_LIST_TYPE, OCI_INDEX_TYPE))
            .doOnNext(downloadResult -> {
                if (DOCKER_MANIFEST_LIST_TYPE.equals(downloadResult.getValue().getMediaType())) {
                    // DockerManifestList manifestList =
                    //     downloadResult.getValue().getContent().toObject(DockerManifestList.class);
                    System.out.println("Got docker manifest list");
                } else if (OCI_INDEX_TYPE.equals(downloadResult.getValue().getMediaType())) {
                    // OciIndex ociIndex = downloadResult.getValue().getContent().toObject(OciIndex.class);
                    System.out.println("Got OCI index");
                } else {
                    throw new IllegalArgumentException("Got unexpected content type: "
                        + downloadResult.getValue().getMediaType());
                }
            })
            .block();
        // END: com.azure.containers.containerregistry.downloadCustomManifestAsync
    }

    private static String prettyPrint(OciImageManifest manifest) {
        try {
            return PRETTY_PRINT.writeValueAsString(manifest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static AsynchronousByteChannel createFileChannel(String name) {
        if (name.startsWith("sha256:")) {
            name = name.substring(7);
        }

        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(OUT_DIRECTORY, name), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            return IOUtils.toAsynchronousByteChannel(fileChannel, 0);
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
