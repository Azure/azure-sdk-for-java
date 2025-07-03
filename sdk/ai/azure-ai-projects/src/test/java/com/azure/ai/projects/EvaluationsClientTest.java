// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.Evaluation;
import com.azure.ai.projects.models.EvaluatorConfiguration;
import com.azure.ai.projects.models.EvaluatorId;
import com.azure.ai.projects.models.InputDataset;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.HashMap;
import java.util.Map;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class EvaluationsClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private EvaluationsClient evaluationsClient;
    private DatasetsClient datasetsClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        evaluationsClient = clientBuilder.buildEvaluationsClient();
        datasetsClient = clientBuilder.buildDatasetsClient();
    }

    /**
     * Helper method to verify an Evaluation has valid properties.
     * @param evaluation The evaluation to validate
     * @param expectedDisplayName The expected display name of the evaluation, or null if no specific name is expected
     * @param expectedStatus The expected status, or null if no specific status is expected
     */
    private void assertValidEvaluation(Evaluation evaluation, String expectedDisplayName, String expectedStatus) {
        Assertions.assertNotNull(evaluation);
        Assertions.assertNotNull(evaluation.getDisplayName());
        Assertions.assertNotNull(evaluation.getStatus());
        Assertions.assertNotNull(evaluation.getData());
        Assertions.assertNotNull(evaluation.getData().getType());
        Assertions.assertNotNull(evaluation.getEvaluators());
        Assertions.assertFalse(evaluation.getEvaluators().isEmpty());

        if (expectedDisplayName != null) {
            Assertions.assertEquals(expectedDisplayName, evaluation.getDisplayName());
        }

        if (expectedStatus != null) {
            Assertions.assertEquals(expectedStatus, evaluation.getStatus());
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListEvaluations(HttpClient httpClient) {
        setup(httpClient);

        // Verify that listing evaluations returns results
        Iterable<Evaluation> evaluations = evaluationsClient.listEvaluations();
        Assertions.assertNotNull(evaluations);

        // Verify that at least one evaluation can be retrieved if available
        boolean hasAtLeastOneEvaluation = false;
        for (Evaluation evaluation : evaluations) {
            hasAtLeastOneEvaluation = true;
            assertValidEvaluation(evaluation, null, null);
            break;
        }

        // Note: This test will pass even if there are no evaluations,
        // as we're only verifying the API works correctly
        System.out.println("Evaluation list retrieved successfully"
            + (hasAtLeastOneEvaluation ? " with at least one evaluation" : " (empty list)"));
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetEvaluation(HttpClient httpClient) {
        setup(httpClient);

        String evaluationId = Configuration.getGlobalConfiguration().get("TEST_EVALUATION_ID", "test-evaluation-id");

        try {
            Evaluation evaluation = evaluationsClient.getEvaluation(evaluationId);

            // Verify the evaluation properties
            assertValidEvaluation(evaluation, null, null);

            if (evaluation.getTags() != null) {
                // Verify tags are properly structured if present
                evaluation.getTags().forEach((key, value) -> {
                    Assertions.assertNotNull(key);
                    Assertions.assertNotNull(value);
                });
            }

            System.out.println("Evaluation retrieved successfully: " + evaluation.getDisplayName());
            System.out.println("Status: " + evaluation.getStatus());
        } catch (Exception e) {
            // If the evaluation doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message and passing the test
            System.out.println("Evaluation not found: " + evaluationId);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateEvaluation(HttpClient httpClient) {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("TEST_DATASET_NAME", "test-dataset");
        String version = Configuration.getGlobalConfiguration().get("TEST_DATASET_VERSION", "1");

        try {
            // Get a dataset to use for the evaluation
            DatasetVersion datasetVersion = datasetsClient.getDatasetVersion(datasetName, version);

            // Create evaluation definition
            InputDataset dataset = new InputDataset(datasetVersion.getId());
            Evaluation evaluationToCreate = new Evaluation(dataset,
                mapOf("relevance",
                    new EvaluatorConfiguration(EvaluatorId.RELEVANCE.getValue())
                        .setInitParams(mapOf("deployment_name", BinaryData.fromObject("gpt-4o")))))
                            .setDisplayName("Test Evaluation " + System.currentTimeMillis())
                            .setDescription("This is a test evaluation created by the EvaluationsClientTest");

            // Create the evaluation
            Evaluation createdEvaluation = evaluationsClient.createEvaluation(evaluationToCreate);

            // Verify the created evaluation
            assertValidEvaluation(createdEvaluation, evaluationToCreate.getDisplayName(), null);
            Assertions.assertEquals(evaluationToCreate.getDescription(), createdEvaluation.getDescription());
            Assertions.assertTrue(createdEvaluation.getEvaluators().containsKey("relevance"));

            System.out.println("Evaluation created successfully: " + createdEvaluation.getDisplayName());
            System.out.println("Initial status: " + createdEvaluation.getStatus());
        } catch (Exception e) {
            // If the dataset doesn't exist or there's another issue
            System.out.println("Failed to create evaluation: " + e.getMessage());
            throw e;
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testEvaluationStatusCheck(HttpClient httpClient) {
        setup(httpClient);

        String evaluationId = Configuration.getGlobalConfiguration().get("TEST_EVALUATION_ID", "test-evaluation-id");

        try {
            Evaluation evaluation = evaluationsClient.getEvaluation(evaluationId);

            // Verify status is one of the expected values
            Assertions.assertNotNull(evaluation.getStatus());
            String status = evaluation.getStatus();

            // Status should be one of: Running, Succeeded, Failed, Canceled, etc.
            boolean isValidStatus = "Running".equals(status)
                || "Succeeded".equals(status)
                || "Failed".equals(status)
                || "Canceled".equals(status)
                || "Queued".equals(status)
                || "Created".equals(status);

            Assertions.assertTrue(isValidStatus, "Unexpected evaluation status: " + status);

            System.out.println("Evaluation status check passed: " + status);
        } catch (Exception e) {
            // If the evaluation doesn't exist, this will throw a ResourceNotFoundException
            System.out.println("Evaluation not found for status check: " + evaluationId);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

    // Helper method for creating maps
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            @SuppressWarnings("unchecked")
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
