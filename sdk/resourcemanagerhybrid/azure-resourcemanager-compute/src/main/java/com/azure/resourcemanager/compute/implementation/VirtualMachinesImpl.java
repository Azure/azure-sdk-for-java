// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.HardwareProfile;
import com.azure.resourcemanager.compute.models.NetworkProfile;
import com.azure.resourcemanager.compute.models.OSDisk;
import com.azure.resourcemanager.compute.models.OSProfile;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.RunCommandInputParameter;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.StorageProfile;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCaptureParameters;
import com.azure.resourcemanager.compute.models.VirtualMachineSizes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachinesClient;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/** The implementation for VirtualMachines. */
public class VirtualMachinesImpl
    extends TopLevelModifiableResourcesImpl<
        VirtualMachine, VirtualMachineImpl, VirtualMachineInner, VirtualMachinesClient, ComputeManager>
    implements VirtualMachines {
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    private final AuthorizationManager authorizationManager;
    private final VirtualMachineSizesImpl vmSizes;
    private final ClientLogger logger = new ClientLogger(VirtualMachinesImpl.class);

    public VirtualMachinesImpl(
        ComputeManager computeManager,
        StorageManager storageManager,
        NetworkManager networkManager,
        AuthorizationManager authorizationManager) {
        super(computeManager.serviceClient().getVirtualMachines(), computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.authorizationManager = authorizationManager;
        this.vmSizes = new VirtualMachineSizesImpl(computeManager.serviceClient().getVirtualMachineSizes());
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
    public String capture(String groupName, String name, String containerName, String vhdPrefix, boolean overwriteVhd) {
        return this.captureAsync(groupName, name, containerName, vhdPrefix, overwriteVhd).block();
    }

    @Override
    public Mono<String> captureAsync(
        String groupName, String name, String containerName, String vhdPrefix, boolean overwriteVhd) {
        VirtualMachineCaptureParameters parameters = new VirtualMachineCaptureParameters();
        parameters.withDestinationContainerName(containerName);
        parameters.withOverwriteVhds(overwriteVhd);
        parameters.withVhdPrefix(vhdPrefix);
        return this
            .inner()
            .captureAsync(groupName, name, parameters)
            .map(
                captureResultInner -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.writeValueAsString(captureResultInner);
                    } catch (JsonProcessingException ex) {
                        throw logger.logExceptionAsError(Exceptions.propagate(ex));
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
    public RunCommandResult runPowerShellScript(
        String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this.runPowerShellScriptAsync(groupName, name, scriptLines, scriptParameters).block();
    }

    @Override
    public Mono<RunCommandResult> runPowerShellScriptAsync(
        String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        RunCommandInput inputCommand = new RunCommandInput();
        inputCommand.withCommandId("RunPowerShellScript");
        inputCommand.withScript(scriptLines);
        inputCommand.withParameters(scriptParameters);
        return this.runCommandAsync(groupName, name, inputCommand);
    }

    @Override
    public RunCommandResult runShellScript(
        String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this.runShellScriptAsync(groupName, name, scriptLines, scriptParameters).block();
    }

    @Override
    public Mono<RunCommandResult> runShellScriptAsync(
        String groupName, String name, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
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
        return this.inner().runCommandAsync(groupName, name, inputCommand).map(RunCommandResultImpl::new);
    }

    @Override
    public Accepted<Void> beginDeleteById(String id) {
        return beginDeleteByResourceGroup(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name) {
        return AcceptedImpl
            .newAccepted(
                logger,
                this.manager().serviceClient().getHttpPipeline(),
                this.manager().serviceClient().getDefaultPollInterval(),
                () -> this.inner().deleteWithResponseAsync(resourceGroupName, name, null).block(),
                Function.identity(),
                Void.class,
                null,
                Context.NONE);
    }

    @Override
    public void deleteById(String id, boolean forceDeletion) {
        deleteByIdAsync(id, forceDeletion).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id, boolean forceDeletion) {
        return deleteByResourceGroupAsync(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id), forceDeletion);
    }

    @Override
    public void deleteByResourceGroup(String resourceGroupName, String name, boolean forceDeletion) {
        deleteByResourceGroupAsync(resourceGroupName, name, forceDeletion).block();
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String resourceGroupName, String name, boolean forceDeletion) {
        return this.inner().deleteAsync(resourceGroupName, name, forceDeletion);
    }

    @Override
    public Accepted<Void> beginDeleteById(String id, boolean forceDeletion) {
        return beginDeleteByResourceGroup(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id), forceDeletion);
    }

    @Override
    public Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name, boolean forceDeletion) {
        return AcceptedImpl
            .newAccepted(
                logger,
                this.manager().serviceClient().getHttpPipeline(),
                this.manager().serviceClient().getDefaultPollInterval(),
                () -> this.inner().deleteWithResponseAsync(resourceGroupName, name, forceDeletion).block(),
                Function.identity(),
                Void.class,
                null,
                Context.NONE);
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
        inner.withStorageProfile(new StorageProfile().withOsDisk(new OSDisk()).withDataDisks(new ArrayList<>()));
        inner.withOsProfile(new OSProfile());
        inner.withHardwareProfile(new HardwareProfile());
        inner.withNetworkProfile(new NetworkProfile().withNetworkInterfaces(new ArrayList<>()));
        return new VirtualMachineImpl(
            name, inner, this.manager(), this.storageManager, this.networkManager, this.authorizationManager);
    }

    @Override
    protected VirtualMachineImpl wrapModel(VirtualMachineInner virtualMachineInner) {
        if (virtualMachineInner == null) {
            return null;
        }
        return new VirtualMachineImpl(
            virtualMachineInner.name(),
            virtualMachineInner,
            this.manager(),
            this.storageManager,
            this.networkManager,
            this.authorizationManager);
    }
}
