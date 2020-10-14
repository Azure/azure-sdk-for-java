// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import java.security.Provider;

/**
 * Context that removes SSLv2Hello protocol from every SSLEngine created.
 */
<<<<<<< HEAD
public class StrictTlsContext extends SSLContext {
=======
class StrictTlsContext extends SSLContext {
>>>>>>> 95a27a56ad7e94c066c6b4113935ad5901940c61
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
