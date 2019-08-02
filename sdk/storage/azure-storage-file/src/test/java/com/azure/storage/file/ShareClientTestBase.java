// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

public abstract class ShareClientTestBase extends TestBase {
    String shareName;

    @Rule
    public TestName testName = new TestName();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Gets the name of the current test being run.
     * <p>
     * NOTE: This could not be implemented in the base class using {@link TestName} because it always returns {@code
     * null}. See https://stackoverflow.com/a/16113631/4220757.
     *
     * @return The name of the current test.
     */

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
    public abstract void getFileClientDoesNotCreateAFile();

    @Test
    public abstract void createDirectoryFromShareClient();

    @Test
    public abstract void createDirectoryInvalidNameFromShareClient();

    @Test
    public abstract void createDirectoryAlreadyExistsFromShareClient();

    @Test
    public abstract void deleteDirectoryFromShareClient();

    @Test
    public abstract void deleteDirectoryDoesNotExistFromShareClient();

    @Test
    public abstract void createFileFromShareClient();

    @Test
    public abstract void createFileInvalidNameFromShareClient();

    @Test
    public abstract void createFileAlreadyExistsFromShareClient();

    @Test
    public abstract void deleteFileFromShareClient();

    @Test
    public abstract void deleteFileDoesNotExistFromShareClient();

    @Test
    public abstract void createFromShareClient();

    @Test
    public abstract void createTwiceSameMetadataFromShareClient();

    @Test
    public abstract void createTwiceDifferentMetadataFromShareClient();

    @Test
    public abstract void createInvalidQuotaFromShareClient();

    @Test
    public abstract void deleteFromShareClient();

    @Test
    public abstract void deleteDoesNotExistFromShareClient();

    @Test
    public abstract void deleteThenCreateFromShareClient();

    /**
     * Cannot re-create a share within 30 seconds of it being deleted.
     */
    @Test
    public abstract void deleteThenCreateTooSoonFromShareClient();

    @Test
    public abstract void snapshot();

    @Test
    public abstract void deleteSnapshotFromShareClient();

    @Test
    public abstract void snapshotSameMetadata();

    @Test
    public abstract void snapshotDifferentMetadata();

    @Test
    public abstract void snapshotDoesNotExist();

    @Test
    public abstract void getPropertiesFromShareClient();

    @Test
    public abstract void getSnapshotPropertiesFromShareClient();

    @Test
    public abstract void getPropertiesDoesNotExistFromShareClient();

    @Test
    public abstract void getSnapshotPropertiesDoesNotExist();

    @Test
    public abstract void setPropertiesFromShareClient();

    @Test
    public abstract void setPropertiesInvalidQuotaFromShareClient();

    @Test
    public abstract void setPropertiesDoesNotExistFromShareClient();

    @Test
    public abstract void getMetadataFromShareClient();

    @Test
    public abstract void getSnapshotMetadataFromShareClient();

    @Test
    public abstract void getMetadataDoesNotExistFromShareClient();

    @Test
    public abstract void getSnapshotMetadataDoesNotExistFromShareClient();

    @Test
    public abstract void setMetadataFromShareClient();

    @Test
    public abstract void setMetadataInvalidMetadataFromShareClient();

    @Test
    public abstract void setMetadataDoesNotExistFromShareClient();

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

    @Test
    public abstract void getSnapshotId();
}
