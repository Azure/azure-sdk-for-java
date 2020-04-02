package com.azure.storage.blob.changefeed;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.util.DownloadUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gets events for a segment (represents approximately an hour of the changefeed).
 */
class Segment {
    private static ClientLogger logger = new ClientLogger(Segment.class);

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Segment manifest location. */
    private final String path;

    /* Cursor associated with parent changefeed. */
    private final BlobChangefeedCursor cfCursor;

    /* User provided cursor. */
    private final BlobChangefeedCursor userCursor;

    /**
     * Creates a segment with the associated path and cursors.
     */
    Segment(BlobContainerAsyncClient client, String segmentPath, BlobChangefeedCursor cfCursor,
        BlobChangefeedCursor userCursor) {
        this.client = client;
        this.path = segmentPath;
        this.cfCursor = cfCursor;
        this.userCursor = userCursor;
    }

    /**
     * Get all the events for the Segment.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        /* Download JSON manifest file. */
        /* We can keep the entire metadata file in memory since it is expected to only be a few hundred bytes. */
        return DownloadUtils.downloadToString(client, path)
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

    /**
     * Round robins among shard events.
     */
    private Flux<BlobChangefeedEventWrapper> roundRobinAmongShards(List<Flux<BlobChangefeedEventWrapper>> shards) {
        return Flux.merge(shards);
    }
}
