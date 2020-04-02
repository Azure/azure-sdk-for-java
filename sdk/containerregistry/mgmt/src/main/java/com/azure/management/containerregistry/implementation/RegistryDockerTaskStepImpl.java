/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.Argument;
import com.azure.management.containerregistry.DockerBuildStepUpdateParameters;
import com.azure.management.containerregistry.DockerTaskStep;
import com.azure.management.containerregistry.OverridingArgument;
import com.azure.management.containerregistry.RegistryDockerTaskStep;
import com.azure.management.containerregistry.RegistryTask;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class RegistryDockerTaskStepImpl
        extends RegistryTaskStepImpl
        implements
        RegistryDockerTaskStep,
        RegistryDockerTaskStep.Definition,
        RegistryDockerTaskStep.Update,
        HasInner<DockerTaskStep> {

    private DockerTaskStep inner;
    private DockerBuildStepUpdateParameters dockerTaskStepUpdateParameters;
    private RegistryTaskImpl taskImpl;

    RegistryDockerTaskStepImpl(RegistryTaskImpl taskImpl) {
        super(taskImpl.inner().step());
        this.inner = new DockerTaskStep();
        if (taskImpl.inner().step() != null && !(taskImpl.inner().step() instanceof DockerTaskStep)) {
            throw new IllegalArgumentException("Constructor for RegistryDockerTaskStepImpl invoked for class that is not DockerTaskStep");
        }
        this.taskImpl = taskImpl;
        this.dockerTaskStepUpdateParameters = new DockerBuildStepUpdateParameters();
    }

    @Override
    public List<String> imageNames() {
        DockerTaskStep dockerTaskStep = (DockerTaskStep) this.taskImpl.inner().step();
        if (dockerTaskStep.imageNames() == null) {
            return Collections.unmodifiableList(new ArrayList<String>());
        }
        return Collections.unmodifiableList(dockerTaskStep.imageNames());
    }

    @Override
    public boolean isPushEnabled() {
        DockerTaskStep dockerTaskStep = (DockerTaskStep) this.taskImpl.inner().step();
        return Utils.toPrimitiveBoolean(dockerTaskStep.isPushEnabled());
    }

    @Override
    public boolean noCache() {
        DockerTaskStep dockerTaskStep = (DockerTaskStep) this.taskImpl.inner().step();
        return Utils.toPrimitiveBoolean(dockerTaskStep.noCache());
    }

    @Override
    public String dockerFilePath() {
        DockerTaskStep dockerTaskStep = (DockerTaskStep) this.taskImpl.inner().step();
        return dockerTaskStep.dockerFilePath();
    }

    @Override
    public List<Argument> arguments() {
        DockerTaskStep dockerTaskStep = (DockerTaskStep) this.taskImpl.inner().step();
        if (dockerTaskStep.arguments() == null) {
            return Collections.unmodifiableList(new ArrayList<Argument>());
        }
        return Collections.unmodifiableList(dockerTaskStep.arguments());
    }

    @Override
    public RegistryDockerTaskStepImpl withDockerFilePath(String path) {
        if (isInCreateMode()) {
            this.inner.withDockerFilePath(path);
        } else {
            this.dockerTaskStepUpdateParameters.withDockerFilePath(path);
        }
        return this;
    }

    @Override
    public RegistryDockerTaskStepImpl withImageNames(List<String> imageNames) {
        if (isInCreateMode()) {
            this.inner.withImageNames(imageNames);
        } else {
            this.dockerTaskStepUpdateParameters.withImageNames(imageNames);
        }
        return this;
    }

    @Override
    public RegistryDockerTaskStepImpl withPushEnabled(boolean enabled) {
        if (isInCreateMode()) {
            this.inner.withIsPushEnabled(enabled);
        } else {
            this.dockerTaskStepUpdateParameters.withIsPushEnabled(enabled);
        }
        return this;
    }


    @Override
    public RegistryDockerTaskStepImpl withCacheEnabled(boolean enabled) {
        if (isInCreateMode()) {
            this.inner.withNoCache(!enabled);
        } else {
            this.dockerTaskStepUpdateParameters.withNoCache(!enabled);
        }
        return this;
    }

    @Override
    public RegistryDockerTaskStepImpl withOverridingArguments(Map<String, OverridingArgument> overridingArguments) {
        if (overridingArguments.size() == 0) {
            return this;
        }
        List<Argument> overridingValuesList = new ArrayList<Argument>();
        for (Map.Entry<String, OverridingArgument> entry : overridingArguments.entrySet()) {
            Argument value = new Argument();
            value.withName(entry.getKey());
            value.withValue(entry.getValue().value());
            value.withIsSecret(entry.getValue().isSecret());
            overridingValuesList.add(value);
        }
        if (isInCreateMode()) {
            this.inner.withArguments(overridingValuesList);
        } else {
            this.dockerTaskStepUpdateParameters.withArguments(overridingValuesList);
        }
        return this;
    }

    @Override
    public RegistryDockerTaskStepImpl withOverridingArgument(String name, OverridingArgument overridingArgument) {
        if (this.inner.arguments() == null) {
            this.inner.withArguments(new ArrayList<Argument>());
        }
        Argument value = new Argument();
        value.withName(name);
        value.withValue(overridingArgument.value());
        value.withIsSecret(overridingArgument.isSecret());
        if (isInCreateMode()) {
            this.inner.arguments().add(value);
        } else {
            this.dockerTaskStepUpdateParameters.arguments().add(value);
        }
        return this;
    }


    @Override
    public RegistryTask.DefinitionStages.SourceTriggerDefinition attach() {
        this.taskImpl.withDockerTaskStepCreateParameters(inner);
        return this.taskImpl;
    }

    @Override
    public RegistryTask.Update parent() {
        this.taskImpl.withDockerTaskStepUpdateParameters(dockerTaskStepUpdateParameters);
        return this.taskImpl;
    }

    @Override
    public DockerTaskStep inner() {
        return this.inner;
    }

    private boolean isInCreateMode() {
        if (this.taskImpl.inner().getId() == null) {
            return true;
        }
        return false;
    }
}
