// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import com.azure.storage.blob.changefeed.implementation.util.DownloadUtils;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;

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

    private static final String CHUNK_FILE_PATHS = "chunkFilePaths";

    private final BlobContainerAsyncClient client; /* Changefeed container */
    private final String segmentPath; /* Segment manifest location. */
    private final ChangefeedCursor changefeedCursor; /* Cursor associated with parent changefeed. */
    private final SegmentCursor userCursor; /* User provided cursor. */
    private final ShardFactory shardFactory;

    /**
     * Creates a new Segment.
     */
    Segment(BlobContainerAsyncClient client, String segmentPath, ChangefeedCursor changefeedCursor,
        SegmentCursor userCursor, ShardFactory shardFactory) {
        this.client = client;
        this.segmentPath = segmentPath;
        this.changefeedCursor = changefeedCursor;
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
        return DownloadUtils.downloadToByteArray(client, segmentPath)
            .flatMap(DownloadUtils::parseJson)
            /* Parse the shards from the manifest. */
            .flatMapIterable(this::getShards)
            /* Get all events for each shard. */
            .concatMap(Shard::getEvents);
    }

    private List<Shard> getShards(JsonNode node) {
        List<Shard> shards = new ArrayList<>();

        /* Iterate over each shard element. */
        for (JsonNode shard : node.withArray(CHUNK_FILE_PATHS)) {
            /* Strip out the changefeed container name and the subsequent / */
            String shardPath =
                shard.asText().substring(BlobChangefeedClientBuilder.CHANGEFEED_CONTAINER_NAME.length() + 1);

            ShardCursor shardCursor = null; /* By default, read shard from the beginning. */
            if (userCursor != null) {
                shardCursor = userCursor.getShardCursors().stream()
                    .filter(sc -> sc.getCurrentChunkPath().contains(shardPath))
                    .findFirst()
                    .orElse(null); /* If this shard does not exist in the list of shard cursors,
                    read shard from the beginning. */
            }
            shards.add(shardFactory.getShard(shardPath, changefeedCursor.toShardCursor(shardPath), shardCursor));
        }
        return shards;
    }
}
