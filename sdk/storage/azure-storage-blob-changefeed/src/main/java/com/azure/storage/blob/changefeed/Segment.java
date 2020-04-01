package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

class Segment {
    private static ClientLogger logger = new ClientLogger(Segment.class);

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Segment manifest location. */
    private final String path;

    private final BlobChangefeedCursor cfCursor;

    private final BlobChangefeedCursor userCursor;

    Segment(BlobContainerAsyncClient client, String path, BlobChangefeedCursor cfCursor,
        BlobChangefeedCursor userCursor) {
        this.client = client;
        this.path = path;
        this.cfCursor = cfCursor;
        this.userCursor = userCursor;
    }

    Flux<BlobChangefeedEventWrapper> getEvents() {
        /* Download JSON manifest file. */
        return client.getBlobAsyncClient(path)
            .download().reduce(new ByteArrayOutputStream(), (os, buffer) -> {
                try {
                    os.write(FluxUtil.byteBufferToArray(buffer));
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new UncheckedIOException(e));
                }
                return os;
            })
            .map(ByteArrayOutputStream::toString)
            /* Parse the JSON for shards. */
            .map(json -> {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = null;
                try {
                    jsonNode = objectMapper.readTree(json);
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new UncheckedIOException(e));
                }
                List<Flux<BlobChangefeedEventWrapper>> shards = new ArrayList<>();
                for (JsonNode shard : jsonNode.withArray("chunkFilePaths")) {
                    String shardPath =
                        shard.asText().substring(BlobChangefeedAsyncClient.CHANGEFEED_CONTAINER_NAME.length() + 1);
                    shards.add(new Shard(client, shardPath, cfCursor.toShardCursor(shardPath), userCursor).getEvents());
                }
                return shards;
            })
            /* Round robin among shards in this segment. */
            .flatMapMany(this::roundRobinAmongShards);
    }

    private Flux<BlobChangefeedEventWrapper> roundRobinAmongShards(List<Flux<BlobChangefeedEventWrapper>> shards) {
        return Flux.merge(shards);
    }
}
