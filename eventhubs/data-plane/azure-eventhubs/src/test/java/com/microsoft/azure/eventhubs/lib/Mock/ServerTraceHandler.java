/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.lib.Mock;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.SslDomain.Mode;
import org.apache.qpid.proton.reactor.Handshaker;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traces all server events if enabled. used for debugging
 */
public class ServerTraceHandler extends BaseHandler {

    private static final Logger TRACE_LOGGER = Logger.getLogger("servicebus.test.trace");

    public ServerTraceHandler(BaseHandler... handlers) {
        add(new Handshaker());
        for (BaseHandler handler : handlers) {
            add(handler);
        }
    }


    @Override
    public void onUnhandled(Event event) {
        if (TRACE_LOGGER.isLoggable(Level.FINE)) {
            TRACE_LOGGER.log(Level.FINE,
                    "Connection.onUnhandled: name[" + event.getConnection().getHostname() + "] : event[" + event + "]");
        }
        super.onUnhandled(event);
    }

    @Override
    public void onConnectionBound(Event event) {
        Transport transport = event.getTransport();
        SslDomain domain = Proton.sslDomain();
        domain.init(Mode.SERVER);

        domain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
        transport.ssl(domain);

        Sasl sasl = transport.sasl();
        sasl.allowSkip(true);
        sasl.setMechanisms("PLAIN");
        // sasl.done(SaslOutcome.PN_SASL_OK);*/
    }

    @Override
    public void onConnectionRemoteOpen(Event event) {
        super.onConnectionRemoteOpen(event);
        event.getConnection().open();
    }
}
