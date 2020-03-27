/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.AgentProperties;
import com.azure.management.containerregistry.Architecture;
import com.azure.management.containerregistry.DockerBuildRequest;
import com.azure.management.containerregistry.EncodedTaskRunRequest;
import com.azure.management.containerregistry.FileTaskRunRequest;
import com.azure.management.containerregistry.OS;
import com.azure.management.containerregistry.OverridingValue;
import com.azure.management.containerregistry.PlatformProperties;
import com.azure.management.containerregistry.ProvisioningState;
import com.azure.management.containerregistry.RegistryTaskRun;
import com.azure.management.containerregistry.RunStatus;
import com.azure.management.containerregistry.RunType;
import com.azure.management.containerregistry.SetValue;
import com.azure.management.containerregistry.TaskRunRequest;
import com.azure.management.containerregistry.Variant;
import com.azure.management.containerregistry.models.RegistriesInner;
import com.azure.management.containerregistry.models.RunInner;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class RegistryTaskRunImpl implements
        RegistryTaskRun,
        RegistryTaskRun.Definition {

    private final ContainerRegistryManager registryManager;
    private final String key = UUID.randomUUID().toString();
    private String resourceGroupName;
    private String registryName;
    private RunInner inner;
    private RegistriesInner registriesInner;
    private FileTaskRunRequest fileTaskRunRequest;
    private EncodedTaskRunRequest encodedTaskRunRequest;
    private DockerBuildRequest dockerTaskRunRequest;
    private TaskRunRequest taskRunRequest;
    private PlatformProperties platform;

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String registryName() {
        return this.registryName;
    }

    @Override
    public String taskName() {
        return this.inner.task();
    }

    @Override
    public RunStatus status() {
        return this.inner.status();
    }

    @Override
    public RunType runType() {
        return this.inner.runType();
    }

    @Override
    public OffsetDateTime lastUpdatedTime() {
        return this.inner.lastUpdatedTime();
    }

    @Override
    public OffsetDateTime createTime() {
        return this.inner.createTime();
    }

    @Override
    public boolean isArchiveEnabled() {
        return Utils.toPrimitiveBoolean(this.inner.isArchiveEnabled());
    }

    @Override
    public PlatformProperties platform() {
        return this.inner.platform();
    }

    @Override
    public int cpu() {
        if (this.inner.agentConfiguration() == null) {
            return 0;
        }
        return Utils.toPrimitiveInt(this.inner.agentConfiguration().cpu());
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner.provisioningState();
    }

    @Override
    public String runId() {
        return this.inner.runId();
    }


    RegistryTaskRunImpl(ContainerRegistryManager registryManager, RunInner runInner) {
        this.registryManager = registryManager;
        this.registriesInner = registryManager.inner().registries();
        this.platform = new PlatformProperties();
        this.inner = runInner;
    }

    @Override
    public RegistryTaskRunImpl withExistingRegistry(String resourceGroupName, String registryName) {
        this.resourceGroupName = resourceGroupName;
        this.registryName = registryName;
        return this;
    }

    @Override
    public RegistryFileTaskRunRequestImpl withFileTaskRunRequest() {
        return new RegistryFileTaskRunRequestImpl(this);
    }

    @Override
    public RegistryEncodedTaskRunRequestImpl withEncodedTaskRunRequest() {
        return new RegistryEncodedTaskRunRequestImpl(this);
    }

    @Override
    public RegistryDockerTaskRunRequestImpl withDockerTaskRunRequest() {
        return new RegistryDockerTaskRunRequestImpl(this);
    }

    @Override
    public RegistryTaskRunImpl withTaskRunRequest(String taskName) {
        this.taskRunRequest = new TaskRunRequest();
        this.taskRunRequest.withTaskName(taskName);
        this.inner.withTask(taskName);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withCpuCount(int count) {
        if (this.fileTaskRunRequest != null) {
            if (this.fileTaskRunRequest.agentConfiguration() == null) {
                this.fileTaskRunRequest.withAgentConfiguration(new AgentProperties());
            }
            this.fileTaskRunRequest.agentConfiguration().withCpu(count);
        } else if (this.encodedTaskRunRequest != null) {
            if (this.encodedTaskRunRequest.agentConfiguration() == null) {
                this.encodedTaskRunRequest.withAgentConfiguration(new AgentProperties());
            }
            this.encodedTaskRunRequest.agentConfiguration().withCpu(count);
        } else if (this.dockerTaskRunRequest != null) {
            if (this.dockerTaskRunRequest.agentConfiguration() == null) {
                this.dockerTaskRunRequest.withAgentConfiguration(new AgentProperties());
            }
            this.dockerTaskRunRequest.agentConfiguration().withCpu(count);
        }
        return this;
    }

    @Override
    public RegistryTaskRunImpl withSourceLocation(String location) {
        if (this.fileTaskRunRequest != null) {
            this.fileTaskRunRequest.withSourceLocation(location);
        } else if (this.encodedTaskRunRequest != null) {
            this.encodedTaskRunRequest.withSourceLocation(location);
        } else if (this.dockerTaskRunRequest != null) {
            this.dockerTaskRunRequest.withSourceLocation(location);
        }
        return this;
    }

    @Override
    public RegistryTaskRunImpl withTimeout(int timeout) {
        if (this.fileTaskRunRequest != null) {
            this.fileTaskRunRequest.withTimeout(timeout);
        } else if (this.encodedTaskRunRequest != null) {
            this.encodedTaskRunRequest.withTimeout(timeout);
        } else if (this.dockerTaskRunRequest != null) {
            this.dockerTaskRunRequest.withTimeout(timeout);
        }
        return this;
    }

    @Override
    public RegistryTaskRunImpl withOverridingValues(Map<String, OverridingValue> overridingValues) {
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
        this.taskRunRequest.withValues(overridingValuesList);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withOverridingValue(String name, OverridingValue overridingValue) {
        if (this.taskRunRequest.values() == null) {
            this.taskRunRequest.withValues(new ArrayList<SetValue>());
        }
        SetValue value = new SetValue();
        value.withName(name);
        value.withValue(overridingValue.value());
        value.withIsSecret(overridingValue.isSecret());
        this.taskRunRequest.values().add(value);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withArchiveEnabled(boolean enabled) {
        if (this.fileTaskRunRequest != null) {
            this.fileTaskRunRequest.withIsArchiveEnabled(enabled);
        } else if (this.encodedTaskRunRequest != null) {
            this.encodedTaskRunRequest.withIsArchiveEnabled(enabled);
        } else if (this.dockerTaskRunRequest != null) {
            this.dockerTaskRunRequest.withIsArchiveEnabled(enabled);
        } else if (this.taskRunRequest != null) {
            this.taskRunRequest.withIsArchiveEnabled(enabled);
        }
        return this;
    }

    @Override
    public RegistryTaskRunImpl withLinux() {
        this.platform.withOs(OS.LINUX);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withWindows() {
        this.platform.withOs(OS.WINDOWS);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withLinux(Architecture architecture) {
        this.platform.withOs(OS.LINUX).withArchitecture(architecture);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withWindows(Architecture architecture) {
        this.platform.withOs(OS.WINDOWS).withArchitecture(architecture);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withLinux(Architecture architecture, Variant variant) {
        this.platform.withOs(OS.LINUX).withArchitecture(architecture).withVariant(variant);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withWindows(Architecture architecture, Variant variant) {
        this.platform.withOs(OS.WINDOWS).withArchitecture(architecture).withVariant(variant);
        return this;
    }

    @Override
    public RegistryTaskRunImpl withPlatform(PlatformProperties platformProperties) {
        this.platform = platformProperties;
        return this;
    }

    @Override
    public RegistryTaskRun execute() {
        return executeAsync().block();
    }

    @Override
    public Mono<RegistryTaskRun> executeAsync() {
        final RegistryTaskRunImpl self = this;
        if (this.fileTaskRunRequest != null) {
            return this.registriesInner.scheduleRunAsync(this.resourceGroupName, this.registryName, this.fileTaskRunRequest)
                .map(runInner -> {
                    self.inner = runInner;
                    return self;
                });
        } else if (this.encodedTaskRunRequest != null) {
            return this.registriesInner.scheduleRunAsync(this.resourceGroupName, this.registryName, this.encodedTaskRunRequest)
                .map(runInner -> {
                    self.inner = runInner;
                    return self;
                });
        } else if (this.dockerTaskRunRequest != null) {
            return this.registriesInner.scheduleRunAsync(this.resourceGroupName, this.registryName, this.dockerTaskRunRequest)
                .map(runInner -> {
                    self.inner = runInner;
                    return self;
                });
        } else if (this.taskRunRequest != null) {
            return this.registriesInner.scheduleRunAsync(this.resourceGroupName, this.registryName, this.taskRunRequest)
                .map(runInner -> {
                    self.inner = runInner;
                    return self;
                });
        }
        throw new RuntimeException("Unsupported file task run request");
    }

    @Override
    public RunInner inner() {
        return this.inner;
    }

    @Override
    public String key() {
        return this.key;
    }

    void withFileTaskRunRequest(FileTaskRunRequest fileTaskRunRequest) {
        this.fileTaskRunRequest = fileTaskRunRequest;
        this.fileTaskRunRequest.withPlatform(this.platform);
    }

    void withEncodedTaskRunRequest(EncodedTaskRunRequest encodedTaskRunRequest) {
        this.encodedTaskRunRequest = encodedTaskRunRequest;
        this.encodedTaskRunRequest.withPlatform(this.platform);
    }

    void withDockerTaskRunRequest(DockerBuildRequest dockerTaskRunRequest) {
        this.dockerTaskRunRequest = dockerTaskRunRequest;
        this.dockerTaskRunRequest.withPlatform(this.platform);
    }

    @Override
    public RegistryTaskRun refresh() {
        return refreshAsync().block();
    }

    @Override
    public Mono<RegistryTaskRun> refreshAsync() {
        final RegistryTaskRunImpl self = this;
        return registryManager.inner().runs().getAsync(this.resourceGroupName, this.registryName, this.inner.runId())
            .map(runInner -> {
                self.inner = runInner;
                return self;
            });
    }
}
