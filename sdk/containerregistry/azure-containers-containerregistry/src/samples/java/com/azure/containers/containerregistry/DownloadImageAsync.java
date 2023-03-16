// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.core.util.FluxUtil;
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

public class DownloadImageAsync {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "samples/nginx";
    private static final ObjectMapper PRETTY_PRINT = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final String OUT_DIRECTORY = getTempDirectory();
    private static final DefaultAzureCredential CREDENTIAL = new DefaultAzureCredentialBuilder().build();

    public static void main(String[] args) {
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        // BEGIN: readme-sample-downloadImageAsync
        blobClient
            .downloadManifest("latest")
            .map(manifestResult -> manifestResult.asOciManifest())
            .doOnSuccess(manifest -> System.out.printf("Got manifest:\n%s\n", prettyPrint(manifest)))
            .flatMapMany(manifest -> {
                String configFileName = manifest.getConfig().getDigest() + ".json";
                FileChannel configChannel = createFileChannel(configFileName);

                Mono<Void> downloadConfig = blobClient
                        .downloadStream(manifest.getConfig().getDigest())
                        .flatMap(downloadResponse -> FluxUtil.writeToWritableByteChannel(downloadResponse.toFluxByteBuffer(), configChannel))
                        .doOnSuccess(i -> System.out.printf("Got config: %s\n", configFileName))
                        .doFinally(i -> closeStream(configChannel));

                Flux<Void> downloadLayers = Flux.fromIterable(manifest.getLayers())
                    .flatMap(layer -> {
                        FileChannel layerChannel = createFileChannel(layer.getDigest());
                        return blobClient.downloadStream(layer.getDigest())
                            .flatMap(downloadResponse -> FluxUtil.writeToWritableByteChannel(downloadResponse.toFluxByteBuffer(), layerChannel))
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
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();
        String digest = "sha256:6581596932dc735fd0df8cc240e6c28845a66829126da5ce25b983cf244e2311";

        // BEGIN: com.azure.containers.containerregistry.downloadStreamAsyncFile
        blobClient
            .downloadStream(digest)
            .flatMap(downloadResult ->
                Mono.using(() -> new FileOutputStream(trimSha(digest)),
                    fileStream -> FluxUtil.writeToWritableByteChannel(
                        downloadResult.toFluxByteBuffer(), fileStream.getChannel()),
                    fileStream -> closeStream(fileStream)))
            .block();
        // END: com.azure.containers.containerregistry.downloadStreamAsyncFile


        // BEGIN: com.azure.containers.containerregistry.downloadStreamAsyncSocket
        blobClient
            .downloadStream(digest)
            .flatMap(downloadResult ->
                Mono.using(
                    () -> openSocket(),
                    socket -> FluxUtil.writeToAsynchronousByteChannel(downloadResult.toFluxByteBuffer(), socket),
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
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.downloadManifestAsync
        blobClient.downloadManifest("latest")
            .doOnNext(downloadResult -> {
                if (ManifestMediaType.OCI_MANIFEST.equals(downloadResult.getManifestMediaType())
                    || ManifestMediaType.DOCKER_MANIFEST.equals(downloadResult.getManifestMediaType())) {
                    OciImageManifest manifest = downloadResult.asOciManifest();
                    System.out.println("Got OCI manifest");
                } else {
                    throw new IllegalArgumentException("Unexpected manifest type: " + downloadResult.getManifestMediaType());
                }
            })
            .block();
        // END: com.azure.containers.containerregistry.downloadManifestAsync
    }

    private static void downloadManifestWithResponse() {
        ContainerRegistryBlobAsyncClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repositoryName(REPOSITORY)
            .credential(CREDENTIAL)
            .buildAsyncClient();

        // BEGIN: com.azure.containers.containerregistry.downloadManifestWithResponseAsync
        blobClient.downloadManifestWithResponse("latest")
            .doOnNext(response -> {
                DownloadManifestResult manifestResult = response.getValue();
                if (ManifestMediaType.OCI_MANIFEST.equals(manifestResult.getManifestMediaType())
                    || ManifestMediaType.DOCKER_MANIFEST.equals(manifestResult.getManifestMediaType())) {
                    OciImageManifest manifest = manifestResult.asOciManifest();
                    System.out.println("Got OCI manifest");
                } else {
                    throw new IllegalArgumentException("Unexpected manifest type: " + manifestResult.getManifestMediaType());
                }
            })
            .block();
        // END: com.azure.containers.containerregistry.downloadManifestWithResponseAsync
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
