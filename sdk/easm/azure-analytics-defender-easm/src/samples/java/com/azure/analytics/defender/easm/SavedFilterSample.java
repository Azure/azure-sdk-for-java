// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.AssetUpdateData;
import com.azure.analytics.defender.easm.models.AssetUpdateState;
import com.azure.analytics.defender.easm.models.SavedFilter;
import com.azure.analytics.defender.easm.models.SavedFilterData;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;

/**
 * Saved Filters are used to store a query within EASM, these saved queries can be used to synchronize exact queries across multiple scripts, or to ensure a team is looking at the same assets
 * In this example, we'll go over how a saved filter could be used to synchronize the a query across multiple scripts
 *
 * Set the following environment variables before running the sample:
 *     1) SUBSCRIPTION_ID - the subscription id for your resource
 *     2) WORKSPACE_NAME - the workspace name for your resource
 *     3) RESOURCE_GROUP - the resource group for your resource
 *     4) REGION - the azure region your resource is in
 */
public class SavedFilterSample {

    /*
        A sample asset list call that could be used to monitor the assets:
     */
    private static void monitor(SavedFilter response){
        // your monitor logic here
    }


    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT");

        EasmClient easmClient = new EasmClientBuilder()
            .endpoint(endpoint)
            // For the purposes of this demo, I've chosen the InteractiveBrowserCredential but any credential will work.
            .credential(new InteractiveBrowserCredentialBuilder().build())
            .buildClient();

        String savedFilterName = "Sample saved filter";
        SavedFilterData savedFilterRequest = new SavedFilterData("IP Address = 1.1.1.1", "Monitored Addresses");

        easmClient.createOrReplaceSavedFilter(savedFilterName, savedFilterRequest);

        String monitorFilter = easmClient.getSavedFilter(savedFilterName).getFilter();

        easmClient.listSavedFilter(monitorFilter, 0)
            .forEach(SavedFilterSample::monitor);

        AssetUpdateData assetUpdateRequest = new AssetUpdateData().setState(AssetUpdateState.CONFIRMED);
        easmClient.updateAssets(monitorFilter, assetUpdateRequest);

        // Should your needs change, the filter can be updated with no need to update the scripts it's used in
        // Simply submit a new `savedFiltersPut` request to replace the old description and filter with a new set

        SavedFilterData newSavedFilterRequest = new SavedFilterData("IP Address = 0.0.0.0", "Monitoring Addresses");

        easmClient.createOrReplaceSavedFilter(savedFilterName, newSavedFilterRequest);

    }
}
