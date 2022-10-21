// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.timezone.TimezoneAsyncClient;
import com.azure.maps.timezone.TimezoneClient;
import com.azure.maps.timezone.TimezoneClientBuilder;

public class GetIanaTimezoneIds {
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

        // Get Timezone Enum IANA -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-enum-iana
        // This API returns a full list of IANA time zone IDs. Updates to the IANA service will be reflected in the system within one day.
        // BEGIN: com.azure.maps.timezone.sync.get_timezone_enum_iana
        client.getIanaTimezoneIds();
        // END: com.azure.maps.timezone.sync.get_timezone_enum_iana

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

        // Get Timezone Enum IANA -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-enum-iana
        // This API returns a full list of IANA time zone IDs. Updates to the IANA service will be reflected in the system within one day.
        // BEGIN: com.azure.maps.timezone.async.get_timezone_enum_iana
        asyncClient.getIanaTimezoneIds();
        // END: com.azure.maps.timezone.async.get_timezone_enum_iana
    }
}
