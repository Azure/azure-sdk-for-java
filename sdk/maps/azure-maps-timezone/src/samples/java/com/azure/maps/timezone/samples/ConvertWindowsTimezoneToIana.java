// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.maps.timezone.TimeZoneAsyncClient;
import com.azure.maps.timezone.TimeZoneClient;
import com.azure.maps.timezone.TimeZoneClientBuilder;

public class ConvertWindowsTimezoneToIana {
    public static void main(String[] args) {
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        TimeZoneClient client = new TimeZoneClientBuilder()
            .credential(keyCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();

        // Convert Windows Timezone To Iana -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-windows-to-iana
        // This API returns a corresponding IANA ID, given a valid Windows Time Zone ID.
        // Multiple IANA IDs may be returned for a single Windows ID.
        // It is possible to narrow these results by adding an optional territory parameter.
        System.out.println("Convert Windows Timezone to Iana Sync Client");
        // BEGIN: com.azure.maps.timezone.sync.convert_windows_timezone_to_iana
        client.convertWindowsTimezoneToIana("pacific standard time", null);
        // END: com.azure.maps.timezone.sync.convert_windows_timezone_to_iana

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();

        TimeZoneAsyncClient asyncClient = new TimeZoneClientBuilder()
            .credential(asyncClientKeyCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // Convert Windows Timezone To Iana -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-windows-to-iana
        // This API returns a corresponding IANA ID, given a valid Windows Time Zone ID.
        // Multiple IANA IDs may be returned for a single Windows ID.
        // It is possible to narrow these results by adding an optional territory parameter.
        System.out.println("Convert Windows Timezone to Iana Async Client");
        // BEGIN: com.azure.maps.timezone.async.convert_windows_timezone_to_iana
        asyncClient.convertWindowsTimezoneToIana("pacific standard time", null);
        // END: com.azure.maps.timezone.async.convert_windows_timezone_to_iana
    }
}
