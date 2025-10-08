// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import java.util.HashMap;
import java.util.Map;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {
    /**
    * Sample for creating low level client.
    */
    public void createClient() {
        // BEGIN: readme-sample-createPartiesClient
        String endpoint = "https://<farmbeats-endpoint>.farmbeats.azure.net";

        // Create Parties Client
        PartiesClientBuilder partiesBuilder = new PartiesClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());
        PartiesAsyncClient partiesClient = partiesBuilder.buildAsyncClient();

        // END: readme-sample-createPartiesClient
        // BEGIN: readme-sample-createBoundariesClient
        // Create Boundaries Client
        BoundariesClientBuilder boundariesBuilder = new BoundariesClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());
        BoundariesAsyncClient boundariesClient = boundariesBuilder.buildAsyncClient();
        // END: readme-sample-createBoundariesClient

        // BEGIN: readme-sample-createScenesClient
        // Create Scenes Client
        ScenesClientBuilder scenesBuilder = new ScenesClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());
        ScenesAsyncClient scenesClient = scenesBuilder.buildAsyncClient();
        // END: readme-sample-createScenesClient

        // BEGIN: readme-sample-createFarmHierarchy
        // Create Party
        Map<String, String> partyData = new HashMap<>();
        partyData.put("name", "party1");
        BinaryData party = BinaryData.fromObject(partyData);
        partiesClient.createOrUpdateWithResponse("contoso-party", party, null).block();

        // Get Party
        Response<BinaryData> response = partiesClient.getWithResponse("contoso-party", new RequestOptions()).block();
        System.out.println(response.getValue());

        // Create Boundary
        BinaryData boundary = BinaryData.fromString("{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[73.70457172393799,20.545385304358106],[73.70457172393799,20.545385304358106],[73.70448589324951,20.542411534243367],[73.70877742767334,20.541688176010233],[73.71023654937744,20.545083911372505],[73.70663166046143,20.546992723579137],[73.70457172393799,20.545385304358106]]]},\"name\":\"string\",\"description\":\"string\"}");
        response = boundariesClient.createOrUpdateWithResponse("contoso-party", "contoso-boundary", boundary, null).block();
        System.out.println(response.getValue());
        // END: readme-sample-createFarmHierarchy

        // BEGIN: readme-sample-ingestSatelliteData
        // Trigger Satellite job and wait for completion
        BinaryData satelliteJob = BinaryData.fromString("{\"boundaryId\":\"contoso-boundary\",\"endDateTime\":\"2022-02-01T00:00:00Z\",\"partyId\":\"contoso-party\",\"source\":\"Sentinel_2_L2A\",\"startDateTime\":\"2022-01-01T00:00:00Z\",\"provider\":\"Microsoft\",\"data\":{\"imageNames\":[\"NDVI\"],\"imageFormats\":[\"TIF\"],\"imageResolutions\":[10]},\"name\":\"string\",\"description\":\"string\"}");
        scenesClient.beginCreateSatelliteDataIngestionJob("contoso-job-46856", satelliteJob, null).getSyncPoller().waitForCompletion();
        System.out.println(scenesClient.getSatelliteDataIngestionJobDetailsWithResponse("contoso-job-46856", null).block().getValue());

        // Iterate through ingested scenes
        Iterable<BinaryData> scenes = scenesClient.list("Microsoft", "contoso-party", "contoso-boundary", "Sentinel_2_L2A", null).toIterable();
        scenes.forEach(scene -> System.out.println(scene));
        // END: readme-sample-ingestSatelliteData
    }
}
