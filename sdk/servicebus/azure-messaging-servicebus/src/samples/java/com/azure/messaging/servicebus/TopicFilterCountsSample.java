// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeProperties;

import java.io.PrintStream;

/**
 * Reads the SQL and correlation filter counts across all subscriptions of an existing topic.
 *
 * <p>Requires a namespace connection string with Manage permission and the name of an existing topic. Uses
 * service API version {@code 2024-05} to read both filter counts.</p>
 */
public class TopicFilterCountsSample {
    /**
     * Prints the topic's filter counts without modifying the topic or its subscriptions.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        String connectionString = getRequiredEnvironmentVariable("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        String topicName = getRequiredEnvironmentVariable("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");

        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .serviceVersion(ServiceBusServiceVersion.V2024_05)
            .buildClient();

        printFilterCounts(client, topicName, System.out);
    }

    static void printFilterCounts(ServiceBusAdministrationClient client, String topicName, PrintStream output) {
        TopicRuntimeProperties properties = client.getTopicRuntimeProperties(topicName);
        output.printf("Topic: %s, SQL filters: %d, correlation filters: %d%n",
            properties.getName(), properties.getSqlFilterCount(), properties.getCorrelationFilterCount());
    }

    private static String getRequiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Set the " + name + " environment variable before running this sample.");
        }
        return value;
    }
}
