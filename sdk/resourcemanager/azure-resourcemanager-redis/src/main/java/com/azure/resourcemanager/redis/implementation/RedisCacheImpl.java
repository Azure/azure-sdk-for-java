// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.redis.RedisManager;
import com.azure.resourcemanager.redis.fluent.inner.RedisAccessKeysInner;
import com.azure.resourcemanager.redis.fluent.inner.RedisLinkedServerWithPropertiesInner;
import com.azure.resourcemanager.redis.fluent.inner.RedisResourceInner;
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
import com.azure.resourcemanager.redis.models.RedisUpdateParameters;
import com.azure.resourcemanager.redis.models.ReplicationRole;
import com.azure.resourcemanager.redis.models.ScheduleEntry;
import com.azure.resourcemanager.redis.models.Sku;
import com.azure.resourcemanager.redis.models.SkuFamily;
import com.azure.resourcemanager.redis.models.SkuName;
import com.azure.resourcemanager.redis.models.TlsVersion;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
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
        return this.inner().provisioningState().toString();
    }

    @Override
    public String hostname() {
        return this.inner().hostname();
    }

    @Override
    public int port() {
        return Utils.toPrimitiveInt(this.inner().port());
    }

    @Override
    public int sslPort() {
        return Utils.toPrimitiveInt(this.inner().sslPort());
    }

    @Override
    public String redisVersion() {
        return this.inner().redisVersion();
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public boolean nonSslPort() {
        return this.inner().enableNonSslPort();
    }

    @Override
    public int shardCount() {
        return Utils.toPrimitiveInt(this.inner().shardCount());
    }

    @Override
    public String subnetId() {
        return this.inner().subnetId();
    }

    @Override
    public String staticIp() {
        return this.inner().staticIp();
    }

    @Override
    public TlsVersion minimumTlsVersion() {
        return this.inner().minimumTlsVersion();
    }

    @Override
    public Map<String, String> redisConfiguration() {
        return Collections.unmodifiableMap(this.inner().redisConfiguration());
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
            this.manager().inner().getRedis().listKeys(this.resourceGroupName(), this.name());
        cachedAccessKeys = new RedisAccessKeysImpl(response);
        return cachedAccessKeys;
    }

    @Override
    public RedisAccessKeys regenerateKey(RedisKeyType keyType) {
        RedisAccessKeysInner response =
            this.manager().inner().getRedis().regenerateKey(this.resourceGroupName(), this.name(), keyType);
        cachedAccessKeys = new RedisAccessKeysImpl(response);
        return cachedAccessKeys;
    }

    @Override
    public void forceReboot(RebootType rebootType) {
        RedisRebootParameters parameters = new RedisRebootParameters().withRebootType(rebootType);
        this.manager().inner().getRedis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void forceReboot(RebootType rebootType, int shardId) {
        RedisRebootParameters parameters = new RedisRebootParameters().withRebootType(rebootType).withShardId(shardId);
        this.manager().inner().getRedis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files) {
        ImportRdbParameters parameters = new ImportRdbParameters().withFiles(files);
        this.manager().inner().getRedis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files, String fileFormat) {
        ImportRdbParameters parameters = new ImportRdbParameters().withFiles(files).withFormat(fileFormat);
        this.manager().inner().getRedis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix) {
        ExportRdbParameters parameters = new ExportRdbParameters().withContainer(containerSASUrl).withPrefix(prefix);
        this.manager().inner().getRedis().exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix, String fileFormat) {
        ExportRdbParameters parameters =
            new ExportRdbParameters().withContainer(containerSASUrl).withPrefix(prefix).withFormat(fileFormat);
        this.manager().inner().getRedis().exportData(this.resourceGroupName(), this.name(), parameters);
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
        rule.inner().withStartIp(lowestIp);
        rule.inner().withEndIp(highestIp);
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
            psch.inner().withScheduleEntries(new ArrayList<>());
            this.patchSchedules.addPatchSchedule(psch);
        } else if (!this.patchScheduleAdded) {
            psch = this.patchSchedules.updateInlinePatchSchedule();
        } else {
            psch = this.patchSchedules.getPatchSchedule();
        }

        psch.inner().scheduleEntries().add(scheduleEntry);
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
        return this.manager().inner().getRedis().getByResourceGroupAsync(this.resourceGroupName(), this.name());
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
        updateParameters.withTags(this.inner().tags());
        this.patchScheduleAdded = false;
        return this
            .manager()
            .inner()
            .getRedis()
            .updateAsync(resourceGroupName(), name(), updateParameters)
            .map(innerToFluentMap(this))
            .filter(
                redisCache -> !redisCache.provisioningState().equalsIgnoreCase(ProvisioningState.SUCCEEDED.toString()))
            .flatMapMany(
                redisCache ->
                    Mono
                        .delay(SdkContext.getDelayDuration(manager().inner().getDefaultPollInterval()))
                        .flatMap(o -> manager().inner().getRedis().getByResourceGroupAsync(resourceGroupName(), name()))
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
        createParameters.withTags(this.inner().tags());
        this.patchScheduleAdded = false;
        return this
            .manager()
            .inner()
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
                .inner()
                .getLinkedServers()
                .create(this.resourceGroupName(), this.name(), linkedRedisName, params);
        return linkedServerInner.name();
    }

    @Override
    public void removeLinkedServer(String linkedServerName) {
        RedisLinkedServerWithPropertiesInner linkedServer =
            this.manager().inner().getLinkedServers().get(this.resourceGroupName(), this.name(), linkedServerName);

        this.manager().inner().getLinkedServers().delete(this.resourceGroupName(), this.name(), linkedServerName);

        RedisResourceInner innerLinkedResource = null;
        RedisResourceInner innerResource = null;
        while (innerLinkedResource == null
            || innerLinkedResource.provisioningState() != ProvisioningState.SUCCEEDED
            || innerResource == null
            || innerResource.provisioningState() != ProvisioningState.SUCCEEDED) {
            SdkContext.sleep(30 * 1000);

            innerLinkedResource =
                this
                    .manager()
                    .inner()
                    .getRedis()
                    .getByResourceGroup(
                        ResourceUtils.groupFromResourceId(linkedServer.id()),
                        ResourceUtils.nameFromResourceId(linkedServer.id()));

            innerResource = this.manager().inner().getRedis().getByResourceGroup(resourceGroupName(), name());
        }
    }

    @Override
    public ReplicationRole getLinkedServerRole(String linkedServerName) {
        RedisLinkedServerWithPropertiesInner linkedServer =
            this.manager().inner().getLinkedServers().get(this.resourceGroupName(), this.name(), linkedServerName);
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
            this.manager().inner().getLinkedServers().list(this.resourceGroupName(), this.name());

        for (RedisLinkedServerWithPropertiesInner linkedServer : paginatedResponse) {
            result.put(linkedServer.name(), linkedServer.serverRole());
        }
        return result;
    }
}
