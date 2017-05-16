package com.microsoft.azure.servicebus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

public class TestUtils {
	private static final String TEST_DIR_NAME = "resources";
	private static final String TEST_PROPERTIES_FILE_NAME = "test.properties";
	
	private static final String NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME = "AZURE_SERVICEBUS_JAVA_CLIENT_TEST_CONNECTION_STRING";
	
	//Queue	
	private static final String NON_PARTITIONED_QUEUE_NAME_PROPERTY = "non.partitioned.queue.name";	
	
	//Sessionful Queue	
	private static final String NON_PARTITIONED_SESSIONFUL_QUEUE_NAME_PROPERTY = "session.non.partitioned.queue.name";	
	
	//Topic and Subscription	
	private static final String NON_PARTITIONED_TOPIC_NAME_PROPERTY = "non.partitioned.topic.name";
	static final String SUBSCRIPTION_NAME_PROPERTY = "subscription.name";
		
	//Sessionful Topic and Subscription	
	private static final String NON_PARTITIONED_SESSIONFUL_TOPIC_NAME_PROPERTY = "session.non.partitioned.topic.name";
	private static final String SESSIONFUL_SUBSCRIPTION_NAME_PROPERTY = "session.subscription.name";	
	
	private static Properties accessProperties;
	private static String namespaceConnectionString;
	
	static
	{
		accessProperties = new Properties();
		String workingDir = System.getProperty("user.dir");
		try
		{
			accessProperties.load(new FileReader(workingDir + File.separator + TEST_DIR_NAME + File.separator + TEST_PROPERTIES_FILE_NAME));
		}
		catch(IOException ioe)
		{
			// User properties file not found. Don't do anything, properties remain empty.
			System.err.println(TEST_PROPERTIES_FILE_NAME + " file not found. Tests will not be able to connecto to any service bus entity.");
		}
		
		// Read connection string
		namespaceConnectionString = System.getenv(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME);
		if(namespaceConnectionString == null || namespaceConnectionString.isEmpty())
		{			
			System.err.println(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME + " environment variable not set. Tests will not be able to connecto to any service bus entity.");
		}
	}
	
	static String getProperty(String propertyName)
	{
		String defaultValue = "";		
		return accessProperties.getProperty(propertyName, defaultValue);
	}
	
	public static ConnectionStringBuilder getNonPartitionedQueueConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(namespaceConnectionString, getProperty(NON_PARTITIONED_QUEUE_NAME_PROPERTY));
	}
	
	public static ConnectionStringBuilder getNonPartitionedSessionfulQueueConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(namespaceConnectionString, getProperty(NON_PARTITIONED_SESSIONFUL_QUEUE_NAME_PROPERTY));
	}
	
	public static ConnectionStringBuilder getNonPartitionedTopicConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(namespaceConnectionString, getProperty(NON_PARTITIONED_TOPIC_NAME_PROPERTY));
	}
	
	public static ConnectionStringBuilder getNonPartitionedSessionfulTopicConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(namespaceConnectionString, getProperty(NON_PARTITIONED_SESSIONFUL_TOPIC_NAME_PROPERTY));
	}
	
	public static ConnectionStringBuilder getNonPartitionedSubscriptionConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(namespaceConnectionString, getProperty(NON_PARTITIONED_TOPIC_NAME_PROPERTY) + "/subscriptions/" + getProperty(SUBSCRIPTION_NAME_PROPERTY));
	}
	
	public static ConnectionStringBuilder getNonPartitionedSessionfulSubscriptionConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(namespaceConnectionString, getProperty(NON_PARTITIONED_SESSIONFUL_TOPIC_NAME_PROPERTY) + "/subscriptions/" + getProperty(SESSIONFUL_SUBSCRIPTION_NAME_PROPERTY));
	}
}
