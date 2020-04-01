/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.EncodedTaskRunRequest;
import com.azure.management.containerregistry.OverridingValue;
import com.azure.management.containerregistry.PlatformProperties;
import com.azure.management.containerregistry.RegistryEncodedTaskRunRequest;
import com.azure.management.containerregistry.SetValue;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RegistryEncodedTaskRunRequestImpl implements
        RegistryEncodedTaskRunRequest,
        RegistryEncodedTaskRunRequest.Definition,
        HasInner<EncodedTaskRunRequest> {

    private EncodedTaskRunRequest inner;
    private RegistryTaskRunImpl registryTaskRunImpl;


    @Override
    public int timeout() {
        return Utils.toPrimitiveInt(this.inner.timeout());
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
        return Utils.toPrimitiveInt(this.inner.agentConfiguration().cpu());
    }

    @Override
    public String sourceLocation() {
        return this.inner.sourceLocation();
    }

    @Override
    public boolean isArchiveEnabled() {
        return Utils.toPrimitiveBoolean(this.inner.isArchiveEnabled());
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
    public EncodedTaskRunRequest inner() {
        return this.inner;
    }
}
