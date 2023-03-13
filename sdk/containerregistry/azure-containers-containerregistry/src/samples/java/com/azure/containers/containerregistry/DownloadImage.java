// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.OciDescriptor;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClient;
import com.azure.containers.containerregistry.specialized.ContainerRegistryBlobClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DownloadImage {
    private static final String ENDPOINT = "https://registryName.azurecr.io";
    private static final String REPOSITORY = "samples/nginx";
    private static final String OUT_DIRECTORY = getTempDirectory();
    private static final ObjectMapper PRETTY_PRINT = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws IOException {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        // BEGIN: readme-sample-downloadImage
        ContainerRegistryBlobClient blobClient = new ContainerRegistryBlobClientBuilder()
            .endpoint(ENDPOINT)
            .repository(REPOSITORY)
            .credential(credential)
            .buildClient();

        DownloadManifestResult manifestResult = blobClient.downloadManifest("latest");

        OciImageManifest manifest = manifestResult.asOciManifest();
        System.out.printf("Got manifest:\n%s\n\n", PRETTY_PRINT.writeValueAsString(manifest));

        String configFileName = manifest.getConfig().getDigest() + ".json";
        blobClient.downloadStream(manifest.getConfig().getDigest(), createWriteChannel(configFileName));
        System.out.printf("Got config: %s\n", configFileName);

        for (OciDescriptor layer : manifest.getLayers()) {
            blobClient.downloadStream(layer.getDigest(), createWriteChannel(layer.getDigest()));
            System.out.printf("Got layer: %s\n", layer.getDigest());
        }
        // END: readme-sample-downloadImage

        System.out.println("Done");
    }

    private static SeekableByteChannel createWriteChannel(String name) throws IOException {
        if (name.startsWith("sha256:")) {
            name = name.substring(7);
        }
        return Files.newByteChannel(Paths.get(OUT_DIRECTORY, name), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
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
