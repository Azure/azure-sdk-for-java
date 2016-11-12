/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.TransparentDataEncryption;
import com.microsoft.azure.management.sql.TransparentDataEncryptionActivity;
import com.microsoft.azure.management.sql.TransparentDataEncryptionStates;

import java.util.List;

/**
 * Implementation for TransparentDataEncryption.
 */
@LangDefinition
class TransparentDataEncryptionImpl
        extends WrapperImpl<TransparentDataEncryptionInner>
        implements TransparentDataEncryption {
    private final ResourceId resourceId;
    private final DatabasesInner databasesInner;

    protected TransparentDataEncryptionImpl(TransparentDataEncryptionInner innerObject, DatabasesInner databasesInner) {
        super(innerObject);
        this.resourceId = ResourceId.parseResourceId(this.inner().id());
        this.databasesInner = databasesInner;
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
    public TransparentDataEncryptionStates status() {
        return this.inner().status();
    }

    @Override
    public TransparentDataEncryption updateState(TransparentDataEncryptionStates transparentDataEncryptionState) {
        this.inner().withStatus(transparentDataEncryptionState);
        this.setInner(this.databasesInner.createOrUpdateTransparentDataEncryptionConfiguration(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.databaseName(),
                this.inner()));

        return this;
    }

    @Override
    public List<TransparentDataEncryptionActivity> listActivity() {
        PagedListConverter<TransparentDataEncryptionActivityInner, TransparentDataEncryptionActivity> converter
                = new PagedListConverter<TransparentDataEncryptionActivityInner, TransparentDataEncryptionActivity>() {
            @Override
            public TransparentDataEncryptionActivity typeConvert(TransparentDataEncryptionActivityInner transparentDataEncryptionActivityInner) {

                return new TransparentDataEncryptionActivityImpl(transparentDataEncryptionActivityInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.databasesInner.listTransparentDataEncryptionActivity(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.databaseName())));
    }
}
