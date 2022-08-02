package com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter;

/**
 *
 * @param <KEY>
 * @param <KEYContext>
 */
public interface CacheKeyDescriptor<KEY, KEYContext> {

    KEY getCacheKey(KEYContext keyContext);
}
