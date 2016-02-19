package com.microsoft.azure.servicebus.amqp;

import java.util.*;
import java.util.logging.Level;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.Message;

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
            			String.format("linkName[%s], localSource[%s]", receiver.getName(), receiver.getSource()));
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
		
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
			TRACE_LOGGER.log(Level.FINE, "linkName["+link.getName()+
					condition != null ? "], ErrorCondition[" + condition.getCondition() + ", " + condition.getDescription() + "]" : "], condition[null]");
        }
		
		this.msgReceiver.onClose(condition);        
	}
	
	public void processOnClose(Link link, Exception exception)
	{
		link.close();
		this.msgReceiver.onError(exception);
	}
		
	@Override
	public void onLinkRemoteDetach(Event event)
	{
		Link link = event.getLink();
        if (link instanceof Receiver)
        {
        	this.processOnClose(link, link.getRemoteCondition());
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
            if(TRACE_LOGGER.isLoggable(Level.FINE) && receiveLink != null)
            {
            	TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "linkName[%s], linkCredit[%s]", receiveLink.getName(), receiveLink.getCredit()));
            }
            
        	for(Delivery unsettledDelivery: deliveries)
        	{
        		unsettledDelivery.settle();
        	}
        	
        	this.msgReceiver.onDelivery(messages);
        }
    }
}
