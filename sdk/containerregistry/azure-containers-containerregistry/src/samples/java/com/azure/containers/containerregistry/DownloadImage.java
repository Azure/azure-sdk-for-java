// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClientBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class DownloadImage {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "samples/nginx";
    private static final String OUT_DIRECTORY = getTempDirectory();
    private static final ObjectMapper PRETTY_PRINT = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final DefaultAzureCredential CREDENTIAL = new DefaultAzureCredentialBuilder().build();
    private static final ManifestMediaType DOCKER_MANIFEST_LIST_TYPE = ManifestMediaType.fromString("application/vnd.docker.distribution.manifest.list.v2+json");
    private static final ManifestMediaType OCI_INDEX_TYPE = ManifestMediaType.fromString("application/vnd.oci.image.index.v1+json");
    public static void main(String[] args) throws IOException {
        // BEGIN: readme-sample-downloadImage
        ContainerRegistryBlobClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        DownloadManifestResult manifestResult = blobClient.downloadManifest("latest");

        OciImageManifest manifest = manifestResult.asOciManifest();
        System.out.printf("Got manifest:\n%s\n", PRETTY_PRINT.writeValueAsString(manifest));

        String configFileName = manifest.getConfig().getDigest() + ".json";
        blobClient.downloadStream(manifest.getConfig().getDigest(), createFileChannel(configFileName));
        System.out.printf("Got config: %s\n", configFileName);

        for (OciDescriptor layer : manifest.getLayers()) {
            blobClient.downloadStream(layer.getDigest(), createFileChannel(layer.getDigest()));
            System.out.printf("Got layer: %s\n", layer.getDigest());
        }
        // END: readme-sample-downloadImage

        System.out.println("Done");
    }

    private static SeekableByteChannel createFileChannel(String fileName) throws IOException {
        if (fileName.startsWith("sha256:")) {
            fileName = fileName.substring(7);
        }
        return Files.newByteChannel(Paths.get(OUT_DIRECTORY, fileName), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
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

    private void downloadStream() throws IOException {
        ContainerRegistryBlobClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        String digest = "sha256:6581596932dc735fd0df8cc240e6c28845a66829126da5ce25b983cf244e2311";

        // BEGIN: com.azure.containers.containerregistry.downloadStream
        Path file = Files.createTempFile(digest, ".tmp");
        SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        blobClient.downloadStream(digest, channel);
        // END: com.azure.containers.containerregistry.downloadStream
    }

    private void downloadManifest() {
        ContainerRegistryBlobClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        // BEGIN: com.azure.containers.containerregistry.downloadManifestTag
        DownloadManifestResult latestResult = blobClient.downloadManifest("latest");
        if (ManifestMediaType.DOCKER_MANIFEST.equals(latestResult.getMediaType())
            || ManifestMediaType.OCI_MANIFEST.equals(latestResult.getMediaType())) {
            OciImageManifest manifest = latestResult.asOciManifest();
        } else {
            throw new IllegalArgumentException("Unexpected manifest type: " + latestResult.getMediaType());
        }
        // END: com.azure.containers.containerregistry.downloadManifestTag

        // BEGIN: com.azure.containers.containerregistry.downloadManifestDigest
        DownloadManifestResult digestResult = blobClient.downloadManifest(
            "sha256:6581596932dc735fd0df8cc240e6c28845a66829126da5ce25b983cf244e2311");
        // END: com.azure.containers.containerregistry.downloadManifestDigest
    }

    private void downloadCustomManifest() {
        ContainerRegistryBlobClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(CREDENTIAL)
            .buildClient();

        // BEGIN: com.azure.containers.containerregistry.downloadCustomManifest
        Response<DownloadManifestResult> response = blobClient.downloadManifestWithResponse(
            "latest",
            Arrays.asList(DOCKER_MANIFEST_LIST_TYPE, OCI_INDEX_TYPE),
            Context.NONE);
        if (DOCKER_MANIFEST_LIST_TYPE.equals(response.getValue().getMediaType())) {
            // DockerManifestList manifestList = downloadResult.getValue().getContent().toObject(DockerManifestList.class);
            System.out.println("Got docker manifest list");
        } else if (OCI_INDEX_TYPE.equals(response.getValue().getMediaType())) {
            // OciIndex ociIndex = downloadResult.getValue().getContent().toObject(OciIndex.class);
            System.out.println("Got OCI index");
        } else {
            throw new IllegalArgumentException("Got unexpected manifest type: " + response.getValue().getMediaType());
        }
        // END: com.azure.containers.containerregistry.downloadCustomManifest
    }
}
