// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.AssetUpdateData;
import com.azure.analytics.defender.easm.models.Task;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * External IDs can be a useful method of keeping track of assets in multiple systems, but it can be time consuming to
 * manually tag each asset. In this example, we'll take a look at how you can, with a map of name/kind/external id, tag
 * each asset in your inventory with an external id automatically using the SDK
 *
 * Set the following environment variables before running the sample:
 *     1) SUBSCRIPTION_ID - the subscription id for your resource
 *     2) WORKSPACE_NAME - the workspace name for your resource
 *     3) RESOURCE_GROUP - the resource group for your resource
 *     4) REGION - the azure region your resource is in
 *     5) MAPPING - a json file with an external id mapping, like so:
 *
 *     [
 *  *         {
 *  *             'name': 'example.com',
 *  *             'kind': 'host',
 *  *             'external_id': 'EXT040'
 *  *         },
 *  *         {
 *  *             'name': 'example.com',
 *  *             'kind': 'domain',
 *  *             'external_id': 'EXT041'
 *  *         }
 *  *     ]
 *
 */
public class ManagingExternalIdsSample {
    public static void main(String[] args) throws JsonProcessingException {
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT");
        String externalIdMapping = Configuration.getGlobalConfiguration().get("MAPPING");

        EasmClient easmClient = new EasmClientBuilder()
            .endpoint(endpoint)
            // For the purposes of this demo, I've chosen the InteractiveBrowserCredential but any credential will work.
            .credential(new InteractiveBrowserCredentialBuilder().build())
            .buildClient();

        // We can update each asset and append the tracking id of the update to our update ID list, so that
        // we can keep track of the progress on each update later
        List<String> updateIds = new ArrayList<>();
        List<String> externalIds = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode mapping = objectMapper.readTree(externalIdMapping);

        mapping.elements().forEachRemaining((mappingInstance) -> {

            externalIds.add(mappingInstance.get("externalId").toString());

            AssetUpdateData assetUpdateRequest = new AssetUpdateData()
                .setExternalId(mappingInstance.get("externalId").toString());
            String filter = "kind = "
                + mappingInstance.get("kind")
                + " AND name = "
                + mappingInstance.get("name");
            Task taskResponse = easmClient.updateAssets(filter, assetUpdateRequest);
            updateIds.add(taskResponse.getId());
        });

        // We can view the progress of each update using the tasksGet method
        updateIds.forEach(id -> {
            Task taskResponse = easmClient.getTask(id);
            System.out.println(taskResponse.getId() + ": " + taskResponse.getState());
        });

        // The updates can be viewed using the `assetsList` method by creating a filter that matches on each
        // external id using an `in` query
        String assetFilter = "External ID in ".concat(String.join(", ", externalIds));

        easmClient.listAssetResource(assetFilter, "lastSeen", 0, null)
            .forEach(assetResponse -> {
                System.out.println(assetResponse.getExternalId() + ", " + assetResponse.getName());
            });
    }
}
