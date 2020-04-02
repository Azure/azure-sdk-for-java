// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage.implementation;


import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.storage.LegalHold;
import com.azure.management.storage.models.LegalHoldInner;

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
