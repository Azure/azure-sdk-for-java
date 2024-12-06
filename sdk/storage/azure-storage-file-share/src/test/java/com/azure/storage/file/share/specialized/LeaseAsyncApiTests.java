// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.*;
import com.azure.storage.file.share.models.*;
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions;
import com.azure.storage.file.share.options.ShareBreakLeaseOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeaseAsyncApiTests extends FileShareTestBase {
    private ShareFileAsyncClient primaryFileAsyncClient;
    private ShareAsyncClient shareAsyncClient;
    private String shareName;

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        String filePath = generatePathName();
        shareAsyncClient = shareBuilderHelper(shareName).buildAsyncClient();
        shareAsyncClient.create().block();
        primaryFileAsyncClient = fileBuilderHelper(shareName, filePath).buildFileAsyncClient();
        primaryFileAsyncClient.create(50).block();
    }

    @Test
    public void acquireFileLease() {
        ShareLeaseAsyncClient leaseClient = createLeaseClient(primaryFileAsyncClient);
        StepVerifier.create(leaseClient.acquireLease()).assertNext(leaseId -> {
            assertNotNull(leaseId);
            assertEquals(leaseId, leaseClient.getLeaseId());
        }).verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.getPropertiesWithResponse(null)).assertNext(response -> {
            ShareFileProperties properties = response.getValue();
            assertEquals(LeaseStateType.LEASED, properties.getLeaseState());
            assertEquals(LeaseDurationType.INFINITE, properties.getLeaseDuration());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void acquireAndReleaseFileLeaseTrailingDot() {
        ShareFileAsyncClient shareFileAsyncClient = getFileAsyncClient(shareName, generatePathName() + ".", true, null);
        Mono<ShareFileInfo> createFile = shareFileAsyncClient.create(DATA.getDefaultDataSizeLong());

        ShareLeaseAsyncClient leaseClient = new ShareLeaseClientBuilder().fileAsyncClient(shareFileAsyncClient)
            .allowTrailingDot(true)
            .buildAsyncClient();

        Mono<Response<String>> acquireLease = leaseClient.acquireLeaseWithResponse(null, null);
        Mono<Response<Void>> releaseLease = leaseClient.releaseLeaseWithResponse(null);

        StepVerifier.create(createFile.then(acquireLease).flatMap(acquireResponse -> {
            assertEquals(201, acquireResponse.getStatusCode());
            assertNotNull(acquireResponse.getValue());
            return releaseLease;
        }).flatMap(releaseResponse -> {
            assertEquals(200, releaseResponse.getStatusCode());
            return Mono.empty();
        })).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void acquireAndReleaseFileLeaseOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(generatePathName());
        Mono<ShareDirectoryInfo> createDir = dirClient.create();
        ShareFileAsyncClient fileClient = dirClient.getFileClient(generatePathName());
        Mono<ShareFileInfo> createFile = fileClient.create(Constants.KB);
        ShareLeaseAsyncClient leaseClient = new ShareLeaseClientBuilder().fileAsyncClient(fileClient)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .buildAsyncClient();

        Mono<Response<String>> acquireLease = leaseClient.acquireLeaseWithResponse(null, null);
        Mono<Response<Void>> releaseLease = leaseClient.releaseLeaseWithResponse(null);

        StepVerifier.create(createDir.then(createFile).then(acquireLease).flatMap(acquireResponse -> {
            assertEquals(201, acquireResponse.getStatusCode());
            assertNotNull(acquireResponse.getValue());
            return releaseLease;
        }).flatMap(releaseResponse -> {
            assertEquals(200, releaseResponse.getStatusCode());
            return Mono.empty();
        })).verifyComplete();
    }

    @Test
    public void acquireFileLeaseError() {
        ShareFileAsyncClient fileClient = shareAsyncClient.getFileClient("garbage");
        StepVerifier.create(createLeaseClient(fileClient).acquireLease())
            .expectError(ShareStorageException.class)
            .verify();
    }

    @Test
    public void releaseLease() {
        StepVerifier
            .create(setupFileLeaseCondition(primaryFileAsyncClient, RECEIVED_LEASE_ID)
                .flatMap(leaseID -> createLeaseClient(primaryFileAsyncClient, leaseID).releaseLeaseWithResponse(null))
                .flatMap(response -> primaryFileAsyncClient.getProperties()))
            .assertNext(properties -> assertEquals(LeaseStateType.AVAILABLE, properties.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void releaseLeaseMin() {
        StepVerifier
            .create(setupFileLeaseCondition(primaryFileAsyncClient, RECEIVED_LEASE_ID)
                .flatMap(leaseID -> createLeaseClient(primaryFileAsyncClient, leaseID).releaseLease()))
            .verifyComplete();
    }

    @Test
    public void releaseFileLeaseError() {
        ShareFileAsyncClient fileClient = shareAsyncClient.getFileClient("garbage");
        StepVerifier.create(createLeaseClient(fileClient, "id").releaseLease())
            .expectError(ShareStorageException.class)
            .verify();
    }

    @Test
    public void breakFileLease() {
        ShareLeaseAsyncClient leaseClient = createLeaseClient(primaryFileAsyncClient, testResourceNamer.randomUuid());
        StepVerifier
            .create(
                leaseClient.acquireLease().then(leaseClient.breakLease()).then(primaryFileAsyncClient.getProperties()))
            .assertNext(properties -> assertEquals(LeaseStateType.BROKEN, properties.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void breakFileLeaseMin() {
        StepVerifier.create(setupFileLeaseCondition(primaryFileAsyncClient, RECEIVED_LEASE_ID)
            .flatMap(leaseID -> createLeaseClient(primaryFileAsyncClient, leaseID).breakLease())).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void breakFileLeaseTrailingDot() {
        ShareFileAsyncClient shareFileAsyncClient = getFileAsyncClient(shareName, generatePathName() + ".", true, null);
        Mono<ShareFileInfo> createFile = shareFileAsyncClient.create(DATA.getDefaultDataSizeLong());

        ShareLeaseAsyncClient leaseClient = new ShareLeaseClientBuilder().fileAsyncClient(shareFileAsyncClient)
            .allowTrailingDot(true)
            .buildAsyncClient();

        Mono<Response<String>> acquireLease = leaseClient.acquireLeaseWithResponse(null, null);
        Mono<Response<Void>> breakLease = leaseClient.breakLeaseWithResponse(null);

        StepVerifier.create(createFile.then(acquireLease)
            .flatMap(acquireResponse -> breakLease)
            .flatMap(breakResponse -> shareFileAsyncClient.getProperties())).assertNext(properties -> {
                assertEquals(LeaseStateType.BROKEN, properties.getLeaseState());
            }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void breakFileLeaseOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(generatePathName());
        Mono<ShareDirectoryInfo> createDir = dirClient.create();
        ShareFileAsyncClient fileClient = dirClient.getFileClient(generatePathName());
        Mono<ShareFileInfo> createFile = fileClient.create(Constants.KB);

        ShareLeaseAsyncClient leaseClient = new ShareLeaseClientBuilder().fileAsyncClient(fileClient)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .buildAsyncClient();
        Mono<Response<String>> acquireLease = leaseClient.acquireLeaseWithResponse(null, null);
        Mono<Response<Void>> breakLease = leaseClient.breakLeaseWithResponse(null);

        StepVerifier.create(createDir.then(createFile)
            .then(acquireLease)
            .flatMap(acquireResponse -> breakLease)
            .flatMap(breakResponse -> fileClient.getProperties())).assertNext(properties -> {
                assertEquals(LeaseStateType.BROKEN, properties.getLeaseState());
            }).verifyComplete();
    }

    @Test
    public void breakFileLeaseError() {
        StepVerifier.create(createLeaseClient(primaryFileAsyncClient).breakLease())
            .expectError(ShareStorageException.class)
            .verify();
    }

    @Test
    public void changeFileLease() {
        String initialLeaseId = testResourceNamer.randomUuid();
        String newLeaseId = testResourceNamer.randomUuid();
        ShareLeaseAsyncClient leaseClient = createLeaseClient(primaryFileAsyncClient, initialLeaseId);

        StepVerifier.create(leaseClient.acquireLease().flatMap(leaseId -> {
            assertNotNull(leaseId);
            return leaseClient.changeLeaseWithResponse(newLeaseId, null).flatMap(changeResponse -> {
                assertEquals(newLeaseId, changeResponse.getValue());
                assertEquals(newLeaseId, leaseClient.getLeaseId());

                ShareLeaseAsyncClient newLeaseClient
                    = createLeaseClient(primaryFileAsyncClient, changeResponse.getValue());
                return newLeaseClient.releaseLeaseWithResponse(null).flatMap(releaseResponse -> {
                    assertEquals(200, releaseResponse.getStatusCode());
                    return Mono.empty();
                });
            });
        })).verifyComplete();
    }

    @Test
    public void changeFileLeaseMin() {
        StepVerifier.create(setupFileLeaseCondition(primaryFileAsyncClient, RECEIVED_LEASE_ID).flatMap(
            leaseID -> createLeaseClient(primaryFileAsyncClient, leaseID).changeLease(testResourceNamer.randomUuid())))
            .expectNextCount(1)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void changeFileLeaseTrailingDot() {
        ShareFileAsyncClient primaryFileAsyncClient
            = getFileAsyncClient(shareName, generatePathName() + ".", true, null);
        Mono<ShareFileInfo> createFile = primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong());

        ShareLeaseAsyncClient leaseClient = new ShareLeaseClientBuilder().fileAsyncClient(primaryFileAsyncClient)
            .allowTrailingDot(true)
            .buildAsyncClient();

        Mono<String> changeLease = leaseClient.acquireLease().flatMap(leaseID -> leaseClient.changeLease(leaseID));

        StepVerifier.create(createFile.then(changeLease)).assertNext(changeResponse -> {
            assertEquals(changeResponse, leaseClient.getLeaseId());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void changeFileLeaseOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(generatePathName());
        Mono<ShareDirectoryInfo> createDir = dirClient.create();
        ShareFileAsyncClient fileClient = dirClient.getFileClient(generatePathName());
        Mono<ShareFileInfo> createFile = fileClient.create(Constants.KB);

        ShareLeaseAsyncClient leaseClient = new ShareLeaseClientBuilder().fileAsyncClient(fileClient)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .buildAsyncClient();

        Mono<String> changeLease = leaseClient.acquireLease().flatMap(leaseID -> leaseClient.changeLease(leaseID));

        StepVerifier.create(createDir.then(createFile).then(changeLease)).assertNext(changeResponse -> {
            assertEquals(changeResponse, leaseClient.getLeaseId());
        }).verifyComplete();
    }

    @Test
    public void changeFileLeaseError() {
        ShareFileAsyncClient fileClient = shareAsyncClient.getFileClient("garbage");
        StepVerifier.create(createLeaseClient(fileClient, "id").changeLease("id"))
            .expectError(ShareStorageException.class)
            .verify();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("acquireShareLeaseSupplier")
    public void acquireShareLease(String proposedID, int leaseTime, LeaseStateType leaseState,
        LeaseDurationType leaseDuration) {
        ShareLeaseAsyncClient leaseClient = createLeaseClient(shareAsyncClient, proposedID);

        Mono<Response<String>> leaseResponseMono
            = leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(leaseTime), null);

        StepVerifier.create(leaseResponseMono.flatMap(leaseResponse -> {
            assertEquals(leaseClient.getLeaseId(), leaseResponse.getValue());
            return shareAsyncClient.getProperties();
        })).assertNext(properties -> {
            assertNotNull(properties);
            assertEquals(leaseState, properties.getLeaseState());
            assertEquals(leaseDuration, properties.getLeaseDuration());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @ParameterizedTest
    @MethodSource("acquireShareLeaseSupplier")
    public void acquireShareLeaseOAuth(String proposedID, int leaseTime, LeaseStateType leaseState,
        LeaseDurationType leaseDuration) {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        ShareLeaseAsyncClient leaseClient = createLeaseClient(shareClient, proposedID);
        Mono<Response<String>> leaseResponseMono
            = leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(leaseTime), null);

        StepVerifier.create(leaseResponseMono.flatMap(leaseResponse -> {
            assertEquals(leaseClient.getLeaseId(), leaseResponse.getValue());
            return shareClient.getProperties();
        })).assertNext(properties -> {
            assertNotNull(properties);
            assertEquals(leaseState, properties.getLeaseState());
            assertEquals(leaseDuration, properties.getLeaseDuration());
        }).verifyComplete();
    }

    private static Stream<Arguments> acquireShareLeaseSupplier() {
        return Stream.of(Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(CoreUtils.randomUuid().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void acquireShareLeaseMin() {
        StepVerifier.create(createLeaseClient(shareAsyncClient).acquireLeaseWithResponse(null, null))
            .assertNext(response -> assertEquals(201, response.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void acquireShareLeaseSnapshot() {
        Mono<Response<String>> response = shareAsyncClient.createSnapshot().flatMap(snapshotInfo -> {
            ShareAsyncClient sc
                = shareBuilderHelper(shareAsyncClient.getShareName(), snapshotInfo.getSnapshot()).buildAsyncClient();
            ShareLeaseAsyncClient leaseClient = createLeaseClient(sc);

            Supplier<Mono<Response<String>>> action
                = () -> leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1));

            Predicate<ShareStorageException> retryPredicate
                = it -> it.getErrorCode() == ShareErrorCode.SHARE_SNAPSHOT_IN_PROGRESS;

            int times = 6;
            Duration delay = Duration.ofSeconds(10);

            return retryAsync(action, retryPredicate, times, delay)
                .flatMap(resp -> createLeaseClient(sc, resp.getValue()).releaseLease().thenReturn(resp));
        });

        StepVerifier.create(response).assertNext(resp -> {
            assertNotNull(resp);
            assertEquals(201, resp.getStatusCode());
        }).verifyComplete();
    }

    @Test
    public void acquireShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareAsyncClient sc = shareBuilderHelper(shareAsyncClient.getShareName(), shareSnapshot).buildAsyncClient();
        StepVerifier
            .create(
                createLeaseClient(sc).acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1), null))
            .expectError(ShareStorageException.class)
            .verify();
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireShareLeaseDurationFail(int duration) {
        ShareLeaseAsyncClient leaseClient = createLeaseClient(shareAsyncClient);
        StepVerifier
            .create(leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(duration), null))
            .expectError(ShareStorageException.class)
            .verify();
    }

    @Test
    public void acquireShareLeaseError() {
        ShareAsyncClient shareAsyncClient = shareBuilderHelper(generateShareName()).buildAsyncClient();
        StepVerifier
            .create(createLeaseClient(shareAsyncClient)
                .acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(20), null))
            .expectError(ShareStorageException.class)
            .verify();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void renewShareLease() {
        Mono<String> leaseIDMono = setupShareLeaseAsyncCondition(shareAsyncClient, RECEIVED_LEASE_ID);
        Mono<ShareLeaseAsyncClient> leaseClientMono
            = leaseIDMono.map(leaseID -> createLeaseClient(shareAsyncClient, leaseID));

        StepVerifier.create(leaseClientMono.flatMap(leaseClient -> {
            // If running in live mode wait for the lease to expire to ensure we are actually renewing it
            return Mono.delay(Duration.ofSeconds(16))
                .then(leaseClient.renewLeaseWithResponse(null))
                .flatMap(renewLeaseResponse -> {
                    assertEquals(leaseClient.getLeaseId(), renewLeaseResponse.getValue());
                    return shareAsyncClient.getProperties();
                })
                .flatMap(properties -> {
                    assertEquals(LeaseStateType.LEASED, properties.getLeaseState());
                    return Mono.empty();
                });
        })).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void renewShareLeaseOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        Mono<String> leaseIDMono = setupShareLeaseAsyncCondition(shareClient, RECEIVED_LEASE_ID);
        Mono<ShareLeaseAsyncClient> leaseClientMono
            = leaseIDMono.map(leaseID -> createLeaseClient(shareClient, leaseID));

        StepVerifier.create(leaseClientMono.flatMap(leaseClient -> {
            // If running in live mode wait for the lease to expire to ensure we are actually renewing it
            return Mono.delay(Duration.ofSeconds(16))
                .then(leaseClient.renewLeaseWithResponse(null))
                .flatMap(renewLeaseResponse -> {
                    assertEquals(leaseClient.getLeaseId(), renewLeaseResponse.getValue());
                    return shareClient.getProperties();
                })
                .flatMap(properties -> {
                    assertEquals(LeaseStateType.LEASED, properties.getLeaseState());
                    return Mono.empty();
                });
        })).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void renewShareLeaseMin() {
        StepVerifier
            .create(setupShareLeaseAsyncCondition(shareAsyncClient, RECEIVED_LEASE_ID)
                .flatMap(leaseID -> createLeaseClient(shareAsyncClient, leaseID).renewLeaseWithResponse(null)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void renewShareLeaseSnapshot() {
        Mono<String> shareSnapshotMono = shareAsyncClient.createSnapshot().map(ShareSnapshotInfo::getSnapshot);
        Mono<ShareAsyncClient> scMono = shareSnapshotMono
            .map(snapshot -> shareBuilderHelper(shareAsyncClient.getShareName(), snapshot).buildAsyncClient());

        StepVerifier.create(scMono.flatMap(sc -> setupShareLeaseAsyncCondition(sc, RECEIVED_LEASE_ID)
            .flatMap(leaseID -> createLeaseClient(sc, leaseID).renewLeaseWithResponse(null))
            .flatMap(resp -> {
                assertEquals(200, resp.getStatusCode());
                return createLeaseClient(sc, resp.getValue()).releaseLeaseWithResponse(null);
            }))).assertNext(releaseResp -> assertEquals(200, releaseResp.getStatusCode())).verifyComplete();
    }

    @Test
    public void renewShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareAsyncClient sc = shareBuilderHelper(shareAsyncClient.getShareName(), shareSnapshot).buildAsyncClient();
        StepVerifier.create(createLeaseClient(sc).renewLease()).expectError(ShareStorageException.class).verify();
    }

    @Test
    public void renewShareLeaseError() {
        ShareAsyncClient shareAsyncClient = shareBuilderHelper(generateShareName()).buildAsyncClient();
        StepVerifier.create(createLeaseClient(shareAsyncClient, "id").renewLease())
            .expectError(ShareStorageException.class)
            .verify();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void releaseShareLease() {
        StepVerifier.create(setupShareLeaseAsyncCondition(shareAsyncClient, RECEIVED_LEASE_ID)
            .flatMap(leaseID -> createLeaseClient(shareAsyncClient, leaseID).releaseLeaseWithResponse(null))
            .flatMap(response -> {
                assertEquals(200, response.getStatusCode());
                return shareAsyncClient.getProperties();
            })).assertNext(properties -> {
                assertEquals(LeaseStateType.AVAILABLE, properties.getLeaseState());
            }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void releaseShareLeaseOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);
        Mono<String> leaseIDMono = setupShareLeaseAsyncCondition(shareClient, RECEIVED_LEASE_ID);
        Mono<Response<Void>> releaseLeaseResponseMono
            = leaseIDMono.flatMap(leaseID -> createLeaseClient(shareClient, leaseID).releaseLeaseWithResponse(null));

        StepVerifier.create(releaseLeaseResponseMono.flatMap(response -> {
            assertEquals(200, response.getStatusCode());
            return shareClient.getProperties();
        })).assertNext(properties -> {
            assertEquals(LeaseStateType.AVAILABLE, properties.getLeaseState());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void releaseShareLeaseMin() {
        StepVerifier
            .create(setupShareLeaseAsyncCondition(shareAsyncClient, RECEIVED_LEASE_ID)
                .flatMap(leaseID -> createLeaseClient(shareAsyncClient, leaseID).releaseLeaseWithResponse(null)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void releaseShareLeaseSnapshot() {
        Mono<String> shareSnapshotMono = shareAsyncClient.createSnapshot().map(ShareSnapshotInfo::getSnapshot);
        Mono<ShareAsyncClient> scMono = shareSnapshotMono
            .map(snapshot -> shareBuilderHelper(shareAsyncClient.getShareName(), snapshot).buildAsyncClient());

        StepVerifier.create(scMono.flatMap(sc -> setupShareLeaseAsyncCondition(sc, RECEIVED_LEASE_ID)
            .flatMap(leaseID -> createLeaseClient(sc, leaseID).releaseLeaseWithResponse(null))
            .flatMap(response -> {
                assertEquals(200, response.getStatusCode());
                return Mono.empty();
            }))).verifyComplete();
    }

    @Test
    public void releaseShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareAsyncClient sc = shareBuilderHelper(shareAsyncClient.getShareName(), shareSnapshot).buildAsyncClient();
        StepVerifier.create(createLeaseClient(sc).releaseLeaseWithResponse(null))
            .expectError(ShareStorageException.class)
            .verify();
    }

    @Test
    public void releaseShareLeaseError() {
        ShareAsyncClient shareAsyncClient = shareBuilderHelper(generateShareName()).buildAsyncClient();
        StepVerifier.create(createLeaseClient(shareAsyncClient, "id").releaseLease())
            .expectError(ShareStorageException.class)
            .verify();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("breakShareLeaseSupplier")
    public void breakShareLease(int leaseTime, Long breakPeriod) {
        ShareLeaseAsyncClient leaseClient = createLeaseClient(shareAsyncClient, testResourceNamer.randomUuid());
        Mono<Response<String>> acquireLeaseMono
            = leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(leaseTime), null);
        Mono<Response<Void>> breakLeaseMono = acquireLeaseMono
            .flatMap(acquireResponse -> leaseClient.breakLeaseWithResponse(new ShareBreakLeaseOptions()
                .setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)), null));

        StepVerifier.create(breakLeaseMono.flatMap(breakResponse -> shareAsyncClient.getProperties().map(properties -> {
            LeaseStateType state = properties.getLeaseState();
            assertTrue(LeaseStateType.BROKEN == state || LeaseStateType.BREAKING == state);
            validateBasicHeaders(breakResponse.getHeaders());
            if (breakPeriod != null) {
                sleepIfRunningAgainstService(breakPeriod * 1000);
            }
            return Mono.empty();
        }).then())).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @ParameterizedTest
    @MethodSource("breakShareLeaseSupplier")
    public void breakShareLeaseOAuth(int leaseTime, Long breakPeriod) {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);
        ShareLeaseAsyncClient leaseClient = createLeaseClient(shareClient, testResourceNamer.randomUuid());

        Mono<Response<String>> acquireLeaseMono
            = leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(leaseTime), null);
        Mono<Response<Void>> breakLeaseMono = acquireLeaseMono
            .flatMap(acquireResponse -> leaseClient.breakLeaseWithResponse(new ShareBreakLeaseOptions()
                .setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)), null));

        StepVerifier.create(breakLeaseMono.flatMap(breakResponse -> shareClient.getProperties().map(properties -> {
            LeaseStateType state = properties.getLeaseState();
            assertTrue(LeaseStateType.BROKEN == state || LeaseStateType.BREAKING == state);
            validateBasicHeaders(breakResponse.getHeaders());
            if (breakPeriod != null) {
                sleepIfRunningAgainstService(breakPeriod * 1000);
            }
            return Mono.empty();
        }).then())).verifyComplete();
    }

    private static Stream<Arguments> breakShareLeaseSupplier() {
        return Stream.of(Arguments.of(-1, null), Arguments.of(-1, 20L), Arguments.of(20, 15L));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void breakShareLeaseMin() {
        StepVerifier
            .create(setupShareLeaseAsyncCondition(shareAsyncClient, RECEIVED_LEASE_ID)
                .then(createLeaseClient(shareAsyncClient).breakLeaseWithResponse(new ShareBreakLeaseOptions(), null)))
            .assertNext(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void breakShareLeaseSnapshot() {
        Mono<String> shareSnapshotMono = shareAsyncClient.createSnapshot().map(ShareSnapshotInfo::getSnapshot);
        Mono<ShareAsyncClient> scMono = shareSnapshotMono
            .map(snapshot -> shareBuilderHelper(shareAsyncClient.getShareName(), snapshot).buildAsyncClient());

        StepVerifier.create(scMono.flatMap(sc -> setupShareLeaseAsyncCondition(sc, RECEIVED_LEASE_ID)
            .flatMap(
                leaseID -> createLeaseClient(sc, leaseID).breakLeaseWithResponse(new ShareBreakLeaseOptions(), null))
            .flatMap(response -> {
                assertEquals(202, response.getStatusCode());
                return Mono.empty();
            }))).verifyComplete();
    }

    @Test
    public void breakShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareAsyncClient sc = shareBuilderHelper(shareAsyncClient.getShareName(), shareSnapshot).buildAsyncClient();
        StepVerifier.create(createLeaseClient(sc).breakLease()).expectError(ShareStorageException.class).verify();
    }

    @Test
    public void breakShareLeaseError() {
        ShareAsyncClient shareAsyncClient = shareBuilderHelper(generateShareName()).buildAsyncClient();
        StepVerifier.create(createLeaseClient(shareAsyncClient).breakLease())
            .expectError(ShareStorageException.class)
            .verify();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void changeShareLease() {
        StepVerifier.create(setupShareLeaseAsyncCondition(shareAsyncClient, RECEIVED_LEASE_ID).flatMap(leaseID -> {
            ShareLeaseAsyncClient leaseClient = createLeaseClient(shareAsyncClient, leaseID);
            String newLeaseId = testResourceNamer.randomUuid();
            return leaseClient.changeLeaseWithResponse(newLeaseId, null).flatMap(changeLeaseResponse -> {
                assertEquals(newLeaseId, changeLeaseResponse.getValue());
                assertEquals(leaseClient.getLeaseId(), changeLeaseResponse.getValue());
                validateBasicHeaders(changeLeaseResponse.getHeaders());
                return createLeaseClient(shareAsyncClient, newLeaseId).releaseLeaseWithResponse(null);
            }).flatMap(releaseLeaseResponse -> {
                assertEquals(200, releaseLeaseResponse.getStatusCode());
                return Mono.empty();
            });
        })).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void changeShareLeaseOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        Mono<String> leaseIDMono = setupShareLeaseAsyncCondition(shareClient, RECEIVED_LEASE_ID);
        Mono<Response<String>> changeLeaseResponseMono = leaseIDMono.flatMap(leaseID -> {
            ShareLeaseAsyncClient leaseClient = createLeaseClient(shareClient, leaseID);
            String newLeaseId = testResourceNamer.randomUuid();
            return leaseClient.changeLeaseWithResponse(newLeaseId, null);
        });

        StepVerifier.create(changeLeaseResponseMono.flatMap(changeLeaseResponse -> {
            String newLeaseId = changeLeaseResponse.getValue();
            assertEquals(newLeaseId, changeLeaseResponse.getValue());
            assertEquals(newLeaseId, createLeaseClient(shareClient, newLeaseId).getLeaseId());
            return createLeaseClient(shareClient, newLeaseId).releaseLeaseWithResponse(null);
        }).flatMap(releaseLeaseResponse -> {
            assertEquals(200, releaseLeaseResponse.getStatusCode());
            return Mono.empty();
        })).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void changeShareLeaseMin() {
        StepVerifier
            .create(setupShareLeaseAsyncCondition(shareAsyncClient, RECEIVED_LEASE_ID)
                .flatMap(leaseID -> createLeaseClient(shareAsyncClient, leaseID)
                    .changeLeaseWithResponse(testResourceNamer.randomUuid(), null)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void changeShareLeaseSnapshot() {
        Mono<String> shareSnapshotMono = shareAsyncClient.createSnapshot().map(ShareSnapshotInfo::getSnapshot);
        Mono<ShareAsyncClient> scMono = shareSnapshotMono
            .map(snapshot -> shareBuilderHelper(shareAsyncClient.getShareName(), snapshot).buildAsyncClient());

        StepVerifier.create(scMono.flatMap(sc -> setupShareLeaseAsyncCondition(sc, RECEIVED_LEASE_ID)
            .flatMap(
                leaseID -> createLeaseClient(sc, leaseID).changeLeaseWithResponse(testResourceNamer.randomUuid(), null))
            .flatMap(resp -> {
                assertEquals(200, resp.getStatusCode());
                return createLeaseClient(sc, resp.getValue()).releaseLeaseWithResponse(null);
            }))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void changeShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareAsyncClient sc = shareBuilderHelper(shareAsyncClient.getShareName(), shareSnapshot).buildAsyncClient();
        StepVerifier.create(createLeaseClient(sc).changeLease(testResourceNamer.randomUuid()))
            .expectError(ShareStorageException.class)
            .verify();
    }

    @Test
    public void changeShareLeaseError() {
        ShareAsyncClient shareAsyncClient = shareBuilderHelper(generateShareName()).buildAsyncClient();
        StepVerifier.create(createLeaseClient(shareAsyncClient, "id").changeLease("id"))
            .expectError(ShareStorageException.class)
            .verify();
    }
}
