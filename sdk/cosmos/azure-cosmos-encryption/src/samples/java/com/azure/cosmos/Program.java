// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncDatabase;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.data.encryption.AzureKeyVaultKeyStoreProvider.AzureKeyVaultKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

// ----------------------------------------------------------------------------------------------------------
// Prerequisites -
//
// 1. An Azure Cosmos account -
//    https://docs.microsoft.com/en-us/azure/cosmos-db/create-cosmosdb-resources-portal
//
// 2. Azure Key Vault setup.
// ----------------------------------------------------------------------------------------------------------
// Sample - demonstrates the basic usage of client-side encryption support in the Cosmos DB SDK.
// ----------------------------------------------------------------------------------------------------------
public class Program {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String databaseId = "samples";
    private static final String containerId = "encryptedContainer";
    private static final String dataEncryptionKeyId = "theDataEncryptionKey";
    private static CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = null;
    private static CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase = null;
    private static CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer = null;
    private static AzureKeyVaultKeyStoreProvider encryptionKeyStoreProvider = null;

    public static void main(String[] args) throws Exception {
        try {
            //NOTE: Please provide credential information in src/samples/resources/settings.properties
            Properties configuration = args.length > 0 ? loadConfig(args[0]) : loadConfig();
            Program.cosmosEncryptionAsyncClient = Program.createClientInstance(configuration);
            Program.initialize(cosmosEncryptionAsyncClient, configuration);
            Program.runDemo(cosmosEncryptionAsyncContainer);
        } catch (CosmosException cre) {
            System.out.println(cre.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("End of demo, press enter key to exit.");
            System.in.read();
            Program.cleanup();
            cosmosEncryptionAsyncClient.close();
        }
    }

    private static CosmosEncryptionAsyncClient createClientInstance(Properties configuration) throws MicrosoftDataEncryptionException {
        String endpoint = configuration.getProperty("CosmosEndpointUrl");
        Preconditions.checkNotNull(endpoint, "Please specify a valid endpoint.");

        String authKey = configuration.getProperty("CosmosMasterKey");
        Preconditions.checkNotNull(endpoint, "Please specify a valid AuthorizationKey.");

        // TODO: moderakh, without contentResponseOnWriteEnabled = true this won't work we get NPE.
        CosmosAsyncClient asyncClient =
            new CosmosClientBuilder().endpoint(endpoint).key(authKey).contentResponseOnWriteEnabled(true).buildAsyncClient();

        // Application credentials for authentication with Azure Key Vault.
        // This application must have keys/wrapKey and keys/unwrapKey permissions
        // on the keys that will be used for encryption.
        TokenCredential tokenCredentials = Program.getTokenCredential(configuration);
        encryptionKeyStoreProvider = new AzureKeyVaultKeyStoreProvider(tokenCredentials);

        return CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(asyncClient, encryptionKeyStoreProvider);
    }

    /**
     * Administrative operations - create the database, container, and generate the necessary data encryption keys.
     * These are initializations and are expected to be invoked only once - do not invoke these before every item
     *
     * @param properties configuration
     * @throws Exception on failure throws
     */
    private static void initialize(CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient, Properties properties) throws Exception {
        cosmosEncryptionAsyncClient.getCosmosAsyncClient().createDatabaseIfNotExists(Program.databaseId).block();
        cosmosEncryptionAsyncDatabase = cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(Program.databaseId);

        // Delete the existing container to prevent create item conflicts.
        try {
            cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getContainer(Program.containerId).delete().block();
        } catch (Exception e) {
        }


        System.out.println("The demo will create data encryption key from key vault key, press enter key to continue.");
        System.in.read();


        // Master key identifier: https://{keyvault-name}.vault.azure
        //.net/{object-type}/{object-name}/{object-version}
        String masterKeyUrlFromConfig = getConfiguration("KeyVaultMasterKeyUrl", properties);
        if (StringUtils.isEmpty(masterKeyUrlFromConfig)) {
            throw new IllegalArgumentException("Please specify a valid MasterKeyUrl in the appSettings.json");
        }

        EncryptionKeyWrapMetadata metadata = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), dataEncryptionKeyId, masterKeyUrlFromConfig);

        /// Generates an encryption key, wraps it using the key wrap metadata provided
        /// and saves the wrapped encryption key as an asynchronous operation in the Azure Cosmos service.
        CosmosClientEncryptionKeyProperties keyProperties = cosmosEncryptionAsyncDatabase.createClientEncryptionKey(
            dataEncryptionKeyId,
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256, metadata).block().getProperties();

        System.out.println("The demo will create a 1000 RU/s container, with encryption policy on " +
            "account number, press enter key to continue.");
        System.in.read();

        ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
        includedPath.setClientEncryptionKeyId(dataEncryptionKeyId);
        includedPath.setPath("/accountNumber");
        includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);
        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath);

        // Create a container with the appropriate partition key definition (we choose the "PurchaseOrderNumber"
        // property
        // here) and throughput (we choose 1000 here).

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(Program.containerId,
            "/purchaseOrderNumber");
        containerProperties.setClientEncryptionPolicy(new ClientEncryptionPolicy(paths));

        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(1000)).block();
        cosmosEncryptionAsyncContainer =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerProperties.getId());

        System.out.println("Initialization completed.");
    }

    private static TokenCredential getTokenCredential(Properties properties) {
        String clientId = getConfiguration("ClientId", properties);
        if (StringUtils.isEmpty(clientId)) {
            throw new IllegalArgumentException("Please specify a valid ClientId in the appSettings.json");
        }

        // TODO: add sample for certificate base token credentials
//        // Certificate's public key must be at least 2048 bits.
//        String clientCertThumbprint = getConfiguration("ClientCertThumbprint", properties);
//        if (StringUtils.isEmpty(clientCertThumbprint)) {
//            throw new IllegalArgumentException("Please specify a valid ClientCertThumbprint in the appSettings.json");
//        }

        // Get the Tenant ID
        String tenantId = getConfiguration("TenantId", properties);
        if (StringUtils.isEmpty(tenantId)) {
            throw new IllegalArgumentException("Please specify a valid Tenant Id in the appSettings.json");
        }

        String clientSecret = getConfiguration("ClientSecret", properties);
        if (StringUtils.isEmpty(tenantId)) {
            throw new IllegalArgumentException("Please specify a valid ClientSecret in the appSettings.json");
        }

        ClientSecretCredential credentials = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();

        // TODO: implement certificate base sample
        return credentials;
    }

    private static void runDemo(CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer) throws IOException {
        String orderId = UUID.randomUUID().toString();
        String account = "Account1";
        SalesOrder order = Program.getSalesOrderSample(account, orderId);

        System.out.println("Account number for SalesOrder going to get saved encrypted in container");
        // Save the sales order into the container - all properties mentioned in client encryption policy on the
        // container are encrypted using the encryption key saved in the database before sending to the Azure Cosmos
        // DB service.
        cosmosEncryptionAsyncContainer.createItem(order,
            new PartitionKey(order.purchaseOrderNumber),
            new CosmosItemRequestOptions()).block();

        System.out.println("Sale order is created in database, account number = " + order.accountNumber);

        // Read the item back - decryption happens automatically as the container contains the reference to the wrapped
        // form of the encryption key and
        // metadata in order to unwrap it.
        System.out.println("For reading the created item, press enter key to continue.");
        System.in.read();
        CosmosItemResponse<SalesOrder> readResponse = cosmosEncryptionAsyncContainer.readItem(orderId,
            new PartitionKey(order.purchaseOrderNumber), new CosmosItemRequestOptions(), SalesOrder.class).block();
        SalesOrder readOrder = readResponse.getItem();
        System.out.println("Sale order is retrieved from readItem api, account number = " + readOrder.accountNumber);

        System.out.println("For querying on encrypted accountNumber, press enter key to continue.");
        //System.in.read();
        System.in.read();
        SqlParameter parameter = new SqlParameter("@accountNumber", "Account1");
        String query = "SELECT * FROM c where c.accountNumber = @accountNumber";
        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(new SqlQuerySpec(query));
        sqlQuerySpecWithEncryption.addEncryptionParameter("/accountNumber", parameter);
        FeedResponse<SalesOrder> salesOrderFeedResponse =
            cosmosEncryptionAsyncContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                new CosmosQueryRequestOptions(), SalesOrder.class).byPage().blockFirst();
        if (salesOrderFeedResponse.getResults().size() < 1) {
            throw new IllegalStateException("Error while querying on encrpted field");
        }
        System.out.println("Sale order is retrieved from query on account number = " + salesOrderFeedResponse.getResults().get(0).accountNumber);

        System.out.println("running demo completed.");
    }

    private static String getConfiguration(String key, Properties properties) {
        return properties.getProperty(key);
    }

    private static SalesOrder getSalesOrderSample(String account, String orderId) {
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.id = orderId;
        salesOrder.accountNumber = account;
        salesOrder.purchaseOrderNumber = "PO18009186470";
        salesOrder.totalDue = 985.01;
        salesOrder.subTotal = 985.01;
        salesOrder.orderDate = Instant.now();
        salesOrder.shippedDate = salesOrder.orderDate.plus(Duration.ofDays(5));
        salesOrder.freight = 472.3108;
        salesOrder.taxAmount = 12.5838;
        salesOrder.items = new SalesOrder.SalesOrderDetail[1];
        salesOrder.items[0] = new SalesOrder.SalesOrderDetail();
        salesOrder.items[0].lineTotal = 419.4589;
        salesOrder.items[0].unitPrice = 419.4589;
        salesOrder.items[0].productId = 760;
        salesOrder.items[0].orderQty = 1;
        salesOrder.timeToLive = 60 * 60 * 24 * 30;

        return salesOrder;
    }

    private static void cleanup() {
        try {
            cosmosEncryptionAsyncClient.getCosmosAsyncClient().getDatabase(databaseId).delete().block();
        } catch (Exception e) {
        }
    }

    // load config from the given file path
    private static Properties loadConfig(String path) throws IOException {
        try (InputStream input = new FileInputStream(path)) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }

    // load config from resource samples/resoruces/settings.properties
    private static Properties loadConfig() throws IOException {

        try (InputStream input = new FileInputStream("src/samples/resources/settings.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }
}




