// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public abstract class ShareClientTestsBase extends TestBase {
    String shareName;
    TestHelpers helper;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    String getShareName() {
        return testResourceNamer.randomName("share", 16).toLowerCase();
    }

    @Test
    public abstract void getRootDirectoryDoesNotCreateADirectory();

    @Test
    public abstract void getDirectoryDoesNotCreateADirectory();

    @Test
    public abstract void createDirectory();

    @Test
    public abstract void createDirectoryOnSnapshotIsNotAllowed();

    @Test
    public abstract void createDirectoryInvalidName();

    @Test
    public abstract void createShare();

    @Test
    public abstract void createShareTwiceSameMetadata();

    @Test
    public abstract void createShareTwiceDifferentMetadata();

    @Test
    public abstract void createShareInvalidQuota();

    @Test
    public abstract void deleteShare();

    @Test
    public abstract void deleteShareDoesNotExist();

    @Test
    public abstract void deleteThenCreateShare();

    /**
     * Cannot re-create a share within 30 seconds of it being deleted.
     */
    @Test
    public abstract void deleteThenCreateShareTooSoon();

    @Test
    public abstract void snapshotShare();

    @Test
    public abstract void deleteShareSnapshot();

    @Test
    public abstract void snapshotShareSameMetadata();

    @Test
    public abstract void snapshotShareDifferentMetadata();

    @Test
    public abstract void snapshotShareDoesNotExist();

    @Test
    public abstract void getShareProperties();

    @Test
    public abstract void getSnapshotShareProperties();

    @Test
    public abstract void getSharePropertiesDoesNotExist();

    @Test
    public abstract void getSnapshotSharePropertiesDoesNotExist();

    @Test
    public abstract void setShareProperties();

    @Test
    public abstract void setSnapshotSharePropertiesIsNotAllowed();

    @Test
    public abstract void setSharePropertiesInvalidQuota();

    @Test
    public abstract void setSharePropertiesDoesNotExist();

    @Test
    public abstract void getShareMetadata();

    @Test
    public abstract void getSnapshotShareMetadata();

    @Test
    public abstract void getShareMetadataDoesNotExist();

    @Test
    public abstract void getSnapshotShareMetadataDoesNotExist();

    @Test
    public abstract void setShareMetadata();

    @Test
    public abstract void setSnapshotShareMetadataIsNotAllowed();

    @Test
    public abstract void setShareMetadataInvalidMetadata();

    @Test
    public abstract void setShareMetadataDoesNotExist();

    @Test
    public abstract void getSharePolicies();

    @Test
    public abstract void getSnapshotSharePoliciesIsNotAllowed();

    @Test
    public abstract void getSharePoliciesDoesNotExist();

    @Test
    public abstract void setSharePolicies();

    @Test
    public abstract void setSnapshotSharePoliciesIsNotAllowed();

    @Test
    public abstract void setSharePoliciesInvalidPermission();

    @Test
    public abstract void setSharePoliciesTooManyPermissions();

    @Test
    public abstract void setSharePoliciesDoesNotExist();

    @Test
    public abstract void getShareStats();

    @Test
    public abstract void getSnapshotShareStatsIsNotAllowed();

    @Test
    public abstract void getShareStatsDoesNotExist();
}
