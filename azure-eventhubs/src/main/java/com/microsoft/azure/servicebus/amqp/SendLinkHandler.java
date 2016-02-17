package com.microsoft.azure.servicebus.amqp;

import java.util.Locale;
import java.util.logging.*;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;

import com.microsoft.azure.servicebus.ClientConstants;
import com.microsoft.azure.servicebus.MessageSender;

public class SendLinkHandler extends BaseLinkHandler
{
	private final MessageSender msgSender;
	private final Object firstFlow;
	private boolean isFirstFlow;
	
	public SendLinkHandler(final MessageSender sender)
	{
		this.msgSender = sender;
		this.firstFlow = new Object();
		this.isFirstFlow = true;
	}
	
	@Override
	public void onLinkRemoteOpen(Event event)
	{
		Link link = event.getLink();
        if (link != null && link instanceof Sender)
        {
        	Sender sender = (Sender) link;
        	if (link.getRemoteTarget() != null)
        	{
        		if(TRACE_LOGGER.isLoggable(Level.FINE))
                {
                	TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "linkName[%s], remoteTarget[%s]", sender.getName(), link.getRemoteTarget()));
                }
        		
        		synchronized (this.firstFlow)
        		{
					this.isFirstFlow = false;
	        		this.msgSender.onOpenComplete(null);
        		}
        	}
        	else
        	{
        		if(TRACE_LOGGER.isLoggable(Level.FINE))
                {
                	TRACE_LOGGER.log(Level.FINE,
                			String.format(Locale.US, "linkName[%s], remoteTarget[null], remoteSource[null], action[waitingForError]", sender.getName()));
                }
        	}
        }
	}

	@Override
    public void onDelivery(Event event)
	{		
		Sender sender = (Sender) event.getLink();
		Delivery delivery = event.getDelivery();
		
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
            TRACE_LOGGER.log(Level.FINE, 
            		"linkName[" + sender.getName() + 
            		"], unsettled[" + sender.getUnsettled() + "], credit[" + sender.getCredit()+ "], deliveryState[" + delivery.getRemoteState() + 
            		"], delivery.isBuffered[" + delivery.isBuffered() +"]");
        }
		
		if (delivery != null)
		{
			msgSender.onSendComplete(delivery.getTag(), delivery.getRemoteState());
			delivery.settle();
		}
	}

	@Override
	public void onLinkFlow(Event event)
	{
		if (this.isFirstFlow)
		{
			synchronized (this.firstFlow)
			{
				if (this.isFirstFlow)
				{
					this.msgSender.onOpenComplete(null);
					this.isFirstFlow = false;
				}
			}
		}
		
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
			Sender sender = (Sender) event.getLink();
			TRACE_LOGGER.log(Level.FINE, "linkName[" + sender.getName() + "], unsettled[" + sender.getUnsettled() + "], credit[" + sender.getCredit()+ "]");
        }
	}
	
	@Override
    public void onLinkRemoteClose(Event event)
	{
		Link link = event.getLink();
        if (link instanceof Sender)
        {
        	ErrorCondition condition = link.getRemoteCondition();
    		this.processOnClose(link, condition);
        }
	}
	
	@Override
	public void onLinkRemoteDetach(Event event)
	{
		this.onLinkRemoteClose(event);
	}
	
	// TODO: abstract this out to an interface - as a connection-child
	public void processOnClose(Link link, ErrorCondition condition)
	{
		if (condition != null)
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
	        {
				TRACE_LOGGER.log(Level.FINE, "linkName[" + link.getName() + "], ErrorCondition[" + condition.getDescription() + "]");
	        }
        }
		
		link.close();
		this.msgSender.onError(condition);
	}
}
