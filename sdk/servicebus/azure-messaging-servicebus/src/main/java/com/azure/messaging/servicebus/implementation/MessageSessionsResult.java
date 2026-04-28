// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.util.List;

/**
 * Result of a {@link ServiceBusManagementNode#getMessageSessions} call.
 *
 * <p>Wraps the page of session IDs together with the {@code skip} value the service returns for the
 * next page. Track 1's {@code SessionBrowser}/{@code MiscRequestResponseOperationHandler} uses the
 * server-returned skip (rather than {@code previousSkip + page.size()}) as the cursor for the
 * subsequent page, so consumers must propagate it verbatim instead of computing their own.</p>
 */
public final class MessageSessionsResult {
    private final List<String> sessionIds;
    private final int nextSkip;

    /**
     * Creates a new result.
     *
     * @param sessionIds Session IDs returned for this page.
     * @param nextSkip The {@code skip} value the service returned for the next page request.
     */
    public MessageSessionsResult(List<String> sessionIds, int nextSkip) {
        this.sessionIds = sessionIds;
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
