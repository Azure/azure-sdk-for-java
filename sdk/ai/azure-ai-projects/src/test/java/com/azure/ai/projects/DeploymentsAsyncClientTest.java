// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Deployment;
import com.azure.ai.projects.models.DeploymentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class DeploymentsAsyncClientTest extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDeployments(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DeploymentsAsyncClient deploymentsAsyncClient = getDeploymentsAsyncClient(httpClient, serviceVersion);

        // Verify that listing deployments returns results
        PagedFlux<Deployment> deploymentsFlux = deploymentsAsyncClient.listDeployments();
        Assertions.assertNotNull(deploymentsFlux);

        // Collect all deployments and verify
        List<Deployment> deployments = new ArrayList<>();
        deploymentsFlux.collectList().block(Duration.ofSeconds(30));

        System.out.println("Deployment list retrieved successfully"
            + (deployments.size() > 0 ? " with " + deployments.size() + " deployments" : " (empty list)"));

        // Verify the first deployment if available
        StepVerifier.create(deploymentsFlux.take(1))
            .assertNext(deployment -> assertValidDeployment(deployment, null, null))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDeploymentsWithFilters(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DeploymentsAsyncClient deploymentsAsyncClient = getDeploymentsAsyncClient(httpClient, serviceVersion);

        // Test listing deployments with model publisher filter
        String testPublisher = "openai";
        PagedFlux<Deployment> publisherFilteredDeployments
            = deploymentsAsyncClient.listDeployments(testPublisher, null, null);
        Assertions.assertNotNull(publisherFilteredDeployments);

        // Verify filtered deployments
        StepVerifier.create(publisherFilteredDeployments.take(10)).thenConsumeWhile(deployment -> {
            assertValidDeployment(deployment, null, null);
            System.out.println("Retrieved publisher-filtered deployment: " + deployment.getName());
            return true;
        }).verifyComplete();

        // Test listing deployments with model name filter
        String testModelName = "gpt-4o-mini";
        PagedFlux<Deployment> modelNameFilteredDeployments
            = deploymentsAsyncClient.listDeployments(null, testModelName, null);
        Assertions.assertNotNull(modelNameFilteredDeployments);

        // Verify filtered deployments
        StepVerifier.create(modelNameFilteredDeployments.take(10)).thenConsumeWhile(deployment -> {
            assertValidDeployment(deployment, null, null);
            System.out.println("Retrieved model-name-filtered deployment: " + deployment.getName());
            return true;
        }).verifyComplete();

        // Test listing deployments with deployment type filter
        PagedFlux<Deployment> typeFilteredDeployments
            = deploymentsAsyncClient.listDeployments(null, null, DeploymentType.MODEL_DEPLOYMENT);
        Assertions.assertNotNull(typeFilteredDeployments);

        // Verify filtered deployments
        StepVerifier.create(typeFilteredDeployments.take(10)).thenConsumeWhile(deployment -> {
            assertValidDeployment(deployment, null, DeploymentType.MODEL_DEPLOYMENT);
            System.out.println("Retrieved type-filtered deployment: " + deployment.getName());
            return true;
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDeployment(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DeploymentsAsyncClient deploymentsAsyncClient = getDeploymentsAsyncClient(httpClient, serviceVersion);

        String deploymentName = Configuration.getGlobalConfiguration().get("TEST_DEPLOYMENT_NAME", "gpt-4o-mini");

        StepVerifier.create(deploymentsAsyncClient.getDeployment(deploymentName)).assertNext(deployment -> {
            assertValidDeployment(deployment, deploymentName, null);
            System.out.println("Deployment retrieved successfully: " + deployment.getName());
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDeploymentAndVerifyType(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DeploymentsAsyncClient deploymentsAsyncClient = getDeploymentsAsyncClient(httpClient, serviceVersion);

        String deploymentName = Configuration.getGlobalConfiguration().get("TEST_DEPLOYMENT_NAME", "gpt-4o-mini");

        StepVerifier.create(deploymentsAsyncClient.getDeployment(deploymentName)).assertNext(deployment -> {
            assertValidDeployment(deployment, deploymentName, DeploymentType.MODEL_DEPLOYMENT);
            System.out.println("Deployment type successfully verified for: " + deployment.getName());
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDeploymentNotFound(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DeploymentsAsyncClient deploymentsAsyncClient = getDeploymentsAsyncClient(httpClient, serviceVersion);

        String nonExistentDeploymentName = "non-existent-deployment-name";

        StepVerifier.create(deploymentsAsyncClient.getDeployment(nonExistentDeploymentName))
            .expectErrorMatches(error -> {
                System.out.println("Expected error received: " + error.getMessage());
                return error.getMessage().contains("404") || error.getMessage().contains("Not Found");
            })
            .verify();
    }
}
