package com.microsoft.azure.servicebus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.TestConnectionStringBuilder;

public class TestUtils {
	private static final String TEST_DIR_NAME = "resources";
	private static final String ACCESS_PROPERTIES_FILE_NAME = "access.properties";
	
	//Queue
	private static final String QUEUE_NAMESPACENAME_PROPERTY = "queue.namespacename";
	private static final String QUEUE_ENTITYPATH_PROPERTY = "queue.entitypath";
	private static final String QUEUE_SHAREDACCESSKEYNAME_PROPERTY = "queue.sharedaccesskeyname";
	private static final String QUEUE_SHAREDACCESSKEY_PROPERTY = "queue.sharedaccesskey";
	
	//Sessionful Queue
	private static final String SESSIONFUL_QUEUE_NAMESPACENAME_PROPERTY = "queue.sessionful.namespacename";
	private static final String SESSIONFUL_QUEUE_ENTITYPATH_PROPERTY = "queue.sessionful.entitypath";
	private static final String SESSIONFUL_QUEUE_SHAREDACCESSKEYNAME_PROPERTY = "queue.sessionful.sharedaccesskeyname";
	private static final String SESSIONFUL_QUEUE_SHAREDACCESSKEY_PROPERTY = "queue.sessionful.sharedaccesskey";
	
	//Topic and Subscription
	private static final String TOPIC_NAMESPACENAME_PROPERTY = "topic.namespacename";
	private static final String TOPIC_ENTITYPATH_PROPERTY = "topic.entitypath";
	private static final String SUBSCRIPTION_ENTITYPATH_PROPERTY = "subscription.entitypath";
	private static final String TOPIC_SHAREDACCESSKEYNAME_PROPERTY = "topic.sharedaccesskeyname";
	private static final String TOPIC_SHAREDACCESSKEY_PROPERTY = "topic.sharedaccesskey";
		
	//Sessionful Topic and Subscription
	private static final String SESSIONFUL_TOPIC_NAMESPACENAME_PROPERTY = "topic.sessionful.namespacename";
	private static final String SESSIONFUL_TOPIC_ENTITYPATH_PROPERTY = "topic.sessionful.entitypath";
	private static final String SESSIONFUL_SUBSCRIPTION_ENTITYPATH_PROPERTY = "subscription.sessionful.entitypath";
	private static final String SESSIONFUL_TOPIC_SHAREDACCESSKEYNAME_PROPERTY = "topic.sessionful.sharedaccesskeyname";
	private static final String SESSIONFUL_TOPIC_SHAREDACCESSKEY_PROPERTY = "topic.sessionful.sharedaccesskey";
	
	private static Properties accessProperties;
	
	static
	{
		accessProperties = new Properties();
		String workingDir = System.getProperty("user.dir");
		try
		{
			accessProperties.load(new FileReader(workingDir + File.separator + TEST_DIR_NAME + File.separator + ACCESS_PROPERTIES_FILE_NAME));
		}
		catch(IOException ioe)
		{
			// User properties file not found. Don't do anything, properties remain empty.
			System.err.println(ACCESS_PROPERTIES_FILE_NAME + " file not found. Tests will not be able to connecto to any service bus entity.");
		}
	}
	
	private static String getProperty(String propertyName)
	{
		return accessProperties.getProperty(propertyName, "");
	}	
	
	public static ConnectionStringBuilder getQueueConnectionStringBuilder()
	{
		return new TestConnectionStringBuilder(getProperty(QUEUE_NAMESPACENAME_PROPERTY), getProperty(QUEUE_ENTITYPATH_PROPERTY), 
				getProperty(QUEUE_SHAREDACCESSKEYNAME_PROPERTY), getProperty(QUEUE_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSessionfulQueueConnectionStringBuilder()
	{
		return new TestConnectionStringBuilder(getProperty(SESSIONFUL_QUEUE_NAMESPACENAME_PROPERTY), getProperty(SESSIONFUL_QUEUE_ENTITYPATH_PROPERTY), 
				getProperty(SESSIONFUL_QUEUE_SHAREDACCESSKEYNAME_PROPERTY), getProperty(SESSIONFUL_QUEUE_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getTopicConnectionStringBuilder()
	{
		return new TestConnectionStringBuilder(getProperty(TOPIC_NAMESPACENAME_PROPERTY), getProperty(TOPIC_ENTITYPATH_PROPERTY), 
				getProperty(TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(TOPIC_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSessionfulTopicConnectionStringBuilder()
	{
		return new TestConnectionStringBuilder(getProperty(SESSIONFUL_TOPIC_NAMESPACENAME_PROPERTY), getProperty(SESSIONFUL_TOPIC_ENTITYPATH_PROPERTY), 
				getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSubscriptionConnectionStringBuilder()
	{
		return new TestConnectionStringBuilder(getProperty(TOPIC_NAMESPACENAME_PROPERTY), getProperty(TOPIC_ENTITYPATH_PROPERTY) + "/subscriptions/" + getProperty(SUBSCRIPTION_ENTITYPATH_PROPERTY), 
				getProperty(TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(TOPIC_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSessionfulSubscriptionConnectionStringBuilder()
	{
		return new TestConnectionStringBuilder(getProperty(SESSIONFUL_TOPIC_NAMESPACENAME_PROPERTY), getProperty(SESSIONFUL_TOPIC_ENTITYPATH_PROPERTY) + "/subscriptions/" + getProperty(SESSIONFUL_SUBSCRIPTION_ENTITYPATH_PROPERTY), 
				getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEY_PROPERTY));
	}	
}
