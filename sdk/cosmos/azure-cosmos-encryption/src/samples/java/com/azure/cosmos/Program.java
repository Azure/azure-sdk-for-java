// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.encryption.AzureKeyVaultCosmosEncryptor;
import com.azure.cosmos.encryption.AzureKeyVaultKeyWrapMetadata;
import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.encryption.EncryptionItemRequestOptions;
import com.azure.cosmos.encryption.EncryptionOptions;
import com.azure.cosmos.encryption.WithEncryption;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Security;
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
    private static final String containerId = "encryptedData";
    private static final String keyContainerId = "keyContainer";
    private static final String dataEncryptionKeyId = "theDataEncryptionKey";
    private static CosmosAsyncClient client = null;
    private static EncryptionCosmosAsyncContainer containerWithEncryption = null;

    public static void main(String[] args) throws Exception {
        try {
            // Read the Cosmos endpointUrl and masterKey from configuration.
            // These values are available from the Azure Management Portal on the Cosmos Account Blade under "Keys".
            // Keep these values in a safe and secure location. Together they provide administrative access to your
            // Cosmos account.
            Properties configuration = args.length > 0 ? loadConfig(args[0]) : loadConfig();
            Program.client = Program.createClientInstance(configuration);
            Program.initialize(client, configuration);
            Program.runDemo(client);
        } catch (CosmosException cre) {
            System.out.println(cre.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("End of demo, press any key to exit.");
            System.in.read();
            Program.cleanup();
            client.close();
        }
    }

    private static CosmosAsyncClient createClientInstance(Properties configuration) {
        String endpoint = configuration.getProperty("CosmosEndpointUrl");
        Preconditions.checkNotNull(endpoint, "Please specify a valid endpoint.");

        String authKey = configuration.getProperty("CosmosMasterKey");
        Preconditions.checkNotNull(endpoint, "Please specify a valid AuthorizationKey.");

        // TODO: moderakh, without contentResponseOnWriteEnabled = true this won't work we get NPE.
        return new CosmosClientBuilder().endpoint(endpoint).key(authKey).contentResponseOnWriteEnabled(true).buildAsyncClient();
    }

    /**
     * Administrative operations - create the database, container, and generate the necessary data encryption keys.
     * These are initializations and are expected to be invoked only once - do not invoke these before every item
     *
     * @param client CosmosAsyncClient to be used
     * @param properties configuration
     * @throws Exception on failure throws
     */
    private static void initialize(CosmosAsyncClient client, Properties properties) throws Exception {
        client.createDatabaseIfNotExists(Program.databaseId).block();
        CosmosAsyncDatabase database = client.getDatabase(Program.databaseId);

        // Delete the existing container to prevent create item conflicts.
        try {
            database.getContainer(Program.containerId).delete().block();
        } catch (Exception e) {
        }

        System.out.println("The demo will create a 1000 RU/s container, press any key to continue.");
        System.in.read();

        // Create a container with the appropriate partition key definition (we choose the "AccountNumber" property
        // here) and throughput (we choose 1000 here).
        database.createContainer(Program.containerId, "/AccountNumber",
            ThroughputProperties.createManualThroughput(1000)).block();

        CosmosAsyncContainer container = database.getContainer(Program.containerId);

        // Application credentials for authentication with Azure Key Vault.
        // This application must have keys/wrapKey and keys/unwrapKey permissions
        // on the keys that will be used for encryption.
        TokenCredential tokenCredentials = Program.getTokenCredential(properties);

        AzureKeyVaultCosmosEncryptor encryptor = new AzureKeyVaultCosmosEncryptor(
            tokenCredentials);
        encryptor.initialize(database, Program.keyContainerId);

        Program.containerWithEncryption = WithEncryption.withEncryptor(container, encryptor);

        // Master key identifier: https://{keyvault-name}.vault.azure
        //.net/{object-type}/{object-name}/{object-version}
        String masterKeyUrlFromConfig = getConfiguration("KeyVaultMasterKeyUrl", properties);
        if (StringUtils.isEmpty(masterKeyUrlFromConfig)) {
            throw new IllegalArgumentException("Please specify a valid MasterKeyUrl in the appSettings.json");
        }

        URI masterKeyUri = new URI(masterKeyUrlFromConfig);

        AzureKeyVaultKeyWrapMetadata wrapMetadata = new AzureKeyVaultKeyWrapMetadata(masterKeyUri.toURL());

        /// Generates an encryption key, wraps it using the key wrap metadata provided
        /// with the key wrapping provider configured on the client
        /// and saves the wrapped encryption key as an asynchronous operation in the Azure Cosmos service.
        encryptor.getDataEncryptionKeyContainer().createDataEncryptionKeyAsync(
            dataEncryptionKeyId,
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED,
            wrapMetadata, new CosmosItemRequestOptions()).block();

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

    private static void runDemo(CosmosAsyncClient client) {
        String orderId = UUID.randomUUID().toString();
        String account = "Account1";
        SalesOrder order = Program.getSalesOrderSample(account, orderId);

        EncryptionItemRequestOptions itemRequestOptions = new EncryptionItemRequestOptions();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setDataEncryptionKeyId(Program.dataEncryptionKeyId);
        encryptionOptions.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED);
        encryptionOptions.setPathsToEncrypt(ImmutableList.of("/TotalDue"));
        itemRequestOptions.setEncryptionOptions(encryptionOptions);

        // Save the sales order into the container - all properties marked with the Encrypt attribute on the
        // SalesOrder class
        // are encrypted using the encryption key referenced below before sending to the Azure Cosmos DB service.
        Program.containerWithEncryption.createItem(
            order,
            new PartitionKey(order.AccountNumber),
            itemRequestOptions).block();

        // Read the item back - decryption happens automatically as the data contains the reference to the wrapped
        // form of the encryption key and
        // metadata in order to unwrap it.
        CosmosItemResponse<SalesOrder> readResponse = Program.containerWithEncryption.readItem(orderId,
            new PartitionKey(account), new CosmosItemRequestOptions(), SalesOrder.class).block();
        SalesOrder readOrder = readResponse.getItem();

        System.out.println("Total due" + order.TotalDue + " After roundtripping: " + readOrder.TotalDue);
        System.out.println("running demo completed.");
    }

    private static String getConfiguration(String key, Properties properties) {
        return properties.getProperty(key);
    }

    private static SalesOrder getSalesOrderSample(String account, String orderId) {
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.id = orderId;
        salesOrder.AccountNumber = account;
        salesOrder.PurchaseOrderNumber = "PO18009186470";
        salesOrder.TotalDue = 985.01;

        // TODO update other fields
        //        {
        //            Id = orderId,
        //                AccountNumber = account,
        //                PurchaseOrderNumber = "PO18009186470",
        //                OrderDate = new DateTime(2005, 7, 1),
        //                SubTotal = 419.4589m,
        //            TaxAmount = 12.5838m,
        //            Freight = 472.3108m,
        //            TotalDue = 985.018m,
        //            Items = new SalesOrderDetail[]
        //                {
        //                    new SalesOrderDetail
        //                    {
        //                        OrderQty = 1,
        //                        ProductId = 760,
        //                        UnitPrice = 419.4589m,
        //                        LineTotal = 419.4589m
        //                    }
        //                },
        //        };

        // Set the "ttl" property to auto-expire sales orders in 30 days
        salesOrder.TimeToLive = 60 * 60 * 24 * 30;

        return salesOrder;
    }

    private static void cleanup() {
        try {
            client.getDatabase(databaseId).delete().block();
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
        try (InputStream input = Program.class.getClassLoader().getResourceAsStream("settings.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }
}



