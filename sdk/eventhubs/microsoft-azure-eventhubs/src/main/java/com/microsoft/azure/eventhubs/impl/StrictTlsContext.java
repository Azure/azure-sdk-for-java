// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import java.security.Provider;

/**
 * Context that removes SSLv2Hello protocol from every SSLEngine created.
 */
class StrictTlsContext extends SSLContext {
    /**
     * Creates an SSLContext object.
     *
     * @param contextSpi The service provider for SSL context.
     * @param provider The security provider.
     * @param protocol The SSL protocol.
     */
    protected StrictTlsContext(SSLContextSpi contextSpi, Provider provider, String protocol) {
        super(contextSpi, provider, protocol);
    }
}
