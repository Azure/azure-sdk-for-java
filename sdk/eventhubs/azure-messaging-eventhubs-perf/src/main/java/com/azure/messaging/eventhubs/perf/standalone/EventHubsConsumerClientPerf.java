// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.standalone;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.qpid.proton.InterruptException;

import java.util.concurrent.CountDownLatch;

/**
 * Standalone app to run performance test for consumer client.
 * TODO: Port to framework test once it supports event based perf testing.
 */
public class EventHubsConsumerClientPerf {
    private static final String EVENTHUB_NAME = System.getenv("EVENTHUB_NAME");;
// Settings copied from
// https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-faq#how-much-does-a-single-capacity-unit-let-me-achieve
    private static final int BYTES_PER_MESSAGE = 1024;

    /**
     * Runs the EventHub consumer client performance test
     *
     * @param args The arguments used to run the performance test.
     * @throws InterruptedException when thread is interrupted during execution.
     */
    public static void main(String[] args) throws InterruptedException {
        Options options = new Options();
        Option clientsOption = new Option("c", "clients", true, "Number of client instances");
        options.addOption(clientsOption);
        Option partitionsOption = new Option("p", "partitions", true, "Number of partitions");
        options.addOption(partitionsOption);
        Option verboseOption = new Option("v", "verbose", false, "Enables verbose output");
        options.addOption(verboseOption);
        Option debugOption = new Option("d", "debug", false, "Enables debug output");
        options.addOption(debugOption);
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
        boolean debug = cmd.hasOption("debug");

        String connectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");

        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalStateException("Environment variable EVENT_HUBS_CONNECTION_STRING must be set");
        }
        receiveMessages(connectionString, partitions, clients, verbose, debug);
    }
    static void receiveMessages(String connectionString, int numPartitions, int numClients, boolean verbose,
                                boolean debug) throws InterruptedException, InterruptException {
        System.out.println(String.format("Receiving messages from %d partitions using %d client instances",
            numPartitions, numClients));
        EventHubConsumerAsyncClient[] clients = new EventHubConsumerAsyncClient[numClients];
        for (int i = 0; i < numClients; i++) {
            clients[i] = new EventHubClientBuilder().connectionString(connectionString, EVENTHUB_NAME)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();
        }
        try {
            EventHubConsumerAsyncClient client = clients[0];
            long[] totalCount = new long[1];
            client.getPartitionIds()
                .concatMap(client::getPartitionProperties)
                .map(partitionProperties -> {
                        long begin = partitionProperties.getBeginningSequenceNumber();
                        long end = partitionProperties.getLastEnqueuedSequenceNumber();
                        long count = end - begin + 1;
                        if (verbose) {
                            System.out.println(String.format("Partition: %s, Begin: %d, End: %d, Count: %d",
                                partitionProperties.getId(), begin, end, count));
                        }
                        totalCount[0] += count;
                        return partitionProperties;
                    }).collectList().block();

            if (verbose) {
                System.out.println(String.format("Total Count: %d", totalCount[0]));
            }
            try {
                CountDownLatch countDownLatch = new CountDownLatch((int) totalCount[0]);
                long start = System.nanoTime();
                for (int i = 0; i < numPartitions; i++) {
                    clients[i % numClients].receiveFromPartition(String.valueOf(i),
                        EventPosition.earliest()).subscribe(event -> {
                        countDownLatch.countDown();
                        if (debug) {
                            long count = countDownLatch.getCount();
                            if (count % 1000 == 0) {
                                System.out.println(count);
                            }
                        }
                    });
                }
                countDownLatch.await();
                long end = System.nanoTime();
                double elapsed = 1.0 * (end - start) / 1000000000;
                long messagesReceived = totalCount[0];
                double messagesPerSecond = messagesReceived / elapsed;
                double megabytesPerSecond = (messagesPerSecond * BYTES_PER_MESSAGE) / (1024 * 1024);
                System.out.println(String.format("Received %d messages of size %d in %.2fs (%.2f msg/s, %.2f MB/s))",
                    messagesReceived, BYTES_PER_MESSAGE, elapsed, messagesPerSecond, megabytesPerSecond));
            } finally {
                for (EventHubConsumerAsyncClient consumerClient : clients) {
                    consumerClient.close();
                }
            }
        } finally {
            for (EventHubConsumerAsyncClient consumerClient : clients) {
                consumerClient.close();
            }
        }
        // Workaround for "IllegalThreadStateException on shutdown from maven exec
        // plugin"
        // https://github.com/ReactiveX/RxJava/issues/2833
        System.exit(0);
    }
}
