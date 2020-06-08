// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.implementation;

import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.models.ContainerServiceLinuxProfile;
import com.azure.resourcemanager.containerservice.models.ContainerServiceNetworkProfile;
import com.azure.resourcemanager.containerservice.models.ContainerServiceSshConfiguration;
import com.azure.resourcemanager.containerservice.models.ContainerServiceSshPublicKey;
import com.azure.resourcemanager.containerservice.models.CredentialResult;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.containerservice.models.KubernetesVersion;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAddonProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAgentPoolProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterServicePrincipalProfile;
import com.azure.resourcemanager.containerservice.fluent.inner.ManagedClusterInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The implementation for KubernetesCluster and its create and update interfaces. */
public class KubernetesClusterImpl
    extends GroupableResourceImpl<
        KubernetesCluster, ManagedClusterInner, KubernetesClusterImpl, ContainerServiceManager>
    implements KubernetesCluster, KubernetesCluster.Definition, KubernetesCluster.Update {

    private List<CredentialResult> adminKubeConfigs;
    private List<CredentialResult> userKubeConfigs;

    protected KubernetesClusterImpl(String name, ManagedClusterInner innerObject, ContainerServiceManager manager) {
        super(name, innerObject, manager);
        if (this.inner().agentPoolProfiles() == null) {
            this.inner().withAgentPoolProfiles(new ArrayList<ManagedClusterAgentPoolProfile>());
        }

        this.adminKubeConfigs = null;
        this.userKubeConfigs = null;
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public String dnsPrefix() {
        return this.inner().dnsPrefix();
    }

    @Override
    public String fqdn() {
        return this.inner().fqdn();
    }

    @Override
    public KubernetesVersion version() {
        return KubernetesVersion.fromString(this.inner().kubernetesVersion());
    }

    @Override
    public List<CredentialResult> adminKubeConfigs() {
        if (this.adminKubeConfigs == null || this.adminKubeConfigs.size() == 0) {
            this.adminKubeConfigs =
                this.manager().kubernetesClusters().listAdminKubeConfigContent(this.resourceGroupName(), this.name());
        }
        return Collections.unmodifiableList(this.adminKubeConfigs);
    }

    @Override
    public List<CredentialResult> userKubeConfigs() {
        if (this.userKubeConfigs == null || this.userKubeConfigs.size() == 0) {
            this.userKubeConfigs =
                this.manager().kubernetesClusters().listUserKubeConfigContent(this.resourceGroupName(), this.name());
        }
        return Collections.unmodifiableList(this.userKubeConfigs);
    }

    @Override
    public byte[] adminKubeConfigContent() {
        for (CredentialResult config : adminKubeConfigs()) {
            return config.value();
        }
        return new byte[0];
    }

    @Override
    public byte[] userKubeConfigContent() {
        for (CredentialResult config : userKubeConfigs()) {
            return config.value();
        }
        return new byte[0];
    }

    @Override
    public String servicePrincipalClientId() {
        if (this.inner().servicePrincipalProfile() != null) {
            return this.inner().servicePrincipalProfile().clientId();
        } else {
            return null;
        }
    }

    @Override
    public String servicePrincipalSecret() {
        if (this.inner().servicePrincipalProfile() != null) {
            return this.inner().servicePrincipalProfile().secret();
        } else {
            return null;
        }
    }

    @Override
    public String linuxRootUsername() {
        if (this.inner().linuxProfile() != null) {
            return this.inner().linuxProfile().adminUsername();
        } else {
            return null;
        }
    }

    @Override
    public String sshKey() {
        if (this.inner().linuxProfile() == null
            || this.inner().linuxProfile().ssh() == null
            || this.inner().linuxProfile().ssh().publicKeys() == null
            || this.inner().linuxProfile().ssh().publicKeys().size() == 0) {
            return null;
        } else {
            return this.inner().linuxProfile().ssh().publicKeys().get(0).keyData();
        }
    }

    @Override
    public Map<String, KubernetesClusterAgentPool> agentPools() {
        Map<String, KubernetesClusterAgentPool> agentPoolMap = new HashMap<>();
        if (this.inner().agentPoolProfiles() != null && this.inner().agentPoolProfiles().size() > 0) {
            for (ManagedClusterAgentPoolProfile agentPoolProfile : this.inner().agentPoolProfiles()) {
                agentPoolMap.put(agentPoolProfile.name(), new KubernetesClusterAgentPoolImpl(agentPoolProfile, this));
            }
        }

        return Collections.unmodifiableMap(agentPoolMap);
    }

    @Override
    public ContainerServiceNetworkProfile networkProfile() {
        return this.inner().networkProfile();
    }

    @Override
    public Map<String, ManagedClusterAddonProfile> addonProfiles() {
        return Collections.unmodifiableMap(this.inner().addonProfiles());
    }

    @Override
    public String nodeResourceGroup() {
        return this.inner().nodeResourceGroup();
    }

    @Override
    public boolean enableRBAC() {
        return this.inner().enableRbac();
    }

    private Mono<List<CredentialResult>> listAdminConfig(final KubernetesClusterImpl self) {
        return this
            .manager()
            .kubernetesClusters()
            .listAdminKubeConfigContentAsync(self.resourceGroupName(), self.name())
            .map(
                kubeConfigs -> {
                    self.adminKubeConfigs = kubeConfigs;
                    return self.adminKubeConfigs;
                });
    }

    private Mono<List<CredentialResult>> listUserConfig(final KubernetesClusterImpl self) {
        return this
            .manager()
            .kubernetesClusters()
            .listUserKubeConfigContentAsync(self.resourceGroupName(), self.name())
            .map(
                kubeConfigs -> {
                    self.userKubeConfigs = kubeConfigs;
                    return self.userKubeConfigs;
                });
    }

    @Override
    protected Mono<ManagedClusterInner> getInnerAsync() {
        final KubernetesClusterImpl self = this;
        final Mono<List<CredentialResult>> adminConfig = listAdminConfig(self);
        final Mono<List<CredentialResult>> userConfig = listUserConfig(self);
        return this
            .manager()
            .inner()
            .getManagedClusters()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name())
            .flatMap(
                managedClusterInner -> Flux.merge(adminConfig, userConfig).last().map(bytes -> managedClusterInner));
    }

    @Override
    public Mono<KubernetesCluster> createResourceAsync() {
        final KubernetesClusterImpl self = this;
        if (!this.isInCreateMode()) {
            this.inner().withServicePrincipalProfile(null);
        }
        final Mono<List<CredentialResult>> adminConfig = listAdminConfig(self);
        final Mono<List<CredentialResult>> userConfig = listUserConfig(self);

        return this
            .manager()
            .inner()
            .getManagedClusters()
            .createOrUpdateAsync(self.resourceGroupName(), self.name(), self.inner())
            .flatMap(
                inner ->
                    Flux
                        .merge(adminConfig, userConfig)
                        .last()
                        .map(
                            bytes -> {
                                self.setInner(inner);
                                return self;
                            }));
    }

    @Override
    public KubernetesClusterImpl withVersion(KubernetesVersion kubernetesVersion) {
        this.inner().withKubernetesVersion(kubernetesVersion.toString());
        return this;
    }

    @Override
    public KubernetesClusterImpl withVersion(String kubernetesVersion) {
        this.inner().withKubernetesVersion(kubernetesVersion);
        return this;
    }

    @Override
    public KubernetesClusterImpl withLatestVersion() {
        this.inner().withKubernetesVersion("");
        return this;
    }

    @Override
    public KubernetesClusterImpl withRootUsername(String rootUserName) {
        if (this.inner().linuxProfile() == null) {
            this.inner().withLinuxProfile(new ContainerServiceLinuxProfile());
        }
        this.inner().linuxProfile().withAdminUsername(rootUserName);

        return this;
    }

    @Override
    public KubernetesClusterImpl withSshKey(String sshKeyData) {
        this
            .inner()
            .linuxProfile()
            .withSsh(
                new ContainerServiceSshConfiguration().withPublicKeys(new ArrayList<ContainerServiceSshPublicKey>()));
        this.inner().linuxProfile().ssh().publicKeys().add(new ContainerServiceSshPublicKey().withKeyData(sshKeyData));

        return this;
    }

    @Override
    public KubernetesClusterImpl withServicePrincipalClientId(String clientId) {
        this.inner().withServicePrincipalProfile(new ManagedClusterServicePrincipalProfile().withClientId(clientId));
        return this;
    }

    @Override
    public KubernetesClusterImpl withServicePrincipalSecret(String secret) {
        this.inner().servicePrincipalProfile().withSecret(secret);
        return this;
    }

    @Override
    public KubernetesClusterImpl withDnsPrefix(String dnsPrefix) {
        this.inner().withDnsPrefix(dnsPrefix);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl defineAgentPool(String name) {
        ManagedClusterAgentPoolProfile innerPoolProfile = new ManagedClusterAgentPoolProfile();
        innerPoolProfile.withName(name);
        return new KubernetesClusterAgentPoolImpl(innerPoolProfile, this);
    }

    @Override
    public KubernetesClusterImpl withAgentPoolVirtualMachineCount(String agentPoolName, int agentCount) {
        if (this.inner().agentPoolProfiles() != null && this.inner().agentPoolProfiles().size() > 0) {
            for (ManagedClusterAgentPoolProfile agentPoolProfile : this.inner().agentPoolProfiles()) {
                if (agentPoolProfile.name().equals(agentPoolName)) {
                    agentPoolProfile.withCount(agentCount);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public KubernetesClusterImpl withAgentPoolVirtualMachineCount(int agentCount) {
        if (this.inner().agentPoolProfiles() != null && this.inner().agentPoolProfiles().size() > 0) {
            for (ManagedClusterAgentPoolProfile agentPoolProfile : this.inner().agentPoolProfiles()) {
                agentPoolProfile.withCount(agentCount);
            }
        }
        return this;
    }

    @Override
    public KubernetesCluster.DefinitionStages.NetworkProfileDefinitionStages.Blank<
            KubernetesCluster.DefinitionStages.WithCreate>
        defineNetworkProfile() {
        return new KubernetesClusterNetworkProfileImpl(this);
    }

    @Override
    public KubernetesClusterImpl withAddOnProfiles(Map<String, ManagedClusterAddonProfile> addOnProfileMap) {
        this.inner().withAddonProfiles(addOnProfileMap);
        return this;
    }

    @Override
    public KubernetesClusterImpl withNetworkProfile(ContainerServiceNetworkProfile networkProfile) {
        this.inner().withNetworkProfile(networkProfile);
        return this;
    }

    @Override
    public KubernetesClusterImpl withRBACEnabled() {
        this.inner().withEnableRbac(true);
        return this;
    }

    @Override
    public KubernetesClusterImpl withRBACDisabled() {
        this.inner().withEnableRbac(false);
        return this;
    }
}
