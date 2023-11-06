// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.aad.msal4j.IBroker;
import com.microsoft.aad.msal4jbrokers.Broker;

import java.util.Locale;

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
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (operatingSystem.contains("win")) {
            builder.supportWindows(true);
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Unsupported operating system. See https://aka.ms/azsdk/java/identity/troubleshoot#broker for more information."));
        }
        return builder.build();
    }
}
