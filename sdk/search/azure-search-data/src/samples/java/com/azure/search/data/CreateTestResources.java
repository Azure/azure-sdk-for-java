// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data;

import com.azure.search.test.environment.setup.AzureSearchResources;
import com.azure.search.test.environment.setup.SearchIndexDocs;
import com.azure.search.test.environment.setup.SearchIndexService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;

public class CreateTestResources {

    /**
     * This is an example of using {@link AzureSearchResources} to create Search resource in Azure,
     * and {@link SearchIndexService} to create Index in Search Azure.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        // Creating Azure Search Resource:
        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            "app-id",
            "domain-id",
            "secret",
            AzureEnvironment.AZURE);

        String subscriptionId = "subscription-id";
        Region location = Region.US_EAST;

        // Use helper tool to provision Azure resources.
        AzureSearchResources azureSearchResources = new AzureSearchResources(
            applicationTokenCredentials, subscriptionId, location);
        azureSearchResources.initialize();

        String serviceName = azureSearchResources.getSearchServiceName();
        String apiAdminKey = azureSearchResources.getSearchAdminKey();
        String indexFileName = "index-file-name";

        //Creating Index:
        SearchIndexService searchIndexService;
        try {
            searchIndexService = new SearchIndexService(indexFileName, serviceName, apiAdminKey);
            searchIndexService.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Uploading Documents:
        try {
            SearchIndexDocs searchIndexDocs = new SearchIndexDocs(serviceName, apiAdminKey,
                searchIndexService.indexName(),
                "search.windows.net",
                "2019-05-06");
            searchIndexDocs.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Waiting 100 secs before cleaning the created Azure Search resource");
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
