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
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class AadAuthorizationTests extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(AadAuthorizationTests.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private final static String PARTITION_KEY_PATH = "/mypk";
    private final String databaseId = CosmosDatabaseForTest.generateId();

    protected AadAuthorizationTests() {
    }

    // Cosmos public emulator only test; this test will fail if run against Azure Cosmos endpoint at this time.
    //   We customize the Aad token to be specifically constructed for the Cosmos public emulator only; for Azure Cosmos
    //   the token will be requested and generated from an Azure Identity service.
    @Test(groups = { "emulator" }, timeOut = 10 * TIMEOUT)
    public void createAadTokenCredential() throws InterruptedException {
        CosmosAsyncDatabase db = null;

        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST) // Cosmos public emulator endpoint
            .key(TestConfigurations.MASTER_KEY) // Cosmos public emulator key
            .buildAsyncClient();

        String containerName = UUID.randomUUID().toString();

        try {
            // CREATE database
            CosmosDatabaseResponse databaseResponse = cosmosAsyncClient.createDatabase(databaseId).block();

            // CREATE collection
            CosmosContainerResponse containerResponse = cosmosAsyncClient.getDatabase(databaseId).createContainer(containerName, PARTITION_KEY_PATH).block();
        } finally {
            if (cosmosAsyncClient != null) {
                safeClose(cosmosAsyncClient);
            }
        }

        Thread.sleep(TIMEOUT);

        TokenCredential dummyServicePrincipal = new ClientSecretCredentialBuilder()
            .authorityHost("https://login.microsoftonline.com")
            .tenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
            .clientId("1950a258-227b-4e31-a9cf-717495945fc2")
            .clientSecret("clientSecret")
            .build();

        try {
            CosmosAsyncClient badCosmosAadClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST) // Cosmos public emulator endpoint
                .credential(dummyServicePrincipal)
                .buildAsyncClient();

        }
        catch (Exception e) {
            log.info("Expected exception: {}", e.getMessage());
            log.info("Expected cause: {}", e.getCause().toString());
            assert e.getMessage().contains("Invalid client secret provided");
            assert e.getCause().toString().contains("Invalid client secret provided");
        }



        TokenCredential emulatorCredential = new AadSimpleEmulatorTokenCredential(TestConfigurations.MASTER_KEY); // Cosmos public emulator key
        CosmosAsyncClient cosmosAadClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST) // Cosmos public emulator endpoint
            .credential(emulatorCredential)
            .buildAsyncClient();

        try {
            // READ database
            db = cosmosAadClient.getDatabase(databaseId).read()
                .map(dabaseResponse -> {
                    CosmosAsyncDatabase database = cosmosAadClient.getDatabase(dabaseResponse.getProperties().getId());
                    log.info("Found database {} with {}", database.getId(), dabaseResponse.getProperties().getETag());
                    return database;
                }).block();
            assert db != null;

            // READ database
            CosmosAsyncContainer container = db.getContainer(containerName).read()
                .map(cosmosContainerResponse -> {
                    CosmosAsyncContainer container1 = cosmosAadClient.getDatabase(databaseId).getContainer(cosmosContainerResponse.getProperties().getId());
                    log.info("Found container {} with {}", container1.getId(), cosmosContainerResponse.getProperties().getETag());
                    return container1;
                }).block();

            // CREATE document
            assert container != null;
            String itemName = UUID.randomUUID().toString();
            String partitionKeyValue = UUID.randomUUID().toString();
            ItemSample itemSample = getDocumentDefinition(itemName, partitionKeyValue);

            CosmosItemResponse<ItemSample> cosmosItemResponse = container.createItem(itemSample, new CosmosItemRequestOptions()).block();

            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            InternalObjectNode item = container
                .readItem(itemName, new PartitionKey(partitionKeyValue), options, InternalObjectNode.class)
                .map(CosmosItemResponse::getItem)
                .map(jsonNode -> {
                    log.info("Found item with content: " + jsonNode.toString());
                    return jsonNode;
                }).block();
            assert item != null;

            // QUERY document
            CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
            CosmosPagedFlux<JsonNode> queryPagedFlux = container
                .queryItems("SELECT * FROM c", requestOptions, JsonNode.class);
            List<JsonNode> feedResponse = queryPagedFlux.byPage()
                .flatMap(jsonNodeFeedResponse -> {
                    return Flux.fromIterable(jsonNodeFeedResponse.getResults());
                }).map(jsonNode -> {
                    log.info("Found item with content: " + jsonNode.toString());
                    return jsonNode;
                })
                .collectList()
                .block();

            // PATCH document
            CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();
            cosmosPatchOperations.set("/testField", "testField2");
            container.patchItem(itemSample.id, new PartitionKey(itemSample.mypk), cosmosPatchOperations, JsonNode.class).block();

            // READ document to verify patch operation
            item = container
                .readItem(itemName, new PartitionKey(partitionKeyValue), options, InternalObjectNode.class)
                .map(CosmosItemResponse::getItem)
                .map(jsonNode -> {
                    log.info("Found item with content: " + jsonNode.toString());
                    return jsonNode;
                }).block();

            assert item != null;
            String testField = item.get("testField").toString();
            assert testField.equals("testField2");

            // DELETE document
            container.deleteItem(item.getId(), new PartitionKey(partitionKeyValue));

        } finally {
            if (db != null) {
                cosmosAsyncClient.getDatabase(databaseId).delete().block();
            }

            if (cosmosAadClient != null) {
                safeClose(cosmosAadClient);
            }
        }

        Thread.sleep(SHUTDOWN_TIMEOUT);
    }

    @Test(groups = { "emulator" }, timeOut = 10 * TIMEOUT)
    public void testAadScopeOverride() throws Exception {
        CosmosAsyncClient setupClient = null;
        CosmosAsyncClient aadClient = null;
        String containerName = UUID.randomUUID().toString();
        String overrideScope = "https://cosmos.azure.com/.default";

        try {
            setupClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            setupClient.createDatabase(databaseId).block();
            setupClient.getDatabase(databaseId).createContainer(containerName, PARTITION_KEY_PATH).block();
        } finally {
            if (setupClient != null) {
                safeClose(setupClient);
            }
        }

        Thread.sleep(TIMEOUT);

        setEnv("AZURE_COSMOS_AAD_SCOPE_OVERRIDE", overrideScope);

        TokenCredential emulatorCredential =
            new AadSimpleEmulatorTokenCredential(TestConfigurations.MASTER_KEY);

        aadClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .credential(emulatorCredential)
            .buildAsyncClient();

        try {
            CosmosAsyncContainer container = aadClient
                .getDatabase(databaseId)
                .getContainer(containerName);

            String itemId = UUID.randomUUID().toString();
            String pk = UUID.randomUUID().toString();
            ItemSample item = getDocumentDefinition(itemId, pk);

            container.createItem(item).block();

            List<String> scopes = AadSimpleEmulatorTokenCredential.getLastScopes();
            assert scopes != null && scopes.size() == 1;
            assert overrideScope.equals(scopes.get(0));

            container.deleteItem(item.id, new PartitionKey(item.mypk)).block();
        } finally {
            try {
                CosmosAsyncClient cleanupClient = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .buildAsyncClient();
                try {
                    cleanupClient.getDatabase(databaseId).delete().block();
                } finally {
                    safeClose(cleanupClient);
                }
            } finally {
                if (aadClient != null) {
                    safeClose(aadClient);
                }
                setEnv("AZURE_COSMOS_AAD_SCOPE_OVERRIDE", "");
            }
        }

        Thread.sleep(SHUTDOWN_TIMEOUT);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setEnv(String key, String value) throws Exception {
        Map<String, String> env = System.getenv();
        Class<?> cl = env.getClass();
        try {
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            if (value == null) {
                writableEnv.remove(key);
            } else {
                writableEnv.put(key, value);
            }
        } catch (NoSuchFieldException nsfe) {
            Field[] fields = cl.getDeclaredFields();
            for (Field f : fields) {
                if (f.getType().getName().equals("java.util.Map")) {
                    f.setAccessible(true);
                    Map<String, String> map = (Map<String, String>) f.get(env);
                    if (value == null) {
                        map.remove(key);
                    } else {
                        map.put(key, value);
                    }
                }
            }
        }
    }

    private ItemSample getDocumentDefinition(String itemId, String partitionKeyValue) {
        ItemSample itemSample = new ItemSample();
        itemSample.id = itemId;
        itemSample.mypk = partitionKeyValue;
        itemSample.sgmts = "[[6519456, 1471916863], [2498434, 1455671440]]";
        itemSample.testField = "testField1";

        return itemSample;
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

    static class AadSimpleEmulatorTokenCredential implements TokenCredential {
        private final String emulatorKeyEncoded;
        private final String AAD_HEADER_COSMOS_EMULATOR = "{\"typ\":\"JWT\",\"alg\":\"RS256\",\"x5t\":\"CosmosEmulatorPrimaryMaster\",\"kid\":\"CosmosEmulatorPrimaryMaster\"}";
        private final String AAD_CLAIM_COSMOS_EMULATOR_FORMAT = "{\"aud\":\"https://localhost.localhost\",\"iss\":\"https://sts.fake-issuer.net/7b1999a1-dfd7-440e-8204-00170979b984\",\"iat\":%d,\"nbf\":%d,\"exp\":%d,\"aio\":\"\",\"appid\":\"localhost\",\"appidacr\":\"1\",\"idp\":\"https://localhost:8081/\",\"oid\":\"96313034-4739-43cb-93cd-74193adbe5b6\",\"rh\":\"\",\"sub\":\"localhost\",\"tid\":\"EmulatorFederation\",\"uti\":\"\",\"ver\":\"1.0\",\"scp\":\"user_impersonation\",\"groups\":[\"7ce1d003-4cb3-4879-b7c5-74062a35c66e\",\"e99ff30c-c229-4c67-ab29-30a6aebc3e58\",\"5549bb62-c77b-4305-bda9-9ec66b85d9e4\",\"c44fd685-5c58-452c-aaf7-13ce75184f65\",\"be895215-eab5-43b7-9536-9ef8fe130330\"]}";

        private static volatile List<String> lastScopes = Collections.emptyList();

        public static List<String> getLastScopes() {
            return lastScopes;
        }
        public AadSimpleEmulatorTokenCredential(String emulatorKey) {
            if (emulatorKey == null || emulatorKey.isEmpty()) {
                throw new IllegalArgumentException("emulatorKey");
            }

            this.emulatorKeyEncoded = Utils.encodeUrlBase64String(emulatorKey.getBytes());
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            List<String> scopes = tokenRequestContext.getScopes(); // List<String>, not String[]
            lastScopes = (scopes != null && !scopes.isEmpty())
                ? new ArrayList<>(scopes)
                : Collections.emptyList();

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

    static class ItemSample {
        public String id;
        public String mypk;
        public String sgmts;
        public String testField;

        public String toString() {
            try {
                return OBJECT_MAPPER.writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Unexpected object mapping exception");
            }
        }
    }
}
