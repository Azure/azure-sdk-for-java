// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.eventhubs.checkpointstore.jedis {
    requires transitive com.azure.core;
    requires redis.clients.jedis;
    requires com.azure.messaging.eventhubs;

    exports com.azure.messaging.eventhubs.checkpointstore.jedis;
}
