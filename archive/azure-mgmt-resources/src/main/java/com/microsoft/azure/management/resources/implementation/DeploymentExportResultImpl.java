/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.DeploymentExportResult;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for {@link DeploymentExportResult}.
 */
final class DeploymentExportResultImpl extends
        WrapperImpl<DeploymentExportResultInner>
        implements
        DeploymentExportResult {

    private ObjectMapper mapper;

    DeploymentExportResultImpl(DeploymentExportResultInner innerModel) {
        super(innerModel);
        mapper = new ObjectMapper();
    }

    @Override
    public Object template() {
        return inner().template();
    }

    @Override
    public String templateAsJson() {
        try {
            return mapper.writeValueAsString(template());
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
