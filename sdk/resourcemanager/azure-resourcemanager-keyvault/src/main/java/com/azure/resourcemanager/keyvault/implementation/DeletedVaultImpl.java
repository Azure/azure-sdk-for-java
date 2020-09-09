// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.resourcemanager.keyvault.models.DeletedVault;
import com.azure.resourcemanager.keyvault.fluent.inner.DeletedVaultInner;
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
        return inner().name();
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public String location() {
        return inner().properties().location();
    }

    @Override
    public OffsetDateTime deletionDate() {
        return inner().properties().deletionDate();
    }

    @Override
    public OffsetDateTime scheduledPurgeDate() {
        return inner().properties().scheduledPurgeDate();
    }

    @Override
    public Map<String, String> tags() {
        return inner().properties().tags();
    }
}
