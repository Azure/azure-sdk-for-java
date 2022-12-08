// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.maps.traffic.TrafficAsyncClient;
import com.azure.maps.traffic.TrafficClient;
import com.azure.maps.traffic.TrafficClientBuilder;
import com.azure.maps.traffic.models.TileFormat;
import com.azure.maps.traffic.models.TileIndex;
import com.azure.maps.traffic.models.TrafficIncidentTileOptions;
import com.azure.maps.traffic.models.TrafficIncidentTileStyle;

public class GetTrafficIncidentTile {
    public static void main(String[] args) throws IOException {
        TrafficClientBuilder builder = new TrafficClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // builder.credential(tokenCredential);

        builder.trafficClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        TrafficClient client = builder.buildClient();

        // Get Traffic Incident Tile -
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-incident-tile
        // BEGIN: com.azure.maps.traffic.sync.get_traffic_incident_tile
        System.out.println("Get Traffic Incident Tile:");

        // options
        client.getTrafficIncidentTile(
            new TrafficIncidentTileOptions()
                .setFormat(TileFormat.PNG).setTrafficIncidentTileStyle(TrafficIncidentTileStyle.S3)
                .setZoom(10).setTileIndex(new TileIndex().setX(1022).setY(680)));

        // complete
        client.getTrafficIncidentTile(
            new TrafficIncidentTileOptions()
                .setFormat(TileFormat.PNG).setTrafficIncidentTileStyle(TrafficIncidentTileStyle.S3)
                .setZoom(10).setTileIndex(new TileIndex().setX(175).setY(408)));
        // END: com.azure.maps.traffic.sync.get_traffic_incident_tile

        TrafficClientBuilder asyncClientbuilder = new TrafficClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        asyncClientbuilder.credential(asyncClientKeyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();
        // asyncClientbuilder.credential(asyncClientTokenCredential);

        asyncClientbuilder.trafficClientId(System.getenv("MAPS_CLIENT_ID"));
        asyncClientbuilder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        TrafficAsyncClient asyncClient = asyncClientbuilder.buildAsyncClient();

        // Get Traffic Incident Tile -
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-incident-tile
        // BEGIN: com.azure.maps.traffic.async.get_traffic_incident_tile
        System.out.println("Get Traffic Incident Tile:");

        // options
        asyncClient.getTrafficIncidentTile(
            new TrafficIncidentTileOptions()
                .setFormat(TileFormat.PNG).setTrafficIncidentTileStyle(TrafficIncidentTileStyle.S3)
                .setZoom(10).setTileIndex(new TileIndex().setX(1022).setY(680)));

        // complete
        asyncClient.getTrafficIncidentTile(
            new TrafficIncidentTileOptions()
                .setFormat(TileFormat.PNG).setTrafficIncidentTileStyle(TrafficIncidentTileStyle.S3)
                .setZoom(10).setTileIndex(new TileIndex().setX(175).setY(408)));
        // END: com.azure.maps.traffic.async.get_traffic_incident_tile
    }
}
