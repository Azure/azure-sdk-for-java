// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.resourcemanager.keyvault.models.DeletedVault;
import com.azure.resourcemanager.keyvault.fluent.models.DeletedVaultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.time.OffsetDateTime;
import java.util.Map;

/** Deleted vault information with extended details. */
public class DeletedVaultImpl extends WrapperImpl<DeletedVaultInner> implements DeletedVault {

    DeletedVaultImpl(DeletedVaultInner inner) {
        super(inner);
    }

    @Override
    public String name() {
        return innerModel().name();
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public String location() {
        return innerModel().properties().location();
    }

    @Override
    public OffsetDateTime deletionDate() {
        return innerModel().properties().deletionDate();
    }

    @Override
    public OffsetDateTime scheduledPurgeDate() {
        return innerModel().properties().scheduledPurgeDate();
    }

    @Override
    public Map<String, String> tags() {
        return innerModel().properties().tags();
    }
}
