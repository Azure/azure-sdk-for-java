package com.microsoft.azure.servicebus;

public class PartitionedQueueSendReceiveTests extends QueueSendReceiveTests{
	@Override
    public String getEntityNamePrefix() {
       return "PartitionedQueueSendReceiveTests";
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }

    @Override
    public boolean isEntityPartitioned() {
        return true;
    }
}
