// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.ContentUnderstandingDefaults;
import com.azure.core.exception.HttpResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

final class ContentUnderstandingLiveTestSetup {
    private static final String COMPLETION_MODEL = "CU_COMPLETION_MODEL";
    private static final String MINI_MODEL = "CU_COMPLETION_MODEL_MINI";
    private static final String EMBEDDING_MODEL = "CU_EMBEDDING_MODEL";
    private static final String COMPLETION_DEPLOYMENT = "CU_COMPLETION_MODEL_DEPLOYMENT";
    private static final String MINI_DEPLOYMENT = "CU_COMPLETION_MINI_DEPLOYMENT";
    private static final String EMBEDDING_DEPLOYMENT = "CU_EMBEDDING_DEPLOYMENT";

    private boolean configured;

    synchronized void ensureConfigured(Function<String, String> configuration,
        Supplier<ContentUnderstandingDefaults> getDefaults,
        Function<Map<String, String>, ContentUnderstandingDefaults> updateDefaults) {
        if (configured) {
            return;
        }

        Map<String, String> requiredDeployments = getRequiredDeployments(configuration);
        Map<String, String> mergedDeployments = new LinkedHashMap<>();
        try {
            ContentUnderstandingDefaults currentDefaults = getDefaults.get();
            if (currentDefaults != null && currentDefaults.getModelDeployments() != null) {
                mergedDeployments.putAll(currentDefaults.getModelDeployments());
            }
        } catch (HttpResponseException exception) {
            if (!isDefaultsNotSet(exception)) {
                throw exception;
            }
        }

        boolean needsUpdate = false;
        for (Map.Entry<String, String> deployment : requiredDeployments.entrySet()) {
            if (!Objects.equals(mergedDeployments.get(deployment.getKey()), deployment.getValue())) {
                mergedDeployments.put(deployment.getKey(), deployment.getValue());
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            updateDefaults.apply(mergedDeployments);
        }
        configured = true;
    }

    static Map<String, String> getRequiredDeployments(Function<String, String> configuration) {
        List<String> missingVariables = new ArrayList<>();
        String completionDeployment = getRequiredValue(configuration, COMPLETION_DEPLOYMENT, missingVariables);
        String embeddingDeployment = getRequiredValue(configuration, EMBEDDING_DEPLOYMENT,
            "TEXT_EMBEDDING_3_LARGE_DEPLOYMENT", missingVariables);
        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException("Content Understanding LIVE test setup requires environment variables: "
                + String.join(", ", missingVariables));
        }

        String completionModel
            = getOptionalValue(configuration, COMPLETION_MODEL, "CONTENTUNDERSTANDING_COMPLETION_MODEL", "gpt-5.2");
        String miniModel = getOptionalValue(configuration, MINI_MODEL, completionModel);
        String embeddingModel = getOptionalValue(configuration, EMBEDDING_MODEL, "text-embedding-3-large");
        String miniDeployment = getOptionalValue(configuration, MINI_DEPLOYMENT, completionDeployment);

        Map<String, String> deployments = new LinkedHashMap<>();
        putDeployment(deployments, completionModel, completionDeployment);
        putDeployment(deployments, miniModel, miniDeployment);
        putDeployment(deployments, embeddingModel, embeddingDeployment);
        putDeployment(deployments, "prebuilt-analyzer-completion", completionDeployment);
        putDeployment(deployments, "prebuilt-analyzer-completion-mini", miniDeployment);
        putDeployment(deployments, "prebuilt-analyzer-embedding", embeddingDeployment);
        return Collections.unmodifiableMap(deployments);
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
            throw new IllegalStateException("Model '" + model + "' maps to multiple deployments ('" + existingDeployment
                + "' and '" + deployment + "'). Use distinct model names or the same deployment.");
        }
        deployments.put(model, deployment);
    }

    private static boolean isDefaultsNotSet(HttpResponseException exception) {
        return exception.getResponse() != null
            && exception.getResponse().getStatusCode() == 400
            && exception.getMessage() != null
            && exception.getMessage().contains("DefaultsNotSet");
    }
}
