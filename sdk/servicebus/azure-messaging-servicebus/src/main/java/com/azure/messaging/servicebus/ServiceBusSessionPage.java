// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A page of sessions.
 */
public final class ServiceBusSessionPage implements ContinuablePage<String, ServiceBusSession> {
    private final IterableStream<ServiceBusSession> elements;
    private final String continuationToken;

    /**
     * Creates an instance of {@link ServiceBusSessionPage}.
     *
     * @param elements The sessions in the page.
     * @param continuationToken The continuation token to get the next page.
     */
    ServiceBusSessionPage(List<String> elements, String continuationToken) {
        this.elements
            = new IterableStream<>(elements.stream().map(ServiceBusSession::new).collect(Collectors.toList()));
        this.continuationToken = continuationToken;
    }

    /**
     * Gets an {@link IterableStream} of sessions in the page.
     *
     * @return An {@link IterableStream} containing the sessions in the page.
     */
    @Override
    public IterableStream<ServiceBusSession> getElements() {
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
