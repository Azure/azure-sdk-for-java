// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroupExportTemplateOptions;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.models.WhatIfOperationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class DeploymentsTests extends ResourceManagerTestBase {
    private ResourceGroups resourceGroups;
    private ResourceGroup resourceGroup;

    private String testId;
    private String rgName;
    private static String templateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.json";
    private static String blankTemplateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/100-blank-template/azuredeploy.json";
    private static String parametersUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.parameters.json";
    private static String updateTemplate = "{\"$schema\":\"https://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json#\",\"contentVersion\":\"1.0.0.0\",\"parameters\":{\"vnetName\":{\"type\":\"string\",\"defaultValue\":\"VNet2\",\"metadata\":{\"description\":\"VNet name\"}},\"vnetAddressPrefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.0.0/16\",\"metadata\":{\"description\":\"Address prefix\"}},\"subnet1Prefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.0.0/24\",\"metadata\":{\"description\":\"Subnet 1 Prefix\"}},\"subnet1Name\":{\"type\":\"string\",\"defaultValue\":\"Subnet1\",\"metadata\":{\"description\":\"Subnet 1 Name\"}},\"subnet2Prefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.1.0/24\",\"metadata\":{\"description\":\"Subnet 2 Prefix\"}},\"subnet2Name\":{\"type\":\"string\",\"defaultValue\":\"Subnet222\",\"metadata\":{\"description\":\"Subnet 2 Name\"}}},\"variables\":{\"apiVersion\":\"2015-06-15\"},\"resources\":[{\"apiVersion\":\"[variables('apiVersion')]\",\"type\":\"Microsoft.Network/virtualNetworks\",\"name\":\"[parameters('vnetName')]\",\"location\":\"[resourceGroup().location]\",\"properties\":{\"addressSpace\":{\"addressPrefixes\":[\"[parameters('vnetAddressPrefix')]\"]},\"subnets\":[{\"name\":\"[parameters('subnet1Name')]\",\"properties\":{\"addressPrefix\":\"[parameters('subnet1Prefix')]\"}},{\"name\":\"[parameters('subnet2Name')]\",\"properties\":{\"addressPrefix\":\"[parameters('subnet2Prefix')]\"}}]}}]}";
    private static String updateParameters = "{\"vnetAddressPrefix\":{\"value\":\"10.0.0.0/16\"},\"subnet1Name\":{\"value\":\"Subnet1\"},\"subnet1Prefix\":{\"value\":\"10.0.0.0/24\"}}";
    private static String contentVersion = "1.0.0.0";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        super.initializeClients(httpPipeline, profile);
        testId = sdkContext.randomResourceName("", 9);
        resourceGroups = resourceClient.resourceGroups();
        rgName = "rg" + testId;
        resourceGroup = resourceGroups.define(rgName)
            .withRegion(Region.US_SOUTH_CENTRAL)
            .create();
    }

    @Override
    protected void cleanUpResources() {
        resourceGroups.beginDeleteByName(rgName);
    }

    @Test
    public void canDeployVirtualNetwork() throws Exception {
        final String dpName = "dpA" + testId;

        // Create
        resourceClient.deployments()
            .define(dpName)
            .withExistingResourceGroup(rgName)
            .withTemplateLink(templateUri, contentVersion)
            .withParametersLink(parametersUri, contentVersion)
            .withMode(DeploymentMode.COMPLETE)
            .create();
        // List
        PagedIterable<Deployment> deployments = resourceClient.deployments().listByResourceGroup(rgName);
        boolean found = false;
        for (Deployment deployment : deployments) {
            if (deployment.name().equals(dpName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
        // Check existence
        Assertions.assertTrue(resourceClient.deployments().checkExistence(rgName, dpName));

        // Get
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dpName);
        Assertions.assertNotNull(deployment);
        Assertions.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource generic = resourceClient.genericResources().get(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15");
        Assertions.assertNotNull(generic);
        // Export
        Assertions.assertNotNull(deployment.exportTemplate().templateAsJson());
        // Export from resource group
        Assertions.assertNotNull(resourceGroup.exportTemplate(ResourceGroupExportTemplateOptions.INCLUDE_BOTH));
        // Deployment operations
        PagedIterable<DeploymentOperation> operations = deployment.deploymentOperations().list();
        Assertions.assertEquals(5, TestUtilities.getSize(operations));
        DeploymentOperation op = deployment.deploymentOperations().getById(operations.iterator().next().operationId());
        Assertions.assertNotNull(op);
        resourceClient.genericResources().delete(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15");
    }

    @Test
    public void canPostDeploymentWhatIfOnResourceGroup() throws Exception {
        final String dpName = "dpA" + testId;

        // Create
        resourceClient.deployments()
            .define(dpName)
            .withExistingResourceGroup(rgName)
            .withTemplateLink(templateUri, contentVersion)
            .withParametersLink(parametersUri, contentVersion)
            .withMode(DeploymentMode.COMPLETE)
            .create();
        // List
        PagedIterable<Deployment> deployments = resourceClient.deployments().listByResourceGroup(rgName);
        boolean found = false;
        for (Deployment deployment : deployments) {
            if (deployment.name().equals(dpName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);

        // Get
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dpName);
        Assertions.assertNotNull(deployment);
        Assertions.assertEquals("Succeeded", deployment.provisioningState());

        //What if
        WhatIfOperationResult result = deployment.prepareWhatIf()
            .withIncrementalMode()
            .withWhatIfTemplateLink(templateUri, contentVersion)
            .whatIf();

        Assertions.assertEquals("Succeeded", result.status());
        Assertions.assertEquals(3, result.changes().size());

        resourceClient.genericResources().delete(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15");
    }

    @Test
    public void canPostDeploymentWhatIfOnSubscription() throws Exception {
        final String dpName = "dpA" + testId;

        // Create
        resourceClient.deployments()
            .define(dpName)
            .withExistingResourceGroup(rgName)
            .withTemplateLink(templateUri, contentVersion)
            .withParametersLink(parametersUri, contentVersion)
            .withMode(DeploymentMode.COMPLETE)
            .create();
        // List
        PagedIterable<Deployment> deployments = resourceClient.deployments().listByResourceGroup(rgName);
        boolean found = false;
        for (Deployment deployment : deployments) {
            if (deployment.name().equals(dpName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);

        // Get
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dpName);
        Assertions.assertNotNull(deployment);
        Assertions.assertEquals("Succeeded", deployment.provisioningState());

        //What if
        WhatIfOperationResult result = deployment.prepareWhatIf()
            .withLocation("westus")
            .withIncrementalMode()
            .withWhatIfTemplateLink(blankTemplateUri, contentVersion)
            .whatIfAtSubscriptionScope();

        Assertions.assertEquals("Succeeded", result.status());
        Assertions.assertEquals(0, result.changes().size());

        resourceClient.genericResources().delete(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15");
    }

    @Test
    @Disabled("deployment.cancel() doesn't throw but provisining state says Running not Cancelled...")
    public void canCancelVirtualNetworkDeployment() throws Exception {
        final String dp = "dpB" + testId;

        // Begin create
        resourceClient.deployments()
            .define(dp)
            .withExistingResourceGroup(rgName)
            .withTemplateLink(templateUri, contentVersion)
            .withParametersLink(parametersUri, contentVersion)
            .withMode(DeploymentMode.COMPLETE)
            .beginCreate();
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assertions.assertEquals(dp, deployment.name());
        // Cancel
        deployment.cancel();
        deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assertions.assertEquals("Canceled", deployment.provisioningState());
        Assertions.assertFalse(resourceClient.genericResources().checkExistence(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15"));
    }

    @Test
    public void canUpdateVirtualNetworkDeployment() throws Exception {
        final String dp = "dpC" + testId;

        // Begin create
        Accepted<Deployment> acceptedDeployment = resourceClient.deployments()
            .define(dp)
            .withExistingResourceGroup(rgName)
            .withTemplateLink(templateUri, contentVersion)
            .withParametersLink(parametersUri, contentVersion)
            .withMode(DeploymentMode.COMPLETE)
            .beginCreate();
        Deployment createdDeployment = acceptedDeployment.getActivationResponse().getValue();
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assertions.assertEquals(createdDeployment.correlationId(), deployment.correlationId());
        Assertions.assertEquals(dp, deployment.name());
        // Cancel
        deployment.cancel();
        deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assertions.assertEquals("Canceled", deployment.provisioningState());
        // Update
        deployment.update()
            .withTemplate(updateTemplate)
            .withParameters(updateParameters)
            .withMode(DeploymentMode.INCREMENTAL)
            .apply();
        deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assertions.assertEquals(DeploymentMode.INCREMENTAL, deployment.mode());
        Assertions.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource genericVnet = resourceClient.genericResources().get(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet2", "2015-06-15");
        Assertions.assertNotNull(genericVnet);
        resourceClient.genericResources().delete(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet2", "2015-06-15");
    }

    @Test
    public void canDeployVirtualNetworkSyncPoll() throws Exception {
        final String dp = "dpD" + testId;

        // Begin create
        Accepted<Deployment> acceptedDeployment = resourceClient.deployments()
            .define(dp)
            .withExistingResourceGroup(rgName)
            .withTemplateLink(templateUri, contentVersion)
            .withParametersLink(parametersUri, contentVersion)
            .withMode(DeploymentMode.COMPLETE)
            .beginCreate();
        Deployment createdDeployment = acceptedDeployment.getActivationResponse().getValue();
        Assertions.assertNotEquals("Succeeded", createdDeployment.provisioningState());

        LongRunningOperationStatus pollStatus = acceptedDeployment.getActivationResponse().getStatus();
        int delayInMills = acceptedDeployment.getActivationResponse().getRetryAfter() == null
            ? 0
            : (int) acceptedDeployment.getActivationResponse().getRetryAfter().toMillis();
        while (!pollStatus.isComplete()) {
            SdkContext.sleep(delayInMills);

            PollResponse<?> pollResponse = acceptedDeployment.getSyncPoller().poll();
            pollStatus = pollResponse.getStatus();
            delayInMills = pollResponse.getRetryAfter() == null
                ? 10000
                : (int) pollResponse.getRetryAfter().toMillis();
        }
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollStatus);
        Deployment deployment = acceptedDeployment.getFinalResult();
        Assertions.assertEquals("Succeeded", deployment.provisioningState());
    }

    @Test
    public void canDeployVirtualNetworkSyncPollWithFailure() throws Exception {
        final String templateJson = "{ \"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\", \"contentVersion\": \"1.0.0.0\", \"resources\": [ { \"type\": \"Microsoft.Storage/storageAccounts\", \"apiVersion\": \"2019-04-01\", \"name\": \"satestnameconflict\", \"location\": \"eastus\", \"sku\": { \"name\": \"Standard_LRS\" }, \"kind\": \"StorageV2\", \"properties\": { \"supportsHttpsTrafficOnly\": true } } ] }";

        final String dp = "dpE" + testId;
        // Begin create
        Accepted<Deployment> acceptedDeployment = resourceClient.deployments()
            .define(dp)
            .withExistingResourceGroup(rgName)
            .withTemplate(templateJson)
            .withParameters("{}")
            .withMode(DeploymentMode.COMPLETE)
            .beginCreate();
        Deployment createdDeployment = acceptedDeployment.getActivationResponse().getValue();
        Assertions.assertNotEquals("Succeeded", createdDeployment.provisioningState());

        LongRunningOperationStatus pollStatus = acceptedDeployment.getActivationResponse().getStatus();
        int delayInMills = acceptedDeployment.getActivationResponse().getRetryAfter() == null
            ? 0
            : (int) acceptedDeployment.getActivationResponse().getRetryAfter().toMillis();
        while (!pollStatus.isComplete()) {
            SdkContext.sleep(delayInMills);

            PollResponse<?> pollResponse = acceptedDeployment.getSyncPoller().poll();
            pollStatus = pollResponse.getStatus();
            delayInMills = pollResponse.getRetryAfter() == null
                ? 10000
                : (int) pollResponse.getRetryAfter().toMillis();
        }
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollStatus);
        Deployment deployment = acceptedDeployment.getFinalResult();
        Assertions.assertEquals("Succeeded", deployment.provisioningState());

        final String newRgName = sdkContext.randomResourceName("rg", 10);
        try {
            final String dp2 = "dpF" + testId;

            // storage name conflict
            acceptedDeployment = resourceClient.deployments()
                .define(dp2)
                .withNewResourceGroup(newRgName, Region.US_EAST2)
                .withTemplate(templateJson)
                .withParameters("{}")
                .withMode(DeploymentMode.COMPLETE)
                .beginCreate();
            createdDeployment = acceptedDeployment.getActivationResponse().getValue();
            Assertions.assertNotEquals("Succeeded", createdDeployment.provisioningState());

            pollStatus = acceptedDeployment.getActivationResponse().getStatus();
            delayInMills = acceptedDeployment.getActivationResponse().getRetryAfter() == null
                ? 0
                : (int) acceptedDeployment.getActivationResponse().getRetryAfter().toMillis();
            while (!pollStatus.isComplete()) {
                SdkContext.sleep(delayInMills);

                PollResponse<?> pollResponse = acceptedDeployment.getSyncPoller().poll();
                pollStatus = pollResponse.getStatus();
                delayInMills = pollResponse.getRetryAfter() == null
                    ? 10000
                    : (int) pollResponse.getRetryAfter().toMillis();
            }
            Assertions.assertEquals(LongRunningOperationStatus.FAILED, pollStatus);

            // check exception
            boolean exceptionOnFinalResult = false;
            try {
                deployment = acceptedDeployment.getFinalResult();
            } catch (ManagementException exception) {
                exceptionOnFinalResult = true;

                ManagementError managementError = exception.getValue();
                Assertions.assertEquals("DeploymentFailed", managementError.getCode());
                Assertions.assertNotNull(managementError.getMessage());
            }
            Assertions.assertTrue(exceptionOnFinalResult);

            // check operations
            deployment = resourceClient.deployments().getByResourceGroup(newRgName, dp2);
            Assertions.assertEquals("Failed", deployment.provisioningState());
            PagedIterable<DeploymentOperation> operations = deployment.deploymentOperations().list();
            Optional<DeploymentOperation> failedOperation = operations.stream()
                .filter(o -> "Failed".equalsIgnoreCase(o.provisioningState())).findFirst();
            Assertions.assertTrue(failedOperation.isPresent());
            Assertions.assertEquals("Conflict", failedOperation.get().statusCode());

            // check poll result again, should stay failed
            Assertions.assertEquals(LongRunningOperationStatus.FAILED, acceptedDeployment.getSyncPoller().poll().getStatus());
            exceptionOnFinalResult = false;
            try {
                deployment = acceptedDeployment.getFinalResult();
            } catch (ManagementException exception) {
                exceptionOnFinalResult = true;
            }
            Assertions.assertTrue(exceptionOnFinalResult);
        } finally {
            resourceClient.resourceGroups().beginDeleteByName(newRgName);
        }
    }
}
