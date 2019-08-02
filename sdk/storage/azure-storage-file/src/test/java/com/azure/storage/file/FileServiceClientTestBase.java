// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.test.TestBase;
import com.azure.storage.file.models.ListSharesOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public abstract class FileServiceClientTestBase extends TestBase {
    String shareName;

    String reallyLongString = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties";

    @Rule
    public TestName testName = new TestName();

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

    String getShareName() {
        return testResourceNamer.randomName("share", 16).toLowerCase();
    }

    ListSharesOptions defaultOptions() {
        return new ListSharesOptions().prefix(shareName);
    }

    @Test
    public abstract void getShareDoesNotCreateAShare();

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
    public abstract void deleteThenCreateShareFromFileServiceClient();

    /**
     * Cannot re-create a share within 30 seconds of it being deleted.
     */
    @Test
    public abstract void deleteThenCreateShareTooSoonFromFileServiceClient();

    @Test
    public abstract void listShares();

    @Test
    public abstract void listSharesWithPrefix();

    @Test
    public abstract void listSharesWithLimit();

    @Test
    public abstract void listSharesInvalidMaxResults();

    @Test
    public abstract void listSharesIncludeMetadata();

    @Test
    public abstract void listSharesIncludeSnapshots();

    @Test
    public abstract void listSharesIncludeMetadataAndSnapshots();

    @Test
    public abstract void setFileServiceProperties();

    @Test
    public abstract void setPropertiesTooManyRules();

    /**
     * A header in AllowedHeaders is not allowed to exceed 256 characters.
     */
    @Test
    public abstract void setPropertiesInvalidAllowedHeader();

    /**
     * A header in ExposedHeaders is not allowed to exceed 256 characters.
     */
    @Test
    public abstract void setPropertiesInvalidExposedHeader();

    /**
     * An origin in AllowedOrigins is not allowed to exceed 256 characters.
     */
    @Test
    public abstract void setPropertiesInvalidAllowedOrigin();

    /**
     * AllowedMethods only supports DELETE, GET, HEAD, MERGE, POST, OPTIONS, and PUT.
     */
    @Test
    public abstract void setPropertiesInvalidAllowedMethod();
}
