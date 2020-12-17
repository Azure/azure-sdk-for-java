// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.perf.standalone;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.PartitionRuntimeInformation;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventPosition;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.CountDownLatch;

import java.util.stream.Collectors;

public class EventhubsReceiverPerf {
    private static final String EVENTHUB_NAME = System.getenv("EVENTHUB_NAME");;

    // Settings copied from
    // https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-faq#how-much-does-a-single-capacity-unit-let-me-achieve
    private static final int MESSAGES_PER_BATCH = 100;
    private static final int BYTES_PER_MESSAGE = 1024;

    public static void main(String[] args)
        throws InterruptedException, IOException, EventHubException, ExecutionException {
        Options options = new Options();

        Option clientsOption = new Option("c", "clients", true, "Number of client instances");
        options.addOption(clientsOption);

        Option partitionsOption = new Option("p", "partitions", true, "Number of partitions");
        options.addOption(partitionsOption);

        Option verboseOption = new Option("v", "verbose", false, "Enables verbose output");
        options.addOption(verboseOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("eventhubsconsumeperf", options);
            System.exit(1);
        }

        int clients = Integer.parseInt(cmd.getOptionValue("clients", "1"));
        int partitions = Integer.parseInt(cmd.getOptionValue("partitions", "5"));
        boolean verbose = cmd.hasOption("verbose");

        String connectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");
        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable EVENT_HUBS_CONNECTION_STRING must be set");
            System.exit(1);
        }

        receiveMessages(connectionString, partitions, clients, verbose);
    }

    static void receiveMessages(String connectionString, int numPartitions, int numClients, boolean verbose)
        throws InterruptedException, EventHubException, IOException, ExecutionException {
        System.out.println(String.format("Receiving messages from %d partitions using %d client instances",
            numPartitions, numClients));

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

        EventHubClient[] clients = new EventHubClient[numClients];
        for (int i = 0; i < numClients; i++) {
            clients[i] = EventHubClient.createSync(
                new ConnectionStringBuilder(connectionString).setEventHubName(EVENTHUB_NAME).toString(), executor);
        }

        try {
            EventHubClient client = clients[0];
            EventHubRuntimeInformation eventHubInfo = client.getRuntimeInformation().get();
            String[] partitionIds = Arrays.copyOfRange(eventHubInfo.getPartitionIds(), 0, numPartitions);

            List<CompletableFuture<PartitionRuntimeInformation>> partitionsFutures = new ArrayList<CompletableFuture<PartitionRuntimeInformation>>(
                numPartitions);
            for (String partitionId : partitionIds) {
                partitionsFutures.add(client.getPartitionRuntimeInformation(partitionId));
            }

            List<PartitionRuntimeInformation> partitions = partitionsFutures.stream().map(CompletableFuture::join)
                .collect(Collectors.toList());

            long totalCount = 0;
            for (PartitionRuntimeInformation partition : partitions) {
                long begin = partition.getBeginSequenceNumber();
                long end = partition.getLastEnqueuedSequenceNumber();
                long count = end - begin + 1;
                totalCount += count;

                if (verbose) {
                    System.out.println(String.format("Partition: %s, Begin: %d, End: %d, Count: %d",
                        partition.getPartitionId(), begin, end, count));
                }
            }
            if (verbose) {
                System.out.println(String.format("Total Count: %d", totalCount));
            }

            List<CompletableFuture<PartitionReceiver>> receiverFutures = new ArrayList<CompletableFuture<PartitionReceiver>>(
                numPartitions);
            for (int i = 0; i < numPartitions; i++) {
                receiverFutures.add(clients[i % numClients].createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                    partitions.get(i).getPartitionId(), EventPosition.fromStartOfStream()));
            }
            List<PartitionReceiver> receivers = receiverFutures.stream().map(CompletableFuture::join)
                .collect(Collectors.toList());

            try {
                CountDownLatch countDownLatch = new CountDownLatch((int) totalCount);

                long start = System.nanoTime();
                for (int i = 0; i < numPartitions; i++) {
                    receiveAllMessages(receivers.get(i), partitions.get(i), executor, countDownLatch);
                }
                countDownLatch.await();
                long end = System.nanoTime();

                double elapsed = 1.0 * (end - start) / 1000000000;
                long messagesReceived = totalCount;
                double messagesPerSecond = messagesReceived / elapsed;
                double megabytesPerSecond = (messagesPerSecond * BYTES_PER_MESSAGE) / (1024 * 1024);

                System.out.println(String.format("Received %d messages of size %d in %.2fs (%.2f msg/s, %.2f MB/s))",
                    messagesReceived, BYTES_PER_MESSAGE, elapsed, messagesPerSecond, megabytesPerSecond));
            } finally {
                receivers.stream().map(PartitionReceiver::close).map(CompletableFuture::join);
            }
        } finally {
            Arrays.stream(clients).map(EventHubClient::close).map(CompletableFuture::join);
            executor.shutdown();
        }
    }

    static void receiveAllMessages(PartitionReceiver receiver, PartitionRuntimeInformation partition, Executor executor,
                                   CountDownLatch countDownLatch) {
        receiver.receive(MESSAGES_PER_BATCH).thenAcceptAsync(receivedEvents -> {
            long lastSequenceNumber = -1;
            if (receivedEvents != null) {
                for (EventData receivedEvent : receivedEvents) {
                    countDownLatch.countDown();
                    if (receivedEvent.getSystemProperties().getSequenceNumber() > lastSequenceNumber) {
                        lastSequenceNumber = receivedEvent.getSystemProperties().getSequenceNumber();
                    }
                }
            }
            if (lastSequenceNumber < partition.getLastEnqueuedSequenceNumber()) {
                receiveAllMessages(receiver, partition, executor, countDownLatch);
            }
        }, executor);
    }
}
