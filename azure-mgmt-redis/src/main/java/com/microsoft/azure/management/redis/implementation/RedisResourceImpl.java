/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.management.redis.RedisResource;
import com.microsoft.azure.management.redis.Sku;
import com.microsoft.azure.management.redis.SkuFamily;
import com.microsoft.azure.management.redis.SkuName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Action1;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
class RedisResourceImpl
        extends GroupableResourceImpl<
            RedisResource,
            RedisResourceWithAccessKeyInner,
            RedisResourceImpl,
            RedisManager>
        implements
        RedisResource,
        RedisResource.Definition,
        RedisResource.Update {

    /*private PublicEndpoints publicEndpoints;
    private AccountStatuses accountStatuses;
    private List<StorageAccountKey> cachedAccountKeys;
    private StorageAccountUpdateParametersInner updateParameters;*/

    private RedisCreateOrUpdateParametersInner createParameters;
    private RedisCreateOrUpdateParametersInner updateParameters;
    private final RedisInner client;

    RedisResourceImpl(String name,
                      RedisResourceWithAccessKeyInner innerModel,
                      final RedisInner client,
                      final RedisManager storageManager) {
        super(name, innerModel, storageManager);
        this.createParameters = new RedisCreateOrUpdateParametersInner();
        this.client = client;
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public RedisResourceImpl refresh() throws Exception {
        ServiceResponse<RedisResourceInner> response =
                this.client.get(this.resourceGroupName(), this.name());
        RedisResourceInner redisResourceInner = response.getBody();
        this.setInner((RedisResourceWithAccessKeyInner) redisResourceInner);
        clearWrapperProperties();
        return this;
    }

    @Override
    public RedisResourceImpl withSku(SkuName skuName, SkuFamily skuFamily) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(skuName).withFamily(skuFamily));
        } else {
            updateParameters.withSku(new Sku().withName(skuName).withFamily(skuFamily));
        }
        return this;
    }

    @Override
    public RedisResourceImpl withSku(SkuName skuName, SkuFamily skuFamily, int capacity) {
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
    public RedisResourceImpl update() {
        updateParameters = new RedisCreateOrUpdateParametersInner();
        return super.update();
    }

    @Override
    public Observable<RedisResource> createResourceAsync() {
        /*createParameters.withLocation(this.regionName());
        createParameters.withTags(this.inner().getTags());
        ServiceResponse<RedisResourceWithAccessKeyInner> redisResourceWithAccesKeyInner =
                this.client.createOrUpdate(this.resourceGroupName(), this.name(), createParameters);
        RedisResourceWithAccessKeyInner body = redisResourceWithAccesKeyInner.getBody();
        this.setInner(body);
        clearWrapperProperties();
        return this;*/

        createParameters.withLocation(this.regionName());
        createParameters.withTags(this.inner().getTags());
        return this.client.createOrUpdateAsync(this.resourceGroupName(), this.name(), createParameters)
                .map(innerToFluentMap(this))
                .doOnNext(new Action1<RedisResource>() {
                    @Override
                    public void call(RedisResource redisResource) {
                        clearWrapperProperties();
                    }
                });

    }

    @Override
    public Observable<RedisResource> applyAsync() {
        return null;
    }

    /*@Override
    public Resource createResource() throws Exception {

    }*/
}
