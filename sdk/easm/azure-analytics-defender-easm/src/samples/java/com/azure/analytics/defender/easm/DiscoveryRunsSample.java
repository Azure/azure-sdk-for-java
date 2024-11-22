// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.DiscoRunResult;
import com.azure.analytics.defender.easm.models.DiscoSource;
import com.azure.analytics.defender.easm.models.DiscoSourceKind;
import com.azure.analytics.defender.easm.models.DiscoGroupData;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;


import java.util.ArrayList;
import java.util.List;

/**
 * This sample demonstrates how to create and manage discovery runs in a workspace
 *
 *  Set the following environment variables before running the sample:
 *     1) SUBSCRIPTION_ID - the subscription id for your resource
 *     2) WORKSPACE_NAME - the workspace name for your resource
 *     3) RESOURCE_GROUP - the resource group for your resource
 *     4) REGION - the azure region your resource is in
 *     5) HOSTS - a comma separated list of hosts you would like to run discovery on
 *     6) DOMAINS - a comma separated list of hosts you would like to run discovery on
 */
public class DiscoveryRunsSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT");
        String discoveryGroupName = "Sample Disco";
        String discoveryGroupDescription = "This is a sample description for a discovery group";

        EasmClient easmClient = new EasmClientBuilder()
            .endpoint(endpoint)
            // For the purposes of this demo, I've chosen the InteractiveBrowserCredential but any credential will work.
            .credential(new InteractiveBrowserCredentialBuilder().build())
            .buildClient();

        // In order to start discovery runs, we must first create a discovery group, which is a collection of known assets that we can pivot off of.
        // These are created using the discoveryGroupsPut method
        List<DiscoSource> seeds = new ArrayList<>();
        seeds.add(new DiscoSource()
            .setKind(DiscoSourceKind.DOMAIN)
            .setName("contoso.org"));

        DiscoGroupData discoGroupRequest = new DiscoGroupData().setName(discoveryGroupName).setDescription(discoveryGroupDescription).setSeeds(seeds);

        easmClient.createOrReplaceDiscoGroup(discoveryGroupName, discoGroupRequest);

        // Discovery groups created through the API's `put` method aren't run automatically, so we need to start the run ourselves.
        easmClient.runDiscoGroup(discoveryGroupName);

        easmClient.listDiscoGroup()
            .forEach((discoGroupResponse -> {
                System.out.println(discoGroupResponse.getName());
                PagedIterable<DiscoRunResult> discoRunPageResponse = easmClient.listRuns(discoGroupResponse.getName(), null, 0);
                discoRunPageResponse.forEach(discoRunResponse -> {
                    System.out.println(" - started: " + discoRunResponse.getStartedDate()
                        + ", finished: " + discoRunResponse.getCompletedDate()
                        + ", assets found: " + discoRunResponse.getTotalAssetsFoundCount());
                });
            }));
    }
}
