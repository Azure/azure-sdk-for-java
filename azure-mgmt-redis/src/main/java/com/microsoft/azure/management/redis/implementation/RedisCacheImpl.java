/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.redis.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import rx.Observable;
import rx.functions.Action1;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
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
    private final RedisInner client;

    RedisCacheImpl(String name,
                   RedisResourceInner innerModel,
                   final RedisInner client,
                   final RedisManager storageManager) {
        super(name, innerModel, storageManager);
        this.createParameters = new RedisCreateParametersInner();
        this.client = client;
    }

    @Override
    public String provisioningState()  { return this.inner().provisioningState(); }

    @Override
    public String hostName() { return this.inner().hostName(); }

    @Override
    public int port() { return this.inner().port(); }

    @Override
    public int sslPort() { return this.inner().sslPort(); }

    @Override
    public String redisVersion() { return this.inner().redisVersion(); }

    @Override
    public Sku sku() { return this.inner().sku(); }

    @Override
    public Map<String, String> redisConfiguration() { return this.inner().redisConfiguration(); }

    @Override
    public Boolean enableNonSslPort() { return this.inner().enableNonSslPort(); }

    @Override
    public Map<String, String> tenantSettings() { return this.inner().tenantSettings(); }

    @Override
    public Integer shardCount() { return this.inner().shardCount(); }

    @Override
    public String subnetId() { return this.inner().subnetId(); }

    @Override
    public String staticIP() { return this.inner().staticIP(); }

    @Override
    public RedisCachePremium asPremium()
    {
        if(this.sku().name().equals(SkuName.PREMIUM)) {
            return (RedisCachePremium) this;
        }
        return null;
    }

    @Override
    public RedisAccessKeys keys() throws CloudException, IOException {
        if (cachedAccessKeys == null) {
            cachedAccessKeys = refreshKeys();
        }
        return cachedAccessKeys;
    }

    @Override
    public RedisAccessKeys refreshKeys() throws CloudException, IOException {
        ServiceResponse<RedisAccessKeysInner> response =
                this.client.listKeys(this.resourceGroupName(), this.name());
        cachedAccessKeys = new RedisAccessKeys(response.getBody());
        return cachedAccessKeys;
    }

    @Override
    public RedisAccessKeys regenerateKey(RedisKeyType keyType) throws CloudException, IOException {
        ServiceResponse<RedisAccessKeysInner> response =
                this.client.regenerateKey(this.resourceGroupName(), this.name(), keyType);
        cachedAccessKeys = new RedisAccessKeys(response.getBody());
        return cachedAccessKeys;
    }

    @Override
    public void forceReboot(RebootType rebootType) throws CloudException, IOException {
        RedisRebootParametersInner parameters = new RedisRebootParametersInner()
                .withRebootType(rebootType);
        this.client.forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void forceReboot(RebootType rebootType, int shardId) throws CloudException, IOException {
        RedisRebootParametersInner parameters = new RedisRebootParametersInner()
                .withRebootType(rebootType)
                .withShardId(shardId);
        this.client.forceReboot(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files)  throws CloudException, IOException, InterruptedException {
        ImportRDBParametersInner parameters = new ImportRDBParametersInner()
                .withFiles(files);
        this.client.importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void importData(List<String> files, String fileFormat)  throws CloudException, IOException, InterruptedException {
        ImportRDBParametersInner parameters = new ImportRDBParametersInner()
                .withFiles(files)
                .withFormat(fileFormat);
        this.client.importData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix) throws CloudException, IOException, InterruptedException {
        ExportRDBParametersInner parameters = new ExportRDBParametersInner()
                .withContainer(containerSASUrl)
                .withPrefix(prefix);
        this.client.exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public void exportData(String containerSASUrl, String prefix, String fileFormat) throws CloudException, IOException, InterruptedException {
        ExportRDBParametersInner parameters = new ExportRDBParametersInner()
                .withContainer(containerSASUrl)
                .withPrefix(prefix)
                .withFormat(fileFormat);
        this.client.exportData(this.resourceGroupName(), this.name(), parameters);
    }

    @Override
    public RedisCacheImpl refresh() throws Exception {
        ServiceResponse<RedisResourceInner> response =
                this.client.get(this.resourceGroupName(), this.name());
        RedisResourceInner redisResourceInner = response.getBody();
        this.setInner(redisResourceInner);
        clearWrapperProperties();
        return this;
    }

    @Override
    public RedisCacheImpl withNonSslPortEnabled() {
        if (isInCreateMode()) {
            createParameters.withEnableNonSslPort(true);
        } else {
            createParameters.withEnableNonSslPort(true);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withNonSslPortDisabled() {
        if (isInCreateMode()) {
            createParameters.withEnableNonSslPort(false);
        } else {
            createParameters.withEnableNonSslPort(false);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withRedisConfiguration(Map<String,String> redisConfiguration) {
        if (isInCreateMode()) {
            createParameters.withRedisConfiguration(redisConfiguration);
        } else {
            createParameters.withRedisConfiguration(redisConfiguration);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withTenantSettings(Map<String,String> tenantSettings) {
        if (isInCreateMode()) {
            createParameters.withTenantSettings(tenantSettings);
        } else {
            createParameters.withTenantSettings(tenantSettings);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withSubnetId(String subnetId) {
        if (isInCreateMode()) {
            createParameters.withSubnetId(subnetId);
        } else {
            createParameters.withSubnetId(subnetId);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withStaticIP(String staticIP) {
        if (isInCreateMode()) {
            createParameters.withStaticIP(staticIP);
        } else {
            createParameters.withStaticIP(staticIP);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withShardCount(int shardCount) {
        if (isInCreateMode()) {
            createParameters.withShardCount(shardCount);
        } else {
            createParameters.withShardCount(shardCount);
        }
        return this;
    }

    @Override
    public RedisCacheImpl withSku(SkuName skuName, SkuFamily skuFamily) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(skuName).withFamily(skuFamily));
        } else {
            updateParameters.withSku(new Sku().withName(skuName).withFamily(skuFamily));
        }
        return this;
    }

    @Override
    public RedisCacheImpl withSku(SkuName skuName, SkuFamily skuFamily, int capacity) {
        if (isInCreateMode()) {
            createParameters.withSku(
                    new Sku()
                            .withName(skuName)
                            .withFamily(skuFamily)
                            .withCapacity(capacity));
        } else {
            updateParameters.withSku(
                    new Sku()
                            .withName(skuName)
                            .withFamily(skuFamily)
                            .withCapacity(capacity));
        }
        return this;
    }

    private void clearWrapperProperties() {
        /*accountStatuses = null;
        publicEndpoints = null;*/
    }

    @Override
    public RedisCacheImpl update() {
        updateParameters = new RedisUpdateParametersInner();
        return super.update();
    }

    @Override
    public Observable<RedisCache> applyAsync() {
        return client.updateAsync(resourceGroupName(), name(), updateParameters)
                .map(innerToFluentMap(this));
    }
    @Override
    public Observable<RedisCache> createResourceAsync() {
        createParameters.withLocation(this.regionName());
        createParameters.withTags(this.inner().getTags());
        return this.client.createAsync(this.resourceGroupName(), this.name(), createParameters)
                .map(innerToFluentMap(this))
                .doOnNext(new Action1<RedisCache>() {
                    @Override
                    public void call(RedisCache redisCache) {
                        clearWrapperProperties();
                    }
                });

    }
}
