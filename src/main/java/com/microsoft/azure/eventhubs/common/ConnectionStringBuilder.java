package com.microsoft.azure.eventhubs.common;

import java.time.*;
import java.util.*;

import org.apache.qpid.proton.engine.impl.StringUtils;

public class ConnectionStringBuilder {

	private String connectionString;
	private String hostName;
	private String sharedAccessKeyName;
	private String sharedAccessKey;
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	
	private ConnectionStringBuilder(
			final String hostName,
			final String sharedAccessKeyName,
			final String sharedAccessKey,
			final Duration operationTimeout,
			final RetryPolicy retryPolicy)  
	{
		this.hostName = hostName;
		this.sharedAccessKey = sharedAccessKey;
		this.sharedAccessKeyName = sharedAccessKeyName;
		this.operationTimeout = operationTimeout;
		this.retryPolicy = retryPolicy;
	}
	
	public ConnectionStringBuilder(final String hostName,
			final String sharedAccessKeyName,
			final String sharedAccessKey)
	{
		this(hostName, sharedAccessKeyName, sharedAccessKey, MessagingFactory.DefaultOperationTimeout, RetryPolicy.Default);
	}
	
	public ConnectionStringBuilder(String connectionString) {
		this.connectionString = connectionString;
	}

	String getHostName() {
		/* if (StringUtil.isNullOrEmpty(this.hostName) && !StringUtil.isNullOrEmpty(this.connectionString))
		{
			this.hostName = 
		}*/
		
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

	public void Validate() {
		// TODO Add Validation on ConnStrBuilder
		
	}
}
