/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.ContainerService;
import com.microsoft.azure.management.compute.ContainerServiceAgentPoolProfile;
import com.microsoft.azure.management.compute.ContainerServiceOrchestratorProfile;
import com.microsoft.azure.management.compute.ContainerServiceCustomProfile;
import com.microsoft.azure.management.compute.ContainerServiceWindowsProfile;
import com.microsoft.azure.management.compute.ContainerServiceLinuxProfile;
import com.microsoft.azure.management.compute.ContainerServiceMasterProfile;
import com.microsoft.azure.management.compute.ContainerServiceServicePrincipalProfile;
import com.microsoft.azure.management.compute.ContainerServiceDiagnosticsProfile;
import com.microsoft.azure.management.compute.CSAgentPoolProfile;
import com.microsoft.azure.management.compute.ContainerServiceMasterProfileCount;
import com.microsoft.azure.management.compute.ContainerServiceSshConfiguration;
import com.microsoft.azure.management.compute.ContainerServiceSshPublicKey;
import com.microsoft.azure.management.compute.ContainerServiceOchestratorTypes;
import com.microsoft.azure.management.compute.ContainerServiceVMDiagnostics;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;

/**
 * The implementation for {@link ContainerService} and its create and update interfaces.
 */
@LangDefinition
public class ContainerServiceImpl
        extends
        GroupableResourceImpl<
                ContainerService,
                ContainerServiceInner,
                ContainerServiceImpl,
                ComputeManager>
        implements ContainerService,
        ContainerService.Definition,
        ContainerService.Update {

    protected ContainerServiceImpl(String name, ContainerServiceInner innerObject, ComputeManager manager) {
        super(name, innerObject, manager);
        if (this.inner().agentPoolProfiles() == null) {
            this.inner().withAgentPoolProfiles(new ArrayList<ContainerServiceAgentPoolProfile>());
        }
    }

    @Override
    public ContainerServiceOrchestratorProfile orchestratorProfile() {
        return this.inner().orchestratorProfile();
    }

    @Override
    public ContainerServiceCustomProfile customProfile() {
        return this.inner().customProfile();
    }

    @Override
    public ContainerServiceServicePrincipalProfile servicePrincipalProfile() {
        return this.inner().servicePrincipalProfile();
    }

    @Override
    public ContainerServiceMasterProfile masterProfile() {
        return this.inner().masterProfile();
    }

    @Override
    public ContainerServiceAgentPoolProfile agentPoolProfile() {
        if (this.inner().agentPoolProfiles().size() > 0) {
            return this.inner().agentPoolProfiles().get(0);
        }

        return null;
    }

    @Override
    public ContainerServiceWindowsProfile windowsProfile() {
        return this.inner().windowsProfile();
    }

    @Override
    public ContainerServiceLinuxProfile linuxProfile() {
        return this.inner().linuxProfile();
    }

    @Override
    public ContainerServiceDiagnosticsProfile diagnosticsProfile() {
        return this.inner().diagnosticsProfile();
    }

    @Override
    public ContainerServiceImpl withMasterProfile(ContainerServiceMasterProfileCount profileCount, String dnsPrefix) {
        ContainerServiceMasterProfile masterProfile = new ContainerServiceMasterProfile();
        masterProfile.withCount(profileCount.count());
        masterProfile.withDnsPrefix(dnsPrefix);
        this.inner().withMasterProfile(masterProfile);
        return this;
    }

    @Override
    public CSAgentPoolProfileImpl defineContainerServiceAgentPoolProfile(String name) {
        ContainerServiceAgentPoolProfile innerPoolProfile = new ContainerServiceAgentPoolProfile();
        innerPoolProfile.withName(name);
        return new CSAgentPoolProfileImpl(innerPoolProfile, this);
    }

    @Override
    protected Observable<ContainerServiceInner> getInnerAsync() {
        return this.manager().inner().containerServices().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<ContainerService> createResourceAsync() {
        final ContainerServiceImpl self = this;
        return this.manager().inner().containerServices().createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(new Func1<ContainerServiceInner, ContainerService>() {
                    @Override
                    public ContainerService call(ContainerServiceInner containerServiceInner) {
                        self.setInner(containerServiceInner);
                        return self;
                    }
                });
    }

    @Override
    public ContainerServiceImpl withDiagnostics() {
        this.withDiagnosticsProfile(true);
        return this;
    }

    @Override
    public ContainerServiceImpl withoutDiagnostics() {
        this.withDiagnosticsProfile(false);
        return this;
    }

    @Override
    public ContainerServiceImpl withLinuxProfile() {
        if (this.inner().linuxProfile() == null) {
            this.inner().withLinuxProfile(new ContainerServiceLinuxProfile());
        }

        return this;
    }

    @Override
    public ContainerServiceImpl withRootUsername(String rootUserName) {
        this.inner().linuxProfile().withAdminUsername(rootUserName);
        return this;
    }

    @Override
    public ContainerServiceImpl withSshKey(String sshKeyData) {
        ContainerServiceSshConfiguration ssh = new ContainerServiceSshConfiguration();
        ssh.withPublicKeys(new ArrayList<ContainerServiceSshPublicKey>());
        ContainerServiceSshPublicKey sshPublicKey = new ContainerServiceSshPublicKey();
        sshPublicKey.withKeyData(sshKeyData);
        ssh.publicKeys().add(sshPublicKey);
        this.inner().linuxProfile().withSsh(ssh);
        return this;
    }

    @Override
    public ContainerServiceImpl withSwarmOrchestration() {
        this.withOrchestratorProfile(ContainerServiceOchestratorTypes.SWARM);
        return this;
    }

    @Override
    public ContainerServiceImpl withDCOSOrchestration() {
        this.withOrchestratorProfile(ContainerServiceOchestratorTypes.DCOS);
        return this;
    }

    @Override
    public ContainerServiceImpl withKubernetesOrchestration() {
        this.withOrchestratorProfile(ContainerServiceOchestratorTypes.KUBERNETES);
        return this;
    }

    void attachAgentPoolProfile(CSAgentPoolProfile agentPoolProfile) {
        this.inner().agentPoolProfiles().add(agentPoolProfile.inner());
    }

    private ContainerServiceImpl withOrchestratorProfile(ContainerServiceOchestratorTypes orchestratorType) {
        ContainerServiceOrchestratorProfile orchestratorProfile = new ContainerServiceOrchestratorProfile();
        orchestratorProfile.withOrchestratorType(orchestratorType);
        this.inner().withOrchestratorProfile(orchestratorProfile);
        return this;
    }

    private ContainerServiceImpl withDiagnosticsProfile(boolean enabled) {
        if (this.inner().diagnosticsProfile() == null) {
            this.inner().withDiagnosticsProfile(new ContainerServiceDiagnosticsProfile());
        }

        if(this.inner().diagnosticsProfile().vmDiagnostics() == null) {
            this.inner().diagnosticsProfile().withVmDiagnostics(new ContainerServiceVMDiagnostics());
        }

        this.inner().diagnosticsProfile().vmDiagnostics().withEnabled(enabled);
        return this;
    }

    @Override
    public Update withAgentPoolCount(int agentPoolCount) {
        if (agentPoolCount < 0 || agentPoolCount > 100) {
            throw new RuntimeException("Agent pool count  must be in the range of 1 to 100 (inclusive)");
        }

        this.inner().agentPoolProfiles().get(0).withCount(agentPoolCount);
        return this;
    }
}
