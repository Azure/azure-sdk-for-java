// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.resourcemanager.keyvault.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.keyvault.models.Reason;
import com.azure.resourcemanager.keyvault.fluent.models.CheckNameAvailabilityResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** The CheckNameAvailability operation response. */
public class CheckNameAvailabilityResultImpl extends WrapperImpl<CheckNameAvailabilityResultInner>
    implements CheckNameAvailabilityResult {

    protected CheckNameAvailabilityResultImpl(CheckNameAvailabilityResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public Boolean nameAvailable() {
        return innerModel().nameAvailable();
    }

    @Override
    public Reason reason() {
        return innerModel().reason();
    }

    @Override
    public String message() {
        return innerModel().message();
    }
}
