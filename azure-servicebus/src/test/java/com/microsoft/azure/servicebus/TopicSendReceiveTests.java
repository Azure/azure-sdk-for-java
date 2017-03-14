package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

public class TopicSendReceiveTests extends SendReceiveTests {
	@Override
	public ConnectionStringBuilder getSenderConnectionStringBuilder() {
		return TestUtils.getTopicConnectionStringBuilder();
	}

	@Override
	public ConnectionStringBuilder getReceiverConnectionStringBuilder() {
		return TestUtils.getSubscriptionConnectionStringBuilder();
	}
}
