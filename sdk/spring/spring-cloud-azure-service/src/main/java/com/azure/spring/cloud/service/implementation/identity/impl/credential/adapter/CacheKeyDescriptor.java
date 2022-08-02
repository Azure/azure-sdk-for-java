package com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter;

public interface CacheKeyDescriptor<KEY, KEYContext> {

    KEY getCacheKey(KEYContext keyContext);

}
