// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionActivity;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionActivityStatus;
import com.azure.resourcemanager.sql.fluent.inner.TransparentDataEncryptionActivityInner;

/** Implementation for TransparentDataEncryptionActivity. */
class TransparentDataEncryptionActivityImpl extends WrapperImpl<TransparentDataEncryptionActivityInner>
    implements TransparentDataEncryptionActivity {
    private final ResourceId resourceId;

    protected TransparentDataEncryptionActivityImpl(TransparentDataEncryptionActivityInner innerObject) {
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
    public TransparentDataEncryptionActivityStatus status() {
        return this.inner().status();
    }

    @Override
    public double percentComplete() {
        return this.inner().percentComplete();
    }
}
