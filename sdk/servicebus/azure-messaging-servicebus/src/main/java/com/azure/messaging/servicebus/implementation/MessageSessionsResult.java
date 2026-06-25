// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of a {@link ServiceBusManagementNode#getMessageSessions} call.
 *
 * <p>Wraps the page of session IDs together with the {@code skip} value the service returns for the
 * next page. Track 1's {@code SessionBrowser}/{@code MiscRequestResponseOperationHandler} uses the
 * server-returned skip (rather than {@code previousSkip + page.size()}) as the cursor for the
 * subsequent page, so consumers must propagate it verbatim instead of computing their own.</p>
 *
 * <p><strong>Internal:</strong> this class lives in the {@code implementation} package and is part
 * of the SDK's internal contract, not its public API. It is declared {@code public} only because
 * {@link ServiceBusManagementNode#getMessageSessions} is consumed from a different package; it may
 * change or be removed without notice.</p>
 */
public final class MessageSessionsResult {
    private final List<String> sessionIds;
    private final int nextSkip;

    /**
     * Creates a new result.
     *
     * @param sessionIds Session IDs returned for this page; defensively copied and exposed as an
     *     unmodifiable list so external mutation cannot alter the result after construction.
     * @param nextSkip The {@code skip} value the service returned for the next page request.
     * @throws NullPointerException if {@code sessionIds} is null.
     */
    public MessageSessionsResult(List<String> sessionIds, int nextSkip) {
        Objects.requireNonNull(sessionIds, "'sessionIds' cannot be null.");
        // Snapshot + wrap so callers see an immutable view even if the source list is later mutated.
        this.sessionIds = Collections.unmodifiableList(new java.util.ArrayList<>(sessionIds));
        this.nextSkip = nextSkip;
    }

    /**
     * Gets the session IDs for this page.
     *
     * @return The session IDs (possibly empty when the service has no more results).
     */
    public List<String> getSessionIds() {
        return sessionIds;
    }

    /**
     * Gets the {@code skip} value to send with the next page request.
     *
     * @return The next-page skip value as reported by the service.
     */
    public int getNextSkip() {
        return nextSkip;
    }
}
