package com.microsoft.azure.servicebus;

public class PartitionedQueueSessionTests extends QueueSessionTests
{
    @Override
    public String getEntityNamePrefix() {
       return "PartitionedQueueSessionTests";
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
