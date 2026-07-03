// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.ModelVersion;
import com.azure.ai.projects.models.UpdateModelVersionInput;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample demonstrating model version operations using the asynchronous BetaModelsAsyncClient.
 *
 * <p>Before running, set {@code FOUNDRY_PROJECT_ENDPOINT} to your Azure AI Foundry project endpoint.</p>
 */
public class ModelsAsyncSample {
    private static final BetaModelsAsyncClient MODELS_ASYNC_CLIENT = new AIProjectClientBuilder()
        .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .beta().buildBetaModelsAsyncClient();

    public static void main(String[] args) {
        listLatestModelVersions()
            .timeout(Duration.ofMinutes(2))
            .block();

        // Uncomment these samples when you have a model asset name/version/blob URI to work with.
//        listModelVersions().block();
//        getModelVersion().block();
//        createModelVersionAsync().block();
//        updateModelVersion().block();
//        deleteModelVersion().block();
    }

    public static Mono<Void> listLatestModelVersions() {
        // BEGIN:com.azure.ai.projects.ModelsAsyncSample.listLatestModelVersions

        AtomicBoolean found = new AtomicBoolean(false);
        return MODELS_ASYNC_CLIENT.listLatestModelVersions()
            .doOnNext(modelVersion -> {
                found.set(true);
                System.out.printf("Model name: %s%n", modelVersion.getName());
                System.out.printf("Model version: %s%n", modelVersion.getVersion());
                System.out.printf("Blob URI: %s%n", modelVersion.getBlobUrl());
                System.out.println("-------------------------------------------------");
            })
            .then(Mono.fromRunnable(() -> {
                if (!found.get()) {
                    System.out.println("No model versions found.");
                }
            }));

        // END:com.azure.ai.projects.ModelsAsyncSample.listLatestModelVersions
    }

    public static Mono<Void> listModelVersions() {
        // BEGIN:com.azure.ai.projects.ModelsAsyncSample.listModelVersions

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        return MODELS_ASYNC_CLIENT.listModelVersions(modelName)
            .doOnNext(modelVersion -> {
                System.out.printf("Model name: %s%n", modelVersion.getName());
                System.out.printf("Model version: %s%n", modelVersion.getVersion());
                System.out.println("-------------------------------------------------");
            })
            .then();

        // END:com.azure.ai.projects.ModelsAsyncSample.listModelVersions
    }

    public static Mono<ModelVersion> getModelVersion() {
        // BEGIN:com.azure.ai.projects.ModelsAsyncSample.getModelVersion

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");
        return MODELS_ASYNC_CLIENT.getModelVersion(modelName, modelVersion)
            .doOnNext(version -> {
                System.out.printf("Model name: %s%n", version.getName());
                System.out.printf("Model version: %s%n", version.getVersion());
                System.out.printf("Description: %s%n", version.getDescription());
            });

        // END:com.azure.ai.projects.ModelsAsyncSample.getModelVersion
    }

    public static Mono<Void> createModelVersionAsync() {
        // BEGIN:com.azure.ai.projects.ModelsAsyncSample.createModelVersionAsync

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");
        String blobUri = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_BLOB_URI");

        ModelVersion modelVersionDefinition = new ModelVersion(blobUri)
            .setDescription("Model version created by the Azure AI Projects Java SDK sample.");

        return MODELS_ASYNC_CLIENT.createModelVersionAsync(modelName, modelVersion, modelVersionDefinition)
            .doOnSuccess(response -> System.out.printf("Started model version creation: %s/%s%n", modelName,
                modelVersion))
            .then();

        // END:com.azure.ai.projects.ModelsAsyncSample.createModelVersionAsync
    }

    public static Mono<ModelVersion> updateModelVersion() {
        // BEGIN:com.azure.ai.projects.ModelsAsyncSample.updateModelVersion

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");

        Map<String, String> tags = new HashMap<>();
        tags.put("sample", "true");

        return MODELS_ASYNC_CLIENT.updateModelVersion(modelName, modelVersion,
            new UpdateModelVersionInput()
                .setDescription("Updated by the Azure AI Projects Java SDK sample.")
                .setTags(tags))
            .doOnNext(updated -> System.out.printf("Updated model version: %s/%s%n", updated.getName(),
                updated.getVersion()));

        // END:com.azure.ai.projects.ModelsAsyncSample.updateModelVersion
    }

    public static Mono<Void> deleteModelVersion() {
        // BEGIN:com.azure.ai.projects.ModelsAsyncSample.deleteModelVersion

        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_NAME");
        String modelVersion = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_ASSET_VERSION");

        return MODELS_ASYNC_CLIENT.deleteModelVersion(modelName, modelVersion)
            .doOnSuccess(unused -> System.out.printf("Deleted model version: %s/%s%n", modelName, modelVersion));

        // END:com.azure.ai.projects.ModelsAsyncSample.deleteModelVersion
    }
}
