// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.SqlDatabaseImportExportResponse;
import com.azure.resourcemanager.sql.fluent.inner.ImportExportResponseInner;
import java.util.UUID;

/** Implementation for SqlDatabaseImportExportResponse. */
public class SqlDatabaseImportExportResponseImpl extends WrapperImpl<ImportExportResponseInner>
    implements SqlDatabaseImportExportResponse {

    private String key;

    protected SqlDatabaseImportExportResponseImpl(ImportExportResponseInner innerObject) {
        super(innerObject);
        this.key = UUID.randomUUID().toString();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String requestType() {
        return this.inner().requestType();
    }

    @Override
    public String requestId() {
        return this.inner().requestId().toString();
    }

    @Override
    public String serverName() {
        return this.inner().serverName();
    }

    @Override
    public String databaseName() {
        return this.inner().databaseName();
    }

    @Override
    public String status() {
        return this.inner().status();
    }

    @Override
    public String lastModifiedTime() {
        return this.inner().lastModifiedTime();
    }

    @Override
    public String queuedTime() {
        return this.inner().queuedTime();
    }

    @Override
    public String blobUri() {
        return this.inner().blobUri();
    }

    @Override
    public String errorMessage() {
        return this.inner().errorMessage();
    }

    @Override
    public String key() {
        return this.key;
    }
}
