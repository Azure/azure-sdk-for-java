// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleModelConfigurationTest {
    @Test
    public void createsMappingsForConfiguredModelNames() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "gpt-5.3");
        configuration.put("CU_COMPLETION_MODEL_MINI", "gpt-5.3-mini");
        configuration.put("CU_EMBEDDING_MODEL", "text-embedding-4");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("CU_COMPLETION_MINI_DEPLOYMENT", "mini-deployment");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "embedding-deployment");

        Map<String, String> deployments = SampleModelConfiguration.getDefaultModelDeployments(configuration::get);

        assertEquals("completion-deployment", deployments.get("gpt-5.3"));
        assertEquals("mini-deployment", deployments.get("gpt-5.3-mini"));
        assertEquals("embedding-deployment", deployments.get("text-embedding-4"));
        assertEquals("completion-deployment", deployments.get("prebuilt-analyzer-completion"));
        assertEquals("mini-deployment", deployments.get("prebuilt-analyzer-completion-mini"));
        assertEquals("embedding-deployment", deployments.get("prebuilt-analyzer-embedding"));
    }

    @Test
    public void defaultsModelNamesAndMiniDeployment() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "embedding-deployment");

        Map<String, String> deployments = SampleModelConfiguration.getDefaultModelDeployments(configuration::get);

        assertEquals("completion-deployment", deployments.get("gpt-5.2"));
        assertEquals("embedding-deployment", deployments.get("text-embedding-3-large"));
        assertEquals("completion-deployment", deployments.get("prebuilt-analyzer-completion-mini"));
    }

    @Test
    public void resolvesConfiguredEmbeddingModel() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_EMBEDDING_MODEL", "text-embedding-4");

        assertEquals("text-embedding-4", SampleModelConfiguration.getEmbeddingModel(configuration::get));
    }

    @Test
    public void defaultsEmbeddingModel() {
        assertEquals("text-embedding-3-large", SampleModelConfiguration.getEmbeddingModel(name -> null));
    }

    @Test
    public void canonicalVariablesTakePrecedenceOverRemainingLegacyVariables() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "canonical-model");
        configuration.put("CONTENTUNDERSTANDING_COMPLETION_MODEL", "legacy-model");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "canonical-completion");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "canonical-embedding");
        configuration.put("TEXT_EMBEDDING_3_LARGE_DEPLOYMENT", "legacy-embedding");

        Map<String, String> deployments = SampleModelConfiguration.getDefaultModelDeployments(configuration::get);

        assertEquals("canonical-completion", deployments.get("canonical-model"));
        assertFalse(deployments.containsKey("legacy-model"));
        assertEquals("canonical-embedding", deployments.get("text-embedding-3-large"));
    }

    @Test
    public void acceptsRemainingLegacyVariablesDuringMigration() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CONTENTUNDERSTANDING_COMPLETION_MODEL", "legacy-model");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("TEXT_EMBEDDING_3_LARGE_DEPLOYMENT", "legacy-embedding");

        Map<String, String> deployments = SampleModelConfiguration.getDefaultModelDeployments(configuration::get);

        assertEquals("completion-deployment", deployments.get("legacy-model"));
        assertEquals("legacy-embedding", deployments.get("text-embedding-3-large"));
    }

    @Test
    public void reportsMissingRequiredDeployments() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> SampleModelConfiguration.getDefaultModelDeployments(name -> null));

        assertTrue(exception.getMessage().contains("CU_COMPLETION_MODEL_DEPLOYMENT"));
        assertTrue(exception.getMessage().contains("CU_EMBEDDING_DEPLOYMENT"));
    }

    @Test
    public void rejectsDifferentDeploymentsForSameModel() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "shared-model");
        configuration.put("CU_COMPLETION_MODEL_MINI", "shared-model");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("CU_COMPLETION_MINI_DEPLOYMENT", "mini-deployment");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "embedding-deployment");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> SampleModelConfiguration.getDefaultModelDeployments(configuration::get));

        assertTrue(exception.getMessage().contains("maps to multiple deployments"));
    }
}
