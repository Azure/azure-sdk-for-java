// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.ModelVersion;
import com.azure.ai.projects.models.UpdateModelVersionInput;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating model version operations using the synchronous BetaModelsClient.
 *
 * <p>Before running, set {@code FOUNDRY_PROJECT_ENDPOINT} to your Azure AI Foundry project endpoint.</p>
 */
public class ModelsSample {
    private static final BetaModelsClient MODELS_CLIENT = new AIProjectClientBuilder()
        .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .beta().buildBetaModelsClient();

    public static void main(String[] args) {
        listLatestModelVersions();

        // Uncomment these samples when you have a model asset name/version/blob URI to work with.
//        listModelVersions();
//        getModelVersion();
//        createModelVersionAsync();
//        updateModelVersion();
//        deleteModelVersion();
    }

    public static void listLatestModelVersions() {
        // BEGIN:com.azure.ai.projects.ModelsSample.listLatestModelVersions

        int count = 0;
        for (ModelVersion modelVersion : MODELS_CLIENT.listLatestModelVersions()) {
            count++;
            System.out.printf("Model name: %s%n", modelVersion.getName());
            System.out.printf("Model version: %s%n", modelVersion.getVersion());
            System.out.printf("Blob URI: %s%n", modelVersion.getBlobUrl());
            System.out.println("-------------------------------------------------");
        }
        if (count == 0) {
            System.out.println("No model versions found.");
        }

        // END:com.azure.ai.projects.ModelsSample.listLatestModelVersions
    }

    public static void listModelVersions() {
        // BEGIN:com.azure.ai.projects.ModelsSample.listModelVersions

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        for (ModelVersion modelVersion : MODELS_CLIENT.listModelVersions(modelName)) {
            System.out.printf("Model name: %s%n", modelVersion.getName());
            System.out.printf("Model version: %s%n", modelVersion.getVersion());
            System.out.println("-------------------------------------------------");
        }

        // END:com.azure.ai.projects.ModelsSample.listModelVersions
    }

    public static void getModelVersion() {
        // BEGIN:com.azure.ai.projects.ModelsSample.getModelVersion

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");
        ModelVersion version = MODELS_CLIENT.getModelVersion(modelName, modelVersion);

        System.out.printf("Model name: %s%n", version.getName());
        System.out.printf("Model version: %s%n", version.getVersion());
        System.out.printf("Description: %s%n", version.getDescription());

        // END:com.azure.ai.projects.ModelsSample.getModelVersion
    }

    public static void createModelVersionAsync() {
        // BEGIN:com.azure.ai.projects.ModelsSample.createModelVersionAsync

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");
        String blobUri = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_BLOB_URI");

        ModelVersion modelVersionDefinition = new ModelVersion(blobUri)
            .setDescription("Model version created by the Azure AI Projects Java SDK sample.");

        MODELS_CLIENT.createModelVersionAsync(modelName, modelVersion, modelVersionDefinition);
        System.out.printf("Started model version creation: %s/%s%n", modelName, modelVersion);

        // END:com.azure.ai.projects.ModelsSample.createModelVersionAsync
    }

    public static void updateModelVersion() {
        // BEGIN:com.azure.ai.projects.ModelsSample.updateModelVersion

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");

        Map<String, String> tags = new HashMap<>();
        tags.put("sample", "true");

        ModelVersion updated = MODELS_CLIENT.updateModelVersion(modelName, modelVersion,
            new UpdateModelVersionInput()
                .setDescription("Updated by the Azure AI Projects Java SDK sample.")
                .setTags(tags));

        System.out.printf("Updated model version: %s/%s%n", updated.getName(), updated.getVersion());

        // END:com.azure.ai.projects.ModelsSample.updateModelVersion
    }

    public static void deleteModelVersion() {
        // BEGIN:com.azure.ai.projects.ModelsSample.deleteModelVersion

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");

        MODELS_CLIENT.deleteModelVersion(modelName, modelVersion);
        System.out.printf("Deleted model version: %s/%s%n", modelName, modelVersion);

        // END:com.azure.ai.projects.ModelsSample.deleteModelVersion
    }
}
