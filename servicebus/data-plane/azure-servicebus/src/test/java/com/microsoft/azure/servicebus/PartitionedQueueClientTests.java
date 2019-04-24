package com.microsoft.azure.servicebus;

public class PartitionedQueueClientTests extends QueueClientTests{
	 @Override
    public String getEntityNamePrefix() {
       return "PartitionedQueueClientTests";
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
