package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

public class QueueSessionTests extends SessionTests
{
	@Override
	public ConnectionStringBuilder getSenderConnectionStringBuilder() {
		return TestUtils.getSessionfulQueueConnectionStringBuilder();
	}

	@Override
	public ConnectionStringBuilder getReceiverConnectionStringBuilder() {
		return TestUtils.getSessionfulQueueConnectionStringBuilder();
	}
}
