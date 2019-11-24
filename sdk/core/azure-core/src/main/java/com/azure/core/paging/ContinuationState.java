package com.azure.core.paging;

/**
 * Internal type to store continuation state.
 *
 * @param <C> the Continuation Token type
 */
class ContinuationState<C> {
    private C lastContinuationToken;
    private boolean isDone;

    ContinuationState(C token) {
        this.lastContinuationToken = token;
    }

    void setLastContinuationToken(C token) {
        this.isDone = token == null ? true : false;
        this.lastContinuationToken = token;
    }

    C getLastContinuationToken() {
        return this.lastContinuationToken;
    }

    boolean isDone() {
        return this.isDone;
    }
}
