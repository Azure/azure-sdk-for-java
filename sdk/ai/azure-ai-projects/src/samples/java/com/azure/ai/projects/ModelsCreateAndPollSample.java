// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.FoundryModelWeightType;
import com.azure.ai.projects.models.ModelPendingUploadInput;
import com.azure.ai.projects.models.ModelVersion;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Sample demonstrating model registration using the explicit pending-upload flow.
 *
 * <p>Before running, set {@code FOUNDRY_PROJECT_ENDPOINT}. You may optionally set {@code FOUNDRY_MODEL_ASSET_NAME},
 * {@code FOUNDRY_MODEL_ASSET_VERSION}, and {@code FOUNDRY_MODEL_ASSET_PATH}. If {@code FOUNDRY_MODEL_ASSET_PATH} is
 * not set, the sample creates a small temporary folder with dummy model files.</p>
 */
public class ModelsCreateAndPollSample {
    public static void main(String[] args) throws IOException, InterruptedException {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME",
            "sample-model-pending-upload-java");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION", "1");
        Path sourceDirectory = getSourceDirectory();

        BetaModelsClient modelsClient = new AIProjectClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .beta().buildBetaModelsClient();

        System.out.printf("Step 1/3: start pending upload for %s/%s%n", modelName, modelVersion);
        BlobUploadLocation uploadLocation = startPendingUpload(modelsClient, modelName, modelVersion);

        System.out.printf("Step 2/3: upload files from %s%n", sourceDirectory);
        uploadDirectory(sourceDirectory, uploadLocation.getSasUrl());

        System.out.printf("Step 3/3: create model version %s/%s%n", modelName, modelVersion);
        modelsClient.createModelVersionAsync(modelName, modelVersion,
            new ModelVersion(uploadLocation.getBlobUrl())
                .setWeightType(FoundryModelWeightType.FULL_WEIGHT)
                .setDescription("Sample model registered from ModelsCreateAndPollSample.java")
                .setTags(Collections.singletonMap("source", "ModelsCreateAndPollSample.java")));

        try {
            Map<?, ?> committed = pollUntilModelVersionExists(modelsClient, modelName, modelVersion);
            System.out.printf("Model version is available: %s/%s%n", committed.get("name"), committed.get("version"));
        } finally {
            modelsClient.deleteModelVersion(modelName, modelVersion);
            System.out.printf("Deleted model version: %s/%s%n", modelName, modelVersion);
        }
    }

    private static Path getSourceDirectory() throws IOException {
        String configuredPath = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_PATH");
        if (configuredPath != null && !configuredPath.isEmpty()) {
            return Paths.get(configuredPath);
        }

        Path sourceDirectory = Files.createTempDirectory("sample-model-");
        Files.write(sourceDirectory.resolve("weights.bin"), "hello-foundry-model".getBytes(StandardCharsets.UTF_8));
        Files.write(sourceDirectory.resolve("config.json"), "{\"sample\":true}".getBytes(StandardCharsets.UTF_8));
        return sourceDirectory;
    }

    private static BlobUploadLocation startPendingUpload(BetaModelsClient modelsClient, String modelName,
        String modelVersion) {
        Response<BinaryData> pendingUploadResponse = modelsClient.startModelPendingUploadWithResponse(modelName,
            modelVersion, BinaryData.fromObject(new ModelPendingUploadInput()), new RequestOptions());

        Map<?, ?> payload = pendingUploadResponse.getValue().toObject(Map.class);
        Object blobReference = payload.get("blobReferenceForConsumption");
        if (blobReference == null) {
            blobReference = payload.get("blobReference");
        }
        if (!(blobReference instanceof Map)) {
            throw new IllegalStateException("Pending upload response did not include a blob reference: " + payload);
        }

        Map<?, ?> blobReferenceMap = (Map<?, ?>) blobReference;
        String blobUrl = stringValue(blobReferenceMap.get("blobUri"));
        if (blobUrl == null) {
            blobUrl = stringValue(blobReferenceMap.get("blobUrl"));
        }

        Object credential = blobReferenceMap.get("credential");
        if (!(credential instanceof Map)) {
            throw new IllegalStateException("Pending upload response did not include a SAS credential: " + payload);
        }
        Map<?, ?> credentialMap = (Map<?, ?>) credential;
        String sasUrl = stringValue(credentialMap.get("sasUri"));
        if (sasUrl == null) {
            sasUrl = stringValue(credentialMap.get("sasUrl"));
        }

        if (blobUrl == null || sasUrl == null) {
            throw new IllegalStateException("Pending upload response missing SAS URL or blob URL: " + payload);
        }
        return new BlobUploadLocation(blobUrl, sasUrl);
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static void uploadDirectory(Path sourceDirectory, String containerSasUrl) throws IOException {
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .endpoint(containerSasUrl)
            .buildClient();

        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String blobName = sourceDirectory.relativize(path).toString().replace('\\', '/');
                containerClient.getBlobClient(blobName).upload(BinaryData.fromFile(path), true);
                System.out.printf("Uploaded %s%n", blobName);
            });
        }
    }

    private static Map<?, ?> pollUntilModelVersionExists(BetaModelsClient modelsClient, String modelName,
        String modelVersion) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 300_000;
        while (true) {
            try {
                return modelsClient.getModelVersionWithResponse(modelName, modelVersion, new RequestOptions())
                    .getValue()
                    .toObject(Map.class);
            } catch (ResourceNotFoundException ex) {
                if (System.currentTimeMillis() >= deadline) {
                    throw ex;
                }
                Thread.sleep(2_000);
            }
        }
    }

    private static final class BlobUploadLocation {
        private final String blobUrl;
        private final String sasUrl;

        private BlobUploadLocation(String blobUrl, String sasUrl) {
            this.blobUrl = blobUrl;
            this.sasUrl = sasUrl;
        }

        private String getBlobUrl() {
            return blobUrl;
        }

        private String getSasUrl() {
            return sasUrl;
        }
    }
}
