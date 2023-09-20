// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://docs.microsoft.com/azure/service-bus-messaging">Microsoft Azure Service Bus</a> is a fully
 * managed enterprise integration message broker. Service Bus can decouple applications and services. Service Bus
 * offers a reliable and secure platform for asynchronous transfer of data and state. Data is transferred between
 * different applications and services using messages.</p>
 *
 * <p>The Azure Service Bus client library allows Java developers to interact with Azure Service Bus entities by
 * publishing to and/or subscribing from queues and topics/subscriptions.</p>
 *
 * <h2>Key Concepts</h2>
 *
 * <ul>
 *     <li><strong>
 *     <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#queues">Queue</a>
 *     :</strong>  Allows for the sending and receiving of messages, ordered first-in-first-out(FIFO).  It is often
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
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Event Hubs.
 * {@link com.azure.messaging.servicebus.ServiceBusSenderClient} and
 * {@link com.azure.messaging.servicebus.ServiceBusSenderAsyncClient} are the sync and async
 * clients for publishing messages to a Service Bus queue or topic.  Similarly,
 * {@link com.azure.messaging.servicebus.ServiceBusReceiverClient} and
 * {@link com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient} are the sync and async clients for consuming
 * messages from a Service Bus queue or topic.  In production scenarios, we recommend customers leverage
 * {@link com.azure.messaging.servicebus.ServiceBusProcessorClient} for consuming messages because recovers from
 * transient failures.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <h3>Publishing Service Bus messages</h3>
 *
 * <p>This library provides two sender clients to publish messages to Azure Service Bus.  The async client,
 * {@link com.azure.messaging.servicebus.ServiceBusSenderAsyncClient} and its sync version,
 * {@link com.azure.messaging.servicebus.ServiceBusSenderClient}.  The samples below demonstrate basic scenarios,
 * additional snippets can be found in the class documentation.</p>
 *
 * <p><strong>Sample: Construct a synchronous sender and send messages</strong></p>
 *
 * <p>The following code sample demonstrates the creation and use of the synchronous client
 * {@link com.azure.messaging.servicebus.ServiceBusSenderClient}.  When performance is very important, consider using
 * {@link com.azure.messaging.servicebus.ServiceBusMessageBatch} to publish multiple messages at once.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch -->
 * <!-- end com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch -->
 */
package com.azure.messaging.servicebus;
