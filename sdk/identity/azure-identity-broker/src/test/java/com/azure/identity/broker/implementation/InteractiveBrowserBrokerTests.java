// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker.implementation;

import com.microsoft.aad.msal4j.IBroker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class InteractiveBrowserBrokerTests {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void canLoadMsalRuntime() {

        Assertions.assertDoesNotThrow(() -> {
            IBroker broker = InteractiveBrowserBroker.getMsalRuntimeBroker();
        });
    }

    @Test
    @EnabledOnOs({ OS.MAC, OS.LINUX })
    public void msalRuntimeErrorThrown() {
        Assertions.assertThrows(ExceptionInInitializerError.class, () -> {
            IBroker broker = InteractiveBrowserBroker.getMsalRuntimeBroker();
        });
    }
}
