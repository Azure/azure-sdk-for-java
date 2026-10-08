// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.Exceptions;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Live-account smoke test. Opt in with group manual-gateway-http2 and ACCOUNT_HOST/ACCOUNT_KEY.
 * Only a run-scoped database and its 400-RU/s test container are created.
 */
public class GatewayHttp2EndToEndTest {
    private static final String GROUP = "manual-gateway-http2";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @BeforeClass(groups = GROUP)
    public void createRunScopedResources() {
        String host = Objects.requireNonNull(System.getenv("ACCOUNT_HOST"), "ACCOUNT_HOST is required");
        String key = Objects.requireNonNull(System.getenv("ACCOUNT_KEY"), "ACCOUNT_KEY is required");
        GatewayConnectionConfig gateway = new GatewayConnectionConfig().setHttp2ConnectionConfig(
            new Http2ConnectionConfig().setEnabled(true).setMinConnectionPoolSize(1)
                .setMaxConnectionPoolSize(1).setMaxConcurrentStreams(30));
        client = new CosmosClientBuilder().endpoint(host).key(key).gatewayMode(gateway)
            .consistencyLevel(ConsistencyLevel.SESSION).preferredRegions(Collections.singletonList("Central US"))
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();
        database = Resources.create(client);
        container = TestSuiteBase.createCollection(database,
            new CosmosContainerProperties("gateway-http2-items", "/pk"), new CosmosContainerRequestOptions(), 400, client);
        Reporter.log("Created run-scoped Gateway/H2 test database: " + database.getId(), true);
    }

    @Test(groups = GROUP, timeOut = 180000)
    public void gatewayHttp2CreateReadReplaceQueryAndDelete() throws Exception {
        PartitionKey key = new PartitionKey("gateway-http2");
        ObjectNode item = MAPPER.createObjectNode().put("id", "gateway-http2-item")
            .put("pk", "gateway-http2").put("value", "created");

        CosmosItemResponse<ObjectNode> created = container.createItem(item).block(TIMEOUT);
        assertThat(created.getStatusCode()).isEqualTo(201);
        assertGatewayHttp2("create", created.getDiagnostics());

        CosmosItemResponse<ObjectNode> read = container.readItem(item.get("id").asText(), key, ObjectNode.class)
            .block(TIMEOUT);
        assertThat(read.getStatusCode()).isEqualTo(200);
        assertThat(read.getItem().get("value").asText()).isEqualTo("created");
        assertGatewayHttp2("read", read.getDiagnostics());

        item.put("value", "replaced");
        CosmosItemResponse<ObjectNode> replaced = container.replaceItem(item, item.get("id").asText(), key)
            .block(TIMEOUT);
        assertThat(replaced.getStatusCode()).isEqualTo(200);
        assertThat(replaced.getItem().get("value").asText()).isEqualTo("replaced");
        assertGatewayHttp2("replace", replaced.getDiagnostics());

        List<FeedResponse<ObjectNode>> pages = container.queryItems("SELECT * FROM c",
            new CosmosQueryRequestOptions().setPartitionKey(key), ObjectNode.class).byPage().collectList().block(TIMEOUT);
        assertThat(pages).isNotEmpty();
        assertThat(pages.stream().flatMap(page -> page.getResults().stream()))
            .singleElement().satisfies(result -> assertThat(result.get("value").asText()).isEqualTo("replaced"));
        for (FeedResponse<ObjectNode> page : pages) {
            assertGatewayHttp2("query", page.getCosmosDiagnostics());
        }

        CosmosItemResponse<Object> deleted = container.deleteItem(item.get("id").asText(), key).block(TIMEOUT);
        assertThat(deleted.getStatusCode()).isEqualTo(204);
        assertGatewayHttp2("delete", deleted.getDiagnostics());
    }

    @AfterClass(groups = GROUP, alwaysRun = true)
    public void deleteRunScopedResources() {
        try {
            if (database != null) {
                String databaseId = database.getId();
                Resources.delete(database);
                assertThatThrownBy(() -> database.read().block(TIMEOUT)).satisfies(error ->
                    assertThat(Exceptions.unwrap(error)).isInstanceOfSatisfying(CosmosException.class,
                        cosmos -> assertThat(cosmos.getStatusCode()).isEqualTo(404)));
                Reporter.log("Cleanup verified: database " + databaseId + " returns 404", true);
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private static void assertGatewayHttp2(String operation, CosmosDiagnostics diagnostics) throws Exception {
        JsonNode json = MAPPER.readTree(diagnostics.toString());
        // Query diagnostics aggregate several client-side request statistics rather than using the point-operation shape.
        List<JsonNode> configurations = json.findValues("clientCfgs");
        assertThat(configurations).isNotEmpty();
        configurations.forEach(config -> assertThat(config.path("connectionMode").asText()).isEqualTo("GATEWAY"));
        List<JsonNode> attemptLists = json.findValues("gatewayStatisticsList");
        assertThat(attemptLists).isNotEmpty();
        int attemptsSeen = 0;
        for (JsonNode attempts : attemptLists) {
            assertThat(attempts.isArray()).isTrue();
            for (JsonNode attempt : attempts) {
                attemptsSeen++;
                assertThat(attempt.path("isHttp2").asBoolean()).as(operation + " must negotiate H2").isTrue();
                assertThat(attempt.path("statusCode").asInt()).isBetween(200, 299);
                assertThat(attempt.path("parentChannelId").asText()).isNotEmpty();
                Reporter.log("Gateway/H2 verified: operation=" + operation + ", status="
                    + attempt.path("statusCode").asInt() + ", parent=" + attempt.path("parentChannelId").asText()
                    + ", stream=" + attempt.path("channelId").asText(), true);
            }
        }
        assertThat(attemptsSeen).isGreaterThan(0);
    }

    private static final class Resources extends TestSuiteBase {
        private static CosmosAsyncDatabase create(CosmosAsyncClient client) {
            return createTestDatabase(client, "gateway-http2-e2e");
        }

        private static void delete(CosmosAsyncDatabase database) {
            safeDeleteDatabase(database);
        }
    }
}
