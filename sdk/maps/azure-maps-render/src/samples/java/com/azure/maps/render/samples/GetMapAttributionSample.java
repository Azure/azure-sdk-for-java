// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoBoundingBox;
import com.azure.maps.render.MapsRenderAsyncClient;
import com.azure.maps.render.MapsRenderClient;
import com.azure.maps.render.MapsRenderClientBuilder;
import com.azure.maps.render.models.TilesetId;

public class GetMapAttributionSample {
    public static void main(String[] args) throws IOException {
        MapsRenderClientBuilder builder = new MapsRenderClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // builder.credential(tokenCredential);

        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsRenderClient client = builder.buildClient();

        // Get Map Attribution
        // BEGIN: com.azure.maps.render.sync.get_map_attribution
        System.out.println("Get Map Attribution");
        GeoBoundingBox bounds = new GeoBoundingBox(-122.414162, 47.57949, -122.247157, 47.668372);
        new TilesetId();
        client.getMapAttribution(TilesetId.MICROSOFT_BASE, 6, bounds);
        // END: com.azure.maps.render.sync.get_map_attribution

        MapsRenderClientBuilder asyncClientbuilder = new MapsRenderClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        asyncClientbuilder.credential(asyncClientKeyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();
        // asyncClientbuilder.credential(asyncClientTokenCredential);

        asyncClientbuilder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        asyncClientbuilder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsRenderAsyncClient asyncClient = asyncClientbuilder.buildAsyncClient();

        // Get Map Attribution
        // BEGIN: com.azure.maps.render.async.get_map_attribution
        System.out.println("Get Map Attribution");
        GeoBoundingBox bounds2 = new GeoBoundingBox(-122.414162, 47.57949, -122.247157, 47.668372);
        new TilesetId();
        asyncClient.getMapAttribution(TilesetId.MICROSOFT_BASE, 6, bounds2).block();
        // END: com.azure.maps.render.async.get_map_attribution
    }
}
