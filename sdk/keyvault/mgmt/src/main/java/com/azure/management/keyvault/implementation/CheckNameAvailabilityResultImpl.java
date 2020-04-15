// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.keyvault.implementation;

import com.azure.management.keyvault.CheckNameAvailabilityResult;
import com.azure.management.keyvault.Reason;
import com.azure.management.keyvault.models.CheckNameAvailabilityResultInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/** The CheckNameAvailability operation response. */
public class CheckNameAvailabilityResultImpl extends WrapperImpl<CheckNameAvailabilityResultInner>
    implements CheckNameAvailabilityResult {

    protected CheckNameAvailabilityResultImpl(CheckNameAvailabilityResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public Boolean nameAvailable() {
        return inner().nameAvailable();
    }

    @Override
    public Reason reason() {
        return inner().reason();
    }

    @Override
    public String message() {
        return inner().message();
    }
}
