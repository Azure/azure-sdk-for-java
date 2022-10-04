// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DeployUpdateSample {
    public static void main(String[] args) {
        DeviceManagementClient client = new DeviceManagementClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        try {
            String updateProvider = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_PROVIDER");
            String updateName = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_NAME");
            String updateVersion = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_VERSION");
            String groupId = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_DEVICE_GROUP");

            String deploymentId = UUID.randomUUID().toString();
            String startAt = OffsetDateTime.now().toString();
            String deployment = String.format("{\"deploymentId\": \"%s\", \"startDateTime\": \"%s\", \"update\": {"
                    + "\"updateId\": {"
                    + "\"provider\": \"%s\", \"name\": \"%s\", \"version\": \"%s\""
                    + "}},"
                    + "\"groupId\": \"%s\""
                    + "}",
                deploymentId, startAt,
                updateProvider, updateName, updateVersion,
                groupId);

            // BEGIN: com.azure.iot.deviceupdate.DeviceManagementClient.DeployUpdate
            Response<BinaryData> response = client.createOrUpdateDeploymentWithResponse(groupId, deploymentId, BinaryData.fromString(deployment), null);
            // END: com.azure.iot.deviceupdate.DeviceManagementClient.DeployUpdate

            // BEGIN: com.azure.iot.deviceupdate.DeviceManagementClient.CheckDeploymentState
            Response<BinaryData> stateResponse = client.getDeploymentStatusWithResponse(groupId, deploymentId, null);
            System.out.println(stateResponse.getValue());
            // END: com.azure.iot.deviceupdate.DeviceManagementClient.CheckDeploymentState

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
                System.out.println("update does not exist");
            }
        }
    }
}
