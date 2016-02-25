/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
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
	private final IAmqpReceiver amqpReceiver;
	private final Object firstResponse;
	private boolean isFirstResponse;
	
	public ReceiveLinkHandler(final IAmqpReceiver receiver)
	{
		super(receiver);
		
		this.amqpReceiver = receiver;
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
	        		this.amqpReceiver.onOpenComplete(null);
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
        if (link != null)
        {
        	ErrorCondition condition = link.getRemoteCondition();
        	this.processOnClose(link, condition);	
        }
	}
		
	@Override
	public void onLinkRemoteDetach(Event event)
	{
		Link link = event.getLink();
        if (link != null)
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
					this.isFirstResponse = false;
					this.amqpReceiver.onOpenComplete(null);
				}
			}
		}
        
		Delivery delivery = event.getDelivery();
        Receiver receiveLink = (Receiver) delivery.getLink();
        
        if (delivery != null && delivery.isReadable() && !delivery.isPartial())
        {    
        	int size = delivery.pending();
            byte[] buffer = new byte[size];
            int read = receiveLink.recv(buffer, 0, size);
            
            Message msg = Proton.message();
            msg.decode(buffer, 0, read);
            
            if(TRACE_LOGGER.isLoggable(Level.FINEST) && receiveLink != null)
            {
            	TRACE_LOGGER.log(Level.FINEST, String.format(Locale.US, "linkName[%s], updatedLinkCredit[%s]", receiveLink.getName(), receiveLink.getCredit()));
            }
            
        	this.amqpReceiver.onReceiveComplete(msg);
        	delivery.settle();
        }
    }
}
