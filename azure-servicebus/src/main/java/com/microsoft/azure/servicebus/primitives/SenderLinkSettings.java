package com.microsoft.azure.servicebus.primitives;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transport.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;

import java.util.Map;

class SenderLinkSettings {
    String linkName;
    String linkPath;
    Source source;
    Target target;
    Map<Symbol, Object> linkProperties;
    SenderSettleMode settleMode;
    boolean requiresAuthentication;
}