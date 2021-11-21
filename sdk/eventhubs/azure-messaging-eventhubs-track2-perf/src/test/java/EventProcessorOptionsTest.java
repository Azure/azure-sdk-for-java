// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.perf.EventProcessorOptions;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventProcessorOptionsTest {
    /**
     * Tests that the defaults are set correctly.
     */
    @Test
    public void usesDefaults() {
        // Arrange
        final int numberOfEvents = 10;
        final String name = "event-hub-name";
        final String connectionString = "eh connection string";
        final String storageConnectionString = "foo-bar-storage";
        final String storageEndpoint = "foo-bar-storage-endpoint";
        final List<String> arguments = Arrays.asList(
            "--storageConnectionString", storageConnectionString,
            "--storageEndpoint", storageEndpoint,
            "--eventsToSend", String.valueOf(numberOfEvents),
            "--connectionString", connectionString,
            "--name", name);
        final EventProcessorOptions options = new EventProcessorOptions();
        final JCommander commander = new JCommander(options);

        // Act
        commander.parse(arguments.toArray(new String[0]));

        // Assert
        assertFalse(options.isBatched());
        assertEquals(100, options.getBatchSize());
        assertEquals(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME, options.getConsumerGroup());
        assertEquals(AmqpTransportType.AMQP, options.getTransportType());

        assertEquals(name, options.getEventHubName());
        assertEquals(connectionString, options.getConnectionString());
        assertEquals(storageConnectionString, options.getStorageConnectionString());
        assertEquals(numberOfEvents, options.getEventsToSend());
        assertEquals(storageEndpoint, options.getStorageEndpoint());
    }

    public static Stream<String> missingRequiredField() {
        return Stream.of("--connectionString", "--name", "--storageConnectionString", "--storageEndpoint");
    }

    /**
     * Tests that we require fields.
     */
    @ParameterizedTest
    @MethodSource
    public void missingRequiredField(String parameterToRemove) {
        // Arrange
        final int numberOfEvents = 10;
        final String name = "event-hub-name";
        final String connectionString = "eh connection string";
        final String storageConnectionString = "foo-bar-storage";
        final String storageEndpoint = "foo-bar-storage-endpoint";

        final Map<String, String> arguments = new HashMap<>();
        arguments.put("--connectionString", connectionString);
        arguments.put("--name", name);
        arguments.put("--storageConnectionString", storageConnectionString);
        arguments.put("--storageEndpoint", storageEndpoint);
        arguments.put("--eventsToSend", String.valueOf(numberOfEvents));

        final String removed = arguments.remove(parameterToRemove);
        assertNotNull(removed, parameterToRemove + " should have been removed.");

        final List<String> argumentList = new ArrayList<>();
        arguments.forEach((key, value) -> {
            argumentList.add(key);
            argumentList.add(value);
        });

        final EventProcessorOptions options = new EventProcessorOptions();
        final JCommander commander = new JCommander(options);

        // Act
        final ParameterException parameterException = assertThrows(ParameterException.class,
            () -> commander.parse(argumentList.toArray(new String[0])));

        // Assert
        assertTrue(parameterException.getMessage().contains(parameterToRemove));
    }

    /**
     * Tests that the transport type throws when not parsed incorrectly works.
     */
    @Test
    public void invalidTransportType() {
        final String name = "event-hub-name";
        final String connectionString = "eh connection string";
        final String storageConnectionString = "foo-bar-storage";
        final String storageEndpoint = "foo-bar-storage-endpoint";
        final String transportType = "invalidType";
        final String transportTypeKey = "--transportType";

        final List<String> arguments = Arrays.asList(
            transportTypeKey, transportType,
            "--storageConnectionString", storageConnectionString,
            "--storageEndpoint", storageEndpoint,
            "--connectionString", connectionString,
            "--name", name);

        final EventProcessorOptions options = new EventProcessorOptions();
        final JCommander commander = new JCommander(options);

        // Act
        final ParameterException exception = assertThrows(ParameterException.class,
            () -> commander.parse(arguments.toArray(new String[0])));

        // Assert
        assertTrue(exception.getMessage().contains(transportTypeKey));
    }

    /**
     * Tests that the transport type works.
     */
    @Test
    public void transportType() {
        final String name = "event-hub-name";
        final String connectionString = "eh connection string";
        final String storageConnectionString = "foo-bar-storage";
        final String storageEndpoint = "foo-bar-storage-endpoint";
        final AmqpTransportType transportType = AmqpTransportType.AMQP_WEB_SOCKETS;

        final List<String> arguments = Arrays.asList(
            "--transportType", transportType.toString(),
            "--storageConnectionString", storageConnectionString,
            "--storageEndpoint", storageEndpoint,
            "--connectionString", connectionString,
            "--name", name);


        final EventProcessorOptions options = new EventProcessorOptions();
        final JCommander commander = new JCommander(options);

        // Act
        commander.parse(arguments.toArray(new String[0]));

        // Assert
        assertEquals(transportType, options.getTransportType());
    }
}
