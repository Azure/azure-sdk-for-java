// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.timezone.TimezoneAsyncClient;
import com.azure.maps.timezone.TimezoneClient;
import com.azure.maps.timezone.TimezoneClientBuilder;
import com.azure.maps.timezone.models.TimezoneCoordinateOptions;
import com.azure.maps.timezone.models.TimezoneOptions;

public class GetTimezoneByCoordinates {
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

        // Get Timezone By Coordinates -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-coordinates
        // This API returns current, historical, and future time zone information for a specified latitude-longitude pair. 
        // In addition, the API provides sunset and sunrise times for a given location.
        // BEGIN: com.azure.maps.timezone.sync.get_timezone_by_coordinates
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimezoneCoordinateOptions options = new TimezoneCoordinateOptions().setCoordinates(coordinate).setOptions(TimezoneOptions.ALL);
        client.getTimezoneByCoordinates(options);
        // END: com.azure.maps.timezone.sync.get_timezone_by_coordinates

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

        // Get Timezone By Coordinates -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-coordinates
        // This API returns current, historical, and future time zone information for a specified latitude-longitude pair. 
        // In addition, the API provides sunset and sunrise times for a given location.
        // BEGIN: com.azure.maps.timezone.async.get_timezone_by_coordinates
        GeoPosition coordinate2 = new GeoPosition(-122, 47.0);
        TimezoneCoordinateOptions options2 = new TimezoneCoordinateOptions().setCoordinates(coordinate2).setOptions(TimezoneOptions.ALL);
        asyncClient.getTimezoneByCoordinates(options2);
        // END: com.azure.maps.timezone.async.get_timezone_by_coordinates
    }
}
