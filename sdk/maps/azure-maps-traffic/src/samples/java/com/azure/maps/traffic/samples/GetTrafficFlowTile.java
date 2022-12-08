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
import com.azure.maps.traffic.models.TrafficFlowTileOptions;
import com.azure.maps.traffic.models.TrafficFlowTileStyle;

public class GetTrafficFlowTile {
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

        // Get traffic flow tile -
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-flow-tile
        // BEGIN: com.azure.maps.traffic.sync.get_traffic_flow_tile
        System.out.println("Get Traffic Flow Tile:");

        // options
        client.getTrafficFlowTile(
            new TrafficFlowTileOptions()
                .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY).setFormat(TileFormat.PNG).setZoom(10)
                .setTileIndex(new TileIndex().setX(1022).setY(680)));

        // complete
        client.getTrafficFlowTile(
            new TrafficFlowTileOptions()
                .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY).setFormat(TileFormat.PNG).setZoom(10)
                .setTileIndex(new TileIndex().setX(1022).setY(680)).setThickness(10));
        // END: com.azure.maps.traffic.sync.get_traffic_flow_tile

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

        // Get traffic flow segment -
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-flow-tile
        // BEGIN: com.azure.maps.traffic.async.get_traffic_flow_tile
        System.out.println("Get Traffic Flow Tile:");

        // options
        asyncClient.getTrafficFlowTile(
            new TrafficFlowTileOptions()
                .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY).setFormat(TileFormat.PNG).setZoom(10)
                .setTileIndex(new TileIndex().setX(1022).setY(680)));

        // complete
        asyncClient.getTrafficFlowTile(
            new TrafficFlowTileOptions()
                .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY).setFormat(TileFormat.PNG).setZoom(10)
                .setTileIndex(new TileIndex().setX(1022).setY(680)).setThickness(10));
        // END: com.azure.maps.traffic.async.get_traffic_flow_tile
    }
}
