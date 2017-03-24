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
import java.util.HashMap;
import java.util.Map;

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

    private Map<String, ContainerServiceAgentPoolProfile> agentPoolProfilesMap =
            new HashMap<String, ContainerServiceAgentPoolProfile>();
    protected ContainerServiceImpl(String name, ContainerServiceInner innerObject, ComputeManager manager) {
        super(name, innerObject, manager);
        if (this.inner().agentPoolProfiles() == null) {
            this.inner().withAgentPoolProfiles(new ArrayList<ContainerServiceAgentPoolProfile>());
        }

        for (ContainerServiceAgentPoolProfile agentPoolProfile : this.inner().agentPoolProfiles()) {
            this.agentPoolProfilesMap.put(agentPoolProfile.name(), agentPoolProfile);
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
    public Map<String, ContainerServiceAgentPoolProfile> agentPoolProfiles() {
        return this.agentPoolProfilesMap;
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
    public ContainerServiceImpl withServicePrincipalProfile(String clientId, String secret) {
        ContainerServiceServicePrincipalProfile servicePrincipalProfile =
                new ContainerServiceServicePrincipalProfile();
        servicePrincipalProfile.withClientId(clientId);
        servicePrincipalProfile.withSecret(secret);
        this.inner().withServicePrincipalProfile(servicePrincipalProfile);
        return this;
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
        if (this.agentPoolProfilesMap.containsKey(name)) {
            throw new RuntimeException("Agent pool profile Name already exists.");
        }

        ContainerServiceAgentPoolProfile innerPoolProfile = new ContainerServiceAgentPoolProfile();
        innerPoolProfile.withName(name);
        return new CSAgentPoolProfileImpl(innerPoolProfile, this);
    }

    @Override
    public CSAgentPoolProfile.Update<Update> updateContainerServiceAgentPoolProfile(String name) {
        if (!this.agentPoolProfilesMap.containsKey(name)) {
            throw new RuntimeException("Agent pool profile with name does not exists.");
        }

        ContainerServiceAgentPoolProfile innerPoolProfile = this.agentPoolProfilesMap.get(name);
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
        this.inner().withLinuxProfile(new ContainerServiceLinuxProfile());
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

    @Override
    public ContainerServiceImpl withWindowsProfile() {
        this.inner().withWindowsProfile(new ContainerServiceWindowsProfile());
        return this;
    }

    @Override
    public ContainerServiceImpl withAdminPassword(String adminPassword) {
        this.windowsProfile().withAdminPassword(adminPassword);
        return this;
    }

    @Override
    public ContainerServiceImpl withAdminUserName(String adminUsername) {
        this.windowsProfile().withAdminUsername(adminUsername);
        return this;
    }

    @Override
    public ContainerServiceImpl removeAgentPoolProfile(CSAgentPoolProfile agentPoolProfile) {
        if (this.agentPoolProfilesMap.containsKey(agentPoolProfile.name())) {
            this.inner().agentPoolProfiles().remove(
                    this.agentPoolProfilesMap.remove(agentPoolProfile.name()));
        }

        return this;
    }

    @Override
    public ContainerServiceImpl removeWindowsProfile() {
        this.inner().withWindowsProfile(null);
        return this;
    }

    @Override
    public ContainerServiceImpl removeOrchestration() {
        this.inner().withOrchestratorProfile(null);
        this.inner().withCustomProfile(null);
        return this;
    }

    @Override
    public ContainerServiceImpl removeServicePrincipalProfile() {
        this.inner().withServicePrincipalProfile(null);
        return this;
    }

    void attachAgentPoolProfile(CSAgentPoolProfile agentPoolProfile) {
        if (!this.agentPoolProfilesMap.containsKey(agentPoolProfile.name())) {
            this.agentPoolProfilesMap.put(agentPoolProfile.name(), agentPoolProfile.inner());
            this.inner().agentPoolProfiles().add(agentPoolProfile.inner());
        }

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
            this.inner().diagnosticsProfile().withVmDiagnostics(new ContainerServiceVMDiagnostics());

        }

        this.inner().diagnosticsProfile().vmDiagnostics().withEnabled(enabled);
        return this;
    }
}
