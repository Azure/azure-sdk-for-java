// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.timezone.TimezoneAsyncClient;
import com.azure.maps.timezone.TimezoneClient;
import com.azure.maps.timezone.TimezoneClientBuilder;
import com.azure.maps.timezone.models.TimezoneIdOptions;
import com.azure.maps.timezone.models.TimezoneOptions;

public class GetTimezoneById {
    public static void main(String[] args) {
        TimezoneClientBuilder builder = new TimezoneClientBuilder();

        // Authenticates using subscription key
        // AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        builder.credential(tokenCredential);
        builder.timezoneClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        TimezoneClient client = builder.buildClient();

        // Get Timezone By Id -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-id
        // This API returns current, historical, and future time zone information for the specified IANA time zone ID.
        // BEGIN: com.azure.maps.timezone.sync.get_timezone_by_id
        TimezoneIdOptions options = new TimezoneIdOptions().setTimezoneId("Asia/Bahrain").setOptions(TimezoneOptions.ALL);
        client.getTimezoneById(options);
        // END: com.azure.maps.timezone.sync.get_timezone_by_id

        TimezoneClientBuilder asyncClientbuilder = new TimezoneClientBuilder();

        // Authenticates using subscription key
        // AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();

        asyncClientbuilder.credential(asyncClientTokenCredential);
        asyncClientbuilder.timezoneClientId(System.getenv("MAPS_CLIENT_ID"));
        asyncClientbuilder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        TimezoneAsyncClient asyncClient = asyncClientbuilder.buildAsyncClient();

        // Get Timezone By Id -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-id
        // This API returns current, historical, and future time zone information for the specified IANA time zone ID.
        // BEGIN: com.azure.maps.timezone.async.get_timezone_by_id
        TimezoneIdOptions options2 = new TimezoneIdOptions().setTimezoneId("Asia/Bahrain").setOptions(TimezoneOptions.ALL);
        asyncClient.getTimezoneById(options2);
        // END: com.azure.maps.timezone.async.get_timezone_by_id
    }
}
