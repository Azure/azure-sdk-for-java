/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.redis.DayOfWeek;
import com.microsoft.azure.management.redis.ExportRDBParameters;
import com.microsoft.azure.management.redis.ImportRDBParameters;
import com.microsoft.azure.management.redis.ProvisioningState;
import com.microsoft.azure.management.redis.RebootType;
import com.microsoft.azure.management.redis.RedisAccessKeys;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCachePremium;
import com.microsoft.azure.management.redis.RedisCreateParameters;
import com.microsoft.azure.management.redis.RedisFirewallRule;
import com.microsoft.azure.management.redis.RedisKeyType;
import com.microsoft.azure.management.redis.RedisLinkedServerCreateParameters;
import com.microsoft.azure.management.redis.RedisRebootParameters;
import com.microsoft.azure.management.redis.RedisUpdateParameters;
import com.microsoft.azure.management.redis.ReplicationRole;
import com.microsoft.azure.management.redis.Sku;
import com.microsoft.azure.management.redis.ScheduleEntry;
import com.microsoft.azure.management.redis.SkuFamily;
import com.microsoft.azure.management.redis.SkuName;
import com.microsoft.azure.management.redis.TlsVersion;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import org.joda.time.Period;
import rx.Completable;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for Redis Cache and its parent interfaces.
 */
@LangDefinition
class RedisCacheImpl
        extends GroupableResourceImpl<
            RedisCache,
            RedisResourceInner,
            RedisCacheImpl,
            RedisManager>
        implements
            RedisCache,
            RedisCachePremium,
            RedisCache.Definition,
            RedisCache.Update {
    private RedisAccessKeys cachedAccessKeys;
    private RedisCreateParameters createParameters;
    private RedisUpdateParameters updateParameters;
    private RedisPatchSchedulesImpl patchSchedules;
    private RedisFirewallRulesImpl firewallRules;
    private boolean patchScheduleAdded;

    RedisCacheImpl(String name,
                   RedisResourceInner innerModel,
                   final RedisManager redisManager) {
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
    public String hostName() {
        return this.inner().hostName();
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
    public String staticIP() {
        return this.inner().staticIP();
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
    @Method
    public RedisCachePremium asPremium() {
        if (this.isPremium()) {
            return (RedisCachePremium) this;
        }
        return null;
    }

    @Override
    public boolean isPremium() {
        if (this.sku().name().equals(SkuName.PREMIUM)) {
            return true;
        }
        return false;
    }

    @Override
    public RedisAccessKeys keys() {
        if (cachedAccessKeys == null) {
            cachedAccessKeys = refreshKeys();
        }
        return cachedAccessKeys;
    }

    @Override
    public RedisAccessKeys getKeys() {
        // TODO: Either this or keys() is redundant, but this was added for parity between Java and .NEt without breaking compat. In V2.0, this needs to be cleaned up.
        return this.refreshKeys();
    }

    @Override
    public RedisAccessKeys refreshKeys() {
        RedisAccessKeysInner response =
                this.manager().inner().redis().listKeys(this.resourceGroupName(), this.name());
        cachedAccessKeys = new RedisAccessKeysImpl(response);
        return cachedAccessKeys;
    }

    @Override
    public RedisAccessKeys regenerateKey(RedisKeyType keyType) {
        RedisAccessKeysInner response =
                this.manager().inner().redis().regenerateKey(this.resourceGroupName(), this.name(), keyType);
        cachedAccessKeys = new RedisAccessKeysImpl(response);
        return cachedAccessKeys;
    }

    @Override
    public void forceReboot(RebootType rebootType) {
        RedisRebootParameters parameters = new RedisRebootParameters()
                .withRebootType(rebootType);
        this.manager().inner().redis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void forceReboot(RebootType rebootType, int shardId) {
        RedisRebootParameters parameters = new RedisRebootParameters()
                .withRebootType(rebootType)
                .withShardId(shardId);
        this.manager().inner().redis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files) {
        ImportRDBParameters parameters = new ImportRDBParameters()
                .withFiles(files);
        this.manager().inner().redis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files, String fileFormat) {
        ImportRDBParameters parameters = new ImportRDBParameters()
                .withFiles(files)
                .withFormat(fileFormat);
        this.manager().inner().redis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix) {
        ExportRDBParameters parameters = new ExportRDBParameters()
                .withContainer(containerSASUrl)
                .withPrefix(prefix);
        this.manager().inner().redis().exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix, String fileFormat) {
        ExportRDBParameters parameters = new ExportRDBParameters()
                .withContainer(containerSASUrl)
                .withPrefix(prefix)
                .withFormat(fileFormat);
        this.manager().inner().redis().exportData(this.resourceGroupName(), this.name(), parameters);
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
                createParameters.withRedisConfiguration(new TreeMap<String, String>());
            }
            createParameters.redisConfiguration().put(key, value);
        } else {
            if (updateParameters.redisConfiguration() == null) {
                updateParameters.withRedisConfiguration(new TreeMap<String, String>());
            }
            updateParameters.redisConfiguration().put(key, value);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withFirewallRule(String name, String lowestIp, String highestIp) {
        RedisFirewallRuleImpl rule = this.firewallRules.defineInlineFirewallRule(name);
        rule.inner().withStartIP(lowestIp);
        rule.inner().withEndIP(highestIp);
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
        if (updateParameters.redisConfiguration() != null && updateParameters.redisConfiguration().containsKey(key)) {
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
                throw new UnsupportedOperationException("Subnet cannot be modified during update operation.");
            }
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStaticIP(String staticIP) {
        if (isInCreateMode()) {
            createParameters.withStaticIP(staticIP);
        } else {
            throw new UnsupportedOperationException("Static IP cannot be modified during update operation.");
        }
        return this;
    }

    @Override
    public RedisCacheImpl withBasicSku() {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku()
                    .withName(SkuName.BASIC)
                    .withFamily(SkuFamily.C));
        } else {
            updateParameters.withSku(new Sku()
                    .withName(SkuName.BASIC)
                    .withFamily(SkuFamily.C));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withBasicSku(int capacity) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku()
                    .withName(SkuName.BASIC)
                    .withFamily(SkuFamily.C)
                    .withCapacity(capacity));
        } else {
            updateParameters.withSku(new Sku()
                    .withName(SkuName.BASIC)
                    .withFamily(SkuFamily.C)
                    .withCapacity(capacity));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStandardSku() {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku()
                    .withName(SkuName.STANDARD)
                    .withFamily(SkuFamily.C));
        } else {
            updateParameters.withSku(new Sku()
                    .withName(SkuName.STANDARD)
                    .withFamily(SkuFamily.C));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStandardSku(int capacity) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku()
                    .withName(SkuName.STANDARD)
                    .withFamily(SkuFamily.C)
                    .withCapacity(capacity));
        } else {
            updateParameters.withSku(new Sku()
                    .withName(SkuName.STANDARD)
                    .withFamily(SkuFamily.C)
                    .withCapacity(capacity));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withPremiumSku() {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku()
                    .withName(SkuName.PREMIUM)
                    .withFamily(SkuFamily.P)
                    .withCapacity(1));
        } else {
            updateParameters.withSku(new Sku()
                    .withName(SkuName.PREMIUM)
                    .withFamily(SkuFamily.P)
                    .withCapacity(1));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withPremiumSku(int capacity) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku()
                    .withName(SkuName.PREMIUM)
                    .withFamily(SkuFamily.P)
                    .withCapacity(capacity));
        } else {
            updateParameters.withSku(new Sku()
                    .withName(SkuName.PREMIUM)
                    .withFamily(SkuFamily.P)
                    .withCapacity(capacity));
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
        return this.withPatchSchedule(new ScheduleEntry()
                .withDayOfWeek(dayOfWeek)
                .withStartHourUtc(startHourUtc));
    }

    @Override
    public RedisCacheImpl withPatchSchedule(DayOfWeek dayOfWeek, int startHourUtc, Period maintenanceWindow) {
        return this.withPatchSchedule(new ScheduleEntry()
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
        RedisPatchScheduleImpl psch = null;
        if (this.patchSchedules.patchSchedulesAsMap().isEmpty()) {
            psch = this.patchSchedules.defineInlinePatchSchedule();
            this.patchScheduleAdded = true;
            psch.inner().withScheduleEntries(new ArrayList<ScheduleEntry>());
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
    public Observable<RedisCache> refreshAsync() {
        return super.refreshAsync().map(new Func1<RedisCache, RedisCache>() {
            @Override
            public RedisCache call(RedisCache redisCache) {
                RedisCacheImpl impl = (RedisCacheImpl) redisCache;
                impl.firewallRules.refresh();
                impl.patchSchedules.refresh();
                return impl;
            }
        });
    }

    @Override
    protected Observable<RedisResourceInner> getInnerAsync() {
        return this.manager().inner().redis().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Completable afterPostRunAsync(final boolean isGroupFaulted) {
        this.firewallRules.clear();
        this.patchSchedules.clear();
        this.patchScheduleAdded = false;
        if (isGroupFaulted) {
            return Completable.complete();
        } else {
            return this.refreshAsync().toCompletable();
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
    public Observable<RedisCache> updateResourceAsync() {
        final RedisCacheImpl self = this;
        return this.manager().inner().redis().updateAsync(resourceGroupName(), name(), updateParameters)
                .map(innerToFluentMap(this))
                .doOnNext(new Action1<RedisCache>() {
                    @Override
                    public void call(RedisCache redisCache) {
                        while (!redisCache.provisioningState().equalsIgnoreCase("Succeeded")) {
                            SdkContext.sleep(30 * 1000);

                            RedisResourceInner innerResource = self.manager().inner().redis().getByResourceGroup(resourceGroupName(), name());
                            ((RedisCacheImpl) redisCache).setInner(innerResource);
                            self.setInner(innerResource);
                            self.patchScheduleAdded = false;
                        }
                    }
                })
                .flatMap(new Func1<RedisCache, Observable<RedisCache>>() {
                    @Override
                    public Observable<RedisCache> call(RedisCache redisCache) {
                        return self.patchSchedules.commitAndGetAllAsync()
                                .map(new Func1<List<RedisPatchScheduleImpl>, RedisCache>() {
                                    @Override
                                    public RedisCache call(List<RedisPatchScheduleImpl> redisPatchSchedules) {
                                        return self;
                                    }
                                });
                    }
                })
                .flatMap(new Func1<RedisCache, Observable<RedisCache>>() {
                    @Override
                    public Observable<RedisCache> call(RedisCache redisCache) {
                        return self.firewallRules.commitAndGetAllAsync()
                                .map(new Func1<List<RedisFirewallRuleImpl>, RedisCache>() {
                                    @Override
                                    public RedisCache call(List<RedisFirewallRuleImpl> redisFirewallRules) {
                                        return self;
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<RedisCache> createResourceAsync() {
        createParameters.withLocation(this.regionName());
        createParameters.withTags(this.inner().getTags());
        this.patchScheduleAdded = false;
        return this.manager().inner().redis().createAsync(this.resourceGroupName(), this.name(), createParameters)
                .map(innerToFluentMap(this));
    }

    @Override
    public String addLinkedServer(String linkedRedisCacheId, String linkedServerLocation, ReplicationRole role) {
        String linkedRedisName = ResourceUtils.nameFromResourceId(linkedRedisCacheId);
        RedisLinkedServerCreateParameters params = new RedisLinkedServerCreateParameters()
                .withLinkedRedisCacheId(linkedRedisCacheId)
                .withLinkedRedisCacheLocation(linkedServerLocation)
                .withServerRole(role);
        RedisLinkedServerWithPropertiesInner linkedServerInner = this.manager().inner().linkedServers().create(
                this.resourceGroupName(),
                this.name(),
                linkedRedisName,
                params);
        return linkedServerInner.name();
    }

    @Override
    public void removeLinkedServer(String linkedServerName) {
        RedisLinkedServerWithPropertiesInner linkedServer = this.manager().inner().linkedServers().get(this.resourceGroupName(), this.name(), linkedServerName);

        this.manager().inner().linkedServers().delete(
                this.resourceGroupName(),
                this.name(),
                linkedServerName);

        RedisResourceInner innerLinkedResource = null;
        RedisResourceInner innerResource = null;
        while (innerLinkedResource == null
                || innerLinkedResource.provisioningState() != ProvisioningState.SUCCEEDED
                || innerResource == null
                || innerResource.provisioningState() != ProvisioningState.SUCCEEDED) {
            SdkContext.sleep(30 * 1000);

            innerLinkedResource = this.manager().inner().redis().getByResourceGroup(
                    ResourceUtils.groupFromResourceId(linkedServer.id()),
                    ResourceUtils.nameFromResourceId(linkedServer.id()));

            innerResource = this.manager().inner().redis().getByResourceGroup(resourceGroupName(), name());
        }
    }

    @Override
    public ReplicationRole getLinkedServerRole(String linkedServerName) {
        RedisLinkedServerWithPropertiesInner linkedServer = this.manager().inner().linkedServers().get(
                this.resourceGroupName(),
                this.name(),
                linkedServerName);
        if (linkedServer == null) {
            throw new IllegalArgumentException("Server returned `null` value for Linked Server '"
                    + linkedServerName + "' for Redis Cache '" + this.name()
                    + "' in Resource Group '" + this.resourceGroupName() + "'.");
        }
        return linkedServer.serverRole();
    }

    @Override
    public Map<String, ReplicationRole> listLinkedServers() {
        Map<String, ReplicationRole> result = new TreeMap<>();
        PagedList<RedisLinkedServerWithPropertiesInner> paginatedResponse = this.manager().inner().linkedServers().list(
                this.resourceGroupName(),
                this.name());

        for (RedisLinkedServerWithPropertiesInner linkedServer :  paginatedResponse) {
            result.put(linkedServer.name(), linkedServer.serverRole());
        }
        return result;
    }
}
