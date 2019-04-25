package com.microsoft.azure.servicebus;

public class PartitionedTopicSessionTests extends TopicSessionTests {

    @Override
    public String getEntityNamePrefix() {
        return "PartitionedTopicSessionTests";
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
