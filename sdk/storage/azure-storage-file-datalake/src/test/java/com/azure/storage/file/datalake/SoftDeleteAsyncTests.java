// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.test.TestMode;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy;
import com.azure.storage.file.datalake.models.DataLakeServiceProperties;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathDeletedItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void restorePath() {
        DataLakeDirectoryAsyncClient dir = fileSystemClient.getDirectoryAsyncClient(generatePathName());
        Mono<Void> dirStep = dir.create().then(dir.delete());

        DataLakeFileAsyncClient file = fileSystemClient.getFileAsyncClient(generatePathName());
        Mono<Void> fileStep = file.create().then(file.delete());

        Mono<List<PathDeletedItem>> paths = fileSystemClient.listDeletedPaths().collectList();

        Mono<DataLakePathAsyncClient> response1 = dirStep.then(fileStep).then(paths)
            .flatMap(r -> fileSystemClient.undeletePath(dir.getDirectoryName(), r.get(0).getDeletionId()));
        StepVerifier.create(response1)
            .assertNext(r -> {
                assertInstanceOf(DataLakeDirectoryAsyncClient.class, r);
                assertEquals(dir.getPathUrl(), r.getPathUrl());
            })
            .verifyComplete();

        StepVerifier.create(dir.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        Mono<DataLakePathAsyncClient> response2 = paths
            .flatMap(r -> fileSystemClient.undeletePath(file.getFileName(), r.get(0).getDeletionId()));
        StepVerifier.create(response2)
            .assertNext(r -> {
                assertInstanceOf(DataLakeFileAsyncClient.class, r);
                assertEquals(file.getPathUrl(), r.getPathUrl());
            })
            .verifyComplete();

        StepVerifier.create(file.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @ParameterizedTest
    @ValueSource(strings = {"!'();[]@&%=+\\$,#äÄöÖüÜß;", "%21%27%28%29%3B%5B%5D%40%26%25%3D%2B%24%2C%23äÄöÖüÜß%3B",
        " my cool directory ", "directory"})
    public void restorePathSpecialCharacters(String name) {
        DataLakeDirectoryAsyncClient dir = fileSystemClient.getDirectoryAsyncClient("dir" + name);
        Mono<Void> dirStep = dir.create().then(dir.delete());

        DataLakeFileAsyncClient file = fileSystemClient.getFileAsyncClient("file" + name);
        Mono<Void> fileStep = file.create().then(file.delete());

        Mono<List<PathDeletedItem>> paths = fileSystemClient.listDeletedPaths().collectList();

        Mono<DataLakePathAsyncClient> response1 = dirStep.then(fileStep).then(paths)
            .flatMap(r -> fileSystemClient.undeletePath(dir.getDirectoryName(), r.get(0).getDeletionId()));
        StepVerifier.create(response1)
            .assertNext(r -> assertInstanceOf(DataLakeDirectoryAsyncClient.class, r))
            .verifyComplete();

        StepVerifier.create(dir.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        Mono<DataLakePathAsyncClient> response2 = paths
            .flatMap(r -> fileSystemClient.undeletePath(file.getFileName(), r.get(0).getDeletionId()));
        StepVerifier.create(response2)
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

        Mono<Void> step = dir.create()
            .flatMap(r -> {
                DataLakeFileAsyncClient fc1 = dir.getFileAsyncClient(generatePathName());
                return fc1.create(true).then(fc1.delete());
            })
            .flatMap(r -> {
                DataLakeFileAsyncClient fc2 = dir.getFileAsyncClient(generatePathName());
                return fc2.create(true).then(fc2.delete());
            })
            .flatMap(r -> {
                DataLakeFileAsyncClient fc3 = fileSystemClient.getFileAsyncClient(generatePathName());
                return fc3.create().then(fc3.delete());
            });

        StepVerifier.create(step.thenMany(fileSystemClient.listDeletedPaths().byPage(1)))
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void listDeletedPathsPath() {
        DataLakeDirectoryAsyncClient dir = fileSystemClient.getDirectoryAsyncClient(generatePathName());
        DataLakeFileAsyncClient fc1 = dir.getFileAsyncClient(generatePathName()); // Create one file under the path
        Mono<Void> step = dir.create()
            .flatMap(r -> fc1.create(true).then(fc1.delete()))
            .flatMap(r -> {
                DataLakeFileAsyncClient fc2 = fileSystemClient.getFileAsyncClient(generatePathName()); // Create another file not under the path
                return fc2.create().then(fc2.delete());
            });

        StepVerifier.create(step.thenMany(fileSystemClient.listDeletedPaths(dir.getDirectoryName())))
            .assertNext(r -> {
                assertFalse(r.isPrefix());
                assertEquals(dir.getDirectoryName() + "/" + fc1.getFileName(), r.getPath());
            })
            .verifyComplete();
    }

    // TODO (gapra): Add more get paths tests (Github issue created)
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void listDeletedPaths() {
        DataLakeFileAsyncClient fc1 = fileSystemClient.getFileAsyncClient(generatePathName());

        Mono<Void> step = fc1.create(true).then(fc1.delete());

        StepVerifier.create(step.thenMany(fileSystemClient.listDeletedPaths()))
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
