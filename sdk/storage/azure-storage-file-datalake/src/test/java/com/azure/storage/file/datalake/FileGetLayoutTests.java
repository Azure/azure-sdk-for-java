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

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link DataLakeFileClient#getLayout(DataLakeFileGetLayoutOptions)}, which proxies the already-implemented
 * {@code BlockBlobClient.getLayout} API. Mirrors {@code BlobClientBaseGetLayoutApiTests} in azure-storage-blob.
 */
public class FileGetLayoutTests extends DataLakeTestBase {
    private DataLakeFileClient fc;

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayout() {
        Iterator<DataLakeFileLayoutInfo> iterator = fc.getLayout(null).iterator();

        assertTrue(iterator.hasNext());
        DataLakeFileLayoutInfo info = iterator.next();

        assertNotNull(info.getETag());
        assertFalse(info.getETag().isEmpty());
        assertEquals(DATA.getDefaultDataSizeLong(), info.getFileSize());
        assertNotNull(info.getLastModified());
        assertNotNull(info.getCreationTime());
        assertEquals(LeaseStatusType.UNLOCKED, info.getLeaseStatus());
        assertEquals(LeaseStateType.AVAILABLE, info.getLeaseState());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutEmptyFile() {
        DataLakeFileClient emptyFile = dataLakeFileSystemClient.createFile(generatePathName());

        assertDoesNotThrow(() -> emptyFile.getLayout(null).stream().count());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutRange() {
        assertDoesNotThrow(
            () -> fc.getLayout(new DataLakeFileGetLayoutOptions().setRange(new FileRange(0, (long) Constants.KB)))
                .stream()
                .count());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutPageSize() {
        Iterator<PagedResponse<DataLakeFileLayoutInfo>> iterator = fc.getLayout(null).iterableByPage(1).iterator();
        int pageCount = 0;

        while (iterator.hasNext()) {
            PagedResponse<DataLakeFileLayoutInfo> page = iterator.next();
            assertTrue(page.getValue().size() <= 1);
            pageCount++;
        }

        assertTrue(pageCount > 0);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutContinuationToken() {
        Iterator<PagedResponse<DataLakeFileLayoutInfo>> iterator = fc.getLayout(null).iterableByPage(1).iterator();
        String token = iterator.next().getContinuationToken();

        assertDoesNotThrow(() -> fc.getLayout(null).iterableByPage(token).iterator().hasNext());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getLayoutAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        match = setupPathMatchCondition(fc, match);
        leaseID = setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions().setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertDoesNotThrow(
            () -> fc.getLayout(new DataLakeFileGetLayoutOptions().setRequestConditions(drc)).stream().count());
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
        DataLakeRequestConditions drc = new DataLakeRequestConditions().setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> fc.getLayout(new DataLakeFileGetLayoutOptions().setRequestConditions(drc)).stream().count());
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
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> fileClient.getLayout(null).stream().count());
    }
}
