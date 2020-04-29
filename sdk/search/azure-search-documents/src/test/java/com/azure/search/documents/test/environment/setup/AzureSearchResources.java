// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.setup;

import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.search.SearchService;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;

import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.fail;

public class AzureSearchResources {
    private static final String RESOURCE_GROUP_NAME_PREFIX = "azsjava";
    private static final String SEARCH_SERVICE_NAME_PREFIX = "azsjava";
    private static final String BLOB_DATASOURCE_NAME_PREFIX = "azsjavablob";
    private static final String STORAGE_NAME_PREFIX = "azsjavastor";
    private static final String AZURE_RESOURCEGROUP_NAME = "AZURE_RESOURCEGROUP_NAME";
    private static final char[] ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final String TEST_RESOURCE_GROUP = "azsjavaresourcegroup";
    private static final String DEFAULT_DNS_SUFFIX = "search.windows.net";
    private static final String DOGFOOD_DNS_SUFFIX = "search-dogfood.windows-int.net";

    private String searchServiceName;
    private String searchAdminKey;
    private String searchDnsSuffix;
    private String endpoint;

    private AzureTokenCredentials azureTokenCredentials;
    private String subscriptionId;
    private Region location;

    private Azure azure = null;
    private static ResourceGroup resourceGroup;
    private SearchService searchService = null;
    private static String testEnvironment = null;


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
     * Creates an instance of AzureTokenCredentials to be used in creating a Resource Group and Azure Cognitive Search
     * service in Azure to be used for tests.
     *
     * @param azureTokenCredentials includes credentials to connect to Azure.
     * @param subscriptionId Azure subscription id.
     * @param location location of the resources to be created in.
     */
    public AzureSearchResources(AzureTokenCredentials azureTokenCredentials, String subscriptionId, Region location) {
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

        initializeAzureResources();
        if (azure == null) {
            azure = Azure.configure()
                .authenticate(azureTokenCredentials)
                .withSubscription(subscriptionId);
        }
        searchDnsSuffix = testEnvironment.equals("DOGFOOD") ? DOGFOOD_DNS_SUFFIX : DEFAULT_DNS_SUFFIX;
        endpoint = String.format("https://%s.%s", searchServiceName, searchDnsSuffix);
    }

    private void validate() {
        Objects.requireNonNull(this.azureTokenCredentials, "azureTokenCredentials cannot be null");
        Objects.requireNonNull(this.location, "location cannot be null");
        if (CoreUtils.isNullOrEmpty(this.subscriptionId)) {
            throw new IllegalArgumentException("subscriptionId cannot be blank");
        }
    }

    /**
     * Creates an Azure Service in an existing resource group
     */
    public void createService(TestResourceNamer testResourceNamer) {
        searchServiceName = testResourceNamer.randomName(SEARCH_SERVICE_NAME_PREFIX, 60);
        System.out.println("Creating Azure Cognitive Search service: " + searchServiceName);
        int recreateCount = 0;
        do {
            try {
                searchService = azure.searchServices()
                    .define(searchServiceName)
                    .withRegion(location)
                    .withExistingResourceGroup(resourceGroup)
                    .withFreeSku()
                    .create();
                recreateCount += 1;
            } catch (CloudException ex) {
                if (ex.getMessage().contains("already exists")) {
                    System.out.println(String.format("Azure Cognitive Search service %s has already been created. ",
                        searchServiceName));
                    break;
                }
            }
        } while (recreateCount < 3 && shouldRetryCreateService());
        if (recreateCount == 3 && shouldRetryCreateService()) {
            fail("Failed to create service");
        }
        searchAdminKey = searchService.getAdminKeys().primaryKey();
    }

    public String getEndpoint() {
        searchDnsSuffix = testEnvironment.equals("DOGFOOD") ? DOGFOOD_DNS_SUFFIX : DEFAULT_DNS_SUFFIX;
        return String.format("https://%s.%s", searchServiceName, searchDnsSuffix);
    }

    private boolean shouldRetryCreateService() {
        String pingAddress = getEndpoint().replaceFirst("https://", "");
        int retryCount = 0;
        boolean shouldRetry = true;
        while (shouldRetry && retryCount < 3) {
            try {
                InetAddress.getByName(pingAddress);
                System.out.println("Sending Ping Request to " + pingAddress);
                return false;
            } catch (IOException ex) {
                System.out.println(String.format("Sorry ! We can't reach to this host: %s.",
                    pingAddress));
                sleep();
            }
            retryCount += 1;
        }
        return true;
    }

    private void sleep() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        String resourceGroupName = Configuration.getGlobalConfiguration().get(AZURE_RESOURCEGROUP_NAME,
            TEST_RESOURCE_GROUP);

        if (azure.resourceGroups().checkExistence(resourceGroupName)) {
            System.out.println("Fetching Resource Group: " + resourceGroupName);
            resourceGroup = azure.resourceGroups()
                .getByName(resourceGroupName);
        } else {
            String deleteTime = OffsetDateTime.now().plusHours(12).format(DateTimeFormatter.ISO_INSTANT);
            System.out.println("Creating Resource Group: " + resourceGroupName);
            resourceGroup = azure.resourceGroups()
                .define(resourceGroupName)
                .withRegion(location)
                .withTag("DeleteAfter", deleteTime)
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
     *
     * @return the storage connection string
     */
    public String createStorageAccount(TestResourceNamer testResourceNamer) {
        String storageName = testResourceNamer.randomName(STORAGE_NAME_PREFIX, 15);

        StorageAccount storageAccount = azure.storageAccounts().define(storageName)
            .withRegion(location)
            .withExistingResourceGroup(resourceGroup.name())
            .create();

        // only one item and we get its key
        StorageAccountKey key = storageAccount.getKeys().get(0);

        // Currently this only works on PROD Azure and not on Dogfood

        return "DefaultEndpointsProtocol=https;AccountName="
            + storageName + ";"
            + "AccountKey="
            + key.value()
            + ";EndpointSuffix=core.windows.net";
    }

    /**
     * Create a blob container inside a given storage account
     *
     * @param storageConnString a given connection string
     * @return the created container name
     */
    public String createBlobContainer(String storageConnString, TestResourceNamer testResourceNamer) {

        // now we create a blob container, no need for an actual blob to be uploaded
        String blobContainerDatasourceName = testResourceNamer.randomName(BLOB_DATASOURCE_NAME_PREFIX, 15);

        BlobServiceClient blobServiceClient =
            new BlobServiceClientBuilder()
                .connectionString(storageConnString)
                .buildClient();
        blobServiceClient.createBlobContainer(blobContainerDatasourceName);

        return blobContainerDatasourceName;
    }

    private static String randomResourceGroupName() {
        StringBuilder builder = new StringBuilder(RESOURCE_GROUP_NAME_PREFIX);
        SecureRandom random = new SecureRandom();

        while (builder.length() < 18) {
            int index = (int) (random.nextFloat() * ALLOWED_CHARS.length);
            builder.append(ALLOWED_CHARS[index]);
        }

        return builder.toString();
    }


    public static AzureSearchResources initializeAzureResources() {
        String appId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String azureDomainId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String secret = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);

        testEnvironment = Configuration.getGlobalConfiguration().get("AZURE_TEST_ENVIRONMENT");
        testEnvironment = (testEnvironment == null) ? "AZURE" : testEnvironment.toUpperCase(Locale.US);

        AzureEnvironment environment = testEnvironment.equals("DOGFOOD") ? getDogfoodEnvironment() : AzureEnvironment.AZURE;

        ApplicationTokenCredentials applicationTokenCredentials =
            new ApplicationTokenCredentials(appId, azureDomainId, secret, environment);

        return new AzureSearchResources(applicationTokenCredentials, subscriptionId, Region.US_WEST2);
    }

    private static AzureEnvironment getDogfoodEnvironment() {
        HashMap<String, String> configuration = new HashMap<>();
        configuration.put("portalUrl", "http://df.onecloud.azure-test.net");
        configuration.put("managementEndpointUrl", "https://management.core.windows.net/");
        configuration.put("resourceManagerEndpointUrl", "https://api-dogfood.resources.windows-int.net/");
        configuration.put("activeDirectoryEndpointUrl", "https://login.windows-ppe.net/");
        configuration.put("activeDirectoryResourceId", "https://management.core.windows.net/");
        configuration.put("activeDirectoryGraphResourceId", "https://graph.ppe.windows.net/");
        configuration.put("activeDirectoryGraphApiVersion", "2013-04-05");
        return new AzureEnvironment(configuration);
    }

}
