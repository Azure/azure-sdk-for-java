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

public class GetDeviceSample {
    public static void main(String[] args) {
        // BEGIN: com.azure.iot.deviceupdate.DeviceManagementAsyncClient.instantiate
        DeviceManagementAsyncClient client = new DeviceManagementClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
        // END: com.azure.iot.deviceupdate.DeviceManagementAsyncClient.instantiate

        try {
            Response<BinaryData> response = client.getDeviceWithResponse(
                Configuration.getGlobalConfiguration().get("DEVICEUPDATE_DEVICE"),
                null).block();

            System.out.println(response.getValue());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
                System.out.println("update does not exist");
            }
        }
    }
}
