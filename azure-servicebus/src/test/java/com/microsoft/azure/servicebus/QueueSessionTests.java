package com.microsoft.azure.servicebus;

public class QueueSessionTests extends SessionTests
{
    @Override
    public String getEntityNamePrefix() {
       return "QueueSessionTests";
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
