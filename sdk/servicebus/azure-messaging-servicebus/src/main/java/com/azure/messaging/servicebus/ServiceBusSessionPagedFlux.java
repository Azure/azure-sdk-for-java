// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Supplier;

/**
 * A {@link reactor.core.publisher.Flux} that supports paging and provides a way to enumerate all sessions.
 */
public final class ServiceBusSessionPagedFlux
    extends ContinuablePagedFluxCore<String, ServiceBusSession, ServiceBusSessionPage> {
    /**
     * Creates an instance of {@link ServiceBusSessionPagedFlux}.
     *
     * @param provider the supplier that provides the service to retrieve pages of sessions.
     * @param pageSize the number of sessions to include in each page.
     */
    ServiceBusSessionPagedFlux(Supplier<PageRetriever<String, ServiceBusSessionPage>> provider, int pageSize) {
        super(provider, pageSize);
    }
}
