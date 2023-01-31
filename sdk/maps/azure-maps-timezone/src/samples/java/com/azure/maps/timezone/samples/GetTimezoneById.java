// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.maps.timezone.TimeZoneAsyncClient;
import com.azure.maps.timezone.TimeZoneClient;
import com.azure.maps.timezone.TimeZoneClientBuilder;
import com.azure.maps.timezone.models.TimeZoneIdOptions;
import com.azure.maps.timezone.models.TimeZoneOptions;

public class GetTimezoneById {
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

        // Get Timezone By Id -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-id
        // This API returns current, historical, and future time zone information for the specified IANA time zone ID.
        System.out.println("Get Timezone By Id Sync Client");
        // BEGIN: com.azure.maps.timezone.sync.get_timezone_by_id
        TimeZoneIdOptions options = new TimeZoneIdOptions("Asia/Bahrain").setOptions(TimeZoneOptions.ALL);
        client.getTimezoneById(options);
        // END: com.azure.maps.timezone.sync.get_timezone_by_id

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

        // Get Timezone By Id -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-id
        // This API returns current, historical, and future time zone information for the specified IANA time zone ID.
        System.out.println("Get Timezone By Id Async Client");
        // BEGIN: com.azure.maps.timezone.async.get_timezone_by_id
        TimeZoneIdOptions options2 = new TimeZoneIdOptions("Asia/Bahrain").setOptions(TimeZoneOptions.ALL);
        asyncClient.getTimezoneById(options2);
        // END: com.azure.maps.timezone.async.get_timezone_by_id
    }
}
