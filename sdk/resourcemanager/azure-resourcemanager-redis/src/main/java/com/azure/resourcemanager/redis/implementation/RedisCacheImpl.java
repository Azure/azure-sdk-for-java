// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.redis.RedisManager;
import com.azure.resourcemanager.redis.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.redis.fluent.models.PrivateLinkResourceInner;
import com.azure.resourcemanager.redis.fluent.models.RedisAccessKeysInner;
import com.azure.resourcemanager.redis.fluent.models.RedisLinkedServerWithPropertiesInner;
import com.azure.resourcemanager.redis.fluent.models.RedisResourceInner;
import com.azure.resourcemanager.redis.models.DayOfWeek;
import com.azure.resourcemanager.redis.models.ExportRdbParameters;
import com.azure.resourcemanager.redis.models.ImportRdbParameters;
import com.azure.resourcemanager.redis.models.ProvisioningState;
import com.azure.resourcemanager.redis.models.RebootType;
import com.azure.resourcemanager.redis.models.RedisAccessKeys;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisCachePremium;
import com.azure.resourcemanager.redis.models.RedisCreateParameters;
import com.azure.resourcemanager.redis.models.RedisFirewallRule;
import com.azure.resourcemanager.redis.models.RedisKeyType;
import com.azure.resourcemanager.redis.models.RedisLinkedServerCreateParameters;
import com.azure.resourcemanager.redis.models.RedisRebootParameters;
import com.azure.resourcemanager.redis.models.RedisRegenerateKeyParameters;
import com.azure.resourcemanager.redis.models.RedisUpdateParameters;
import com.azure.resourcemanager.redis.models.ReplicationRole;
import com.azure.resourcemanager.redis.models.ScheduleEntry;
import com.azure.resourcemanager.redis.models.Sku;
import com.azure.resourcemanager.redis.models.SkuFamily;
import com.azure.resourcemanager.redis.models.SkuName;
import com.azure.resourcemanager.redis.models.TlsVersion;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpoint;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnectionProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Implementation for Redis Cache and its parent interfaces. */
class RedisCacheImpl extends GroupableResourceImpl<RedisCache, RedisResourceInner, RedisCacheImpl, RedisManager>
    implements RedisCache, RedisCachePremium, RedisCache.Definition, RedisCache.Update {
    private final ClientLogger logger = new ClientLogger(getClass());
    private RedisAccessKeys cachedAccessKeys;
    private RedisCreateParameters createParameters;
    private RedisUpdateParameters updateParameters;
    private RedisPatchSchedulesImpl patchSchedules;
    private RedisFirewallRulesImpl firewallRules;
    private boolean patchScheduleAdded;

    RedisCacheImpl(String name, RedisResourceInner innerModel, final RedisManager redisManager) {
        super(name, innerModel, redisManager);
        this.createParameters = new RedisCreateParameters();
        this.patchSchedules = new RedisPatchSchedulesImpl(this);
        this.firewallRules = new RedisFirewallRulesImpl(this);
        this.patchSchedules.enablePostRunMode();
        this.firewallRules.enablePostRunMode();
        this.patchScheduleAdded = false;
    }

    @Override
    public Map<String, RedisFirewallRule> firewallRules() {
        return this.firewallRules.rulesAsMap();
    }

    @Override
    public List<ScheduleEntry> patchSchedules() {
        List<ScheduleEntry> patchSchedules = listPatchSchedules();
        if (patchSchedules == null) {
            return new ArrayList<>();
        }
        return patchSchedules;
    }

    @Override
    public List<ScheduleEntry> listPatchSchedules() {
        // for backward compatibility this method should return Null when there is no records for Patch Schedule
        RedisPatchScheduleImpl patchSchedule = this.patchSchedules.getPatchSchedule();
        if (patchSchedule == null) {
            return null;
        }
        return patchSchedule.scheduleEntries();
    }

    @Override
    public String provisioningState() {
        return this.innerModel().provisioningState().toString();
    }

    @Override
    public String hostname() {
        return this.innerModel().hostname();
    }

    @Override
    public int port() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().port());
    }

    @Override
    public int sslPort() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().sslPort());
    }

    @Override
    public String redisVersion() {
        return this.innerModel().redisVersion();
    }

    @Override
    public Sku sku() {
        return this.innerModel().sku();
    }

    @Override
    public boolean nonSslPort() {
        return this.innerModel().enableNonSslPort();
    }

    @Override
    public int shardCount() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().shardCount());
    }

    @Override
    public String subnetId() {
        return this.innerModel().subnetId();
    }

    @Override
    public String staticIp() {
        return this.innerModel().staticIp();
    }

    @Override
    public TlsVersion minimumTlsVersion() {
        return this.innerModel().minimumTlsVersion();
    }

    @Override
    public Map<String, String> redisConfiguration() {
        return Collections.unmodifiableMap(this.innerModel().redisConfiguration());
    }

    @Override
    public RedisCachePremium asPremium() {
        if (this.isPremium()) {
            return this;
        }
        return null;
    }

    @Override
    public boolean isPremium() {
        return this.sku().name().equals(SkuName.PREMIUM);
    }

    @Override
    public RedisAccessKeys keys() {
        if (cachedAccessKeys == null) {
            cachedAccessKeys = refreshKeys();
        }
        return cachedAccessKeys;
    }

    @Override
    public RedisAccessKeys refreshKeys() {
        RedisAccessKeysInner response =
            this.manager().serviceClient().getRedis().listKeys(this.resourceGroupName(), this.name());
        cachedAccessKeys = new RedisAccessKeysImpl(response);
        return cachedAccessKeys;
    }

    @Override
    public RedisAccessKeys regenerateKey(RedisKeyType keyType) {
        RedisAccessKeysInner response =
            this.manager().serviceClient().getRedis().regenerateKey(this.resourceGroupName(), this.name(), new RedisRegenerateKeyParameters().withKeyType(keyType));
        cachedAccessKeys = new RedisAccessKeysImpl(response);
        return cachedAccessKeys;
    }

    @Override
    public void forceReboot(RebootType rebootType) {
        RedisRebootParameters parameters = new RedisRebootParameters().withRebootType(rebootType);
        this.manager().serviceClient().getRedis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void forceReboot(RebootType rebootType, int shardId) {
        RedisRebootParameters parameters = new RedisRebootParameters().withRebootType(rebootType).withShardId(shardId);
        this.manager().serviceClient().getRedis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files) {
        ImportRdbParameters parameters = new ImportRdbParameters().withFiles(files);
        this.manager().serviceClient().getRedis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files, String fileFormat) {
        ImportRdbParameters parameters = new ImportRdbParameters().withFiles(files).withFormat(fileFormat);
        this.manager().serviceClient().getRedis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix) {
        ExportRdbParameters parameters = new ExportRdbParameters().withContainer(containerSASUrl).withPrefix(prefix);
        this.manager().serviceClient().getRedis().exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix, String fileFormat) {
        ExportRdbParameters parameters =
            new ExportRdbParameters().withContainer(containerSASUrl).withPrefix(prefix).withFormat(fileFormat);
        this.manager().serviceClient().getRedis().exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public RedisCacheImpl withNonSslPort() {
        if (isInCreateMode()) {
            createParameters.withEnableNonSslPort(true);
        } else {
            updateParameters.withEnableNonSslPort(true);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withoutNonSslPort() {
        if (!isInCreateMode()) {
            updateParameters.withEnableNonSslPort(false);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withRedisConfiguration(Map<String, String> redisConfiguration) {
        if (isInCreateMode()) {
            createParameters.withRedisConfiguration(redisConfiguration);
        } else {
            updateParameters.withRedisConfiguration(redisConfiguration);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withRedisConfiguration(String key, String value) {
        if (isInCreateMode()) {
            if (createParameters.redisConfiguration() == null) {
                createParameters.withRedisConfiguration(new TreeMap<>());
            }
            createParameters.redisConfiguration().put(key, value);
        } else {
            if (updateParameters.redisConfiguration() == null) {
                updateParameters.withRedisConfiguration(new TreeMap<>());
            }
            updateParameters.redisConfiguration().put(key, value);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withFirewallRule(String name, String lowestIp, String highestIp) {
        RedisFirewallRuleImpl rule = this.firewallRules.defineInlineFirewallRule(name);
        rule.innerModel().withStartIp(lowestIp);
        rule.innerModel().withEndIp(highestIp);
        return this.withFirewallRule(rule);
    }

    @Override
    public RedisCacheImpl withFirewallRule(RedisFirewallRule rule) {
        this.firewallRules.addRule((RedisFirewallRuleImpl) rule);
        return this;
    }

    @Override
    public RedisCacheImpl withMinimumTlsVersion(TlsVersion tlsVersion) {
        if (isInCreateMode()) {
            createParameters.withMinimumTlsVersion(tlsVersion);
        } else {
            updateParameters.withMinimumTlsVersion(tlsVersion);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withoutMinimumTlsVersion() {
        updateParameters.withMinimumTlsVersion(null);
        return this;
    }

    @Override
    public RedisCacheImpl withoutFirewallRule(String name) {
        this.firewallRules.removeRule(name);
        return this;
    }

    @Override
    public RedisCacheImpl withoutRedisConfiguration() {
        if (updateParameters.redisConfiguration() != null) {
            updateParameters.redisConfiguration().clear();
        }
        return this;
    }

    @Override
    public RedisCacheImpl withoutRedisConfiguration(String key) {
        if (updateParameters.redisConfiguration() != null) {
            updateParameters.redisConfiguration().remove(key);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withSubnet(HasId networkResource, String subnetName) {
        if (networkResource != null) {
            String subnetId = networkResource.id() + "/subnets/" + subnetName;
            return withSubnet(subnetId);
        } else {
            createParameters.withSubnetId(null);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withSubnet(String subnetId) {
        if (subnetId != null) {
            if (isInCreateMode()) {
                createParameters.withSubnetId(subnetId);
            } else {
                throw logger
                    .logExceptionAsError(
                        new UnsupportedOperationException("Subnet cannot be modified during update operation."));
            }
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStaticIp(String staticIp) {
        if (isInCreateMode()) {
            createParameters.withStaticIp(staticIp);
        } else {
            throw logger
                .logExceptionAsError(
                    new UnsupportedOperationException("Static IP cannot be modified during update operation."));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withBasicSku() {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(SkuName.BASIC).withFamily(SkuFamily.C));
        } else {
            updateParameters.withSku(new Sku().withName(SkuName.BASIC).withFamily(SkuFamily.C));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withBasicSku(int capacity) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(SkuName.BASIC).withFamily(SkuFamily.C).withCapacity(capacity));
        } else {
            updateParameters.withSku(new Sku().withName(SkuName.BASIC).withFamily(SkuFamily.C).withCapacity(capacity));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStandardSku() {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(SkuName.STANDARD).withFamily(SkuFamily.C));
        } else {
            updateParameters.withSku(new Sku().withName(SkuName.STANDARD).withFamily(SkuFamily.C));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStandardSku(int capacity) {
        if (isInCreateMode()) {
            createParameters
                .withSku(new Sku().withName(SkuName.STANDARD).withFamily(SkuFamily.C).withCapacity(capacity));
        } else {
            updateParameters
                .withSku(new Sku().withName(SkuName.STANDARD).withFamily(SkuFamily.C).withCapacity(capacity));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withPremiumSku() {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(SkuName.PREMIUM).withFamily(SkuFamily.P).withCapacity(1));
        } else {
            updateParameters.withSku(new Sku().withName(SkuName.PREMIUM).withFamily(SkuFamily.P).withCapacity(1));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withPremiumSku(int capacity) {
        if (isInCreateMode()) {
            createParameters
                .withSku(new Sku().withName(SkuName.PREMIUM).withFamily(SkuFamily.P).withCapacity(capacity));
        } else {
            updateParameters
                .withSku(new Sku().withName(SkuName.PREMIUM).withFamily(SkuFamily.P).withCapacity(capacity));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withShardCount(int shardCount) {
        if (isInCreateMode()) {
            createParameters.withShardCount(shardCount);
        } else {
            updateParameters.withShardCount(shardCount);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withPatchSchedule(DayOfWeek dayOfWeek, int startHourUtc) {
        return this.withPatchSchedule(new ScheduleEntry().withDayOfWeek(dayOfWeek).withStartHourUtc(startHourUtc));
    }

    @Override
    public RedisCacheImpl withPatchSchedule(DayOfWeek dayOfWeek, int startHourUtc, Duration maintenanceWindow) {
        return this
            .withPatchSchedule(
                new ScheduleEntry()
                    .withDayOfWeek(dayOfWeek)
                    .withStartHourUtc(startHourUtc)
                    .withMaintenanceWindow(maintenanceWindow));
    }

    @Override
    public RedisCacheImpl withPatchSchedule(List<ScheduleEntry> scheduleEntries) {
        this.patchSchedules.clear();
        for (ScheduleEntry entry : scheduleEntries) {
            this.withPatchSchedule(entry);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withPatchSchedule(ScheduleEntry scheduleEntry) {
        RedisPatchScheduleImpl psch;
        if (this.patchSchedules.patchSchedulesAsMap().isEmpty()) {
            psch = this.patchSchedules.defineInlinePatchSchedule();
            this.patchScheduleAdded = true;
            psch.innerModel().withScheduleEntries(new ArrayList<>());
            this.patchSchedules.addPatchSchedule(psch);
        } else if (!this.patchScheduleAdded) {
            psch = this.patchSchedules.updateInlinePatchSchedule();
        } else {
            psch = this.patchSchedules.getPatchSchedule();
        }

        psch.innerModel().scheduleEntries().add(scheduleEntry);
        return this;
    }

    @Override
    public RedisCacheImpl withoutPatchSchedule() {
        if (this.patchSchedules.patchSchedulesAsMap().isEmpty()) {
            return this;
        } else {
            this.patchSchedules.deleteInlinePatchSchedule();
        }
        return this;
    }

    @Override
    public void deletePatchSchedule() {
        this.patchSchedules.removePatchSchedule();
        this.patchSchedules.refresh();
    }

    @Override
    public Mono<RedisCache> refreshAsync() {
        return super
            .refreshAsync()
            .then(this.firewallRules.refreshAsync())
            .then(this.patchSchedules.refreshAsync())
            .then(Mono.just(this));
    }

    @Override
    protected Mono<RedisResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getRedis().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<Void> afterPostRunAsync(final boolean isGroupFaulted) {
        this.firewallRules.clear();
        this.patchSchedules.clear();
        this.patchScheduleAdded = false;
        if (isGroupFaulted) {
            return Mono.empty();
        } else {
            return this.refreshAsync().then();
        }
    }

    @Override
    public RedisCacheImpl update() {
        this.updateParameters = new RedisUpdateParameters();
        this.patchSchedules.enableCommitMode();
        this.firewallRules.enableCommitMode();
        return super.update();
    }

    @Override
    public Mono<RedisCache> updateResourceAsync() {
        updateParameters.withTags(this.innerModel().tags());
        this.patchScheduleAdded = false;
        return this
            .manager()
            .serviceClient()
            .getRedis()
            .updateAsync(resourceGroupName(), name(), updateParameters)
            .map(innerToFluentMap(this))
            .filter(
                redisCache -> !redisCache.provisioningState().equalsIgnoreCase(ProvisioningState.SUCCEEDED.toString()))
            .flatMapMany(
                redisCache ->
                    Mono
                        .delay(ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(
                            manager().serviceClient().getDefaultPollInterval()))
                        .flatMap(o ->
                            manager().serviceClient().getRedis().getByResourceGroupAsync(resourceGroupName(), name()))
                        .doOnNext(this::setInner)
                        .repeat()
                        .takeUntil(
                            redisResourceInner ->
                                redisResourceInner
                                    .provisioningState()
                                    .toString()
                                    .equalsIgnoreCase(ProvisioningState.SUCCEEDED.toString())))
            .then(this.patchSchedules.commitAndGetAllAsync())
            .then(this.firewallRules.commitAndGetAllAsync())
            .then(Mono.just(this));
    }

    @Override
    public Mono<RedisCache> createResourceAsync() {
        createParameters.withLocation(this.regionName());
        createParameters.withTags(this.innerModel().tags());
        this.patchScheduleAdded = false;
        return this
            .manager()
            .serviceClient()
            .getRedis()
            .createAsync(this.resourceGroupName(), this.name(), createParameters)
            .map(innerToFluentMap(this));
    }

    @Override
    public String addLinkedServer(String linkedRedisCacheId, String linkedServerLocation, ReplicationRole role) {
        String linkedRedisName = ResourceUtils.nameFromResourceId(linkedRedisCacheId);
        RedisLinkedServerCreateParameters params =
            new RedisLinkedServerCreateParameters()
                .withLinkedRedisCacheId(linkedRedisCacheId)
                .withLinkedRedisCacheLocation(linkedServerLocation)
                .withServerRole(role);
        RedisLinkedServerWithPropertiesInner linkedServerInner =
            this
                .manager()
                .serviceClient()
                .getLinkedServers()
                .create(this.resourceGroupName(), this.name(), linkedRedisName, params);
        return linkedServerInner.name();
    }

    @Override
    public void removeLinkedServer(String linkedServerName) {
        RedisLinkedServerWithPropertiesInner linkedServer = this.manager().serviceClient().getLinkedServers()
            .get(this.resourceGroupName(), this.name(), linkedServerName);

        this.manager().serviceClient().getLinkedServers()
            .delete(this.resourceGroupName(), this.name(), linkedServerName);

        RedisResourceInner innerLinkedResource = null;
        RedisResourceInner innerResource = null;
        while (innerLinkedResource == null
            || innerLinkedResource.provisioningState() != ProvisioningState.SUCCEEDED
            || innerResource == null
            || innerResource.provisioningState() != ProvisioningState.SUCCEEDED) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(30));

            innerLinkedResource =
                this
                    .manager()
                    .serviceClient()
                    .getRedis()
                    .getByResourceGroup(
                        ResourceUtils.groupFromResourceId(linkedServer.id()),
                        ResourceUtils.nameFromResourceId(linkedServer.id()));

            innerResource = this.manager().serviceClient().getRedis().getByResourceGroup(resourceGroupName(), name());
        }
    }

    @Override
    public ReplicationRole getLinkedServerRole(String linkedServerName) {
        RedisLinkedServerWithPropertiesInner linkedServer = this.manager().serviceClient().getLinkedServers()
            .get(this.resourceGroupName(), this.name(), linkedServerName);
        if (linkedServer == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Server returned `null` value for Linked Server '"
                            + linkedServerName
                            + "' for Redis Cache '"
                            + this.name()
                            + "' in Resource Group '"
                            + this.resourceGroupName()
                            + "'."));
        }
        return linkedServer.serverRole();
    }

    @Override
    public Map<String, ReplicationRole> listLinkedServers() {
        Map<String, ReplicationRole> result = new TreeMap<>();
        PagedIterable<RedisLinkedServerWithPropertiesInner> paginatedResponse =
            this.manager().serviceClient().getLinkedServers().list(this.resourceGroupName(), this.name());

        for (RedisLinkedServerWithPropertiesInner linkedServer : paginatedResponse) {
            result.put(linkedServer.name(), linkedServer.serverRole());
        }
        return result;
    }

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return new PagedIterable<>(listPrivateLinkResourcesAsync());
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        return PagedConverter.mapPage(this.manager().serviceClient().getPrivateLinkResources()
            .listByRedisCacheAsync(this.resourceGroupName(), this.name()), PrivateLinkResourceImpl::new);
    }

    @Override
    public PagedIterable<PrivateEndpointConnection> listPrivateEndpointConnections() {
        return new PagedIterable<>(listPrivateEndpointConnectionsAsync());
    }

    @Override
    public PagedFlux<PrivateEndpointConnection> listPrivateEndpointConnectionsAsync() {
        return PagedConverter.mapPage(this.manager().serviceClient().getPrivateEndpointConnections()
            .listAsync(this.resourceGroupName(), this.name()), PrivateEndpointConnectionImpl::new);
    }

    @Override
    public void approvePrivateEndpointConnection(String privateEndpointConnectionName) {
        approvePrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager().serviceClient().getPrivateEndpointConnections()
            .putWithResponseAsync(this.resourceGroupName(), this.name(), privateEndpointConnectionName,
                new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                    new com.azure.resourcemanager.redis.models.PrivateLinkServiceConnectionState()
                        .withStatus(
                            com.azure.resourcemanager.redis.models.PrivateEndpointServiceConnectionStatus.APPROVED)))
            .then();
    }

    @Override
    public void rejectPrivateEndpointConnection(String privateEndpointConnectionName) {
        rejectPrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager().serviceClient().getPrivateEndpointConnections()
            .putWithResponseAsync(this.resourceGroupName(), this.name(), privateEndpointConnectionName,
                new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                    new com.azure.resourcemanager.redis.models.PrivateLinkServiceConnectionState()
                        .withStatus(
                            com.azure.resourcemanager.redis.models.PrivateEndpointServiceConnectionStatus.REJECTED)))
            .then();
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
            return Collections.unmodifiableList(innerModel.requiredZoneNames());
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
                innerModel.privateLinkServiceConnectionState().actionsRequired());
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
