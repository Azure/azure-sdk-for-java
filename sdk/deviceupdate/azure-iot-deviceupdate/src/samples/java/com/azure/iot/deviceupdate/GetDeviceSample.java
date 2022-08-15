// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetDeviceSample {
    public static void main(String[] args) {
        // BEGIN: com.azure.iot.deviceupdate.DeviceManagementClient.instantiate
        DeviceManagementClient client = new DeviceManagementClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.iot.deviceupdate.DeviceManagementClient.instantiate

        try {
            System.out.println("Devices:");
            // BEGIN: com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateDevices
            PagedIterable<BinaryData> devices = client.listDevices(null);
            for (BinaryData d: devices) {
                System.out.println(d);
            }
            // END: com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateDevices

            System.out.println("\nDevice groups:");
            // BEGIN: com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateGroups
            PagedIterable<BinaryData> groups = client.listGroups(null);
            for (BinaryData g: groups) {
                System.out.println(g);
            }
            // END: com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateGroups

            System.out.println("\nDevice classes:");
            // BEGIN: com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateDeviceClasses
            PagedIterable<BinaryData> deviceClasses = client.listDeviceClasses(null);
            for (BinaryData dc: deviceClasses) {
                System.out.println(dc);
            }
            // END: com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateDeviceClasses

            String groupId = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_DEVICE_GROUP");
            System.out.println("\nFor group '" + groupId + "' the best updates are:");
            // BEGIN: com.azure.iot.deviceupdate.DeviceManagementClient.GetBestUpdates
            PagedIterable<BinaryData> bestUpdates = client.listBestUpdatesForGroup(groupId, null);
            for (BinaryData bu: bestUpdates) {
                System.out.println(bu);
            }
            // END: com.azure.iot.deviceupdate.DeviceManagementClient.GetBestUpdates

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
                System.out.println("update does not exist");
            }
        }
    }
}
