// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

public final class FileServiceClientAsyncTests extends FileServiceClientTestsBase {
    private FileServiceAsyncClient client;

    @Override
    public void setupTest() {
        shareName = getShareName();
    }

    @Override
    public void teardownTest() {

    }

    @Override
    public void getShareDoesNotCreateAShare() {

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
    public void listShares() {

    }

    @Override
    public void listSharesInvalidMaxResults() {

    }

    @Override
    public void listSharesIncludeMetadata() {

    }

    @Override
    public void listSharesIncludeSnapshots() {

    }

    @Override
    public void listSharesIncludeMetadataAndSnapshots() {

    }

    @Override
    public void getProperties() {

    }

    @Override
    public void setProperties() {

    }

    @Override
    public void setPropertiesTooManyRules() {

    }

    @Override
    public void setPropertiesInvalidAllowedHeader() {

    }

    @Override
    public void setPropertiesInvalidExposedHeader() {

    }

    @Override
    public void setPropertiesInvalidAllowedOrigin() {

    }

    @Override
    public void setPropertiesInvalidAllowedMethod() {

    }
}
