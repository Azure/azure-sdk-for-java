// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
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

public class DownloadImage {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "samples/nginx";
    private static final String OUT_DIRECTORY = getTempDirectory();
    private static final ObjectMapper PRETTY_PRINT = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final DefaultAzureCredential CREDENTIAL = new DefaultAzureCredentialBuilder().build();

    public static void main(String[] args) throws IOException {
        ContainerRepository repository = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

        RegistryArtifact artifact = repository.getArtifact("latest");
        // BEGIN: readme-sample-downloadImage
        DownloadManifestResult manifestResult = artifact.getManifest();

        OciImageManifest manifest = manifestResult.asOciImageManifest();
        System.out.printf("Got manifest:\n%s\n", PRETTY_PRINT.writeValueAsString(manifest));

        String configFileName = manifest.getConfig().getDigest() + ".json";
        repository.downloadStream(manifest.getConfig().getDigest(), createFileChannel(configFileName));
        System.out.printf("Got config: %s\n", configFileName);

        for (OciDescriptor layer : manifest.getLayers()) {
            repository.downloadStream(layer.getDigest(), createFileChannel(layer.getDigest()));
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
        ContainerRepository repository = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

        String digest = "sha256:6581596932dc735fd0df8cc240e6c28845a66829126da5ce25b983cf244e2311";

        // BEGIN: com.azure.containers.containerregistry.downloadStream
        Path file = Files.createTempFile(digest, ".tmp");
        SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        repository.downloadStream(digest, channel);
        // END: com.azure.containers.containerregistry.downloadStream
    }

    private void downloadManifest() {
        ContainerRepository repository = new ContainerRegistryClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .buildClient()
            .getRepository(REPOSITORY);

        // BEGIN: com.azure.containers.containerregistry.downloadManifestTag
        RegistryArtifact artifact = repository.getArtifact("latest");
        DownloadManifestResult latestResult = artifact.getManifest();
        if (ManifestMediaType.DOCKER_MANIFEST.equals(latestResult.getManifestMediaType())
            || ManifestMediaType.OCI_MANIFEST.equals(latestResult.getManifestMediaType())) {
            OciImageManifest manifest = latestResult.asOciImageManifest();
        } else {
            throw new IllegalArgumentException("Unexpected manifest type: " + latestResult.getManifestMediaType());
        }
        // END: com.azure.containers.containerregistry.downloadManifestTag

        // BEGIN: com.azure.containers.containerregistry.downloadManifestDigest
        DownloadManifestResult downloadManifestResult = repository.getArtifact("sha256:6581596932dc735fd0df8cc240e6c28845a66829126da5ce25b983cf244e2311").getManifest();
        // END: com.azure.containers.containerregistry.downloadManifestDigest

        // BEGIN: com.azure.containers.containerregistry.downloadManifestWithResponse
        Response<DownloadManifestResult> downloadResponse = artifact.getManifestWithResponse(Context.NONE);
        System.out.printf("Received manifest: digest - %s, response code: %s\n", downloadResponse.getValue().getDigest(),
            downloadResponse.getStatusCode());
        // END: com.azure.containers.containerregistry.downloadManifestWithResponse
    }
}
