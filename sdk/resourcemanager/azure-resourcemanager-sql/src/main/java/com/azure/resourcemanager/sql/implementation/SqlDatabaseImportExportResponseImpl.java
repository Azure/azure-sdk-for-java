// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.SqlDatabaseImportExportResponse;
import com.azure.resourcemanager.sql.fluent.models.ImportExportResponseInner;
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
        return this.innerModel().name();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String requestType() {
        return this.innerModel().requestType();
    }

    @Override
    public String requestId() {
        return this.innerModel().requestId().toString();
    }

    @Override
    public String serverName() {
        return this.innerModel().serverName();
    }

    @Override
    public String databaseName() {
        return this.innerModel().databaseName();
    }

    @Override
    public String status() {
        return this.innerModel().status();
    }

    @Override
    public String lastModifiedTime() {
        return this.innerModel().lastModifiedTime();
    }

    @Override
    public String queuedTime() {
        return this.innerModel().queuedTime();
    }

    @Override
    public String blobUri() {
        return this.innerModel().blobUri();
    }

    @Override
    public String errorMessage() {
        return this.innerModel().errorMessage();
    }

    @Override
    public String key() {
        return this.key;
    }
}
