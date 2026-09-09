// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentUnderstandingModelProfileTest {
    @Test
    public void gaProfileUsesConcreteModelNamesWithoutAliases() {
        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2025_11_01, Collections.emptyMap());

        Map<String, String> deployments = profile.getDefaultModelDeployments();

        assertTrue(deployments.containsKey("gpt-4.1"));
        assertTrue(deployments.containsKey("gpt-4.1-mini"));
        assertTrue(deployments.containsKey("text-embedding-3-large"));
        assertFalse(deployments.containsKey("prebuilt-analyzer-completion"));
    }

    @Test
    public void previewProfileIncludesPrebuiltAliases() {
        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, Collections.emptyMap());

        Map<String, String> deployments = profile.getDefaultModelDeployments();

        assertTrue(deployments.containsKey(profile.getCompletionModel()));
        assertTrue(deployments.containsKey("text-embedding-3-large"));
        assertEquals(deployments.get(profile.getCompletionModel()), deployments.get("prebuilt-analyzer-completion"));
        assertTrue(deployments.containsKey("prebuilt-analyzer-completion-mini"));
        assertEquals(deployments.get("text-embedding-3-large"), deployments.get("prebuilt-analyzer-embedding"));
    }

    @Test
    public void gaProfileUsesSharedPreviewDeploymentWhenConfigured() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "gpt-5.2");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "gpt-5.2-deployment");
        configuration.put("CU_COMPLETION_MINI_DEPLOYMENT", "gpt-5.2-deployment");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "embedding-deployment");

        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2025_11_01, configuration);
        Map<String, String> deployments = profile.getDefaultModelDeployments();

        assertEquals("gpt-5.2", profile.getCompletionModel());
        assertEquals("gpt-5.2-deployment", deployments.get("gpt-5.2"));
        assertEquals("embedding-deployment", deployments.get("text-embedding-3-large"));
        assertFalse(deployments.containsKey("gpt-4.1"));
        assertFalse(deployments.containsKey("prebuilt-analyzer-completion"));
    }

    @Test
    public void profileRejectsDifferentDeploymentsForSameCompletionModel() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "gpt-5.2");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("CU_COMPLETION_MINI_DEPLOYMENT", "mini-deployment");

        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, configuration);

        IllegalStateException exception
            = assertThrows(IllegalStateException.class, profile::getDefaultModelDeployments);
        assertTrue(exception.getMessage().contains("share model name 'gpt-5.2'"));
    }

    @Test
    public void profileRejectsDifferentDeploymentsForSameCompletionAndEmbeddingModel() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "shared-model");
        configuration.put("CU_EMBEDDING_MODEL", "shared-model");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "embedding-deployment");

        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, configuration);

        IllegalStateException exception
            = assertThrows(IllegalStateException.class, profile::getDefaultModelDeployments);
        assertTrue(exception.getMessage().contains("maps to multiple deployments"));
    }

    @Test
    public void recordedProfileReplaysResolvedValuesWithoutConfiguration() {
        Map<String, String> recordingConfiguration = new HashMap<>();
        recordingConfiguration.put("CU_COMPLETION_MODEL", "custom-completion-model");
        recordingConfiguration.put("CU_COMPLETION_MODEL_MINI", "custom-mini-model");
        recordingConfiguration.put("CU_EMBEDDING_MODEL", "custom-embedding-model");
        recordingConfiguration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "custom-completion-deployment");
        recordingConfiguration.put("CU_COMPLETION_MINI_DEPLOYMENT", "custom-mini-deployment");
        recordingConfiguration.put("CU_EMBEDDING_DEPLOYMENT", "custom-embedding-deployment");

        ContentUnderstandingModelProfile recordingProfile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, recordingConfiguration);
        Queue<String> recordedVariables = new LinkedList<>();
        recordingProfile.recordVariables(recordedVariables::add);

        ContentUnderstandingModelProfile playbackProfile
            = ContentUnderstandingModelProfile.fromRecordedVariables(recordedVariables::remove);

        assertEquals(recordingProfile.getCompletionModel(), playbackProfile.getCompletionModel());
        assertEquals(recordingProfile.getDefaultModelDeployments(), playbackProfile.getDefaultModelDeployments());
        assertTrue(recordedVariables.isEmpty());
    }

    @Test
    public void gaRecordedProfileReplaysSharedDeploymentValuesWithoutAliases() {
        Map<String, String> recordingConfiguration = new HashMap<>();
        recordingConfiguration.put("CU_COMPLETION_MODEL", "custom-ga-model");
        recordingConfiguration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "custom-ga-deployment");
        recordingConfiguration.put("CU_COMPLETION_MINI_DEPLOYMENT", "custom-ga-deployment");
        recordingConfiguration.put("CU_EMBEDDING_DEPLOYMENT", "custom-embedding-deployment");

        ContentUnderstandingModelProfile recordingProfile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2025_11_01, recordingConfiguration);
        Queue<String> recordedVariables = new LinkedList<>();
        recordingProfile.recordVariables(recordedVariables::add);

        ContentUnderstandingModelProfile playbackProfile
            = ContentUnderstandingModelProfile.fromRecordedVariables(recordedVariables::remove);

        assertEquals(recordingProfile.getCompletionModel(), playbackProfile.getCompletionModel());
        assertEquals(recordingProfile.getDefaultModelDeployments(), playbackProfile.getDefaultModelDeployments());
        assertFalse(playbackProfile.getDefaultModelDeployments().containsKey("prebuilt-analyzer-completion"));
        assertTrue(recordedVariables.isEmpty());
    }

    @Test
    public void previewProfileUsesConfiguredLogicalModelNames() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "gpt-5.3");
        configuration.put("CU_COMPLETION_MODEL_MINI", "gpt-5.3-mini");
        configuration.put("CU_EMBEDDING_MODEL", "text-embedding-4");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("CU_COMPLETION_MINI_DEPLOYMENT", "mini-deployment");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "embedding-deployment");

        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, configuration);
        Map<String, String> deployments = profile.getDefaultModelDeployments();

        assertEquals("gpt-5.3", profile.getCompletionModel());
        assertEquals("text-embedding-4", profile.getEmbeddingModel());
        assertEquals("completion-deployment", deployments.get("gpt-5.3"));
        assertEquals("mini-deployment", deployments.get("gpt-5.3-mini"));
        assertEquals("embedding-deployment", deployments.get("text-embedding-4"));
    }

    @Test
    public void profileAcceptsRemainingLegacyPreviewVariablesDuringMigration() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CONTENTUNDERSTANDING_COMPLETION_MODEL", "legacy-model");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("TEXT_EMBEDDING_3_LARGE_DEPLOYMENT", "legacy-embedding");

        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, configuration);
        Map<String, String> deployments = profile.getDefaultModelDeployments();

        assertEquals("legacy-model", profile.getCompletionModel());
        assertEquals("completion-deployment", deployments.get("legacy-model"));
        assertEquals("legacy-embedding", deployments.get("text-embedding-3-large"));
    }

    @Test
    public void missingRecordedProfileReturnsNullForLegacyRecordings() {
        Queue<String> recordedVariables = new LinkedList<>();

        assertNull(ContentUnderstandingModelProfile.fromRecordedVariables(recordedVariables::remove));
    }

    @Test
    public void missingProxyVariableQueueReturnsNullForLegacyRecordings() {
        assertNull(ContentUnderstandingModelProfile.fromRecordedVariables(() -> {
            throw new RuntimeException("'proxyVariableQueue' cannot be null or empty.");
        }));
    }

    @Test
    public void invalidRecordedProfileMarkerIsRejected() {
        Queue<String> recordedVariables = new LinkedList<>();
        recordedVariables.add("not-a-model-profile");

        assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingModelProfile.fromRecordedVariables(recordedVariables::remove));
    }

    @Test
    public void truncatedRecordedProfileReportsMissingValue() {
        Queue<String> recordedVariables = new LinkedList<>();
        recordedVariables.add("content-understanding-model-profile-v1");
        recordedVariables.add("completion-model");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingModelProfile.fromRecordedVariables(recordedVariables::remove));

        assertTrue(exception.getMessage().contains("Recorded model profile value is missing"));
    }

    @Test
    public void blankRecordedProfileValueIsRejected() {
        LinkedList<String> recordedVariables = recordedProfileVariables();
        recordedVariables.removeLast();
        recordedVariables.add(" ");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingModelProfile.fromRecordedVariables(recordedVariables::remove));

        assertTrue(exception.getMessage().contains("includesPrebuiltAliases"));
    }

    @Test
    public void invalidRecordedAliasesFlagIsRejected() {
        LinkedList<String> recordedVariables = recordedProfileVariables();
        recordedVariables.removeLast();
        recordedVariables.add("not-a-boolean");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingModelProfile.fromRecordedVariables(recordedVariables::remove));

        assertTrue(exception.getMessage().contains("Invalid recorded includesPrebuiltAliases"));
    }

    @Test
    public void unexpectedRecordedVariableFailurePropagates() {
        RuntimeException expected = new RuntimeException("variable service failed");

        RuntimeException actual
            = assertThrows(RuntimeException.class, () -> ContentUnderstandingModelProfile.fromRecordedVariables(() -> {
                throw expected;
            }));

        assertSame(expected, actual);
    }

    @Test
    public void deploymentMapsAreIndependent() {
        ContentUnderstandingModelProfile profile = ContentUnderstandingModelProfile
            .forServiceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, Collections.emptyMap());

        Map<String, String> first = profile.getDefaultModelDeployments();
        first.put("caller-added", "deployment");
        Map<String, String> second = profile.getDefaultModelDeployments();

        assertFalse(second.containsKey("caller-added"));
    }

    private static LinkedList<String> recordedProfileVariables() {
        LinkedList<String> variables = new LinkedList<>();
        variables.add("content-understanding-model-profile-v1");
        variables.add("completion-model");
        variables.add("completion-deployment");
        variables.add("mini-model");
        variables.add("mini-deployment");
        variables.add("embedding-model");
        variables.add("embedding-deployment");
        variables.add("true");
        return variables;
    }
}
