// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluent.models.DeploymentExportResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.models.DeploymentExportResult;

import java.io.IOException;

/**
 * Implementation for {@link DeploymentExportResult}.
 */
final class DeploymentExportResultImpl extends
        WrapperImpl<DeploymentExportResultInner>
        implements
        DeploymentExportResult {

    private SerializerAdapter serializerAdapter;

    DeploymentExportResultImpl(DeploymentExportResultInner innerModel) {
        super(innerModel);
        serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
    }

    @Override
    public Object template() {
        return innerModel().template();
    }

    @Override
    public String templateAsJson() {
        try {
            return serializerAdapter.serialize(template(), SerializerEncoding.JSON);
        } catch (IOException e) {
            return null;
        }
    }
}
