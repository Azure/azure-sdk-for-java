// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure Service Bus client library allows Java developers to interact with Azure Service Bus entities by
 * publishing to and/or subscribing from queues and topics/subscriptions.
 * <a href="https://docs.microsoft.com/azure/service-bus-messaging">Microsoft Azure Service Bus</a> is a fully
 * managed enterprise integration message broker. Service Bus can decouple applications and services. Service Bus
 * offers a reliable and secure platform for asynchronous transfer of data and state. Data is transferred between
 * different applications and services using messages.</p>
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
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation</a>.
 * </p>
 *
 * <h3>Publishing Service Bus messages</h3>
 *
 * <p>This library provides two sender clients to publish messages to Azure Service Bus.  The async client,
 * {@link com.azure.messaging.servicebus.ServiceBusSenderAsyncClient} and its sync version,
 * {@link com.azure.messaging.servicebus.ServiceBusSenderClient}.  The samples below demonstrate basic scenarios,
 * additional snippets can be found in the class documentation for
 * {@link com.azure.messaging.servicebus.ServiceBusClientBuilder} and any of the clients.</p>
 *
 * <p><strong>Sample: Construct a synchronous sender and send messages</strong></p>
 *
 * <p>The following code sample demonstrates the creation and use of the synchronous client
 * {@link com.azure.messaging.servicebus.ServiceBusSenderClient} to send messages to a queue.  When performance is
 * important, consider using {@link com.azure.messaging.servicebus.ServiceBusMessageBatch} to publish multiple messages
 * at once.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusSenderClient sender = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sender&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .buildClient&#40;&#41;;
 *
 * List&lt;ServiceBusMessage&gt; messages = Arrays.asList&#40;
 *     new ServiceBusMessage&#40;&quot;test-1&quot;&#41;,
 *     new ServiceBusMessage&#40;&quot;test-2&quot;&#41;&#41;;
 *
 * &#47;&#47; Creating a batch without options set.
 * ServiceBusMessageBatch batch = sender.createMessageBatch&#40;&#41;;
 * for &#40;ServiceBusMessage message : messages&#41; &#123;
 *     if &#40;batch.tryAddMessage&#40;message&#41;&#41; &#123;
 *         continue;
 *     &#125;
 *
 *     &#47;&#47; The batch is full. Send the current batch and create a new one.
 *     sender.sendMessages&#40;batch&#41;;
 *
 *     batch = sender.createMessageBatch&#40;&#41;;
 *
 *     &#47;&#47; Add the message we couldn't before.
 *     if &#40;!batch.tryAddMessage&#40;message&#41;&#41; &#123;
 *         throw new IllegalArgumentException&#40;&quot;Message is too large for an empty batch.&quot;&#41;;
 *     &#125;
 * &#125;
 *
 * &#47;&#47; Send the final batch if there are any messages in it.
 * if &#40;batch.getCount&#40;&#41; &gt; 0&#41; &#123;
 *     sender.sendMessages&#40;batch&#41;;
 * &#125;
 *
 * &#47;&#47; Continue using the sender and finally, dispose of the sender.
 * &#47;&#47; Clients should be long-lived objects as they require resources
 * &#47;&#47; and time to establish a connection to the service.
 * sender.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch -->
 *
 * <h3>Receiving Service Bus messages</h3>
 *
 * <p>This library provides several clients to receive messages from Azure Service Bus.  The async client,
 * {@link com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient} and its sync version,
 * {@link com.azure.messaging.servicebus.ServiceBusReceiverClient}.  For session-enabled entities, there is
 * {@link com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient} and
 * {@link com.azure.messaging.servicebus.ServiceBusSessionReceiverClient}.  In production scenarios,
 * {@link com.azure.messaging.servicebus.ServiceBusProcessorClient} is recommended because it recovers from transient
 * errors such as temporary network failures.</p>
 *
 * <p>The samples below demonstrate basic scenarios, additional snippets can be found in the class documentation.</p>
 *
 * <p><strong>Sample: Create a ServiceBusProcessorClient and receive messages</strong></p>
 *
 * <p>The following code sample demonstrates the creation and use of the synchronous client
 * {@link com.azure.messaging.servicebus.ServiceBusProcessorClient} to receive messages from a Service Bus queue.
 * By default, messages are received using {@link com.azure.messaging.servicebus.models.ServiceBusReceiveMode#PEEK_LOCK}
 * and customers must settle their messages using one of the settlement methods on the receiver client.
 * "<a href="https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#peeklock">
 *     "Settling receive operations</a>" provides additional information about message settlement.
 * {@link com.azure.messaging.servicebus.ServiceBusProcessorClient} continues fetching messages from the queue until
 * the processor is stopped.  If it encounters a transient error, it will try to recover, then continue processing
 * messages.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation -->
 * <pre>
 * &#47;&#47; Function that gets called whenever a message is received.
 * Consumer&lt;ServiceBusReceivedMessageContext&gt; processMessage = context -&gt; &#123;
 *     final ServiceBusReceivedMessage message = context.getMessage&#40;&#41;;
 *     &#47;&#47; Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
 *     &#47;&#47; handling message reaches desired state such that it doesn't require Service Bus to redeliver
 *     &#47;&#47; the same message, then context.complete&#40;&#41; should be called otherwise context.abandon&#40;&#41;.
 *     final boolean success = Math.random&#40;&#41; &lt; 0.5;
 *     if &#40;success&#41; &#123;
 *         try &#123;
 *             context.complete&#40;&#41;;
 *         &#125; catch &#40;RuntimeException error&#41; &#123;
 *             System.out.printf&#40;&quot;Completion of the message %s failed.%n Error: %s%n&quot;,
 *                 message.getMessageId&#40;&#41;, error&#41;;
 *         &#125;
 *     &#125; else &#123;
 *         try &#123;
 *             context.abandon&#40;&#41;;
 *         &#125; catch &#40;RuntimeException error&#41; &#123;
 *             System.out.printf&#40;&quot;Abandoning of the message %s failed.%nError: %s%n&quot;,
 *                 message.getMessageId&#40;&#41;, error&#41;;
 *         &#125;
 *     &#125;
 * &#125;;
 *
 * &#47;&#47; Sample code that gets called if there's an error
 * Consumer&lt;ServiceBusErrorContext&gt; processError = errorContext -&gt; &#123;
 *     if &#40;errorContext.getException&#40;&#41; instanceof ServiceBusException&#41; &#123;
 *         ServiceBusException exception = &#40;ServiceBusException&#41; errorContext.getException&#40;&#41;;
 *
 *         System.out.printf&#40;&quot;Error source: %s, reason %s%n&quot;, errorContext.getErrorSource&#40;&#41;,
 *             exception.getReason&#40;&#41;&#41;;
 *     &#125; else &#123;
 *         System.out.printf&#40;&quot;Error occurred: %s%n&quot;, errorContext.getException&#40;&#41;&#41;;
 *     &#125;
 * &#125;;
 *
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Create the processor client via the builder and its sub-builder
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .processor&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .receiveMode&#40;ServiceBusReceiveMode.PEEK_LOCK&#41;
 *     .disableAutoComplete&#40;&#41;  &#47;&#47; Make sure to explicitly opt in to manual settlement &#40;e.g. complete, abandon&#41;.
 *     .processMessage&#40;processMessage&#41;
 *     .processError&#40;processError&#41;
 *     .buildProcessorClient&#40;&#41;;
 *
 * &#47;&#47; Starts the processor in the background. Control returns immediately.
 * processorClient.start&#40;&#41;;
 *
 * &#47;&#47; Stop processor and dispose when done processing messages.
 * processorClient.stop&#40;&#41;;
 * processorClient.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation -->
 *
 * <p><strong>Sample: Create a receiver and receive messages</strong></p>
 *
 * <p>The following code sample demonstrates the creation and use of the synchronous client
 * {@link com.azure.messaging.servicebus.ServiceBusReceiverClient} to receive messages from a Service Bus subscription.
 * The receive operation returns when either 10 messages are received or 30 seconds has elapsed.  By default, messages
 * are received using {@link com.azure.messaging.servicebus.models.ServiceBusReceiveMode#PEEK_LOCK} and customers must
 * settle their messages using one of the settlement methods on the receiver client.
 * "<a href="https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#peeklock">
 *     "Settling receive operations</a>" provides additional information about message settlement.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.receiveMessages-int-duration -->
 * <pre>
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusReceiverClient receiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .receiver&#40;&#41;
 *     .topicName&#40;topicName&#41;
 *     .subscriptionName&#40;subscriptionName&#41;
 *     .buildClient&#40;&#41;;
 *
 * &#47;&#47; Receives a batch of messages when 10 messages are received or until 30 seconds have elapsed, whichever
 * &#47;&#47; happens first.
 * IterableStream&lt;ServiceBusReceivedMessage&gt; messages = receiver.receiveMessages&#40;10, Duration.ofSeconds&#40;30&#41;&#41;;
 * messages.forEach&#40;message -&gt; &#123;
 *     System.out.printf&#40;&quot;Id: %s. Contents: %s%n&quot;, message.getMessageId&#40;&#41;, message.getBody&#40;&#41;&#41;;
 *
 *     &#47;&#47; If able to process message, complete it. Otherwise, abandon it and allow it to be
 *     &#47;&#47; redelivered.
 *     if &#40;isMessageProcessed&#41; &#123;
 *         receiver.complete&#40;message&#41;;
 *     &#125; else &#123;
 *         receiver.abandon&#40;message&#41;;
 *     &#125;
 * &#125;&#41;;
 *
 * &#47;&#47; When program ends, or you're done receiving all messages, dispose of the receiver.
 * &#47;&#47; Clients should be long-lived objects as they
 * &#47;&#47; require resources and time to establish a connection to the service.
 * receiver.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.receiveMessages-int-duration -->
 */
package com.azure.messaging.servicebus;
