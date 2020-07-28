// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.LegalHold;
import com.azure.resourcemanager.storage.fluent.inner.LegalHoldInner;
import java.util.List;

class LegalHoldImpl extends WrapperImpl<LegalHoldInner> implements LegalHold {
    private final StorageManager manager;

    LegalHoldImpl(LegalHoldInner inner, StorageManager manager) {
        super(inner);
        this.manager = manager;
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public Boolean hasLegalHold() {
        return this.inner().hasLegalHold();
    }

    @Override
    public List<String> tags() {
        return this.inner().tags();
    }
}
