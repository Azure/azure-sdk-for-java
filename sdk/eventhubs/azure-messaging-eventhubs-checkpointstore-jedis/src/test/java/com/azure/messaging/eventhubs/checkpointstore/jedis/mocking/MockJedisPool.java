// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis.mocking;

import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import org.apache.commons.pool2.impl.DefaultPooledObjectInfo;
import org.apache.commons.pool2.impl.EvictionPolicy;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.Set;

public class MockJedisPool extends JedisPool {
    private final Jedis jedis;

    public MockJedisPool(Jedis jedis) {
        super();
        this.jedis = jedis;
    }

    @Override
    public Jedis getResource() {
        return jedis;
    }

    @Override
    public void setMessagesStatistics(boolean messagesDetails) {
    }

    @Override
    public void setEvictionPolicy(EvictionPolicy<Jedis> evictionPolicy) {
    }

    @Override
    protected void setConfig(BaseObjectPoolConfig<Jedis> config) {
    }

    @Override
    public void setAbandonedConfig(AbandonedConfig abandonedConfig) {
    }

    @Override
    protected void markReturningState(PooledObject<Jedis> pooledObject) {
    }

    @Override
    public boolean isAbandonedConfig() {
        return false;
    }

    @Override
    public Duration getRemoveAbandonedTimeoutDuration() {
        return Duration.ZERO;
    }

    @Override
    public int getRemoveAbandonedTimeout() {
        return 0;
    }

    @Override
    public boolean getRemoveAbandonedOnMaintenance() {
        return false;
    }

    @Override
    public boolean getRemoveAbandonedOnBorrow() {
        return false;
    }

    @Override
    public boolean getMessageStatistics() {
        return false;
    }

    @Override
    public boolean getLogAbandoned() {
        return false;
    }

    @Override
    public EvictionPolicy<Jedis> getEvictionPolicy() {
        return null;
    }

    @Override
    public void use(Jedis pooledObject) {
    }

    @Override
    protected void toStringAppendFields(StringBuilder builder) {
    }

    @Override
    public void setMinIdle(int minIdle) {
    }

    @Override
    public void setMaxIdle(int maxIdle) {
    }

    @Override
    public void setConfig(GenericObjectPoolConfig<Jedis> conf) {
    }

    @Override
    public void returnObject(Jedis obj) {
    }

    @Override
    public void preparePool() throws Exception {
    }

    @Override
    public Set<DefaultPooledObjectInfo> listAllObjects() {
        return null;
    }

    @Override
    public void invalidateObject(Jedis obj, DestroyMode destroyMode) throws Exception {
    }

    @Override
    public void invalidateObject(Jedis obj) throws Exception {
    }

    @Override
    public int getNumWaiters() {
        return 0;
    }

    @Override
    public int getNumIdle() {
        return 0;
    }

    @Override
    public int getNumActive() {
        return 0;
    }

    @Override
    public int getMinIdle() {
        return 0;
    }

    @Override
    public int getMaxIdle() {
        return 0;
    }

    @Override
    public String getFactoryType() {
        return "";
    }

    @Override
    public PooledObjectFactory<Jedis> getFactory() {
        return null;
    }

    @Override
    public void evict() throws Exception {
    }

    @Override
    public void clear() {
    }

    @Override
    public Jedis borrowObject(long borrowMaxWaitMillis) throws Exception {
        return null;
    }

    @Override
    public Jedis borrowObject(Duration borrowMaxWaitDuration) throws Exception {
        return null;
    }

    @Override
    public Jedis borrowObject() throws Exception {
        return null;
    }

    @Override
    public void addObject() throws Exception {
    }

    @Override
    public void addObjects(int count) {
    }

    @Override
    public void returnBrokenResource(Jedis resource) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void close() {
    }

    @Override
    public void returnResource(Jedis resource) {
    }
}
