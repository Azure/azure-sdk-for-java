// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoBoundingBox;
import com.azure.maps.traffic.TrafficAsyncClient;
import com.azure.maps.traffic.TrafficClient;
import com.azure.maps.traffic.TrafficClientBuilder;
import com.azure.maps.traffic.models.IncidentDetailStyle;
import com.azure.maps.traffic.models.IncidentGeometryType;
import com.azure.maps.traffic.models.ProjectionStandard;
import com.azure.maps.traffic.models.TrafficIncidentDetailOptions;


public class GetTrafficIncidentDetail {
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

        // Get traffic incident detail -
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-incident-detail
        // BEGIN: com.azure.maps.traffic.sync.get_traffic_incident_detail
        System.out.println("Get Traffic Incident Detail:");

        // options
        client.getTrafficIncidentDetail(
            new TrafficIncidentDetailOptions()
                .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45)).setBoundingZoom(11)
                .setIncidentDetailStyle(IncidentDetailStyle.S3).setBoundingZoom(11)
                .setTrafficmodelId("1335294634919"));

        // complete
        client.getTrafficIncidentDetail(
            new TrafficIncidentDetailOptions()
                .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45)).setBoundingZoom(11)
                .setIncidentDetailStyle(IncidentDetailStyle.S3).setBoundingZoom(11)
                .setTrafficmodelId("1335294634919").setLanguage("en")
                .setProjectionStandard(ProjectionStandard.EPSG900913).setIncidentGeometryType(IncidentGeometryType.ORIGINAL)
                .setExpandCluster(false).setOriginalPosition(false));
        // END: com.azure.maps.traffic.sync.get_traffic_incident_detail

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

        // Get traffic incident detail -
        // https://docs.microsoft.com/en-us/rest/api/maps/traffic/get-traffic-incident-detail
        // BEGIN: com.azure.maps.traffic.async.get_traffic_incident_detail
        System.out.println("Get Traffic Incident Detail:");

        // options
        asyncClient.getTrafficIncidentDetail(
            new TrafficIncidentDetailOptions()
                .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45)).setBoundingZoom(11)
                .setIncidentDetailStyle(IncidentDetailStyle.S3).setBoundingZoom(11)
                .setTrafficmodelId("1335294634919"));

        // complete
        asyncClient.getTrafficIncidentDetail(
            new TrafficIncidentDetailOptions()
                .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45)).setBoundingZoom(11)
                .setIncidentDetailStyle(IncidentDetailStyle.S3).setBoundingZoom(11)
                .setTrafficmodelId("1335294634919").setLanguage("en")
                .setProjectionStandard(ProjectionStandard.EPSG900913).setIncidentGeometryType(IncidentGeometryType.ORIGINAL)
                .setExpandCluster(false).setOriginalPosition(false));
        // END: com.azure.maps.traffic.async.get_traffic_incident_detail
    }
}
