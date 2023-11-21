// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure Service Bus Administration client library allows for management of entities in their Service Bus
 * namespace.  It can be used to create, delete, update, or list queues, topics, rules, and subscriptions.</p>

 * <p><a href="https://docs.microsoft.com/azure/service-bus-messaging">Microsoft Azure Service Bus</a> is a fully
 * managed enterprise integration message broker. Service Bus can decouple applications and services. Service Bus
 * offers a reliable and secure platform for asynchronous transfer of data and state. Data is transferred between
 * different applications and services using messages.</p>
 *
 * <h2>Key Concepts</h2>
 *
 * <ul>
 *     <li><strong>
 *     <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#queues">Queue</a>
 *     :</strong>  Allows for the sending and receiving of messages, ordered first-in-first-out (FIFO).  It is often
 *     used for point to point communication.</li>
 *
 *     <li><strong>
 *     <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#topics">Topic</a>
 *     :</strong>  Allows for sending messages to multiple receivers, simultaneously.  This is suited for publisher and
 *     subscriber scenarios.</li>
 *
 *     <li><strong>
 *     <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-queues-topics-subscriptions#topics-and-subscriptions">
 *     Subscription</a>:</strong>  Receives messages from a topic.  Each subscription is independent and receives a
 *     copy of every message sent to the topic.  Each subscription has a filter.  Filters, also known as rules, are
 *     applied to each message to determine whether they will be published to the subscription.</li>
 *
 *     <li><strong>Rule Filters:</strong>  A filter, associated with a subscription, that is tested against every
 *     message sent to a Service Bus topic.  If the filter returns true, a copy of the message is published to that
 *     subscription.  There are 3 types of filters: SQL filters, boolean filters, and correlation filters.  More
 *     information can be found in: <a href="https://learn.microsoft.com/azure/service-bus-messaging/topic-filters">
 *         Topic filters</a>.</li>
 *
 *     <li><strong><a href="https://learn.microsoft.com/azure/service-bus-messaging/topic-filters#actions">Rule Actions
 *     </a>:</strong> A modification applied to a Service Bus message when it matches the associated rule filter.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Service Bus.
 * {@link com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient} and
 * {@link com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient} are the sync and async
 * clients for managing entities in the Service Bus namespace.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation</a>.
 * </p>
 *
 * <h3>Creating clients</h3>
 *
 * <p><strong>Sample: Create a ServiceBusAdministrationClient</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous administration client.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation -->
 * <pre>
 * HttpLogOptions logOptions = new HttpLogOptions&#40;&#41;
 *     .setLogLevel&#40;HttpLogDetailLevel.HEADERS&#41;;
 *
 * &#47;&#47; DefaultAzureCredential creates a credential based on the environment it is executed in.
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .httpLogOptions&#40;logOptions&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.instantiation -->
 *
 * <h3>Managing queues</h3>
 *
 * <p><strong>Sample: Create a queue</strong></p>

 * <p>The following code sample demonstrates the creation of a Service Bus queue with some configured options.  If
 * {@link com.azure.messaging.servicebus.administration.models.CreateQueueOptions} are not passed in, default values
 * are used to create the queue.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string-createqueueoptions -->
 * <pre>
 * CreateQueueOptions queueOptions = new CreateQueueOptions&#40;&#41;
 *     .setLockDuration&#40;Duration.ofMinutes&#40;2&#41;&#41;
 *     .setMaxDeliveryCount&#40;15&#41;;
 *
 * QueueProperties queue = client.createQueue&#40;&quot;my-new-queue&quot;, queueOptions&#41;;
 * System.out.printf&#40;&quot;Queue created. Name: %s. Lock Duration: %s.%n&quot;,
 *     queue.getName&#40;&#41;, queue.getLockDuration&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.createqueue#string-createqueueoptions -->
 *
 * <p><strong>Sample: Update a queue</strong></p>
 *
 * <p>The following code sample demonstrates updating a Service bus queue.  Users should fetch the queue's properties,
 * modify the properties, and then pass the object to update method.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatequeue -->
 * <pre>
 * QueueProperties queue = client.getQueue&#40;&quot;queue-that-exists&quot;&#41;;
 *
 * queue.setLockDuration&#40;Duration.ofMinutes&#40;3&#41;&#41;
 *     .setMaxDeliveryCount&#40;15&#41;
 *     .setDeadLetteringOnMessageExpiration&#40;true&#41;;
 *
 * QueueProperties updatedQueue = client.updateQueue&#40;queue&#41;;
 *
 * System.out.printf&#40;&quot;Queue updated. Name: %s. Lock duration: %s. Max delivery count: %s.%n&quot;,
 *     updatedQueue.getName&#40;&#41;, updatedQueue.getLockDuration&#40;&#41;, updatedQueue.getMaxDeliveryCount&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatequeue -->
 *
 * <h3>Managing topics and subscriptions</h3>
 *
 * <p><strong>Sample: Create a topic, subscription, and rule</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a Service Bus topic and subscription.  The subscription
 * filters for messages with a correlation id {@code "emails"} and has a {@code "importance"} property set
 * to {@code "high"}.  Consequently, all high importance Service Bus messages will be delivered to the
 * {@code "high-importance-subscription"} subscription. See
 * <a href="https://learn.microsoft.com/azure/service-bus-messaging/topic-filters">Topic filters</a> for additional
 * information.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.createsubscription#string-string-string -->
 * <pre>
 * String topicName = &quot;my-new-topic&quot;;
 * TopicProperties topic = client.createTopic&#40;topicName&#41;;
 *
 * String subscriptionName = &quot;high-importance-subscription&quot;;
 * String ruleName = &quot;important-emails-filter&quot;;
 * CreateSubscriptionOptions subscriptionOptions = new CreateSubscriptionOptions&#40;&#41;
 *     .setMaxDeliveryCount&#40;15&#41;
 *     .setLockDuration&#40;Duration.ofMinutes&#40;2&#41;&#41;;
 *
 * CorrelationRuleFilter ruleFilter = new CorrelationRuleFilter&#40;&#41;
 *     .setCorrelationId&#40;&quot;emails&quot;&#41;;
 * ruleFilter.getProperties&#40;&#41;.put&#40;&quot;importance&quot;, &quot;high&quot;&#41;;
 *
 * CreateRuleOptions createRuleOptions = new CreateRuleOptions&#40;&#41;
 *     .setFilter&#40;ruleFilter&#41;;
 *
 * SubscriptionProperties subscription = client.createSubscription&#40;topicName, subscriptionName, ruleName,
 *     subscriptionOptions, createRuleOptions&#41;;
 *
 * System.out.printf&#40;&quot;Subscription created. Name: %s. Topic name: %s. Lock Duration: %s.%n&quot;,
 *     subscription.getSubscriptionName&#40;&#41;, subscription.getTopicName&#40;&#41;, subscription.getLockDuration&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.createsubscription#string-string-string -->
 *
 * <p><strong>Sample: Update a subscription</strong></p>
 *
 * <p>The following code sample demonstrates updating an existing subscription.  Users should fetch the subscription,
 * modify the properties, and pass that object into the update method.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatesubscription#subscriptionproperties -->
 * <pre>
 * &#47;&#47; To update the subscription we have to:
 * &#47;&#47; 1. Get the subscription info from the service.
 * &#47;&#47; 2. Update the SubscriptionProperties we want to change.
 * &#47;&#47; 3. Call the updateSubscription&#40;&#41; with the updated object.
 * SubscriptionProperties subscription = client.getSubscription&#40;&quot;my-topic&quot;, &quot;my-subscription&quot;&#41;;
 *
 * System.out.println&#40;&quot;Original delivery count: &quot; + subscription.getMaxDeliveryCount&#40;&#41;&#41;;
 *
 * &#47;&#47; Updating it to a new value.
 * subscription.setMaxDeliveryCount&#40;5&#41;;
 *
 * &#47;&#47; Persisting the updates to the subscription object.
 * SubscriptionProperties updated = client.updateSubscription&#40;subscription&#41;;
 *
 * System.out.printf&#40;&quot;Subscription updated. Name: %s. Delivery count: %s.%n&quot;,
 *     updated.getSubscriptionName&#40;&#41;, updated.getMaxDeliveryCount&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.updatesubscription#subscriptionproperties -->
 */
package com.azure.messaging.servicebus.administration;
