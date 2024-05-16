// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.timezone.TimeZoneAsyncClient;
import com.azure.maps.timezone.TimeZoneClient;
import com.azure.maps.timezone.TimeZoneClientBuilder;
import com.azure.maps.timezone.models.TimeZoneCoordinateOptions;
import com.azure.maps.timezone.models.TimeZoneOptions;

public class GetTimezoneByCoordinates {
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

        // Get Timezone By Coordinates -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-coordinates
        // This API returns current, historical, and future time zone information for a specified latitude-longitude pair.
        // In addition, the API provides sunset and sunrise times for a given location.
        System.out.println("Get Timezone By Coordinates Sync Client");
        // BEGIN: com.azure.maps.timezone.sync.get_timezone_by_coordinates
        GeoPosition cd = new GeoPosition(-122, 47.0);
        TimeZoneCoordinateOptions op = new TimeZoneCoordinateOptions(cd).setTimezoneOptions(TimeZoneOptions.ALL);
        client.getTimezoneByCoordinates(op);
        // END: com.azure.maps.timezone.sync.get_timezone_by_coordinates

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

        // Get Timezone By Coordinates -
        // https://docs.microsoft.com/en-us/rest/api/maps/timezone/get-timezone-by-coordinates
        // This API returns current, historical, and future time zone information for a specified latitude-longitude pair.
        // In addition, the API provides sunset and sunrise times for a given location.
        System.out.println("Get Timezone By Coordinates Async Client");
        // BEGIN: com.azure.maps.timezone.async.get_timezone_by_coordinates
        GeoPosition c2 = new GeoPosition(-122, 47.0);
        TimeZoneCoordinateOptions op2 = new TimeZoneCoordinateOptions(c2).setTimezoneOptions(TimeZoneOptions.ALL);
        asyncClient.getTimezoneByCoordinates(op2);
        // END: com.azure.maps.timezone.async.get_timezone_by_coordinates
    }
}
