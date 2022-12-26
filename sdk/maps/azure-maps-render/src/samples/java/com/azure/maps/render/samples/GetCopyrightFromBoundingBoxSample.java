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

public class GetCopyrightFromBoundingBoxSample {
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

        // Get Copyright From Bounding Box
        System.out.println("Get Copyright From Bounding Box");
        // BEGIN: com.azure.maps.render.sync.get_copyright_from_bounding_box
        GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        client.getCopyrightFromBoundingBox(boundingBox, true);
        // END: com.azure.maps.render.sync.get_copyright_from_bounding_box

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

        // Get Copyright From Bounding Box
        System.out.println("Get Copyright From Bounding Box");
        // BEGIN: com.azure.maps.render.async.get_copyright_from_bounding_box
        GeoBoundingBox boundingBox2 = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        asyncClient.getCopyrightFromBoundingBox(boundingBox2, true).block();
        // END: com.azure.maps.render.async.get_copyright_from_bounding_box
    }
}
