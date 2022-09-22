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

public class ConvertWindowsTimezoneToIana {
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

        // Convert Windows Timezone To Iana -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-windows-to-iana
        // This API returns a corresponding IANA ID, given a valid Windows Time Zone ID. 
        // Multiple IANA IDs may be returned for a single Windows ID. 
        // It is possible to narrow these results by adding an optional territory parameter.
        // BEGIN: com.azure.maps.timezone.sync.convert_windows_timezone_to_iana
        client.convertWindowsTimezoneToIana("pacific standard time", null);
        // END: com.azure.maps.timezone.sync.convert_windows_timezone_to_iana

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

        // Convert Windows Timezone To Iana -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-windows-to-iana
        // This API returns a corresponding IANA ID, given a valid Windows Time Zone ID. 
        // Multiple IANA IDs may be returned for a single Windows ID. 
        // It is possible to narrow these results by adding an optional territory parameter.
        // BEGIN: com.azure.maps.timezone.async.convert_windows_timezone_to_iana
        asyncClient.convertWindowsTimezoneToIana("pacific standard time", null);
        // END: com.azure.maps.timezone.async.convert_windows_timezone_to_iana
    }
}
