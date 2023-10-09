// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker;

import com.azure.identity.broker.implementation.InteractiveBrowserBroker;
import com.microsoft.aad.msal4j.IBroker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InteractiveBrowserBrokerTests {

    @Test
    public void canLoadMsalRuntime() {

        Assertions.assertDoesNotThrow(() -> {
            IBroker broker = InteractiveBrowserBroker.getMsalRuntimeBroker();
        });
    }
}
