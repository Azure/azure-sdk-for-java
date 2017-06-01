/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.redis.DayOfWeek;
import com.microsoft.azure.management.redis.RebootType;
import com.microsoft.azure.management.redis.RedisAccessKeys;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCachePremium;
import com.microsoft.azure.management.redis.RedisKeyType;
import com.microsoft.azure.management.redis.Sku;
import com.microsoft.azure.management.redis.ScheduleEntry;
import com.microsoft.azure.management.redis.SkuFamily;
import com.microsoft.azure.management.redis.SkuName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import org.joda.time.Period;
import rx.Observable;
import rx.functions.Action1;

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
    private RedisCreateParametersInner createParameters;
    private RedisUpdateParametersInner updateParameters;
    private Map<DayOfWeek, ScheduleEntry> scheduleEntries;

    RedisCacheImpl(String name,
                   RedisResourceInner innerModel,
                   final RedisManager redisManager) {
        super(name, innerModel, redisManager);
        this.createParameters = new RedisCreateParametersInner();
        this.scheduleEntries = new TreeMap<>();
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState();
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
        RedisRebootParametersInner parameters = new RedisRebootParametersInner()
                .withRebootType(rebootType);
        this.manager().inner().redis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void forceReboot(RebootType rebootType, int shardId) {
        RedisRebootParametersInner parameters = new RedisRebootParametersInner()
                .withRebootType(rebootType)
                .withShardId(shardId);
        this.manager().inner().redis().forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files) {
        ImportRDBParametersInner parameters = new ImportRDBParametersInner()
                .withFiles(files);
        this.manager().inner().redis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files, String fileFormat) {
        ImportRDBParametersInner parameters = new ImportRDBParametersInner()
                .withFiles(files)
                .withFormat(fileFormat);
        this.manager().inner().redis().importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix) {
        ExportRDBParametersInner parameters = new ExportRDBParametersInner()
                .withContainer(containerSASUrl)
                .withPrefix(prefix);
        this.manager().inner().redis().exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix, String fileFormat) {
        ExportRDBParametersInner parameters = new ExportRDBParametersInner()
                .withContainer(containerSASUrl)
                .withPrefix(prefix)
                .withFormat(fileFormat);
        this.manager().inner().redis().exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    protected Observable<RedisResourceInner> getInnerAsync() {
        return this.manager().inner().redis().getByResourceGroupAsync(this.resourceGroupName(), this.name());
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
            if (isInCreateMode()) {
                createParameters.withSubnetId(subnetId);
            } else {
                updateParameters.withSubnetId(subnetId);
            }
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStaticIP(String staticIP) {
        if (isInCreateMode()) {
            createParameters.withStaticIP(staticIP);
        } else {
            updateParameters.withStaticIP(staticIP);
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
        this.scheduleEntries.clear();
        for (ScheduleEntry entry : scheduleEntries) {
            this.withPatchSchedule(entry);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withPatchSchedule(ScheduleEntry scheduleEntry) {
        this.scheduleEntries.put(scheduleEntry.dayOfWeek(), scheduleEntry);
        return this;
    }

    @Override
    public List<ScheduleEntry> listPatchSchedules() {
        RedisPatchScheduleInner patchSchedules =  this.manager().inner().patchSchedules().get(resourceGroupName(), name());
        if (patchSchedules != null) {
            return Lists.transform(patchSchedules.scheduleEntries(),
                    new Function<ScheduleEntryInner, ScheduleEntry>() {
                        public ScheduleEntry apply(ScheduleEntryInner entryInner) {
                            return new ScheduleEntry(entryInner);
                        }
                    });
        }
        return null;
    }

    @Override
    public void deletePatchSchedule() {
        this.manager().inner().patchSchedules().delete(resourceGroupName(), name());
    }

    private void updatePatchSchedules() {
        if (this.scheduleEntries != null && !this.scheduleEntries.isEmpty()) {
            RedisPatchScheduleInner parameters = new RedisPatchScheduleInner()
                    .withScheduleEntries(new ArrayList<ScheduleEntryInner>());
            for (ScheduleEntry entry : this.scheduleEntries.values()) {
                parameters.scheduleEntries().add(new ScheduleEntryInner()
                        .withDayOfWeek(entry.dayOfWeek())
                        .withMaintenanceWindow(entry.maintenanceWindow())
                        .withStartHourUtc(entry.startHourUtc()));
            }
            this.manager().inner().patchSchedules().createOrUpdate(resourceGroupName(), name(), parameters.scheduleEntries());
        }
    }

    @Override
    public RedisCacheImpl update() {
        this.updateParameters = new RedisUpdateParametersInner();
        this.scheduleEntries = new TreeMap<>();
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
                        }
                        updatePatchSchedules();
                    }
                });
    }

    @Override
    public Observable<RedisCache> createResourceAsync() {
        createParameters.withLocation(this.regionName());
        createParameters.withTags(this.inner().getTags());
        return this.manager().inner().redis().createAsync(this.resourceGroupName(), this.name(), createParameters)
                .map(innerToFluentMap(this))
                .doOnNext(new Action1<RedisCache>() {
                    @Override
                    public void call(RedisCache redisCache) {
                        updatePatchSchedules();
                    }
                });

    }
}
