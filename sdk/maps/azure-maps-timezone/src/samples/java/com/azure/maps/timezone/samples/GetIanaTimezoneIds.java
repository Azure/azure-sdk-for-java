// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.maps.timezone.TimeZoneAsyncClient;
import com.azure.maps.timezone.TimeZoneClient;
import com.azure.maps.timezone.TimeZoneClientBuilder;

public class GetIanaTimezoneIds {
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

        // Get Timezone Enum IANA -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-enum-iana
        // This API returns a full list of IANA time zone IDs. Updates to the IANA service will be reflected in the system within one day.
        System.out.println("Get Timezone Enum IANA Sync Client");
        // BEGIN: com.azure.maps.timezone.sync.get_timezone_enum_iana
        client.getIanaTimezoneIds();
        // END: com.azure.maps.timezone.sync.get_timezone_enum_iana

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

        // Get Timezone Enum IANA -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-enum-iana
        // This API returns a full list of IANA time zone IDs. Updates to the IANA service will be reflected in the system within one day.
        System.out.println("Get Timezone Enum IANA Async Client");
        // BEGIN: com.azure.maps.timezone.async.get_timezone_enum_iana
        asyncClient.getIanaTimezoneIds();
        // END: com.azure.maps.timezone.async.get_timezone_enum_iana
    }
}
