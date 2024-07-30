// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageSeekableByteChannelShareFileWriteBehaviorTests extends FileShareTestBase {
    @ParameterizedTest
    @MethodSource("writeBehaviorWriteCallsToClientCorrectlySupplier")
    public void writeBehaviorWriteCallsToClientCorrectly(int offset, ShareRequestConditions conditions,
        FileLastWrittenMode mode) throws IOException {
        // Stubbed ShareFileUploadInfo
        ShareFileUploadInfo stubbedUploadInfo = new ShareFileUploadInfo("randomEtag", testResourceNamer.now(),
            new byte[0], false);
        // Stubbed Response
        Response<ShareFileUploadInfo> stubbedResponse = new SimpleResponse<>(null, 200, new HttpHeaders(),
            stubbedUploadInfo);

        AtomicInteger uploadRangeCallCount = new AtomicInteger(0);
        ShareFileClient client = new ShareFileClient(null, new AzureFileStorageImpl(null, null, "fakeurl", false,
            false), "testshare", "testpath", null, null, null, null) {
            @Override
            public Response<ShareFileUploadInfo> uploadRangeWithResponse(ShareFileUploadRangeOptions options,
                Duration timeout, Context context) {
                assertNull(timeout);
                assertNull(context);
                uploadRangeCallCount.incrementAndGet();
                assertEquals(offset, options.getOffset());
                assertEquals(conditions, options.getRequestConditions());
                assertEquals(mode, options.getLastWrittenMode());
                TestUtils.assertArraysEqual(DATA.getDefaultBytes(), getBytes(options.getDataStream()));

                return stubbedResponse;
            }
        };

        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, conditions, mode);

        behavior.write(DATA.getDefaultData(), offset);
        assertEquals(1, uploadRangeCallCount.get());
    }

    private static byte[] getBytes(InputStream is) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int bytesRead;
            // reading the content of the stream within a byte buffer
            byte[] data = new byte[8192];

            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
        AtomicInteger getPropertiesCallCount = new AtomicInteger(0);
        ShareFileClient client = new ShareFileClient(null, new AzureFileStorageImpl(null, null, "fakeurl", false,
            false), "testshare", "testpath", null, null, null, null) {
            @Override
            public ShareFileProperties getProperties() {
                getPropertiesCallCount.incrementAndGet();
                return new ShareFileProperties(null, null, null, null, fileSize, null,
                    null, null, null, null, null, null, null, null, null, null, null, null);
            }
        };
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);

        behavior.assertCanSeek(position);
        assertEquals(1, getPropertiesCallCount.get());
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
        AtomicInteger getPropertiesCallCount = new AtomicInteger(0);
        ShareFileClient client = new ShareFileClient(null, new AzureFileStorageImpl(null, null, "fakeurl", false,
            false), "testshare", "testpath", null, null, null, null) {
            @Override
            public ShareFileProperties getProperties() {
                getPropertiesCallCount.incrementAndGet();
                return new ShareFileProperties(null, null, null, null, fileSize, null,
                    null, null, null, null, null, null, null, null, null, null, null, null);
            }
        };

        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);

        assertThrows(UnsupportedOperationException.class, () -> behavior.assertCanSeek(position));
        assertEquals(1, getPropertiesCallCount.get());
    }

    private static Stream<Arguments> writeBehaviorThrowsWhenSeekingBeyondRangeSupplier() {
        return Stream.of(
            Arguments.of(Constants.KB, Constants.KB + 1),
            Arguments.of(Constants.KB, -1));
    }

    @Test
    public void writeBehaviorTruncateUnsupported() {
        ShareFileClient client = new ShareFileClient(null, new AzureFileStorageImpl(null, null, "fakeurl", false,
            false), "testshare", "testpath", null, null, null, null);
        StorageSeekableByteChannelShareFileWriteBehavior behavior =
            new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null);
        assertThrows(UnsupportedOperationException.class, () -> behavior.resize(10));
    }

}
