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
    public abstract void createDirectoryAlreadyExists();

    @Test
    public abstract void deleteDirectory();

    @Test
    public abstract void deleteDirectoryDoesNotExist();

    @Test
    public abstract void create();

    @Test
    public abstract void createTwiceSameMetadata();

    @Test
    public abstract void createTwiceDifferentMetadata();

    @Test
    public abstract void createInvalidQuota();

    @Test
    public abstract void delete();

    @Test
    public abstract void deleteDoesNotExist();

    @Test
    public abstract void deleteThenCreate();

    /**
     * Cannot re-create a share within 30 seconds of it being deleted.
     */
    @Test
    public abstract void deleteThenCreateTooSoon();

    @Test
    public abstract void snapshot();

    @Test
    public abstract void deleteSnapshot();

    @Test
    public abstract void snapshotSameMetadata();

    @Test
    public abstract void snapshotDifferentMetadata();

    @Test
    public abstract void snapshotDoesNotExist();

    @Test
    public abstract void getProperties();

    @Test
    public abstract void getSnapshotProperties();

    @Test
    public abstract void getPropertiesDoesNotExist();

    @Test
    public abstract void getSnapshotPropertiesDoesNotExist();

    @Test
    public abstract void setProperties();

    @Test
    public abstract void setPropertiesInvalidQuota();

    @Test
    public abstract void setPropertiesDoesNotExist();

    @Test
    public abstract void getMetadata();

    @Test
    public abstract void getSnapshotMetadata();

    @Test
    public abstract void getMetadataDoesNotExist();

    @Test
    public abstract void getSnapshotMetadataDoesNotExist();

    @Test
    public abstract void setMetadata();

    @Test
    public abstract void setMetadataInvalidMetadata();

    @Test
    public abstract void setMetadataDoesNotExist();

    @Test
    public abstract void getPolicies();

    @Test
    public abstract void getPoliciesDoesNotExist();

    @Test
    public abstract void setPolicies();

    @Test
    public abstract void setPoliciesInvalidPermission();

    @Test
    public abstract void setPoliciesTooManyPermissions();

    @Test
    public abstract void setPoliciesDoesNotExist();

    @Test
    public abstract void getStats();

    @Test
    public abstract void getStatsDoesNotExist();
}
