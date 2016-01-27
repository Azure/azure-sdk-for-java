package com.microsoft.azure.eventhubs.lib;

import java.util.*;
import java.util.logging.Level;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.*;

/**
 * Sends 1 Msg on the first onLinkFlow event
 */
public class Sender1MsgOnLinkFlowHandler extends ServerTraceHandler
{
	private final Object firstFlow;
	private boolean isFirstFlow;
	
	public Sender1MsgOnLinkFlowHandler()
	{
		this.firstFlow = new Object();
		this.isFirstFlow = true;
	}
	
	@Override
	public void onLinkFlow(Event event)
	{
		if (this.isFirstFlow) {
			synchronized(this.firstFlow) {
				if (this.isFirstFlow) {
					Sender sender = (Sender) event.getLink();
					if (sender != null)
					{
						byte[] bytes = new byte[5 * 1024];
						Message msg = Proton.message();
						Map<String, String> properties = new HashMap<String, String>();
						properties.put("testkey", "testvalue");
						msg.setApplicationProperties(new ApplicationProperties(properties));
						int length = msg.encode(bytes,  0,  4 * 1024);
						
						byte[] tag = String.valueOf(1).getBytes();
						Delivery delivery = sender.delivery(tag);
						sender.send(bytes, 0, length);
						
						sender.advance();
						this.isFirstFlow = false;
					}
				}
			}
		}
	}
}
