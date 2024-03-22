// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.FileShareTestBase;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.LeaseDurationType;
import com.azure.storage.file.share.models.LeaseStateType;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions;
import com.azure.storage.file.share.options.ShareBreakLeaseOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeaseApiTests extends FileShareTestBase {
    private ShareFileClient primaryFileClient;
    private ShareClient shareClient;
    private String shareName;

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        String filePath = generatePathName();
        shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
        primaryFileClient.create(50);
    }

    @Test
    public void acquireFileLease() {
        ShareLeaseClient leaseClient = createLeaseClient(primaryFileClient, testResourceNamer.randomUuid());
        String leaseId = leaseClient.acquireLease();
        assertNotNull(leaseId);
        assertEquals(leaseId, leaseClient.getLeaseId());
        Response<ShareFileProperties> response = primaryFileClient.getPropertiesWithResponse(null, null);
        ShareFileProperties properties = response.getValue();
        assertEquals(LeaseStateType.LEASED, properties.getLeaseState());
        assertEquals(LeaseDurationType.INFINITE, properties.getLeaseDuration());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @Test
    public void acquireAndReleaseFileLeaseTrailingDot() {
        ShareFileClient shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        shareFileClient.create(DATA.getDefaultDataSizeLong());
        ShareLeaseClient leaseClient = new ShareLeaseClientBuilder()
            .fileClient(shareFileClient)
            .allowTrailingDot(true)
            .buildClient();

        Response<String> acquireResponse = leaseClient.acquireLeaseWithResponse(null, null);
        assertEquals(201, acquireResponse.getStatusCode());
        assertNotNull(acquireResponse.getValue());
        Response<Void> releaseResponse = leaseClient.releaseLeaseWithResponse(null, null);
        assertEquals(200, releaseResponse.getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void acquireAndReleaseFileLeaseOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB);
        ShareLeaseClient leaseClient = new ShareLeaseClientBuilder()
            .fileClient(fileClient)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .buildClient();

        Response<String> acquireResponse = leaseClient.acquireLeaseWithResponse(null, null);
        assertEquals(201, acquireResponse.getStatusCode());
        assertNotNull(acquireResponse.getValue());
        Response<Void> releaseResponse = leaseClient.releaseLeaseWithResponse(null, null);
        assertEquals(200, releaseResponse.getStatusCode());
    }

    @Test
    public void acquireFileLeaseError() {
        ShareFileClient fileClient = shareClient.getFileClient("garbage");
        assertThrows(ShareStorageException.class, () -> createLeaseClient(fileClient).acquireLease());
    }

    @Test
    public void releaseLease() {
        String leaseID = setupFileLeaseCondition(primaryFileClient, RECEIVED_LEASE_ID);
        createLeaseClient(primaryFileClient, leaseID).releaseLeaseWithResponse(null, null).getHeaders();
        assertEquals(LeaseStateType.AVAILABLE, primaryFileClient.getProperties().getLeaseState());
    }

    @Test
    public void releaseLeaseMin() {
        String leaseID = setupFileLeaseCondition(primaryFileClient, RECEIVED_LEASE_ID);
        assertDoesNotThrow(() -> createLeaseClient(primaryFileClient, leaseID).releaseLease());
    }

    @Test
    public void releaseFileLeaseError() {
        ShareFileClient fc = shareClient.getFileClient("garbage");
        assertThrows(ShareStorageException.class, () -> createLeaseClient(fc, "id").releaseLease());
    }

    @Test
    public void breakFileLease() {
        ShareLeaseClient leaseClient = createLeaseClient(primaryFileClient, testResourceNamer.randomUuid());
        leaseClient.acquireLease();

        leaseClient.breakLease();
        LeaseStateType leaseState = primaryFileClient.getProperties().getLeaseState();
        assertEquals(LeaseStateType.BROKEN, leaseState);
    }

    @Test
    public void breakFileLeaseMin() {
        setupFileLeaseCondition(primaryFileClient, RECEIVED_LEASE_ID);
        assertDoesNotThrow(() -> createLeaseClient(primaryFileClient).breakLease());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @Test
    public void breakFileLeaseTrailingDot() {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);

        primaryFileClient.create(DATA.getDefaultDataSizeLong());

        ShareLeaseClient leaseClient = new ShareLeaseClientBuilder()
            .fileClient(primaryFileClient)
            .allowTrailingDot(true)
            .buildClient();
        leaseClient.acquireLease();

        leaseClient.breakLease();
        LeaseStateType leaseState = primaryFileClient.getProperties().getLeaseState();
        assertEquals(LeaseStateType.BROKEN, leaseState);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void breakFileLeaseOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB);

        ShareLeaseClient leaseClient = new ShareLeaseClientBuilder()
            .fileClient(fileClient)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .buildClient();
        leaseClient.acquireLease();

        leaseClient.breakLease();
        LeaseStateType leaseState = fileClient.getProperties().getLeaseState();
        assertEquals(LeaseStateType.BROKEN, leaseState);
    }

    @Test
    public void breakFileLeaseError() {
        assertThrows(ShareStorageException.class, () -> createLeaseClient(primaryFileClient).breakLease());
    }

    @Test
    public void changeFileLease() {
        ShareLeaseClient leaseClient = createLeaseClient(primaryFileClient, testResourceNamer.randomUuid());
        leaseClient.acquireLease();

        String newLeaseId = testResourceNamer.randomUuid();
        Response<String> changeLeaseResponse = leaseClient.changeLeaseWithResponse(newLeaseId, null, null);
        assertEquals(leaseClient.getLeaseId(), changeLeaseResponse.getValue());
        assertEquals(newLeaseId, leaseClient.getLeaseId());

        ShareLeaseClient leaseClient2 = createLeaseClient(primaryFileClient, changeLeaseResponse.getValue());
        assertEquals(200, leaseClient2.releaseLeaseWithResponse(null, null).getStatusCode());
    }

    @Test
    public void changeFileLeaseMin() {
        String leaseID = setupFileLeaseCondition(primaryFileClient, RECEIVED_LEASE_ID);
        assertDoesNotThrow(() -> createLeaseClient(primaryFileClient, leaseID)
            .changeLease(testResourceNamer.randomUuid()));
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @Test
    public void changeFileLeaseTrailingDot() {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        ShareLeaseClient leaseClient = new ShareLeaseClientBuilder()
            .fileClient(primaryFileClient)
            .allowTrailingDot(true)
            .buildClient();

        String leaseID = leaseClient.acquireLease();
        String newLeaseID = leaseClient.changeLease(leaseID);
        assertEquals(leaseID, newLeaseID);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void changeFileLeaseOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB);

        ShareLeaseClient leaseClient = new ShareLeaseClientBuilder()
            .fileClient(fileClient)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .buildClient();

        String leaseID = leaseClient.acquireLease();
        String newLeaseID = leaseClient.changeLease(leaseID);
        assertEquals(leaseID, newLeaseID);
    }

    @Test
    public void changeFileLeaseError() {
        ShareFileClient fc = shareClient.getFileClient("garbage");
        assertThrows(ShareStorageException.class, () -> createLeaseClient(fc, "id").changeLease("id"));
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @ParameterizedTest
    @MethodSource("acquireShareLeaseSupplier")
    public void acquireShareLease(String proposedID, int leaseTime, LeaseStateType leaseState,
        LeaseDurationType leaseDuration) {
        ShareLeaseClient leaseClient = createLeaseClient(shareClient, proposedID);

        Response<String> leaseResponse = leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions()
            .setDuration(leaseTime), null, null);
        assertEquals(leaseClient.getLeaseId(), leaseResponse.getValue());

        ShareProperties properties = shareClient.getProperties();
        assertNotNull(leaseResponse.getValue());
        validateBasicHeaders(leaseResponse.getHeaders());
        assertEquals(leaseState, properties.getLeaseState());
        assertEquals(leaseDuration, properties.getLeaseDuration());
    }

    private static Stream<Arguments> acquireShareLeaseSupplier() {
        return Stream.of(
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(UUID.randomUUID().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
        );
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void acquireShareLeaseMin() {
        assertEquals(201, createLeaseClient(shareClient).acquireLeaseWithResponse(null, null, null).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void acquireShareLeaseSnapshot() throws InterruptedException {
        String shareSnapshot = shareClient.createSnapshot().getSnapshot();
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        ShareLeaseClient leaseClient = createLeaseClient(sc);

        Supplier<Response<String>> action = () ->
            leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1), null, null);

        Predicate<ShareStorageException> retryPredicate =
            it -> it.getErrorCode() == ShareErrorCode.SHARE_SNAPSHOT_IN_PROGRESS;

        int times = 6;
        Duration delay = Duration.ofSeconds(10);

        Response<String> resp = retry(action, retryPredicate, times, delay);
        assertNotNull(resp);
        assertEquals(201, resp.getStatusCode());
        createLeaseClient(sc, resp.getValue()).releaseLeaseWithResponse(null, null);

    }

    @Test
    public void acquireShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();

        assertThrows(ShareStorageException.class, () ->
            createLeaseClient(sc).acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1), null, null));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, 10, 70})
    public void acquireShareLeaseDurationFail(int duration) {
        ShareLeaseClient leaseClient = createLeaseClient(shareClient);
        assertThrows(ShareStorageException.class, () ->
            leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(duration), null, null));
    }

    @Test
    public void acquireShareLeaseError() {
        shareClient = shareBuilderHelper(generateShareName()).buildClient();
        assertThrows(ShareStorageException.class, () ->
            createLeaseClient(shareClient).acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(20),
                null, null));
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void renewShareLease() {
        String leaseID = setupShareLeaseCondition(shareClient, RECEIVED_LEASE_ID);
        ShareLeaseClient leaseClient = createLeaseClient(shareClient, leaseID);

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000);
        Response<String> renewLeaseResponse = leaseClient.renewLeaseWithResponse(null, null);
        assertEquals(leaseClient.getLeaseId(), renewLeaseResponse.getValue());
        assertEquals(LeaseStateType.LEASED, shareClient.getProperties().getLeaseState());
        validateBasicHeaders(renewLeaseResponse.getHeaders());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void renewShareLeaseMin() {
        String leaseID = setupShareLeaseCondition(shareClient, RECEIVED_LEASE_ID);
        assertEquals(200, createLeaseClient(shareClient, leaseID).renewLeaseWithResponse(null, null).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void renewShareLeaseSnapshot() {
        String shareSnapshot = shareClient.createSnapshot().getSnapshot();
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        String leaseID = setupShareLeaseCondition(sc, RECEIVED_LEASE_ID);

        Response<String> resp = createLeaseClient(sc, leaseID).renewLeaseWithResponse(null, null);
        assertEquals(200, resp.getStatusCode());
        createLeaseClient(sc, resp.getValue()).releaseLeaseWithResponse(null, null);
    }

    @Test
    public void renewShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        assertThrows(ShareStorageException.class, () -> createLeaseClient(sc).renewLease());
    }

    @Test
    public void renewShareLeaseError() {
        shareClient = shareBuilderHelper(generateShareName()).buildClient();
        assertThrows(ShareStorageException.class, () -> createLeaseClient(shareClient, "id").renewLease());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void releaseShareLease() {
        String leaseID = setupShareLeaseCondition(shareClient, RECEIVED_LEASE_ID);
        Response<Void> releaseLeaseResponse = createLeaseClient(shareClient, leaseID)
            .releaseLeaseWithResponse(null, null);

        assertEquals(LeaseStateType.AVAILABLE, shareClient.getProperties().getLeaseState());
        validateBasicHeaders(releaseLeaseResponse.getHeaders());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void releaseShareLeaseMin() {
        String leaseID = setupShareLeaseCondition(shareClient, RECEIVED_LEASE_ID);
        assertEquals(200, createLeaseClient(shareClient, leaseID).releaseLeaseWithResponse(null, null).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void releaseShareLeaseSnapshot() {
        String shareSnapshot = shareClient.createSnapshot().getSnapshot();
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        String leaseID = setupShareLeaseCondition(sc, RECEIVED_LEASE_ID);

        assertEquals(200, createLeaseClient(sc, leaseID).releaseLeaseWithResponse(null, null).getStatusCode());
    }

    @Test
    public void releaseShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        assertThrows(ShareStorageException.class, () ->
            createLeaseClient(shareClient).releaseLeaseWithResponse(null, null));
    }

    @Test
    public void releaseShareLeaseError() {
        shareClient = shareBuilderHelper(generateShareName()).buildClient();
        assertThrows(ShareStorageException.class, () -> createLeaseClient(shareClient, "id").releaseLease());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @ParameterizedTest
    @MethodSource("breakShareLeaseSupplier")
    public void breakShareLease(int leaseTime, Long breakPeriod) {
        ShareLeaseClient leaseClient = createLeaseClient(shareClient, testResourceNamer.randomUuid());
        leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(leaseTime), null, null);

        Response<Void> breakLeaseResponse = leaseClient.breakLeaseWithResponse(new ShareBreakLeaseOptions()
            .setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)), null, null);
        LeaseStateType state = shareClient.getProperties().getLeaseState();
        assertTrue(LeaseStateType.BROKEN == state || LeaseStateType.BREAKING == state);
        validateBasicHeaders(breakLeaseResponse.getHeaders());
        if (breakPeriod != null) {
            // If running in live mode wait for the lease to break so we can delete the share after the test completes
            sleepIfRecord(breakPeriod * 1000);
        }
    }

    private static Stream<Arguments> breakShareLeaseSupplier() {
        return Stream.of(Arguments.of(-1, null), Arguments.of(-1, 20L), Arguments.of(20, 15L));
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void breakShareLeaseMin() {
        setupShareLeaseCondition(shareClient, RECEIVED_LEASE_ID);
        assertEquals(202, createLeaseClient(shareClient).breakLeaseWithResponse(new ShareBreakLeaseOptions(), null,
            null).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void breakShareLeaseSnapshot() {
        String shareSnapshot = shareClient.createSnapshot().getSnapshot();
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        String leaseID = setupShareLeaseCondition(sc, RECEIVED_LEASE_ID);
        assertEquals(202, createLeaseClient(sc, leaseID).breakLeaseWithResponse(new ShareBreakLeaseOptions(), null,
            null).getStatusCode());


    }

    @Test
    public void breakShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        assertThrows(ShareStorageException.class, () -> createLeaseClient(sc).breakLease());
    }

    @Test
    public void breakShareLeaseError() {
        shareClient = shareBuilderHelper(generateShareName()).buildClient();
        assertThrows(ShareStorageException.class, () -> createLeaseClient(shareClient).breakLease());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void changeShareLease() {
        String leaseID = setupShareLeaseCondition(shareClient, RECEIVED_LEASE_ID);
        ShareLeaseClient leaseClient = createLeaseClient(shareClient, leaseID);
        String newLeaseId = testResourceNamer.randomUuid();
        Response<String> changeLeaseResponse = leaseClient.changeLeaseWithResponse(newLeaseId, null, null);
        assertEquals(newLeaseId, changeLeaseResponse.getValue());
        assertEquals(leaseClient.getLeaseId(), changeLeaseResponse.getValue());
        assertEquals(200, createLeaseClient(shareClient, newLeaseId).releaseLeaseWithResponse(null, null)
            .getStatusCode());
        validateBasicHeaders(changeLeaseResponse.getHeaders());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void changeShareLeaseMin() {
        String leaseID = setupShareLeaseCondition(shareClient, RECEIVED_LEASE_ID);
        assertEquals(200, createLeaseClient(shareClient, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), null, null).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void changeShareLeaseSnapshot() {
        String shareSnapshot = shareClient.createSnapshot().getSnapshot();
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        String leaseID = setupShareLeaseCondition(sc, RECEIVED_LEASE_ID);
        Response<String> resp = createLeaseClient(sc, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), null,  null);
        assertEquals(200, resp.getStatusCode());
        createLeaseClient(sc, resp.getValue()).releaseLeaseWithResponse(null, null);
    }

    @Test
    public void changeShareLeaseSnapshotFail() {
        String shareSnapshot = "2020-08-19T19:26:08.0000000Z";
        ShareClient sc = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient();
        assertThrows(ShareStorageException.class,
            () -> createLeaseClient(sc).changeLease(testResourceNamer.randomUuid()));
    }

    @Test
    public void changeShareLeaseError() {
        shareClient = shareBuilderHelper(generateShareName()).buildClient();
        assertThrows(ShareStorageException.class, () -> createLeaseClient(shareClient, "id").changeLease("id"));
    }
}
