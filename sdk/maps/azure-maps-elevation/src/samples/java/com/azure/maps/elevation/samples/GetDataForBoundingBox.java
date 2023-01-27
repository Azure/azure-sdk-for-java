// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.GeoBoundingBox;
import com.azure.maps.elevation.ElevationAsyncClient;
import com.azure.maps.elevation.ElevationClient;
import com.azure.maps.elevation.ElevationClientBuilder;

public class GetDataForBoundingBox {
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

        // Get Data For Bounding Box -
        // https://docs.microsoft.com/en-us/rest/api/maps/elevation/get-data-for-bounding-box
        // The Get Data for Bounding Box API provides elevation data at equally spaced locations within a bounding box.
        // A bounding box is defined by the coordinates for two corners (southwest, northeast)
        // and then subsequently divided into rows and columns.
        // Elevations are returned for the vertices of the grid created by the rows and columns.
        // Up to 2,000 elevations can be returned in a single request.
        // The returned elevation values are ordered, starting at the southwest corner,
        // and then proceeding west to east along the row. At the end of the row,
        // it moves north to the next row, and repeats the process until it reaches the far northeast corner.
        System.out.println("Get Data For Bounding Box Sync Client");
        // BEGIN: com.azure.maps.elevation.sync.get_data_for_bounding_box
        client.getDataForBoundingBox(new GeoBoundingBox(-121.668533621438, 46.8464647986371,
            -121.658533621438, 46.8564647986371), 3, 3);
        // END: com.azure.maps.elevation.sync.get_data_for_bounding_box

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

        // Get Data For Bounding Box -
        // https://docs.microsoft.com/en-us/rest/api/maps/elevation/get-data-for-bounding-box
        // The Get Data for Bounding Box API provides elevation data at equally spaced locations within a bounding box.
        // A bounding box is defined by the coordinates for two corners (southwest, northeast)
        // and then subsequently divided into rows and columns.
        // Elevations are returned for the vertices of the grid created by the rows and columns.
        // Up to 2,000 elevations can be returned in a single request.
        // The returned elevation values are ordered, starting at the southwest corner,
        // and then proceeding west to east along the row. At the end of the row,
        // it moves north to the next row, and repeats the process until it reaches the far northeast corner.
        System.out.println("Get Data For Bounding Box Async Client");
        // BEGIN: com.azure.maps.elevation.async.get_data_for_bounding_box
        asyncClient.getDataForBoundingBox(new GeoBoundingBox(-121.668533621438f, 46.8464647986371f,
            -121.658533621438f, 46.8564647986371f), 3, 3);
        // END: com.azure.maps.elevation.async.get_data_for_bounding_box
    }
}
