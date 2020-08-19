// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class AadAuthorizationTests extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(AadAuthorizationTests.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    protected AadAuthorizationTests() {
        super();
    }

    private static Properties properties = System.getProperties();

    private final static String EMULATOR_KEY = "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";
    private final static String HOST = "https://localhost:8081/";

    private final static String PARTITION_KEY_PATH_1 = "/mypk";
    private final String databaseId = "dbAad" + RandomStringUtils.randomAlphabetic(6);;


    @Test(groups = { "emulator" }, timeOut = 200 * TIMEOUT)
    public void createAadTokenCredential() throws InterruptedException {
        TokenCredential emulatorCredential = new AadSimpleEmulatorTokenCredential(EMULATOR_KEY);

        CosmosAsyncDatabase db = null;
        CosmosAsyncClient cosmosAsyncClient;

        cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(HOST)
            .credential(emulatorCredential)
            .gatewayMode()
            .buildAsyncClient();

        try {
            CosmosDatabaseResponse databaseResponse = cosmosAsyncClient.createDatabase(databaseId).block();

            db = cosmosAsyncClient.getDatabase(databaseId).read()
                .map(dabaseResponse -> {
                    CosmosAsyncDatabase database = cosmosAsyncClient.getDatabase(dabaseResponse.getProperties().getId());
                    log.info("Found database {} with {}", database.getId(), dabaseResponse.getProperties().getETag());
                    return database;
                }).block();

            // CREATE collection
            assert db != null;
            String containerName = UUID.randomUUID().toString();
            CosmosContainerResponse containerResponse = db.createContainer(containerName, PARTITION_KEY_PATH_1).block();

            CosmosAsyncContainer container = db.getContainer(containerName).read()
                .map(cosmosContainerResponse -> {
                    CosmosAsyncContainer container1 = cosmosAsyncClient.getDatabase(databaseId).getContainer(cosmosContainerResponse.getProperties().getId());
                    log.info("Found container {} with {}", container1.getId(), cosmosContainerResponse.getProperties().getETag());
                    return container1;
                }).block();

            // CREATE document
            assert container != null;
            String itemName = UUID.randomUUID().toString();
            String partitionKeyValue = UUID.randomUUID().toString();
            InternalObjectNode properties = getDocumentDefinition(itemName, PARTITION_KEY_PATH_1);

            CosmosItemResponse<InternalObjectNode> cosmosItemResponse = container.createItem(properties, new CosmosItemRequestOptions()).block();

            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            InternalObjectNode item = container
                .readItem(itemName, new PartitionKey(PARTITION_KEY_PATH_1), options, InternalObjectNode.class)
                .map(CosmosItemResponse::getItem).block();

            assert item != null;
        } finally {
            if (db != null) {
                safeDeleteDatabase(db);
            }

            if (cosmosAsyncClient != null) {
                safeClose(cosmosAsyncClient);
            }
        }

        Thread.sleep(10000);
    }

    private InternalObjectNode getDocumentDefinition(String itemId, String partitionKeyValue) {
        final InternalObjectNode properties = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , itemId, partitionKeyValue));
        return properties;
    }

    @Factory(dataProvider = "clientBuilders")
    public AadAuthorizationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeMethod(groups = { "emulator" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
    public void beforeMethod() {
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorTest() {
    }

    @AfterMethod(groups = { "emulator" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
    }

    @AfterClass(groups = { "emulator" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
    }

    static String getPropertyValueOrEnvironmentOrDefault(String propertyName, String defaultValue) {
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            propertyValue = System.getenv().get(propertyName);
        }
        return propertyValue == null || propertyValue.isEmpty() ? defaultValue : propertyValue;
    }

    class AadSimpleEmulatorTokenCredential implements TokenCredential {
        private final String emulatorKeyEncoded;
        private final String AAD_HEADER_COSMOS_EMULATOR = "{\"typ\":\"JWT\",\"alg\":\"RS256\",\"x5t\":\"CosmosEmulatorPrimaryMaster\",\"kid\":\"CosmosEmulatorPrimaryMaster\"}";
        private final String AAD_CLAIM_COSMOS_EMULATOR_FORMAT = "{\"aud\":\"https://localhost.localhost\",\"iss\":\"https://sts.fake-issuer.net/7b1999a1-dfd7-440e-8204-00170979b984\",\"iat\":%d,\"nbf\":%d,\"exp\":%d,\"aio\":\"\",\"appid\":\"localhost\",\"appidacr\":\"1\",\"idp\":\"https://localhost:8081/\",\"oid\":\"96313034-4739-43cb-93cd-74193adbe5b6\",\"rh\":\"\",\"sub\":\"localhost\",\"tid\":\"EmulatorFederation\",\"uti\":\"\",\"ver\":\"1.0\",\"scp\":\"user_impersonation\",\"groups\":[\"7ce1d003-4cb3-4879-b7c5-74062a35c66e\",\"e99ff30c-c229-4c67-ab29-30a6aebc3e58\",\"5549bb62-c77b-4305-bda9-9ec66b85d9e4\",\"c44fd685-5c58-452c-aaf7-13ce75184f65\",\"be895215-eab5-43b7-9536-9ef8fe130330\"]}";

        public AadSimpleEmulatorTokenCredential(String emulatorKey) {
            if (emulatorKey == null || emulatorKey.isEmpty()) {
                throw new IllegalArgumentException("emulatorKey");
            }

            this.emulatorKeyEncoded = Utils.encodeUrlBase64String(emulatorKey.getBytes());
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            String aadToken = emulatorKey_based_AAD_String();
            return Mono.just(new AccessToken(aadToken, OffsetDateTime.now().plusHours(2)));
        }

        String emulatorKey_based_AAD_String() {
            ZonedDateTime currentTime = ZonedDateTime.now();
            String part1Encoded = Utils.encodeUrlBase64String(AAD_HEADER_COSMOS_EMULATOR.getBytes());
            String part2 = String.format(AAD_CLAIM_COSMOS_EMULATOR_FORMAT,
                currentTime.toEpochSecond(),
                currentTime.toEpochSecond(),
                currentTime.plusHours(2).toEpochSecond());
            String part2Encoded = Utils.encodeUrlBase64String(part2.getBytes());
            return part1Encoded + "." + part2Encoded + "." + this.emulatorKeyEncoded;
        }
    }
}
