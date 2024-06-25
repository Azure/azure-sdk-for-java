// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.paging;

import java.util.function.Predicate;

/**
 * Maintains the continuation state for a {@link ContinuablePagedIterable}.
 *
 * @param <C> The type of the continuation token.
 */
class ContinuationState<C> {
    private final Predicate<C> continuationPredicate;

    // The last seen continuation token
    private C lastContinuationToken;
    // Indicate whether to call the PageRetrieval Function
    private boolean isDone;

    /**
     * Creates ContinuationState.
     *
     * @param token An optional continuation token for the beginning state.
     * @param continuationPredicate The predicate that tests if paging should continue.
     */
    ContinuationState(C token, Predicate<C> continuationPredicate) {
        this.lastContinuationToken = token;
        this.continuationPredicate = continuationPredicate;
    }

    /**
     * Store the last seen continuation token.
     * <p>
     * Determining if paging should continue is done by checking the token against the continuation predicate.
     *
     * @param token The continuation token.
     */
    void setLastContinuationToken(C token) {
        this.isDone = !continuationPredicate.test(token);
        this.lastContinuationToken = token;
    }

    /**
     * Gets the last continuation token that has been seen.
     *
     * @return The last continuation token.
     */
    C getLastContinuationToken() {
        return this.lastContinuationToken;
    }

    /**
     * Gets whether continuation is done.
     *
     * @return A flag determining if continuation is done.
     */
    boolean isDone() {
        return this.isDone;
    }
}
