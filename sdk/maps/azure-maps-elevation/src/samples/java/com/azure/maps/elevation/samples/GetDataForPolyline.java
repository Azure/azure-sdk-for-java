// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoPosition;
import com.azure.maps.elevation.ElevationAsyncClient;
import com.azure.maps.elevation.ElevationClient;
import com.azure.maps.elevation.ElevationClientBuilder;
import java.util.Arrays;

public class GetDataForPolyline {
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

        // Get Data For Polyline -
        // https://docs.microsoft.com/en-us/rest/api/maps/elevation/get-data-for-polyline
        // A polyline is defined by passing in between 2 and N endpoint coordinates separated by a pipe ('|') character.
        // In addition to passing in endpoints, customers can specify the number of sample points that will be used to divide polyline into equally spaced segments.
        // Elevation data at both start and end points, as well as equally spaced points along the polyline will be returned.
        // The results will be listed in the direction from the first endpoint towards the last endpoint.
        // A line between two endpoints is a straight Cartesian line, the shortest line between those two points in the coordinate reference system.
        // Note that the point is chosen based on Euclidean distance and may markedly differ from the geodesic path along the curved surface of the reference ellipsoid.
        // If you intend to pass more than 100 coordinates as a pipeline delimited string, use the POST Data For Polyline.
        System.out.println("Get Data For Polyline Sync Client");
        // BEGIN: com.azure.maps.elevation.sync.get_data_for_polyline
        client.getDataForPolyline(Arrays.asList(
            new GeoPosition(-121.66853362143818, 46.84646479863713),
            new GeoPosition(-121.65853362143818, 46.85646479863713)), 5);
        // END: com.azure.maps.elevation.sync.get_data_for_polyline

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

        // Get Data For Polyline -
        // https://docs.microsoft.com/en-us/rest/api/maps/elevation/get-data-for-polyline
        // A polyline is defined by passing in between 2 and N endpoint coordinates separated by a pipe ('|') character.
        // In addition to passing in endpoints, customers can specify the number of sample points that will be used to divide polyline into equally spaced segments.
        // Elevation data at both start and end points, as well as equally spaced points along the polyline will be returned.
        // The results will be listed in the direction from the first endpoint towards the last endpoint.
        // A line between two endpoints is a straight Cartesian line, the shortest line between those two points in the coordinate reference system.
        // Note that the point is chosen based on Euclidean distance and may markedly differ from the geodesic path along the curved surface of the reference ellipsoid.
        // If you intend to pass more than 100 coordinates as a pipeline delimited string, use the POST Data For Polyline.
        System.out.println("Get Data For Polyline Async Client");
        // BEGIN: com.azure.maps.elevation.async.get_data_for_polyline
        asyncClient.getDataForPolyline(Arrays.asList(
            new GeoPosition(-121.66853362143818, 46.84646479863713),
            new GeoPosition(-121.65853362143818, 46.85646479863713)), 5);
        // END: com.azure.maps.elevation.async.get_data_for_polyline
    }
}
