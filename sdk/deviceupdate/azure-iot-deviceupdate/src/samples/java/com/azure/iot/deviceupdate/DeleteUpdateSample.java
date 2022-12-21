// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DeleteUpdateSample {
    public static void main(String[] args) {
        DeviceUpdateClient client = new DeviceUpdateClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        try {
            String updateProvider = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_PROVIDER");
            String updateName = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_NAME");
            String updateVersion = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_VERSION");

            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.DeleteUpdate
            SyncPoller<BinaryData, BinaryData> response = client.beginDeleteUpdate(updateProvider, updateName, updateVersion, null);
            response.waitForCompletion();
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.DeleteUpdate

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
                System.out.println("update does not exist");
            }
        }
    }
}
