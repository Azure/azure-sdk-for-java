// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.resourcemanager.containerregistry.models.Argument;
import com.azure.resourcemanager.containerregistry.models.DockerBuildRequest;
import com.azure.resourcemanager.containerregistry.models.OverridingArgument;
import com.azure.resourcemanager.containerregistry.models.PlatformProperties;
import com.azure.resourcemanager.containerregistry.models.RegistryDockerTaskRunRequest;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RegistryDockerTaskRunRequestImpl
    implements RegistryDockerTaskRunRequest, RegistryDockerTaskRunRequest.Definition, HasInner<DockerBuildRequest> {

    private DockerBuildRequest inner;
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

    RegistryDockerTaskRunRequestImpl(RegistryTaskRunImpl registryTaskRunImpl) {
        this.inner = new DockerBuildRequest();
        this.registryTaskRunImpl = registryTaskRunImpl;
    }

    @Override
    public RegistryDockerTaskRunRequestImpl defineDockerTaskStep() {
        return this;
    }

    @Override
    public RegistryDockerTaskRunRequestImpl withDockerFilePath(String path) {
        this.inner.withDockerFilePath(path);
        return this;
    }

    @Override
    public RegistryDockerTaskRunRequestImpl withImageNames(List<String> imageNames) {
        this.inner.withImageNames(imageNames);
        return this;
    }

    @Override
    public RegistryDockerTaskRunRequestImpl withPushEnabled(boolean enabled) {
        this.inner.withIsPushEnabled(enabled);
        return this;
    }

    @Override
    public RegistryDockerTaskRunRequestImpl withCacheEnabled(boolean enabled) {
        this.inner.withNoCache(enabled);
        return this;
    }

    @Override
    public RegistryDockerTaskRunRequestImpl withOverridingArguments(
        Map<String, OverridingArgument> overridingArguments) {
        if (overridingArguments.size() == 0) {
            return this;
        }
        List<Argument> overridingArgumentsList = new ArrayList<Argument>();
        for (Map.Entry<String, OverridingArgument> entry : overridingArguments.entrySet()) {
            Argument argument = new Argument();
            argument.withName(entry.getKey());
            argument.withValue(entry.getValue().value());
            argument.withIsSecret(entry.getValue().isSecret());
            overridingArgumentsList.add(argument);
        }
        this.inner.withArguments(overridingArgumentsList);
        return this;
    }

    @Override
    public DefinitionStages.DockerTaskRunRequestStepAttachable withOverridingArgument(
        String name, OverridingArgument overridingArgument) {
        if (this.inner.arguments() == null) {
            this.inner.withArguments(new ArrayList<Argument>());
        }
        Argument argument = new Argument();
        argument.withName(name);
        argument.withValue(overridingArgument.value());
        argument.withIsSecret(overridingArgument.isSecret());
        this.inner.arguments().add(argument);
        return this;
    }

    @Override
    public RegistryTaskRunImpl attach() {
        this.registryTaskRunImpl.withDockerTaskRunRequest(this.inner);
        return this.registryTaskRunImpl;
    }

    @Override
    public DockerBuildRequest inner() {
        return this.inner;
    }
}
