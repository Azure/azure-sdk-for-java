// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.core.util.paging.PageRetrieverSync;

import java.util.function.Supplier;

/**
 * An {@link Iterable} that supports paging and provides a way to enumerate all sessions.
 */
public final class ServiceBusSessionPagedIterable
    extends ContinuablePagedIterable<String, ServiceBusSession, ServiceBusSessionPage> {
    /**
     * Creates an instance of {@link ServiceBusSessionPagedIterable}.
     *
     * @param provider the supplier that provides the service to retrieve pages of sessions.
     * @param pageSize the number of sessions to include in each page.
     */
    ServiceBusSessionPagedIterable(Supplier<PageRetrieverSync<String, ServiceBusSessionPage>> provider, int pageSize) {
        super(provider, pageSize, null);
    }
}
