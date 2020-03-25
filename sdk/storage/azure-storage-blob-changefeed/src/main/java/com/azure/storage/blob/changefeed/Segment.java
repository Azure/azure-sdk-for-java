package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
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

    /* List of shards associated with this segment. */
    private List<Flux<BlobChangefeedEvent>> shards;

    Segment(BlobContainerAsyncClient client, String path) {
        this.client = client;
        this.path = path;
        shards = new ArrayList<>();
    }

    Flux<BlobChangefeedEvent> getEvents() {
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
                for (JsonNode shard : jsonNode.withArray("chunkFilePaths")) {
                    String shardPath = shard.asText().substring("$blobchangefeed/".length());
                    shards.add(new Shard(client, shardPath).getEvents());
                }
                return json;
            })
            /* Round robin among shards in this segment. */
            .thenMany(Flux.merge(shards));
    }
}
