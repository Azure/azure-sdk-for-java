// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageSeekableByteChannelShareFileWriteBehaviorTests extends FileShareTestBase {
    @ParameterizedTest
    @MethodSource("writeBehaviorWriteCallsToClientCorrectlySupplier")
    public void writeBehaviorWriteCallsToClientCorrectly(int offset, ShareRequestConditions conditions,
        FileLastWrittenMode mode) throws IOException {
        ShareFileClient client = Mockito.mock(ShareFileClient.class);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, conditions, mode);
        // Stubbed ShareFileUploadInfo
        ShareFileUploadInfo stubbedUploadInfo = new ShareFileUploadInfo("randomEtag", testResourceNamer.now(),
            new byte[0], false);

        // Stubbed Response
        Response<ShareFileUploadInfo> stubbedResponse = new SimpleResponse<>(null, 200, new HttpHeaders(),
            stubbedUploadInfo);

        when(client.uploadRangeWithResponse(ArgumentMatchers.argThat(new ShareFileUploadRangeOptionsMatcher(offset,
            conditions, mode)), isNull(), isNull()))
            .thenReturn(stubbedResponse);

        behavior.write(DATA.getDefaultData(), offset);

        verify(client, times(1)).uploadRangeWithResponse(any(), isNull(), isNull());
    }

    private static class ShareFileUploadRangeOptionsMatcher implements ArgumentMatcher<ShareFileUploadRangeOptions> {
        private final int offset;
        private final ShareRequestConditions conditions;
        private final FileLastWrittenMode mode;

        ShareFileUploadRangeOptionsMatcher(int offset, ShareRequestConditions conditions, FileLastWrittenMode mode) {
            this.offset = offset;
            this.conditions = conditions;
            this.mode = mode;
        }

        @Override
        public boolean matches(ShareFileUploadRangeOptions options) {
            try {
                return options.getOffset() == offset && (conditions == null ? options.getRequestConditions() == null
                    : conditions.equals(options.getRequestConditions()))
                    && (mode == null ? options.getLastWrittenMode() == null : mode.equals(options.getLastWrittenMode()))
                    && Arrays.equals(getBytes(options.getDataStream()), DATA.getDefaultBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
    public void writeBehaviorCanSeekAnywhereInFileRange(long fileSize, int position) {
        ShareFileClient client = Mockito.mock(ShareFileClient.class);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);
        when(client.getProperties())
            .thenReturn(new ShareFileProperties(null, null, null, null, fileSize, null,
                null, null, null, null, null, null, null, null, null, null, null, null));

        behavior.assertCanSeek(position);

        verify(client, times(1)).getProperties();
    }

    private static Stream<Arguments> writeBehaviorCanSeekAnywhereInFileRangeSupplier() {
        return Stream.of(
            Arguments.of(Constants.KB, 0),
            Arguments.of(Constants.KB, 500),
            Arguments.of(Constants.KB, Constants.KB));
    }

    @ParameterizedTest
    @MethodSource("writeBehaviorThrowsWhenSeekingBeyondRangeSupplier")
    public void writeBehaviorThrowsWhenSeekingBeyondRange(long fileSize, int position) {
        ShareFileClient client = Mockito.mock(ShareFileClient.class);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);
        when(client.getProperties())
            .thenReturn(new ShareFileProperties(null, null, null, null, fileSize, null,
                null, null, null, null, null, null, null, null, null, null, null, null));

        assertThrows(UnsupportedOperationException.class, () -> behavior.assertCanSeek(position));

        verify(client, times(1)).getProperties();
    }

    private static Stream<Arguments> writeBehaviorThrowsWhenSeekingBeyondRangeSupplier() {
        return Stream.of(
            Arguments.of(Constants.KB, Constants.KB + 1),
            Arguments.of(Constants.KB, -1));
    }

    @Test
    public void writeBehaviorTruncateUnsupported() {
        ShareFileClient client = Mockito.mock(ShareFileClient.class);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);
        assertThrows(UnsupportedOperationException.class, () -> behavior.resize(10));
    }

}
