// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionMixedArrayE2ETest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MIXED_ARRAYS_ENABLED = "COSMOS.ENCRYPTION_MIXED_ARRAYS_ENABLED";
    private static final String HTTP2_ENABLED = "COSMOS.HTTP2_ENABLED";
    private static final String THINCLIENT_ENABLED = "COSMOS.THINCLIENT_ENABLED";
    private static final Pattern FEATURE_MASK = Pattern.compile("\\|F([0-9A-F]+)$");
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final String runId = "encma-" + UUID.randomUUID().toString().substring(0, 8);
    private final String databaseId = runId;
    private final String containerId = "mixed-arrays";
    private String endpoint;
    private String key;
    private CosmosAsyncClient adminClient;
    private boolean databaseCreated;

    @BeforeClass(groups = "encryption-live", timeOut = 180000)
    public void createIsolatedEncryptionContainer() throws Exception {
        endpoint = TestConfigurations.HOST;
        key = TestConfigurations.MASTER_KEY;
        assertThat(endpoint).as("ACCOUNT_HOST in the live-test configuration").isNotBlank();
        assertThat(key).as("ACCOUNT_KEY in the live-test configuration").isNotBlank();
        assertThat(java.net.URI.create(endpoint).getHost()).as("Service telemetry requires a live account")
            .isNotIn("localhost", "127.0.0.1");

        Map<String, String> previous = configure("GatewayV1", false);
        try {
            adminClient = clientBuilder("GatewayV1", runId + "-setup").buildAsyncClient();
            adminClient.createDatabase(databaseId).block(TIMEOUT);
            databaseCreated = true;
            CosmosEncryptionAsyncClient encryptionClient = wrap(adminClient);
            CosmosEncryptionAsyncDatabase database = encryptionClient.getCosmosEncryptionAsyncDatabase(databaseId);
            database.createClientEncryptionKey("key1", CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(),
                new EncryptionKeyWrapMetadata("TEST_KEY_RESOLVER", "key1", "tempmetadata1", "RSA-OAEP")).block(TIMEOUT);
            ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath()
                .setPath("/sensitive")
                .setClientEncryptionKeyId("key1")
                .setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName())
                .setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());
            CosmosContainerProperties container = new CosmosContainerProperties(containerId, "/pk");
            container.setClientEncryptionPolicy(new ClientEncryptionPolicy(Collections.singletonList(includedPath), 2));
            adminClient.getDatabase(databaseId).createContainer(container, ThroughputProperties.createManualThroughput(400))
                .block(TIMEOUT);
            System.out.println("ENCRYPTION_E2E_SETUP runId=" + runId + " account="
                + java.net.URI.create(endpoint).getHost().split("\\.")[0] + " utc=" + Instant.now());
        } finally {
            restore(previous);
        }
    }

    @DataProvider(name = "connectionModes")
    public Object[][] connectionModes() {
        return new Object[][] {
            {"GatewayV1", false}, {"GatewayV1", true},
            {"GatewayV2", false}, {"GatewayV2", true},
            {"Direct", false}, {"Direct", true}
        };
    }

    @Test(groups = "encryption-live", dataProvider = "connectionModes", timeOut = 240000)
    public void mixedArraysAndUserAgent(String mode, boolean optOut) throws Exception {
        String caseId = mode + (optOut ? "-out" : "-in");
        Map<String, String> previous = configure(mode, optOut);
        Instant started = Instant.now();
        try (CosmosAsyncClient client = clientBuilder(mode, runId + "-" + caseId).buildAsyncClient()) {
            CosmosEncryptionAsyncContainer container = wrap(client).getCosmosEncryptionAsyncDatabase(databaseId)
                .getCosmosEncryptionAsyncContainer(containerId);
            PartitionKey partitionKey = new PartitionKey(caseId);
            ObjectNode document = MAPPER.createObjectNode();
            document.put("id", caseId);
            document.put("pk", caseId);
            JsonNode original = MAPPER.readTree(
                "[{\"label\":\"CREATE_SECRET\"},42,\"SCALAR_SECRET\",true,null,[\"NESTED_SECRET\"]]");
            document.set("sensitive", original.deepCopy());

            CosmosItemResponse<JsonNode> created = container.<JsonNode>createItem(document, partitionKey,
                new CosmosItemRequestOptions()).block(TIMEOUT);
            assertThat(created.getItem().get("sensitive").toString()).isEqualTo(original.toString());
            record(mode, optOut, "create", started, created);

            CosmosItemResponse<JsonNode> raw = container.getCosmosAsyncContainer()
                .readItem(caseId, partitionKey, JsonNode.class).block(TIMEOUT);
            assertStoredArray(raw.getItem().get("sensitive"), optOut, "CREATE_SECRET", 42);
            record(mode, optOut, "raw-read-create", started, raw);

            CosmosItemResponse<JsonNode> read = container.readItem(caseId, partitionKey, JsonNode.class).block(TIMEOUT);
            assertThat(read.getItem().get("sensitive").toString()).isEqualTo(original.toString());
            record(mode, optOut, "read-create", started, read);

            JsonNode patched = MAPPER.readTree(
                "[{\"label\":\"PATCH_SECRET\"},43,\"SCALAR_SECRET\",true,null,[\"NESTED_SECRET\"]]");
            CosmosItemResponse<JsonNode> patch = container.patchItem(caseId, partitionKey,
                CosmosPatchOperations.create().set("/sensitive", patched.deepCopy()),
                new CosmosPatchItemRequestOptions(), JsonNode.class).block(TIMEOUT);
            assertThat(patch.getItem().get("sensitive").toString()).isEqualTo(patched.toString());
            record(mode, optOut, "patch", started, patch);

            raw = container.getCosmosAsyncContainer().readItem(caseId, partitionKey, JsonNode.class).block(TIMEOUT);
            assertStoredArray(raw.getItem().get("sensitive"), optOut, "PATCH_SECRET", 43);
            record(mode, optOut, "raw-read-patch", started, raw);
            read = container.readItem(caseId, partitionKey, JsonNode.class).block(TIMEOUT);
            assertThat(read.getItem().get("sensitive").toString()).isEqualTo(patched.toString());
            record(mode, optOut, "read-patch", started, read);
        } finally {
            restore(previous);
        }
    }

    private void assertStoredArray(JsonNode array, boolean optOut, String objectSecret, int number) {
        assertThat(array.get(0).get("label").asText()).isNotEqualTo(objectSecret);
        assertThat(array.get(5).get(0).asText()).isNotEqualTo("NESTED_SECRET");
        assertThat(array.get(4).isNull()).isTrue();
        if (optOut) {
            assertThat(array.get(1).asInt()).isEqualTo(number);
            assertThat(array.get(2).asText()).isEqualTo("SCALAR_SECRET");
            assertThat(array.get(3).asBoolean()).isTrue();
        } else {
            assertThat(array.get(1).isTextual()).isTrue();
            assertThat(array.get(3).isTextual()).isTrue();
            assertThat(array.toString()).doesNotContain(objectSecret, "SCALAR_SECRET", "NESTED_SECRET");
        }
    }

    private void record(String mode, boolean optOut, String operation, Instant started,
                        CosmosItemResponse<JsonNode> response) throws Exception {
        String userAgent = response.getDiagnostics().getUserAgent();
        Matcher matcher = FEATURE_MASK.matcher(userAgent);
        int flags = matcher.find() ? Integer.parseInt(matcher.group(1), 16) : 0;
        assertThat((flags & 0x80) != 0).as("Opt-out bit in the response diagnostics").isEqualTo(optOut);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runId", runId);
        result.put("mode", mode);
        result.put("optOut", optOut);
        result.put("operation", operation);
        result.put("startedUtc", started.toString());
        result.put("finishedUtc", Instant.now().toString());
        result.put("activityId", response.getActivityId());
        result.put("status", response.getStatusCode());
        result.put("userAgent", userAgent);
        System.out.println("ENCRYPTION_E2E " + MAPPER.writeValueAsString(result));
    }

    private CosmosClientBuilder clientBuilder(String mode, String suffix) {
        CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(endpoint).key(key)
            .consistencyLevel(ConsistencyLevel.SESSION).contentResponseOnWriteEnabled(true).userAgentSuffix(suffix);
        return "Direct".equals(mode) ? builder.directMode()
            : builder.gatewayMode(new GatewayConnectionConfig()
                .setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled("GatewayV2".equals(mode))));
    }

    private CosmosEncryptionAsyncClient wrap(CosmosAsyncClient client) {
        return new CosmosEncryptionClientBuilder().cosmosAsyncClient(client)
            .keyEncryptionKeyResolver(new TestSuiteBase.TestKeyEncryptionKeyResolver())
            .keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildAsyncClient();
    }

    private Map<String, String> configure(String mode, boolean optOut) {
        Map<String, String> previous = new LinkedHashMap<>();
        previous.put(MIXED_ARRAYS_ENABLED, System.getProperty(MIXED_ARRAYS_ENABLED));
        previous.put(HTTP2_ENABLED, System.getProperty(HTTP2_ENABLED));
        previous.put(THINCLIENT_ENABLED, System.getProperty(THINCLIENT_ENABLED));
        System.setProperty(MIXED_ARRAYS_ENABLED, Boolean.toString(!optOut));
        System.setProperty(HTTP2_ENABLED, Boolean.toString("GatewayV2".equals(mode)));
        System.setProperty(THINCLIENT_ENABLED, Boolean.toString("GatewayV2".equals(mode)));
        return previous;
    }

    private void restore(Map<String, String> previous) {
        previous.forEach((name, value) -> {
            if (value == null) {
                System.clearProperty(name);
            } else {
                System.setProperty(name, value);
            }
        });
    }

    @AfterClass(groups = "encryption-live", alwaysRun = true, timeOut = 120000)
    public void deleteIsolatedDatabase() {
        if (adminClient != null) {
            try {
                if (databaseCreated) {
                    adminClient.getDatabase(databaseId).delete().block(TIMEOUT);
                    System.out.println("ENCRYPTION_E2E_CLEANUP database=" + databaseId + " deleted=true");
                }
            } finally {
                adminClient.close();
            }
        }
    }
}
