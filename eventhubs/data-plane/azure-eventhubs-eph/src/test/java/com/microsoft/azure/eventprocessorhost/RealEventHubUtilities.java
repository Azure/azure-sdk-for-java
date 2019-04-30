// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.PartitionSender;
import org.junit.Assume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

final class RealEventHubUtilities {
    static final int QUERY_ENTITY_FOR_PARTITIONS = -1;
    static final String SYNTACTICALLY_CORRECT_DUMMY_EVENT_HUB_PATH = "doesnotexist";
    static final String SYNTACTICALLY_CORRECT_DUMMY_CONNECTION_STRING =
            "Endpoint=sb://doesnotexist.servicebus.windows.net/;SharedAccessKeyName=doesnotexist;SharedAccessKey=dGhpcyBpcyBub3QgYSB2YWxpZCBrZXkgLi4uLi4uLi4=;EntityPath="
                + RealEventHubUtilities.SYNTACTICALLY_CORRECT_DUMMY_EVENT_HUB_PATH;

    private ConnectionStringBuilder hubConnectionString = null;
    private String hubName = null;
    private String consumerGroup = EventHubClient.DEFAULT_CONSUMER_GROUP_NAME;
    private EventHubClient client = null;
    private ArrayList<String> cachedPartitionIds = null;
    private HashMap<String, PartitionSender> partitionSenders = new HashMap<String, PartitionSender>();

    RealEventHubUtilities() {
    }

    ArrayList<String> setup(boolean skipIfFakeEH, int fakePartitions) throws EventHubException, IOException {
        ArrayList<String> partitionIds = setupWithoutSenders(skipIfFakeEH, fakePartitions);

        // EventHubClient is source of all senders
        this.client = EventHubClient.createSync(this.hubConnectionString.toString(), TestUtilities.EXECUTOR_SERVICE);

        return partitionIds;
    }

    ArrayList<String> setupWithoutSenders(boolean skipIfFakeEH, int fakePartitions) throws EventHubException, IOException {
        // Get the connection string from the environment
        ehCacheCheck(skipIfFakeEH);

        // Get the consumer group from the environment, if present.
        String tempConsumerGroup = System.getenv("EVENT_HUB_CONSUMER_GROUP");
        if (tempConsumerGroup != null) {
            this.consumerGroup = tempConsumerGroup;
        }

        ArrayList<String> partitionIds = null;

        if (fakePartitions == RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS) {
            partitionIds = getPartitionIdsForTest();
        } else {
            partitionIds = new ArrayList<String>();
            for (int i = 0; i < fakePartitions; i++) {
                partitionIds.add(Integer.toString(i));
            }
        }

        return partitionIds;
    }

    void shutdown() throws EventHubException {
        for (PartitionSender sender : this.partitionSenders.values()) {
            sender.closeSync();
        }
        if (this.client != null) {
            this.client.closeSync();
        }
    }

    ConnectionStringBuilder getConnectionString(boolean skipIfFakeEH) {
        ehCacheCheck(skipIfFakeEH);
        return this.hubConnectionString;
    }

    private void ehCacheCheck(boolean skipIfFakeEH) {
        if (this.hubName == null) {
            if (skipIfFakeEH) {
                TestUtilities.skipIfAppveyor();
            }
            String rawConnectionString = System.getenv("EVENT_HUB_CONNECTION_STRING");
            if (rawConnectionString == null) {
                if (skipIfFakeEH) {
                    TestBase.logInfo("SKIPPING - REQUIRES REAL EVENT HUB");
                    Assume.assumeTrue(rawConnectionString != null);
                }
                TestBase.logInfo("Using dummy event hub connection string");
                rawConnectionString = RealEventHubUtilities.SYNTACTICALLY_CORRECT_DUMMY_CONNECTION_STRING;
            }

            this.hubConnectionString = new ConnectionStringBuilder(rawConnectionString);
            this.hubName = this.hubConnectionString.getEventHubName();
        }
    }

    String getConsumerGroup() {
        return this.consumerGroup;
    }

    void sendToAny(String body, int count) throws EventHubException {
        for (int i = 0; i < count; i++) {
            sendToAny(body);
        }
    }

    void sendToAny(String body) throws EventHubException {
        EventData event = EventData.create(body.getBytes());
        this.client.sendSync(event);
    }

    void sendToPartition(String partitionId, String body) throws IllegalArgumentException, EventHubException {
        EventData event = EventData.create(body.getBytes());
        PartitionSender sender = null;
        if (this.partitionSenders.containsKey(partitionId)) {
            sender = this.partitionSenders.get(partitionId);
        } else {
            sender = this.client.createPartitionSenderSync(partitionId);
            this.partitionSenders.put(partitionId, sender);
        }
        sender.sendSync(event);
    }

    ArrayList<String> getPartitionIdsForTest() throws EventHubException, IOException {
        if (this.cachedPartitionIds == null) {
            this.cachedPartitionIds = new ArrayList<String>();
            ehCacheCheck(true);

            EventHubClient idClient = EventHubClient.createSync(this.hubConnectionString.toString(), TestUtilities.EXECUTOR_SERVICE);
            try {
                EventHubRuntimeInformation info = idClient.getRuntimeInformation().get();
                String[] ids = info.getPartitionIds();
                for (String id : ids) {
                    this.cachedPartitionIds.add(id);
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new IllegalArgumentException("Error getting partition ids in test framework", e.getCause());
            }
        }

        return this.cachedPartitionIds;
    }

}
