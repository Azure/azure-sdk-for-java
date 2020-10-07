// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.ByteStreams;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Azure Resource sample for deploying resources using an ARM template and
 * showing progress.
 */

public final class DeployUsingARMTemplateWithDeploymentOperations {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param defaultPollingInterval polling interval in seconds
     * @return true if sample runs successfully
     */
    public static boolean runSample(final AzureResourceManager azureResourceManager, int defaultPollingInterval) throws InterruptedException {
        final String rgPrefix = Utils.randomResourceName(azureResourceManager, "rgJavaTest", 16);
        final String deploymentPrefix = Utils.randomResourceName(azureResourceManager, "javaTest", 16);
        final String sshKey = getSSHPublicKey();
        final int numDeployments = 3;
        final int pollingInterval = defaultPollingInterval < 0 ? 15 : defaultPollingInterval; // in seconds

        try {
            // Use the Simple VM Template with SSH Key auth from GH quickstarts

            final String templateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vm-sshkey/azuredeploy.json";
            final String templateContentVersion = "1.0.0.0";

            // Template only needs an SSH Key parameter

            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode rootNode = mapper.createObjectNode();
            rootNode.set("adminPublicKey", mapper.createObjectNode().put("value", sshKey));
            rootNode.set("projectName", mapper.createObjectNode().put("value", "fluenttest"));
            rootNode.set("adminUsername", mapper.createObjectNode().put("value", "fluenttesting"));
            final String parameters = rootNode.toString();
            System.out.println("Starting VM deployments...");

            // Store all deployments in a list
            final List<Deployment> deploymentList = new ArrayList<>();
            final CountDownLatch latch = new CountDownLatch(1);

            Flux.range(1, numDeployments)
                    .flatMap(integer -> {
                        try {
                            String params;
                            if (integer == numDeployments) {
                                rootNode.set("adminPublicKey", mapper.createObjectNode().put("value", "bad content"));
                                params = rootNode.toString(); // Invalid parameters as a negative path
                            } else {
                                params = parameters;
                            }
                            return azureResourceManager.deployments()
                                    .define(deploymentPrefix + "-" + integer)
                                    .withNewResourceGroup(rgPrefix + "-" + integer, Region.US_SOUTH_CENTRAL)
                                    .withTemplateLink(templateUri, templateContentVersion)
                                    .withParameters(params)
                                    .withMode(DeploymentMode.COMPLETE)
                                    .beginCreateAsync();
                        } catch (IOException e) {
                            return Flux.error(e);
                        }
                    })
                    .doOnNext(deployment -> {
                        System.out.println("Deployment created: " + deployment.name());
                        deploymentList.add(deployment);
                    })
                    .onErrorResume(e -> Mono.empty())
                    .doOnComplete(() -> latch.countDown())
                    .subscribe();

            latch.await();

            // Track status of deployment operations

            System.out.println("Checking deployment operations...");
            final CountDownLatch operationLatch = new CountDownLatch(1);
            Flux.fromIterable(deploymentList)
                    .flatMap(deployment -> deployment.refreshAsync()
                            .flatMapMany(dp -> dp.deploymentOperations().listAsync())
                            .collectList()
                            .map(deploymentOperations -> {
                                synchronized (deploymentList) {
                                    System.out.println("--------------------" + deployment.name() + "--------------------");
                                    for (DeploymentOperation operation : deploymentOperations) {
                                        if (operation.targetResource() != null) {
                                            System.out.println(String.format("%s - %s: %s %s",
                                                    operation.targetResource().resourceName(),
                                                    operation.targetResource().resourceName(),
                                                    operation.provisioningState(),
                                                    operation.statusMessage() != null ? operation.statusMessage() : ""));
                                        }
                                    }
                                }
                                return deploymentOperations;
                            })
                            .repeatWhen(observable -> observable.delaySubscription(Duration.ofSeconds(pollingInterval)))
                            .takeUntil(deploymentOperations -> {
                                return "Succeeded".equalsIgnoreCase(deployment.provisioningState())
                                        || "Canceled".equalsIgnoreCase(deployment.provisioningState())
                                        || "Failed".equalsIgnoreCase(deployment.provisioningState());

                            })
                    )
                    .onErrorResume(e -> Mono.empty())
                    .doOnComplete(() -> operationLatch.countDown())
                    .subscribe();

            operationLatch.await();

            // Summary

            List<String> succeeded = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            for (Deployment deployment : deploymentList) {
                if ("Succeeded".equalsIgnoreCase(deployment.provisioningState())) {
                    succeeded.add(deployment.name());
                } else {
                    failed.add(deployment.name());
                }
            }
            System.out.println(String.format("Deployments %s succeeded. %s failed.",
                    String.join(", ", succeeded), String.join(", ", failed)));

            return true;
        } finally {
            try {
                for (int i = 1; i != numDeployments; i++) {
                    String rgName = rgPrefix + "-" + i;
                    System.out.println("Deleting Resource Group: " + rgName);
                    azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                }
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }

        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=================================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager, -1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private static String getSSHPublicKey() {
        byte[] content;
        try {
            content = ByteStreams.toByteArray(DeployUsingARMTemplateWithDeploymentOperations.class.getResourceAsStream("/rsa.pub"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private DeployUsingARMTemplateWithDeploymentOperations() {

    }
}
