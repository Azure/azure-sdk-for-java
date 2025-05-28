// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Deployment;
import com.azure.ai.projects.models.DeploymentType;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DeploymentsClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private DeploymentsClient deploymentsClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        deploymentsClient = clientBuilder.buildDeploymentsClient();
    }

    /**
     * Helper method to verify a Deployment has valid properties.
     * @param deployment The deployment to validate
     * @param expectedName The expected name of the deployment, or null if no specific name is expected
     * @param expectedType The expected deployment type, or null if no specific type is expected
     */
    private void assertValidDeployment(Deployment deployment, String expectedName, DeploymentType expectedType) {
        Assertions.assertNotNull(deployment);
        Assertions.assertNotNull(deployment.getName());
        Assertions.assertNotNull(deployment.getType());

        if (expectedName != null) {
            Assertions.assertEquals(expectedName, deployment.getName());
        }

        if (expectedType != null) {
            Assertions.assertEquals(expectedType, deployment.getType());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDeployments(HttpClient httpClient) {
        setup(httpClient);

        // Verify that listing deployments returns results
        Iterable<Deployment> deployments = deploymentsClient.listDeployments();
        Assertions.assertNotNull(deployments);

        // Verify that at least one deployment can be retrieved if available
        boolean hasAtLeastOneDeployment = false;
        for (Deployment deployment : deployments) {
            hasAtLeastOneDeployment = true;
            assertValidDeployment(deployment, null, null);
            break;
        }

        // Note: This test will pass even if there are no deployments,
        // as we're only verifying the API works correctly
        System.out.println("Deployment list retrieved successfully"
            + (hasAtLeastOneDeployment ? " with at least one deployment" : " (empty list)"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDeploymentsWithFilters(HttpClient httpClient) {
        setup(httpClient);

        // Test listing deployments with model publisher filter
        String testPublisher = "openai";
        Iterable<Deployment> filteredDeployments = deploymentsClient.listDeployments(testPublisher, null, null);
        Assertions.assertNotNull(filteredDeployments);

        // Test listing deployments with model name filter
        String testModelName = "gpt-4o-mini";
        Iterable<Deployment> modelNameFilteredDeployments
            = deploymentsClient.listDeployments(null, testModelName, null);
        Assertions.assertNotNull(modelNameFilteredDeployments);

        // Test listing deployments with deployment type filter
        Iterable<Deployment> typeFilteredDeployments
            = deploymentsClient.listDeployments(null, null, DeploymentType.MODEL_DEPLOYMENT);
        Assertions.assertNotNull(typeFilteredDeployments);

        // Verify that all returned deployments have the correct type
        typeFilteredDeployments.forEach(deployment -> {
            assertValidDeployment(deployment, null, DeploymentType.MODEL_DEPLOYMENT);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDeployment(HttpClient httpClient) {
        setup(httpClient);

        String deploymentName = Configuration.getGlobalConfiguration().get("TEST_DEPLOYMENT_NAME", "gpt-4o-mini");

        try {
            Deployment deployment = deploymentsClient.getDeployment(deploymentName);

            // Verify the deployment properties
            assertValidDeployment(deployment, deploymentName, null);

            System.out.println("Deployment retrieved successfully: " + deployment.getName());
            System.out.println("Deployment type: " + deployment.getType().getValue());
        } catch (Exception e) {
            // If the deployment doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message and passing the test
            System.out.println("Deployment not found: " + deploymentName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDeploymentAndVerifyType(HttpClient httpClient) {
        setup(httpClient);

        String deploymentName = Configuration.getGlobalConfiguration().get("TEST_DEPLOYMENT_NAME", "gpt-4o-mini");

        try {
            Deployment deployment = deploymentsClient.getDeployment(deploymentName);

            // Verify the deployment properties
            assertValidDeployment(deployment, deploymentName, DeploymentType.MODEL_DEPLOYMENT);

            System.out.println("Deployment type successfully verified for: " + deployment.getName());
        } catch (Exception e) {
            // If the deployment doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message and passing the test
            System.out.println("Deployment not found for type verification test: " + deploymentName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }
}
