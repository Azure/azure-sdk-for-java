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

public class ListUpdatesSample {
    public static void main(String[] args) {
        DeviceUpdateClient client = new DeviceUpdateClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        try {
            System.out.println("Enumerate providers:");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateProviders
            PagedIterable<BinaryData> providers = client.listProviders(null);
            for (BinaryData p: providers) {
                System.out.println(p);
            }
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateProviders

            String updateProvider = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_PROVIDER");
            System.out.println("\nEnumerate '" + updateProvider + "' names:");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateNames
            System.out.println("Providers:");
            PagedIterable<BinaryData> names = client.listNames(updateProvider, null);
            for (BinaryData n: names) {
                System.out.println(n);
            }
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateNames

            String updateName = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_NAME");
            System.out.println("\nEnumerate provider '" + updateProvider + "' and name '" + updateName + "' versions:");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateVersions
            PagedIterable<BinaryData> versions = client.listVersions(updateProvider, updateName, null);
            for (BinaryData v: versions) {
                System.out.println(v);
            }
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateVersions

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
                System.out.println("update does not exist");
            }
        }
    }
}
