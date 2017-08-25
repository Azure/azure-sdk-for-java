package com.microsoft.azure.servicebus;

public class SubscriptionClientSessionTests extends ClientSessionTests{
    @Override
    public String getEntityNamePrefix() {
       return "SubscriptionClientSessionTests";
    }

    @Override
    public boolean isEntityQueue() {
        return true;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }

    @Override
    public boolean isEntityPartitioned() {
        return false;
    }
}
