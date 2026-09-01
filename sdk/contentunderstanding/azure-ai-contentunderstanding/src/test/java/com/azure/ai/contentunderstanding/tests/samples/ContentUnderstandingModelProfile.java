// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.core.util.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class ContentUnderstandingModelProfile {
    private static final String RECORDED_PROFILE_MARKER = "content-understanding-model-profile-v1";

    private final String completionModel;
    private final String completionDeployment;
    private final String miniCompletionModel;
    private final String miniCompletionDeployment;
    private final String embeddingModel;
    private final String embeddingDeployment;
    private final boolean includesPrebuiltAliases;

    private ContentUnderstandingModelProfile(String completionModel, String completionDeployment,
        String miniCompletionModel, String miniCompletionDeployment, String embeddingModel, String embeddingDeployment,
        boolean includesPrebuiltAliases) {
        this.completionModel = completionModel;
        this.completionDeployment = completionDeployment;
        this.miniCompletionModel = miniCompletionModel;
        this.miniCompletionDeployment = miniCompletionDeployment;
        this.embeddingModel = embeddingModel;
        this.embeddingDeployment = embeddingDeployment;
        this.includesPrebuiltAliases = includesPrebuiltAliases;
    }

    static ContentUnderstandingModelProfile forServiceVersion(ContentUnderstandingServiceVersion serviceVersion) {
        return forServiceVersion(serviceVersion, name -> Configuration.getGlobalConfiguration().get(name));
    }

    static ContentUnderstandingModelProfile forServiceVersion(ContentUnderstandingServiceVersion serviceVersion,
        Map<String, String> configuration) {
        return forServiceVersion(serviceVersion, configuration::get);
    }

    static ContentUnderstandingModelProfile fromRecordedVariables(Supplier<String> variableSupplier) {
        String marker;
        try {
            marker = variableSupplier.get();
        } catch (NoSuchElementException ignored) {
            return null;
        } catch (RuntimeException exception) {
            if ("'proxyVariableQueue' cannot be null or empty.".equals(exception.getMessage())) {
                return null;
            }
            throw exception;
        }
        if (!RECORDED_PROFILE_MARKER.equals(marker)) {
            throw new IllegalStateException("Unexpected recorded model profile marker: " + marker);
        }

        String completionModel = getRecordedVariable(variableSupplier, "completionModel");
        String completionDeployment = getRecordedVariable(variableSupplier, "completionDeployment");
        String miniCompletionModel = getRecordedVariable(variableSupplier, "miniCompletionModel");
        String miniCompletionDeployment = getRecordedVariable(variableSupplier, "miniCompletionDeployment");
        String embeddingModel = getRecordedVariable(variableSupplier, "embeddingModel");
        String embeddingDeployment = getRecordedVariable(variableSupplier, "embeddingDeployment");
        String includesPrebuiltAliases = getRecordedVariable(variableSupplier, "includesPrebuiltAliases");
        if (!"true".equals(includesPrebuiltAliases) && !"false".equals(includesPrebuiltAliases)) {
            throw new IllegalStateException(
                "Invalid recorded includesPrebuiltAliases value: " + includesPrebuiltAliases);
        }
        return new ContentUnderstandingModelProfile(completionModel, completionDeployment, miniCompletionModel,
            miniCompletionDeployment, embeddingModel, embeddingDeployment,
            Boolean.parseBoolean(includesPrebuiltAliases));
    }

    private static ContentUnderstandingModelProfile forServiceVersion(ContentUnderstandingServiceVersion serviceVersion,
        Function<String, String> configuration) {
        String configuredSharedCompletion = getConfiguredValue(configuration, "CU_COMPLETION_MODEL_DEPLOYMENT", null);
        String configuredSharedMini = getConfiguredValue(configuration, "CU_COMPLETION_MINI_DEPLOYMENT", null);
        String sharedCompletionDeployment = configuredSharedCompletion == null ? "gpt-5.2" : configuredSharedCompletion;
        String sharedMiniDeployment = configuredSharedMini == null ? sharedCompletionDeployment : configuredSharedMini;
        String sharedEmbeddingDeployment = getConfiguredValue(configuration, "CU_EMBEDDING_DEPLOYMENT",
            "TEXT_EMBEDDING_3_LARGE_DEPLOYMENT", "text-embedding-3-large");
        String completionModel = getConfiguredValue(configuration, "CU_COMPLETION_MODEL",
            "CONTENTUNDERSTANDING_COMPLETION_MODEL", "gpt-5.2");
        String miniCompletionModel = getConfiguredValue(configuration, "CU_COMPLETION_MODEL_MINI", completionModel);
        String embeddingModel = getConfiguredValue(configuration, "CU_EMBEDDING_MODEL", "text-embedding-3-large");

        if (serviceVersion == ContentUnderstandingServiceVersion.V2025_11_01) {
            String legacyCompletion = getConfiguredValue(configuration, "GPT_4_1_DEPLOYMENT", null);
            String legacyMini = getConfiguredValue(configuration, "GPT_4_1_MINI_DEPLOYMENT", null);
            if (legacyCompletion != null || legacyMini != null || configuredSharedCompletion == null) {
                return new ContentUnderstandingModelProfile("gpt-4.1",
                    legacyCompletion == null ? "gpt-4.1" : legacyCompletion, "gpt-4.1-mini",
                    legacyMini == null ? "gpt-4.1-mini" : legacyMini, embeddingModel, sharedEmbeddingDeployment, false);
            }
            return new ContentUnderstandingModelProfile(completionModel, sharedCompletionDeployment,
                miniCompletionModel, sharedMiniDeployment, embeddingModel, sharedEmbeddingDeployment, false);
        }
        if (serviceVersion == ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW) {
            return new ContentUnderstandingModelProfile(completionModel, sharedCompletionDeployment,
                miniCompletionModel, sharedMiniDeployment, embeddingModel, sharedEmbeddingDeployment, true);
        }
        throw new IllegalArgumentException("Unsupported service version: " + serviceVersion);
    }

    String getCompletionModel() {
        return completionModel;
    }

    String getEmbeddingModel() {
        return embeddingModel;
    }

    void recordVariables(Consumer<String> variableConsumer) {
        variableConsumer.accept(RECORDED_PROFILE_MARKER);
        variableConsumer.accept(completionModel);
        variableConsumer.accept(completionDeployment);
        variableConsumer.accept(miniCompletionModel);
        variableConsumer.accept(miniCompletionDeployment);
        variableConsumer.accept(embeddingModel);
        variableConsumer.accept(embeddingDeployment);
        variableConsumer.accept(Boolean.toString(includesPrebuiltAliases));
    }

    Map<String, String> getDefaultModelDeployments() {
        Map<String, String> deployments = new LinkedHashMap<>();
        putDeployment(deployments, completionModel, completionDeployment);
        if (!miniCompletionModel.equals(completionModel)) {
            putDeployment(deployments, miniCompletionModel, miniCompletionDeployment);
        } else if (!miniCompletionDeployment.equals(completionDeployment)) {
            throw new IllegalStateException("Completion and mini completion share model name '" + completionModel
                + "' but have different deployments ('" + completionDeployment + "' vs '" + miniCompletionDeployment
                + "'). Use distinct model names or the same deployment.");
        }
        putDeployment(deployments, embeddingModel, embeddingDeployment);
        if (includesPrebuiltAliases) {
            putDeployment(deployments, "prebuilt-analyzer-completion", completionDeployment);
            putDeployment(deployments, "prebuilt-analyzer-completion-mini", miniCompletionDeployment);
            putDeployment(deployments, "prebuilt-analyzer-embedding", embeddingDeployment);
        }
        return deployments;
    }

    private static void putDeployment(Map<String, String> deployments, String model, String deployment) {
        String existingDeployment = deployments.get(model);
        if (existingDeployment != null && !existingDeployment.equals(deployment)) {
            throw new IllegalStateException("Model '" + model + "' maps to multiple deployments ('" + existingDeployment
                + "' and '" + deployment + "'). Use distinct model names or the same deployment.");
        }
        deployments.put(model, deployment);
    }

    private static String getConfiguredValue(Function<String, String> configuration, String name, String defaultValue) {
        String value = configuration.apply(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private static String getConfiguredValue(Function<String, String> configuration, String name, String legacyName,
        String defaultValue) {
        String value = getConfiguredValue(configuration, name, null);
        return value == null ? getConfiguredValue(configuration, legacyName, defaultValue) : value;
    }

    private static String getRecordedVariable(Supplier<String> variableSupplier, String name) {
        String value;
        try {
            value = variableSupplier.get();
        } catch (NoSuchElementException exception) {
            throw new IllegalStateException("Recorded model profile value is missing: " + name, exception);
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Recorded model profile value is missing: " + name);
        }
        return value;
    }
}
