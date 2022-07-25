// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.jedis.implementation.authentication.AccessTokenCache;
import redis.clients.jedis.*;
import redis.clients.jedis.args.*;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.*;
import redis.clients.jedis.resps.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Jedis Client to connect to Azure Redis Cache.
 * NOTE: This class only overrides, Jedis, Server and Database Commands.
 */
public class AzureJedisClient extends Jedis {
    private ApiManager apiManager;
    private Jedis jedis;
    private HostAndPort hostAndPort;
    private ClientLogger clientLogger = new ClientLogger(AzureJedisClient.class);

    AzureJedisClient(final String host, final int port, String username, TokenCredential tokenCredential, RetryOptions retryOptions) {
        jedis = new Jedis(host, port);
        hostAndPort = new HostAndPort(host, port);
        this.apiManager = new ApiManager(new Authenticator(username, new AccessTokenCache(tokenCredential)), retryOptions);
    }

    AzureJedisClient(final String host, final int port, String username, String password, RetryOptions retryOptions) {
        jedis = new Jedis(host, port);
        hostAndPort = new HostAndPort(host, port);
        this.apiManager = new ApiManager(new Authenticator(username, password), retryOptions);
    }

    void resetClientIfBroken() {
        if (jedis.isBroken()) {
            resetClient();
        }
    }

    void resetClient() {
        jedis.close();
        jedis = new Jedis(hostAndPort);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBroken() {
        return jedis.isBroken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String set(final String key, final String value) {
        return apiManager.execute(() -> jedis.set(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String set(String key, String value, SetParams params) {
        return apiManager.execute(() -> jedis.set(key, value, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public String get(final String key) {
        return apiManager.execute(() -> jedis.get(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String getDel(String key) {
        return apiManager.execute(() -> jedis.getDel(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String getEx(String key, GetExParams params) {
        return apiManager.execute(() -> jedis.getEx(key, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean setbit(String key, long offset, boolean value) {
        return apiManager.execute(() -> jedis.setbit(key, offset, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean getbit(String key, long offset) {
        return apiManager.execute(() -> jedis.getbit(key, offset), this);
    }

    /**
     * {@inheritDoc}
     */
    public long setrange(String key, long offset, String value) {
        return apiManager.execute(() -> jedis.setrange(key, offset, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String getrange(String key, long startOffset, long endOffset) {
        return apiManager.execute(() -> jedis.getrange(key, startOffset, endOffset), this);
    }

    /**
     * {@inheritDoc}
     */
    public String getSet(String key, String value) {
        return apiManager.execute(() -> jedis.getSet(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public long setnx(String key, String value) {
        return apiManager.execute(() -> jedis.setnx(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String setex(String key, long seconds, String value) {
        return apiManager.execute(() -> jedis.setex(key, seconds, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String psetex(String key, long milliseconds, String value) {
        return apiManager.execute(() -> jedis.psetex(key, milliseconds, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> mget(String... keys) {
        return apiManager.execute(() -> jedis.mget(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public String mset(String... keysvalues) {
        return apiManager.execute(() -> jedis.mset(keysvalues), this);
    }

    /**
     * {@inheritDoc}
     */
    public long msetnx(String... keysvalues) {
        return apiManager.execute(() -> jedis.msetnx(keysvalues), this);
    }

    /**
     * {@inheritDoc}
     */
    public long incr(String key) {
        return apiManager.execute(() -> jedis.incr(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long incrBy(String key, long increment) {
        return apiManager.execute(() -> jedis.incrBy(key, increment), this);
    }

    /**
     * {@inheritDoc}
     */
    public double incrByFloat(String key, double increment) {
        return apiManager.execute(() -> jedis.incrByFloat(key, increment), this);
    }

    /**
     * {@inheritDoc}
     */
    public long decr(String key) {
        return apiManager.execute(() -> jedis.decr(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long decrBy(String key, long decrement) {
        return apiManager.execute(() -> jedis.decrBy(key, decrement), this);
    }

    /**
     * {@inheritDoc}
     */
    public long append(String key, String value) {
        return apiManager.execute(() -> jedis.append(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String substr(String key, int start, int end) {
        return apiManager.execute(() -> jedis.substr(key, start, end), this);
    }

    /**
     * {@inheritDoc}
     */
    public long strlen(String key) {
        return apiManager.execute(() -> jedis.strlen(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long bitcount(String key) {
        return apiManager.execute(() -> jedis.bitcount(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long bitcount(String key, long start, long end) {
        return apiManager.execute(() -> jedis.bitcount(key, start, end), this);
    }

    /**
     * {@inheritDoc}
     */
    public long bitpos(String key, boolean value) {
        return apiManager.execute(() -> jedis.bitpos(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public long bitpos(String key, boolean value, BitPosParams params) {
        return apiManager.execute(() -> jedis.bitpos(key, value, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Long> bitfield(String key, String... arguments) {
        return apiManager.execute(() -> jedis.bitfield(key, arguments), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Long> bitfieldReadonly(String key, String... arguments) {
        return apiManager.execute(() -> jedis.bitfieldReadonly(key, arguments), this);
    }

    /**
     * {@inheritDoc}
     */
    public long bitop(BitOP op, String destKey, String... srcKeys) {
        return apiManager.execute(() -> jedis.bitop(op, destKey, srcKeys), this);
    }

    /**
     * {@inheritDoc}
     */
    public LCSMatchResult strAlgoLCSKeys(String keyA, String keyB, StrAlgoLCSParams params) {
        return apiManager.execute(() -> jedis.strAlgoLCSKeys(keyA, keyB, params), this);
    }


    /**
     * {@inheritDoc}
     */
    public boolean copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
        return apiManager.execute(() -> jedis.copy(srcKey, dstKey, db, replace), this);
    }

    @Override
    public String migrate(String host, int port, byte[] key, int destinationDB, int timeout) {
        return apiManager.execute(() -> jedis.migrate(host, port, key, destinationDB, timeout), this);
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
        return apiManager.execute(() -> jedis.migrate(host, port, destinationDB, timeout, params, keys), this);
    }

    @Override
    public String migrate(String host, int port, String key, int destinationDB, int timeout) {
        return apiManager.execute(() -> jedis.migrate(host, port, key, destinationDB, timeout), this);
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
        return apiManager.execute(() -> jedis.migrate(host, port, destinationDB, timeout, params, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        return apiManager.execute(() -> jedis.copy(srcKey, dstKey, replace), this);   }


    /**
     * {@inheritDoc}
     */
    public String ping() {
        return apiManager.execute(() -> jedis.ping(), this);
    }

    /**
     * {@inheritDoc}
     */
    public String ping(String message) {
        return apiManager.execute(() -> jedis.ping(message), this);
    }

    /**
     * {@inheritDoc}
     */
    public String echo(String string) {
        return apiManager.execute(() -> jedis.echo(string), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] echo(byte[] arg) {
        return new byte[0];
    }

    /**
     * {@inheritDoc}
     */
    public String quit() {
        return apiManager.execute(() -> jedis.quit(), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] ping(final byte[] message) {
        return apiManager.execute(() -> jedis.ping(message), this);
    }


    /**
     * {@inheritDoc}
     */
    public String set(final byte[] key, final byte[] value) {
        return apiManager.execute(() -> jedis.set(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String set(final byte[] key, final byte[] value, final SetParams params) {
        return apiManager.execute(() -> jedis.set(key, value, params), this);
    }


    /**
     * {@inheritDoc}
     */
    public byte[] get(final byte[] key) {
        return apiManager.execute(() -> jedis.get(key), this);
    }


    /**
     * {@inheritDoc}
     */
    public byte[] getDel(final byte[] key) {
        return apiManager.execute(() -> jedis.getDel(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getEx(final byte[] key, final GetExParams params) {
        return apiManager.execute(() -> jedis.getEx(key, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public long exists(final byte[]... keys) {
        return apiManager.execute(() -> jedis.exists(keys), this);   }

    /**
     * {@inheritDoc}
     */
    public boolean exists(final byte[] key) {
        return apiManager.execute(() -> jedis.exists(key), this);  }


    /**
     * {@inheritDoc}
     */
    public long del(final byte[]... keys) {
        return apiManager.execute(() -> jedis.del(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long del(final byte[] key) {
        return apiManager.execute(() -> jedis.del(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long unlink(final byte[]... keys) {
        return apiManager.execute(() -> jedis.unlink(keys), this);
    }


    /**
     * {@inheritDoc}
     */
    public String type(byte[] key) {
        return apiManager.execute(() -> jedis.type(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String flushDB() {
        return apiManager.execute(() -> jedis.flushDB(), this);
    }

    /**
     * {@inheritDoc}
     */
    public String flushDB(FlushMode flushMode) {
        return apiManager.execute(() -> jedis.flushDB(flushMode), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<byte[]> keys(byte[] pattern) {
        return apiManager.execute(() -> jedis.keys(pattern), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] randomBinaryKey() {
        return apiManager.execute(() -> jedis.randomBinaryKey(), this);

    }

    /**
     * {@inheritDoc}
     */
    public String rename(byte[] oldkey, byte[] newkey) {
        return apiManager.execute(() -> jedis.rename(oldkey, newkey), this);
    }

    /**
     * {@inheritDoc}
     */
    public long renamenx(byte[] oldkey, byte[] newkey) {
        return apiManager.execute(() -> jedis.renamenx(oldkey, newkey), this);
    }

    /**
     * {@inheritDoc}
     */
    public long dbSize() {
        return apiManager.execute(() -> jedis.dbSize(), this);
    }

    /**
     * {@inheritDoc}
     */
    public long expire(byte[] key, long seconds) {
        return apiManager.execute(() -> jedis.expire(key, seconds), this);
    }

    /**
     * {@inheritDoc}
     */
    public long expireAt(byte[] key, long unixTime) {
        return apiManager.execute(() -> jedis.expireAt(key, unixTime), this);
    }

    /**
     * {@inheritDoc}
     */
    public long ttl(byte[] key) {
        return apiManager.execute(() -> jedis.ttl(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long touch(byte[]... keys) {
        return apiManager.execute(() -> jedis.touch(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long touch(byte[] key) {
        return apiManager.execute(() -> jedis.touch(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String select(int index) {
        return apiManager.execute(() -> jedis.select(index), this);
    }

    /**
     * {@inheritDoc}
     */
    public String swapDB(int index1, int index2) {
        return apiManager.execute(() -> jedis.swapDB(index1, index2), this);
    }

    @Override
    public long move(String key, int dbIndex) {
        return apiManager.execute(() -> jedis.move(key, dbIndex), this);
    }

    /**
     * {@inheritDoc}
     */
    public long move(byte[] key, int dbIndex) {
        return apiManager.execute(() -> jedis.move(key, dbIndex), this);
    }

    @Override
    public boolean copy(String srcKey, String dstKey, int db, boolean replace) {
        return apiManager.execute(() -> jedis.copy(srcKey, dstKey, db, replace), this);
    }

    /**
     * {@inheritDoc}
     */
    public String flushAll() {
        return apiManager.execute(() -> jedis.flushAll(), this);
    }

    /**
     * {@inheritDoc}
     */
    public String flushAll(FlushMode flushMode) {
        return apiManager.execute(() -> jedis.flushAll(flushMode), this);
    }

    String authenticate(String username, String password) {
        return jedis.auth(username, password);
    }

    /**
     * {@inheritDoc}
     */
    public String auth(String password) {
        throw clientLogger.logExceptionAsError(new UnsupportedOperationException("Operation not supported"));
    }

    /**
     * {@inheritDoc}
     */
    public String auth(String user, String password) {
        throw clientLogger.logExceptionAsError(new UnsupportedOperationException("Operation not supported"));
    }

    /**
     * {@inheritDoc}
     */
    public String save() {
        return apiManager.execute(() -> jedis.save(), this);
    }

    /**
     * {@inheritDoc}
     */
    public String bgsave() {
        return apiManager.execute(() -> jedis.bgsave(), this);
    }

    /**
     * {@inheritDoc}
     */
    public String bgrewriteaof() {
        return apiManager.execute(() -> jedis.bgrewriteaof(), this);
    }

    /**
     * {@inheritDoc}
     */
    public long lastsave() {
        return apiManager.execute(() -> jedis.lastsave(), this);
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() throws JedisException {

    }

    /**
     * {@inheritDoc}
     */
    public void shutdown(SaveMode saveMode) throws JedisException {

    }

    /**
     * {@inheritDoc}
     */
    public String info() {
        return apiManager.execute(() -> jedis.info(), this);
    }

    /**
     * {@inheritDoc}
     */
    public String info(String section) {
        return apiManager.execute(() -> jedis.info(section), this);
    }

    /**
     * {@inheritDoc}
     */
    public String slaveof(String host, int port) {
        return apiManager.execute(() -> jedis.slaveof(host, port), this);
    }

    /**
     * {@inheritDoc}
     */
    public String slaveofNoOne() {
        return apiManager.execute(() -> jedis.slaveofNoOne(), this);
    }

    /**
     * {@inheritDoc}
     */
    public long waitReplicas(int replicas, long timeout) {
        return apiManager.execute(() -> jedis.waitReplicas(replicas, timeout), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getSet(byte[] key, byte[] value) {
        return apiManager.execute(() -> jedis.getSet(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<byte[]> mget(byte[]... keys) {
        return apiManager.execute(() -> jedis.mget(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long setnx(byte[] key, byte[] value) {
        return apiManager.execute(() -> jedis.setnx(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String setex(byte[] key, long seconds, byte[] value) {
        return apiManager.execute(() -> jedis.setex(key, seconds, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String mset(byte[]... keysvalues) {
        return apiManager.execute(() -> jedis.mset(keysvalues), this);
    }

    /**
     * {@inheritDoc}
     */
    public long msetnx(byte[]... keysvalues) {
        return apiManager.execute(() -> jedis.msetnx(keysvalues), this);
    }

    /**
     * {@inheritDoc}
     */
    public long decrBy(byte[] key, long decrement) {
        return apiManager.execute(() -> jedis.decrBy(key, decrement), this);
    }

    /**
     * {@inheritDoc}
     */
    public long decr(byte[] key) {
        return apiManager.execute(() -> jedis.decr(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long incrBy(byte[] key, long increment) {
        return apiManager.execute(() -> jedis.incrBy(key, increment), this);
    }

    /**
     * {@inheritDoc}
     */
    public double incrByFloat(byte[] key, double increment) {
        return apiManager.execute(() -> jedis.incrByFloat(key, increment), this);
    }

    /**
     * {@inheritDoc}
     */
    public long incr(byte[] key) {
        return apiManager.execute(() -> jedis.incr(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long append(byte[] key, byte[] value) {
        return apiManager.execute(() -> jedis.append(key, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] substr(byte[] key, int start, int end) {
        return apiManager.execute(() -> jedis.substr(key, start, end), this);

    }

    /**
     * {@inheritDoc}
     */
    public long hset(byte[] key, byte[] field, byte[] value) {
        return apiManager.execute(() -> jedis.hset(key, field, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hset(byte[] key, Map<byte[], byte[]> hash) {
        return apiManager.execute(() -> jedis.hset(key, hash), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] hget(byte[] key, byte[] field) {
        return apiManager.execute(() -> jedis.hget(key, field), this);

    }

    /**
     * {@inheritDoc}
     */
    public long hsetnx(byte[] key, byte[] field, byte[] value) {
        return apiManager.execute(() -> jedis.hsetnx(key, field, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return apiManager.execute(() -> jedis.hmset(key, hash), this);
    }

    /**
     * {@inheritDoc}
     */
    public long geoadd(String key, double longitude, double latitude, String member) {
        return apiManager.execute(() -> jedis.geoadd(key, longitude, latitude, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return apiManager.execute(() -> jedis.geoadd(key, memberCoordinateMap), this);
    }

    /**
     * {@inheritDoc}
     */
    public long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        return apiManager.execute(() -> jedis.geoadd(key, params, memberCoordinateMap), this);
    }

    /**
     * {@inheritDoc}
     */
    public Double geodist(String key, String member1, String member2) {
        return apiManager.execute(() -> jedis.geodist(key, member1, member2), this);
    }

    /**
     * {@inheritDoc}
     */
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return apiManager.execute(() -> jedis.geodist(key, member1, member2), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> geohash(String key, String... members) {
        return apiManager.execute(() -> jedis.geohash(key, members), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoCoordinate> geopos(String key, String... members) {
        return apiManager.execute(() -> jedis.geopos(key, members), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadius(key, longitude, latitude, radius, unit), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadius(key, longitude, latitude, radius, unit, param), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadiusByMember(key, member, radius, unit), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadiusByMemberReadonly(key, member, radius, unit), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadiusByMember(key, member, radius, unit, param), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadiusByMemberReadonly(key, member, radius, unit, param), this);
    }

    /**
     * {@inheritDoc}
     */
    public long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return apiManager.execute(() -> jedis.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam), this);
    }

    /**
     * {@inheritDoc}
     */
    public long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return apiManager.execute(() -> jedis.georadiusByMemberStore(key, member, radius, unit, param, storeParam), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hset(String key, String field, String value) {
        return apiManager.execute(() -> jedis.hset(key, field, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hset(String key, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.hset(key, hash), this);
    }

    /**
     * {@inheritDoc}
     */
    public String hget(String key, String field) {
        return apiManager.execute(() -> jedis.hget(key, field), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hsetnx(String key, String field, String value) {
        return apiManager.execute(() -> jedis.hsetnx(key, field, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String hmset(String key, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.hmset(key, hash), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> hmget(String key, String... fields) {
        return apiManager.execute(() -> jedis.hmget(key, fields), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hincrBy(String key, String field, long value) {
        return apiManager.execute(() -> jedis.hincrBy(key, field, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public double hincrByFloat(String key, String field, double value) {
        return apiManager.execute(() -> jedis.hincrByFloat(key, field, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hexists(String key, String field) {
        return apiManager.execute(() -> jedis.hexists(key, field), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hdel(String key, String... field) {
        return apiManager.execute(() -> jedis.hdel(key, field), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hlen(String key) {
        return apiManager.execute(() -> jedis.hlen(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> hkeys(String key) {
        return apiManager.execute(() -> jedis.hkeys(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> hvals(String key) {
        return apiManager.execute(() -> jedis.hvals(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> hgetAll(String key) {
        return apiManager.execute(() -> jedis.hgetAll(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String hrandfield(String key) {
        return apiManager.execute(() -> jedis.hrandfield(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> hrandfield(String key, long count) {
        return apiManager.execute(() -> jedis.hrandfield(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> hrandfieldWithValues(String key, long count) {
        return apiManager.execute(() -> jedis.hrandfieldWithValues(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.hscan(key, cursor, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public long hstrlen(String key, String field) {
        return apiManager.execute(() -> jedis.hstrlen(key, field), this);
    }

    /**
     * {@inheritDoc}
     */
    public long pfadd(String key, String... elements) {
        return apiManager.execute(() -> jedis.pfadd(key, elements), this);
    }

    /**
     * {@inheritDoc}
     */
    public String pfmerge(String destkey, String... sourcekeys) {
        return apiManager.execute(() -> jedis.pfmerge(destkey, sourcekeys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long pfcount(String key) {
        return apiManager.execute(() -> jedis.pfcount(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long pfcount(String... keys) {
        return apiManager.execute(() -> jedis.pfcount(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(String key) {
        return apiManager.execute(() -> jedis.exists(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long exists(String... keys) {
        return apiManager.execute(() -> jedis.exists(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long persist(String key) {
        return apiManager.execute(() -> jedis.persist(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String type(String key) {
        return apiManager.execute(() -> jedis.type(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] dump(String key) {
        return new byte[0];
    }

    /**
     * {@inheritDoc}
     */
    public String restore(String key, long ttl, byte[] serializedValue) {
        return apiManager.execute(() -> jedis.restore(key, ttl, serializedValue), this);
    }

    /**
     * {@inheritDoc}
     */
    public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        return apiManager.execute(() -> jedis.restore(key, ttl, serializedValue, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public long expire(String key, long seconds) {
        return apiManager.execute(() -> jedis.expire(key, seconds), this);
    }

    /**
     * {@inheritDoc}
     */
    public long pexpire(String key, long milliseconds) {
        return apiManager.execute(() -> jedis.pexpire(key, milliseconds), this);
    }

    /**
     * {@inheritDoc}
     */
    public long expireAt(String key, long unixTime) {
        return apiManager.execute(() -> jedis.expireAt(key, unixTime), this);
    }

    /**
     * {@inheritDoc}
     */
    public long pexpireAt(String key, long millisecondsTimestamp) {
        return apiManager.execute(() -> jedis.pexpireAt(key, millisecondsTimestamp), this);
    }

    /**
     * {@inheritDoc}
     */
    public long ttl(String key) {
        return apiManager.execute(() -> jedis.ttl(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long pttl(String key) {
        return apiManager.execute(() -> jedis.pttl(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long touch(String key) {
        return apiManager.execute(() -> jedis.touch(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long touch(String... keys) {
        return apiManager.execute(() -> jedis.touch(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> sort(String key) {
        return apiManager.execute(() -> jedis.sort(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long sort(String key, String dstkey) {
        return apiManager.execute(() -> jedis.sort(key, dstkey), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> sort(String key, SortingParams sortingParameters) {
        return apiManager.execute(() -> jedis.sort(key, sortingParameters), this);
    }

    /**
     * {@inheritDoc}
     */
    public long sort(String key, SortingParams sortingParameters, String dstkey) {
        return apiManager.execute(() -> jedis.sort(key, sortingParameters, dstkey), this);
    }

    /**
     * {@inheritDoc}
     */
    public long del(String key) {
        return apiManager.execute(() -> jedis.del(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long del(String... keys) {
        return apiManager.execute(() -> jedis.del(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long unlink(String key) {
        return apiManager.execute(() -> jedis.unlink(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long unlink(String... keys) {
        return apiManager.execute(() -> jedis.unlink(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(String srcKey, String dstKey, boolean replace) {
        return apiManager.execute(() -> jedis.copy(srcKey, dstKey, replace), this);
    }

    /**
     * {@inheritDoc}
     */
    public String rename(String oldkey, String newkey) {
        return apiManager.execute(() -> jedis.rename(oldkey, newkey), this);
    }

    /**
     * {@inheritDoc}
     */
    public long renamenx(String oldkey, String newkey) {
        return apiManager.execute(() -> jedis.renamenx(oldkey, newkey), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long memoryUsage(String key) {
        return apiManager.execute(() -> jedis.memoryUsage(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long memoryUsage(String key, int samples) {
        return apiManager.execute(() -> jedis.memoryUsage(key, samples), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long objectRefcount(String key) {
        return apiManager.execute(() -> jedis.objectRefcount(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String objectEncoding(String key) {
        return apiManager.execute(() -> jedis.objectEncoding(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long objectIdletime(String key) {
        return apiManager.execute(() -> jedis.objectIdletime(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long objectFreq(String key) {
        return apiManager.execute(() -> jedis.objectFreq(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public String migrate(String host, int port, String key, int timeout) {
        return apiManager.execute(() -> jedis.migrate(host, port, key, timeout), this);
    }

    /**
     * {@inheritDoc}
     */
    public String migrate(String host, int port, int timeout, MigrateParams params, String... keys) {
        return apiManager.execute(() -> jedis.migrate(host, port, timeout, params, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> keys(String pattern) {
        return apiManager.execute(() -> jedis.keys(pattern), this);
    }

    /**
     * {@inheritDoc}
     */
    public ScanResult<String> scan(String cursor) {
        return apiManager.execute(() -> jedis.scan(cursor), this);
    }

    /**
     * {@inheritDoc}
     */
    public ScanResult<String> scan(String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.scan(cursor, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public ScanResult<String> scan(String cursor, ScanParams params, String type) {
        return apiManager.execute(() -> jedis.scan(cursor, params, type), this);
    }

    /**
     * {@inheritDoc}
     */
    public String randomKey() {
        return apiManager.execute(() -> jedis.randomKey(), this);
    }

    /**
     * {@inheritDoc}
     */
    public long rpush(String key, String... string) {
        return apiManager.execute(() -> jedis.rpush(key, string), this);
    }

    /**
     * {@inheritDoc}
     */
    public long lpush(String key, String... string) {
        return apiManager.execute(() -> jedis.lpush(key, string), this);
    }

    /**
     * {@inheritDoc}
     */
    public long llen(String key) {
        return apiManager.execute(() -> jedis.llen(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> lrange(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.lrange(key, start, stop), this);
    }

    /**
     * {@inheritDoc}
     */
    public String ltrim(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.ltrim(key, start, stop), this);
    }

    /**
     * {@inheritDoc}
     */
    public String lindex(String key, long index) {
        return apiManager.execute(() -> jedis.lindex(key, index), this);
    }

    /**
     * {@inheritDoc}
     */
    public String lset(String key, long index, String value) {
        return apiManager.execute(() -> jedis.lset(key, index, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public long lrem(String key, long count, String value) {
        return apiManager.execute(() -> jedis.lrem(key, count, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public String lpop(String key) {
        return apiManager.execute(() -> jedis.lpop(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> lpop(String key, int count) {
        return apiManager.execute(() -> jedis.lpop(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long lpos(String key, String element) {
        return apiManager.execute(() -> jedis.lpos(key, element), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long lpos(String key, String element, LPosParams params) {
        return apiManager.execute(() -> jedis.lpos(key, element, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Long> lpos(String key, String element, LPosParams params, long count) {
        return apiManager.execute(() -> jedis.lpos(key, element, params, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public String rpop(String key) {
        return apiManager.execute(() -> jedis.rpop(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> rpop(String key, int count) {
        return apiManager.execute(() -> jedis.rpop(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public long linsert(String key, ListPosition where, String pivot, String value) {
        return apiManager.execute(() -> jedis.linsert(key, where, pivot, value), this);
    }

    /**
     * {@inheritDoc}
     */
    public long lpushx(String key, String... string) {
        return apiManager.execute(() -> jedis.lpushx(key, string), this);
    }

    /**
     * {@inheritDoc}
     */
    public long rpushx(String key, String... string) {
        return apiManager.execute(() -> jedis.rpushx(key, string), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> blpop(int timeout, String key) {
        return apiManager.execute(() -> jedis.blpop(timeout, key), this);
    }

    /**
     * {@inheritDoc}
     */
    public KeyedListElement blpop(double timeout, String key) {
        return apiManager.execute(() -> jedis.blpop(timeout, key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> brpop(int timeout, String key) {
        return apiManager.execute(() -> jedis.brpop(timeout, key), this);
    }

    /**
     * {@inheritDoc}
     */
    public KeyedListElement brpop(double timeout, String key) {
        return apiManager.execute(() -> jedis.brpop(timeout, key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> blpop(int timeout, String... keys) {
        return apiManager.execute(() -> jedis.blpop(timeout, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public KeyedListElement blpop(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.blpop(timeout, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> brpop(int timeout, String... keys) {
        return apiManager.execute(() -> jedis.brpop(timeout, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public KeyedListElement brpop(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.brpop(timeout, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public String rpoplpush(String srckey, String dstkey) {
        return apiManager.execute(() -> jedis.rpoplpush(srckey, dstkey), this);
    }

    /**
     * {@inheritDoc}
     */
    public String brpoplpush(String source, String destination, int timeout) {
        return apiManager.execute(() -> jedis.brpoplpush(source, destination, timeout), this);
    }

    /**
     * {@inheritDoc}
     */
    public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        return apiManager.execute(() -> jedis.lmove(srcKey, dstKey, from, to), this);
    }

    /**
     * {@inheritDoc}
     */
    public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        return apiManager.execute(() -> jedis.blmove(srcKey, dstKey, from, to, timeout), this);
    }

    /**
     * {@inheritDoc}
     */
    public Object eval(String script) {
        return apiManager.execute(() -> jedis.eval(script), this);
    }

    /**
     * {@inheritDoc}
     */
    public Object eval(String script, int keyCount, String... params) {
        return apiManager.execute(() -> jedis.eval(script, keyCount, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public Object eval(String script, List<String> keys, List<String> args) {
        return apiManager.execute(() -> jedis.eval(script, keys, args), this);
    }

    /**
     * {@inheritDoc}
     */
    public Object evalsha(String sha1) {
        return apiManager.execute(() -> jedis.evalsha(sha1), this);
    }

    /**
     * {@inheritDoc}
     */
    public Object evalsha(String sha1, int keyCount, String... params) {
        return apiManager.execute(() -> jedis.evalsha(sha1, keyCount, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return apiManager.execute(() -> jedis.evalsha(sha1, keys, args), this);
    }

    /**
     * {@inheritDoc}
     */
    public long sadd(String key, String... member) {
        return apiManager.execute(() -> jedis.sadd(key, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> smembers(String key) {
        return apiManager.execute(() -> jedis.smembers(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public long srem(String key, String... member) {
        return apiManager.execute(() -> jedis.srem(key, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public String spop(String key) {
        return apiManager.execute(() -> jedis.spop(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> spop(String key, long count) {
        return apiManager.execute(() -> jedis.spop(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public long scard(String key) {
        return apiManager.execute(() -> jedis.scard(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean sismember(String key, String member) {
        return apiManager.execute(() -> jedis.sismember(key, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Boolean> smismember(String key, String... members) {
        return apiManager.execute(() -> jedis.smismember(key, members), this);
    }

    /**
     * {@inheritDoc}
     */
    public String srandmember(String key) {
        return apiManager.execute(() -> jedis.srandmember(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> srandmember(String key, int count) {
        return apiManager.execute(() -> jedis.srandmember(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.sscan(key, cursor, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> sdiff(String... keys) {
        return apiManager.execute(() -> jedis.sdiff(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long sdiffstore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.sdiffstore(dstkey, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> sinter(String... keys) {
        return apiManager.execute(() -> jedis.sinter(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long sinterstore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.sinterstore(dstkey, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> sunion(String... keys) {
        return apiManager.execute(() -> jedis.sunion(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long sunionstore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.sunionstore(dstkey, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long smove(String srckey, String dstkey, String member) {
        return apiManager.execute(() -> jedis.smove(srckey, dstkey, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zadd(String key, double score, String member) {
        return apiManager.execute(() -> jedis.zadd(key, score, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zadd(String key, double score, String member, ZAddParams params) {
        return apiManager.execute(() -> jedis.zadd(key, score, member, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zadd(String key, Map<String, Double> scoreMembers) {
        return apiManager.execute(() -> jedis.zadd(key, scoreMembers), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return apiManager.execute(() -> jedis.zadd(key, scoreMembers, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public Double zaddIncr(String key, double score, String member, ZAddParams params) {
        return apiManager.execute(() -> jedis.zaddIncr(key, score, member, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zrem(String key, String... members) {
        return apiManager.execute(() -> jedis.zrem(key, members), this);
    }

    /**
     * {@inheritDoc}
     */
    public double zincrby(String key, double increment, String member) {
        return apiManager.execute(() -> jedis.zincrby(key, increment, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        return apiManager.execute(() -> jedis.zincrby(key, increment, member, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long zrank(String key, String member) {
        return apiManager.execute(() -> jedis.zrank(key, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public Long zrevrank(String key, String member) {
        return apiManager.execute(() -> jedis.zrevrank(key, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrange(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrange(key, start, stop), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrevrange(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrevrange(key, start, stop), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrangeWithScores(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrangeWithScores(key, start, stop), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrevrangeWithScores(key, start, stop), this);
    }

    /**
     * {@inheritDoc}
     */
    public String zrandmember(String key) {
        return apiManager.execute(() -> jedis.zrandmember(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrandmember(String key, long count) {
        return apiManager.execute(() -> jedis.zrandmember(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrandmemberWithScores(String key, long count) {
        return apiManager.execute(() -> jedis.zrandmemberWithScores(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zcard(String key) {
        return apiManager.execute(() -> jedis.zcard(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public Double zscore(String key, String member) {
        return apiManager.execute(() -> jedis.zscore(key, member), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Double> zmscore(String key, String... members) {
        return apiManager.execute(() -> jedis.zmscore(key, members), this);
    }

    /**
     * {@inheritDoc}
     */
    public Tuple zpopmax(String key) {
        return apiManager.execute(() -> jedis.zpopmax(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zpopmax(String key, int count) {
        return apiManager.execute(() -> jedis.zpopmax(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public Tuple zpopmin(String key) {
        return apiManager.execute(() -> jedis.zpopmin(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zpopmin(String key, int count) {
        return apiManager.execute(() -> jedis.zpopmin(key, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zcount(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zcount(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zcount(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zcount(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrangeByScore(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrangeByScore(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrevrangeByScore(String key, double max, double min) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrevrangeByScore(String key, String max, String min) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zremrangeByRank(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zremrangeByRank(key, start, stop), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zremrangeByScore(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zremrangeByScore(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zremrangeByScore(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zremrangeByScore(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zlexcount(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zlexcount(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrangeByLex(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zrangeByLex(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByLex(key, min, max, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrevrangeByLex(String key, String max, String min) {
        return apiManager.execute(() -> jedis.zrevrangeByLex(key, max, min), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByLex(key, max, min, offset, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zremrangeByLex(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zremrangeByLex(key, min, max), this);
    }

    /**
     * {@inheritDoc}
     */
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.zscan(key, cursor, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public KeyedZSetElement bzpopmax(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.bzpopmax(timeout, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public KeyedZSetElement bzpopmin(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.bzpopmin(timeout, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> zdiff(String... keys) {
        return apiManager.execute(() -> jedis.zdiff(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Tuple> zdiffWithScores(String... keys) {
        return apiManager.execute(() -> jedis.zdiffWithScores(keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zdiffStore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.zdiffStore(dstkey, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zinterstore(String dstkey, String... sets) {
        return apiManager.execute(() -> jedis.zinterstore(dstkey, sets), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zinterstore(String dstkey, ZParams params, String... sets) {
        return apiManager.execute(() -> jedis.zinterstore(dstkey, params, sets), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> zinter(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zinter(params, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Tuple> zinterWithScores(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zinterWithScores(params, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> zunion(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zunion(params, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Tuple> zunionWithScores(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zunionWithScores(params, keys), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zunionstore(String dstkey, String... sets) {
        return apiManager.execute(() -> jedis.zunionstore(dstkey, sets), this);
    }

    /**
     * {@inheritDoc}
     */
    public long zunionstore(String dstkey, ZParams params, String... sets) {
        return apiManager.execute(() -> jedis.zunionstore(dstkey, params, sets), this);
    }

    /**
     * {@inheritDoc}
     */
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.xadd(key, id, hash), this);
    }

    /**
     * {@inheritDoc}
     */
    public StreamEntryID xadd(String key, XAddParams params, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.xadd(key, params, hash), this);
    }

    /**
     * {@inheritDoc}
     */
    public long xlen(String key) {
        return apiManager.execute(() -> jedis.xlen(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
        return apiManager.execute(() -> jedis.xrange(key, start, end), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        return apiManager.execute(() -> jedis.xrange(key, start, end, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrange(String key, String start, String end) {
        return apiManager.execute(() -> jedis.xrange(key, start, end), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrange(String key, String start, String end, int count) {
        return apiManager.execute(() -> jedis.xrange(key, start, end, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrevrange(String key, String end, String start) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xrevrange(String key, String end, String start, int count) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start, count), this);
    }

    /**
     * {@inheritDoc}
     */
    public long xack(String key, String group, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xack(key, group, ids), this);
    }

    /**
     * {@inheritDoc}
     */
    public String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
        return apiManager.execute(() -> jedis.xgroupCreate(key, groupname, id, makeStream), this);
    }

    /**
     * {@inheritDoc}
     */
    public String xgroupSetID(String key, String groupname, StreamEntryID id) {
        return apiManager.execute(() -> jedis.xgroupSetID(key, groupname, id), this);
    }


    /**
     * {@inheritDoc}
     */
    public long xgroupDestroy(String key, String groupname) {
        return apiManager.execute(() -> jedis.xgroupDestroy(key, groupname), this);
    }

    /**
     * {@inheritDoc}
     */
    public long xgroupDelConsumer(String key, String groupname, String consumername) {
        return apiManager.execute(() -> jedis.xgroupDelConsumer(key, groupname, consumername), this);
    }

    /**
     * {@inheritDoc}
     */
    public StreamPendingSummary xpending(String key, String groupname) {
        return apiManager.execute(() -> jedis.xpending(key, groupname), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end, int count, String consumername) {
        return apiManager.execute(() -> jedis.xpending(key, groupname, start, end, count, consumername), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamPendingEntry> xpending(String key, String groupname, XPendingParams params) {
        return apiManager.execute(() -> jedis.xpending(key, groupname, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public long xdel(String key, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xdel(key, ids), this);
    }

    /**
     * {@inheritDoc}
     */
    public long xtrim(String key, long maxLen, boolean approximate) {
        return apiManager.execute(() -> jedis.xtrim(key, maxLen, approximate), this);
    }

    /**
     * {@inheritDoc}
     */
    public long xtrim(String key, XTrimParams params) {
        return apiManager.execute(() -> jedis.xtrim(key, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xclaim(key, group, consumername, minIdleTime, params, ids), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamEntryID> xclaimJustId(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids), this);
    }

    /**
     * {@inheritDoc}
     */
    public Map.Entry<StreamEntryID, List<StreamEntry>> xautoclaim(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return apiManager.execute(() -> jedis.xautoclaim(key, group, consumerName, minIdleTime, start, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public Map.Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return apiManager.execute(() -> jedis.xautoclaimJustId(key, group, consumerName, minIdleTime, start, params), this);
    }

    /**
     * {@inheritDoc}
     */
    public StreamInfo xinfoStream(String key) {
        return apiManager.execute(() -> jedis.xinfoStream(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamGroupInfo> xinfoGroup(String key) {
        return apiManager.execute(() -> jedis.xinfoGroup(key), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
        return apiManager.execute(() -> jedis.xinfoConsumers(key, group), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Map.Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
        return apiManager.execute(() -> jedis.xread(xReadParams, streams), this);
    }

    /**
     * {@inheritDoc}
     */
    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        return apiManager.execute(() -> jedis.xreadGroup(groupname, consumer, xReadGroupParams, streams), this);
    }

    /**
     * {@inheritDoc}
     */
    public void close()  {
        jedis.close();
    }
}
