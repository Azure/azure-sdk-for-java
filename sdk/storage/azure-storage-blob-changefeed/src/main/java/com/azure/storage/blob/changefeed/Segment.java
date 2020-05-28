// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.DownloadUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Segment in Changefeed.
 *
 * A segment is a blob that points to a manifest file.
 * The segment manifest file (meta.json) shows the path of the change feed files for that segment in the
 * chunkFilePaths property. (Note: these chunkFilePaths are really shardPaths in this implementation.)
 * The chunkFilePaths property looks something like this.
 * { ...
 * "chunkFilePaths": [
 *         "$blobchangefeed/log/00/2019/02/22/1810/",
 *         "$blobchangefeed/log/01/2019/02/22/1810/"
 *     ],
 * ...}
 */
class Segment {

    private final ClientLogger logger = new ClientLogger(Segment.class);

    private static final String CHUNK_FILE_PATHS = "chunkFilePaths";

    private final BlobContainerAsyncClient client; /* Changefeed container */
    private final String segmentPath; /* Segment manifest location. */
    private final ChangefeedCursor cfCursor; /* Cursor associated with parent changefeed. */
    private final ChangefeedCursor userCursor; /* User provided cursor. */
    private final ShardFactory shardFactory;

    /**
     * Creates a new Segment.
     */
    Segment(BlobContainerAsyncClient client, String segmentPath, ChangefeedCursor cfCursor,
        ChangefeedCursor userCursor, ShardFactory shardFactory) {
        this.client = client;
        this.segmentPath = segmentPath;
        this.cfCursor = cfCursor;
        this.userCursor = userCursor;
        this.shardFactory = shardFactory;
    }

    /**
     * Get all the events for the Segment.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        /* Download JSON manifest file. */
        /* We can keep the entire metadata file in memory since it is expected to only be a few hundred bytes. */
        return DownloadUtils.downloadToString(client, segmentPath)
            .flatMap(this::parseJson)
            /* Parse the JSON for shards. */
            .flatMapMany(this::getShards)
            /* Get all events for each shard. */
            .concatMap(Shard::getEvents);
    }

    private Mono<JsonNode> parseJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return Mono.just(jsonNode);
        } catch (IOException e) {
            return FluxUtil.monoError(logger, new UncheckedIOException(e));
        }
    }

    private Flux<Shard> getShards(JsonNode node) {
        List<Shard> shards = new ArrayList<>();
        boolean validShard = false;

        /* Iterate over each shard element. */
        for (JsonNode shard : node.withArray(CHUNK_FILE_PATHS)) {
            /* Strip out the changefeed container name and the subsequent / */
            String shardPath =
                shard.asText().substring(BlobChangefeedAsyncClient.CHANGEFEED_CONTAINER_NAME.length() + 1);

            if (userCursor == null) {
                validShard = true;
            } else {
                /* If a user cursor was provided, figure out the associated shard to start at. */
                if (shardPath.equals(userCursor.getShardPath())) {
                    validShard = true;
                }
            }

            if (validShard) {
                shards.add(shardFactory.getShard(shardPath, cfCursor.toShardCursor(shardPath), userCursor));
            }
        }
        return Flux.fromIterable(shards);
    }
}
