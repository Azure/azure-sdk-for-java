// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobAsyncClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.FileChannel;
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
                FileChannel configChannel = createFileChannel(configFileName);

                Mono<Void> downloadConfig = blobClient
                        .downloadStream(manifest.getConfig().getDigest())
                        .flatMap(downloadResponse -> downloadResponse.writeValueTo(configChannel))
                        .doOnSuccess(i -> System.out.printf("Got config: %s\n", configFileName))
                        .doFinally(i -> closeStream(configChannel));

                Flux<Void> downloadLayers = Flux.fromIterable(manifest.getLayers())
                    .flatMap(layer -> {
                        FileChannel layerChannel = createFileChannel(layer.getDigest());
                        return blobClient.downloadStream(layer.getDigest())
                            .flatMap(downloadResponse -> downloadResponse.writeValueTo(layerChannel))
                            .doOnSuccess(i -> System.out.printf("Got layer: %s\n", layer.getDigest()))
                            .doFinally(i -> closeStream(layerChannel));
                    });

                return Flux.concat(downloadConfig, downloadLayers);
            })
            .blockLast();
        // END: readme-sample-downloadImageAsync

        System.out.println("Done");
    }

    private static void downloadBlob() {
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();
        String digest = "sha256:6581596932dc735fd0df8cc240e6c28845a66829126da5ce25b983cf244e2311";

        // BEGIN: com.azure.containers.containerregistry.downloadStreamAsyncFile
        blobClient
            .downloadStream(digest)
            .flatMap(downloadResult ->
                Mono.using(() -> new FileOutputStream(trimSha(digest)),
                    fileStream -> downloadResult.writeValueTo(fileStream.getChannel()),
                    fileStream -> closeStream(fileStream)))
            .block();
        // END: com.azure.containers.containerregistry.downloadStreamAsyncFile


        // BEGIN: com.azure.containers.containerregistry.downloadStreamAsyncSocket
        blobClient
            .downloadStream(digest)
            .flatMap(downloadResult ->
                Mono.using(
                    () -> openSocket(),
                    socket -> downloadResult.writeValueToAsync(socket),
                    socket -> closeStream(socket)))
            .block();
        // END: com.azure.containers.containerregistry.downloadStreamAsyncSocket
    }

    private static FileChannel createFileChannel(String name) {
        try {
            return FileChannel.open(Paths.get(OUT_DIRECTORY, trimSha(name)), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static AsynchronousSocketChannel openSocket() {
        // new AsynchronousSocketChannel(...).bind(...);
        return null;
    }

    private static void downloadManifest() {
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

    private static void downloadCustomManifestMediaType() {
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

    private static String trimSha(String digest) {
        return digest.startsWith("sha256:") ? digest.substring(7) : digest;
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

    private static void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
