/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice.implementation;

import com.azure.management.containerservice.ContainerService;
import com.azure.management.containerservice.ContainerServiceAgentPool;
import com.azure.management.containerservice.ContainerServiceAgentPoolProfile;
import com.azure.management.containerservice.ContainerServiceDiagnosticsProfile;
import com.azure.management.containerservice.ContainerServiceLinuxProfile;
import com.azure.management.containerservice.ContainerServiceMasterProfile;
import com.azure.management.containerservice.ContainerServiceMasterProfileCount;
import com.azure.management.containerservice.ContainerServiceOrchestratorProfile;
import com.azure.management.containerservice.ContainerServiceOrchestratorTypes;
import com.azure.management.containerservice.ContainerServicePrincipalProfile;
import com.azure.management.containerservice.ContainerServiceSshConfiguration;
import com.azure.management.containerservice.ContainerServiceSshPublicKey;
import com.azure.management.containerservice.ContainerServiceStorageProfileTypes;
import com.azure.management.containerservice.ContainerServiceVMDiagnostics;
import com.azure.management.containerservice.ContainerServiceVMSizeTypes;
import com.azure.management.containerservice.Count;
import com.azure.management.containerservice.models.ContainerServiceInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation for ContainerService and its create and update interfaces.
 */
public class ContainerServiceImpl extends
        GroupableResourceImpl<
                        ContainerService,
                        ContainerServiceInner,
                    ContainerServiceImpl,
                    ContainerServiceManager>
        implements
            ContainerService,
            ContainerService.Definition,
            ContainerService.Update {

    private String networkId;
    private String subnetName;

    protected ContainerServiceImpl(String name, ContainerServiceInner innerObject, ContainerServiceManager manager) {
        super(name, innerObject, manager);
        if (this.inner().agentPoolProfiles() == null) {
            this.inner().withAgentPoolProfiles(new ArrayList<ContainerServiceAgentPoolProfile>());
        }

        if (this.inner().masterProfile() != null && this.inner().masterProfile().vnetSubnetID() != null) {
            this.networkId = ResourceUtils.parentResourceIdFromResourceId(this.inner().masterProfile().vnetSubnetID());
            this.subnetName = ResourceUtils.nameFromResourceId(this.inner().masterProfile().vnetSubnetID());
        } else {
            this.networkId = null;
            this.subnetName = null;
        }
    }

    @Override
    public int masterNodeCount() {
        if (this.inner().masterProfile() == null
                || this.inner().masterProfile().count() == null) {
            return 0;
        }

        return Integer.parseInt(this.inner().masterProfile().count().toString());
    }

    @Override
    public ContainerServiceOrchestratorTypes orchestratorType() {
        if (this.inner().orchestratorProfile() == null) {
            throw new RuntimeException("Orchestrator profile is missing!");
        }

        return this.inner().orchestratorProfile().orchestratorType();
    }

    @Override
    public String masterDnsPrefix() {
        if (this.inner().masterProfile() == null) {
            return null;
        }

        return this.inner().masterProfile().dnsPrefix();
    }

    @Override
    public String masterFqdn() {
        if (this.inner().masterProfile() == null) {
            return null;
        }

        return this.inner().masterProfile().fqdn();
    }

    @Override
    public Map<String, ContainerServiceAgentPool> agentPools() {
        Map<String, ContainerServiceAgentPool> agentPoolMap = new HashMap<>();
        if (this.inner().agentPoolProfiles() != null && this.inner().agentPoolProfiles().size() > 0) {
            for (ContainerServiceAgentPoolProfile agentPoolProfile : this.inner().agentPoolProfiles()) {
                agentPoolMap.put(agentPoolProfile.name(), new ContainerServiceAgentPoolImpl(agentPoolProfile, this));
            }
        }

        return Collections.unmodifiableMap(agentPoolMap);
    }

    @Override
    public String linuxRootUsername() {
        if (this.inner().linuxProfile() == null) {
            return null;
        }

        return this.inner().linuxProfile().adminUsername();
    }

    @Override
    public String sshKey() {
        if (this.inner().linuxProfile() == null
                || this.inner().linuxProfile().ssh() == null
                || this.inner().linuxProfile().ssh().publicKeys() == null
                || this.inner().linuxProfile().ssh().publicKeys().size() == 0) {
            return null;
        }

        return this.inner().linuxProfile().ssh().publicKeys().get(0).keyData();
    }

    @Override
    public String servicePrincipalClientId() {
        if (this.inner().servicePrincipalProfile() == null) {
            return null;
        }

        return this.inner().servicePrincipalProfile().clientId();
    }

    @Override
    public String servicePrincipalSecret() {
        if (this.inner().servicePrincipalProfile() == null) {
            return null;
        }

        return this.inner().servicePrincipalProfile().secret();
    }

    @Override
    public int masterOSDiskSizeInGB() {
        if (this.inner().masterProfile() == null || this.inner().masterProfile().osDiskSizeGB() == null) {
            return 0;
        }

        return this.inner().masterProfile().osDiskSizeGB();
    }

    @Override
    public ContainerServiceStorageProfileTypes masterStorageProfile() {
        if (this.inner().masterProfile() == null) {
            return null;
        }

        return this.inner().masterProfile().storageProfile();
    }

    @Override
    public String masterSubnetName() {
        return subnetName;
    }

    @Override
    public String networkId() {
        return networkId;
    }

    @Override
    public boolean isDiagnosticsEnabled() {
        if (this.inner().diagnosticsProfile() == null
                || this.inner().diagnosticsProfile().vmDiagnostics() == null) {
            throw new RuntimeException("Diagnostic profile is missing!");
        }

        return this.inner().diagnosticsProfile().vmDiagnostics().enabled();
    }

    @Override
    public ContainerServiceImpl withMasterNodeCount(ContainerServiceMasterProfileCount profileCount) {
        ContainerServiceMasterProfile masterProfile = new ContainerServiceMasterProfile().withVmSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2);
        masterProfile.withCount(Count.fromString(String.valueOf(profileCount.count())));
        this.inner().withMasterProfile(masterProfile);
        return this;
    }

    @Override
    public ContainerServiceImpl withMasterDnsPrefix(String dnsPrefix) {
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
        this.withOrchestratorProfile(ContainerServiceOrchestratorTypes.SWARM);
        return this;
    }

    @Override
    public ContainerServiceImpl withDcosOrchestration() {
        this.withOrchestratorProfile(ContainerServiceOrchestratorTypes.DCOS);
        return this;
    }

    @Override
    public ContainerServiceImpl withKubernetesOrchestration() {
        this.withOrchestratorProfile(ContainerServiceOrchestratorTypes.KUBERNETES);
        return this;
    }

    @Override
    public ContainerServiceImpl withServicePrincipal(String clientId, String secret) {
        ContainerServicePrincipalProfile serviceProfile =
                new ContainerServicePrincipalProfile();
        serviceProfile.withClientId(clientId);
        serviceProfile.withSecret(secret);
        this.inner().withServicePrincipalProfile(serviceProfile);
        return this;
    }

    private ContainerServiceImpl withOrchestratorProfile(ContainerServiceOrchestratorTypes orchestratorType) {
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
    public ContainerServiceImpl withAgentVirtualMachineCount(int agentCount) {
        this.inner().agentPoolProfiles().get(0).withCount(agentCount);
        return this;
    }

    @Override
    public ContainerServiceImpl withMasterVMSize(ContainerServiceVMSizeTypes vmSize) {
        this.inner().masterProfile().withVmSize(vmSize);
        return this;
    }

    @Override
    public ContainerServiceImpl withMasterStorageProfile(ContainerServiceStorageProfileTypes storageProfile) {
        this.inner().masterProfile().withStorageProfile(storageProfile);
        return this;
    }

    @Override
    public ContainerServiceImpl withMasterOSDiskSizeInGB(int osDiskSizeInGB) {
        this.inner().masterProfile().withOsDiskSizeGB(osDiskSizeInGB);
        return this;
    }

    @Override
    public ContainerServiceImpl withSubnet(String networkId, String subnetName) {
        this.networkId = networkId;
        this.subnetName = subnetName;
        this.inner().masterProfile().withVnetSubnetID(networkId + "/subnets/" + subnetName);
        if (this.inner().agentPoolProfiles() != null) {
            for (ContainerServiceAgentPoolProfile agentPoolProfile : this.inner().agentPoolProfiles()) {
                String agentPoolSubnet = agentPoolProfile.vnetSubnetID();
                if (agentPoolSubnet == null) {
                    agentPoolProfile.withVnetSubnetID(networkId + "/subnets/" + subnetName);
                } else {
                    agentPoolProfile.withVnetSubnetID(networkId + "/subnets/" + agentPoolSubnet);
                }
            }
        }
        return this;
    }

    @Override
    protected Mono<ContainerServiceInner> getInnerAsync() {
        return this.manager().inner().containerServices().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<ContainerService> createResourceAsync() {
        final ContainerServiceImpl self = this;
        if (!this.isInCreateMode()) {
            this.inner().withServicePrincipalProfile(null);
        }

        return this.manager().inner().containerServices().createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(containerServiceInner -> {
                    self.setInner(containerServiceInner);
                    return self;
                });
    }

}
