/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.ContainerService;
import com.microsoft.azure.management.compute.ContainerServiceAgentPoolProfile;
import com.microsoft.azure.management.compute.ContainerServiceVMSizeTypes;
import com.microsoft.azure.management.compute.ContainerServiceOchestratorTypes;
import com.microsoft.azure.management.compute.ContainerServiceMasterProfileCount;
import com.microsoft.azure.management.compute.ContainerServiceLinuxProfile;
import com.microsoft.azure.management.compute.ContainerServiceOrchestratorProfile;
import com.microsoft.azure.management.compute.ContainerServiceSshPublicKey;
import com.microsoft.azure.management.compute.ContainerServiceMasterProfile;
import com.microsoft.azure.management.compute.ContainerServiceSshConfiguration;
import com.microsoft.azure.management.compute.ContainerServiceAgentPool;
import com.microsoft.azure.management.compute.ContainerServiceVMDiagnostics;
import com.microsoft.azure.management.compute.ContainerServiceDiagnosticsProfile;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;

/**
 * The implementation for ContainerService and its create and update interfaces.
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
    public int masterNodeCount() {
        return this.inner().masterProfile().count();
    }

    @Override
    public ContainerServiceOchestratorTypes orchestratorType() {
        return this.inner().orchestratorProfile().orchestratorType();
    }

    @Override
    public String masterLeafDomainLabel() {
        return this.inner().masterProfile().dnsPrefix();
    }

    @Override
    public String masterFqdn() {
        return this.inner().masterProfile().fqdn();
    }

    @Override
    public String agentPoolName() {
        return this.getSingleAgentPool().name();
    }

    @Override
    public int agentPoolCount() {
        return this.getSingleAgentPool().count();
    }

    @Override
    public String agentPoolLeafDomainLabel() {
        return this.getSingleAgentPool().dnsPrefix();
    }

    @Override
    public ContainerServiceVMSizeTypes agentPoolVMSize() {
        return this.getSingleAgentPool().vmSize();
    }

    @Override
    public String agentPoolFqdn() {
        return this.getSingleAgentPool().fqdn();
    }

    @Override
    public String linuxRootUsername() {
        return this.inner().linuxProfile().adminUsername();
    }

    @Override
    public String sshKey() {
        return this.inner().linuxProfile().ssh().publicKeys().get(0).keyData();
    }

    @Override
    public boolean isDiagnosticsEnabled() {
        return this.inner().diagnosticsProfile().vmDiagnostics().enabled();
    }

    @Override
    public ContainerServiceImpl withMasterNodeCount(ContainerServiceMasterProfileCount profileCount) {
        ContainerServiceMasterProfile masterProfile = new ContainerServiceMasterProfile();
        masterProfile.withCount(profileCount.count());
        this.inner().withMasterProfile(masterProfile);
        return this;
    }

    @Override
    public ContainerServiceImpl withMasterLeafDomainLabel(String dnsPrefix) {
        this.inner().masterProfile().withDnsPrefix(dnsPrefix);
        return this;
    }

    @Override
    public ContainerServiceAgentPoolImpl defineAgentPool(String name) {
        ContainerServiceAgentPoolProfile innerPoolProfile = new ContainerServiceAgentPoolProfile();
        innerPoolProfile.withName(name);
        return new ContainerServiceAgentPoolImpl(innerPoolProfile, this);
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
    public ContainerServiceImpl withLinux() {
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
    public ContainerServiceImpl withDcosOrchestration() {
        this.withOrchestratorProfile(ContainerServiceOchestratorTypes.DCOS);
        return this;
    }

    @Override
    public ContainerServiceImpl withKubernetesOrchestration() {
        this.withOrchestratorProfile(ContainerServiceOchestratorTypes.KUBERNETES);
        return this;
    }

    void attachAgentPoolProfile(ContainerServiceAgentPool agentPoolProfile) {
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

        if (this.inner().diagnosticsProfile().vmDiagnostics() == null) {
            this.inner().diagnosticsProfile().withVmDiagnostics(new ContainerServiceVMDiagnostics());
        }

        this.inner().diagnosticsProfile().vmDiagnostics().withEnabled(enabled);
        return this;
    }

    @Override
    public Update withAgentVMCount(int agentCount) {
        if (agentCount < 0 || agentCount > 100) {
            throw new RuntimeException("Agent pool count  must be in the range of 1 to 100 (inclusive)");
        }

        this.inner().agentPoolProfiles().get(0).withCount(agentCount);
        return this;
    }

    private ContainerServiceAgentPoolProfile getSingleAgentPool() {
        return this.inner().agentPoolProfiles().get(0);
    }
}
