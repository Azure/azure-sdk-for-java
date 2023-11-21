// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.aad.msal4j.IBroker;
import com.microsoft.aad.msal4jbrokers.Broker;

/**
 * This class is used to create various types of {@link IBroker} objects.
 */
public final class InteractiveBrowserBroker {
    static final ClientLogger LOGGER = new ClientLogger(InteractiveBrowserBroker.class);

    InteractiveBrowserBroker() {
    }

    /**
     * Gets a {@link Broker}.
     * @return the {@link Broker}.
     */
    public static IBroker getMsalRuntimeBroker() {
        Broker.Builder builder = new Broker.Builder();
        builder.supportWindows(true);
        return builder.build();
    }
}
