package com.microsoft.azure.servicebus;

public class PartitionedSubscriptionClientTests extends SubscriptionClientTests{	
	@Override
    public String getEntityNamePrefix() {
        return "PartitionedSubscriptionClientTests";
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
