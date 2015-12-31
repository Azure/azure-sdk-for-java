package com.microsoft.azure.servicebus;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.util.*;
import java.util.regex.*;

import org.apache.qpid.proton.engine.Endpoint;
import org.apache.qpid.proton.engine.impl.StringUtils;

public class ConnectionStringBuilder {

	final static String endpointFormat = "amqps://%s.servicebus.windows.net";
	
	final static String EndpointConfigName = "Endpoint";
	final static String SharedAccessKeyNameConfigName = "SharedAccessKeyName";
	final static String SharedAccessKeyConfigName = "SharedAccessKey";
	final static String EntityPathConfigName = "EntityPath";
	final static String KeyValueSeparator = "=";
	final static String KeyValuePairDelimiter = ";";

	private static final String AllKeyEnumerateRegex = "(" + EndpointConfigName + "|" + SharedAccessKeyNameConfigName
			+ "|" + SharedAccessKeyConfigName + "|" + EntityPathConfigName + ")";

	private static final String KeysWithDelimitersRegex = KeyValuePairDelimiter + AllKeyEnumerateRegex
			+ KeyValueSeparator;

	private String connectionString;
	private URI endpoint;
	private String sharedAccessKeyName;
	private String sharedAccessKey;
	private String entityPath;
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;

	// TODO: upgrade to public after implementing retryPolicy
	private ConnectionStringBuilder(final String namespaceName, final String entityPath, final String sharedAccessKeyName,
			final String sharedAccessKey, final Duration operationTimeout, final RetryPolicy retryPolicy) {
		
		try {
			this.endpoint = new URI(String.format(Locale.US, endpointFormat, namespaceName));
		} catch(URISyntaxException exception) {
			throw new IllegalConnectionStringFormatException(
					String.format(Locale.US, "Invalid namespace name: %s", namespaceName),
					exception);
		}
		
		this.sharedAccessKey = sharedAccessKey;
		this.sharedAccessKeyName = sharedAccessKeyName;
		this.operationTimeout = operationTimeout;
		this.retryPolicy = retryPolicy;
		this.entityPath = entityPath;
	}

	/**
	 * Build connection string consumable by {@link EventHubClient#createFromConnectionString(String)}
	 * @param namespaceName Namespace name (dns suffix - ex: .servicebus.windows.net is not required) 
	 * @param entityPath Entity path
	 * @param sharedAccessKeyName Shared Access Key name
	 * @param sharedAccessKey Shared Access Key
	 */
	public ConnectionStringBuilder(final String namespaceName, final String entityPath, final String sharedAccessKeyName,
			final String sharedAccessKey) {
		this(namespaceName, entityPath, sharedAccessKeyName, sharedAccessKey, MessagingFactory.DefaultOperationTimeout, RetryPolicy.getDefault());
	}

	/*
	 * ConnectionString format:
	 * Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY
	 */
	public ConnectionStringBuilder(String connectionString) {
		this.parseConnectionString(connectionString);
		this.connectionString = connectionString;
	}
	
	URI getEndpoint() {
		return this.endpoint;
	}

	String getSasKey() {
		return this.sharedAccessKey;
	}

	String getSasKeyName() {
		return this.sharedAccessKeyName;
	}
	
	public String getEntityPath() {
		return this.entityPath;
	}
	
	public Duration getOperationTimeout() {
		return (this.operationTimeout == null ? MessagingFactory.DefaultOperationTimeout : this.operationTimeout);
	}
	
	public RetryPolicy getRetryPolicy() {
		return (this.retryPolicy == null ? RetryPolicy.getDefault() : this.retryPolicy);
	}

	@Override
	public String toString() {

		if (StringUtil.isNullOrWhiteSpace(this.connectionString)) {
			StringBuilder connectionStringBuilder = new StringBuilder();
			if (this.endpoint != null) {
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", EndpointConfigName, KeyValueSeparator,
					this.endpoint.toString(), KeyValuePairDelimiter));
			}
			
			if (!StringUtil.isNullOrWhiteSpace(this.entityPath)) {
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", EntityPathConfigName,
					KeyValueSeparator, this.entityPath, KeyValuePairDelimiter));
			}
			
			if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessKeyName)) {
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SharedAccessKeyNameConfigName,
					KeyValueSeparator, this.sharedAccessKeyName, KeyValuePairDelimiter));
			}
			
			if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessKey)) {
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s", SharedAccessKeyConfigName,
					KeyValueSeparator, this.sharedAccessKey));
			}
			
			this.connectionString = connectionStringBuilder.toString();
		}

		return this.connectionString;
	}

	private void parseConnectionString(String connectionString) {

		// TODO: Trace and throw
		if (StringUtil.isNullOrWhiteSpace(connectionString)) {
			throw new IllegalConnectionStringFormatException(String.format("connectionString cannot be empty"));
		}

		String connection = KeyValuePairDelimiter + connectionString;

		Pattern keyValuePattern = Pattern.compile(KeysWithDelimitersRegex, Pattern.CASE_INSENSITIVE);
		String[] values = keyValuePattern.split(connection);
		Matcher keys = keyValuePattern.matcher(connection);

		if (values == null || values.length <= 1 || keys.groupCount() == 0) {
			throw new IllegalConnectionStringFormatException("Connection String cannot be parsed.");
		}

		if (!StringUtil.isNullOrWhiteSpace((values[0]))) {
			throw new IllegalConnectionStringFormatException(
					String.format(Locale.US, "Cannot parse part of ConnectionString: %s", values[0]));
		}

		int valueIndex = 0;
		while (keys.find()) {
			
			valueIndex++;

			String key = keys.group();
			key = key.substring(1, key.length() - 1);
			
			if (values.length < valueIndex + 1){
				throw new IllegalConnectionStringFormatException(
						String.format(Locale.US, "Value for the connection string parameter name: %s, not found", key));
			}
			
			if (key.equalsIgnoreCase(EndpointConfigName)) {
				try {
					this.endpoint = new URI(values[valueIndex]); 
				} catch(URISyntaxException exception) {
					throw new IllegalConnectionStringFormatException(
							String.format(Locale.US, "%s should be in format scheme://fullyQualifiedServiceBusNamespaceEndpointName", EndpointConfigName),
							exception);
				}
			}
			else if(key.equalsIgnoreCase(SharedAccessKeyNameConfigName)) {
				this.sharedAccessKeyName = values[valueIndex];
			}
			else if(key.equalsIgnoreCase(SharedAccessKeyConfigName)) {
				this.sharedAccessKey = values[valueIndex];
			}
			else if (key.equalsIgnoreCase(EntityPathConfigName)) {
				this.entityPath = values[valueIndex];
			}
			else {
				throw new IllegalConnectionStringFormatException(
						String.format(Locale.US, "Illegal connection string parameter name: %s", key));
			}
		}
	}
}
