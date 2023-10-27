// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker;


import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential} configured to use a secure broker.
 *
 */
public class InteractiveBrowserBrokerCredentialBuilder extends InteractiveBrowserCredentialBuilder {

    /**
     * For Windows, sets the parent window handle used by the broker.
     * @param windowHandle The window handle of the current application, or 0 for a console application.
     * @return An updated instance of this builder with the interactive browser broker configured.
     */
    public InteractiveBrowserBrokerCredentialBuilder setWindowHandle(long windowHandle) {
        this.identityClientOptions.setBrokerWindowHandle(windowHandle);
        return this;
    }

    /**
     * Enables Microsoft Account (MSA) passthrough.
     * @return The updated InteractiveBrowserCredentialBuilder object.
     */
    public InteractiveBrowserBrokerCredentialBuilder enableLegacyMsaPassthrough() {
        this.identityClientOptions.setEnableLegacyMsaPassthrough(true);
        return this;
    }
}
