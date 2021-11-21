// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The sample demonstrates how to use dead letter queues:
 * <ul>
 * <li>
 * <b>Scenario 1:</b> Send a message and then retrieve and abandon the message until the maximum delivery count is
 * exhausted and the message is automatically dead-lettered.
 * </li>
 * <li>
 * <b>Scenario 2:</b> Send a set of messages, and explicitly dead-letter messages that do not match a certain criterion
 * and would therefore not be processed correctly. The messages are then picked up from the dead-letter queue, are
 * automatically corrected, and resubmitted.
 * </li>
 * </ul>
 *
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead-letter
 *     Queue</a>
 */
public class DeadletterQueueSample {
    private final List<Person> personList = Arrays.asList(
        new Person("Einstein", "Albert"),
        new Person("Heisenberg", "Werner"),
        new Person("Curie", "Marie"),
        new Person("Hawking", "Steven"),
        new Person("Newton", "Isaac"),
        new Person("Bohr", "Niels"),
        new Person("Faraday", "Michael"),
        new Person("Galilei", "Galileo"),
        new Person("Kepler", "Johannes"),
        new Person("Kopernikus", "Nikolaus")
    );

    /**
     * Main method to show how to dead letter within an Azure Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        final DeadletterQueueSample sample = new DeadletterQueueSample();
        sample.run();
    }

    /**
     * Run method to invoke this demo on how to dead letter within an Azure Service Bus Queue.
     */
    @Test
    public void run() {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "queueName" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.
        final String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        final String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder().connectionString(connectionString);

        try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient()) {
            // Scenario 1: Dead letters a message by abandoning it until the MaxDeliveryCount is exceeded.
            sendMessages(sender, 1);
            deadLetterByExceedingMaxDelivery(builder, queueName).block();
            receiveAndCompleteDeadLetterQueueMessages(builder, queueName).block();

            // Scenario 2: Dead letters a message by explicitly invoking receiver.deadLetter().
            sendMessages(sender, personList.size());
            receiveAndDeadletterMessages(builder, queueName).block();
            receiveAndFixDeadLetterQueueMessages(builder, queueName, sender).block();
        }
    }

    /**
     * Sends {@link ServiceBusMessage messages} to an Azure Service Bus Queue.
     *
     * @param sender Sender client.
     * @param maxMessages Maximum number of messages to send.
     */
    private void sendMessages(ServiceBusSenderClient sender, int maxMessages) {
        final int numberOfMessages = Math.min(personList.size(), maxMessages);

        final List<ServiceBusMessage> serviceBusMessages = IntStream.range(0, numberOfMessages)
            .mapToObj(index -> {
                final Person person = personList.get(index);

                return new ServiceBusMessage(person.toJson())
                    .setContentType("application/json")
                    .setSubject(index % 2 == 0 ? "Scientist" : "Physicist")
                    .setMessageId(Integer.toString(index))
                    .setTimeToLive(Duration.ofMinutes(2));
            }).collect(Collectors.toList());

        sender.sendMessages(serviceBusMessages);
    }

    /**
     * <strong>Scenario 1: Part 1</strong>
     *
     * <p>
     * Receive {@link ServiceBusMessage messages} and return the {@link ServiceBusMessage messages} back to the queue.
     * When the max number of deliveries for each {@link ServiceBusMessage message} expires, then it is moved into the
     * dead letter queue.
     * </p>
     *
     * @param builder Service Bus client builder.
     * @param queueName Name of the queue to receive from.
     *
     * @return A Mono that completes when all messages in queue have been processed.(because a message has not been
     *     received in the last 30 seconds).
     */
    private Mono<Void> deadLetterByExceedingMaxDelivery(ServiceBusClientBuilder builder, String queueName) {
        // This Mono creates an async receiver, and continues receiving from that queue until it has not seen a message
        // for 30 seconds.
        //
        // When it receives any messages, it abandons them so they are returned to the queue to be re-received again.
        // When a message is abandoned, its delivery count is incremented. If the delivery count exceeds the
        // MaxDeliveryCount for a queue, the message is placed in the deadletter queue for that particular queue.
        return Mono.using(() -> {
            return builder.receiver()
                .queueName(queueName)
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .buildAsyncClient();
        }, receiver -> {
            // Continue to receive messages until no message has been seen for 30 seconds.
            return receiver.receiveMessages()
                .timeout(Duration.ofSeconds(30))
                .flatMap(message -> {
                    System.out.printf("Received message. Sequence # %s; DeliveryCount %d%n", message.getSequenceNumber(),
                        message.getDeliveryCount());

                    return receiver.abandon(message);
                })
                .onErrorResume(TimeoutException.class, exception -> {
                    System.out.println("No messages received after 30 seconds. Queue is empty.");
                    return Mono.empty();
                })
                .then();
        }, receiver -> {
            // When the receiving operation is completed, close the client.
            receiver.close();
        });
    }

    /**
     * <strong>Scenario 1: Part 2</strong>
     *
     * <p>
     * This method continues to receive messages from the dead letter queue, then completes them.
     * </p>
     *
     * @param builder Service Bus client builder.
     * @param queueName Name of the queue to receive from.
     *
     * @return A Mono that completes when all messages in queue have been processed (because a message has not been
     *     received in the last 30 seconds).
     */
    private Mono<Void> receiveAndCompleteDeadLetterQueueMessages(ServiceBusClientBuilder builder, String queueName) {
        return Mono.using(() -> {
            return builder.receiver()
                .queueName(queueName)
                .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                .buildAsyncClient();
        }, deadLetterQueueReceiver -> {
            // Continue to receive messages until no message has been seen for 30 seconds.
            return deadLetterQueueReceiver.receiveMessages()
                .timeout(Duration.ofSeconds(30))
                .flatMap(message -> {
                    System.out.printf("Received message from dead-letter queue. Sequence #: %s. DeliveryCount %d%n",
                        message.getSequenceNumber(), message.getDeliveryCount());

                    // When messages are dead lettered, there are system properties that can be set to denote the
                    // reason why via fields such as dead-letter reason and dead-letter description.
                    System.out.printf("Dead-Letter Reason: %s. Description: %s. Source: %s%n",
                        message.getDeadLetterReason(), message.getDeadLetterErrorDescription(),
                        message.getDeadLetterSource());

                    System.out.println("Application properties:");
                    message.getApplicationProperties().forEach((key, value) ->
                        System.out.printf("\t%s=%s%n", key, value));

                    // We complete the messages we received from the queue.
                    // In a real example, you may do some sort of computation to understand why previous attempts to
                    // process the message failed. This is shown in Scenario 2.
                    return deadLetterQueueReceiver.complete(message);
                })
                .onErrorResume(TimeoutException.class, exception -> {
                    System.out.println("No messages received after 30 seconds. Dead-letter queue is empty.");
                    return Mono.empty();
                })
                .then();
        }, deadLetterQueueReceiver -> {
            // When the receiving operation is completed, close the client.
            deadLetterQueueReceiver.close();
        });
    }

    /**
     * <strong>Scenario 2: Part 1</strong>
     *
     * <p>
     * Receives {@link ServiceBusMessage messages} and dead letters them if it has a subject of "Scientist" and content
     * type of "application/json". This is to simulate that the message may be malformed or didn't contain the right
     * content, so we dead letter it so other receivers don't process this message.
     * </p>
     *
     * @param builder Service Bus client builder.
     * @param queueName Name of queue to receive messages from.
     *
     * @return A Mono that completes when all the messages in the queue have been processed (because a message has not
     *     been received in the last 30 seconds).
     */
    private Mono<Void> receiveAndDeadletterMessages(ServiceBusClientBuilder builder, String queueName) {
        return Mono.using(
            () -> {
                // Creates the async receiver.
                return builder.receiver()
                    .queueName(queueName)
                    .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                    .buildAsyncClient();
            },
            receiver -> {
                // The scope of the receiver resource is to receive messages for a duration of `receiveDuration` then
                // complete or dead-letter the message based on its content-type and subject.
                return receiver.receiveMessages()
                    .timeout(Duration.ofSeconds(30))
                    .flatMap(message -> {
                        final String subject = message.getSubject();
                        final String contentType = message.getContentType();
                        final Person person;
                        try {
                            person = Person.fromJson(message.getBody().toString());
                        } catch (RuntimeException e) {
                            return Mono.error(new RuntimeException("Could not deserialize message: "
                                + message.getSequenceNumber(), e));
                        }

                        System.out.printf("Received message. SequenceNumber = %s. EnqueuedTimeUtc = %s. "
                                + "ExpiresAtUtc = %s. ContentType = %s. Content: [ %s ]%n",
                            message.getSequenceNumber(), message.getEnqueuedTime(), message.getExpiresAt(),
                            message.getContentType(), person);

                        // Simulates that something could not be processed in the message, so we dead letter it.
                        if ("Scientist".equals(subject) && "application/json".equals(contentType)) {
                            return receiver.complete(message);
                        } else {
                            return receiver.deadLetter(message);
                        }
                    })
                    .onErrorResume(TimeoutException.class, exception -> {
                        System.out.println("No messages received after 30 seconds. Queue is empty.");
                        return Mono.empty();
                    })
                    .then();
            },
            receiver -> {
                // When the receiving operation is completed, close the client.
                receiver.close();
            });
    }

    /**
     * <strong>Scenario 2: Part 2</strong>
     *
     * <p>
     * Receives {@link ServiceBusMessage messages} from the dead letter queue, it fixes up the message then resends the
     * fixed message to the queue again. This simulates messages that may have errors in them, were dead-lettered,
     * and reprocessed in the dead-letter queue so the data is correct again.
     * </p>
     *
     * @param builder Service Bus client builder.
     * @param queueName Name of queue to receive messages from.
     * @param resubmitSender Service Bus sender client. When messages are fixed, they are published via this sender.
     *
     * @return A Mono that completes when all the messages in the queue have been processed (because a message has not
     *     been received in the last 30 seconds).
     */
    private Mono<Void> receiveAndFixDeadLetterQueueMessages(ServiceBusClientBuilder builder, String queueName,
        ServiceBusSenderClient resubmitSender) {

        return Mono.using(() -> {
            return builder.receiver()
                .queueName(queueName)
                .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                .buildAsyncClient();
        }, deadLetterQueueReceiver -> {
            // Continue to receive messages until no message has been seen for 30 seconds.
            return deadLetterQueueReceiver.receiveMessages()
                .timeout(Duration.ofSeconds(30))
                .flatMap(message -> {
                    if ("Physicist".equals(message.getSubject())) {
                        System.out.printf("Fixing DLQ message. MessageId = %s. SequenceNumber = %s. Subject = %s%n",
                            message.getMessageId(), message.getSequenceNumber(), message.getSubject());

                        // Create a copy of this message and then set the subject to the correct one.
                        ServiceBusMessage resubmitMessage = new ServiceBusMessage(message)
                            .setSubject("Scientist");

                        // Sending that corrected message.
                        resubmitSender.sendMessage(resubmitMessage);
                    } else {
                        System.out.printf("Message resubmission is not required. MessageId = %s. SequenceNumber = %s. "
                                + "Subject = %s%n",
                            message.getMessageId(), message.getSequenceNumber(), message.getSubject());
                    }

                    return deadLetterQueueReceiver.complete(message);
                })
                .onErrorResume(TimeoutException.class, exception -> {
                    System.out.println("No messages received after 30 seconds. Dead-letter queue is empty.");
                    return Mono.empty();
                })
                .then();
        }, deadLetterQueueReceiver -> {
            // When the receiving operation is completed, close the client.
            deadLetterQueueReceiver.close();
        });
    }

    private static final class Person {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private final String lastName;
        private final String firstName;

        Person(@JsonProperty String lastName, @JsonProperty String firstName) {
            this.lastName = lastName;
            this.firstName = firstName;
        }

        String getLastName() {
            return lastName;
        }

        String getFirstName() {
            return firstName;
        }

        /**
         * Serializes an item into its JSON string equivalent.
         *
         * @return The JSON representation.
         *
         * @throws RuntimeException if the person could not be serialized.
         */
        String toJson() {
            try {
                return OBJECT_MAPPER.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not serialize object.", e);
            }
        }

        /**
         * Deserializes a JSON string into a Person.
         *
         * @return The corresponding person.
         *
         * @throws RuntimeException if the JSON string could not be deserialized.
         */
        private static Person fromJson(String json) {
            try {
                return OBJECT_MAPPER.readValue(json, Person.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not deserialize object.", e);
            }
        }
    }
}
