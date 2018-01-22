/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ConnectionStringBuilder} can be used to construct a connection string which can establish communication with Event Hub instances.
 * In addition to constructing a connection string, the {@link ConnectionStringBuilder} can be used to modify an existing connection string.
 * <p> Sample Code:
 * <pre>{@code
 *  // Construct a new connection string
 * 	ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder()
 * 	    .setNamespaceName("EventHubsNamespaceName")
 * 	    .setEventHubName("EventHubsEntityName")
 * 	    .setSasKeyName("SharedAccessSignatureKeyName")
 * 	    .setSasKey("SharedAccessSignatureKey")
 *
 *  string connString = connectionStringBuilder.build();
 *
 *  // Modify an existing connection string
 *  ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(existingConnectionString)
 *      .setEventHubName("SomeOtherEventHubsName")
 *      .setOperationTimeout(Duration.ofSeconds(30)
 *
 *  string connString = connectionStringBuilder.build();
 * }</pre>
 * <p>
 * A connection string is basically a string consisting of key-value pairs separated by ";".
 * The basic format is {{@literal <}key{@literal >}={@literal <}value{@literal >}[;{@literal <}key{@literal >}={@literal <}value{@literal >}]} where supported key name are as follow:
 * <ul>
 * <li> Endpoint - the URL that contains the EventHubs namespace
 * <li> EntityPath - the EventHub name which you are connecting to
 * <li> SharedAccessKeyName - the key name to the corresponding shared access policy rule for the namespace, or entity.
 * <li> SharedAccessKey - the key for the corresponding shared access policy rule of the namespace or entity.
 * </ul>
 */
public class ConnectionStringBuilder {
    final static String endpointFormat = "sb://%s.%s";
    final static String hostnameFormat = "sb://%s";
    final static String defaultDomainName = "servicebus.windows.net";

    final static String HostnameConfigName = "Hostname";    // Hostname is a key that is used in IoTHub.
    final static String EndpointConfigName = "Endpoint";    // Endpoint key is used in EventHubs. It's identical to Hostname in IoTHub.
    final static String EntityPathConfigName = "EntityPath";
    final static String OperationTimeoutConfigName = "OperationTimeout";
    final static String KeyValueSeparator = "=";
    final static String KeyValuePairDelimiter = ";";
    final static String SharedAccessKeyNameConfigName = "SharedAccessKeyName";  // We use a (KeyName, Key) pair OR the SAS token - never both.
    final static String SharedAccessKeyConfigName = "SharedAccessKey";
    final static String SharedAccessSignatureConfigName = "SharedAccessSignature";

    private static final String AllKeyEnumerateRegex = "(" + HostnameConfigName + "|" + EndpointConfigName + "|" + SharedAccessKeyNameConfigName
            + "|" + SharedAccessKeyConfigName + "|" + SharedAccessSignatureConfigName + "|" + EntityPathConfigName + "|" + OperationTimeoutConfigName
            + "|" + ")";

    private static final String KeysWithDelimitersRegex = KeyValuePairDelimiter + AllKeyEnumerateRegex
            + KeyValueSeparator;

    private URI endpoint;
    private String eventHubName;
    private String sharedAccessKeyName;
    private String sharedAccessKey;
    private String sharedAccessSignature;
    private Duration operationTimeout;

    /**
     * Creates an empty {@link ConnectionStringBuilder}. At minimum, a namespace name, an entity path, SAS key name, and SAS key
     * need to be set before a valid connection string can be built.
     *
     * For advanced users, the following replacements can be done:
     * <ul>
     *     <li>An endpoint can be provided instead of a namespace name.</li>
     *     <li>A SAS token can be provided instead of a SAS key name and SAS key.</li>
     *     <li>Optionally, users can set an operation timeout instead of using the default value.</li>
     * </ul>
     */
    public ConnectionStringBuilder() {}

    /**
     * ConnectionString format:
     * Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY
     *
     * @param connectionString EventHubs ConnectionString
     * @throws IllegalConnectionStringFormatException when the format of the ConnectionString is not valid
     */
    public ConnectionStringBuilder(String connectionString) {
        parseConnectionString(connectionString);
    }

    /**
     * Get the endpoint which can be used to connect to the EventHub instance.
     *
     * @return The currently set endpoint
     */
    public URI getEndpoint() {
        return this.endpoint;
    }

    /**
     * Set an endpoint which can be used to connect to the EventHub instance.
     *
     * @param endpoint is a combination of the namespace name and domain name. Together, these pieces make a valid
     *                 endpoint. For example, the default domain name is "servicebus.windows.net", so a sample endpoint
     *                 would look like this: "sb://namespace_name.servicebus.windows.net".
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set an endpoint which can be used to connect to the EventHub instance.
     *
     * @param namespaceName the name of the namespace to connect to.
     * @param domainName    identifies the domain the namespace is located in. For non-public and national clouds,
     *                      the domain will not be "servicebus.windows.net". Available options include:
     *                          - "servicebus.usgovcloudapi.net"
     *                          - "servicebus.cloudapi.de"
     *                          - "servicebus.chinacloudapi.cn"
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setEndpoint(String namespaceName, String domainName) {
        try {
            this.endpoint = new URI(String.format(Locale.US, endpointFormat, namespaceName, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalConnectionStringFormatException(
                    String.format(Locale.US, "Invalid namespace name: %s", namespaceName),
                    exception);
        }
        return this;
    }

    /**
     * Set a namespace name which will be used to connect to an EventHubs instance. This method adds
     * "servicebus.windows.net" as the default domain name.
     *
     * @param namespaceName the name of the namespace to connect to.
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setNamespaceName(String namespaceName) {
        return this.setEndpoint(namespaceName, defaultDomainName);
    }

    /**
     * Get the entity path value from the connection string.
     *
     * @return Entity Path
     */
    public String getEventHubName() {
        return this.eventHubName;
    }

    /**
     * Set the entity path value from the connection string.
     *
     * @param eventHubName the name of the Event Hub to connect to.
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setEventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    /**
     * Get the shared access policy key value from the connection string
     *
     * @return Shared Access Signature key
     */
    public String getSasKey() {
        return this.sharedAccessKey;
    }

    /**
     * Set the shared access policy key value from the connection string
     *
     * @param sasKey the SAS key
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setSasKey(String sasKey) {
        this.sharedAccessKey = sasKey;
        return this;
    }

    /**
     * Get the shared access policy owner name from the connection string
     *
     * @return Shared Access Signature key name.
     */
    public String getSasKeyName() {
        return this.sharedAccessKeyName;
    }

    /**
     * Set the shared access policy owner name from the connection string
     *
     * @param sasKeyName the SAS key name
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setSasKeyName(String sasKeyName) {
        this.sharedAccessKeyName = sasKeyName;
        return this;
    }

    /**
     * Get the shared access signature (also referred as SAS Token) from the connection string
     *
     * @return Shared Access Signature
     */
    public String getSharedAccessSignature() {
        return this.sharedAccessSignature;
    }

    /**
     * Set the shared access signature (also referred as SAS Token) from the connection string
     *
     * @param sharedAccessSignature the shared access key signature
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setSharedAccessSignature(String sharedAccessSignature) {
        this.sharedAccessSignature = sharedAccessSignature;
        return this;
    }

    /**
     * OperationTimeout is applied in erroneous situations to notify the caller about the relevant {@link EventHubException}
     *
     * @return operationTimeout
     */
    public Duration getOperationTimeout() {
        return (this.operationTimeout == null ? MessagingFactory.DefaultOperationTimeout : this.operationTimeout);
    }

    /**
     * Set the OperationTimeout value in the Connection String. This value will be used by all operations which uses this {@link ConnectionStringBuilder}, unless explicitly over-ridden.
     * <p>ConnectionString with operationTimeout is not inter-operable between java and clients in other platforms.
     *
     * @param operationTimeout Operation Timeout
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder setOperationTimeout(final Duration operationTimeout) {
        this.operationTimeout = operationTimeout;
        return this;
    }

    /**
     * Returns an inter-operable connection string that can be used to connect to EventHubs instances.
     *
     * @return connection string
     */
    @Override
    public String toString() {
        final StringBuilder connectionStringBuilder = new StringBuilder();
        if (this.endpoint != null) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", EndpointConfigName, KeyValueSeparator,
                    this.endpoint.toString(), KeyValuePairDelimiter));
        }

        if (!StringUtil.isNullOrWhiteSpace(this.eventHubName)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", EntityPathConfigName,
                    KeyValueSeparator, this.eventHubName, KeyValuePairDelimiter));
        }

        if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessKeyName)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SharedAccessKeyNameConfigName,
                    KeyValueSeparator, this.sharedAccessKeyName, KeyValuePairDelimiter));
        }

        if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessKey)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SharedAccessKeyConfigName,
                    KeyValueSeparator, this.sharedAccessKey, KeyValuePairDelimiter));
        }

        if (!StringUtil.isNullOrWhiteSpace(this.sharedAccessSignature)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SharedAccessSignatureConfigName,
                    KeyValueSeparator, this.sharedAccessSignature, KeyValuePairDelimiter));
        }

        if (this.operationTimeout != null) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", OperationTimeoutConfigName,
                    KeyValueSeparator, this.operationTimeout.toString(), KeyValuePairDelimiter));
        }

        connectionStringBuilder.deleteCharAt(connectionStringBuilder.length() - 1);
        return connectionStringBuilder.toString();
    }



    private void parseConnectionString(final String connectionString) {
        if (StringUtil.isNullOrWhiteSpace(connectionString)) {
            throw new IllegalConnectionStringFormatException("connectionString cannot be empty");
        }

        final String connection = KeyValuePairDelimiter + connectionString;

        final Pattern keyValuePattern = Pattern.compile(KeysWithDelimitersRegex, Pattern.CASE_INSENSITIVE);
        final String[] values = keyValuePattern.split(connection);
        final Matcher keys = keyValuePattern.matcher(connection);

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

            if (values.length < valueIndex + 1) {
                throw new IllegalConnectionStringFormatException(
                        String.format(Locale.US, "Value for the connection string parameter name: %s, not found", key));
            }

            if (key.equalsIgnoreCase(EndpointConfigName)) {
                if (this.endpoint != null) {
                    // we have parsed the endpoint once, which means we have multiple config which is not allowed
                    throw new IllegalConnectionStringFormatException(
                            String.format(Locale.US, "Multiple %s and/or %s detected. Make sure only one is defined", EndpointConfigName, HostnameConfigName));
                }

                try {
                    this.endpoint = new URI(values[valueIndex]);
                } catch (URISyntaxException exception) {
                    throw new IllegalConnectionStringFormatException(
                            String.format(Locale.US, "%s should be in format scheme://fullyQualifiedServiceBusNamespaceEndpointName", EndpointConfigName),
                            exception);
                }
            } else if (key.equalsIgnoreCase(HostnameConfigName)) {
                if (this.endpoint != null) {
                    // we have parsed the endpoint once, which means we have multiple config which is not allowed
                    throw new IllegalConnectionStringFormatException(
                            String.format(Locale.US, "Multiple %s and/or %s detected. Make sure only one is defined", EndpointConfigName, HostnameConfigName));
                }

                try {
                    this.endpoint = new URI(String.format(Locale.US, hostnameFormat, values[valueIndex]));
                } catch (URISyntaxException exception) {
                    throw new IllegalConnectionStringFormatException(
                            String.format(Locale.US, "%s should be a fully quantified host name address", HostnameConfigName),
                            exception);
                }
            } else if (key.equalsIgnoreCase(SharedAccessKeyNameConfigName)) {
                this.sharedAccessKeyName = values[valueIndex];
            } else if (key.equalsIgnoreCase(SharedAccessKeyConfigName)) {
                this.sharedAccessKey = values[valueIndex];
            } else if (key.equalsIgnoreCase(SharedAccessSignatureConfigName)) {
                this.sharedAccessSignature = values[valueIndex];
            } else if (key.equalsIgnoreCase(EntityPathConfigName)) {
                this.eventHubName = values[valueIndex];
            } else if (key.equalsIgnoreCase(OperationTimeoutConfigName)) {
                try {
                    this.operationTimeout = Duration.parse(values[valueIndex]);
                } catch (DateTimeParseException exception) {
                    throw new IllegalConnectionStringFormatException("Invalid value specified for property 'Duration' in the ConnectionString.", exception);
                }
            } else {
                throw new IllegalConnectionStringFormatException(
                        String.format(Locale.US, "Illegal connection string parameter name: %s", key));
            }
        }
    }
}
