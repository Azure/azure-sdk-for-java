// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Callers that resume a query from a continuation token and read a single page per call, for example a stateless
 * "load more" API, must cost one backend query per page. Reading ahead would issue a query for a page the caller
 * fetches again, with the same continuation token, on its next call.
 */
public class QueryPageByPageResumeTest extends TestSuiteBase {
    private static final String PARTITION_KEY_VALUE = "pk-1";
    private static final int DOCUMENT_COUNT = 10;
    private static final int PAGE_SIZE = 2;
    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(30);

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;
    private final AtomicInteger documentQueries = new AtomicInteger();

    // Backend queries are counted on the gateway HTTP client, so the client must use gateway mode.
    @Factory(dataProvider = "simpleClientBuilderGatewaySession")
    public QueryPageByPageResumeTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "queryOptions")
    public Object[][] queryOptions() {
        Supplier<CosmosQueryRequestOptions> singlePartition =
            () -> new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE));
        Supplier<CosmosQueryRequestOptions> crossPartition = CosmosQueryRequestOptions::new;
        return new Object[][] {
            { "single partition", singlePartition },
            { "cross partition", crossPartition }
        };
    }

    @Test(groups = { "query" }, dataProvider = "queryOptions", timeOut = TIMEOUT)
    public void resumingOnePageAtATimeSendsOneBackendQueryPerPage(
        String scope,
        Supplier<CosmosQueryRequestOptions> queryOptions) {

        Set<String> readIds = new HashSet<>();
        String continuationToken = null;
        int pages = 0;
        do {
            int queriesBefore = documentQueries.get();
            FeedResponse<ObjectNode> page = container
                .queryItems("SELECT * FROM c", queryOptions.get(), ObjectNode.class)
                .byPage(continuationToken, PAGE_SIZE)
                .next()
                .block(PAGE_TIMEOUT);

            assertThat(page).isNotNull();
            assertThat(documentQueries.get() - queriesBefore)
                .as("backend queries for page %d (%s)", pages + 1, scope)
                .isEqualTo(1);
            page.getResults().forEach(item -> assertThat(readIds.add(item.get("id").asText())).isTrue());
            continuationToken = page.getContinuationToken();
            pages++;
        } while (continuationToken != null && pages <= DOCUMENT_COUNT);

        assertThat(continuationToken).isNull();
        assertThat(readIds).hasSize(DOCUMENT_COUNT);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_QueryPageByPageResumeTest() {
        client = getClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .buildAsyncClient();

        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(UUID.randomUUID().toString(), "/mypk");
        container = createCollection(
            getSharedCosmosDatabase(client),
            containerProperties,
            new CosmosContainerRequestOptions(),
            400);

        List<ObjectNode> documents = new ArrayList<>();
        for (int i = 0; i < DOCUMENT_COUNT; i++) {
            documents.add(JsonNodeFactory.instance.objectNode()
                .put("id", UUID.randomUUID().toString())
                .put("mypk", PARTITION_KEY_VALUE));
        }
        insertAllItemsBlocking(container, documents, true);

        countDocumentQueriesOnGatewayHttpClient();
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(container);
        safeClose(client);
    }

    private void countDocumentQueriesOnGatewayHttpClient() {
        RxDocumentClientImpl documentClient = (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        RxStoreModel gatewayProxy = ReflectionUtils.getGatewayProxy(documentClient);
        HttpClient gatewayHttpClient = ReflectionUtils.getGatewayHttpClient(documentClient);
        HttpClient countingHttpClient = mock(HttpClient.class);
        when(countingHttpClient.send(any(HttpRequest.class), any(Duration.class))).thenAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            if (isDocumentQuery(request)) {
                documentQueries.incrementAndGet();
            }
            return gatewayHttpClient.send(request, invocation.getArgument(1));
        });
        ReflectionUtils.setGatewayHttpClient(gatewayProxy, countingHttpClient);
    }

    private static boolean isDocumentQuery(HttpRequest request) {
        return "true".equalsIgnoreCase(request.headers().value(HttpConstants.HttpHeaders.IS_QUERY))
            && request.headers().value(HttpConstants.HttpHeaders.IS_QUERY_PLAN_REQUEST) == null;
    }
}
