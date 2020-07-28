// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.RunCommandInputParameter;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetIpConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetNetworkProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetOSDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetOSProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetStorageProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSets;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineScaleSetInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineScaleSetsClient;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.storage.StorageManager;
import java.util.ArrayList;
import java.util.List;
import reactor.core.publisher.Mono;

/** The implementation for VirtualMachineScaleSets. */
public class VirtualMachineScaleSetsImpl
    extends TopLevelModifiableResourcesImpl<
        VirtualMachineScaleSet,
        VirtualMachineScaleSetImpl,
        VirtualMachineScaleSetInner,
        VirtualMachineScaleSetsClient,
    ComputeManager>
    implements VirtualMachineScaleSets {
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    private final AuthorizationManager authorizationManager;

    public VirtualMachineScaleSetsImpl(
        ComputeManager computeManager,
        StorageManager storageManager,
        NetworkManager networkManager,
        AuthorizationManager authorizationManager) {
        super(computeManager.inner().getVirtualMachineScaleSets(), computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.authorizationManager = authorizationManager;
    }

    @Override
    public void deallocate(String groupName, String name) {
        this.deallocateAsync(groupName, name).block();
    }

    @Override
    public Mono<Void> deallocateAsync(String groupName, String name) {
        return this.inner().deallocateAsync(groupName, name, null);
    }

    @Override
    public void powerOff(String groupName, String name) {
        this.powerOffAsync(groupName, name).block();
    }

    @Override
    public Mono<Void> powerOffAsync(String groupName, String name) {
        return this.inner().powerOffAsync(groupName, name, null, null);
    }

    @Override
    public void restart(String groupName, String name) {
        this.restartAsync(groupName, name).block();
    }

    @Override
    public Mono<Void> restartAsync(String groupName, String name) {
        return this.inner().restartAsync(groupName, name, null);
    }

    @Override
    public void start(String groupName, String name) {
        this.startAsync(groupName, name).block();
    }

    @Override
    public Mono<Void> startAsync(String groupName, String name) {
        return this.inner().startAsync(groupName, name, null);
    }

    @Override
    public void reimage(String groupName, String name) {
        this.reimageAsync(groupName, name).block();
    }

    @Override
    public Mono<Void> reimageAsync(String groupName, String name) {
        return this.inner().reimageAsync(groupName, name, null);
    }

    @Override
    public RunCommandResult runPowerShellScriptInVMInstance(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters) {
        return this
            .runPowerShellScriptInVMInstanceAsync(groupName, scaleSetName, vmId, scriptLines, scriptParameters)
            .block();
    }

    @Override
    public Mono<RunCommandResult> runPowerShellScriptInVMInstanceAsync(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters) {
        RunCommandInput inputCommand = new RunCommandInput();
        inputCommand.withCommandId("RunPowerShellScript");
        inputCommand.withScript(scriptLines);
        inputCommand.withParameters(scriptParameters);
        return this.runCommandVMInstanceAsync(groupName, scaleSetName, vmId, inputCommand);
    }

    @Override
    public RunCommandResult runShellScriptInVMInstance(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters) {
        return this
            .runShellScriptInVMInstanceAsync(groupName, scaleSetName, vmId, scriptLines, scriptParameters)
            .block();
    }

    @Override
    public Mono<RunCommandResult> runShellScriptInVMInstanceAsync(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters) {
        RunCommandInput inputCommand = new RunCommandInput();
        inputCommand.withCommandId("RunShellScript");
        inputCommand.withScript(scriptLines);
        inputCommand.withParameters(scriptParameters);
        return this.runCommandVMInstanceAsync(groupName, scaleSetName, vmId, inputCommand);
    }

    @Override
    public RunCommandResult runCommandInVMInstance(
        String groupName, String scaleSetName, String vmId, RunCommandInput inputCommand) {
        return this.runCommandVMInstanceAsync(groupName, scaleSetName, vmId, inputCommand).block();
    }

    @Override
    public Mono<RunCommandResult> runCommandVMInstanceAsync(
        String groupName, String scaleSetName, String vmId, RunCommandInput inputCommand) {
        return this
            .manager()
            .inner()
            .getVirtualMachineScaleSetVMs()
            .runCommandAsync(groupName, scaleSetName, vmId, inputCommand)
            .map(RunCommandResultImpl::new);
    }

    @Override
    public VirtualMachineScaleSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected VirtualMachineScaleSetImpl wrapModel(String name) {
        VirtualMachineScaleSetInner inner = new VirtualMachineScaleSetInner();

        inner.withVirtualMachineProfile(new VirtualMachineScaleSetVMProfile());
        inner
            .virtualMachineProfile()
            .withStorageProfile(
                new VirtualMachineScaleSetStorageProfile()
                    .withOsDisk(new VirtualMachineScaleSetOSDisk().withVhdContainers(new ArrayList<String>())));
        inner.virtualMachineProfile().withOsProfile(new VirtualMachineScaleSetOSProfile());

        inner.virtualMachineProfile().withNetworkProfile(new VirtualMachineScaleSetNetworkProfile());

        inner
            .virtualMachineProfile()
            .networkProfile()
            .withNetworkInterfaceConfigurations(new ArrayList<VirtualMachineScaleSetNetworkConfiguration>());

        VirtualMachineScaleSetNetworkConfiguration primaryNetworkInterfaceConfiguration =
            new VirtualMachineScaleSetNetworkConfiguration()
                .withPrimary(true)
                .withName("primary-nic-cfg")
                .withIpConfigurations(new ArrayList<VirtualMachineScaleSetIpConfiguration>());
        primaryNetworkInterfaceConfiguration
            .ipConfigurations()
            .add(new VirtualMachineScaleSetIpConfiguration().withName("primary-nic-ip-cfg"));

        inner
            .virtualMachineProfile()
            .networkProfile()
            .networkInterfaceConfigurations()
            .add(primaryNetworkInterfaceConfiguration);

        return new VirtualMachineScaleSetImpl(
            name, inner, this.manager(), this.storageManager, this.networkManager, this.authorizationManager);
    }

    @Override
    protected VirtualMachineScaleSetImpl wrapModel(VirtualMachineScaleSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineScaleSetImpl(
            inner.name(), inner, this.manager(), this.storageManager, this.networkManager, this.authorizationManager);
    }
}
