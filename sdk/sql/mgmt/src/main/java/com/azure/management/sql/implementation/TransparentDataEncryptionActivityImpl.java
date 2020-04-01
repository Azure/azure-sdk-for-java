/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;


import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.TransparentDataEncryptionActivity;
import com.azure.management.sql.TransparentDataEncryptionActivityStatus;
import com.azure.management.sql.models.TransparentDataEncryptionActivityInner;

/**
 * Implementation for TransparentDataEncryptionActivity.
 */
class TransparentDataEncryptionActivityImpl
        extends WrapperImpl<TransparentDataEncryptionActivityInner>
        implements TransparentDataEncryptionActivity {
    private final ResourceId resourceId;

    protected TransparentDataEncryptionActivityImpl(TransparentDataEncryptionActivityInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().getId());
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
