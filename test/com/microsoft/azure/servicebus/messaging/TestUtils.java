package com.microsoft.azure.servicebus.messaging;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class TestUtils {
	private static final String ACCESS_PROPERTIES_FILE_PATH = "access.properties";
	private static final String NAMESPACENAME_PROPERTY = "namespacename";
	private static final String ENTITYPATH_PROPERTY = "entitypath";
	private static final String SHAREDACCESSKEYNAME_PROPERTY = "sharedaccesskeyname";
	private static final String SHAREDACCESSKEY_PROPERTY = "sharedaccesskey";
	private static Properties accessProperties;
	
	static
	{
		accessProperties = new Properties();
		String workingDir = System.getProperty("user.dir");
		try
		{
			accessProperties.load(new FileReader(workingDir + File.separator + ACCESS_PROPERTIES_FILE_PATH));
		}
		catch(IOException ioe)
		{
			// User properties file not found. Don't do anything, properties remain empty.
			System.err.println(ACCESS_PROPERTIES_FILE_PATH + " file not found. Tests will not be able to connecto to any service bus entity.");
		}
	}
	
	public static String getNamespace()
	{
		return accessProperties.getProperty(NAMESPACENAME_PROPERTY, "");
	}
	
	public static String getEntityPath()
	{
		return accessProperties.getProperty(ENTITYPATH_PROPERTY, "");
	}
	
	public static String getSharedAccessKeyName()
	{
		return accessProperties.getProperty(SHAREDACCESSKEYNAME_PROPERTY, "");
	}
	
	public static String getSharedAccessKey()
	{
		return accessProperties.getProperty(SHAREDACCESSKEY_PROPERTY, "");
	}
}
