// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class SampleModelConfiguration {
    private static final String DEFAULT_COMPLETION_MODEL = "gpt-5.2";
    private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-large";

    private SampleModelConfiguration() {
    }

    static String getCompletionModel() {
        return getCompletionModel(System::getenv);
    }

    static String getCompletionModel(Function<String, String> configuration) {
        return getOptionalValue(configuration, "CU_COMPLETION_MODEL", "CONTENTUNDERSTANDING_COMPLETION_MODEL",
            DEFAULT_COMPLETION_MODEL);
    }

    static String getEmbeddingModel() {
        return getEmbeddingModel(System::getenv);
    }

    static String getEmbeddingModel(Function<String, String> configuration) {
        return getOptionalValue(configuration, "CU_EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL);
    }

    static Map<String, String> getDefaultModelDeployments() {
        return getDefaultModelDeployments(System::getenv);
    }

    static Map<String, String> getDefaultModelDeployments(Function<String, String> configuration) {
        List<String> missingVariables = new ArrayList<>();
        String completionDeployment
            = getRequiredValue(configuration, "CU_COMPLETION_MODEL_DEPLOYMENT", missingVariables);
        String embeddingDeployment = getRequiredValue(configuration, "CU_EMBEDDING_DEPLOYMENT",
            "TEXT_EMBEDDING_3_LARGE_DEPLOYMENT", missingVariables);
        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException("Sample00_UpdateDefaults requires environment variables: "
                + String.join(", ", missingVariables));
        }

        String completionModel = getCompletionModel(configuration);
        String miniCompletionModel
            = getOptionalValue(configuration, "CU_COMPLETION_MODEL_MINI", completionModel);
        String embeddingModel = getEmbeddingModel(configuration);
        String miniCompletionDeployment
            = getOptionalValue(configuration, "CU_COMPLETION_MINI_DEPLOYMENT", completionDeployment);

        Map<String, String> deployments = new LinkedHashMap<>();
        putDeployment(deployments, completionModel, completionDeployment);
        putDeployment(deployments, miniCompletionModel, miniCompletionDeployment);
        putDeployment(deployments, embeddingModel, embeddingDeployment);
        putDeployment(deployments, "prebuilt-analyzer-completion", completionDeployment);
        putDeployment(deployments, "prebuilt-analyzer-completion-mini", miniCompletionDeployment);
        putDeployment(deployments, "prebuilt-analyzer-embedding", embeddingDeployment);
        return deployments;
    }

    private static String getRequiredValue(Function<String, String> configuration, String name,
        List<String> missingVariables) {
        return getRequiredValue(configuration, name, null, missingVariables);
    }

    private static String getRequiredValue(Function<String, String> configuration, String name, String legacyName,
        List<String> missingVariables) {
        String value = getOptionalValue(configuration, name, legacyName, null);
        if (value == null) {
            missingVariables.add(name);
        }
        return value;
    }

    private static String getOptionalValue(Function<String, String> configuration, String name, String defaultValue) {
        return getOptionalValue(configuration, name, null, defaultValue);
    }

    private static String getOptionalValue(Function<String, String> configuration, String name, String legacyName,
        String defaultValue) {
        String value = getValue(configuration, name);
        if (value == null && legacyName != null) {
            value = getValue(configuration, legacyName);
        }
        return value == null ? defaultValue : value;
    }

    private static String getValue(Function<String, String> configuration, String name) {
        String value = configuration.apply(name);
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private static void putDeployment(Map<String, String> deployments, String model, String deployment) {
        String existingDeployment = deployments.get(model);
        if (existingDeployment != null && !existingDeployment.equals(deployment)) {
            throw new IllegalStateException("Model '" + model + "' maps to multiple deployments ('"
                + existingDeployment + "' and '" + deployment + "'). Use distinct model names or the same deployment.");
        }
        deployments.put(model, deployment);
    }
}
