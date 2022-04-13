// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.fluent.models.ManagedClusterInner;
import com.azure.resourcemanager.containerservice.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.containerservice.fluent.models.PrivateLinkResourceInner;
import com.azure.resourcemanager.containerservice.models.ContainerServiceLinuxProfile;
import com.azure.resourcemanager.containerservice.models.ContainerServiceNetworkProfile;
import com.azure.resourcemanager.containerservice.models.ContainerServiceSshConfiguration;
import com.azure.resourcemanager.containerservice.models.ContainerServiceSshPublicKey;
import com.azure.resourcemanager.containerservice.models.CredentialResult;
import com.azure.resourcemanager.containerservice.models.Format;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAadProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAddonProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAgentPoolProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterApiServerAccessProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterIdentity;
import com.azure.resourcemanager.containerservice.models.ManagedClusterPropertiesAutoScalerProfile;
import com.azure.resourcemanager.containerservice.models.ManagedClusterServicePrincipalProfile;
import com.azure.resourcemanager.containerservice.models.PowerState;
import com.azure.resourcemanager.containerservice.models.ResourceIdentityType;
import com.azure.resourcemanager.containerservice.models.UserAssignedIdentity;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpoint;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnectionProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** The implementation for KubernetesCluster and its create and update interfaces. */
public class KubernetesClusterImpl
    extends GroupableResourceImpl<
        KubernetesCluster, ManagedClusterInner, KubernetesClusterImpl, ContainerServiceManager>
    implements KubernetesCluster, KubernetesCluster.Definition, KubernetesCluster.Update {
    private final ClientLogger logger = new ClientLogger(getClass());

    private List<CredentialResult> adminKubeConfigs;
    private List<CredentialResult> userKubeConfigs;
    private final Map<Format, List<CredentialResult>> formatUserKubeConfigsMap = new ConcurrentHashMap<>();

    private ManagedClusterInner parameterSnapshotOnUpdate;
    private static final SerializerAdapter SERIALIZER_ADAPTER =
        SerializerFactory.createDefaultManagementSerializerAdapter();

    protected KubernetesClusterImpl(String name, ManagedClusterInner innerObject, ContainerServiceManager manager) {
        super(name, innerObject, manager);
        if (this.innerModel().agentPoolProfiles() == null) {
            this.innerModel().withAgentPoolProfiles(new ArrayList<>());
        }

        this.adminKubeConfigs = null;
        this.userKubeConfigs = null;
    }

    @Override
    public String provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public String dnsPrefix() {
        return this.innerModel().dnsPrefix();
    }

    @Override
    public String fqdn() {
        return this.innerModel().fqdn();
    }

    @Override
    public String version() {
        return this.innerModel().kubernetesVersion();
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
    public List<CredentialResult> userKubeConfigs(Format format) {
        if (format == null) {
            return userKubeConfigs();
        }
        return Collections.unmodifiableList(
            this.formatUserKubeConfigsMap.computeIfAbsent(
                format,
                key -> KubernetesClusterImpl.this
                    .manager()
                    .kubernetesClusters()
                    .listUserKubeConfigContent(
                        KubernetesClusterImpl.this.resourceGroupName(),
                        KubernetesClusterImpl.this.name(),
                        format
                    ))
        );
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
    public byte[] userKubeConfigContent(Format format) {
        if (format == null) {
            return userKubeConfigContent();
        }
        for (CredentialResult config : userKubeConfigs(format)) {
            return config.value();
        }
        return new byte[0];
    }

    @Override
    public String servicePrincipalClientId() {
        if (this.innerModel().servicePrincipalProfile() != null) {
            return this.innerModel().servicePrincipalProfile().clientId();
        } else {
            return null;
        }
    }

    @Override
    public String servicePrincipalSecret() {
        if (this.innerModel().servicePrincipalProfile() != null) {
            return this.innerModel().servicePrincipalProfile().secret();
        } else {
            return null;
        }
    }

    @Override
    public String linuxRootUsername() {
        if (this.innerModel().linuxProfile() != null) {
            return this.innerModel().linuxProfile().adminUsername();
        } else {
            return null;
        }
    }

    @Override
    public String sshKey() {
        if (this.innerModel().linuxProfile() == null
            || this.innerModel().linuxProfile().ssh() == null
            || this.innerModel().linuxProfile().ssh().publicKeys() == null
            || this.innerModel().linuxProfile().ssh().publicKeys().size() == 0) {
            return null;
        } else {
            return this.innerModel().linuxProfile().ssh().publicKeys().get(0).keyData();
        }
    }

    @Override
    public Map<String, KubernetesClusterAgentPool> agentPools() {
        Map<String, KubernetesClusterAgentPool> agentPoolMap = new HashMap<>();
        if (this.innerModel().agentPoolProfiles() != null && this.innerModel().agentPoolProfiles().size() > 0) {
            for (ManagedClusterAgentPoolProfile agentPoolProfile : this.innerModel().agentPoolProfiles()) {
                agentPoolMap.put(agentPoolProfile.name(), new KubernetesClusterAgentPoolImpl(agentPoolProfile, this));
            }
        }

        return Collections.unmodifiableMap(agentPoolMap);
    }

    @Override
    public ContainerServiceNetworkProfile networkProfile() {
        return this.innerModel().networkProfile();
    }

    @Override
    public Map<String, ManagedClusterAddonProfile> addonProfiles() {
        return Collections.unmodifiableMap(this.innerModel().addonProfiles());
    }

    @Override
    public String nodeResourceGroup() {
        return this.innerModel().nodeResourceGroup();
    }

    @Override
    public boolean enableRBAC() {
        return this.innerModel().enableRbac();
    }

    @Override
    public PowerState powerState() {
        return this.innerModel().powerState();
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        String objectId = null;
        if (this.innerModel().identityProfile() != null) {
            UserAssignedIdentity identity =
                this.innerModel().identityProfile().get("kubeletidentity");
            if (identity != null) {
                objectId = identity.objectId();
            }
        }
        return objectId;
    }

    @Override
    public List<String> azureActiveDirectoryGroupIds() {
        if (innerModel().aadProfile() == null
            || CoreUtils.isNullOrEmpty(innerModel().aadProfile().adminGroupObjectIDs())) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(innerModel().aadProfile().adminGroupObjectIDs());
        }
    }

    @Override
    public boolean isLocalAccountsEnabled() {
        return !ResourceManagerUtils.toPrimitiveBoolean(innerModel().disableLocalAccounts());
    }

    @Override
    public boolean isAzureRbacEnabled() {
        return innerModel().aadProfile() != null
            && ResourceManagerUtils.toPrimitiveBoolean(innerModel().aadProfile().enableAzureRbac());
    }

    @Override
    public String diskEncryptionSetId() {
        return innerModel().diskEncryptionSetId();
    }

    @Override
    public void start() {
        this.startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return manager().kubernetesClusters().startAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void stop() {
        this.stopAsync().block();
    }

    @Override
    public Mono<Void> stopAsync() {
        return manager().kubernetesClusters().stopAsync(this.resourceGroupName(), this.name());
    }

    @Override
    protected Mono<ManagedClusterInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getManagedClusters()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name())
            .map(inner -> {
                clearKubeConfig();
                return inner;
            });
    }

    @Override
    public KubernetesClusterImpl update() {
        parameterSnapshotOnUpdate = this.deepCopyInner();
        parameterSnapshotOnUpdate.withServicePrincipalProfile(null);    // servicePrincipalProfile is null in update
        return super.update();
    }

    boolean isClusterModifiedDuringUpdate(ManagedClusterInner parameter) {
        if (parameterSnapshotOnUpdate == null || parameter == null) {
            return true;
        } else {
            final List<ManagedClusterAgentPoolProfile> parameterSnapshotAgentPools =
                parameterSnapshotOnUpdate.agentPoolProfiles();
            final List<ManagedClusterAgentPoolProfile> parameterAgentPools = parameter.agentPoolProfiles();

            // intersection of agent pool names
            Set<String> intersectAgentPoolNames = parameter.agentPoolProfiles().stream()
                .map(ManagedClusterAgentPoolProfile::name)
                .collect(Collectors.toSet());
            intersectAgentPoolNames.retainAll(parameterSnapshotOnUpdate.agentPoolProfiles().stream()
                .map(ManagedClusterAgentPoolProfile::name)
                .collect(Collectors.toSet()));

            // compare the intersection, as add/delete is handled by REST API on AgentPoolsClient
            List<ManagedClusterAgentPoolProfile> agentPools = parameterSnapshotOnUpdate.agentPoolProfiles()
                .stream()
                .filter(p -> intersectAgentPoolNames.contains(p.name()))
                .collect(Collectors.toList());
            // will be reverted in finally block
            parameterSnapshotOnUpdate.withAgentPoolProfiles(agentPools);

            agentPools = parameter.agentPoolProfiles()
                .stream()
                .filter(p -> intersectAgentPoolNames.contains(p.name()))
                .collect(Collectors.toList());
            // will be reverted in finally block
            parameter.withAgentPoolProfiles(agentPools);

            try {
                String jsonStrSnapshot =
                    SERIALIZER_ADAPTER.serialize(parameterSnapshotOnUpdate, SerializerEncoding.JSON);
                String jsonStr = SERIALIZER_ADAPTER.serialize(parameter, SerializerEncoding.JSON);
                return !jsonStr.equals(jsonStrSnapshot);
            } catch (IOException e) {
                // ignored, treat as modified
                return true;
            } finally {
                parameterSnapshotOnUpdate.withAgentPoolProfiles(parameterSnapshotAgentPools);
                parameter.withAgentPoolProfiles(parameterAgentPools);
            }
        }
    }

    ManagedClusterInner deepCopyInner() {
        ManagedClusterInner updateParameter;
        try {
            // deep copy via json
            String jsonStr = SERIALIZER_ADAPTER.serialize(this.innerModel(), SerializerEncoding.JSON);
            updateParameter =
                SERIALIZER_ADAPTER.deserialize(jsonStr, ManagedClusterInner.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            // ignored, null to signify not available
            updateParameter = null;
        }
        return updateParameter;
    }

    @Override
    public Mono<KubernetesCluster> createResourceAsync() {
        final KubernetesClusterImpl self = this;
        if (!this.isInCreateMode()) {
            this.innerModel().withServicePrincipalProfile(null);
        }

        final boolean createOrModified = this.isInCreateMode() || this.isClusterModifiedDuringUpdate(this.innerModel());

        if (createOrModified) {
            return this
                .manager()
                .serviceClient()
                .getManagedClusters()
                .createOrUpdateAsync(self.resourceGroupName(), self.name(), self.innerModel())
                .map(inner -> {
                    self.setInner(inner);
                    clearKubeConfig();
                    return self;
                });
        } else {
            return Mono.just(this);
        }
    }

    private void clearKubeConfig() {
        this.adminKubeConfigs = null;
        this.userKubeConfigs = null;
        this.formatUserKubeConfigsMap.clear();
    }

    @Override
    public KubernetesClusterImpl withVersion(String kubernetesVersion) {
        this.innerModel().withKubernetesVersion(kubernetesVersion);
        return this;
    }

    @Override
    public KubernetesClusterImpl withDefaultVersion() {
        this.innerModel().withKubernetesVersion("");
        return this;
    }

    @Override
    public KubernetesClusterImpl withRootUsername(String rootUserName) {
        if (this.innerModel().linuxProfile() == null) {
            this.innerModel().withLinuxProfile(new ContainerServiceLinuxProfile());
        }
        this.innerModel().linuxProfile().withAdminUsername(rootUserName);

        return this;
    }

    @Override
    public KubernetesClusterImpl withSshKey(String sshKeyData) {
        this
            .innerModel()
            .linuxProfile()
            .withSsh(
                new ContainerServiceSshConfiguration().withPublicKeys(new ArrayList<ContainerServiceSshPublicKey>()));
        this.innerModel().linuxProfile().ssh().publicKeys().add(
            new ContainerServiceSshPublicKey().withKeyData(sshKeyData));

        return this;
    }

    @Override
    public KubernetesClusterImpl withServicePrincipalClientId(String clientId) {
        this.innerModel().withServicePrincipalProfile(
            new ManagedClusterServicePrincipalProfile().withClientId(clientId));
        return this;
    }

    @Override
    public KubernetesClusterImpl withSystemAssignedManagedServiceIdentity() {
        this.innerModel().withIdentity(new ManagedClusterIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED));
        return this;
    }

    @Override
    public KubernetesClusterImpl withServicePrincipalSecret(String secret) {
        this.innerModel().servicePrincipalProfile().withSecret(secret);
        return this;
    }

    @Override
    public KubernetesClusterImpl withDnsPrefix(String dnsPrefix) {
        this.innerModel().withDnsPrefix(dnsPrefix);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl defineAgentPool(String name) {
        ManagedClusterAgentPoolProfile innerPoolProfile = new ManagedClusterAgentPoolProfile()
            .withName(name)
            .withOrchestratorVersion(this.innerModel().kubernetesVersion());
        return new KubernetesClusterAgentPoolImpl(innerPoolProfile, this);
    }

    @Override
    public KubernetesClusterAgentPoolImpl updateAgentPool(String name) {
        for (ManagedClusterAgentPoolProfile agentPoolProfile : innerModel().agentPoolProfiles()) {
            if (agentPoolProfile.name().equals(name)) {
                return new KubernetesClusterAgentPoolImpl(agentPoolProfile, this);
            }
        }
        throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
            "Cannot get agent pool named %s", name)));
    }

    @Override
    public Update withoutAgentPool(String name) {
        if (innerModel().agentPoolProfiles() != null) {
            innerModel().withAgentPoolProfiles(
                innerModel().agentPoolProfiles().stream()
                    .filter(p -> !name.equals(p.name()))
                    .collect(Collectors.toList()));

            this.addDependency(context ->
                manager().serviceClient().getAgentPools().deleteAsync(resourceGroupName(), name(), name)
                    .then(context.voidMono()));
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
        this.innerModel().withAddonProfiles(addOnProfileMap);
        return this;
    }

    @Override
    public KubernetesClusterImpl withNetworkProfile(ContainerServiceNetworkProfile networkProfile) {
        this.innerModel().withNetworkProfile(networkProfile);
        return this;
    }

    @Override
    public KubernetesClusterImpl withRBACEnabled() {
        this.innerModel().withEnableRbac(true);
        return this;
    }

    @Override
    public KubernetesClusterImpl withRBACDisabled() {
        this.innerModel().withEnableRbac(false);
        return this;
    }

    public KubernetesClusterImpl addNewAgentPool(KubernetesClusterAgentPoolImpl agentPool) {
        if (!isInCreateMode()) {
            this.addDependency(context ->
                manager().serviceClient().getAgentPools().createOrUpdateAsync(
                    resourceGroupName(), name(), agentPool.name(), agentPool.getAgentPoolInner())
                    .then(context.voidMono()));
        }
        innerModel().agentPoolProfiles().add(agentPool.innerModel());
        return this;
    }

    @Override
    public KubernetesClusterImpl withAutoScalerProfile(ManagedClusterPropertiesAutoScalerProfile autoScalerProfile) {
        this.innerModel().withAutoScalerProfile(autoScalerProfile);
        return this;
    }

    @Override
    public KubernetesClusterImpl enablePrivateCluster() {
        if (innerModel().apiServerAccessProfile() == null) {
            innerModel().withApiServerAccessProfile(new ManagedClusterApiServerAccessProfile());
        }
        innerModel().apiServerAccessProfile().withEnablePrivateCluster(true);
        return this;
    }

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return new PagedIterable<>(listPrivateLinkResourcesAsync());
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        Mono<Response<List<PrivateLinkResource>>> retList = this.manager().serviceClient().getPrivateLinkResources()
            .listWithResponseAsync(this.resourceGroupName(), this.name())
            .map(response -> new SimpleResponse<>(response, response.getValue().value().stream()
                .map(PrivateLinkResourceImpl::new)
                .collect(Collectors.toList())));

        return PagedConverter.convertListToPagedFlux(retList);
    }

    @Override
    public PagedIterable<PrivateEndpointConnection> listPrivateEndpointConnections() {
        return new PagedIterable<>(listPrivateEndpointConnectionsAsync());
    }

    @Override
    public PagedFlux<PrivateEndpointConnection> listPrivateEndpointConnectionsAsync() {
        Mono<Response<List<PrivateEndpointConnection>>> retList = this.manager().serviceClient()
            .getPrivateEndpointConnections()
            .listWithResponseAsync(this.resourceGroupName(), this.name())
            .map(response -> new SimpleResponse<>(response, response.getValue().value().stream()
                .map(PrivateEndpointConnectionImpl::new)
                .collect(Collectors.toList())));

        return PagedConverter.convertListToPagedFlux(retList);
    }

    @Override
    public KubernetesClusterImpl withAzureActiveDirectoryGroup(String activeDirectoryGroupObjectId) {
        this.withRBACEnabled();

        if (innerModel().aadProfile() == null) {
            innerModel().withAadProfile(new ManagedClusterAadProfile().withManaged(true));
        }
        if (innerModel().aadProfile().adminGroupObjectIDs() == null) {
            innerModel().aadProfile().withAdminGroupObjectIDs(new ArrayList<>());
        }
        innerModel().aadProfile().adminGroupObjectIDs().add(activeDirectoryGroupObjectId);
        return this;
    }

    @Override
    public KubernetesClusterImpl enableAzureRbac() {
        this.withRBACEnabled();

        if (innerModel().aadProfile() == null) {
            innerModel().withAadProfile(new ManagedClusterAadProfile().withManaged(true));
        }
        innerModel().aadProfile().withEnableAzureRbac(true);
        return this;
    }

    @Override
    public KubernetesClusterImpl enableLocalAccounts() {
        innerModel().withDisableLocalAccounts(false);
        return this;
    }

    @Override
    public KubernetesClusterImpl disableLocalAccounts() {
        innerModel().withDisableLocalAccounts(true);
        return this;
    }

    @Override
    public KubernetesCluster.DefinitionStages.WithCreate disableKubernetesRbac() {
        this.innerModel().withEnableRbac(false);
        return this;
    }

    @Override
    public KubernetesCluster.DefinitionStages.WithCreate withDiskEncryptionSet(String diskEncryptionSetId) {
        this.innerModel().withDiskEncryptionSetId(diskEncryptionSetId);
        return this;
    }

    private static final class PrivateLinkResourceImpl implements PrivateLinkResource {
        private final PrivateLinkResourceInner innerModel;

        private PrivateLinkResourceImpl(PrivateLinkResourceInner innerModel) {
            this.innerModel = innerModel;
        }

        @Override
        public String groupId() {
            return innerModel.groupId();
        }

        @Override
        public List<String> requiredMemberNames() {
            return Collections.unmodifiableList(innerModel.requiredMembers());
        }

        @Override
        public List<String> requiredDnsZoneNames() {
            return Collections.emptyList();
        }
    }

    private static final class PrivateEndpointConnectionImpl implements PrivateEndpointConnection {
        private final PrivateEndpointConnectionInner innerModel;

        private final PrivateEndpoint privateEndpoint;
        private final com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState
            privateLinkServiceConnectionState;
        private final PrivateEndpointConnectionProvisioningState provisioningState;

        private PrivateEndpointConnectionImpl(PrivateEndpointConnectionInner innerModel) {
            this.innerModel = innerModel;

            this.privateEndpoint = innerModel.privateEndpoint() == null
                ? null
                : new PrivateEndpoint(innerModel.privateEndpoint().id());
            this.privateLinkServiceConnectionState = innerModel.privateLinkServiceConnectionState() == null
                ? null
                : new com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState(
                innerModel.privateLinkServiceConnectionState().status() == null
                    ? null
                    : com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointServiceConnectionStatus
                    .fromString(innerModel.privateLinkServiceConnectionState().status().toString()),
                innerModel.privateLinkServiceConnectionState().description(),
                "");
            this.provisioningState = innerModel.provisioningState() == null
                ? null
                : PrivateEndpointConnectionProvisioningState.fromString(innerModel.provisioningState().toString());
        }

        @Override
        public String id() {
            return innerModel.id();
        }

        @Override
        public String name() {
            return innerModel.name();
        }

        @Override
        public String type() {
            return innerModel.type();
        }

        @Override
        public PrivateEndpoint privateEndpoint() {
            return privateEndpoint;
        }

        @Override
        public com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState
            privateLinkServiceConnectionState() {
            return privateLinkServiceConnectionState;
        }

        @Override
        public PrivateEndpointConnectionProvisioningState provisioningState() {
            return provisioningState;
        }
    }
}
