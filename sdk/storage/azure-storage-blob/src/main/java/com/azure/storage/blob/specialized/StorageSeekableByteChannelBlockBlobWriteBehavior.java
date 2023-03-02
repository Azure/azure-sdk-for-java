// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteMode;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.common.StorageSeekableByteChannel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

class StorageSeekableByteChannelBlockBlobWriteBehavior implements StorageSeekableByteChannel.WriteBehavior {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannelBlockBlobWriteBehavior.class);

    private final BlockBlobClient client;
    private final BlobHttpHeaders headers;
    private final Map<String, String> metadata;
    private final Map<String, String> tags;
    private final AccessTier tier;
    private final BlobRequestConditions conditions;
    private final BlockBlobSeekableByteChannelWriteMode mode;
    private final List<String> existingBlockIds;
    private final List<String> newBlockIds = new ArrayList<>();

    StorageSeekableByteChannelBlockBlobWriteBehavior(BlockBlobClient client, BlobHttpHeaders headers,
        Map<String, String> metadata, Map<String, String> tags, AccessTier tier, BlobRequestConditions conditions,
        BlockBlobSeekableByteChannelWriteMode mode, List<String> existingBlockIds) {
        this.client = Objects.requireNonNull(client);
        this.headers = headers;
        this.metadata = metadata;
        this.tags = tags;
        this.tier = tier;
        this.conditions = conditions;
        this.mode = Objects.requireNonNull(mode);
        this.existingBlockIds = existingBlockIds != null ? existingBlockIds : Collections.emptyList();
    }

    @Override
    public void write(ByteBuffer src, long destOffset) {
        String blockId = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));
        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(blockId, BinaryData.fromByteBuffer(src));
        if (conditions != null) {
            options.setLeaseId(conditions.getLeaseId());
        }

        client.stageBlockWithResponse(options, null, null);
        newBlockIds.add(blockId);
    }

    @Override
    public void commit(long totalLength) {
        List<String> commitList;
        switch (this.mode) {
            case OVERWRITE:
                commitList = newBlockIds;
                break;
            case APPEND:
                commitList = Stream.of(existingBlockIds, newBlockIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
                break;
            case PREPEND:
                commitList = Stream.of(newBlockIds, existingBlockIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
                break;
            default:
                // Unreachable block to satisfy compiler
                throw new UnsupportedOperationException(
                    "Commit not supported with the configured BlockBlobSeekableByteChannelWriteMode.");
        }

        client.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(commitList)
                .setHeaders(headers).setMetadata(metadata).setTags(tags).setTier(tier).setRequestConditions(conditions),
            null, null);
    }

    @Override
    public boolean canSeek(long position) {
        return false;
    }

    @Override
    public void resize(long newSize) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Block blobs cannot have size explicitly set."));
    }
}
