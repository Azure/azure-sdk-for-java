// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.storage.file.models.StorageErrorException;

public class ShareClientTests extends ShareClientTestsBase {
    private final ServiceLogger logger = new ServiceLogger(ShareClient.class);

    private ShareClient client;

    @Override
    public void beforeTest() {
        shareName = getShareName();
        helper = new TestHelpers();

        if (interceptorManager.isPlaybackMode()) {
            client = helper.setupClient((connectionString, endpoint) -> ShareClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildSync(), true, logger);
        } else {
            client = helper.setupClient((connectionString, endpoint) -> ShareClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildSync(), false, logger);
        }
    }

    @Override
    public void afterTest() {
        try {
            client.delete();
        } catch (StorageErrorException ex) {
            // Ignore the exception as the share is already deleted and that is what we wanted.
        }
    }

    @Override
    public void getRootDirectoryDoesNotCreateADirectory() {

    }

    @Override
    public void getDirectoryDoesNotCreateADirectory() {

    }

    @Override
    public void createDirectory() {

    }

    @Override
    public void createDirectoryOnSnapshotIsNotAllowed() {

    }

    @Override
    public void createDirectoryInvalidName() {

    }

    @Override
    public void createShare() {

    }

    @Override
    public void createShareTwiceSameMetadata() {

    }

    @Override
    public void createShareTwiceDifferentMetadata() {

    }

    @Override
    public void createShareInvalidQuota() {

    }

    @Override
    public void deleteShare() {

    }

    @Override
    public void deleteShareDoesNotExist() {

    }

    @Override
    public void deleteThenCreateShare() {

    }

    @Override
    public void deleteThenCreateShareTooSoon() {

    }

    @Override
    public void snapshotShare() {

    }

    @Override
    public void deleteShareSnapshot() {

    }

    @Override
    public void snapshotShareSameMetadata() {

    }

    @Override
    public void snapshotShareDifferentMetadata() {

    }

    @Override
    public void snapshotShareDoesNotExist() {

    }

    @Override
    public void getShareProperties() {

    }

    @Override
    public void getSnapshotShareProperties() {

    }

    @Override
    public void getSharePropertiesDoesNotExist() {

    }

    @Override
    public void getSnapshotSharePropertiesDoesNotExist() {

    }

    @Override
    public void setShareProperties() {

    }

    @Override
    public void setSnapshotSharePropertiesIsNotAllowed() {

    }

    @Override
    public void setSharePropertiesInvalidQuota() {

    }

    @Override
    public void setSharePropertiesDoesNotExist() {

    }

    @Override
    public void getShareMetadata() {

    }

    @Override
    public void getSnapshotShareMetadata() {

    }

    @Override
    public void getShareMetadataDoesNotExist() {

    }

    @Override
    public void getSnapshotShareMetadataDoesNotExist() {

    }

    @Override
    public void setShareMetadata() {

    }

    @Override
    public void setSnapshotShareMetadataIsNotAllowed() {

    }

    @Override
    public void setShareMetadataInvalidMetadata() {

    }

    @Override
    public void setShareMetadataDoesNotExist() {

    }

    @Override
    public void getSharePolicies() {

    }

    @Override
    public void getSnapshotSharePoliciesIsNotAllowed() {

    }

    @Override
    public void getSharePoliciesDoesNotExist() {

    }

    @Override
    public void setSharePolicies() {

    }

    @Override
    public void setSnapshotSharePoliciesIsNotAllowed() {

    }

    @Override
    public void setSharePoliciesInvalidPermission() {

    }

    @Override
    public void setSharePoliciesTooManyPermissions() {

    }

    @Override
    public void setSharePoliciesDoesNotExist() {

    }

    @Override
    public void getShareStats() {

    }

    @Override
    public void getSnapshotShareStatsIsNotAllowed() {

    }

    @Override
    public void getShareStatsDoesNotExist() {

    }
}
