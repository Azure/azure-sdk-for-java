// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.RestorePoint;
import com.azure.resourcemanager.sql.models.RestorePointType;
import com.azure.resourcemanager.sql.fluent.inner.RestorePointInner;
import java.time.OffsetDateTime;

/** Implementation for Restore point interface. */
class RestorePointImpl extends WrapperImpl<RestorePointInner> implements RestorePoint {
    private final ResourceId resourceId;
    private final String sqlServerName;
    private final String resourceGroupName;

    protected RestorePointImpl(String resourceGroupName, String sqlServerName, RestorePointInner innerObject) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.resourceId = ResourceId.fromString(this.inner().id());
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
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String databaseName() {
        return resourceId.parent().name();
    }

    @Override
    public String databaseId() {
        return resourceId.parent().id();
    }

    @Override
    public RestorePointType restorePointType() {
        return this.inner().restorePointType();
    }

    @Override
    public OffsetDateTime restorePointCreationDate() {
        return this.inner().restorePointCreationDate();
    }

    @Override
    public OffsetDateTime earliestRestoreDate() {
        return this.inner().earliestRestoreDate();
    }
}
