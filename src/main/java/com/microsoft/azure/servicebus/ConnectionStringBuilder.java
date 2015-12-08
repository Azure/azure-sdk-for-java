package com.microsoft.azure.servicebus;

import java.time.*;
import java.util.*;

import org.apache.qpid.proton.engine.impl.StringUtils;

public class ConnectionStringBuilder {

	final String EndpointConfigName = "Endpoint";
	final String SharedAccessKeyNameConfigName = "SharedAccessKeyName";
	final String SharedAccessKeyConfigName = "SharedAccessKey";
	
	private String connectionString;
	private String hostName;
	private String sharedAccessKeyName;
	private String sharedAccessKey;
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	
	private ConnectionStringBuilder(
			final String namespaceName,
			final String sharedAccessKeyName,
			final String sharedAccessKey,
			final Duration operationTimeout,
			final RetryPolicy retryPolicy)  
	{
		this.hostName = namespaceName;
		this.sharedAccessKey = sharedAccessKey;
		this.sharedAccessKeyName = sharedAccessKeyName;
		this.operationTimeout = operationTimeout;
		this.retryPolicy = retryPolicy;
	}
	
	public ConnectionStringBuilder(final String namespaceName,
			final String sharedAccessKeyName,
			final String sharedAccessKey)
	{
		this(namespaceName, sharedAccessKeyName, sharedAccessKey, MessagingFactory.DefaultOperationTimeout, RetryPolicy.Default);
	}
	
	public ConnectionStringBuilder(String connectionString) {
		this.connectionString = connectionString;
	}

	String getHostName() {
		return this.hostName;
	}
	
	String getSasKey() {
		return this.sharedAccessKey;
	}
	
	String getSasKeyName() {
		return this.sharedAccessKeyName;
	}
	
	@Override public String toString() {
		return this.connectionString;
	}
	
	void parseConnectionString(String connectionString) {
		
	}
}
