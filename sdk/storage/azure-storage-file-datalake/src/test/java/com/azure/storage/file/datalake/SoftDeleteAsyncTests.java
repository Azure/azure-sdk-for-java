// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.test.TestMode;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy;
import com.azure.storage.file.datalake.models.DataLakeServiceProperties;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathDeletedItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ResourceLock("ServiceProperties")
public class SoftDeleteAsyncTests extends DataLakeTestBase {
    private DataLakeServiceAsyncClient softDeleteDataLakeServiceClient;
    private DataLakeFileSystemAsyncClient fileSystemClient;

    @BeforeAll
    public static void setupClass() {
        if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        // This is to enable soft delete until better way is found. No need for recording.
        DataLakeServiceAsyncClient setupClient = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getDataLakeSoftDeleteAccount().getDataLakeEndpoint())
            .credential(ENVIRONMENT.getDataLakeSoftDeleteAccount().getCredential())
            .buildAsyncClient();

        setupClient.setProperties(new DataLakeServiceProperties()
            .setDeleteRetentionPolicy(new DataLakeRetentionPolicy().setEnabled(true).setDays(2))).block();

        sleepIfLiveTesting(30000);
    }

    @BeforeEach
    public void setup() {
        softDeleteDataLakeServiceClient = getServiceAsyncClient(ENVIRONMENT.getDataLakeSoftDeleteAccount());
        fileSystemClient = softDeleteDataLakeServiceClient.getFileSystemAsyncClient(generateFileSystemName());
        fileSystemClient.create().block();
    }

    @AfterEach
    public void cleanup() {
        fileSystemClient.delete().block();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @Test
    public void restorePath() {
        DataLakeDirectoryAsyncClient dir = fileSystemClient.getDirectoryAsyncClient(generatePathName());
        dir.create().block();
        dir.delete().block();

        DataLakeFileAsyncClient file = fileSystemClient.getFileAsyncClient(generatePathName());
        file.create().block();
        file.delete().block();

        List<PathDeletedItem> paths = fileSystemClient.listDeletedPaths().collectList().block();

        String dirDeletionId = paths.get(0).getDeletionId();
        String fileDeletionId = paths.get(1).getDeletionId();

        StepVerifier.create(fileSystemClient.undeletePath(dir.getDirectoryName(), dirDeletionId))
            .assertNext(r -> {
                assertInstanceOf(DataLakeDirectoryAsyncClient.class, r);
                assertEquals(dir.getPathUrl(), r.getPathUrl());
            })
            .verifyComplete();

        StepVerifier.create(dir.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(fileSystemClient.undeletePath(file.getFileName(), fileDeletionId))
            .assertNext(r -> {
                assertInstanceOf(DataLakeFileAsyncClient.class, r);
                assertEquals(file.getPathUrl(), r.getPathUrl());
            })
            .verifyComplete();

        StepVerifier.create(file.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @ParameterizedTest
    @ValueSource(strings = {"!'();[]@&%=+\\$,#äÄöÖüÜß;", "%21%27%28%29%3B%5B%5D%40%26%25%3D%2B%24%2C%23äÄöÖüÜß%3B",
        " my cool directory ", "directory"})
    public void restorePathSpecialCharacters(String name) {
        name = Utility.urlEncode(name);
        DataLakeDirectoryAsyncClient dir = fileSystemClient.getDirectoryAsyncClient("dir" + name);
        dir.create().block();
        dir.delete().block();

        DataLakeFileAsyncClient file = fileSystemClient.getFileAsyncClient("file" + name);
        file.create().block();
        file.delete().block();

        List<PathDeletedItem> paths = fileSystemClient.listDeletedPaths().collectList().block();

        String dirDeletionId = paths.get(0).getDeletionId();
        String fileDeletionId = paths.get(1).getDeletionId();

        StepVerifier.create(fileSystemClient.undeletePath(Utility.urlEncode(dir.getDirectoryName()),
            dirDeletionId))
            .assertNext(r -> assertInstanceOf(DataLakeDirectoryAsyncClient.class, r))
            .verifyComplete();

        StepVerifier.create(dir.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(fileSystemClient.undeletePath(Utility.urlEncode(file.getFileName()), fileDeletionId))
            .assertNext(r -> assertInstanceOf(DataLakeFileAsyncClient.class, r))
            .verifyComplete();

        StepVerifier.create(file.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void restorePathError() {
        DataLakeFileSystemAsyncClient fsc = softDeleteDataLakeServiceClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(fsc.undeletePath("foo", "bar"))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void listDeletedPathsOptionsMaxResultsByPage() {
        DataLakeDirectoryAsyncClient dir = fileSystemClient.getDirectoryAsyncClient(generatePathName());
        dir.create().block();
        DataLakeFileAsyncClient fc1 = dir.getFileAsyncClient(generatePathName());
        fc1.create(true).block();
        fc1.delete().block();

        DataLakeFileAsyncClient fc2 = dir.getFileAsyncClient(generatePathName());
        fc2.create(true).block();
        fc2.delete().block();

        DataLakeFileAsyncClient fc3 = fileSystemClient.getFileAsyncClient(generatePathName());
        fc3.create().block();
        fc3.delete().block();

        StepVerifier.create(fileSystemClient.listDeletedPaths().byPage(1))
            .thenConsumeWhile(r -> {
                assertEquals(1, r.getValue().size());
                return true;
            })
            .verifyComplete();
    }

    @Test
    public void listDeletedPathsError() {
        DataLakeFileSystemAsyncClient fsc = softDeleteDataLakeServiceClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(fsc.listDeletedPaths())
            .verifyError(DataLakeStorageException.class);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @Test
    public void listDeletedPathsPath() {
        DataLakeDirectoryAsyncClient dir = fileSystemClient.getDirectoryAsyncClient(generatePathName());
        dir.create().block();
        DataLakeFileAsyncClient fc1 = dir.getFileAsyncClient(generatePathName()); // Create one file under the path
        fc1.create(true).block();
        fc1.delete().block();

        DataLakeFileAsyncClient fc2 = fileSystemClient.getFileAsyncClient(generatePathName()); // Create another file not under the path
        fc2.create().block();
        fc2.delete().block();

        StepVerifier.create(fileSystemClient.listDeletedPaths(dir.getDirectoryName()))
            .assertNext(r -> {
                assertFalse(r.isPrefix());
                assertEquals(dir.getDirectoryName() + "/" + fc1.getFileName(), r.getPath());
            })
            .verifyComplete();
    }

    // TODO (gapra): Add more get paths tests (Github issue created)
    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200804ServiceVersion")
    @Test
    public void listDeletedPaths() {
        DataLakeFileAsyncClient fc1 = fileSystemClient.getFileAsyncClient(generatePathName());
        fc1.create(true).block();
        fc1.delete().block();

        StepVerifier.create(fileSystemClient.listDeletedPaths())
            .assertNext(r -> {
                assertFalse(r.isPrefix());
                assertEquals(fc1.getFileName(), r.getPath());
                assertNotNull(r.getDeletedOn());
                assertNotNull(r.getDeletionId());
                assertNotNull(r.getRemainingRetentionDays());
            })
            .verifyComplete();
    }

}
