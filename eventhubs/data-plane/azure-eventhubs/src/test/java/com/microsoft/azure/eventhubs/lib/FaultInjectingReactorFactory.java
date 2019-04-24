// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.impl.CustomIOHandler;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.impl.ReactorHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;

public class FaultInjectingReactorFactory extends MessagingFactory.ReactorFactory {

    private volatile FaultType faultType;

    public void setFaultType(final FaultType faultType) {
        this.faultType = faultType;
    }

    @Override
    public Reactor create(final ReactorHandler reactorHandler, final int maxFrameSize) throws IOException {
        final Reactor reactor = Proton.reactor(reactorHandler);

        switch (this.faultType) {
            case NetworkOutage:
                reactor.setGlobalHandler(new NetworkOutageSimulator());
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return reactor;
    }

    public enum FaultType {
        NetworkOutage
    }

    public static final class NetworkOutageSimulator extends CustomIOHandler {

        @Override
        public void onUnhandled(final Event event) {
            switch (event.getType()) {
                case CONNECTION_BOUND:
                    this.handleBound(event);
                    break;
                default:
                    super.onUnhandled(event);
            }
        }

        private void handleBound(final Event event) {
            final Transport transport = event.getConnection().getTransport();
            final ErrorCondition condition = new ErrorCondition();
            condition.setCondition(Symbol.getSymbol("proton:io"));
            condition.setDescription("induced fault");
            transport.setCondition(condition);
            transport.close_tail();
            transport.close_head();
            transport.pop(Math.max(0, transport.pending()));

            this.selectableTransport(event.getReactor(), null, transport);
        }
    }
}
