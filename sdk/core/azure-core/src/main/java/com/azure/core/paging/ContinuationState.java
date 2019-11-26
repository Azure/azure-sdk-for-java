// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

/**
 * Internal type to store continuation state.
 *
 * @param <C> the Continuation Token type
 */
class ContinuationState<C> {
    // The last seen continuation token
    private C lastContinuationToken;
    // Indicate whether to call the PageRetrieval Function
    private boolean isDone;

    /**
     * Creates ContinuationState.
     *
     * @param token the token to start with
     */
    ContinuationState(C token) {
        this.lastContinuationToken = token;
    }

    /**
     * Store the last seen continuation token.
     *
     * @param token the token
     */
    void setLastContinuationToken(C token) {
        this.isDone = token == null ? true : false;
        this.lastContinuationToken = token;
    }

    /**
     * @return the last seen token
     */
    C getLastContinuationToken() {
        return this.lastContinuationToken;
    }

    /**
     * @return true if the PageRetrieval Function needs to be invoked
     * for next set of pages.
     */
    boolean isDone() {
        return this.isDone;
    }
}
