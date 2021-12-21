// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import java.util.function.Predicate;

/**
 * Maintains the continuation state for a {@link ContinuablePagedFlux} or {@link ContinuablePagedIterable}.
 *
 * @param <C> The type of the continuation token.
 */
class ContinuationState<C> {
    private final Predicate<C> isDonePredicate;

    // The last seen continuation token
    private C lastContinuationToken;
    // Indicate whether to call the PageRetrieval Function
    private boolean isDone;

    /**
     * Creates ContinuationState.
     *
     * @param token An optional continuation token for the beginning state.
     * @param isDonePredicate The predicate that tests if continuation is done.
     */
    ContinuationState(C token, Predicate<C> isDonePredicate) {
        this.lastContinuationToken = token;
        this.isDonePredicate = isDonePredicate;
    }

    /**
     * Store the last seen continuation token.
     * <p>
     * Determination for continuation being done is checking if the continuation token is null.
     *
     * @param token The continuation token.
     */
    void setLastContinuationToken(C token) {
        this.isDone = isDonePredicate.test(token);
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
