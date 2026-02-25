// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the custom headers (workload-id) feature on CosmosClientBuilder and request options classes.
 * <p>
 * These tests verify the public API surface: builder fluent methods, getter behavior,
 * null/empty handling, and that setHeader() is publicly accessible on all request options classes.
 */
public class CustomHeadersTests {

    /**
     * Verifies that custom headers (e.g., workload-id) set via CosmosClientBuilder.customHeaders()
     * are stored correctly and retrievable via getCustomHeaders().
     */
    @Test(groups = { "unit" })
    public void customHeadersSetOnBuilder() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-cosmos-workload-id", "25");

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .customHeaders(headers);

        assertThat(builder.getCustomHeaders()).containsEntry("x-ms-cosmos-workload-id", "25");
    }

    /**
     * Verifies that passing null to customHeaders() does not throw and that
     * getCustomHeaders() returns null, ensuring graceful null handling.
     */
    @Test(groups = { "unit" })
    public void customHeadersNullHandledGracefully() {
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .customHeaders(null);

        assertThat(builder.getCustomHeaders()).isNull();
    }

    /**
     * Verifies that passing an empty map to customHeaders() is accepted and
     * getCustomHeaders() returns an empty (not null) map.
     */
    @Test(groups = { "unit" })
    public void customHeadersEmptyMapHandled() {
        Map<String, String> emptyHeaders = new HashMap<>();

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .customHeaders(emptyHeaders);

        assertThat(builder.getCustomHeaders()).isEmpty();
    }

    /**
     * Verifies that multiple custom headers can be set at once on the builder and
     * all entries are preserved and retrievable with correct keys and values.
     */
    @Test(groups = { "unit" })
    public void multipleCustomHeadersSupported() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-cosmos-workload-id", "15");
        headers.put("x-ms-custom-header", "value");

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .customHeaders(headers);

        assertThat(builder.getCustomHeaders()).hasSize(2);
        assertThat(builder.getCustomHeaders()).containsEntry("x-ms-cosmos-workload-id", "15");
        assertThat(builder.getCustomHeaders()).containsEntry("x-ms-custom-header", "value");
    }

    /**
     * Verifies that setHeader() is publicly accessible on CosmosItemRequestOptions
     * (previously package-private) and supports fluent chaining for per-request
     * header overrides on CRUD operations.
     */
    @Test(groups = { "unit" })
    public void setHeaderOnItemRequestOptionsIsPublic() {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setHeader("x-ms-cosmos-workload-id", "15");

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setHeader() is publicly accessible on CosmosBatchRequestOptions
     * (previously package-private) and supports fluent chaining for per-request
     * header overrides on batch operations.
     */
    @Test(groups = { "unit" })
    public void setHeaderOnBatchRequestOptionsIsPublic() {
        CosmosBatchRequestOptions options = new CosmosBatchRequestOptions()
            .setHeader("x-ms-cosmos-workload-id", "20");

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setHeader() is publicly accessible on CosmosChangeFeedRequestOptions
     * (previously package-private) and supports fluent chaining for per-request
     * header overrides on change feed operations.
     */
    @Test(groups = { "unit" })
    public void setHeaderOnChangeFeedRequestOptionsIsPublic() {
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange())
            .setHeader("x-ms-cosmos-workload-id", "25");

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setHeader() is publicly accessible on CosmosBulkExecutionOptions
     * (previously package-private) and supports fluent chaining for per-request
     * header overrides on bulk ingestion operations.
     */
    @Test(groups = { "unit" })
    public void setHeaderOnBulkExecutionOptionsIsPublic() {
        CosmosBulkExecutionOptions options = new CosmosBulkExecutionOptions()
            .setHeader("x-ms-cosmos-workload-id", "30");

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that the new delegating setHeader() method on CosmosQueryRequestOptions
     * is publicly accessible and supports fluent chaining for per-request header
     * overrides on query operations.
     */
    @Test(groups = { "unit" })
    public void setHeaderOnQueryRequestOptionsIsPublic() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
            .setHeader("x-ms-cosmos-workload-id", "35");

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that the WORKLOAD_ID constant in HttpConstants.HttpHeaders is defined
     * with the correct canonical header name "x-ms-cosmos-workload-id" as expected
     * by the Cosmos DB service.
     */
    @Test(groups = { "unit" })
    public void workloadIdHttpHeaderConstant() {
        assertThat(HttpConstants.HttpHeaders.WORKLOAD_ID).isEqualTo("x-ms-cosmos-workload-id");
    }
}
