package com.microsoft.azure.eventhubs.common;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.Event.Type;
import org.apache.qpid.proton.message.Message;

public class SendLinkHandler extends BaseHandler {

	private static final Logger TRACE_LOGGER = Logger.getLogger("eventhub.trace");
	
	private final String name;
	private final MessageSender msgSender;
	
	SendLinkHandler(final String name, final MessageSender sender) {
		this.name = name;
		this.msgSender = sender;
	}

	@Override
    public void onUnhandled(Event event) {
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
            TRACE_LOGGER.log(Level.FINE, "sendLink.onUnhandled: name[" + event.getLink().getName() + "] : event["+event+"]");
        }		
	}

	@Override
    public void onDelivery(Event event) {
		
		Sender sender = (Sender) event.getLink();
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
            TRACE_LOGGER.log(Level.FINE, "sendLink.onDelivery: name["+sender.getName()+"] : unsettled[" + sender.getUnsettled() + "] : credit[" + sender.getCredit()+ "]");
        }
		
		Delivery delivery = event.getDelivery();
		if (delivery != null) {
			msgSender.onSendComplete(delivery.getTag(), delivery.getRemoteState());
			delivery.settle();
		}
	}

	@Override
    public void onLinkRemoteClose(Event event) {
		Link link = event.getLink();
        if (link instanceof Sender) {
        	ErrorCondition condition = link.getRemoteCondition();
    		if (condition != null) {
                System.err.println(String.format("LinkError(name: %s): " + condition.getDescription(), this.name));
            } else {
                System.err.println("LinkError (no description returned).");
            }
        }
	}
}
