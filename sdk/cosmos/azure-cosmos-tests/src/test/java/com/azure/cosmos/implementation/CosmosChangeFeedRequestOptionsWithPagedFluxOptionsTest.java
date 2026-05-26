// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

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
    public void emptyPagesAllowed_isPropagated_whenContinuationTokenSupplied() {
        // GIVEN a CosmosChangeFeedRequestOptions with emptyPagesAllowed=true (the value the Spark connector sets)
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange());
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .setAllowEmptyPages(src, true);

        // AND a continuation token supplied via the paged-flux pull mechanism
        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setRequestContinuation(buildContinuationToken());

        // WHEN computing the effective options
        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        // THEN emptyPagesAllowed must be preserved on the freshly-built impl
        assertThat(ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getAllowEmptyPages(effective))
            .describedAs("emptyPagesAllowed must survive the paged-flux pull continuation rebuild")
            .isTrue();
    }

    @Test(groups = { "unit" })
    public void emptyPagesAllowedFalse_isPropagated_whenContinuationTokenSupplied() {
        // The default value should also round-trip cleanly (sanity check that we're not just hard-coding true).
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange());

        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setRequestContinuation(buildContinuationToken());

        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        assertThat(ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getAllowEmptyPages(effective))
            .describedAs("emptyPagesAllowed default (false) must survive the paged-flux pull continuation rebuild")
            .isFalse();
    }

    @Test(groups = { "unit" })
    public void emptyPagesAllowed_isPreserved_whenNoContinuationTokenSupplied() {
        // No continuation → withCosmosPagedFluxOptions returns `this` unchanged.
        CosmosChangeFeedRequestOptions src = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange());
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .setAllowEmptyPages(src, true);

        CosmosPagedFluxOptions pagedFluxOptions = new CosmosPagedFluxOptions();
        pagedFluxOptions.setMaxItemCount(50);

        CosmosChangeFeedRequestOptions effective = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(src, pagedFluxOptions);

        assertThat(ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .getAllowEmptyPages(effective))
            .isTrue();
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
}
