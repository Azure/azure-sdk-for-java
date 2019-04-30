package com.microsoft.azure.servicebus;

public class PartitionedSubscriptionClientSessionTests extends SubscriptionClientSessionTests{
    @Override
    public String getEntityNamePrefix() {
       return "PartitionedSubscriptionClientSessionTests";
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