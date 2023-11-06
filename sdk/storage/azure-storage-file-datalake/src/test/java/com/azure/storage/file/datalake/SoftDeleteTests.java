// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.TestMode;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy;
import com.azure.storage.file.datalake.models.DataLakeServiceProperties;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathDeletedItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ResourceLock("ServiceProperties")
public class SoftDeleteTests extends DataLakeTestBase {
    private DataLakeServiceClient softDeleteDataLakeServiceClient;
    private DataLakeFileSystemClient fileSystemClient;

    @BeforeAll
    public static void setupClass() {
        if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        // This is to enable soft delete until better way is found. No need for recording.
        DataLakeServiceClient setupClient = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getDataLakeSoftDeleteAccount().getDataLakeEndpoint())
            .credential(ENVIRONMENT.getDataLakeSoftDeleteAccount().getCredential())
            .buildClient();

        setupClient.setProperties(new DataLakeServiceProperties()
            .setDeleteRetentionPolicy(new DataLakeRetentionPolicy().setEnabled(true).setDays(2)));

        sleepIfLiveTesting(30000);
    }

    @BeforeEach
    public void setup() {
        softDeleteDataLakeServiceClient = getServiceClient(ENVIRONMENT.getDataLakeSoftDeleteAccount());
        fileSystemClient = softDeleteDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        fileSystemClient.create();
    }

    @AfterEach
    public void cleanup() {
        fileSystemClient.delete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @Test
    public void restorePath() {
        DataLakeDirectoryClient dir = fileSystemClient.getDirectoryClient(generatePathName());
        dir.create();
        dir.delete();

        DataLakeFileClient file = fileSystemClient.getFileClient(generatePathName());
        file.create();
        file.delete();

        Iterator<PathDeletedItem> paths = fileSystemClient.listDeletedPaths().iterator();

        String dirDeletionId = paths.next().getDeletionId();
        String fileDeletionId = paths.next().getDeletionId();

        DataLakePathClient returnedClient = fileSystemClient.undeletePath(dir.getDirectoryName(), dirDeletionId);

        assertInstanceOf(DataLakeDirectoryClient.class, returnedClient);
        assertNotNull(dir.getProperties());
        assertEquals(dir.getPathUrl(), returnedClient.getPathUrl());

        returnedClient = fileSystemClient.undeletePath(file.getFileName(), fileDeletionId);

        assertInstanceOf(DataLakeFileClient.class, returnedClient);
        assertNotNull(file.getProperties());
        assertEquals(file.getPathUrl(), returnedClient.getPathUrl());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @ParameterizedTest
    @ValueSource(strings = {"!'();[]@&%=+\\$,#äÄöÖüÜß;", "%21%27%28%29%3B%5B%5D%40%26%25%3D%2B%24%2C%23äÄöÖüÜß%3B",
        " my cool directory ", "directory"})
    public void restorePathSpecialCharacters(String name) {
        name = Utility.urlEncode(name);
        DataLakeDirectoryClient dir = fileSystemClient.getDirectoryClient("dir" + name);
        dir.create();
        dir.delete();

        DataLakeFileClient file = fileSystemClient.getFileClient("file" + name);
        file.create();
        file.delete();

        Iterator<PathDeletedItem> paths = fileSystemClient.listDeletedPaths().iterator();

        String dirDeletionId = paths.next().getDeletionId();
        String fileDeletionId = paths.next().getDeletionId();

        DataLakePathClient returnedClient = fileSystemClient.undeletePath(Utility.urlEncode(dir.getDirectoryName()),
            dirDeletionId);

        assertInstanceOf(DataLakeDirectoryClient.class, returnedClient);
        assertNotNull(dir.getProperties());

        returnedClient = fileSystemClient.undeletePath(Utility.urlEncode(file.getFileName()), fileDeletionId);

        assertInstanceOf(DataLakeFileClient.class, returnedClient);
        assertNotNull(file.getProperties());
    }

    @Test
    public void restorePathError() {
        DataLakeFileSystemClient fsc = softDeleteDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> fsc.undeletePath("foo", "bar"));
    }

    @Test
    public void listDeletedPathsOptionsMaxResultsByPage() {
        DataLakeDirectoryClient dir = fileSystemClient.getDirectoryClient(generatePathName());
        dir.create();
        DataLakeFileClient fc1 = dir.getFileClient(generatePathName());
        fc1.create(true);
        fc1.delete();

        DataLakeFileClient fc2 = dir.getFileClient(generatePathName());
        fc2.create(true);
        fc2.delete();

        DataLakeFileClient fc3 = fileSystemClient.getFileClient(generatePathName());
        fc3.create();
        fc3.delete();

        for (PagedResponse<PathDeletedItem > page : fileSystemClient.listDeletedPaths().iterableByPage(1)) {
            assertEquals(1, page.getValue().size());
        }
    }

    @Test
    public void listDeletedPathsError() {
        DataLakeFileSystemClient fsc = softDeleteDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> fsc.listDeletedPaths().iterator().next());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @Test
    public void listDeletedPathsPath() {
        DataLakeDirectoryClient dir = fileSystemClient.getDirectoryClient(generatePathName());
        dir.create();
        DataLakeFileClient fc1 = dir.getFileClient(generatePathName()); // Create one file under the path
        fc1.create(true);
        fc1.delete();

        DataLakeFileClient fc2 = fileSystemClient.getFileClient(generatePathName()); // Create another file not under the path
        fc2.create();
        fc2.delete();

        List<PathDeletedItem> deletedFiles = fileSystemClient.listDeletedPaths(dir.getDirectoryName(), null, null)
            .stream().collect(Collectors.toList());

        assertEquals(1, deletedFiles.size());
        assertFalse(deletedFiles.get(0).isPrefix());
        assertEquals(dir.getDirectoryName() + "/" + fc1.getFileName(), deletedFiles.get(0).getPath());
    }

    // TODO (gapra): Add more get paths tests (Github issue created)
    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @Test
    public void listDeletedPaths() {
        DataLakeFileClient fc1 = fileSystemClient.getFileClient(generatePathName());
        fc1.create(true);
        fc1.delete();

        List<PathDeletedItem> deletedFiles = fileSystemClient.listDeletedPaths().stream().collect(Collectors.toList());

        assertEquals(1, deletedFiles.size());
        assertFalse(deletedFiles.get(0).isPrefix());
        assertEquals(fc1.getFileName(), deletedFiles.get(0).getPath());
        assertNotNull(deletedFiles.get(0).getDeletedOn());
        assertNotNull(deletedFiles.get(0).getDeletionId());
        assertNotNull(deletedFiles.get(0).getRemainingRetentionDays());
    }
}
