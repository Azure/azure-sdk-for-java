// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.brokeredauthentication;

import com.microsoft.aad.msal4j.IBroker;
import com.microsoft.aad.msal4jbrokers.MsalRuntimeBroker;

/**
 * This class is used to create various types of {@link IBroker} objects.
 */
public class InteractiveBrowserBroker {

    InteractiveBrowserBroker() {
    }

    /**
     * Gets a {@link MsalRuntimeBroker}.
     * @return the {@link MsalRuntimeBroker}.
     */
    public static IBroker getMsalRuntimeBroker() {
        return new MsalRuntimeBroker();
    }
}
