package com.microsoft.azure.servicebus;

public class PartitionedTopicSendReceiveTests extends TopicSendReceiveTests {

    @Override
    public String getEntityNamePrefix() {
        return "PartitionedTopicSendReceiveTests";
    }

    @Override
    public boolean isEntityPartitioned() {
        return true;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }
}
