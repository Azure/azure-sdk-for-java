package com.microsoft.azure.servicebus;

public abstract class Tests {
    public abstract String getEntityNamePrefix();
    
    public abstract boolean isEntityQueue();
    
    public abstract boolean isEntityPartitioned();
    
    /**
     * Tells this class whether to create an entity for every test and delete it after the test. Creating an entity for every test makes the tests independent of 
     * each other and advisable if the SB namespace allows it. If the namespace doesn't allow creation and deletion of many entities in a short span of time, the suite
     * will create one entity at the start, uses it for all test and deletes the entity at the end.
     * @return true if each test should create and delete its own entity. Else return false.
     */
    public abstract boolean shouldCreateEntityForEveryTest();
}
