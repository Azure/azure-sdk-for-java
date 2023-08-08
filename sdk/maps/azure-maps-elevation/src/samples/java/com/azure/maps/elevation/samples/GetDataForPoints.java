// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.elevation.ElevationAsyncClient;
import com.azure.maps.elevation.ElevationClient;
import com.azure.maps.elevation.ElevationClientBuilder;
import java.util.Arrays;

public class GetDataForPoints {
    public static void main(String[] args) {
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        ElevationClient client = new ElevationClientBuilder()
            .credential(keyCredential)
            .elevationClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();

        // Get Data For Points -
        // https://docs.microsoft.com/en-us/rest/api/maps/elevation/get-data-for-points
        // The Get Data for Points API provides elevation data for one or more points.
        // A point is defined in lat,long coordinate format.
        // Due to the URL character length limit of 2048, it's not possible to pass more than 100 coordinates as a pipeline delimited string in a URL GET request.
        // If you intend to pass more than 100 coordinates as a pipeline delimited string, use the POST Data For Points.
        System.out.println("Get Data For Points Sync Client");
        // BEGIN: com.azure.maps.elevation.sync.get_data_for_points
        client.getDataForPoints(Arrays.asList(
            new GeoPosition(-121.66853362143818, 46.84646479863713),
            new GeoPosition(-121.68853362143818, 46.856464798637127)));
        // END: com.azure.maps.elevation.sync.get_data_for_points

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();

        ElevationAsyncClient asyncClient = new ElevationClientBuilder()
            .credential(asyncClientKeyCredential)
            .elevationClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // Get Data For Points -
        // https://docs.microsoft.com/en-us/rest/api/maps/elevation/get-data-for-points
        // The Get Data for Points API provides elevation data for one or more points.
        // A point is defined in lat,long coordinate format.
        // Due to the URL character length limit of 2048, it's not possible to pass more than 100 coordinates as a pipeline delimited string in a URL GET request.
        // If you intend to pass more than 100 coordinates as a pipeline delimited string, use the POST Data For Points.
        System.out.println("Get Data For Points Async Client");
        // BEGIN: com.azure.maps.elevation.async.get_data_for_points
        asyncClient.getDataForPoints(Arrays.asList(
            new GeoPosition(-121.66853362143818, 46.84646479863713),
            new GeoPosition(-121.68853362143818, 46.856464798637127)));
        // END: com.azure.maps.elevation.async.get_data_for_points
    }
}
