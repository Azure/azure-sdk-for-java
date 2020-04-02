/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.management.compute.models.VirtualMachineInner;
import com.azure.management.compute.models.VirtualMachinesInner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.management.compute.HardwareProfile;
import com.azure.management.compute.NetworkProfile;
import com.azure.management.compute.OSDisk;
import com.azure.management.compute.OSProfile;
import com.azure.management.compute.RunCommandInput;
import com.azure.management.compute.RunCommandInputParameter;
import com.azure.management.compute.RunCommandResult;
import com.azure.management.compute.StorageProfile;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineCaptureParameters;
import com.azure.management.compute.VirtualMachineSizes;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.management.storage.implementation.StorageManager;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for VirtualMachines.
 */
class VirtualMachinesImpl
        extends TopLevelModifiableResourcesImpl<
        VirtualMachine,
        VirtualMachineImpl,
        VirtualMachineInner,
        VirtualMachinesInner,
        ComputeManager>
        implements VirtualMachines {
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    private final GraphRbacManager rbacManager;
    private final VirtualMachineSizesImpl vmSizes;

    VirtualMachinesImpl(ComputeManager computeManager,
                        StorageManager storageManager,
                        NetworkManager networkManager,
                        GraphRbacManager rbacManager) {
        super(computeManager.inner().virtualMachines(), computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.rbacManager = rbacManager;
        this.vmSizes = new VirtualMachineSizesImpl(computeManager.inner().virtualMachineSizes());
    }

    // Actions

    @Override
    public VirtualMachine.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public void deallocate(String groupName, String name) {
        this.inner().deallocate(groupName, name);
    }

    @Override
    public Mono<Void> deallocateAsync(String groupName, String name) {
        return this.inner().deallocateAsync(groupName, name);
    }

    @Override
    public void generalize(String groupName, String name) {
        this.inner().generalize(groupName, name);
    }

    @Override
    public Mono<Void> generalizeAsync(String groupName, String name) {
        return this.inner().generalizeAsync(groupName, name);
    }

    @Override
    public void powerOff(String groupName, String name) {
        this.powerOffAsync(groupName, name).block();
    }

    @Override
    public Mono<Void> powerOffAsync(String groupName, String name) {
        return this.inner().powerOffAsync(groupName, name, null);
    }

    @Override
    public void restart(String groupName, String name) {
        this.inner().restart(groupName, name);
    }

    @Override
    public Mono<Void> restartAsync(String groupName, String name) {
        return this.inner().restartAsync(groupName, name);
    }

    @Override
    public void start(String groupName, String name) {
        this.inner().start(groupName, name);
    }

    @Override
    public Mono<Void> startAsync(String groupName, String name) {
        return this.inner().startAsync(groupName, name);
    }

    @Override
    public void redeploy(String groupName, String name) {
        this.inner().redeploy(groupName, name);
    }

    @Override
    public Mono<Void> redeployAsync(String groupName, String name) {
        return this.inner().redeployAsync(groupName, name);
    }

    @Override
    public String capture(String groupName, String name,
                          String containerName,
                          String vhdPrefix,
                          boolean overwriteVhd) {
        return this.captureAsync(groupName, name, containerName, vhdPrefix, overwriteVhd).block();
    }

    @Override
    public Mono<String> captureAsync(String groupName, String name, String containerName, String vhdPrefix, boolean overwriteVhd) {
        VirtualMachineCaptureParameters parameters = new VirtualMachineCaptureParameters();
        parameters.withDestinationContainerName(containerName);
        parameters.withOverwriteVhds(overwriteVhd);
        parameters.withVhdPrefix(vhdPrefix);
        return this.inner().captureAsync(groupName, name, parameters)
                .map(captureResultInner -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.writeValueAsString(captureResultInner);
                    } catch (JsonProcessingException ex) {
                        throw Exceptions.propagate(ex);
                    }
                });
    }

    @Override
    public void migrateToManaged(String groupName, String name) {
        this.inner().convertToManagedDisks(groupName, name);
    }

    @Override
    public Mono<Void> migrateToManagedAsync(String groupName, String name) {
       return this.inner().convertToManagedDisksAsync(groupName, name);
    }

    @Override
    public RunCommandResult runPowerShellScript(String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this.runPowerShellScriptAsync(groupName, name, scriptLines, scriptParameters).block();
    }

    @Override
    public Mono<RunCommandResult> runPowerShellScriptAsync(String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        RunCommandInput inputCommand = new RunCommandInput();
        inputCommand.withCommandId("RunPowerShellScript");
        inputCommand.withScript(scriptLines);
        inputCommand.withParameters(scriptParameters);
        return this.runCommandAsync(groupName, name, inputCommand);
    }

    @Override
    public RunCommandResult runShellScript(String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this.runShellScriptAsync(groupName, name, scriptLines, scriptParameters).block();
    }

    @Override
    public Mono<RunCommandResult> runShellScriptAsync(String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        RunCommandInput inputCommand = new RunCommandInput();
        inputCommand.withCommandId("RunShellScript");
        inputCommand.withScript(scriptLines);
        inputCommand.withParameters(scriptParameters);
        return this.runCommandAsync(groupName, name, inputCommand);
    }

    @Override
    public RunCommandResult runCommand(String groupName, String name, RunCommandInput inputCommand) {
        return this.runCommandAsync(groupName, name, inputCommand).block();
    }

    @Override
    public Mono<RunCommandResult> runCommandAsync(String groupName, String name, RunCommandInput inputCommand) {
        return this.inner().runCommandAsync(groupName, name, inputCommand)
                .map(RunCommandResultImpl::new);
    }

    // Getters
    @Override
    public VirtualMachineSizes sizes() {
        return this.vmSizes;
    }


    // Helper methods

    @Override
    protected VirtualMachineImpl wrapModel(String name) {
        VirtualMachineInner inner = new VirtualMachineInner();
        inner.withStorageProfile(new StorageProfile()
                .withOsDisk(new OSDisk())
                .withDataDisks(new ArrayList<>()));
        inner.withOsProfile(new OSProfile());
        inner.withHardwareProfile(new HardwareProfile());
        inner.withNetworkProfile(new NetworkProfile()
                .withNetworkInterfaces(new ArrayList<>()));
        return new VirtualMachineImpl(name,
                inner,
                this.manager(),
                this.storageManager,
                this.networkManager,
                this.rbacManager);
    }

    @Override
    protected VirtualMachineImpl wrapModel(VirtualMachineInner virtualMachineInner) {
        if (virtualMachineInner == null) {
            return null;
        }
        return new VirtualMachineImpl(virtualMachineInner.getName(),
                virtualMachineInner,
                this.manager(),
                this.storageManager,
                this.networkManager,
                this.rbacManager);
    }
}