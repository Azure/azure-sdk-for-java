// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluent.models.ResourceGroupExportResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.models.DeploymentExportResult;
import com.azure.resourcemanager.resources.models.ResourceGroupExportResult;

import java.io.IOException;

/**
 * Implementation for {@link DeploymentExportResult}.
 */
final class ResourceGroupExportResultImpl extends
        WrapperImpl<ResourceGroupExportResultInner>
        implements
        ResourceGroupExportResult {

    private final SerializerAdapter serializerAdapter;

    ResourceGroupExportResultImpl(ResourceGroupExportResultInner innerModel) {
        super(innerModel);
        this.serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
    }

    @Override
    public Object template() {
        return innerModel().template();
    }

    @Override
    public String templateJson() {
        try {
            return serializerAdapter.serialize(template(), SerializerEncoding.JSON);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public ManagementError error() {
        return innerModel().error();
    }
}
