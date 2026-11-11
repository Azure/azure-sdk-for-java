// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that {@link ShareFileAsyncClient#beginCopy} reports invalid input through the returned {@link PollerFlux}
 * rather than throwing, and sends no request.
 */
public class AsyncBeginCopyValidationTests {
    private static final String SOURCE_URL = FileIdTestHelper.ENDPOINT + "/" + FileIdTestHelper.SHARE_NAME + "/source";

    private final AtomicInteger requestCount = new AtomicInteger();

    @Test
    public void fileIdAddressedClient() {
        ShareFileAsyncClient client = builder().fileId(FileIdTestHelper.FILE_ID).buildFileAsyncClient();

        verifyError(() -> client.beginCopy(SOURCE_URL, Collections.emptyMap(), null), IllegalStateException.class);
        verifyError(() -> client.beginCopy(SOURCE_URL, new ShareFileCopyOptions(), null), IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("copyFlagConflictSupplier")
    public void copyFlagAndSmbPropertyConflict(
        Function<CopyableFileSmbPropertiesList, CopyableFileSmbPropertiesList> flag,
        Function<FileSmbProperties, FileSmbProperties> property) {
        ShareFileAsyncClient client = builder().resourcePath("destination").buildFileAsyncClient();
        ShareFileCopyOptions options
            = new ShareFileCopyOptions().setSmbPropertiesToCopy(flag.apply(new CopyableFileSmbPropertiesList()))
                .setSmbProperties(property.apply(new FileSmbProperties()));

        verifyError(() -> client.beginCopy(SOURCE_URL, options, null), IllegalArgumentException.class);
    }

    private static Stream<Arguments> copyFlagConflictSupplier() {
        OffsetDateTime time = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        return Stream.of(
            Arguments.of(flag(list -> list.setFileAttributes(true)),
                property(p -> p.setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY)))),
            Arguments.of(flag(list -> list.setCreatedOn(true)), property(p -> p.setFileCreationTime(time))),
            Arguments.of(flag(list -> list.setLastWrittenOn(true)), property(p -> p.setFileLastWriteTime(time))),
            Arguments.of(flag(list -> list.setChangedOn(true)), property(p -> p.setFileChangeTime(time))));
    }

    @Test
    public void permissionWithSourceCopyMode() {
        ShareFileAsyncClient client = builder().resourcePath("destination").buildFileAsyncClient();
        ShareFileCopyOptions options = new ShareFileCopyOptions().setFilePermission("permission")
            .setPermissionCopyModeType(PermissionCopyModeType.SOURCE);

        verifyError(() -> client.beginCopy(SOURCE_URL, options, null), IllegalArgumentException.class);
    }

    private void verifyError(Supplier<PollerFlux<ShareFileCopyInfo, Void>> beginCopy,
        Class<? extends Throwable> expected) {
        PollerFlux<ShareFileCopyInfo, Void> poller = assertDoesNotThrow(beginCopy::get);
        StepVerifier.create(poller).verifyError(expected);
        assertEquals(0, requestCount.get());
    }

    private ShareFileClientBuilder builder() {
        HttpClient httpClient = request -> {
            requestCount.incrementAndGet();
            return Mono.error(new IllegalStateException("No request should be sent."));
        };
        return new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .credential(new StorageSharedKeyCredential("account", "a2V5"))
            .httpClient(httpClient);
    }

    private static Function<CopyableFileSmbPropertiesList, CopyableFileSmbPropertiesList>
        flag(Function<CopyableFileSmbPropertiesList, CopyableFileSmbPropertiesList> f) {
        return f;
    }

    private static Function<FileSmbProperties, FileSmbProperties>
        property(Function<FileSmbProperties, FileSmbProperties> f) {
        return f;
    }
}
