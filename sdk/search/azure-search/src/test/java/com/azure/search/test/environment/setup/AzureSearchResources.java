// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test.environment.setup;

import com.azure.search.models.DataSource;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.search.SearchService;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class AzureSearchResources {
    private static final String RESOURCE_GROUP_NAME_PREFIX = "azs-sdk";
    private static final String SEARCH_SERVICE_NAME_PREFIX = "azs-sdk";
    private static final String BLOB_DATASOURCE_NAME_PREFIX = "azsblob";
    private static final String STORAGE_NAME_PREFIX = "azsstor";


    private String resourceGroupName;
    private String searchServiceName;
    private String searchAdminKey;
    private String blobContainerDatasourceName;
    private String storageName;

    private AzureTokenCredentials azureTokenCredentials;
    private String subscriptionId;
    private Region location;

    private Azure azure = null;
    private ResourceGroup resourceGroup = null;
    private SearchService searchService = null;
    private DataSource blobDatasource = null;

    /**
     * @return The created Azure Cognitive Search service name
     */
    public String getSearchServiceName() {
        return searchServiceName;
    }

    /**
     * @return The Azure Cognitive Search service admin key
     */
    public String getSearchAdminKey() {
        return searchAdminKey;
    }

    /**
     * Creates an instance of AzureTokenCredentials to be used in creating a Resource Group and Azure Cognitive Search service
     * in Azure to be used for tests.
     *
     * @param azureTokenCredentials includes credentials to connect to Azure.
     * @param subscriptionId Azure subscription id.
     * @param location location of the resources to be created in.
     */
    public AzureSearchResources(
        AzureTokenCredentials azureTokenCredentials, String subscriptionId,
        Region location) {
        this.azureTokenCredentials = azureTokenCredentials;
        this.subscriptionId = subscriptionId;
        this.location = location;
    }

    /**
     * Creates a Resource Group and Azure Cognitive Search Service in Azure, and updates their names in class variables
     * to be retrieved later.
     */
    public void initialize() {
        validate();
        if (azure == null) {
            azure = Azure.configure()
                .authenticate(azureTokenCredentials)
                .withSubscription(subscriptionId);
        }
    }

    private void validate() {
        Objects.requireNonNull(this.azureTokenCredentials, "azureTokenCredentials cannot be null");
        Objects.requireNonNull(this.location, "location cannot be null");
        if (StringUtils.isBlank(this.subscriptionId)) {
            throw new IllegalArgumentException("subscriptionId cannot be blank");
        }
    }

    /**
     * Creates an Azure Service in an existing resource group
     */
    public void createService() {
        searchServiceName = SdkContext.randomResourceName(SEARCH_SERVICE_NAME_PREFIX, 24);
        System.out.println("Creating Azure Cognitive Search service: " + searchServiceName);
        searchService = azure.searchServices()
            .define(searchServiceName)
            .withRegion(location)
            .withExistingResourceGroup(resourceGroup)
            .withFreeSku()
            .create();

        searchAdminKey = searchService.getAdminKeys().primaryKey();
    }

    /**
     * Deletes the Azure Cognitive Search service.
     */
    public void deleteService() {
        if (searchService != null) {
            System.out.println("Deleting Azure Cognitive Search service: " + searchService.name());
            azure.searchServices().deleteById(searchService.id());
        }
    }

    /**
     * Creates the Resource Group in Azure. This should be run at @BeforeAll
     */
    public void createResourceGroup() {
        if (resourceGroup == null) {
            resourceGroupName = SdkContext.randomResourceName(RESOURCE_GROUP_NAME_PREFIX, 24);
            System.out.println("Creating Resource Group: " + resourceGroupName);
            resourceGroup = azure.resourceGroups()
                .define(resourceGroupName)
                .withRegion(location)
                .create();
        }
    }

    /**
     * Deletes the Resource Group in Azure. This should be run at @AfterAll
     */
    public void deleteResourceGroup() {
        if (resourceGroup != null) {
            System.out.println("Deleting Resource Group: " + resourceGroup.name());
            azure.resourceGroups().beginDeleteByName(resourceGroup.name());
            resourceGroup = null;
        }
    }

    /**
     * Create a new storage account
     * @return the storage connection string
     */
    public String createStorageAccount() {
        storageName = SdkContext.randomResourceName(STORAGE_NAME_PREFIX, 15);

        StorageAccount storageAccount = azure.storageAccounts().define(storageName)
            .withRegion(location)
            .withExistingResourceGroup(resourceGroup.name())
            .create();

        // only one item and we get its key
        StorageAccountKey key = storageAccount.getKeys().get(0);

        // Currently this only works on PROD Azure and not on Dogfood
        String storageConnString =
            "DefaultEndpointsProtocol=https;AccountName="
                + storageName + ";"
                + "AccountKey="
                + key.value()
                + ";EndpointSuffix=core.windows.net";

        return storageConnString;
    }

    /**
     * Create a blob container inside a given storage account
     * @param storageConnString a given connection string
     * @return the created container name
     */
    public String createBlobContainer(String storageConnString) {

        // now we create a blob container, no need for an actual blob to be uploaded
        blobContainerDatasourceName =
            SdkContext.randomResourceName(BLOB_DATASOURCE_NAME_PREFIX, 15);

        BlobServiceClient blobServiceClient =
            new BlobServiceClientBuilder()
                .connectionString(storageConnString)
                .buildClient();
        blobServiceClient.createBlobContainer(blobContainerDatasourceName);

        return blobContainerDatasourceName;
    }
}
