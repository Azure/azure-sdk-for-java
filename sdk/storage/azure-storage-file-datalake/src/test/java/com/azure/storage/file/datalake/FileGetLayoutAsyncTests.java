// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.file.datalake.models.DataLakeFileLayoutInfo;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.options.DataLakeFileGetLayoutOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Async counterpart to {@link FileGetLayoutTests}.
 */
public class FileGetLayoutAsyncTests extends DataLakeTestBase {
    private DataLakeFileAsyncClient fc;

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemAsyncClient.createFile(generatePathName()).block();
        fc.append(DATA.getDefaultBinaryData(), 0).then(fc.flush(DATA.getDefaultDataSizeLong(), true)).block();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayout() {
        StepVerifier.create(fc.getLayout(null).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            DataLakeFileLayoutInfo info = r.get(0);
            assertNotNull(info.getETag());
            assertFalse(info.getETag().isEmpty());
            assertEquals(DATA.getDefaultDataSizeLong(), info.getFileSize());
            assertNotNull(info.getLastModified());
            assertNotNull(info.getCreationTime());
            assertEquals(LeaseStatusType.UNLOCKED, info.getLeaseStatus());
            assertEquals(LeaseStateType.AVAILABLE, info.getLeaseState());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutEmptyFile() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMapMany(emptyFile -> emptyFile.getLayout(null))
            .then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutRange() {
        StepVerifier
            .create(
                fc.getLayout(new DataLakeFileGetLayoutOptions().setRange(new FileRange(0, (long) Constants.KB))).then())
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutPageSize() {
        StepVerifier.create(fc.getLayout(null).byPage(1).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            r.forEach(page -> assertTrue(page.getValue().size() <= 1));
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutContinuationToken() {
        Flux<PagedResponse<DataLakeFileLayoutInfo>> response
            = fc.getLayout(null).byPage(1).next().flatMapMany(r -> fc.getLayout(null).byPage(r.getContinuationToken()));

        StepVerifier.create(response.then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getLayoutAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        Flux<DataLakeFileLayoutInfo> response
            = Mono
                .zip(setupPathLeaseCondition(fc, leaseID), setupPathMatchCondition(fc, match),
                    DataLakeTestBase::convertNulls)
                .flatMapMany(conditions -> {
                    DataLakeRequestConditions drc = new DataLakeRequestConditions().setLeaseId(conditions.get(0))
                        .setIfMatch(conditions.get(1))
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);

                    return fc.getLayout(new DataLakeFileGetLayoutOptions().setRequestConditions(drc));
                });

        StepVerifier.create(response.then()).verifyComplete();
    }

    private static Stream<Arguments> modifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match        | noneMatch   | leaseID
            Arguments.of(null, null, null, null, null), Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null), Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void getLayoutACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        Mono<Long> response
            = Mono
                .zip(setupPathLeaseCondition(fc, leaseID), setupPathMatchCondition(fc, noneMatch),
                    DataLakeTestBase::convertNulls)
                .flatMap(conditions -> {
                    DataLakeRequestConditions drc = new DataLakeRequestConditions().setLeaseId(conditions.get(0))
                        .setIfMatch(match)
                        .setIfNoneMatch(conditions.get(1))
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);

                    return fc.getLayout(new DataLakeFileGetLayoutOptions().setRequestConditions(drc)).count();
                });

        StepVerifier.create(response).verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidModifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match        | noneMatch   | leaseID
            Arguments.of(NEW_DATE, null, null, null, null), Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null), Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutError() {
        DataLakeFileAsyncClient fileClient = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fileClient.getLayout(null)).verifyError(DataLakeStorageException.class);
    }
}
