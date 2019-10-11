// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.search.common.SearchApiKeyPipelinePolicy;
import com.azure.search.implementation.SearchServiceRestClientBuilder;
import com.azure.search.implementation.SearchServiceRestClientImpl;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.search.SearchService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

public class CreateSearchServiceAndIndexExample {

    /**
     * This is an example of using {@link SearchService} to create Search resource in Azure,
     * and {@link SearchServiceClient} to create Index in Azure Search.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        // Required user input:
        String servicePrincipalAppId = "<service principal app id>";
        String domainId = "<domain id>";
        String servicePrincipalAppSecret = "<service principal app secret>";
        String resourceGroupName = "<resource group name>";
        String searchServiceName = "<search service name>";
        String subscriptionId = "<subscription id>";
        Region region = Region.US_EAST;

        // Creating Azure Search Resource:
        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            servicePrincipalAppId,
            domainId,
            servicePrincipalAppSecret,
            AzureEnvironment.AZURE);

        // Create resource group
        Azure azure = Azure.configure()
            .authenticate(applicationTokenCredentials)
            .withSubscription(subscriptionId);
        System.out.println("Creating Resource Group: " + resourceGroupName);
        ResourceGroup resourceGroup = azure.resourceGroups()
            .define(resourceGroupName)
            .withRegion(region)
            .create();

        // Create search service
        System.out.println("Creating Search Service: " + searchServiceName);
        SearchService searchService = azure.searchServices()
            .define(searchServiceName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFreeSku()
            .create();
        String adminKey = searchService.getAdminKeys().primaryKey();
        System.out.println("Admin key: " + adminKey);

        // Create index
        String indexName = "hotels";
        System.out.println("Creating Index: " + indexName);
        SearchServiceRestClientImpl searchServiceClient = searchServiceClient = new SearchServiceRestClientBuilder()
            .apiVersion("2019-05-06")
            .searchServiceName(searchServiceName)
            .pipeline(
                new HttpPipelineBuilder()
                    .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                    .policies(new SearchApiKeyPipelinePolicy(new ApiKeyCredentials(adminKey)))
                    .build())
            .build();
        Index index = new Index()
            .setName(indexName)
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE),
                new Field()
                    .setName("Tags")
                    .setType(DataType.COLLECTION_EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                )
            );
        searchServiceClient.indexes()
            .createWithRestResponseAsync(index, Context.NONE)
            .block();

        /*
        // Alternatively, create index from json file
        try {
            Reader indexData = new InputStreamReader(CreateSearchServiceAndIndexExample.class.getResourceAsStream("HotelsIndex.json"));
            Index indexFromFile = new ObjectMapper().readValue(indexData, Index.class);
            searchServiceClient.indexes()
                .createWithRestResponseAsync(indexFromFile, Context.NONE)
                .block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
