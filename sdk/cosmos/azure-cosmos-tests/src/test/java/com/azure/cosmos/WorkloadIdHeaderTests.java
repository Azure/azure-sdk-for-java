// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.FeedRange;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive unit tests for the workload-id / additional headers feature.
 * <p>
 * Covers three layers:
 * <ol>
 *   <li><b>Public API surface</b> — {@link CosmosHeaderName} constants, {@code CosmosClientBuilder.additionalHeaders()},
 *       and that {@code setAdditionalHeaders()} is callable on every request options class.</li>
 *   <li><b>Validation</b> — non-numeric workload-id rejected at builder and request-options levels;
 *       out-of-range values accepted (range enforcement is the backend's responsibility).</li>
 *   <li><b>Internal wiring</b> — headers set via {@code setAdditionalHeaders()} actually reach
 *       {@code RequestOptions.getHeaders()}, which is what {@code RxDocumentClientImpl.getRequestHeaders()}
 *       reads to populate outbound request headers. Covers both data-plane and control-plane classes.</li>
 * </ol>
 * E2E tests (SDK → Gateway/Backend → Jarvis) are in {@code WorkloadIdJarvisValidationTests}.
 */
public class WorkloadIdHeaderTests {

    private static final String WORKLOAD_ID_HEADER = HttpConstants.HttpHeaders.WORKLOAD_ID;
    private static final String TEST_WORKLOAD_ID = "42";

    // ==============================================================================================
    // 1. CosmosHeaderName constants
    // ==============================================================================================

    @Test(groups = { "unit" })
    public void workloadIdHttpHeaderConstant() {
        assertThat(HttpConstants.HttpHeaders.WORKLOAD_ID).isEqualTo("x-ms-cosmos-workload-id");
    }

    @Test(groups = { "unit" })
    public void cosmosHeaderNameWorkloadIdValue() {
        assertThat(CosmosHeaderName.WORKLOAD_ID.getHeaderName()).isEqualTo("x-ms-cosmos-workload-id");
    }

    @Test(groups = { "unit" })
    public void cosmosHeaderNameFromStringResolvesKnownHeader() {
        assertThat(CosmosHeaderName.fromString("x-ms-cosmos-workload-id")).isEqualTo(CosmosHeaderName.WORKLOAD_ID);
    }

    @Test(groups = { "unit" })
    public void cosmosHeaderNameFromStringIsCaseInsensitive() {
        assertThat(CosmosHeaderName.fromString("X-MS-COSMOS-WORKLOAD-ID")).isEqualTo(CosmosHeaderName.WORKLOAD_ID);
    }

    @Test(groups = { "unit" })
    public void cosmosHeaderNameFromStringRejectsUnknownHeader() {
        assertThatThrownBy(() -> CosmosHeaderName.fromString("x-ms-custom-header"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("x-ms-custom-header");
    }

    // ==============================================================================================
    // 2. CosmosClientBuilder — additionalHeaders()
    // ==============================================================================================

    @Test(groups = { "unit" })
    public void builderStoresAdditionalHeaders() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "25");

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers);

        assertThat(builder.getAdditionalHeaders())
            .containsEntry(WORKLOAD_ID_HEADER, "25");
    }

    @Test(groups = { "unit" })
    public void builderHandlesNullAdditionalHeaders() {
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(null);

        assertThat(builder.getAdditionalHeaders()).isNull();
    }

    @Test(groups = { "unit" })
    public void builderHandlesEmptyAdditionalHeaders() {
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(new HashMap<>());

        assertThat(builder.getAdditionalHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void clonedBuilderPreservesAdditionalHeaders() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "25");

        CosmosClientBuilder original = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers);

        CosmosClientBuilder cloned = CosmosBridgeInternal.cloneCosmosClientBuilder(original);

        assertThat(cloned.getAdditionalHeaders())
            .as("cloned builder should preserve additionalHeaders")
            .containsEntry(WORKLOAD_ID_HEADER, "25");
    }

    @Test(groups = { "unit" })
    public void clonedBuilderHandlesNullAdditionalHeaders() {
        CosmosClientBuilder original = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==");

        CosmosClientBuilder cloned = CosmosBridgeInternal.cloneCosmosClientBuilder(original);

        assertThat(cloned.getAdditionalHeaders())
            .as("cloned builder should handle null additionalHeaders")
            .isNull();
    }

    // ==============================================================================================
    // 3. Validation — non-numeric rejected, out-of-range accepted
    // ==============================================================================================

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

    @Test(groups = { "unit" })
    public void nonNumericWorkloadIdRejectedAtItemRequestOptionsLevel() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "abc");

        assertThatThrownBy(() -> new CosmosItemRequestOptions().setAdditionalHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("abc");
    }

    @Test(groups = { "unit" })
    public void nonNumericWorkloadIdRejectedAtBulkExecutionOptionsLevel() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "not-a-number");

        assertThatThrownBy(() -> new CosmosBulkExecutionOptions().setAdditionalHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not-a-number");
    }

    @Test(groups = { "unit" })
    public void nonNumericWorkloadIdRejectedAtDatabaseRequestOptionsLevel() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "not-a-number");

        assertThatThrownBy(() -> new CosmosDatabaseRequestOptions().setAdditionalHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not-a-number");
    }

    /** Range validation [1, 50] is the backend's responsibility — SDK only validates integer format. */
    @Test(groups = { "unit" })
    public void outOfRangeWorkloadIdAcceptedByBuilder() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "51");

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers);

        assertThat(builder.getAdditionalHeaders())
            .containsEntry(WORKLOAD_ID_HEADER, "51");
    }

    // ==============================================================================================
    // 4. Internal wiring — workload-id reaches RequestOptions.getHeaders()
    //    This is the acceptance proof that the header will be on the wire.
    // ==============================================================================================

    /**
     * Data provider covering request options classes that have a no-arg {@code toRequestOptions()}.
     * Includes both data-plane and control-plane classes.
     */
    @DataProvider(name = "requestOptionsWithToRequestOptions")
    public Object[][] requestOptionsWithToRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, TEST_WORKLOAD_ID);

        return new Object[][] {
            // Data-plane
            { "CosmosItemRequestOptions",
                reflectToRequestOptions(new CosmosItemRequestOptions().setAdditionalHeaders(headers)) },
            { "CosmosBatchRequestOptions",
                reflectToRequestOptions(new CosmosBatchRequestOptions().setAdditionalHeaders(headers)) },
            // Control-plane
            { "CosmosDatabaseRequestOptions",
                reflectToRequestOptions(new CosmosDatabaseRequestOptions().setAdditionalHeaders(headers)) },
            { "CosmosContainerRequestOptions",
                reflectToRequestOptions(new CosmosContainerRequestOptions().setAdditionalHeaders(headers)) },
            { "CosmosStoredProcedureRequestOptions",
                reflectToRequestOptions(new CosmosStoredProcedureRequestOptions().setAdditionalHeaders(headers)) },
        };
    }

    @Test(groups = { "unit" }, dataProvider = "requestOptionsWithToRequestOptions")
    public void workloadIdReachesOutboundHeaders(String optionsClassName, RequestOptions requestOptions) {
        assertThat(requestOptions.getHeaders())
            .as("workload-id should reach RequestOptions.getHeaders() for " + optionsClassName)
            .isNotNull()
            .containsEntry(WORKLOAD_ID_HEADER, TEST_WORKLOAD_ID);
    }

    /** CosmosQueryRequestOptions uses a bridge accessor pattern (no simple toRequestOptions()). */
    @Test(groups = { "unit" })
    public void workloadIdReachesQueryRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, TEST_WORKLOAD_ID);

        RequestOptions opts = ImplementationBridgeHelpers
            .CosmosQueryRequestOptionsHelper
            .getCosmosQueryRequestOptionsAccessor()
            .toRequestOptions(new CosmosQueryRequestOptions().setAdditionalHeaders(headers));

        assertThat(opts.getHeaders()).containsEntry(WORKLOAD_ID_HEADER, TEST_WORKLOAD_ID);
    }

    /** CosmosChangeFeedRequestOptions uses a bridge accessor that exposes getHeaders(). */
    @Test(groups = { "unit" })
    public void workloadIdReachesChangeFeedRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, TEST_WORKLOAD_ID);

        Map<String, String> extracted = ImplementationBridgeHelpers
            .CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getHeaders(
                CosmosChangeFeedRequestOptions
                    .createForProcessingFromBeginning(FeedRange.forFullRange())
                    .setAdditionalHeaders(headers));

        assertThat(extracted).containsEntry(WORKLOAD_ID_HEADER, TEST_WORKLOAD_ID);
    }

    /** CosmosReadManyRequestOptions uses getImpl().applyToRequestOptions(). */
    @Test(groups = { "unit" })
    public void workloadIdReachesReadManyRequestOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, TEST_WORKLOAD_ID);

        RequestOptions opts = ImplementationBridgeHelpers
            .CosmosReadManyRequestOptionsHelper
            .getCosmosReadManyRequestOptionsAccessor()
            .getImpl(new CosmosReadManyRequestOptions().setAdditionalHeaders(headers))
            .applyToRequestOptions(new RequestOptions());

        assertThat(opts.getHeaders()).containsEntry(WORKLOAD_ID_HEADER, TEST_WORKLOAD_ID);
    }

    /** CosmosBulkExecutionOptions delegates to an internal CosmosBatchRequestOptions. */
    @Test(groups = { "unit" })
    public void workloadIdReachesBulkExecutionOptions() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, TEST_WORKLOAD_ID);

        Map<String, String> extracted = reflectGetHeaders(
            new CosmosBulkExecutionOptions().setAdditionalHeaders(headers));

        assertThat(extracted).containsEntry(WORKLOAD_ID_HEADER, TEST_WORKLOAD_ID);
    }

    // ==============================================================================================
    // 5. Null/empty additionalHeaders does not inject workload-id into outbound headers
    // ==============================================================================================

    @Test(groups = { "unit" })
    public void nullAdditionalHeadersDoesNotInjectWorkloadId() {
        assertNoWorkloadId(reflectToRequestOptions(new CosmosItemRequestOptions().setAdditionalHeaders(null)));
        assertNoWorkloadId(reflectToRequestOptions(new CosmosDatabaseRequestOptions().setAdditionalHeaders(null)));
        assertNoWorkloadId(reflectToRequestOptions(new CosmosContainerRequestOptions().setAdditionalHeaders(null)));
        assertNoWorkloadId(reflectToRequestOptions(new CosmosStoredProcedureRequestOptions().setAdditionalHeaders(null)));
    }

    @Test(groups = { "unit" })
    public void emptyAdditionalHeadersDoesNotInjectWorkloadId() {
        Map<CosmosHeaderName, String> empty = new HashMap<>();
        assertNoWorkloadId(reflectToRequestOptions(new CosmosItemRequestOptions().setAdditionalHeaders(empty)));
        assertNoWorkloadId(reflectToRequestOptions(new CosmosDatabaseRequestOptions().setAdditionalHeaders(empty)));
        assertNoWorkloadId(reflectToRequestOptions(new CosmosContainerRequestOptions().setAdditionalHeaders(empty)));
        assertNoWorkloadId(reflectToRequestOptions(new CosmosStoredProcedureRequestOptions().setAdditionalHeaders(empty)));
    }

    // ==============================================================================================
    // 6. Coverage matrix guard — setAdditionalHeaders() exists on all expected classes
    // ==============================================================================================

    /** Guard against accidental removal of setAdditionalHeaders() from any request options class. */
    @Test(groups = { "unit" })
    public void allExpectedRequestOptionsClassesSupportAdditionalHeaders() {
        Class<?>[] expectedClasses = {
            // Data-plane
            CosmosItemRequestOptions.class,
            CosmosBatchRequestOptions.class,
            CosmosBulkExecutionOptions.class,
            CosmosQueryRequestOptions.class,
            CosmosReadManyRequestOptions.class,
            CosmosChangeFeedRequestOptions.class,
            // Control-plane
            CosmosDatabaseRequestOptions.class,
            CosmosContainerRequestOptions.class,
            CosmosStoredProcedureRequestOptions.class,
        };

        for (Class<?> clazz : expectedClasses) {
            assertThat(hasSetAdditionalHeaders(clazz))
                .as(clazz.getSimpleName() + " should have setAdditionalHeaders()")
                .isTrue();
        }
    }

    // ==============================================================================================
    // Helpers
    // ==============================================================================================

    private static RequestOptions reflectToRequestOptions(Object cosmosRequestOptions) {
        try {
            java.lang.reflect.Method m = cosmosRequestOptions.getClass().getDeclaredMethod("toRequestOptions");
            m.setAccessible(true);
            return (RequestOptions) m.invoke(cosmosRequestOptions);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to call toRequestOptions() on " + cosmosRequestOptions.getClass().getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> reflectGetHeaders(Object cosmosRequestOptions) {
        try {
            java.lang.reflect.Method m = cosmosRequestOptions.getClass().getDeclaredMethod("getHeaders");
            m.setAccessible(true);
            return (Map<String, String>) m.invoke(cosmosRequestOptions);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to call getHeaders() on " + cosmosRequestOptions.getClass().getSimpleName(), e);
        }
    }

    private static void assertNoWorkloadId(RequestOptions options) {
        Map<String, String> headers = options.getHeaders();
        if (headers != null) {
            assertThat(headers).doesNotContainKey(WORKLOAD_ID_HEADER);
        }
    }

    private static boolean hasSetAdditionalHeaders(Class<?> clazz) {
        try {
            clazz.getMethod("setAdditionalHeaders", Map.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}

