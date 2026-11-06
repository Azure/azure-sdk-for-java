// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryPagePrefetchTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private final AtomicInteger documentQueries = new AtomicInteger();

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void setup() {
        client = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY).gatewayMode().buildAsyncClient();
        database = createTestDatabase(client, "page-prefetch");
        database.createContainer(new CosmosContainerProperties("items", "/pk")).block();
        container = database.getContainer("items");
        for (int i = 0; i < 6; i++) {
            ObjectNode document = JsonNodeFactory.instance.objectNode().put("id", Integer.toString(i)).put("pk", "p");
            container.createItem(document).block();
        }

        RxDocumentClientImpl internalClient = (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        RxStoreModel gateway = ReflectionUtils.getGatewayProxy(internalClient);
        HttpClient original = ReflectionUtils.getGatewayHttpClient(client);
        HttpClient transport = mock(HttpClient.class);
        when(transport.send(any(HttpRequest.class), any(Duration.class))).thenAnswer(invocation ->
            send(original, invocation.getArgument(0), invocation.getArgument(1)));
        when(transport.send(any(HttpRequest.class))).thenAnswer(invocation ->
            send(original, invocation.getArgument(0), null));
        ReflectionUtils.setGatewayHttpClient(gateway, transport);
    }

    private Mono<HttpResponse> send(HttpClient original, HttpRequest request, Duration timeout) {
        return Mono.defer(() -> {
            Map<String, String> headers = request.headers().toMap();
            if ("true".equalsIgnoreCase(headers.get(HttpConstants.HttpHeaders.IS_QUERY))
                && !"true".equalsIgnoreCase(headers.get(HttpConstants.HttpHeaders.IS_QUERY_PLAN_REQUEST))) {
                documentQueries.incrementAndGet();
            }
            // Force an asynchronous response, including when transport data is already available.
            return Mono.delay(Duration.ofMillis(10))
                .then(timeout == null ? original.send(request) : original.send(request, timeout));
        });
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void oneBackendQueryPerResumedPage() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
            .setPartitionKey(new PartitionKey("p")).setMaxPrefetchPageCount(0);
        documentQueries.set(0);
        Set<String> ids = new HashSet<>();
        String continuation = null;
        int pages = 0;
        do {
            int before = documentQueries.get();
            FeedResponse<ObjectNode> page = container.queryItems("SELECT * FROM c", options, ObjectNode.class)
                .byPage(continuation, 2).next().block(Duration.ofSeconds(30));
            assertThat(page).isNotNull();
            assertThat(documentQueries.get() - before).isEqualTo(1);
            page.getResults().forEach(item -> assertThat(ids.add(item.get("id").asText())).isTrue());
            continuation = page.getContinuationToken();
            assertThat(++pages).isLessThanOrEqualTo(6);
        } while (continuation != null);
        assertThat(ids).hasSize(6);
        assertThat(pages).isGreaterThan(1);
        assertThat(documentQueries.get()).isEqualTo(pages);
    }

    @AfterClass(groups = { "query" }, alwaysRun = true, timeOut = SHUTDOWN_TIMEOUT)
    public void cleanup() {
        try {
            if (database != null) {
                safeDeleteDatabase(database);
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
