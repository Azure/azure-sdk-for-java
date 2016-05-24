/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.util.Locale;
import java.util.logging.Level;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.ClientConstants;


// ServiceBus <-> ProtonReactor interaction 
// handles all recvLink - reactor events
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
		synchronized (this.firstResponse)
		{
			if (this.isFirstResponse)
			{
				this.isFirstResponse = false;
				this.amqpReceiver.onOpenComplete(null);
			}
		}

		Delivery delivery = event.getDelivery();
		Receiver receiveLink = (Receiver) delivery.getLink();

		// If a message spans across deliveries (for ex: 200k message will be 4 frames (deliveries) 64k 64k 64k 8k), 
		// all until "last-1" deliveries will be partial
		// reactor will raise onDelivery event for all of these - we only need the last one
		if (!delivery.isPartial())
		{
			// delivery.pending() should return current deliveries pending bytes to be read.
			// we ran into an issue with proton-j where delivery.pending() returned lessthan available bytes and hence the below Math.max(...)
			int msgSize = delivery.pending() + ClientConstants.MAX_FRAME_SIZE_BYTES;
			byte[] buffer = new byte[msgSize];
			int read = receiveLink.recv(buffer, 0, msgSize);

			delivery.settle();

			if (read != -1)
			{
				Message msg = Proton.message();
				msg.decode(buffer, 0, read);
				this.amqpReceiver.onReceiveComplete(msg);
			}
		}

		if(TRACE_LOGGER.isLoggable(Level.FINEST) && receiveLink != null)
		{
			TRACE_LOGGER.log(Level.FINEST, 
					String.format(Locale.US, "linkName[%s], updatedLinkCredit[%s], remoteCredit[%s], remoteCondition[%s], delivery.isPartial[%s]", 
							receiveLink.getName(), receiveLink.getCredit(), receiveLink.getRemoteCredit(), receiveLink.getRemoteCondition(), delivery.isPartial()));
		}
	}
}
