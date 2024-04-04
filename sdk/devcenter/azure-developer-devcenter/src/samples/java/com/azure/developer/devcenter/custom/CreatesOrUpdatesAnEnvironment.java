// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.devcenter.custom;

import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.devcenter.DeploymentEnvironmentsClient;
import com.azure.developer.devcenter.DeploymentEnvironmentsClientBuilder;
import com.azure.developer.devcenter.models.DevCenterEnvironment;
import com.azure.developer.devcenter.models.DevCenterOperationDetails;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.HashMap;
import java.util.Map;

public class CreatesOrUpdatesAnEnvironment {
    public static void main(String[] args) {
        DeploymentEnvironmentsClient deploymentEnvironmentsClient
            = new DeploymentEnvironmentsClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(
                    "https://8a40af38-3b4c-4672-a6a4-5e964b1870ed-contosodevcenter.centralus.devcenter.azure.com/")
                .buildClient();
        // BEGIN:com.azure.developer.devcenter.createorupdateenvironment.createsorupdatesanenvironment
        Map<String, Object> paramenters = new HashMap<String, Object>() {{
                put("functionAppRuntime", "node");
                put("storageAccountType", "Standard_LRS");
            }};

        SyncPoller<DevCenterOperationDetails, DevCenterEnvironment> response
            = deploymentEnvironmentsClient.beginCreateOrUpdateEnvironment("myProject", "me",
                new DevCenterEnvironment("mydevenv", "DevTest", "main", "helloworld")
                    .setParameters(paramenters));
        // END:com.azure.developer.devcenter.createorupdateenvironment.createsorupdatesanenvironment
    }
}
