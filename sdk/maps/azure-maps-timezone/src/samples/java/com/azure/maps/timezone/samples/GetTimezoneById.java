// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.timezone.TimezoneAsyncClient;
import com.azure.maps.timezone.TimezoneClient;
import com.azure.maps.timezone.TimezoneClientBuilder;
import com.azure.maps.timezone.models.TimezoneIdOptions;
import com.azure.maps.timezone.models.TimezoneOptions;

public class GetTimezoneById {
    public static void main(String[] args) {
        // Authenticates using subscription key
        // AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        TimezoneClient client = new TimezoneClientBuilder() 
            .credential(tokenCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();

        // Get Timezone By Id -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-id
        // This API returns current, historical, and future time zone information for the specified IANA time zone ID.
        // BEGIN: com.azure.maps.timezone.sync.get_timezone_by_id
        TimezoneIdOptions options = new TimezoneIdOptions("Asia/Bahrain").setOptions(TimezoneOptions.ALL);
        client.getTimezoneById(options);
        // END: com.azure.maps.timezone.sync.get_timezone_by_id

        // Authenticates using subscription key
        // AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();

        TimezoneAsyncClient asyncClient = new TimezoneClientBuilder()
            .credential(asyncClientTokenCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // Get Timezone By Id -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-id
        // This API returns current, historical, and future time zone information for the specified IANA time zone ID.
        // BEGIN: com.azure.maps.timezone.async.get_timezone_by_id
        TimezoneIdOptions options2 = new TimezoneIdOptions("Asia/Bahrain").setOptions(TimezoneOptions.ALL);
        asyncClient.getTimezoneById(options2);
        // END: com.azure.maps.timezone.async.get_timezone_by_id
    }
}
