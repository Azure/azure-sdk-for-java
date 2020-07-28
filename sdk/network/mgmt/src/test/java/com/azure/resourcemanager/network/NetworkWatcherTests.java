// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.models.AvailableProviders;
import com.azure.resourcemanager.network.models.AzureReachabilityReport;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NetworkWatcherTests extends NetworkManagementTest {

    @Test
    @Disabled("https://github.com/Azure/azure-rest-api-specs/issues/7579")
    public void canListProvidersAndGetReachabilityReport() throws Exception {
        String nwName = sdkContext.randomResourceName("nw", 8);
        Region region = Region.US_WEST;
        // make sure Network Watcher is disabled in current subscription and region as only one can exist
        PagedIterable<NetworkWatcher> nwList = networkManager.networkWatchers().list();
        for (NetworkWatcher nw : nwList) {
            if (region.equals(nw.region())) {
                networkManager.networkWatchers().deleteById(nw.id());
            }
        }
        // create Network Watcher
        NetworkWatcher nw =
            networkManager.networkWatchers().define(nwName).withRegion(region).withNewResourceGroup(rgName).create();
        AvailableProviders providers = nw.availableProviders().execute();
        Assertions.assertTrue(providers.providersByCountry().size() > 1);
        Assertions.assertNotNull(providers.providersByCountry().get("United States"));

        providers =
            nw
                .availableProviders()
                .withAzureLocation("West US")
                .withCountry("United States")
                .withState("washington")
                .execute();
        Assertions.assertEquals(1, providers.providersByCountry().size());
        Assertions
            .assertEquals(
                "washington", providers.providersByCountry().get("United States").states().get(0).stateName());
        Assertions
            .assertTrue(providers.providersByCountry().get("United States").states().get(0).providers().size() > 0);

        String localProvider = providers.providersByCountry().get("United States").states().get(0).providers().get(0);
        AzureReachabilityReport report =
            nw
                .azureReachabilityReport()
                .withProviderLocation("United States", "washington")
                .withStartTime(OffsetDateTime.parse("2018-04-10"))
                .withEndTime(OffsetDateTime.parse("2018-04-12"))
                .withProviders(localProvider)
                .withAzureLocations("West US")
                .execute();
        Assertions.assertEquals("State", report.aggregationLevel());
        Assertions.assertTrue(report.reachabilityReport().size() > 0);
    }
}
