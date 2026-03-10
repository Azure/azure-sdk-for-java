// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the additional headers (workload-id) feature on CosmosClientBuilder and request options classes.
 * <p>
 * These tests verify the public API surface: builder fluent methods, getter behavior,
 * null/empty handling, CosmosHeaderName enum, and that setAdditionalHeaders() is publicly accessible
 * on all request options classes.
 */
public class AdditionalHeadersTests {

    /**
     * Verifies that additional headers (e.g., workload-id) set via CosmosClientBuilder.additionalHeaders()
     * are stored correctly and retrievable via getAdditionalHeaders().
     */
    @Test(groups = { "unit" })
    public void additionalHeadersSetOnBuilder() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "25");

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers);

        assertThat(builder.getAdditionalHeaders())
            .containsEntry("x-ms-cosmos-workload-id", "25");
    }

    /**
     * Verifies that passing null to additionalHeaders() does not throw and that
     * getAdditionalHeaders() returns null, ensuring graceful null handling.
     */
    @Test(groups = { "unit" })
    public void additionalHeadersNullHandledGracefully() {
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(null);

        assertThat(builder.getAdditionalHeaders()).isNull();
    }

    /**
     * Verifies that passing an empty map to additionalHeaders() is accepted and
     * getAdditionalHeaders() returns an empty (not null) map.
     */
    @Test(groups = { "unit" })
    public void additionalHeadersEmptyMapHandled() {
        Map<CosmosHeaderName, String> emptyHeaders = new HashMap<>();

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(emptyHeaders);

        assertThat(builder.getAdditionalHeaders()).isEmpty();
    }

    /**
     * Verifies that CosmosHeaderName.WORKLOAD_ID maps to the correct header string.
     */
    @Test(groups = { "unit" })
    public void cosmosHeaderNameWorkloadIdValue() {
        assertThat(CosmosHeaderName.WORKLOAD_ID.getHeaderName())
            .isEqualTo("x-ms-cosmos-workload-id");
    }

    /**
     * Verifies that CosmosHeaderName.fromString() resolves known header strings to the
     * correct enum value. This is used by the Spark connector to convert config strings
     * to enum keys.
     */
    @Test(groups = { "unit" })
    public void cosmosHeaderNameFromStringResolvesKnownHeader() {
        CosmosHeaderName name = CosmosHeaderName.fromString("x-ms-cosmos-workload-id");
        assertThat(name).isEqualTo(CosmosHeaderName.WORKLOAD_ID);
    }

    /**
     * Verifies that CosmosHeaderName.fromString() is case-insensitive.
     */
    @Test(groups = { "unit" })
    public void cosmosHeaderNameFromStringIsCaseInsensitive() {
        CosmosHeaderName name = CosmosHeaderName.fromString("X-MS-COSMOS-WORKLOAD-ID");
        assertThat(name).isEqualTo(CosmosHeaderName.WORKLOAD_ID);
    }

    /**
     * Verifies that CosmosHeaderName.fromString() throws IllegalArgumentException
     * for unknown header strings. This is the runtime equivalent of the compile-time
     * safety provided by the enum — used when converting from Spark JSON config.
     */
    @Test(groups = { "unit" })
    public void cosmosHeaderNameFromStringRejectsUnknownHeader() {
        assertThatThrownBy(() -> CosmosHeaderName.fromString("x-ms-custom-header"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("x-ms-custom-header")
            .hasMessageContaining("Unknown header");
    }

    /**
     * Verifies that setAdditionalHeaders() is publicly accessible on CosmosItemRequestOptions
     * and supports fluent chaining for per-request header overrides on CRUD operations.
     */
    @Test(groups = { "unit" })
    public void setAdditionalHeadersOnItemRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "15");

        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setAdditionalHeaders(headers);

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setAdditionalHeaders() is publicly accessible on CosmosBatchRequestOptions
     * and supports fluent chaining for per-request header overrides on batch operations.
     */
    @Test(groups = { "unit" })
    public void setAdditionalHeadersOnBatchRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "20");

        CosmosBatchRequestOptions options = new CosmosBatchRequestOptions()
            .setAdditionalHeaders(headers);

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setAdditionalHeaders() is publicly accessible on CosmosChangeFeedRequestOptions
     * and supports fluent chaining for per-request header overrides on change feed operations.
     */
    @Test(groups = { "unit" })
    public void setAdditionalHeadersOnChangeFeedRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "25");

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange())
            .setAdditionalHeaders(headers);

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setAdditionalHeaders() is publicly accessible on CosmosBulkExecutionOptions
     * and supports fluent chaining for per-request header overrides on bulk ingestion operations.
     */
    @Test(groups = { "unit" })
    public void setAdditionalHeadersOnBulkExecutionOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "30");

        CosmosBulkExecutionOptions options = new CosmosBulkExecutionOptions()
            .setAdditionalHeaders(headers);

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setAdditionalHeaders() is publicly accessible on CosmosQueryRequestOptions
     * and supports fluent chaining for per-request header overrides on query operations.
     */
    @Test(groups = { "unit" })
    public void setAdditionalHeadersOnQueryRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "35");

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
            .setAdditionalHeaders(headers);

        assertThat(options).isNotNull();
    }

    /**
     * Verifies that setAdditionalHeaders() is publicly accessible on CosmosReadManyRequestOptions
     * and supports fluent chaining for per-request header overrides on read-many operations.
     */
    @Test(groups = { "unit" })
    public void setAdditionalHeadersOnReadManyRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "40");

        CosmosReadManyRequestOptions options = new CosmosReadManyRequestOptions()
            .setAdditionalHeaders(headers);

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

    /**
     * Verifies that a non-numeric workload-id value is rejected at builder level with
     * IllegalArgumentException. This covers both Gateway and Direct modes consistently
     * (unlike RntbdRequestHeaders.addWorkloadId() which only covers Direct mode).
     */
    @Test(groups = { "unit" })
    public void nonNumericWorkloadIdRejectedAtBuilderLevel() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "abc");

        assertThatThrownBy(() -> new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("abc")
            .hasMessageContaining("valid integer");
    }

    /**
     * Verifies that out-of-range workload-id values (e.g., 51) are accepted by the SDK.
     * Range validation [1, 50] is the backend's responsibility — the SDK only validates
     * that the value is a valid integer. This avoids hardcoding a range the backend team
     * might change in the future.
     */
    @Test(groups = { "unit" })
    public void outOfRangeWorkloadIdAcceptedByBuilder() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "51");

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers);

        assertThat(builder.getAdditionalHeaders())
            .containsEntry(HttpConstants.HttpHeaders.WORKLOAD_ID, "51");
    }

    /**
     * Verifies that a non-numeric workload-id value is rejected at request-options level
     * (CosmosItemRequestOptions) with IllegalArgumentException, ensuring validation is
     * symmetric with the builder level.
     */
    @Test(groups = { "unit" })
    public void nonNumericWorkloadIdRejectedAtItemRequestOptionsLevel() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "abc");

        assertThatThrownBy(() -> new CosmosItemRequestOptions()
            .setAdditionalHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("abc")
            .hasMessageContaining("valid integer");
    }

    /**
     * Verifies that a non-numeric workload-id value is rejected at request-options level
     * (CosmosBulkExecutionOptions) with IllegalArgumentException, ensuring validation is
     * symmetric with the builder level.
     */
    @Test(groups = { "unit" })
    public void nonNumericWorkloadIdRejectedAtBulkExecutionOptionsLevel() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "not-a-number");

        assertThatThrownBy(() -> new CosmosBulkExecutionOptions()
            .setAdditionalHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not-a-number")
            .hasMessageContaining("valid integer");
    }

    /**
     * Verifies that a valid workload-id value passes builder validation.
     */
    @Test(groups = { "unit" })
    public void validWorkloadIdAcceptedByBuilder() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "15");

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers);

        assertThat(builder.getAdditionalHeaders())
            .containsEntry(HttpConstants.HttpHeaders.WORKLOAD_ID, "15");
    }
}

