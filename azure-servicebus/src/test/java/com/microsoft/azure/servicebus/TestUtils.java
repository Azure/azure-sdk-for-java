package com.microsoft.azure.servicebus;

import java.net.URI;
import java.util.UUID;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.Util;

public class TestUtils {
	
	private static final String NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME = "AZURE_SERVICEBUS_JAVA_CLIENT_TEST_CONNECTION_STRING";
    public static final String FIRST_SUBSCRIPTION_NAME = "subscription1";
	private static String namespaceConnectionString;
	private static ConnectionStringBuilder namespaceConnectionStringBuilder;
	
	static
	{
		// Read connection string
        namespaceConnectionString = System.getenv(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME);
		if(namespaceConnectionString == null || namespaceConnectionString.isEmpty())
		{			
			System.err.println(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME + " environment variable not set. Tests will not be able to connect to to any service bus entity.");
		}
		namespaceConnectionStringBuilder = new ConnectionStringBuilder(namespaceConnectionString);
	}
	
	public static URI getNamespaceEndpointURI()
    {
        return namespaceConnectionStringBuilder.getEndpoint();
    }
    
    public static ClientSettings getClientSettings()
    {
        return Util.getClientSettingsFromConnectionStringBuilder(namespaceConnectionStringBuilder);
    }
    
    // AADTokens cannot yet be used for management operations, sent directly to gateway
    public static ClientSettings getManagementClientSettings()
    {
        return Util.getClientSettingsFromConnectionStringBuilder(namespaceConnectionStringBuilder);
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
