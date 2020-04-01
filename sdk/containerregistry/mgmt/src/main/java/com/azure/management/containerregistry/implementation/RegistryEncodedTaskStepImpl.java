/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.EncodedTaskStep;
import com.azure.management.containerregistry.EncodedTaskStepUpdateParameters;
import com.azure.management.containerregistry.OverridingValue;
import com.azure.management.containerregistry.RegistryEncodedTaskStep;
import com.azure.management.containerregistry.RegistryTask;
import com.azure.management.containerregistry.SetValue;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class RegistryEncodedTaskStepImpl
        extends RegistryTaskStepImpl
        implements
        RegistryEncodedTaskStep,
        RegistryEncodedTaskStep.Definition,
        RegistryEncodedTaskStep.Update,
        HasInner<EncodedTaskStep> {

    private EncodedTaskStep inner;
    private EncodedTaskStepUpdateParameters encodedTaskStepUpdateParameters;
    private RegistryTaskImpl taskImpl;

    RegistryEncodedTaskStepImpl(RegistryTaskImpl taskImpl) {
        super(taskImpl.inner().step());
        this.inner = new EncodedTaskStep();
        if (taskImpl.inner().step() != null && !(taskImpl.inner().step() instanceof EncodedTaskStep)) {
            throw new IllegalArgumentException("Constructor for RegistryEncodedTaskStepImpl invoked for class that is not an EncodedTaskStep");
        }
        this.taskImpl = taskImpl;
        this.encodedTaskStepUpdateParameters = new EncodedTaskStepUpdateParameters();
    }

    @Override
    public String encodedTaskContent() {
        EncodedTaskStep encodedTaskStep = (EncodedTaskStep) this.taskImpl.inner().step();
        return encodedTaskStep.encodedTaskContent();
    }

    @Override
    public String encodedValuesContent() {
        EncodedTaskStep encodedTaskStep = (EncodedTaskStep) this.taskImpl.inner().step();
        return encodedTaskStep.encodedValuesContent();
    }

    @Override
    public List<SetValue> values() {
        EncodedTaskStep encodedTaskStep = (EncodedTaskStep) this.taskImpl.inner().step();
        if (encodedTaskStep.values() == null) {
            return Collections.unmodifiableList(new ArrayList<SetValue>());
        }
        return Collections.unmodifiableList(encodedTaskStep.values());
    }

    @Override
    public RegistryEncodedTaskStepImpl withBase64EncodedTaskContent(String encodedTaskContent) {
        if (isInCreateMode()) {
            this.inner.withEncodedTaskContent(encodedTaskContent);
        } else {
            this.encodedTaskStepUpdateParameters.withEncodedTaskContent(encodedTaskContent);
        }
        return this;
    }

    @Override
    public RegistryEncodedTaskStepImpl withBase64EncodedValueContent(String encodedValueContent) {
        if (isInCreateMode()) {
            this.inner.withEncodedValuesContent(encodedValueContent);
        } else {
            this.encodedTaskStepUpdateParameters.withEncodedValuesContent(encodedValueContent);
        }
        return this;
    }

    @Override
    public RegistryEncodedTaskStepImpl withOverridingValues(Map<String, OverridingValue> overridingValues) {
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
        if (isInCreateMode()) {
            this.inner.withValues(overridingValuesList);
        } else {
            this.encodedTaskStepUpdateParameters.withValues(overridingValuesList);
        }
        return this;
    }

    @Override
    public RegistryEncodedTaskStepImpl withOverridingValue(String name, OverridingValue overridingValue) {
        if (this.inner.values() == null) {
            this.inner.withValues(new ArrayList<SetValue>());
        }
        SetValue value = new SetValue();
        value.withName(name);
        value.withValue(overridingValue.value());
        value.withIsSecret(overridingValue.isSecret());
        if (isInCreateMode()) {
            this.inner.values().add(value);
        } else {
            this.encodedTaskStepUpdateParameters.values().add(value);
        }
        return this;
    }

    @Override
    public RegistryTask.DefinitionStages.SourceTriggerDefinition attach() {
        this.taskImpl.withEncodedTaskStepCreateParameters(this.inner);
        return this.taskImpl;
    }

    @Override
    public RegistryTask.Update parent() {
        this.taskImpl.withEncodedTaskStepUpdateParameters(this.encodedTaskStepUpdateParameters);
        return this.taskImpl;
    }

    @Override
    public EncodedTaskStep inner() {
        return this.inner;
    }

    private boolean isInCreateMode() {
        if (this.taskImpl.inner().getId() == null) {
            return true;
        }
        return false;
    }
}
