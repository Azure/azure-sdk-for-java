// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the paged-flux pull continuation path on
 * {@link CosmosChangeFeedRequestOptions#withCosmosPagedFluxOptions(CosmosPagedFluxOptions)} (package-visible via
 * {@link ModelBridgeInternal#getEffectiveChangeFeedRequestOptions(CosmosChangeFeedRequestOptions, CosmosPagedFluxOptions)}).
 *
 * <p>That method silently builds a brand-new {@code CosmosChangeFeedRequestOptionsImpl} when the caller supplies a
 * continuation token via {@link CosmosPagedFluxOptions}, so any field NOT explicitly copied is dropped. These tests
 * lock in the propagation of fields whose loss would silently break a feature.
 */
public class CosmosChangeFeedRequestOptionsWithPagedFluxOptionsTest {

    @Test(groups = { "unit" })
    public void endLSN_isPropagated_whenContinuationTokenSupplied() {
        // Locks in the bounded-change-feed contract across a byPage(savedContinuation) round-trip:
        // a caller who set endLSN=42 must continue to see iteration bounded by LSN 42 after resume.
        // Before the inheritNonContinuationFieldsFrom fix, endLSN was silently dropped on the rebuild
        // path, turning a bounded change feed into an unbounded one.
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange());
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .setEndLSN(src, 42L);

        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setRequestContinuation(buildContinuationToken());

        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        assertThat(ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getEndLSN(effective))
            .describedAs("endLSN must survive the paged-flux pull continuation rebuild")
            .isEqualTo(42L);
    }

    @Test(groups = { "unit" })
    public void customSerializer_isPropagated_whenContinuationTokenSupplied() {
        // Locks in custom-serializer preservation across a byPage(savedContinuation) round-trip:
        // a caller who registered a custom CosmosItemSerializer must continue to see items
        // deserialized through that serializer after resume. Before the inheritNonContinuationFieldsFrom
        // fix, the customSerializer was silently dropped on the rebuild path, falling back to the
        // SDK's internal default serializer and potentially producing wrong field values.
        CosmosItemSerializer sentinel = new CosmosItemSerializer() {
            @Override
            public <T> java.util.Map<String, Object> serialize(T item) { return null; }

            @Override
            public <T> T deserialize(java.util.Map<String, Object> jsonNodeMap, Class<T> classType) { return null; }
        };
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange());
        src.setCustomItemSerializer(sentinel);

        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setRequestContinuation(buildContinuationToken());

        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        assertThat(effective.getCustomItemSerializer())
            .describedAs("customSerializer must survive the paged-flux pull continuation rebuild")
            .isSameAs(sentinel);
    }

    @Test(groups = { "unit" })
    public void tokenEncodedFields_overrideCallerSuppliedValues_whenContinuationTokenSupplied() {
        // Negative pin: the four token-encoded fields (continuationState, feedRangeInternal, mode,
        // startFromInternal) MUST come from the token, not from the caller's pre-resume options.
        // The caller's options here have continuationState=null (createForProcessingFromBeginning),
        // but the resulting effective options must have a non-null continuationState parsed from
        // the supplied token. If a future refactor accidentally inherits the token-encoded fields
        // from the source impl (e.g. moving them into inheritNonContinuationFieldsFrom), this test
        // catches the regression because the source's continuationState would clobber the token's.
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange());

        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setRequestContinuation(buildContinuationToken());

        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        assertThat(ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getImpl(effective)
            .getContinuation())
            .describedAs("continuationState is encoded in the token and MUST override the caller's pre-resume value")
            .isNotNull();
    }

    @Test(groups = { "unit" })
    public void fullFidelityWireFormatHeader_isPreserved_whenSourceHasNoCustomHeaders() {
        // Reviewer-found bug: inheritNonContinuationFieldsFrom used to do
        // `this.customOptions = source.customOptions`, which clobbered the
        // CHANGE_FEED_WIRE_FORMAT_VERSION header set by the constructor when mode==FULL_FIDELITY.
        // If the source's customOptions is null (typical for callers who only set high-level
        // options), the resume would produce a FULL_FIDELITY request WITHOUT the required wire
        // format header. The merge-don't-clobber fix preserves token-mode-derived headers.
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRangeEpkImpl.forFullRange());

        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setRequestContinuation(buildFullFidelityContinuationToken());

        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        java.util.Map<String, String> headers = ImplementationBridgeHelpers
            .CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getHeaders(effective);

        assertThat(headers)
            .describedAs("token-derived FULL_FIDELITY wire format header must survive the rebuild")
            .isNotNull()
            .containsEntry(
                HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION,
                HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);
    }

    @Test(groups = { "unit" })
    public void callerSuppliedCustomHeaders_areMergedWith_tokenDerivedHeaders() {
        // Companion to the above: when the source HAS its own custom headers AND the token's
        // mode triggers constructor-set headers, both must coexist after inherit. The merge
        // semantics: token-mode headers win on key collision; source headers are added otherwise.
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRangeEpkImpl.forFullRange());
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .setHeader(src, "X-Caller-Header", "caller-value");

        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setRequestContinuation(buildFullFidelityContinuationToken());

        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        java.util.Map<String, String> headers = ImplementationBridgeHelpers
            .CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getHeaders(effective);

        assertThat(headers)
            .describedAs("both caller-supplied and token-mode-derived headers must be present")
            .containsEntry("X-Caller-Header", "caller-value")
            .containsEntry(
                HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION,
                HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);
    }

    private static String buildContinuationToken() {
        // Build a real ChangeFeedState so we can serialize a valid (base64-encoded) continuation token.
        // We use the state's own toString() which round-trips through createForProcessingFromContinuation.
        ChangeFeedStateV1 state = new ChangeFeedStateV1(
            "someContainerRid",
            FeedRangeEpkImpl.forFullRange(),
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromBeginning(),
            null);
        return state.toString();
    }

    private static String buildFullFidelityContinuationToken() {
        ChangeFeedStateV1 state = new ChangeFeedStateV1(
            "someContainerRid",
            FeedRangeEpkImpl.forFullRange(),
            ChangeFeedMode.FULL_FIDELITY,
            ChangeFeedStartFromInternal.createFromNow(),
            null);
        return state.toString();
    }
}
