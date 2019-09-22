// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test.environment.setup;

import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.search.SearchService;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class AzureSearchResources {
    private static final String RESOURCE_GROUP_NAME_PREFIX = "azs-sdk";
    private static final String SEARCH_SERVICE_NAME_PREFIX = "azs-sdk";

    private String resourceGroupName;
    private String searchServiceName;
    private String searchAdminKey;
    private String indexFileName;

    private AzureTokenCredentials azureTokenCredentials;
    private String subscriptionId;
    private Region location;

    private Azure azure = null;
    private ResourceGroup resourceGroup = null;
    private SearchService searchService = null;

    /**
     *
     * @return The created Resource Group name
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    /**
     *
     * @return
     */
    public String getIndexFileName() {
        return indexFileName;
    }

    /**
     *
     * @return The created Search service name
     */
    public String getSearchServiceName() {
        return searchServiceName;
    }

    /**
     *
     * @return The Search service admin key
     */
    public String getSearchAdminKey() {
        return searchAdminKey;
    }

    /**
     * Creates an instance of AzureTokenCredentials to be used in creating a Resource Group and Search service
     * in Azure to be used for tests.
     *
     * @param azureTokenCredentials includes credentials to connect to Azure.
     * @param subscriptionId        Azure subscription id.
     * @param location              location of the resources to be created in.
     */
    public AzureSearchResources(
        AzureTokenCredentials azureTokenCredentials, String subscriptionId,
        Region location) {
        this.azureTokenCredentials = azureTokenCredentials;
        this.subscriptionId = subscriptionId;
        this.location = location;
    }

    /**
     * Creates a Resource Group and Search Service in Azure, and updates their names in class variables
     * to be retrieved later.
     */
    public void initialize() {
        validate();
        if (azure == null) {
            azure = Azure.configure()
                .authenticate(azureTokenCredentials)
                .withSubscription(subscriptionId);
        }

        if (resourceGroup == null) {
            resourceGroupName = SdkContext.randomResourceName(RESOURCE_GROUP_NAME_PREFIX, 24);
            System.out.println("Creating Resource Group: " + resourceGroupName);
            resourceGroup = azure.resourceGroups()
                .define(resourceGroupName)
                .withRegion(location)
                .create();
        }

        if (searchService == null) {
            searchServiceName = SdkContext.randomResourceName(SEARCH_SERVICE_NAME_PREFIX, 24);
            System.out.println("Creating Search Service: " + searchServiceName);
            searchService = azure.searchServices()
                .define(searchServiceName)
                .withRegion(location)
                .withExistingResourceGroup(resourceGroup)
                .withFreeSku()
                .create();
        }

        searchAdminKey = searchService.getAdminKeys().primaryKey();
    }

    private void validate() {
        Objects.requireNonNull(this.azureTokenCredentials, "azureTokenCredentials cannot be null");
        Objects.requireNonNull(this.location, "location cannot be null");
        if (StringUtils.isBlank(this.subscriptionId)) {
            throw new IllegalArgumentException("subscriptionId cannot be blank");
        }
    }

    /**
     * Deletes the created resources in Azure. This should be run after finishing all tests.
     */
    public void cleanup() {
        if (searchService != null) {
            System.out.println("Deleting Search Service: " + searchService.name());
            azure.searchServices().deleteById(searchService.id());
        }
        if (resourceGroup != null) {
            System.out.println("Deleting Resource Group: " + resourceGroup.name());
            azure.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }
}


