// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.search.models.CheckNameAvailabilityOutput;
import com.azure.resourcemanager.search.models.SearchService;
import com.azure.resourcemanager.search.models.SkuName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchServiceOperationTests extends SearchManagementTest {

    private String rgName = "";
    private final Region region = Region.US_WEST;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateUpdateSearchService() {
        String searchServiceName = generateRandomResourceName("search", 15);

        resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityOutput result = searchManager.searchServices().checkNameAvailability(searchServiceName);
        Assertions.assertTrue(result.isNameAvailable());

        SearchService searchService = searchManager.searchServices().define(searchServiceName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withFreeSku()
            .create();

        Assertions.assertNotNull(searchService);
        Assertions.assertEquals(SkuName.FREE, searchService.sku().name());
        Assertions.assertEquals(0, searchService.tags().size());

        boolean foundSearchService = false;
        for (SearchService service : searchManager.searchServices().list()) {
            if (searchServiceName.equals(service.name())) {
                foundSearchService = true;
                break;
            }
        }
        Assertions.assertTrue(foundSearchService);
        
        if (!isPlaybackMode()) {
            searchService.update()
                .withTag("key1", "value1")
                .apply();

            SearchService updatedSearchService = searchManager.searchServices().getByResourceGroup(rgName, searchServiceName);
            Assertions.assertNotNull(updatedSearchService);
            Assertions.assertEquals(SkuName.FREE, updatedSearchService.sku().name());
            Assertions.assertEquals(1, updatedSearchService.tags().size());
            Assertions.assertEquals("value1", updatedSearchService.tags().get("key1"));
        }
    }
}
