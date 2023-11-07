// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker;


import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential} configured to use a secure broker.
 *
 * <p><strong>Sample: Construct a {@link InteractiveBrowserCredential} for brokered authentication</strong></p>
 *
 * <p>the following code sample shows to use this type:</p>
 * <!-- src_embed com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct -->
 * <pre>
 * InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder&#40;&#41;;
 * InteractiveBrowserCredential credential = builder.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct -->
 */
public class InteractiveBrowserBrokerCredentialBuilder extends InteractiveBrowserCredentialBuilder {

    /**
     * Sets the parent window handle used by the broker. For use on Windows only.
     * @param windowHandle The window handle of the current application, or 0 for a console application.
     * @return An updated instance of this builder with the interactive browser broker configured.
     */
    public InteractiveBrowserBrokerCredentialBuilder setWindowHandle(long windowHandle) {
        this.identityClientOptions.setBrokerWindowHandle(windowHandle);
        return this;
    }

    /**
     * Enables Microsoft Account (MSA) pass-through. This allows the user to sign in with a Microsoft Account (MSA)
     * instead of a work or school account.
     * @return The updated InteractiveBrowserCredentialBuilder object.
     */
    public InteractiveBrowserBrokerCredentialBuilder enableLegacyMsaPassthrough() {
        this.identityClientOptions.setEnableLegacyMsaPassthrough(true);
        return this;
    }
}
