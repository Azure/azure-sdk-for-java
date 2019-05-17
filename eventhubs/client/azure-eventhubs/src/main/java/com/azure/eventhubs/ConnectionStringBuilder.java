// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.util.ImplUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ConnectionStringBuilder} can be used to construct a connection string which can establish communication with Event Hub instances.
 * In addition to constructing a connection string, the {@link ConnectionStringBuilder} can be used to modify an existing connection string.
 * <p> Sample Code:
 * <pre>{@code
 *  // Construct a new connection string
 *  ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder()
 *      .namespaceName("EventHubsNamespaceName")
 *      .eventHubName("EventHubsEntityName")
 *      .sasKeyName("SharedAccessSignatureKeyName")
 *      .setSasKey("SharedAccessSignatureKey")
 *
 *  string connString = connectionStringBuilder.build();
 *
 *  // Modify an existing connection string
 *  ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(existingConnectionString)
 *      .eventHubName("SomeOtherEventHubsName")
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
public final class ConnectionStringBuilder {
    private static final String END_POINT_FORMAT = "sb://%s.%s";
    private static final String HOST_NAME_FORMAT = "sb://%s";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net";

    private static final String HOST_NAME_CONFIG_NAME = "Hostname";    // Hostname is a key that is used in IoTHub.
    private static final String ENDPOINT_CONFIG_NAME = "Endpoint";    // Endpoint key is used in EventHubs. It's identical to Hostname in IoTHub.
    private static final String ENTITY_PATH_CONFIG_NAME = "EntityPath";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String KEY_VALUE_PAIR_DELIMITER = ";";
    private static final String SHARED_ACCESS_KEY_NAME_CONFIG_NAME = "SharedAccessKeyName";  // We use a (KeyName, Key) pair OR the SAS token - never both.
    private static final String SHARED_ACCESS_KEY_CONFIG_NAME = "SharedAccessKey";
    private static final String SHARED_ACCESS_SIGNATURE_CONFIG_NAME = "SharedAccessSignature";

    private static final String ALL_KEY_ENUMERATE_REGEX = "(" + HOST_NAME_CONFIG_NAME + "|" + ENDPOINT_CONFIG_NAME + "|"
        + SHARED_ACCESS_KEY_NAME_CONFIG_NAME + "|" + SHARED_ACCESS_KEY_CONFIG_NAME + "|"
        + SHARED_ACCESS_SIGNATURE_CONFIG_NAME + "|" + ENTITY_PATH_CONFIG_NAME + ")";

    private static final String KEYS_WITH_DELIMITERS_REGEX = KEY_VALUE_PAIR_DELIMITER + ALL_KEY_ENUMERATE_REGEX
            + KEY_VALUE_SEPARATOR;

    private URI endpoint;
    private String eventHubName;
    private String sharedAccessKeyName;
    private String sharedAccessKey;
    private String sharedAccessSignature;

    /**
     * Creates an empty {@link ConnectionStringBuilder}. At minimum, a namespace name, an entity path, SAS key name, and SAS key
     * need to be set before a valid connection string can be built.
     *
     * <p>
     * For advanced users, the following replacements can be done:
     * <ul>
     *      <li>An endpoint can be provided instead of a namespace name.</li>
     *      <li>A SAS token can be provided instead of a SAS key name and SAS key.</li>
     *      <li>Optionally, users can set an operation timeout instead of using the default value.</li>
     * </ul>
     */
    public ConnectionStringBuilder() {
    }

    /**
     * ConnectionString format:
     * Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY
     *
     * @param connectionString EventHubs ConnectionString
     * @throws IllegalArgumentException when the format of the ConnectionString is not valid
     */
    public ConnectionStringBuilder(String connectionString) {
        parseConnectionString(connectionString);
    }

    /**
     * Get the endpoint which can be used to connect to the EventHub instance.
     *
     * @return The currently set endpoint
     */
    public URI endpoint() {
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
    public ConnectionStringBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set an endpoint which can be used to connect to the EventHub instance.
     *
     * @param namespaceName the name of the namespace to connect to.
     * @param domainName    identifies the domain the namespace is located in. For non-public and national clouds,
     *                      the domain will not be "servicebus.windows.net". Available options include:
     *                      - "servicebus.usgovcloudapi.net"
     *                      - "servicebus.cloudapi.de"
     *                      - "servicebus.chinacloudapi.cn"
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder endpoint(String namespaceName, String domainName) {
        try {
            this.endpoint = new URI(String.format(Locale.US, END_POINT_FORMAT, namespaceName, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(
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
    public ConnectionStringBuilder namespaceName(String namespaceName) {
        return this.endpoint(namespaceName, DEFAULT_DOMAIN_NAME);
    }

    /**
     * Get the entity path value from the connection string.
     *
     * @return Entity Path
     */
    public String eventHubName() {
        return this.eventHubName;
    }

    /**
     * Set the entity path value from the connection string.
     *
     * @param eventHubName the name of the Event Hub to connect to.
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    /**
     * Get the shared access policy key value from the connection string
     *
     * @return Shared Access Signature key
     */
    public String sasKey() {
        return this.sharedAccessKey;
    }

    /**
     * Set the shared access policy key value from the connection string
     *
     * @param sasKey the SAS key
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder sasKey(String sasKey) {
        this.sharedAccessKey = sasKey;
        return this;
    }

    /**
     * Get the shared access policy owner name from the connection string
     *
     * @return Shared Access Signature key name.
     */
    public String sasKeyName() {
        return this.sharedAccessKeyName;
    }

    /**
     * Set the shared access policy owner name from the connection string
     *
     * @param sasKeyName the SAS key name
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder sasKeyName(String sasKeyName) {
        this.sharedAccessKeyName = sasKeyName;
        return this;
    }

    /**
     * Get the shared access signature (also referred as SAS Token) from the connection string
     *
     * @return Shared Access Signature
     */
    public String sharedAccessSignature() {
        return this.sharedAccessSignature;
    }

    /**
     * Set the shared access signature (also referred as SAS Token) from the connection string
     *
     * @param sharedAccessSignature the shared access key signature
     * @return the {@link ConnectionStringBuilder} being set.
     */
    public ConnectionStringBuilder sharedAccessSignature(String sharedAccessSignature) {
        this.sharedAccessSignature = sharedAccessSignature;
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
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", ENDPOINT_CONFIG_NAME, KEY_VALUE_SEPARATOR,
                    this.endpoint.toString(), KEY_VALUE_PAIR_DELIMITER));
        }

        if (!ImplUtils.isNullOrWhiteSpace(this.eventHubName)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", ENTITY_PATH_CONFIG_NAME,
                    KEY_VALUE_SEPARATOR, this.eventHubName, KEY_VALUE_PAIR_DELIMITER));
        }

        if (!ImplUtils.isNullOrWhiteSpace(this.sharedAccessKeyName)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SHARED_ACCESS_KEY_NAME_CONFIG_NAME,
                    KEY_VALUE_SEPARATOR, this.sharedAccessKeyName, KEY_VALUE_PAIR_DELIMITER));
        }

        if (!ImplUtils.isNullOrWhiteSpace(this.sharedAccessKey)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SHARED_ACCESS_KEY_CONFIG_NAME,
                    KEY_VALUE_SEPARATOR, this.sharedAccessKey, KEY_VALUE_PAIR_DELIMITER));
        }

        if (!ImplUtils.isNullOrWhiteSpace(this.sharedAccessSignature)) {
            connectionStringBuilder.append(String.format(Locale.US, "%s%s%s%s", SHARED_ACCESS_SIGNATURE_CONFIG_NAME,
                    KEY_VALUE_SEPARATOR, this.sharedAccessSignature, KEY_VALUE_PAIR_DELIMITER));
        }

        connectionStringBuilder.deleteCharAt(connectionStringBuilder.length() - 1);
        return connectionStringBuilder.toString();
    }

    private void parseConnectionString(final String connectionString) {
        if (ImplUtils.isNullOrWhiteSpace(connectionString)) {
            throw new IllegalArgumentException("connectionString cannot be empty");
        }

        final String connection = KEY_VALUE_PAIR_DELIMITER + connectionString;

        final Pattern keyValuePattern = Pattern.compile(KEYS_WITH_DELIMITERS_REGEX, Pattern.CASE_INSENSITIVE);
        final String[] values = keyValuePattern.split(connection);
        final Matcher keys = keyValuePattern.matcher(connection);

        if (values == null || values.length <= 1 || keys.groupCount() == 0) {
            throw new IllegalArgumentException("Connection String cannot be parsed.");
        }

        if (!ImplUtils.isNullOrWhiteSpace((values[0]))) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Cannot parse part of ConnectionString: %s", values[0]));
        }

        int valueIndex = 0;
        while (keys.find()) {
            valueIndex++;

            String key = keys.group();
            key = key.substring(1, key.length() - 1);

            if (values.length < valueIndex + 1) {
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Value for the connection string parameter name: %s, not found", key));
            }

            if (key.equalsIgnoreCase(ENDPOINT_CONFIG_NAME)) {
                if (this.endpoint != null) {
                    // we have parsed the endpoint once, which means we have multiple config which is not allowed
                    throw new IllegalArgumentException(
                            String.format(Locale.US, "Multiple %s and/or %s detected. Make sure only one is defined", ENDPOINT_CONFIG_NAME, HOST_NAME_CONFIG_NAME));
                }

                try {
                    this.endpoint = new URI(values[valueIndex]);
                } catch (URISyntaxException exception) {
                    throw new IllegalArgumentException(
                            String.format(Locale.US, "%s should be in format scheme://fullyQualifiedServiceBusNamespaceEndpointName", ENDPOINT_CONFIG_NAME),
                            exception);
                }
            } else if (key.equalsIgnoreCase(HOST_NAME_CONFIG_NAME)) {
                if (this.endpoint != null) {
                    // we have parsed the endpoint once, which means we have multiple config which is not allowed
                    throw new IllegalArgumentException(
                            String.format(Locale.US, "Multiple %s and/or %s detected. Make sure only one is defined", ENDPOINT_CONFIG_NAME, HOST_NAME_CONFIG_NAME));
                }

                try {
                    this.endpoint = new URI(String.format(Locale.US, HOST_NAME_FORMAT, values[valueIndex]));
                } catch (URISyntaxException exception) {
                    throw new IllegalArgumentException(
                            String.format(Locale.US, "%s should be a fully quantified host name address", HOST_NAME_CONFIG_NAME),
                            exception);
                }
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY_NAME_CONFIG_NAME)) {
                this.sharedAccessKeyName = values[valueIndex];
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY_CONFIG_NAME)) {
                this.sharedAccessKey = values[valueIndex];
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_SIGNATURE_CONFIG_NAME)) {
                this.sharedAccessSignature = values[valueIndex];
            } else if (key.equalsIgnoreCase(ENTITY_PATH_CONFIG_NAME)) {
                this.eventHubName = values[valueIndex];
            } else {
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Illegal connection string parameter name: %s", key));
            }
        }
    }
}
