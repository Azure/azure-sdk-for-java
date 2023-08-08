// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;

import java.net.URI;

/**
 * A utility class that parses a connection string into sections. An Event Hubs connection string is a set of key-value
 * pairs separated by semicolon. A typical example is
 * {@code "Endpoint=sb://foo.EventHub.windows.net/;SharedAccessKeyName=someKeyName;SharedAccessKey=someKeyValue"}.
 *
 * <p>A connection may have the following sections:
 * <ul>
 *     <li>Endpoint, which is mandatory. The fully qualifed namespace of the Event Hubs namespace.  It will look similar
 *     to "{your-namespace}.servicebus.windows.net"</li>
 *
 *     <li>SharedAccessKeyName and SharedAccessKey, optional, used to authenticate the access to the Event Hubs
 *     namespace or Event Hub instance.</li>
 *     <li>EntityPath, optional, the name of the Event Hub instance.</li>
 *     <li>SharedAccessSignature, optional, an alternative way to authenticate the access to an Event Hub instance.</li>
 * </ul>
 *
 * <p>When you have an Event Hubs connection string, you can use {@link EventHubClientBuilder#connectionString(String)}
 * to build a client. If you'd like to use a {@link TokenCredential} to access an Event Hub, you can use this utility
 * class to get the fully qualified namespace and entity path from the connection string and then use
 * {@link EventHubClientBuilder#credential(String, String, TokenCredential)}.</p>
 *
 * <p><strong>Sample: Construct a producer using Event Hub specific connection string</strong></p>
 *
 * <p>The code snippet below shows how to create a sync producer using a connection string that is scoped to a specific
 * Event Hub.  This can be found in the Azure Portal by navigating to the Event Hubs namespace, selecting an Event Hub,
 * then choosing "Shared access policies" in the "Settings" panel.  The visual difference between an Event Hub specific
 * connection string and an Event Hubs namespace connection string is the "EntityPath" section.  Additionally, this
 * type of connection string is scoped to that specific Event Hub instance.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct -->
 * <pre>
 * String connectionString = &quot;Endpoint=sb:&#47;&#47;demo-hub.servicebus.windows.net&#47;;SharedAccessKeyName=TestAccessKey;&quot;
 *     + &quot;SharedAccessKey=TestAccessKeyValue;EntityPath=MyEventHub&quot;;
 *
 * EventHubConnectionStringProperties properties = EventHubConnectionStringProperties.parse&#40;connectionString&#41;;
 * AzureNamedKeyCredential credential = new AzureNamedKeyCredential&#40;properties.getSharedAccessKeyName&#40;&#41;,
 *     properties.getSharedAccessKey&#40;&#41;&#41;;
 *
 * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;properties.getFullyQualifiedNamespace&#40;&#41;, properties.getEntityPath&#40;&#41;, credential&#41;
 *     .buildProducerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct -->
 *
 * <p><strong>Sample: Construct a producer using Event Hubs namespace connection string</strong></p>
 *
 * <p>The code snippet below shows how to create a sync producer using an Event Hubs namespace connection string.  This
 * can be found in the Azure Portal by navigating to the Event Hubs namespace then choosing "Shared access policies" in
 * the "Settings" panel.  The visual difference between an Event Hubs namespace connection string and an Event Hub
 * specific namespace connection string is the absence of the "EntityPath" section.  Namespace connection strings have
 * access to <i>all</i> Event Hub instances in that namespace.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.namespace -->
 * <pre>
 * String connectionString = &quot;Endpoint=sb:&#47;&#47;demo-hub.servicebus.windows.net&#47;;&quot;
 *     + &quot;SharedAccessKeyName=NamespaceAccessKey;SharedAccessKey=NamespaceAccessKeyValue&quot;;
 *
 * String eventHubName = &quot;my-event-hub&quot;;
 *
 * EventHubConnectionStringProperties properties = EventHubConnectionStringProperties.parse&#40;connectionString&#41;;
 * AzureNamedKeyCredential credential = new AzureNamedKeyCredential&#40;properties.getSharedAccessKeyName&#40;&#41;,
 *     properties.getSharedAccessKey&#40;&#41;&#41;;
 *
 * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;properties.getFullyQualifiedNamespace&#40;&#41;, eventHubName, credential&#41;
 *     .buildProducerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.namespace -->
 *
 * <p><strong>Sample: Construct a producer using a shared access signature (SAS)</strong></p>
 *
 * <p>The code snippet below shows how to create a sync producer using a shared access signature (SAS).  Shared access
 * signatures allow for granular control over access to an Event Hub.
 * <a href="https://learn.microsoft.com/en-us/azure/event-hubs/authenticate-shared-access-signature">Authenticate access
 * to Event Hubs resources using shared access signatures (SAS)</a> contains information about how to configure and
 * generate signatures.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.sas -->
 * <pre>
 * &#47;&#47; &quot;sr&quot; is the URI of the resource being accessed.
 * &#47;&#47; &quot;se&quot; is the expiration date of the signature.
 * &#47;&#47; &quot;skn&quot; is name of the authorization policy used to create the SAS
 * String connectionString = &quot;Endpoint=&#123;endpoint&#125;;EntityPath=&#123;entityPath&#125;;SharedAccessSignature=&quot;
 *     + &quot;SharedAccessSignature sr=&#123;fullyQualifiedNamespace&#125;&amp;sig=&#123;signature&#125;&amp;se=&#123;expiry&#125;&amp;skn=&#123;policyName&#125;&quot;;
 *
 * EventHubConnectionStringProperties properties = EventHubConnectionStringProperties.parse&#40;connectionString&#41;;
 * AzureSasCredential credential = new AzureSasCredential&#40;connectionString&#41;;
 *
 * EventHubConsumerClient consumer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;properties.getFullyQualifiedNamespace&#40;&#41;, properties.getEntityPath&#40;&#41;, credential&#41;
 *     .buildConsumerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.sas -->
 *
 * @see EventHubClientBuilder#connectionString(String, String)
 * @see EventHubClientBuilder#credential(String, String, AzureNamedKeyCredential)
 * @see EventHubClientBuilder#credential(String, String, AzureSasCredential)
 */
public final class EventHubConnectionStringProperties {
    private final URI endpoint;
    private final String entityPath;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;

    private EventHubConnectionStringProperties(ConnectionStringProperties properties) {
        this.endpoint = properties.getEndpoint();
        this.entityPath = properties.getEntityPath();
        this.sharedAccessKeyName = properties.getSharedAccessKeyName();
        this.sharedAccessKey = properties.getSharedAccessKey();
    }

    /**
     * Parse a Event Hub connection string into an instance of this class.
     *
     * @param connectionString The connection string to be parsed.
     *
     * @return An instance of this class.
     * @throws NullPointerException if {@code connectionString} is null.
     * @throws IllegalArgumentException if the {@code connectionString} is empty or malformed.
     */
    public static EventHubConnectionStringProperties parse(String connectionString) {
        return new EventHubConnectionStringProperties(new ConnectionStringProperties(connectionString));
    }

    /**
     * Gets the "EntityPath" value of the connection string.
     *
     * @return The entity path, or {@code null} if the connection string doesn't have an "EntityPath".
     */
    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Gets the "Endpoint" value of the connection string.
     *
     * @return The endpoint.
     */
    public String getEndpoint() {
        return String.format("%s://%s", endpoint.getScheme(), endpoint.getHost());
    }

    /**
     * Gets the fully qualified namespace, or hostname, from the connection string "Endpoint" section.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return endpoint.getHost();
    }

    /**
     * Gets the "SharedAccessKeyName" section of the connection string.
     *
     * @return The shared access key name, or {@code null} if the connection string doesn't have a
     *     "SharedAccessKeyName".
     */
    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    /**
     * Gets the "SharedAccessSignature" section of the connection string.
     *
     * @return The shared access key value, or {@code null} if the connection string doesn't have a
     *     "SharedAccessSignature".
     */
    public String getSharedAccessKey() {
        return sharedAccessKey;
    }

}
