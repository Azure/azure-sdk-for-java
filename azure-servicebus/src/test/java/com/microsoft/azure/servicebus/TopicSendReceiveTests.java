package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

public class TopicSendReceiveTests extends SendReceiveTests {
	@Override
	public ConnectionStringBuilder getSenderConnectionStringBuilder() {
		return TestUtils.getNonPartitionedTopicConnectionStringBuilder();
	}

	@Override
	public ConnectionStringBuilder getReceiverConnectionStringBuilder() {
		return TestUtils.getNonPartitionedSubscriptionConnectionStringBuilder();
	}
}
