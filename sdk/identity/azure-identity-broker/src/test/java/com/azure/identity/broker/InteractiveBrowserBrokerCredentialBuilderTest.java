//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.

package com.azure.identity.broker;

import com.azure.identity.InteractiveBrowserCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;



class InteractiveBrowserBrokerCredentialBuilderTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void canEnableLegacyMsa() {

        Assertions.assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.enableLegacyMsaPassthrough();
            InteractiveBrowserCredential credential = builder.build();
        });
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void canSetWindowHandle() {} {
        Assertions.assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.setWindowHandle(1L);
            InteractiveBrowserCredential credential = builder.build();
        });
    }
}
