// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.RegistriesClient;
import com.azure.resourcemanager.containerregistry.fluent.models.RunInner;
import com.azure.resourcemanager.containerregistry.models.AgentProperties;
import com.azure.resourcemanager.containerregistry.models.Architecture;
import com.azure.resourcemanager.containerregistry.models.DockerBuildRequest;
import com.azure.resourcemanager.containerregistry.models.EncodedTaskRunRequest;
import com.azure.resourcemanager.containerregistry.models.FileTaskRunRequest;
import com.azure.resourcemanager.containerregistry.models.OS;
import com.azure.resourcemanager.containerregistry.models.OverridingValue;
import com.azure.resourcemanager.containerregistry.models.PlatformProperties;
import com.azure.resourcemanager.containerregistry.models.ProvisioningState;
import com.azure.resourcemanager.containerregistry.models.RegistryTaskRun;
import com.azure.resourcemanager.containerregistry.models.RunStatus;
import com.azure.resourcemanager.containerregistry.models.RunType;
import com.azure.resourcemanager.containerregistry.models.SetValue;
import com.azure.resourcemanager.containerregistry.models.TaskRunRequest;
import com.azure.resourcemanager.containerregistry.models.Variant;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class RegistryTaskRunImpl implements RegistryTaskRun, RegistryTaskRun.Definition {

    private final ClientLogger logger = new ClientLogger(getClass());
    private final ContainerRegistryManager registryManager;
    private final String key = UUID.randomUUID().toString();
    private String resourceGroupName;
    private String registryName;
    private RunInner inner;
    private RegistriesClient registriesInner;
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
        return ResourceManagerUtils.toPrimitiveBoolean(this.inner.isArchiveEnabled());
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
        return ResourceManagerUtils.toPrimitiveInt(this.inner.agentConfiguration().cpu());
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
        this.registriesInner = registryManager.serviceClient().getRegistries();
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
            return this
                .registriesInner
                .scheduleRunAsync(this.resourceGroupName, this.registryName, this.fileTaskRunRequest)
                .map(
                    runInner -> {
                        self.inner = runInner;
                        return self;
                    });
        } else if (this.encodedTaskRunRequest != null) {
            return this
                .registriesInner
                .scheduleRunAsync(this.resourceGroupName, this.registryName, this.encodedTaskRunRequest)
                .map(
                    runInner -> {
                        self.inner = runInner;
                        return self;
                    });
        } else if (this.dockerTaskRunRequest != null) {
            return this
                .registriesInner
                .scheduleRunAsync(this.resourceGroupName, this.registryName, this.dockerTaskRunRequest)
                .map(
                    runInner -> {
                        self.inner = runInner;
                        return self;
                    });
        } else if (this.taskRunRequest != null) {
            return this
                .registriesInner
                .scheduleRunAsync(this.resourceGroupName, this.registryName, this.taskRunRequest)
                .map(
                    runInner -> {
                        self.inner = runInner;
                        return self;
                    });
        }
        throw logger.logExceptionAsError(new RuntimeException("Unsupported file task run request"));
    }

    @Override
    public RunInner innerModel() {
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
        return registryManager
            .serviceClient()
            .getRuns()
            .getAsync(this.resourceGroupName, this.registryName, this.inner.runId())
            .map(
                runInner -> {
                    self.inner = runInner;
                    return self;
                });
    }
}
