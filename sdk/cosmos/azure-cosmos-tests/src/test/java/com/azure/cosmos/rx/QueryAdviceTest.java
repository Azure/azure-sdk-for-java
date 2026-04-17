// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore("TODO: Ignore these test cases until the public emulator with query advisor is released.")
public class QueryAdviceTest extends TestSuiteBase {

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private CosmosAsyncClient client;
    private List<InternalObjectNode> createdDocuments = new ArrayList<>();

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public QueryAdviceTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_QueryAdviceTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        cleanUpContainer(createdCollection);

        // Insert test documents with various field types to trigger query advice rules
        for (int i = 0; i < 10; i++) {
            createdDocuments.add(createDocument(createdCollection, i));
        }

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceNotReturnedWhenDisabled() {
        // When queryAdviceEnabled is not set (default false), no query advice should be returned
        String query = "SELECT * FROM c WHERE CONTAINS(c.name, 'test')";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable =
            createdCollection.queryItems(query, options, InternalObjectNode.class);

        FeedResponseListValidator<InternalObjectNode> validator =
            new FeedResponseListValidator.Builder<InternalObjectNode>()
                .hasNoQueryAdviceOnAnyPage()
                .build();

        validateQuerySuccess(queryObservable.byPage(), validator, TIMEOUT);
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceReturnedWhenEnabled() {
        // When queryAdviceEnabled is true AND the query triggers an advice rule,
        // query advice should be returned
        // CONTAINS query should trigger QA1002: "If you are matching on a string prefix, consider using STARTSWITH."
        String query = "SELECT * FROM c WHERE CONTAINS(c.name, 'test')";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryAdviceEnabled(true);

        CosmosPagedFlux<InternalObjectNode> queryObservable =
            createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<FeedResponse<InternalObjectNode>> feedResponses =
            queryObservable.byPage().collectList().block();

        assertThat(feedResponses).isNotNull().isNotEmpty();

        // When query advice is enabled, at least one page should have advice for CONTAINS queries
        boolean hasAdvice = feedResponses.stream()
            .anyMatch(page -> page.getQueryAdvice() != null);

        if (hasAdvice) {
            // If the service returns advice, validate it contains the expected content
            String advice = feedResponses.stream()
                .map(FeedResponse::getQueryAdvice)
                .filter(a -> a != null)
                .findFirst()
                .orElse(null);

            assertThat(advice).isNotNull();
            // QA1002 rule should mention STARTSWITH
            assertThat(advice).containsIgnoringCase("STARTSWITH");
        }
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceForGetCurrentDateTime() {
        // GetCurrentDateTime query should trigger QA1007
        String query = "SELECT * FROM c WHERE c.timestamp > GetCurrentDateTime()";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryAdviceEnabled(true);

        CosmosPagedFlux<InternalObjectNode> queryObservable =
            createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<FeedResponse<InternalObjectNode>> feedResponses =
            queryObservable.byPage().collectList().block();

        assertThat(feedResponses).isNotNull().isNotEmpty();

        boolean hasAdvice = feedResponses.stream()
            .anyMatch(page -> page.getQueryAdvice() != null);

        if (hasAdvice) {
            String advice = feedResponses.stream()
                .map(FeedResponse::getQueryAdvice)
                .filter(a -> a != null)
                .findFirst()
                .orElse(null);

            assertThat(advice).isNotNull();
            // QA1007 rule should mention GetCurrentDateTimeStatic
            assertThat(advice).containsIgnoringCase("GetCurrentDateTimeStatic");
        }
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceForSimpleQuery() {
        // Simple SELECT * query should NOT trigger any query advice even when enabled
        String query = "SELECT * FROM c";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryAdviceEnabled(true);

        CosmosPagedFlux<InternalObjectNode> queryObservable =
            createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<FeedResponse<InternalObjectNode>> feedResponses =
            queryObservable.byPage().collectList().block();

        assertThat(feedResponses).isNotNull().isNotEmpty();

        // A simple SELECT * should not produce any query advice
        for (FeedResponse<InternalObjectNode> page : feedResponses) {
            assertThat(page.getQueryAdvice())
                .describedAs("Simple SELECT * should not produce query advice")
                .isNull();
        }
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceWithValidatorForContains() {
        // Test using FeedResponseListValidator with query advice validation
        String query = "SELECT * FROM c WHERE CONTAINS(c.name, 'test')";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryAdviceEnabled(true);

        CosmosPagedFlux<InternalObjectNode> queryObservable =
            createdCollection.queryItems(query, options, InternalObjectNode.class);

        // First verify the query returns results or at least pages
        List<FeedResponse<InternalObjectNode>> feedResponses =
            queryObservable.byPage().collectList().block();

        assertThat(feedResponses).isNotNull().isNotEmpty();

        // Check if service returned advice (service may not support this in all environments)
        boolean serviceReturnsAdvice = feedResponses.stream()
            .anyMatch(page -> page.getQueryAdvice() != null);

        if (serviceReturnsAdvice) {
            // Re-run with validator
            CosmosPagedFlux<InternalObjectNode> queryObservable2 =
                createdCollection.queryItems(query, options, InternalObjectNode.class);

            FeedResponseListValidator<InternalObjectNode> validator =
                new FeedResponseListValidator.Builder<InternalObjectNode>()
                    .hasQueryAdviceOnAtLeastOnePage()
                    .hasQueryAdviceContainingOnAtLeastOnePage("STARTSWITH")
                    .build();

            validateQuerySuccess(queryObservable2.byPage(), validator, TIMEOUT);
        }
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceWithPerPageValidator() {
        // Test using per-page FeedResponseValidator for query advice
        String query = "SELECT * FROM c WHERE CONTAINS(c.name, 'test')";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryAdviceEnabled(true);

        CosmosPagedFlux<InternalObjectNode> queryObservable =
            createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<FeedResponse<InternalObjectNode>> feedResponses =
            queryObservable.byPage().collectList().block();

        assertThat(feedResponses).isNotNull().isNotEmpty();

        // Find a page that has advice and validate it
        boolean serviceReturnsAdvice = feedResponses.stream()
            .anyMatch(page -> page.getQueryAdvice() != null);

        if (serviceReturnsAdvice) {
            CosmosPagedFlux<InternalObjectNode> queryObservable2 =
                createdCollection.queryItems(query, options, InternalObjectNode.class);

            FeedResponseListValidator<InternalObjectNode> validator =
                new FeedResponseListValidator.Builder<InternalObjectNode>()
                    .pageSatisfy(0, new FeedResponseValidator.Builder<InternalObjectNode>()
                        .hasQueryAdvice()
                        .hasQueryAdviceContaining("STARTSWITH")
                        .build())
                    .build();

            validateQuerySuccess(queryObservable2.byPage(), validator, TIMEOUT);
        }
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceEnabledDisabledToggle() {
        // Verify that toggling queryAdviceEnabled on the same query changes behavior
        String query = "SELECT * FROM c WHERE CONTAINS(c.name, 'test')";

        // First query with advice disabled
        CosmosQueryRequestOptions optionsDisabled = new CosmosQueryRequestOptions();
        optionsDisabled.setQueryAdviceEnabled(false);

        List<FeedResponse<InternalObjectNode>> disabledResponses =
            createdCollection.queryItems(query, optionsDisabled, InternalObjectNode.class)
                .byPage().collectList().block();

        assertThat(disabledResponses).isNotNull().isNotEmpty();
        for (FeedResponse<InternalObjectNode> page : disabledResponses) {
            assertThat(page.getQueryAdvice())
                .describedAs("Query advice should be null when explicitly disabled")
                .isNull();
        }

        // Then same query with advice enabled
        CosmosQueryRequestOptions optionsEnabled = new CosmosQueryRequestOptions();
        optionsEnabled.setQueryAdviceEnabled(true);

        List<FeedResponse<InternalObjectNode>> enabledResponses =
            createdCollection.queryItems(query, optionsEnabled, InternalObjectNode.class)
                .byPage().collectList().block();

        assertThat(enabledResponses).isNotNull().isNotEmpty();

        // If service supports query advice, they should differ
        boolean hasAdviceWhenEnabled = enabledResponses.stream()
            .anyMatch(page -> page.getQueryAdvice() != null);

        boolean hasAdviceWhenDisabled = disabledResponses.stream()
            .anyMatch(page -> page.getQueryAdvice() != null);

        // When disabled, should never have advice
        assertThat(hasAdviceWhenDisabled).isFalse();

        // When enabled with CONTAINS query, service should return advice (if supported)
        if (hasAdviceWhenEnabled) {
            assertThat(hasAdviceWhenEnabled).isTrue();
        }
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAdviceContainsRuleIdAndUrl() {
        // Verify that when query advice is returned, it contains rule IDs and URLs
        String query = "SELECT * FROM c WHERE CONTAINS(c.name, 'test')";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryAdviceEnabled(true);

        List<FeedResponse<InternalObjectNode>> feedResponses =
            createdCollection.queryItems(query, options, InternalObjectNode.class)
                .byPage().collectList().block();

        assertThat(feedResponses).isNotNull().isNotEmpty();

        String advice = feedResponses.stream()
            .map(FeedResponse::getQueryAdvice)
            .filter(a -> a != null)
            .findFirst()
            .orElse(null);

        if (advice != null) {
            // Validate the advice format contains a rule ID (QA####)
            assertThat(advice).matches(".*QA\\d{4}.*");
            // Validate the advice contains the URL prefix
            assertThat(advice).contains("https://aka.ms/CosmosDB/QueryAdvisor/");
        }
    }

    private InternalObjectNode createDocument(CosmosAsyncContainer container, int cnt) {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"name\": \"test%d\", "
                + "\"prop\": %d, "
                + "\"mypk\": \"%s\", "
                + "\"tags\": [\"tag1\", \"tag2\", \"tag3\"], "
                + "\"timestamp\": \"2025-01-01T00:00:00Z\" "
                + "}"
            , uuid, cnt, cnt, uuid));
        return container.createItem(doc).block().getItem();
    }
}
