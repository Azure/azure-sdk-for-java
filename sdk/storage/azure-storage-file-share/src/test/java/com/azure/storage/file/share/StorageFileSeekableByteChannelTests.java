// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.common.implementation.StorageSeekableByteChannel;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.options.ShareFileSeekableByteChannelReadOptions;
import com.azure.storage.file.share.options.ShareFileSeekableByteChannelWriteOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StorageFileSeekableByteChannelTests extends FileShareTestBase {

    private ShareFileClient primaryFileClient;
    private ShareClient shareClient;

    @BeforeEach
    public void setup() {
        String shareName = generateShareName();
        String filePath = generatePathName();
        shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
    }

    @AfterEach
    public void cleanup() {
        shareClient.deleteIfExists();
    }

    @Test
    public void e2EChannelWrite() throws IOException {
        int streamBufferSize = 50;
        int copyBufferSize = 40;
        byte[] data = getRandomByteArray(1024);

        //when: "Channel initialized"
        SeekableByteChannel channel = primaryFileClient.getFileSeekableByteChannelWrite(
            new ShareFileSeekableByteChannelWriteOptions(true).setFileSize((long) data.length)
                .setChunkSizeInBytes((long) streamBufferSize));

        //then: "Channel initialized to position zero"
        assertEquals(channel.position(), 0);

        //when: "write to channel"
        int copied = FileShareTestHelper.copy(new ByteArrayInputStream(data), channel, copyBufferSize);

        //then: "channel position updated accordingly"
        assertEquals(copied, data.length);
        assertEquals(channel.position(), data.length);

        //when: "data fully flushed to Storage"
        channel.close();

        //and: "resource downloaded"
        ByteArrayOutputStream downloadedData = new ByteArrayOutputStream();
        primaryFileClient.download(downloadedData);

        //then: "expected data downloaded"
        assertArrayEquals(downloadedData.toByteArray(), data);
    }

    @ParameterizedTest
    @MethodSource("e2EChannelReadSupplier")
    public void e2EChannelRead(int streamBufferSize, int copyBufferSize, int dataLength) throws IOException {
        byte[] data = getRandomByteArray(dataLength);
        primaryFileClient.create(dataLength);
        primaryFileClient.upload(new ByteArrayInputStream(data), data.length, null);

        //when: "Channel initialized"
        SeekableByteChannel channel = primaryFileClient.getFileSeekableByteChannelRead(
            new ShareFileSeekableByteChannelReadOptions().setChunkSizeInBytes((long) streamBufferSize));

        //then: "Channel initialized to position zero"
        assertEquals(channel.position(), 0);

        //when: "read from channel"
        ByteArrayOutputStream downloadedData = new ByteArrayOutputStream();
        int copied = FileShareTestHelper.copy(channel, downloadedData, copyBufferSize);

        //then: "channel position updated accordingly"
        assertEquals(copied, data.length);
        assertEquals(channel.position(), data.length);

        //and: "expected data downloaded"
        assertArrayEquals(downloadedData.toByteArray(), data);
    }

    private static Stream<Arguments> e2EChannelReadSupplier() {
        return Stream.of(Arguments.of(50, 40, Constants.KB), Arguments.of(2 * Constants.KB, 40, Constants.KB) // initial fetch larger than resource size
        );
    }

    @ParameterizedTest
    @MethodSource("clientCreatesAppropriateChannelWriteModeSupplier")
    public void clientCreatesAppropriateChannelWriteMode(ShareRequestConditions conditions,
        FileLastWrittenMode lastWrittenMode) {
        //when: "make channel in write mode"
        StorageSeekableByteChannel channel
            = (StorageSeekableByteChannel) primaryFileClient.getFileSeekableByteChannelWrite(
                new ShareFileSeekableByteChannelWriteOptions(true).setRequestConditions(conditions)
                    .setFileLastWrittenMode(lastWrittenMode)
                    .setFileSize((long) Constants.KB));

        //then: "channel WriteBehavior has appropriate values"
        StorageSeekableByteChannelShareFileWriteBehavior writeBehavior
            = (StorageSeekableByteChannelShareFileWriteBehavior) channel.getWriteBehavior();
        assertEquals(writeBehavior.getClient(), primaryFileClient);
        assertEquals(writeBehavior.getRequestConditions(), conditions);
        assertEquals(writeBehavior.getLastWrittenMode(), lastWrittenMode);

        //and: "channel ReadBehavior is null"
        assertNull(channel.getReadBehavior());
    }

    private static Stream<Arguments> clientCreatesAppropriateChannelWriteModeSupplier() {
        return Stream.of(Arguments.of(null, null), Arguments.of(new ShareRequestConditions(), null),
            Arguments.of(null, FileLastWrittenMode.PRESERVE));
    }

    @Test
    public void clientCreatesAppropriateChannelReadMode() {
        List<ShareRequestConditions> conditions = Arrays.asList(null, new ShareRequestConditions());
        for (ShareRequestConditions condition : conditions) {
            //when: "make channel in read mode"
            StorageSeekableByteChannel channel
                = (StorageSeekableByteChannel) primaryFileClient.getFileSeekableByteChannelRead(
                    new ShareFileSeekableByteChannelReadOptions().setRequestConditions(condition));

            //then: "channel WriteBehavior is null"
            assertNull(channel.getWriteBehavior());

            //and: "channel ReadBehavior has appropriate values"
            StorageSeekableByteChannelShareFileReadBehavior readBehavior
                = (StorageSeekableByteChannelShareFileReadBehavior) channel.getReadBehavior();
            assertEquals(readBehavior.getClient(), primaryFileClient);
            assertEquals(readBehavior.getRequestConditions(), condition);

        }
    }
}
