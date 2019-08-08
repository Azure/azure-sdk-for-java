// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.test.TestBase;
import com.azure.core.util.configuration.ConfigurationManager;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

public abstract class DirectoryClientTestBase extends TestBase {
    protected String dirName;
    protected String shareName;

    String azureStorageFileEndpoint = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");
    final Map<String, String> basicMetadata = Collections.singletonMap("test", "metadata");
    final Map<String, String> invalidMetadata = Collections.singletonMap("1", "metadata");

    @Rule
    public TestName testName = new TestName();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    void beforeDirectoryTest() {
        dirName = testResourceNamer.randomName("directory", 16);
    }

    /**
     * Gets the name of the current test being run.
     * <p>
     * NOTE: This could not be implemented in the base class using {@link TestName} because it always returns {@code
     * null}. See https://stackoverflow.com/a/16113631/4220757.
     *
     * @return The name of the current test.
     */
    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Test
    public abstract void urlFromDirClient();

    @Test
    public abstract void getFileClientFromDirClient();

    @Test
    public abstract void getSubDirectoryClient();

    @Test
    public abstract void createMinFromDirClient();

    @Test
    public abstract void createTwiceFromDirClient();

    @Test
    public abstract void createWithMetadataFromDirClient();

    @Test
    public abstract void deleteFromDirClient();

    @Test
    public abstract void deleteNotExistFromDirClient();

    @Test
    public abstract void getPropertiesFromDirClient();

    @Test
    public abstract void clearMetadataFromDirClient();

    @Test
    public abstract void setMetadataFromDirClient();

    @Test
    public abstract void setMetadataInvalidKeyFromDirClient();

    @Test
    public abstract void listFilesAndDirectoriesFromDirClient();

    @Test
    public abstract void getHandlesFromDirClient();

    @Test
    public abstract void forceCloseHandlesFromDirClient();

    @Test
    public abstract void createSubDirectory();

    @Test
    public abstract void createSubDirectoryWithMetadata();

    @Test
    public abstract void createSubDirectoryTwiceSameMetadata();

    @Test
    public abstract void deleteSubDirectory();

    @Test
    public abstract void createFileFromDirClient();

    @Test
    public abstract void createFileWithoutCreateDirFromDirClient();

    @Test
    public abstract void deleteFileFromDirClient();

    @Test
    public abstract void deleteFileWithoutCreateFileFromDirClient();
}
