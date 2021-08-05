// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.resourcemanager.containerregistry.models.EncodedTaskRunRequest;
import com.azure.resourcemanager.containerregistry.models.OverridingValue;
import com.azure.resourcemanager.containerregistry.models.PlatformProperties;
import com.azure.resourcemanager.containerregistry.models.RegistryEncodedTaskRunRequest;
import com.azure.resourcemanager.containerregistry.models.SetValue;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RegistryEncodedTaskRunRequestImpl
    implements RegistryEncodedTaskRunRequest,
        RegistryEncodedTaskRunRequest.Definition,
        HasInnerModel<EncodedTaskRunRequest> {

    private EncodedTaskRunRequest inner;
    private RegistryTaskRunImpl registryTaskRunImpl;

    @Override
    public int timeout() {
        return ResourceManagerUtils.toPrimitiveInt(this.inner.timeout());
    }

    @Override
    public PlatformProperties platform() {
        return this.inner.platform();
    }

    @Override
    public int cpuCount() {
        if (this.inner.agentConfiguration() == null) {
            return 0;
        }
        return ResourceManagerUtils.toPrimitiveInt(this.inner.agentConfiguration().cpu());
    }

    @Override
    public String sourceLocation() {
        return this.inner.sourceLocation();
    }

    @Override
    public boolean isArchiveEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.inner.isArchiveEnabled());
    }

    RegistryEncodedTaskRunRequestImpl(RegistryTaskRunImpl registryTaskRunImpl) {
        this.inner = new EncodedTaskRunRequest();
        this.registryTaskRunImpl = registryTaskRunImpl;
    }

    @Override
    public RegistryEncodedTaskRunRequestImpl defineEncodedTaskStep() {
        return this;
    }

    @Override
    public RegistryEncodedTaskRunRequestImpl withBase64EncodedTaskContent(String encodedTaskContent) {
        this.inner.withEncodedTaskContent(encodedTaskContent);
        return this;
    }

    @Override
    public RegistryEncodedTaskRunRequestImpl withBase64EncodedValueContent(String encodedValueContent) {
        this.inner.withEncodedValuesContent(encodedValueContent);
        return this;
    }

    @Override
    public RegistryEncodedTaskRunRequestImpl withOverridingValues(Map<String, OverridingValue> overridingValues) {
        if (overridingValues.size() == 0) {
            return this;
        }
        List<SetValue> overridingValuesList = new ArrayList<SetValue>();
        for (Map.Entry<String, OverridingValue> entry : overridingValues.entrySet()) {
            SetValue value = new SetValue();
            value.withName(entry.getKey());
            value.withValue(entry.getValue().value());
            value.withIsSecret(entry.getValue().isSecret());
            overridingValuesList.add(value);
        }
        this.inner.withValues(overridingValuesList);
        return this;
    }

    @Override
    public RegistryEncodedTaskRunRequestImpl withOverridingValue(String name, OverridingValue overridingValue) {
        if (this.inner.values() == null) {
            this.inner.withValues(new ArrayList<SetValue>());
        }
        SetValue value = new SetValue();
        value.withName(name);
        value.withValue(overridingValue.value());
        value.withIsSecret(overridingValue.isSecret());
        this.inner.values().add(value);
        return this;
    }

    @Override
    public RegistryTaskRunImpl attach() {
        this.registryTaskRunImpl.withEncodedTaskRunRequest(this.inner);
        return this.registryTaskRunImpl;
    }

    @Override
    public EncodedTaskRunRequest innerModel() {
        return this.inner;
    }
}
