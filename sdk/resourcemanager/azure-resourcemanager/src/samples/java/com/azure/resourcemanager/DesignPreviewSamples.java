// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.management.Region;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;

import java.time.Duration;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class DesignPreviewSamples {

    private final String rgName = "rg-test";
    private final String contentVersion = "1.0.0.0";
    private final String templateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-cognitive-services-translate/azuredeploy.json";
    private final String parametersUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-cognitive-services-translate/azuredeploy.parameters.json";

    // extra empty lines to compensate import lines






























    // THIS LINE MUST BE AT LINE NO. 60
    public void pollLongRunningOperation(AzureResourceManager azure) throws InterruptedException {
        Duration defaultDelay = Duration.ofSeconds(10);
        // begin provision
        Accepted<Deployment> acceptedDeployment = azure.deployments()
            .define("deployment")
            .withNewResourceGroup(rgName, Region.US_WEST)
            .withTemplateLink(templateUri, contentVersion)
            .withParametersLink(parametersUri, contentVersion)
            .withMode(DeploymentMode.COMPLETE)
            .beginCreate();
        // get deployment before provision complete
        Deployment provisioningDeployment = acceptedDeployment.getActivationResponse().getValue();
        // get status of activation response
        LongRunningOperationStatus pollStatus = acceptedDeployment.getActivationResponse().getStatus();
        Duration delay = acceptedDeployment.getActivationResponse().getRetryAfter() == null
            ? defaultDelay
            : acceptedDeployment.getActivationResponse().getRetryAfter();
        while (!pollStatus.isComplete()) {
            Thread.sleep(delay.toMillis());
            // poll and get status
            PollResponse<?> pollResponse = acceptedDeployment.getSyncPoller().poll();
            pollStatus = pollResponse.getStatus();
            delay = pollResponse.getRetryAfter() == null
                ? defaultDelay
                : pollResponse.getRetryAfter();
        }
        // pollStatus == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, if successful
        // get final result
        Deployment deployment = acceptedDeployment.getFinalResult();
    }

    public String getResourceGroupName() {
        return rgName;
    }
}
