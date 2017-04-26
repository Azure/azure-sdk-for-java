/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.RestorePoint;
import com.microsoft.azure.management.sql.RestorePointTypes;
import org.joda.time.DateTime;

/**
 * Implementation for Restore point interface.
 */
@LangDefinition
class RestorePointImpl
        extends WrapperImpl<RestorePointInner>
        implements RestorePoint {
    private final ResourceId resourceId;

    protected RestorePointImpl(RestorePointInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().id());
    }

    @Override
    public String name() {
        return this.resourceId.name();
    }

    @Override
    public String id() {
        return this.resourceId.id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceId.resourceGroupName();
    }

    @Override
    public String sqlServerName() {
        return resourceId.parent().parent().name();
    }

    @Override
    public String databaseName() {
        return resourceId.parent().name();
    }

    @Override
    public RestorePointTypes restorePointType() {
        return this.inner().restorePointType();
    }

    @Override
    public DateTime restorePointCreationDate() {
        return this.inner().restorePointCreationDate();
    }

    @Override
    public DateTime earliestRestoreDate() {
        return this.inner().earliestRestoreDate();
    }
}
