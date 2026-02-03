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
import com.azure.cosmos.implementation.Configs;
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
    @Test(groups = { "long-emulator" }, timeOut = 10 * TIMEOUT)
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

    @Test(groups = { "long-emulator" }, timeOut = 10 * TIMEOUT)
    public void overrideScope_only_noFallback_onSuccess() throws Exception {
        CosmosAsyncClient client = null;
        ScopeRecorder.clear();

        java.net.URI ep = new java.net.URI(TestConfigurations.HOST);
        final String overrideScope = ep.getScheme() + "://" + ep.getHost() + "/.default";
        setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, overrideScope);

        try {
            TokenCredential cred = new AadSimpleEmulatorTokenCredential(TestConfigurations.MASTER_KEY);

            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .credential(cred)
                .buildAsyncClient();

            client.readAllDatabases().byPage().blockFirst();

            java.util.List<String> scopes = ScopeRecorder.all();
            assert scopes.size() >= 1 : "Expected at least one AAD call";
            for (String s : scopes) {
                assert overrideScope.equals(s) : "Expected only override scope; saw: " + scopes;
            }
        } finally {
            if (client != null) safeClose(client);
            setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, Configs.DEFAULT_AAD_SCOPE_OVERRIDE);
        }
    }

    @Test(groups = { "long-emulator"}, timeOut = 10 * TIMEOUT)
    public void overrideScope_authError_noFallback() throws Exception {
        CosmosAsyncClient client = null;
        ScopeRecorder.clear();

        final String overrideScope = "https://my.custom.scope/.default";
        setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, overrideScope);

        try {
            TokenCredential cred = new AlwaysFail500011Credential();

            try {
                client = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .credential(cred)
                    .buildAsyncClient();

                client.readAllDatabases().byPage().blockFirst();
                assert false : "Expected an auth failure with override scope";
            } catch (Exception ex) {
                // Only the override scope should have been attempted; no fallback allowed
                java.util.List<String> scopes = ScopeRecorder.all();
                assert scopes.size() >= 1 : "Expected at least one scope attempt";
                for (String s : scopes) {
                    assert overrideScope.equals(s) : "No fallback allowed in override mode; saw: " + scopes;
                }
            }
        } finally {
            if (client != null) safeClose(client);
            setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, Configs.DEFAULT_AAD_SCOPE_OVERRIDE);
        }
    }

    @Test(groups = { "long-emulator"}, timeOut = 10 * TIMEOUT)
    public void accountScope_only_whenNoOverride_andNoAuthFailure() throws Exception {
        CosmosAsyncClient client = null;
        ScopeRecorder.clear();
        setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, Configs.DEFAULT_AAD_SCOPE_OVERRIDE);

        java.net.URI ep = new java.net.URI(TestConfigurations.HOST);
        String accountScope = ep.getScheme() + "://" + ep.getHost() + "/.default";

        try {
            TokenCredential cred = new AadSimpleEmulatorTokenCredential(TestConfigurations.MASTER_KEY);

            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .credential(cred)
                .buildAsyncClient();

            client.readAllDatabases().byPage().blockFirst();

            java.util.List<String> scopes = ScopeRecorder.all();
            assert scopes.size() >= 1 : "Expected at least one AAD call";
            for (String s : scopes) {
                assert accountScope.equals(s) : "Expected only account scope; saw: " + scopes;
            }
        } finally {
            if (client != null) safeClose(client);
            setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, Configs.DEFAULT_AAD_SCOPE_OVERRIDE);
        }
    }

    @Test(groups = { "long-emulator"}, timeOut = 10 * TIMEOUT)
    public void accountScope_fallbackToCosmosScope_onAadSts500011() throws Exception {
        CosmosAsyncClient client = null;
        ScopeRecorder.clear();
        setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, Configs.DEFAULT_AAD_SCOPE_OVERRIDE);

        java.net.URI ep = new java.net.URI(TestConfigurations.HOST);
        String accountScope = ep.getScheme() + "://" + ep.getHost() + "/.default";
        String fallbackScope = "https://cosmos.azure.com/.default";

        try {
            // Fail on account scope with AADSTS500011
            TokenCredential cred = new AccountThenFallbackCredential(TestConfigurations.MASTER_KEY, accountScope);

            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .credential(cred)
                .buildAsyncClient();

            client.readAllDatabases().byPage().blockFirst();

            java.util.List<String> scopes = ScopeRecorder.all();
            assert scopes.contains(accountScope) : "Expected primary account scope attempt; saw: " + scopes;
            assert scopes.contains(fallbackScope) : "Expected fallback to cosmos public scope; saw: " + scopes;
        } finally {
            if (client != null) safeClose(client);
            setEnv(Configs.AAD_SCOPE_OVERRIDE_VARIABLE, Configs.DEFAULT_AAD_SCOPE_OVERRIDE);
        }
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

    // Records all scopes used during the test run (append-only).
    static final class ScopeRecorder {
        private static final java.util.concurrent.CopyOnWriteArrayList<String> SEEN = new java.util.concurrent.CopyOnWriteArrayList<>();
        static void clear() { SEEN.clear(); }
        static void record(TokenRequestContext ctx) {
            java.util.List<String> s = ctx.getScopes();
            if (s != null) SEEN.addAll(s);
        }
        static java.util.List<String> all() { return new java.util.ArrayList<>(SEEN); }
    }

    /**
     * Always fails with an AADSTS500011 message for any scope.
     * Used to prove that the "override scope" path does NOT fallback.
     */
    static class AlwaysFail500011Credential implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            ScopeRecorder.record(tokenRequestContext);
            return Mono.error(new RuntimeException("AADSTS500011: Application <id> was not found in the directory"));
        }
    }

    static class AccountThenFallbackCredential implements TokenCredential {
        private final AadSimpleEmulatorTokenCredential emulatorIssuer;
        private final String accountScope;
        private final String cosmosPublicScope = "https://cosmos.azure.com/.default";
        private final java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger(0);

        AccountThenFallbackCredential(String emulatorKey, String accountScope) {
            this.emulatorIssuer = new AadSimpleEmulatorTokenCredential(emulatorKey);
            this.accountScope = accountScope;
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            ScopeRecorder.record(tokenRequestContext);

            String scope = tokenRequestContext.getScopes() != null && !tokenRequestContext.getScopes().isEmpty()
                ? tokenRequestContext.getScopes().get(0)
                : "";

            int n = calls.incrementAndGet();

            // Fail on the first attempt if it is the account scope, to trigger fallback
            if (n == 1 && scope.equals(accountScope)) {
                return Mono.error(new RuntimeException("AADSTS500011: Application <id> was not found in the directory"));
            }

            // When SDK retries with the cosmos public scope, succeed with a valid emulator token
            if (scope.equals(cosmosPublicScope)) {
                return emulatorIssuer.getToken(tokenRequestContext);
            }

            // If anything unexpected happens, fail loudly so the test points to the issue
            return Mono.error(new IllegalStateException("Unexpected scope or call ordering. Scope=" + scope + " call=" + n));
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

    @BeforeMethod(groups = { "long-emulator" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
    public void beforeMethod() {
    }

    @BeforeClass(groups = { "long-emulator" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorTest() {
    }

    @AfterMethod(groups = { "long-emulator" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
    }

    @AfterClass(groups = { "long-emulator" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
    }

    static class AadSimpleEmulatorTokenCredential implements TokenCredential {
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
            // Record scopes for verification in tests
            AadAuthorizationTests.ScopeRecorder.record(tokenRequestContext);

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
