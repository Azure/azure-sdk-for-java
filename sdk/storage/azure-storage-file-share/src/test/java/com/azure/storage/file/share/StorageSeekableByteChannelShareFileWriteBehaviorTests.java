// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareRequestConditions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;

public class StorageSeekableByteChannelShareFileWriteBehaviorTests extends FileShareTestBase {
    @ParameterizedTest
    @MethodSource("writeBehaviorWriteCallsToClientCorrectlySupplier")
    public void writeBehaviorWriteCallsToClientCorrectly(long offset, ShareRequestConditions conditions,
        FileLastWrittenMode lastWrittenMode) throws IOException {
        ShareFileClient client = Mockito.mock(ShareFileClient.class);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, conditions, lastWrittenMode);

        //when: "WriteBehavior.write() called"
        behavior.write(DATA.getDefaultData(), offset);

        //then: "Expected ShareFileClient upload parameters given"
        Mockito.verify(client, Mockito.times(1)).uploadRangeWithResponse(
            argThat(options -> {
                try {
                    return options.getOffset() == offset
                        && options.getRequestConditions() == conditions
                        && options.getLastWrittenMode() == lastWrittenMode
                        && Arrays.equals(getBytes(options.getDataStream()), DATA.getDefaultBytes());
                } catch (IOException e) {
                    return false;
                }
            }),
            isNull(),
            isNull());
    }

    private static byte[] getBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int bytesRead;
            // reading the content of the stream within a byte buffer
            byte[] data = new byte[8192];

            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            return buffer.toByteArray();
        }
    }

    private static Stream<Arguments> writeBehaviorWriteCallsToClientCorrectlySupplier() {
        return Stream.of(
            Arguments.of(0, null, null),
            Arguments.of(50, null, null),
            Arguments.of(0, new ShareRequestConditions(), null),
            Arguments.of(0, null, FileLastWrittenMode.PRESERVE));
    }

    @ParameterizedTest
    @MethodSource("writeBehaviorCanSeekAnywhereInFileRangeSupplier")
    public void writeBehaviorCanSeekAnywhereInFileRange(Long fileSize, long position) {
        ShareFileClient client = Mockito.mock(ShareFileClient.class);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);

        //when: "WriteBehavior.assertCanSeek() is called"
        behavior.assertCanSeek(position);

        //then: "Expected behavior"
        assertDoesNotThrow(() -> Mockito.verify(client, Mockito.times(1)).getProperties());

        Mockito.when(client.getProperties()).thenReturn(new ShareFileProperties(null, null, null, null, fileSize, null,
            null, null, null, null, null, null, null, null, null, null, null, null));
    }

    private static Stream<Arguments> writeBehaviorCanSeekAnywhereInFileRangeSupplier() {
        return Stream.of(
            Arguments.of(Constants.KB, 0),
            Arguments.of(Constants.KB, 500),
            Arguments.of(Constants.KB, Constants.KB));
    }

    @ParameterizedTest
    @MethodSource("writeBehaviorThrowsWhenSeekingBeyondRangeSupplier")
    public void writeBehaviorThrowsWhenSeekingBeyondRange(long fileSize, long position) {
        ShareFileClient client = Mockito.mock(ShareFileClient.class);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);

        //when: "WriteBehavior.assertCanSeek() is called"
        behavior.assertCanSeek(position);

        //then: "Expected behavior"
        assertThrows(UnsupportedOperationException.class, () -> Mockito.verify(client, Mockito.times(1))
            .getProperties());

        Mockito.when(client.getProperties()).thenReturn(new ShareFileProperties(null, null, null, null, fileSize, null,
            null, null, null, null, null, null, null, null, null, null, null, null));
    }

    private static Stream<Arguments> writeBehaviorThrowsWhenSeekingBeyondRangeSupplier() {
        return Stream.of(
            Arguments.of(Constants.KB, Constants.KB + 1),
            Arguments.of(Constants.KB, -1));
    }

    @Test
    public void writeBehaviorTruncateUnsupported() {
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(Mockito.mock(ShareFileClient.class), null, null);

        assertThrows(UnsupportedOperationException.class, () -> behavior.resize(10));
    }
}
