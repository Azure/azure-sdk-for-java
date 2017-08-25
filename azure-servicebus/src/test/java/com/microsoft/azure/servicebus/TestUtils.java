package com.microsoft.azure.servicebus;

import java.util.UUID;

public class TestUtils {
	
	private static final String NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME = "AZURE_SERVICEBUS_JAVA_CLIENT_TEST_CONNECTION_STRING";	
	public static final String FIRST_SUBSCRIPTION_NAME = "subscription1";	
	private static String namespaceConnectionString;
	
	static
	{
		// Read connection string
		namespaceConnectionString = System.getenv(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME);
		if(namespaceConnectionString == null || namespaceConnectionString.isEmpty())
		{			
			System.err.println(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME + " environment variable not set. Tests will not be able to connecto to any service bus entity.");
		}
	}
	
	public static String getNamespaceConnectionString()
	{
	    return namespaceConnectionString;
	}
	
	public static String randomizeEntityName(String entityName)
	{
	    return entityName + getRandomString();
	}

    public static String getRandomString()
    {
    	return UUID.randomUUID().toString();
    }
    
    /**
     * Tells this class whether to create an entity for every test and delete it after the test. Creating an entity for every test makes the tests independent of 
     * each other and advisable if the SB namespace allows it. If the namespace doesn't allow creation and deletion of many entities in a short span of time, the suite
     * will create one entity at the start, uses it for all test and deletes the entity at the end.
     * @return true if each test should create and delete its own entity. Else return false.
     */
    public static boolean shouldCreateEntityForEveryTest()
    {
        return true;
    }
}
