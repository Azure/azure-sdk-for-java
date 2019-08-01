// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential}.
 *
 * @see InteractiveBrowserCredential
 */
public class InteractiveBrowserCredentialBuilder extends AadCredentialBuilderBase<InteractiveBrowserCredentialBuilder> {
    private int port;

    /**
     * Sets the port for the local HTTP server, for which {@code http://localhost:{port}} must be
     * registered as a valid reply URL on the application.
     *
     * @param port the port on which the credential will listen for the browser authentication result
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public InteractiveBrowserCredentialBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * @return a {@link InteractiveBrowserCredential} with the current configurations.
     */
    public InteractiveBrowserCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("port", port);
            }});
        return new InteractiveBrowserCredential(clientId, port, identityClientOptions);
    }
}
