package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

public class TopicSessionTests extends SessionTests {

	@Override
	public ConnectionStringBuilder getSenderConnectionStringBuilder() {
		return TestUtils.getSessionfulTopicConnectionStringBuilder();
	}

	@Override
	public ConnectionStringBuilder getReceiverConnectionStringBuilder() {
		return TestUtils.getSessionfulSubscriptionConnectionStringBuilder();
	}
}
