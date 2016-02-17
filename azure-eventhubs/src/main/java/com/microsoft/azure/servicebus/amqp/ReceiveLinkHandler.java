package com.microsoft.azure.servicebus.amqp;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.microsoft.azure.servicebus.ClientConstants;
import com.microsoft.azure.servicebus.MessageReceiver;

/** 
 * ServiceBus <-> ProtonReactor interaction 
 * handles all recvLink - reactor events
 */
public final class ReceiveLinkHandler extends BaseLinkHandler
{
	private final MessageReceiver msgReceiver;
	private final Object firstResponse;
	private boolean isFirstResponse;
	
	public ReceiveLinkHandler(final MessageReceiver receiver)
	{
		this.msgReceiver = receiver;
		this.firstResponse = new Object();
		this.isFirstResponse = true;
	}
	
	@Override
    public void onLinkLocalOpen(Event evt)
	{
        Link link = evt.getLink();
        if (link instanceof Receiver)
        {
            Receiver receiver = (Receiver) link;
            
            if(TRACE_LOGGER.isLoggable(Level.FINE))
            {
            	TRACE_LOGGER.log(Level.FINE,
            			String.format("linkName[%s], initialCredit[%s]", receiver.getName(), receiver.getCredit()));
            }
        }
    }
	
	@Override
	public void onLinkRemoteOpen(Event event)
	{
		Link link = event.getLink();
        if (link != null && link instanceof Receiver)
        {
        	Receiver receiver = (Receiver) link;
        	if (link.getRemoteSource() != null)
        	{
        		if(TRACE_LOGGER.isLoggable(Level.FINE))
                {
                	TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "linkName[%s], remoteSource[%s]", receiver.getName(), link.getRemoteSource()));
                }
        		
        		synchronized (this.firstResponse)
        		{
					this.isFirstResponse = false;
	        		this.msgReceiver.onOpenComplete(null);
        		}
        	}
        	else
        	{
        		if(TRACE_LOGGER.isLoggable(Level.FINE))
                {
                	TRACE_LOGGER.log(Level.FINE,
                			String.format(Locale.US, "linkName[%s], remoteTarget[null], remoteSource[null], action[waitingForError]", receiver.getName()));
                }
        	}
        	
        	if (receiver.getCredit() < this.msgReceiver.getPrefetchCount())
        	{
        		receiver.flow(this.msgReceiver.getPrefetchCount() - receiver.getCredit());
        	}
        }
	}
	
	@Override
    public void onLinkRemoteClose(Event event)
	{
		Link link = event.getLink();
        if (link instanceof Receiver)
        {
        	ErrorCondition condition = link.getRemoteCondition();
        	this.processOnClose(link, condition);	
        }
	}
	
	public void processOnClose(Link link, ErrorCondition condition)
	{
		link.close();
		
    	if (condition != null)
		{
    		if (condition.getCondition() == null)
    		{
    			if(TRACE_LOGGER.isLoggable(Level.FINE))
    	        {
    				TRACE_LOGGER.log(Level.FINE, "linkName["+link.getName()+"] : ErrorCondition[" + condition.getCondition() + ", " + condition.getDescription() + "]");
    	        }
    			
    			this.msgReceiver.onClose();
    			return;
    		}
    		
			if(TRACE_LOGGER.isLoggable(Level.WARNING))
	        {
				TRACE_LOGGER.log(Level.WARNING, "linkName["+link.getName()+"] : ErrorCondition[" + condition.getCondition() + ", " + condition.getDescription() + "]");
	        }
        } 
		
    	this.msgReceiver.onError(condition);
	}
	
	
	@Override
	public void onLinkRemoteDetach(Event event)
	{
		Link link = event.getLink();
        if (link instanceof Receiver)
        {
        	ErrorCondition condition = link.getRemoteCondition();
        	if (condition != null)
        	{
        		if (TRACE_LOGGER.isLoggable(Level.WARNING))
        		TRACE_LOGGER.log(Level.WARNING, "linkName[" + link.getName() + "] : ErrorCondition[" + condition.getCondition() + ", " + condition.getDescription() + "]");
            }

            link.close();
            this.msgReceiver.onError(condition);
        }
	}
	
	@Override
    public void onDelivery(Event event)
	{
		if (this.isFirstResponse)
        {
			synchronized (this.firstResponse)
			{
				if (this.isFirstResponse)
				{
					this.msgReceiver.onOpenComplete(null);
					this.isFirstResponse = false;
				}
			}
		}
        
		LinkedList<Delivery> deliveries = new LinkedList<Delivery>();
        Delivery delivery = event.getDelivery();
        Receiver receiveLink = (Receiver) delivery.getLink();
        LinkedList<Message> messages = new LinkedList<Message>();
        
        while (delivery != null && delivery.isReadable() && !delivery.isPartial())
        {    
        	int size = delivery.pending();
            byte[] buffer = new byte[size];
            int read = receiveLink.recv(buffer, 0, buffer.length);
            
            Message msg = Proton.message();
            msg.decode(buffer, 0, read);
            
            messages.add(msg);
            deliveries.add(delivery);
            
            if (!receiveLink.advance())
            {
            	break;
            }
            else
            {
            	delivery = receiveLink.current();
            }
        }
        
        if (messages != null && messages.size() > 0)
        {
        	for(Delivery unsettledDelivery: deliveries)
        	{
        		unsettledDelivery.settle();
        	}
        	
        	this.msgReceiver.onDelivery(messages);
            if(TRACE_LOGGER.isLoggable(Level.FINE) && receiveLink != null)
            {
            	TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "linkName[%s], linkCredit[%s]", receiveLink.getName(), receiveLink.getCredit()));
            }
        }
    }
}
