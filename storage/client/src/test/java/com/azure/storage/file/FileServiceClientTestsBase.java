// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.storage.file.models.ListSharesOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.function.BiFunction;

import static org.junit.Assert.fail;

abstract class FileServiceClientTestsBase extends TestBase {
    private final ServiceLogger logger = new ServiceLogger(FileServiceClientTestsBase.class);
    private final String azureStorageConnectionString = "AZURE_STORAGE_CONNECTION_STRING";
    private final String azureStorageFileEndpoint = "AZURE_STORAGE_FILE_ENDPOINT";

    final String shareNamePrefix = "share";
    String shareName;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    <T> T setupClient(BiFunction<String, String, T> clientBuilder) {
        String connectionString = ConfigurationManager.getConfiguration().get(azureStorageConnectionString);
        String endpoint = ConfigurationManager.getConfiguration().get(azureStorageFileEndpoint);

        if (ImplUtils.isNullOrEmpty(connectionString) || ImplUtils.isNullOrEmpty(endpoint)) {
            logger.asWarning().log("{} and {} must be set to build the testing client", azureStorageConnectionString, azureStorageFileEndpoint);
            fail();
            return null;
        }

        return clientBuilder.apply(connectionString, endpoint);
    }

    String getShareName() {
        return testResourceNamer.randomName(shareNamePrefix, 16).toLowerCase();
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
    public abstract void deleteThenCreateShare();

    /**
     * Cannot re-create a share within 30 seconds of it being deleted.
     */
    @Test
    public abstract void deleteThenCreateShareTooSoon();

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
    public abstract void setProperties();

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
