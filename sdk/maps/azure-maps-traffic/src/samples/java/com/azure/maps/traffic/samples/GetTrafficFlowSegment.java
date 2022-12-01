// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.maps.traffic.TrafficAsyncClient;
import com.azure.maps.traffic.TrafficClient;
import com.azure.maps.traffic.TrafficClientBuilder;
import com.azure.maps.traffic.models.SpeedUnit;
import com.azure.maps.traffic.models.TrafficFlowSegmentOptions;
import com.azure.maps.traffic.models.TrafficFlowSegmentStyle;

public class GetTrafficFlowSegment {
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

        // Get traffic flow segment -
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-flow-segment
        // BEGIN: com.azure.maps.traffic.sync.get_traffic_flow_segment
        System.out.println("Get Traffic Flow Segment:");

        // options
        client.getTrafficFlowSegment(
            new TrafficFlowSegmentOptions()
                .setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE).setZoom(10)
                .setCoordinates(new GeoPosition(4.84239, 52.41072)));

        // complete
        client.getTrafficFlowSegment(
            new TrafficFlowSegmentOptions()
                .setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE).setZoom(10)
                .setCoordinates(new GeoPosition(4.84239, 52.41072)).setOpenLr(false)
                .setThickness(2).setUnit(SpeedUnit.MPH));
        // END: com.azure.maps.traffic.sync.get_traffic_flow_segment

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
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-flow-segment
        // BEGIN: com.azure.maps.traffic.async.get_traffic_flow_segment
        System.out.println("Get Traffic Flow Segment:");

        // options
        asyncClient.getTrafficFlowSegment(
            new TrafficFlowSegmentOptions()
                .setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE).setZoom(10)
                .setCoordinates(new GeoPosition(4.84239, 52.41072)));

        // complete
        asyncClient.getTrafficFlowSegment(
            new TrafficFlowSegmentOptions()
                .setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE).setZoom(10)
                .setCoordinates(new GeoPosition(4.84239, 52.41072)).setOpenLr(false)
                .setThickness(2).setUnit(SpeedUnit.MPH));
        // END: com.azure.maps.traffic.async.get_traffic_flow_segment
    }
}
