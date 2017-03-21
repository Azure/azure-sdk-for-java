/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        for(ContainerServiceAgentPoolProfile agentPoolProfile : this.inner().agentPoolProfiles()) {
            this.agentPoolProfilesMap.put(agentPoolProfile.name(), agentPoolProfile);
        }
    }
    /**
     * Properties of the orchestrator.
     */
    public ContainerServiceOrchestratorProfile orchestratorProfile() {
        return this.inner().orchestratorProfile();
    }

    /**
     * Properties for custom clusters.
     */
    public ContainerServiceCustomProfile customProfile() {
        return this.inner().customProfile();
    }

    /**
     * Properties for cluster service principals.
     */
    public ContainerServiceServicePrincipalProfile servicePrincipalProfile() {
        return this.inner().servicePrincipalProfile();
    }

    /**
     * Properties of master agents.
     */
    public ContainerServiceMasterProfile masterProfile() {
        return this.inner().masterProfile();
    }

    /**
     * Properties of the agent pool.
     */
    public Map<String, ContainerServiceAgentPoolProfile> agentPoolProfiles() {
        return this.agentPoolProfilesMap;
    }

    /**
     * Properties of Windows VMs.
     */
    public ContainerServiceWindowsProfile windowsProfile() {
        return this.inner().windowsProfile();
    }

    /**
     * Properties of Linux VMs.
     */
    public ContainerServiceLinuxProfile linuxProfile() {
        return this.inner().linuxProfile();
    }

    /**
     * Properties of the diagnostic agent.
     */
    public ContainerServiceDiagnosticsProfile diagnosticsProfile() {
        return this.inner().diagnosticsProfile();
    }

    /**
     * Properties of the orchestrator.
     *
     * @return the next stage
     */
    public ContainerServiceImpl withOrchestratorProfile(ContainerServiceOchestratorTypes orchestratorType) {
        ContainerServiceOrchestratorProfile orchestratorProfile = new ContainerServiceOrchestratorProfile();
        orchestratorProfile.withOrchestratorType(orchestratorType);
        this.inner().withOrchestratorProfile(orchestratorProfile);
        return this;
    }

    /**
     * Properties for custom clusters.
     *
     * @return the next stage
     */
    public ContainerServiceImpl withCustomProfile(String orchestrator) {
        ContainerServiceCustomProfile customProfile = new ContainerServiceCustomProfile();
        customProfile.withOrchestrator(orchestrator);
        this.inner().withCustomProfile(customProfile);
        return this;
    }

    /**
     * Properties for cluster service principals.
     *
     * @return the next stage
     */
    public ContainerServiceImpl withServicePrincipalProfile(String clientId,String secret) {
        ContainerServiceServicePrincipalProfile servicePrincipalProfile =
                new ContainerServiceServicePrincipalProfile();
        servicePrincipalProfile.withClientId(clientId);
        servicePrincipalProfile.withSecret(secret);
        return this;
    }
    /**
     * Properties of master agents.
     *
     * @return the next stage
     */
    public ContainerServiceImpl withMasterProfile(int count,String dnsPrefix) {
        ContainerServiceMasterProfile masterProfile = new ContainerServiceMasterProfile();
        masterProfile.withCount(count);
        masterProfile.withDnsPrefix(dnsPrefix);
        return this;
    }
    /**
     * Properties of the agent pool.
     *
     * @param name
     * @return the next stage
     */
    public CSAgentPoolProfile.DefinitionStages.Blank<ContainerService.DefinitionStages.WithCreate> defineContainerServiceAgentPoolProfile(String name) {
        if(this.agentPoolProfilesMap.containsKey(name)) {
            throw new RuntimeException("Agent pool profile Name already exists.");
        }

        ContainerServiceAgentPoolProfile innerPoolProfile = new ContainerServiceAgentPoolProfile();
        innerPoolProfile.withName(name);
        return new CSAgentPoolProfileImpl(innerPoolProfile, this);
    }
    /**
     * Properties of Windows VMs.
     *
     * @return the next stage
     */
    public ContainerServiceImpl withWindowsProfile(String adminUsername,String adminPassword) {
        ContainerServiceWindowsProfile windowsProfile = new ContainerServiceWindowsProfile();
        windowsProfile.withAdminPassword(adminPassword);
        windowsProfile.withAdminUsername(adminUsername);
        return this;
    }
    /**
     * Properties of Linux VMs.
     *
     * @return the next stage
     */
    public ContainerServiceImpl withLinuxProfile(String adminUsername, String sshKeyData) {
        ContainerServiceLinuxProfile linuxProfile = new ContainerServiceLinuxProfile();
        linuxProfile.withAdminUsername(adminUsername);
        ContainerServiceSshConfiguration ssh = new ContainerServiceSshConfiguration();
        ssh.withPublicKeys(new ArrayList<ContainerServiceSshPublicKey>());
        ContainerServiceSshPublicKey sshPublicKey = new ContainerServiceSshPublicKey();
        ssh.publicKeys().add(sshPublicKey);
        linuxProfile.withSsh(ssh);
        return this;
    }
    /**
     * Properties of the diagnostic agent.
     *
     * @return the next stage
     */
    public ContainerServiceImpl withDiagnosticsProfile(ContainerServiceDiagnosticsProfile vmDiagnostics) {
        this.inner().withDiagnosticsProfile(vmDiagnostics);
        return this;
    }

    /**
     * Properties of the agent pool.
     *
     * @param name
     * @return the next stage
     */
    public CSAgentPoolProfile.Update<Update> updateContainerServiceAgentPoolProfile(String name) {
        if(!this.agentPoolProfilesMap.containsKey(name)) {
            throw new RuntimeException("Agent pool profile with name does not exists.");
        }

        ContainerServiceAgentPoolProfile innerPoolProfile = this.agentPoolProfilesMap.get(name);
        return new CSAgentPoolProfileImpl(innerPoolProfile, this);
    }

    void attachAgentPoolProfile(CSAgentPoolProfile agentPoolProfile) {
        if(!this.agentPoolProfilesMap.containsKey(agentPoolProfile.name())) {
            this.agentPoolProfilesMap.put(agentPoolProfile.name(), agentPoolProfile.inner());
            this.inner().agentPoolProfiles().add(agentPoolProfile.inner());
        }

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
}
