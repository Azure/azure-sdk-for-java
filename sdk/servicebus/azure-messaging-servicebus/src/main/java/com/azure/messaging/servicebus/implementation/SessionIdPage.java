// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.util.List;

/**
 * A page of session ids.
 */
public final class SessionIdPage {
    private final List<String> ids;
    private final int skip;

    /**
     * Creates an instance of {@link SessionIdPage}.
     *
     * @param ids the sessions in the page.
     * @param skip the skip value to get the next page.
     */
    SessionIdPage(List<String> ids, int skip) {
        this.skip = skip;
        this.ids = ids;
    }

    /**
     * Gets the list of sessions in the page.
     *
     * @return a list containing the sessions in the page.
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * Gets the skip value to get the next page.
     *
     * @return the skip value.
     */
    public int getSkip() {
        return skip;
    }
}
