// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetUpdateSample {
    public static void main(String[] args) {
        // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.instantiate
        DeviceUpdateClient client = new DeviceUpdateClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.iot.deviceupdate.DeviceUpdateClient.instantiate

        try {
            String updateProvider = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_PROVIDER");
            String updateName = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_NAME");
            String updateVersion = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_VERSION");

            System.out.println("Get update data for provider '" + updateProvider + "', name '" + updateName + "' and version '" + updateVersion + "'...");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.GetUpdate
            Response<BinaryData> response = client.getUpdateWithResponse(updateProvider, updateName, updateVersion, null);
            System.out.println(response.getValue());
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.GetUpdate

            System.out.println("\nEnumerate update files...");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateUpdateFiles
            PagedIterable<BinaryData> items = client.listFiles(updateProvider, updateName, updateVersion, null);
            for (BinaryData i: items) {
                System.out.println(i);
            }
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateUpdateFiles

            System.out.println("\nGet file data...");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.GetFiles
            PagedIterable<BinaryData> files = client.listFiles(updateProvider, updateName, updateVersion, null);
            for (BinaryData f: files) {
                Response<BinaryData> fileResponse  = client.getFileWithResponse(updateProvider, updateName, updateVersion, f.toString(), null);
                System.out.println(fileResponse.getValue());
            }
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.GetFiles

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
                System.out.println("update does not exist");
            }
        }
    }
}
