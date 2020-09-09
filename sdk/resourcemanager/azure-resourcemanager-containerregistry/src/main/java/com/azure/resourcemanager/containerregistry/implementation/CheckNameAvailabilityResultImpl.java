// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.resourcemanager.containerregistry.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.containerregistry.fluent.inner.RegistryNameStatusInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** Implementation for CheckNameAvailabilityResult. */
public class CheckNameAvailabilityResultImpl extends WrapperImpl<RegistryNameStatusInner>
    implements CheckNameAvailabilityResult {
    /**
     * Creates an instance of the check name availability result object.
     *
     * @param inner the inner object
     */
    CheckNameAvailabilityResultImpl(RegistryNameStatusInner inner) {
        super(inner);
    }

    @Override
    public boolean isAvailable() {
        return inner().nameAvailable();
    }

    @Override
    public String unavailabilityReason() {
        return inner().reason();
    }

    @Override
    public String unavailabilityMessage() {
        return inner().message();
    }
}
