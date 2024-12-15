// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;

public class StorageSeekableByteChannelBlockBlobWriteBehaviorTests extends BlobTestBase {

    private static Map<String, String> testStringMap() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("fizz", "buzz");
        return map;
    }

    private StorageSeekableByteChannelBlockBlobWriteBehavior getSimpleBehavior(BlockBlobClient client) {
        return new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
            StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, null);
    }

    @ParameterizedTest
    @MethodSource("writeBehaviorWriteCallsToClientCorrectlySupplier")
    public void writeBehaviorWriteCallsToClientCorrectly(int offset, BlobRequestConditions conditions)
        throws IOException {
        // Given
        BlockBlobClient client = Mockito.mock(BlockBlobClient.class);
        // and: "Block blob behavior"
        StorageSeekableByteChannelBlockBlobWriteBehavior behavior
            = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, conditions,
                StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, null);

        // when: "WriteBehavior.write() called"
        behavior.write(DATA.getDefaultData(), offset);

        // and: "mockito verification capture"
        ArgumentCaptor<BlockBlobStageBlockOptions> optionsCaptor
            = ArgumentCaptor.forClass(BlockBlobStageBlockOptions.class);
        Mockito.verify(client, times(1)).stageBlockWithResponse(optionsCaptor.capture(), isNull(), isNull());

        // then: "Expected BlockBlobClient upload parameters given"
        assertEquals(optionsCaptor.getValue().getLeaseId(), conditions == null ? null : "foo");
    }

    private static Stream<Arguments> writeBehaviorWriteCallsToClientCorrectlySupplier() {
        return Stream.of(Arguments.of(0, null), Arguments.of(50, null),
            Arguments.of(0, new BlobRequestConditions().setLeaseId("foo")));
    }

    @ParameterizedTest
    @MethodSource("writeBehaviorCommitsWithCorrectSettingsSupplier")
    public void writeBehaviorCommitsWithCorrectSettings(BlobHttpHeaders headers, Map<String, String> metadata,
        Map<String, String> tags, AccessTier tier, BlobRequestConditions conditions) throws IOException {
        BlockBlobClient client = Mockito.mock(BlockBlobClient.class);
        StorageSeekableByteChannelBlockBlobWriteBehavior behavior
            = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, headers, metadata, tags, tier, conditions,
                StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, null);

        // Three writes
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);

        // WriteBehavior.commit() called
        behavior.commit(0);

        // mockito verification capture
        ArgumentCaptor<BlockBlobCommitBlockListOptions> optionsCaptor
            = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions.class);
        Mockito.verify(client, times(3)).stageBlockWithResponse(any(), any(), any());
        Mockito.verify(client, times(1)).commitBlockListWithResponse(optionsCaptor.capture(), isNull(), isNull());

        // Expected commit options
        assertEquals(headers, optionsCaptor.getValue().getHeaders());
        assertEquals(metadata, optionsCaptor.getValue().getMetadata());
        assertEquals(tags, optionsCaptor.getValue().getTags());
        assertEquals(tier, optionsCaptor.getValue().getTier());
        assertEquals(conditions, optionsCaptor.getValue().getRequestConditions());
        assertEquals(3, optionsCaptor.getValue().getBase64BlockIds().size());
    }

    private static Stream<Arguments> writeBehaviorCommitsWithCorrectSettingsSupplier() {
        return Stream.of(Arguments.of(null, null, null, null, null),
            Arguments.of(new BlobHttpHeaders(), null, null, null, null),
            Arguments.of(null, testStringMap(), null, null, null),
            Arguments.of(null, null, testStringMap(), null, null), Arguments.of(null, null, null, AccessTier.HOT, null),
            Arguments.of(null, null, null, null, new BlobRequestConditions()));
    }

    @Test
    public void writeBehaviorCommitOverwrite() throws IOException {
        // Given: "existing blocks"
        List<String> blocks = Arrays.asList("1", "2", "3", "4", "5");

        // Behavior set to overwrite
        BlockBlobClient client = Mockito.mock(BlockBlobClient.class);
        StorageSeekableByteChannelBlockBlobWriteBehavior behavior
            = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
                StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, blocks);

        // Three writes
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);

        // WriteBehavior.commit() called
        behavior.commit(0);

        // Mockito verification capture
        ArgumentCaptor<BlockBlobCommitBlockListOptions> optionsCaptor
            = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions.class);
        Mockito.verify(client, times(3)).stageBlockWithResponse(any(), any(), any());
        Mockito.verify(client, times(1)).commitBlockListWithResponse(optionsCaptor.capture(), isNull(), isNull());

        // Expected three writes and appropriate blocklist
        assertEquals(3, optionsCaptor.getValue().getBase64BlockIds().size());
        assertTrue(optionsCaptor.getValue().getBase64BlockIds().stream().noneMatch(blocks::contains));
    }

    @Test
    public void writeBehaviorCommitAppend() throws IOException {
        // Given: "existing blocks"
        List<String> blocks = Arrays.asList("1", "2", "3", "4", "5");

        // Behavior set to append
        BlockBlobClient client = Mockito.mock(BlockBlobClient.class);
        StorageSeekableByteChannelBlockBlobWriteBehavior behavior
            = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
                StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.APPEND, blocks);

        // Three writes
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);

        // WriteBehavior.commit() called
        behavior.commit(0);

        // Mockito verification capture
        ArgumentCaptor<BlockBlobCommitBlockListOptions> optionsCaptor
            = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions.class);
        Mockito.verify(client, times(3)).stageBlockWithResponse(any(), any(), any());
        Mockito.verify(client, times(1)).commitBlockListWithResponse(optionsCaptor.capture(), isNull(), isNull());

        // Expected three writes and appropriate blocklist
        assertEquals(blocks.size() + 3, optionsCaptor.getValue().getBase64BlockIds().size());
        List<String> capturedBlocks = optionsCaptor.getValue().getBase64BlockIds().subList(0, blocks.size());
        assertEquals(blocks, capturedBlocks);
    }

    @Test
    public void writeBehaviorCommitPrepend() throws IOException {
        // Given: "existing blocks"
        List<String> blocks = Arrays.asList("1", "2", "3", "4", "5");

        // Behavior set to prepend
        BlockBlobClient client = Mockito.mock(BlockBlobClient.class);
        StorageSeekableByteChannelBlockBlobWriteBehavior behavior
            = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
                StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.PREPEND, blocks);

        // Three writes
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);
        behavior.write(DATA.getDefaultData(), 0);

        // WriteBehavior.commit() called
        behavior.commit(0);

        // Mockito verification capture
        ArgumentCaptor<BlockBlobCommitBlockListOptions> optionsCaptor
            = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions.class);
        Mockito.verify(client, times(3)).stageBlockWithResponse(any(), any(), any());
        Mockito.verify(client, times(1)).commitBlockListWithResponse(optionsCaptor.capture(), isNull(), isNull());

        // Expected three writes and appropriate blocklist
        assertEquals(blocks.size() + 3, optionsCaptor.getValue().getBase64BlockIds().size());
        List<String> capturedBlocks = optionsCaptor.getValue().getBase64BlockIds().subList(3, blocks.size() + 3);
        assertEquals(blocks, capturedBlocks);
    }

    @Test
    public void writeBehaviorSeekUnsupported() {
        StorageSeekableByteChannelBlockBlobWriteBehavior behavior
            = getSimpleBehavior(Mockito.mock(BlockBlobClient.class));

        assertThrows(UnsupportedOperationException.class, () -> behavior.assertCanSeek(10));
    }

    @Test
    public void writeBehaviorTruncateUnsupported() {
        StorageSeekableByteChannelBlockBlobWriteBehavior behavior
            = getSimpleBehavior(Mockito.mock(BlockBlobClient.class));

        assertThrows(UnsupportedOperationException.class, () -> behavior.resize(10));
    }
}
