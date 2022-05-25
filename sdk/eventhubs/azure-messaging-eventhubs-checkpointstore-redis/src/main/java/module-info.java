module com.azure.messaging.eventhubs.checkpointstore.redis {
    requires transitive com.azure.core;
    requires redis.clients.jedis;
    requires com.azure.messaging.eventhubs;
}
