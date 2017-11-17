/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerregistry.CheckNameAvailabilityResult;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for CheckNameAvailabilityResult.
 */
@LangDefinition
public class CheckNameAvailabilityResultImpl
    extends WrapperImpl<RegistryNameStatusInner>
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
