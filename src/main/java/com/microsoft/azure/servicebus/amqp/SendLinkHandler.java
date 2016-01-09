package com.microsoft.azure.servicebus.amqp;

import java.util.logging.*;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;

import com.microsoft.azure.servicebus.MessageSender;

public class SendLinkHandler extends BaseHandler
{
	private static final Logger TRACE_LOGGER = Logger.getLogger("eventhub.trace");
	
	private final String name;
	private final MessageSender msgSender;
	private final Object firstFlow;
	private boolean isFirstFlow;
	
	public SendLinkHandler(final String name, final MessageSender sender)
	{
		this.name = name;
		this.msgSender = sender;
		this.firstFlow = new Object();
		this.isFirstFlow = true;
	}

	@Override
    public void onUnhandled(Event event)
	{
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
            TRACE_LOGGER.log(Level.FINE, "sendLink.onUnhandled: name[" + event.getLink().getName() + "] : event[" + event + "]");
        }		
	}

	@Override
    public void onDelivery(Event event)
	{		
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
			TRACE_LOGGER.log(Level.FINE, "sendLink.onFlow: name[" + sender.getName() + "] : unsettled[" + sender.getUnsettled() + "] : credit[" + sender.getCredit()+ "]");
        }
	}
	
	@Override
    public void onLinkRemoteClose(Event event)
	{
		Link link = event.getLink();
        if (link instanceof Sender)
        {
        	ErrorCondition condition = link.getRemoteCondition();
    		if (condition != null)
    		{
    			if(TRACE_LOGGER.isLoggable(Level.WARNING))
    	        {
    				TRACE_LOGGER.log(Level.WARNING, "sendLink.onLinkRemoteClose: name[" + link.getName() + "] : ErrorCondition[" + condition.getDescription() + "]");
    	        }
            } 
    		
    		this.msgSender.onError(condition);
        }
	}
}
