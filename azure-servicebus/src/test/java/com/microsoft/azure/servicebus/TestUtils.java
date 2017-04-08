package com.microsoft.azure.servicebus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.IllegalConnectionStringFormatException;

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
	
	//Namespace suffix
	private static final String NAMESPACE_SUFFIX_PROPERTY = "namespace.suffix";
	private static final String DEFAULT_NAMESPACE_SUFFIX = "servicebus.windows.net";
	
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
		String defaultValue = "";
		if(propertyName.equalsIgnoreCase(NAMESPACE_SUFFIX_PROPERTY))
		{
			defaultValue = DEFAULT_NAMESPACE_SUFFIX;
		}
		
		return accessProperties.getProperty(propertyName, defaultValue);
	}
	
	private static URI getEndPointURI(String namespace)
	{
		String namespaceSuffix = getProperty(NAMESPACE_SUFFIX_PROPERTY);
		try {			
			return new URI("amqps://" + namespace + "." + namespaceSuffix);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					String.format(Locale.US, "Invalid namespace or namespace suffix: %s, %s", namespace, namespaceSuffix),
					e);
		}
	}
	
	public static ConnectionStringBuilder getQueueConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(getEndPointURI(getProperty(QUEUE_NAMESPACENAME_PROPERTY)), getProperty(QUEUE_ENTITYPATH_PROPERTY), 
				getProperty(QUEUE_SHAREDACCESSKEYNAME_PROPERTY), getProperty(QUEUE_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSessionfulQueueConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(getEndPointURI(getProperty(SESSIONFUL_QUEUE_NAMESPACENAME_PROPERTY)), getProperty(SESSIONFUL_QUEUE_ENTITYPATH_PROPERTY), 
				getProperty(SESSIONFUL_QUEUE_SHAREDACCESSKEYNAME_PROPERTY), getProperty(SESSIONFUL_QUEUE_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getTopicConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(getEndPointURI(getProperty(TOPIC_NAMESPACENAME_PROPERTY)), getProperty(TOPIC_ENTITYPATH_PROPERTY), 
				getProperty(TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(TOPIC_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSessionfulTopicConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(getEndPointURI(getProperty(SESSIONFUL_TOPIC_NAMESPACENAME_PROPERTY)), getProperty(SESSIONFUL_TOPIC_ENTITYPATH_PROPERTY), 
				getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSubscriptionConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(getEndPointURI(getProperty(TOPIC_NAMESPACENAME_PROPERTY)), getProperty(TOPIC_ENTITYPATH_PROPERTY) + "/subscriptions/" + getProperty(SUBSCRIPTION_ENTITYPATH_PROPERTY), 
				getProperty(TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(TOPIC_SHAREDACCESSKEY_PROPERTY));
	}
	
	public static ConnectionStringBuilder getSessionfulSubscriptionConnectionStringBuilder()
	{
		return new ConnectionStringBuilder(getEndPointURI(getProperty(SESSIONFUL_TOPIC_NAMESPACENAME_PROPERTY)), getProperty(SESSIONFUL_TOPIC_ENTITYPATH_PROPERTY) + "/subscriptions/" + getProperty(SESSIONFUL_SUBSCRIPTION_ENTITYPATH_PROPERTY), 
				getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEYNAME_PROPERTY), getProperty(SESSIONFUL_TOPIC_SHAREDACCESSKEY_PROPERTY));
	}
}
