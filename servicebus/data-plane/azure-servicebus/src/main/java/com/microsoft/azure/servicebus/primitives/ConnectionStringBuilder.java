/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.net.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.*;

/**
 * This class can be used to construct a connection string which can establish communication with ServiceBus entities.
 * It can also be used to perform basic validation on an existing connection string.
 * <p> Sample Code:
 * <pre>{@code 
 * ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(
 *                                          "ServiceBusNamespaceName", 
 *                                          "ServiceBusEntityName", // QueueName or TopicName or SubscriptionPath
 *                                          "SharedAccessSignatureKeyName", 
 *                                          "SharedAccessSignatureKey");
 *  
 * String connectionString = connectionStringBuilder.toString();
 * }</pre>
 * <p>
 * A connection string is basically a string consisted of key-value pair separated by ";". 
 * Basic format is {{@literal <}key{@literal >}={@literal <}value{@literal >}[;{@literal <}key{@literal >}={@literal <}value{@literal >}]} where supported key name are as follow:
 * <ul>
 * <li> Endpoint - URL that points to the servicebus namespace
 * <li> EntityPath - Path to the service bus entity (queue/topic/subscription/). For queues and topics, it is just the entity name. For subscriptions, path is &lt;topicName&gt;/subscriptions/&lt;subscriptionName&gt;
 * <li> SharedAccessKeyName - Key name to the corresponding shared access policy rule for the namespace, or entity.
 * <li> SharedAccessKey - Key value for the corresponding shared access policy rule of the namespace or entity.
 * <li> SharedAccessSignatureToken - Instead of a key name and key value, clients can provide an already generated SAS Token.
 * <li> OperationTimeout - Default timeout to be used for all senders, receiver and clients created from this connection string.
 * <li> RetryPolicy - Name of the retry policy.
 * </ul>
 * @since 1.0
 */

public class ConnectionStringBuilder
{
	private final static String END_POINT_RAW_FORMAT = "amqps://%s";

	private final static String HOSTNAME_CONFIG_NAME = "Hostname";
	private final static String ENDPOINT_CONFIG_NAME = "Endpoint";
	private final static String SHARED_ACCESS_KEY_NAME_CONFIG_NAME = "SharedAccessKeyName";
	private final static String SHARED_ACCESS_KEY_CONFIG_NAME = "SharedAccessKey";
	private final static String ALTERNATE_SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME = "SharedAccessSignature";
	private final static String SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME = "SharedAccessSignatureToken";
	private final static String TRANSPORT_TYPE_CONFIG_NAME = "TransportType";
	private final static String ENTITY_PATH_CONFIG_NAME = "EntityPath";
	private final static String OPERATION_TIMEOUT_CONFIG_NAME = "OperationTimeout";
	private final static String RETRY_POLICY_CONFIG_NAME = "RetryPolicy";
	private final static String KEY_VALUE_SEPARATOR = "=";
	private final static String KEY_VALUE_PAIR_DELIMITER = ";";

	private static final String ALL_KEY_ENUMERATE_REGEX = "(" + HOSTNAME_CONFIG_NAME + "|" +  ENDPOINT_CONFIG_NAME + "|" + SHARED_ACCESS_KEY_NAME_CONFIG_NAME
			+ "|" + SHARED_ACCESS_KEY_CONFIG_NAME + "|"  + SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME + "|" + ENTITY_PATH_CONFIG_NAME + "|" + OPERATION_TIMEOUT_CONFIG_NAME
			+ "|" + RETRY_POLICY_CONFIG_NAME + "|" + ALTERNATE_SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME + "|" + TRANSPORT_TYPE_CONFIG_NAME + "|" +")";

	private static final String KEYS_WITH_DELIMITERS_REGEX = KEY_VALUE_PAIR_DELIMITER + ALL_KEY_ENUMERATE_REGEX	+ KEY_VALUE_SEPARATOR;

	private String connectionString;
	private URI endpoint;
	private String sharedAccessKeyName;
	private String sharedAccessKey;
	private String sharedAccessSingatureToken;
	private String sharedAccessSignatureTokenKeyName;
	private String entityPath;
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	private TransportType transportType;
	
	/**
	 * Default operation timeout if timeout is not specified in the connection string. 30 seconds.
	 */
    public static final Duration DefaultOperationTimeout = Duration.ofSeconds(ClientConstants.DEFAULT_OPERATION_TIMEOUT_IN_SECONDS);

	private ConnectionStringBuilder(
            final URI endpointAddress,
            final String entityPath,
            final Duration operationTimeout,
            final RetryPolicy retryPolicy)
    {
        this.endpoint = endpointAddress;
        this.operationTimeout = operationTimeout;
        this.retryPolicy = retryPolicy;
        this.entityPath = entityPath;
    }
	
	private ConnectionStringBuilder(
			final URI endpointAddress,
			final String entityPath,
			final String sharedAccessKeyName,
			final String sharedAccessKey, 
			final Duration operationTimeout,
			final RetryPolicy retryPolicy)
	{
		this(endpointAddress, entityPath, operationTimeout, retryPolicy);
		this.sharedAccessKey = sharedAccessKey;
		this.sharedAccessKeyName = sharedAccessKeyName;
	}
	
	private ConnectionStringBuilder(
            final URI endpointAddress, 
            final String entityPath, 
            final String sharedAccessSingatureToken,
            final Duration operationTimeout, 
            final RetryPolicy retryPolicy)
    {
        this(endpointAddress, entityPath, operationTimeout, retryPolicy);
        this.sharedAccessSingatureToken = sharedAccessSingatureToken;
    }
	
	private ConnectionStringBuilder(
			final String namespaceName,
			final String entityPath,
			final String sharedAccessKeyName,
			final String sharedAccessKey,
			final Duration operationTimeout,
			final RetryPolicy retryPolicy)
	{
		this(Util.convertNamespaceToEndPointURI(namespaceName), entityPath, sharedAccessKeyName, sharedAccessKey, operationTimeout, retryPolicy);		
	}
	
	private ConnectionStringBuilder(
            final String namespaceName,
            final String entityPath,
            final String sharedAccessSingatureToken,
            final Duration operationTimeout,
            final RetryPolicy retryPolicy)
    {
        this(Util.convertNamespaceToEndPointURI(namespaceName), entityPath, sharedAccessSingatureToken, operationTimeout, retryPolicy);        
    }

	/**
	 * Creates a new instance from namespace, entity path and SAS Key name and value.
	 * @param namespaceName Namespace name (dns suffix - ex: .servicebus.windows.net is not required)
	 * @param entityPath Entity path. For queue or topic, use name. For subscription use &lt;topicName&gt;/subscriptions/&lt;subscriptionName&gt;
	 * @param sharedAccessKeyName Shared Access Key name
	 * @param sharedAccessKey Shared Access Key
	 */
	public ConnectionStringBuilder(
			final String namespaceName,
			final String entityPath,
			final String sharedAccessKeyName,
			final String sharedAccessKey)
	{
		this(namespaceName, entityPath, sharedAccessKeyName, sharedAccessKey, ConnectionStringBuilder.DefaultOperationTimeout, RetryPolicy.getDefault());
	}
	
	/**
     * Creates a new instance from namespace, entity path and already generated SAS token.
     * @param namespaceName Namespace name (dns suffix - ex: .servicebus.windows.net is not required)
     * @param entityPath Entity path. For queue or topic, use name. For subscription use &lt;topicName&gt;/subscriptions/&lt;subscriptionName&gt;
     * @param sharedAccessSingature Shared Access Signature already generated
     */
	public ConnectionStringBuilder(
            final String namespaceName,
            final String entityPath,
            final String sharedAccessSingature)
    {
        this(namespaceName, entityPath, sharedAccessSingature, ConnectionStringBuilder.DefaultOperationTimeout, RetryPolicy.getDefault());
    }
	

	/**
	 * Creates a new instance from endpoint address of the namesapce, entity path and SAS Key name and value
	 * @param endpointAddress namespace level endpoint. This needs to be in the format of scheme://fullyQualifiedServiceBusNamespaceEndpointName
	 * @param entityPath Entity path. For queue or topic, use name. For subscription use &lt;topicName&gt;/subscriptions/&lt;subscriptionName&gt;
	 * @param sharedAccessKeyName Shared Access Key name
	 * @param sharedAccessKey Shared Access Key
	 */
	public ConnectionStringBuilder(
			final URI endpointAddress,
			final String entityPath,
			final String sharedAccessKeyName,
			final String sharedAccessKey)
	{
		this(endpointAddress, entityPath, sharedAccessKeyName, sharedAccessKey, ConnectionStringBuilder.DefaultOperationTimeout, RetryPolicy.getDefault());
	}
	
	/**
	 * Creates a new instance from endpoint address of the namesapce, entity path and already generated SAS token.
	 * @param endpointAddress namespace level endpoint. This needs to be in the format of scheme://fullyQualifiedServiceBusNamespaceEndpointName
	 * @param entityPath Entity path. For queue or topic, use name. For subscription use &lt;topicName&gt;/subscriptions/&lt;subscriptionName&gt;
	 * @param sharedAccessSingature Shared Access Signature already generated
	 */
	public ConnectionStringBuilder(
            final URI endpointAddress,
            final String entityPath,
            final String sharedAccessSingature)
    {
        this(endpointAddress, entityPath, sharedAccessSingature, ConnectionStringBuilder.DefaultOperationTimeout, RetryPolicy.getDefault());
    }

	/**
	 * Creates a new instance from the given connection string.
	 * ConnectionString format:
	 * 		Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY
	 * or Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;SharedAccessSignatureToken=SHARED_ACCESS_SIGNATURE_TOKEN
	 * @param connectionString ServiceBus ConnectionString
	 * @throws IllegalConnectionStringFormatException when the format of the ConnectionString is not valid
	 */
	public ConnectionStringBuilder(String connectionString)
	{
		this.parseConnectionString(connectionString);
	}
	
	/**
	 * Creates a new instance from the given connection string and entity path. A connection string may or may not include the entity path.
	 * ConnectionString format:
	 * 		Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY
	 * or Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;SharedAccessSignatureToken=SHARED_ACCESS_SIGNATURE_TOKEN
	 * @param namespaceConnectionString connections string of the ServiceBus namespace. This doesn't include the entity path.
	 * @param entityPath path to the entity within the namespace
	 */
	public ConnectionStringBuilder(String namespaceConnectionString, String entityPath)
	{
		this(namespaceConnectionString);
		this.entityPath = entityPath;
	}

	/**
	 * Get the endpoint which can be used to connect to the ServiceBus Namespace
	 * @return Endpoint representing the service bus namespace
	 */
	public URI getEndpoint()
	{
		return this.endpoint;
	}

	/**
	 * Get the shared access policy key value from the connection string or null.
	 * @return Shared Access Signature key value
	 */
	public String getSasKey()
	{
		return this.sharedAccessKey;
	}

	/**
	 * Get the shared access policy owner name from the connection string or null.
	 * @return Shared Access Signature key name
	 */
	public String getSasKeyName()
	{
		return this.sharedAccessKeyName;
	}
	
	/**
	 * Returns the shared access signature token from the connection string or null. 
	 * @return Shared Access Signature Token
	 */
	public String getSharedAccessSignatureToken()
	{
	    return this.sharedAccessSingatureToken;
	}

	/**
	 * Get the entity path value from the connection string
	 * @return Entity Path
	 */
	public String getEntityPath()
	{
		return this.entityPath;
	}

	/**
	 * Gets the duration after which a pending operation like Send or RECEIVE will time out. If a timeout is not specified, it defaults to {@link #DefaultOperationTimeout}
	 * This value will be used by all operations which uses this {@link ConnectionStringBuilder}, unless explicitly over-ridden. 
	 * @return operationTimeout
	 */
	public Duration getOperationTimeout()
	{
		return (this.operationTimeout == null ? ConnectionStringBuilder.DefaultOperationTimeout : this.operationTimeout);
	}

	/**
	 * Set the OperationTimeout value in the Connection String. This value will be used by all operations which uses this {@link ConnectionStringBuilder}, unless explicitly over-ridden.
	 * <p>ConnectionString with operationTimeout is not inter-operable between java and clients in other platforms.
	 * @param operationTimeout Operation Timeout
	 */
	public void setOperationTimeout(final Duration operationTimeout)
	{
		this.operationTimeout = operationTimeout;
	}

	/**
	 * Get the retry policy instance that was created as part of this builder's creation.
	 * @return RetryPolicy applied for any operation performed using this ConnectionString
	 */
	public RetryPolicy getRetryPolicy()
	{
		return (this.retryPolicy == null ? RetryPolicy.getDefault() : this.retryPolicy);
	}

	/**
	 * Set the retry policy.
	 * <p>RetryPolicy is not Serialized as part of {@link ConnectionStringBuilder#toString()} and is not interoperable with ServiceBus clients in other platforms. 
	 * @param retryPolicy RetryPolicy applied for any operation performed using this ConnectionString
	 */
	public void setRetryPolicy(final RetryPolicy retryPolicy)
	{
		this.retryPolicy = retryPolicy;
	}


	/**
	 * TransportType on which all the communication for the Service Bus created using this ConnectionString.
	 * Default value is {@link TransportType#AMQP}.
	 *
	 * @return transportType
	 */
	public TransportType getTransportType()
	{
		return (this.transportType == null ? TransportType.AMQP : transportType);
	}

	/**
	 * Set the TransportType value in the Connection String. If no TransportType is set, this defaults to {@link TransportType#AMQP}.
	 *
	 * @param transportType Transport Type
	 * @return the {@link ConnectionStringBuilder} instance being set.
	 */
	public ConnectionStringBuilder setTransportType(final TransportType transportType)
	{
		this.transportType = transportType;
		return this;
	}

	/**
	 * Returns an inter-operable connection string that can be used to connect to ServiceBus Namespace
	 * @return connection string
	 */
	@Override
	public String toString()
	{
		if (StringUtil.isNullOrWhiteSpace(this.connectionString))
		{
			StringBuilder connectionStringBuilder = new StringBuilder();
			if (this.endpoint != null)
			{
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", ENDPOINT_CONFIG_NAME, KEY_VALUE_SEPARATOR,
						this.endpoint.toString(), KEY_VALUE_PAIR_DELIMITER));
			}

			if (!StringUtil.isNullOrWhiteSpace(this.entityPath))
			{
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", ENTITY_PATH_CONFIG_NAME,
						KEY_VALUE_SEPARATOR, this.entityPath, KEY_VALUE_PAIR_DELIMITER));
			}

			if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessKeyName))
			{
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SHARED_ACCESS_KEY_NAME_CONFIG_NAME,
						KEY_VALUE_SEPARATOR, this.sharedAccessKeyName, KEY_VALUE_PAIR_DELIMITER));
			}

			if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessKey))
			{
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s", SHARED_ACCESS_KEY_CONFIG_NAME,
						KEY_VALUE_SEPARATOR, this.sharedAccessKey));
			}
			
			if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessSingatureToken))
            {
                connectionStringBuilder.append(String.format(Locale.US, "%s%s%s", sharedAccessSignatureTokenKeyName,
                        KEY_VALUE_SEPARATOR, this.sharedAccessSingatureToken));
            }

			if (this.operationTimeout != null)
			{
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", KEY_VALUE_PAIR_DELIMITER, OPERATION_TIMEOUT_CONFIG_NAME,
						KEY_VALUE_SEPARATOR, this.operationTimeout.toString()));
			}

			if (this.retryPolicy != null)
			{
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", KEY_VALUE_PAIR_DELIMITER, RETRY_POLICY_CONFIG_NAME,
						KEY_VALUE_SEPARATOR, this.retryPolicy.toString()));
			}

			if (this.transportType != null)
			{
				connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", KEY_VALUE_PAIR_DELIMITER, TRANSPORT_TYPE_CONFIG_NAME,
						KEY_VALUE_SEPARATOR, this.transportType.toString()));
			}

			this.connectionString = connectionStringBuilder.toString();
		}

		return this.connectionString;
	}
	
	private void parseConnectionString(String connectionString)
	{
		// TODO: Trace and throw
		if (StringUtil.isNullOrWhiteSpace(connectionString))
		{
			throw new IllegalConnectionStringFormatException(String.format("connectionString cannot be empty"));
		}

		String connection = KEY_VALUE_PAIR_DELIMITER + connectionString;

		Pattern keyValuePattern = Pattern.compile(KEYS_WITH_DELIMITERS_REGEX, Pattern.CASE_INSENSITIVE);
		String[] values = keyValuePattern.split(connection);
		Matcher keys = keyValuePattern.matcher(connection);

		if (values == null || values.length <= 1 || keys.groupCount() == 0)
		{
			throw new IllegalConnectionStringFormatException("Connection String cannot be parsed.");
		}

		if (!StringUtil.isNullOrWhiteSpace((values[0])))
		{
			throw new IllegalConnectionStringFormatException(
					String.format(Locale.US, "Cannot parse part of ConnectionString: %s", values[0]));
		}

		int valueIndex = 0;
		while (keys.find())
		{
			valueIndex++;

			String key = keys.group();
			key = key.substring(1, key.length() - 1);

			if (values.length < valueIndex + 1)
			{
				throw new IllegalConnectionStringFormatException(
						String.format(Locale.US, "Value for the connection string parameter name: %s, not found", key));
			}

			if (key.equalsIgnoreCase(ENDPOINT_CONFIG_NAME))
			{
				if (this.endpoint != null)
				{
					// we have parsed the endpoint once, which means we have multiple config which is not allowed
					throw new IllegalConnectionStringFormatException(
							String.format(Locale.US, "Multiple %s and/or %s detected. Make sure only one is defined", ENDPOINT_CONFIG_NAME, HOSTNAME_CONFIG_NAME));
				}
				
				try
				{
					this.endpoint = new URI(values[valueIndex]); 
				}
				catch(URISyntaxException exception)
				{
					throw new IllegalConnectionStringFormatException(
							String.format(Locale.US, "%s should be in format scheme://fullyQualifiedServiceBusNamespaceEndpointName", ENDPOINT_CONFIG_NAME),
							exception);
				}
			}
			else if (key.equalsIgnoreCase(HOSTNAME_CONFIG_NAME))
			{
				if (this.endpoint != null)
				{
					// we have parsed the endpoint once, which means we have multiple config which is not allowed
					throw new IllegalConnectionStringFormatException(
							String.format(Locale.US, "Multiple %s and/or %s detected. Make sure only one is defined", ENDPOINT_CONFIG_NAME, HOSTNAME_CONFIG_NAME));
				}
				
				try
				{
					this.endpoint = new URI(String.format(Locale.US, END_POINT_RAW_FORMAT, values[valueIndex]));
				}
				catch(URISyntaxException exception)
				{
					throw new IllegalConnectionStringFormatException(
							String.format(Locale.US, "%s should be a fully quantified host name address", HOSTNAME_CONFIG_NAME),
							exception);
				}
			}
			else if(key.equalsIgnoreCase(SHARED_ACCESS_KEY_NAME_CONFIG_NAME))
			{
				this.sharedAccessKeyName = values[valueIndex];
			}
			else if(key.equalsIgnoreCase(SHARED_ACCESS_KEY_CONFIG_NAME))
			{
				this.sharedAccessKey = values[valueIndex];
			}
			else if(key.equalsIgnoreCase(SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME))
            {
                this.sharedAccessSingatureToken = values[valueIndex];
                this.sharedAccessSignatureTokenKeyName = SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME;
            }
			else if(key.equalsIgnoreCase(ALTERNATE_SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME))
            {
                this.sharedAccessSingatureToken = values[valueIndex];
                this.sharedAccessSignatureTokenKeyName = ALTERNATE_SHARED_ACCESS_SIGNATURE_TOKEN_CONFIG_NAME;
            }
			else if (key.equalsIgnoreCase(ENTITY_PATH_CONFIG_NAME))
			{
				this.entityPath = values[valueIndex];
			}
			else if (key.equalsIgnoreCase(OPERATION_TIMEOUT_CONFIG_NAME))
			{
				try
				{
					this.operationTimeout = Duration.parse(values[valueIndex]);
				}
				catch(DateTimeParseException exception)
				{
					throw new IllegalConnectionStringFormatException("Invalid value specified for property 'Duration' in the ConnectionString.", exception);
				}
			}
			else if (key.equalsIgnoreCase(RETRY_POLICY_CONFIG_NAME))
			{
				this.retryPolicy = values[valueIndex].equals(ClientConstants.DEFAULT_RETRY)
						? RetryPolicy.getDefault()
								: (values[valueIndex].equals(ClientConstants.NO_RETRY) ? RetryPolicy.getNoRetry() : null);

						if (this.retryPolicy == null)
							throw new IllegalConnectionStringFormatException(
									String.format(Locale.US, "Connection string parameter '%s'='%s' is not recognized",
											RETRY_POLICY_CONFIG_NAME, values[valueIndex]));
			}
			else if (key.equalsIgnoreCase(TRANSPORT_TYPE_CONFIG_NAME))
			{
				try
				{
					this.transportType = TransportType.fromString(values[valueIndex]);
				} catch (IllegalArgumentException exception)
				{
					throw new IllegalConnectionStringFormatException(
							String.format("Invalid value specified for property '%s' in the ConnectionString.", TRANSPORT_TYPE_CONFIG_NAME),
							exception);
				}
			}
			else
			{
				throw new IllegalConnectionStringFormatException(
						String.format(Locale.US, "Illegal connection string parameter name: %s", key));
			}
		}		
	}
	
	// Generates a string that is logged in traces. Excludes secrets
    public String toLoggableString()
    {
        StringBuilder connectionStringBuilder = new StringBuilder();
        if (this.endpoint != null)
        {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", ENDPOINT_CONFIG_NAME, KEY_VALUE_SEPARATOR,
                    this.endpoint.toString(), KEY_VALUE_PAIR_DELIMITER));
        }

        if (!StringUtil.isNullOrWhiteSpace(this.entityPath))
        {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", ENTITY_PATH_CONFIG_NAME,
                    KEY_VALUE_SEPARATOR, this.entityPath, KEY_VALUE_PAIR_DELIMITER));
        }
        
        return connectionStringBuilder.toString();
    }
}
