/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.RestorePoint;
import com.azure.management.sql.RestorePointType;
import com.azure.management.sql.models.RestorePointInner;

import java.time.OffsetDateTime;


/**
 * Implementation for Restore point interface.
 */
class RestorePointImpl
        extends WrapperImpl<RestorePointInner>
        implements RestorePoint {
    private final ResourceId resourceId;
    private final String sqlServerName;
    private final String resourceGroupName;

    protected RestorePointImpl(String resourceGroupName, String sqlServerName, RestorePointInner innerObject) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.resourceId = ResourceId.fromString(this.inner().getId());
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String id() {
        return this.inner().getId();
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
