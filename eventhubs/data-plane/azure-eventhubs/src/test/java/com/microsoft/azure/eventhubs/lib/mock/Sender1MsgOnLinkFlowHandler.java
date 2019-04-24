// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib.mock;

import com.microsoft.azure.eventhubs.impl.AmqpConstants;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Handshaker;

import java.util.HashMap;
import java.util.Map;

/**
 * Sends 1 Msg on the first onLinkFlow event
 */
public class Sender1MsgOnLinkFlowHandler extends ServerTraceHandler {
    private final Object firstFlow;
    private boolean isFirstFlow;

    public Sender1MsgOnLinkFlowHandler() {
        add(new Handshaker());

        this.firstFlow = new Object();
        this.isFirstFlow = true;
    }

    @Override
    public void onLinkFlow(Event event) {
        if (this.isFirstFlow) {
            synchronized (this.firstFlow) {
                if (this.isFirstFlow) {
                    Sender sender = (Sender) event.getLink();
                    if (sender != null) {
                        byte[] bytes = new byte[5 * 1024];
                        Message msg = Proton.message();
                        Map<String, Object> properties = new HashMap<String, Object>();
                        properties.put("testkey", "testvalue");
                        msg.setApplicationProperties(new ApplicationProperties(properties));
                        Map<Symbol, Object> annotations = new HashMap<Symbol, Object>();
                        annotations.put(AmqpConstants.OFFSET, "11111111");
                        MessageAnnotations msgAnnotation = new MessageAnnotations(annotations);
                        msg.setMessageAnnotations(msgAnnotation);
                        int length = msg.encode(bytes, 0, 4 * 1024);

                        byte[] tag = String.valueOf(1).getBytes();
                        sender.delivery(tag);
                        sender.send(bytes, 0, length);

                        sender.advance();
                        this.isFirstFlow = false;
                    }
                }
            }
        }
    }
}
