// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;

import java.util.List;

/**
 * A page of session ids.
 */
public final class ServiceBusSessionIdPage implements ContinuablePage<String, String> {
    private final IterableStream<String> elements;
    private final String continuationToken;

    /**
     * Creates an instance of {@link ServiceBusSessionIdPage}.
     *
     * @param elements The sessions in the page.
     * @param continuationToken The continuation token to get the next page.
     */
    ServiceBusSessionIdPage(List<String> elements, String continuationToken) {
        this.elements = new IterableStream<>(elements);
        this.continuationToken = continuationToken;
    }

    /**
     * Gets an {@link IterableStream} of sessions in the page.
     *
     * @return An {@link IterableStream} containing the sessions in the page.
     */
    @Override
    public IterableStream<String> getElements() {
        return elements;
    }

    /**
     * Gets the reference to the next page of sessions.
     *
     * @return The next page reference.
     */
    @Override
    public String getContinuationToken() {
        return continuationToken;
    }
}
