// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.jedis.implementation.authentication.AccessTokenCache;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.args.BitOP;
import redis.clients.jedis.args.FlushMode;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.args.SaveMode;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.BitPosParams;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.params.RestoreParams;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.SortingParams;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.params.XAutoClaimParams;
import redis.clients.jedis.params.XClaimParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.params.XTrimParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;
import redis.clients.jedis.params.ZParams;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.StreamConsumersInfo;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamInfo;
import redis.clients.jedis.resps.StreamPendingSummary;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.util.KeyValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Jedis Client to connect to Azure Redis Cache.
 * NOTE: This class only overrides, Jedis, Server and Database Commands.
 */
public class AzureJedisClient extends Jedis {
    private static final ClientLogger LOGGER = new ClientLogger(AzureJedisClient.class);

    private final ApiManager apiManager;
    private Jedis jedis;
    private final HostAndPort hostAndPort;

    AzureJedisClient(final String host, final int port, TokenCredential tokenCredential, boolean useSSL,
        RetryOptions retryOptions) {
        jedis = new Jedis(host, port, useSSL);
        hostAndPort = new HostAndPort(host, port);
        this.apiManager = new ApiManager(new Authenticator(new AccessTokenCache(tokenCredential)), retryOptions);
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

    @Override
    public boolean isBroken() {
        return jedis.isBroken();
    }

    @Override
    public String set(final String key, final String value) {
        return apiManager.execute(() -> jedis.set(key, value), this);
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return apiManager.execute(() -> jedis.set(key, value, params), this);
    }

    @Override
    public String get(final String key) {
        return apiManager.execute(() -> jedis.get(key), this);
    }

    @Override
    public String getDel(String key) {
        return apiManager.execute(() -> jedis.getDel(key), this);
    }

    @Override
    public String getEx(String key, GetExParams params) {
        return apiManager.execute(() -> jedis.getEx(key, params), this);
    }

    @Override
    public boolean setbit(String key, long offset, boolean value) {
        return apiManager.execute(() -> jedis.setbit(key, offset, value), this);
    }

    @Override
    public boolean getbit(String key, long offset) {
        return apiManager.execute(() -> jedis.getbit(key, offset), this);
    }

    @Override
    public long setrange(String key, long offset, String value) {
        return apiManager.execute(() -> jedis.setrange(key, offset, value), this);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return apiManager.execute(() -> jedis.getrange(key, startOffset, endOffset), this);
    }

    @Override
    public String getSet(String key, String value) {
        return apiManager.execute(() -> jedis.getSet(key, value), this);
    }

    @Override
    public long setnx(String key, String value) {
        return apiManager.execute(() -> jedis.setnx(key, value), this);
    }

    @Override
    public String setex(String key, long seconds, String value) {
        return apiManager.execute(() -> jedis.setex(key, seconds, value), this);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return apiManager.execute(() -> jedis.psetex(key, milliseconds, value), this);
    }

    @Override
    public List<String> mget(String... keys) {
        return apiManager.execute(() -> jedis.mget(keys), this);
    }

    @Override
    public String mset(String... keysvalues) {
        return apiManager.execute(() -> jedis.mset(keysvalues), this);
    }

    @Override
    public long msetnx(String... keysvalues) {
        return apiManager.execute(() -> jedis.msetnx(keysvalues), this);
    }

    @Override
    public long incr(String key) {
        return apiManager.execute(() -> jedis.incr(key), this);
    }

    @Override
    public long incrBy(String key, long increment) {
        return apiManager.execute(() -> jedis.incrBy(key, increment), this);
    }

    @Override
    public double incrByFloat(String key, double increment) {
        return apiManager.execute(() -> jedis.incrByFloat(key, increment), this);
    }

    @Override
    public long decr(String key) {
        return apiManager.execute(() -> jedis.decr(key), this);
    }

    @Override
    public long decrBy(String key, long decrement) {
        return apiManager.execute(() -> jedis.decrBy(key, decrement), this);
    }

    @Override
    public long append(String key, String value) {
        return apiManager.execute(() -> jedis.append(key, value), this);
    }

    @Override
    public String substr(String key, int start, int end) {
        return apiManager.execute(() -> jedis.substr(key, start, end), this);
    }

    @Override
    public long strlen(String key) {
        return apiManager.execute(() -> jedis.strlen(key), this);
    }

    @Override
    public long bitcount(String key) {
        return apiManager.execute(() -> jedis.bitcount(key), this);
    }

    @Override
    public long bitcount(String key, long start, long end) {
        return apiManager.execute(() -> jedis.bitcount(key, start, end), this);
    }

    @Override
    public long bitpos(String key, boolean value) {
        return apiManager.execute(() -> jedis.bitpos(key, value), this);
    }

    @Override
    public long bitpos(String key, boolean value, BitPosParams params) {
        return apiManager.execute(() -> jedis.bitpos(key, value, params), this);
    }

    @Override
    public List<Long> bitfield(String key, String... arguments) {
        return apiManager.execute(() -> jedis.bitfield(key, arguments), this);
    }

    @Override
    public List<Long> bitfieldReadonly(String key, String... arguments) {
        return apiManager.execute(() -> jedis.bitfieldReadonly(key, arguments), this);
    }

    @Override
    public long bitop(BitOP op, String destKey, String... srcKeys) {
        return apiManager.execute(() -> jedis.bitop(op, destKey, srcKeys), this);
    }

    @Override
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

    @Override
    public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        return apiManager.execute(() -> jedis.copy(srcKey, dstKey, replace), this);   }


    @Override
    public String ping() {
        return apiManager.execute(() -> jedis.ping(), this);
    }

    @Override
    public String ping(String message) {
        return apiManager.execute(() -> jedis.ping(message), this);
    }

    @Override
    public String echo(String string) {
        return apiManager.execute(() -> jedis.echo(string), this);
    }

    @Override
    public byte[] echo(byte[] arg) {
        return new byte[0];
    }

    @Override
    public byte[] ping(final byte[] message) {
        return apiManager.execute(() -> jedis.ping(message), this);
    }


    @Override
    public String set(final byte[] key, final byte[] value) {
        return apiManager.execute(() -> jedis.set(key, value), this);
    }

    @Override
    public String set(final byte[] key, final byte[] value, final SetParams params) {
        return apiManager.execute(() -> jedis.set(key, value, params), this);
    }


    @Override
    public byte[] get(final byte[] key) {
        return apiManager.execute(() -> jedis.get(key), this);
    }


    @Override
    public byte[] getDel(final byte[] key) {
        return apiManager.execute(() -> jedis.getDel(key), this);
    }

    @Override
    public byte[] getEx(final byte[] key, final GetExParams params) {
        return apiManager.execute(() -> jedis.getEx(key, params), this);
    }

    @Override
    public long exists(final byte[]... keys) {
        return apiManager.execute(() -> jedis.exists(keys), this);   }

    @Override
    public boolean exists(final byte[] key) {
        return apiManager.execute(() -> jedis.exists(key), this);  }


    @Override
    public long del(final byte[]... keys) {
        return apiManager.execute(() -> jedis.del(keys), this);
    }

    @Override
    public long del(final byte[] key) {
        return apiManager.execute(() -> jedis.del(key), this);
    }

    @Override
    public long unlink(final byte[]... keys) {
        return apiManager.execute(() -> jedis.unlink(keys), this);
    }


    @Override
    public String type(byte[] key) {
        return apiManager.execute(() -> jedis.type(key), this);
    }

    @Override
    public String flushDB() {
        return apiManager.execute(() -> jedis.flushDB(), this);
    }

    @Override
    public String flushDB(FlushMode flushMode) {
        return apiManager.execute(() -> jedis.flushDB(flushMode), this);
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
        return apiManager.execute(() -> jedis.keys(pattern), this);
    }

    @Override
    public byte[] randomBinaryKey() {
        return apiManager.execute(() -> jedis.randomBinaryKey(), this);

    }

    @Override
    public String rename(byte[] oldkey, byte[] newkey) {
        return apiManager.execute(() -> jedis.rename(oldkey, newkey), this);
    }

    @Override
    public long renamenx(byte[] oldkey, byte[] newkey) {
        return apiManager.execute(() -> jedis.renamenx(oldkey, newkey), this);
    }

    @Override
    public long dbSize() {
        return apiManager.execute(() -> jedis.dbSize(), this);
    }

    @Override
    public long expire(byte[] key, long seconds) {
        return apiManager.execute(() -> jedis.expire(key, seconds), this);
    }

    @Override
    public long expireAt(byte[] key, long unixTime) {
        return apiManager.execute(() -> jedis.expireAt(key, unixTime), this);
    }

    @Override
    public long ttl(byte[] key) {
        return apiManager.execute(() -> jedis.ttl(key), this);
    }

    @Override
    public long touch(byte[]... keys) {
        return apiManager.execute(() -> jedis.touch(keys), this);
    }

    @Override
    public long touch(byte[] key) {
        return apiManager.execute(() -> jedis.touch(key), this);
    }

    @Override
    public String select(int index) {
        return apiManager.execute(() -> jedis.select(index), this);
    }

    @Override
    public String swapDB(int index1, int index2) {
        return apiManager.execute(() -> jedis.swapDB(index1, index2), this);
    }

    @Override
    public long move(String key, int dbIndex) {
        return apiManager.execute(() -> jedis.move(key, dbIndex), this);
    }

    @Override
    public long move(byte[] key, int dbIndex) {
        return apiManager.execute(() -> jedis.move(key, dbIndex), this);
    }

    @Override
    public boolean copy(String srcKey, String dstKey, int db, boolean replace) {
        return apiManager.execute(() -> jedis.copy(srcKey, dstKey, db, replace), this);
    }

    @Override
    public String flushAll() {
        return apiManager.execute(() -> jedis.flushAll(), this);
    }

    @Override
    public String flushAll(FlushMode flushMode) {
        return apiManager.execute(() -> jedis.flushAll(flushMode), this);
    }

    String authenticate(String username, String password) {
        return jedis.auth(username, password);
    }

    @Override
    public String auth(String password) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Operation not supported"));
    }

    @Override
    public String auth(String user, String password) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Operation not supported"));
    }

    @Override
    public String save() {
        return apiManager.execute(() -> jedis.save(), this);
    }

    @Override
    public String bgsave() {
        return apiManager.execute(() -> jedis.bgsave(), this);
    }

    @Override
    public String bgrewriteaof() {
        return apiManager.execute(() -> jedis.bgrewriteaof(), this);
    }

    @Override
    public long lastsave() {
        return apiManager.execute(() -> jedis.lastsave(), this);
    }

    @Override
    public void shutdown() throws JedisException {

    }

    @Override
    public void shutdown(SaveMode saveMode) throws JedisException {

    }

    @Override
    public String info() {
        return apiManager.execute(() -> jedis.info(), this);
    }

    @Override
    public String info(String section) {
        return apiManager.execute(() -> jedis.info(section), this);
    }

    @Override
    public String slaveof(String host, int port) {
        return apiManager.execute(() -> jedis.slaveof(host, port), this);
    }

    @Override
    public String slaveofNoOne() {
        return apiManager.execute(() -> jedis.slaveofNoOne(), this);
    }

    @Override
    public long waitReplicas(int replicas, long timeout) {
        return apiManager.execute(() -> jedis.waitReplicas(replicas, timeout), this);
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return apiManager.execute(() -> jedis.getSet(key, value), this);
    }

    @Override
    public List<byte[]> mget(byte[]... keys) {
        return apiManager.execute(() -> jedis.mget(keys), this);
    }

    @Override
    public long setnx(byte[] key, byte[] value) {
        return apiManager.execute(() -> jedis.setnx(key, value), this);
    }

    @Override
    public String setex(byte[] key, long seconds, byte[] value) {
        return apiManager.execute(() -> jedis.setex(key, seconds, value), this);
    }

    @Override
    public String mset(byte[]... keysvalues) {
        return apiManager.execute(() -> jedis.mset(keysvalues), this);
    }

    @Override
    public long msetnx(byte[]... keysvalues) {
        return apiManager.execute(() -> jedis.msetnx(keysvalues), this);
    }

    @Override
    public long decrBy(byte[] key, long decrement) {
        return apiManager.execute(() -> jedis.decrBy(key, decrement), this);
    }

    @Override
    public long decr(byte[] key) {
        return apiManager.execute(() -> jedis.decr(key), this);
    }

    @Override
    public long incrBy(byte[] key, long increment) {
        return apiManager.execute(() -> jedis.incrBy(key, increment), this);
    }

    @Override
    public double incrByFloat(byte[] key, double increment) {
        return apiManager.execute(() -> jedis.incrByFloat(key, increment), this);
    }

    @Override
    public long incr(byte[] key) {
        return apiManager.execute(() -> jedis.incr(key), this);
    }

    @Override
    public long append(byte[] key, byte[] value) {
        return apiManager.execute(() -> jedis.append(key, value), this);
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return apiManager.execute(() -> jedis.substr(key, start, end), this);

    }

    @Override
    public long hset(byte[] key, byte[] field, byte[] value) {
        return apiManager.execute(() -> jedis.hset(key, field, value), this);
    }

    @Override
    public long hset(byte[] key, Map<byte[], byte[]> hash) {
        return apiManager.execute(() -> jedis.hset(key, hash), this);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return apiManager.execute(() -> jedis.hget(key, field), this);

    }

    @Override
    public long hsetnx(byte[] key, byte[] field, byte[] value) {
        return apiManager.execute(() -> jedis.hsetnx(key, field, value), this);
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return apiManager.execute(() -> jedis.hmset(key, hash), this);
    }

    @Override
    public long geoadd(String key, double longitude, double latitude, String member) {
        return apiManager.execute(() -> jedis.geoadd(key, longitude, latitude, member), this);
    }

    @Override
    public long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return apiManager.execute(() -> jedis.geoadd(key, memberCoordinateMap), this);
    }

    @Override
    public long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        return apiManager.execute(() -> jedis.geoadd(key, params, memberCoordinateMap), this);
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        return apiManager.execute(() -> jedis.geodist(key, member1, member2), this);
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return apiManager.execute(() -> jedis.geodist(key, member1, member2), this);
    }

    @Override
    public List<String> geohash(String key, String... members) {
        return apiManager.execute(() -> jedis.geohash(key, members), this);
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        return apiManager.execute(() -> jedis.geopos(key, members), this);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadius(key, longitude, latitude, radius, unit), this);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit), this);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadius(key, longitude, latitude, radius, unit, param), this);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param), this);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadiusByMember(key, member, radius, unit), this);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
        return apiManager.execute(() -> jedis.georadiusByMemberReadonly(key, member, radius, unit), this);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadiusByMember(key, member, radius, unit, param), this);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return apiManager.execute(() -> jedis.georadiusByMemberReadonly(key, member, radius, unit, param), this);
    }

    @Override
    public long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return apiManager.execute(() -> jedis.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam), this);
    }

    @Override
    public long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return apiManager.execute(() -> jedis.georadiusByMemberStore(key, member, radius, unit, param, storeParam), this);
    }

    @Override
    public long hset(String key, String field, String value) {
        return apiManager.execute(() -> jedis.hset(key, field, value), this);
    }

    @Override
    public long hset(String key, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.hset(key, hash), this);
    }

    @Override
    public String hget(String key, String field) {
        return apiManager.execute(() -> jedis.hget(key, field), this);
    }

    @Override
    public long hsetnx(String key, String field, String value) {
        return apiManager.execute(() -> jedis.hsetnx(key, field, value), this);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.hmset(key, hash), this);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return apiManager.execute(() -> jedis.hmget(key, fields), this);
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        return apiManager.execute(() -> jedis.hincrBy(key, field, value), this);
    }

    @Override
    public double hincrByFloat(String key, String field, double value) {
        return apiManager.execute(() -> jedis.hincrByFloat(key, field, value), this);
    }

    @Override
    public boolean hexists(String key, String field) {
        return apiManager.execute(() -> jedis.hexists(key, field), this);
    }

    @Override
    public long hdel(String key, String... field) {
        return apiManager.execute(() -> jedis.hdel(key, field), this);
    }

    @Override
    public long hlen(String key) {
        return apiManager.execute(() -> jedis.hlen(key), this);
    }

    @Override
    public Set<String> hkeys(String key) {
        return apiManager.execute(() -> jedis.hkeys(key), this);
    }

    @Override
    public List<String> hvals(String key) {
        return apiManager.execute(() -> jedis.hvals(key), this);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return apiManager.execute(() -> jedis.hgetAll(key), this);
    }

    @Override
    public String hrandfield(String key) {
        return apiManager.execute(() -> jedis.hrandfield(key), this);
    }

    @Override
    public List<String> hrandfield(String key, long count) {
        return apiManager.execute(() -> jedis.hrandfield(key, count), this);
    }

    @Override
    public List<Map.Entry<String, String>> hrandfieldWithValues(String key, long count) {
        return apiManager.execute(() -> jedis.hrandfieldWithValues(key, count), this);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.hscan(key, cursor, params), this);
    }

    @Override
    public long hstrlen(String key, String field) {
        return apiManager.execute(() -> jedis.hstrlen(key, field), this);
    }

    @Override
    public long pfadd(String key, String... elements) {
        return apiManager.execute(() -> jedis.pfadd(key, elements), this);
    }

    @Override
    public String pfmerge(String destkey, String... sourcekeys) {
        return apiManager.execute(() -> jedis.pfmerge(destkey, sourcekeys), this);
    }

    @Override
    public long pfcount(String key) {
        return apiManager.execute(() -> jedis.pfcount(key), this);
    }

    @Override
    public long pfcount(String... keys) {
        return apiManager.execute(() -> jedis.pfcount(keys), this);
    }

    @Override
    public boolean exists(String key) {
        return apiManager.execute(() -> jedis.exists(key), this);
    }

    @Override
    public long exists(String... keys) {
        return apiManager.execute(() -> jedis.exists(keys), this);
    }

    @Override
    public long persist(String key) {
        return apiManager.execute(() -> jedis.persist(key), this);
    }

    @Override
    public String type(String key) {
        return apiManager.execute(() -> jedis.type(key), this);
    }

    @Override
    public byte[] dump(String key) {
        return new byte[0];
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue) {
        return apiManager.execute(() -> jedis.restore(key, ttl, serializedValue), this);
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        return apiManager.execute(() -> jedis.restore(key, ttl, serializedValue, params), this);
    }

    @Override
    public long expire(String key, long seconds) {
        return apiManager.execute(() -> jedis.expire(key, seconds), this);
    }

    @Override
    public long pexpire(String key, long milliseconds) {
        return apiManager.execute(() -> jedis.pexpire(key, milliseconds), this);
    }

    @Override
    public long expireAt(String key, long unixTime) {
        return apiManager.execute(() -> jedis.expireAt(key, unixTime), this);
    }

    @Override
    public long pexpireAt(String key, long millisecondsTimestamp) {
        return apiManager.execute(() -> jedis.pexpireAt(key, millisecondsTimestamp), this);
    }

    @Override
    public long ttl(String key) {
        return apiManager.execute(() -> jedis.ttl(key), this);
    }

    @Override
    public long pttl(String key) {
        return apiManager.execute(() -> jedis.pttl(key), this);
    }

    @Override
    public long touch(String key) {
        return apiManager.execute(() -> jedis.touch(key), this);
    }

    @Override
    public long touch(String... keys) {
        return apiManager.execute(() -> jedis.touch(keys), this);
    }

    @Override
    public List<String> sort(String key) {
        return apiManager.execute(() -> jedis.sort(key), this);
    }

    @Override
    public long sort(String key, String dstkey) {
        return apiManager.execute(() -> jedis.sort(key, dstkey), this);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return apiManager.execute(() -> jedis.sort(key, sortingParameters), this);
    }

    @Override
    public long sort(String key, SortingParams sortingParameters, String dstkey) {
        return apiManager.execute(() -> jedis.sort(key, sortingParameters, dstkey), this);
    }

    @Override
    public long del(String key) {
        return apiManager.execute(() -> jedis.del(key), this);
    }

    @Override
    public long del(String... keys) {
        return apiManager.execute(() -> jedis.del(keys), this);
    }

    @Override
    public long unlink(String key) {
        return apiManager.execute(() -> jedis.unlink(key), this);
    }

    @Override
    public long unlink(String... keys) {
        return apiManager.execute(() -> jedis.unlink(keys), this);
    }

    @Override
    public boolean copy(String srcKey, String dstKey, boolean replace) {
        return apiManager.execute(() -> jedis.copy(srcKey, dstKey, replace), this);
    }

    @Override
    public String rename(String oldkey, String newkey) {
        return apiManager.execute(() -> jedis.rename(oldkey, newkey), this);
    }

    @Override
    public long renamenx(String oldkey, String newkey) {
        return apiManager.execute(() -> jedis.renamenx(oldkey, newkey), this);
    }

    @Override
    public Long memoryUsage(String key) {
        return apiManager.execute(() -> jedis.memoryUsage(key), this);
    }

    @Override
    public Long memoryUsage(String key, int samples) {
        return apiManager.execute(() -> jedis.memoryUsage(key, samples), this);
    }

    @Override
    public Long objectRefcount(String key) {
        return apiManager.execute(() -> jedis.objectRefcount(key), this);
    }

    @Override
    public String objectEncoding(String key) {
        return apiManager.execute(() -> jedis.objectEncoding(key), this);
    }

    @Override
    public Long objectIdletime(String key) {
        return apiManager.execute(() -> jedis.objectIdletime(key), this);
    }

    @Override
    public Long objectFreq(String key) {
        return apiManager.execute(() -> jedis.objectFreq(key), this);
    }

    @Override
    public String migrate(String host, int port, String key, int timeout) {
        return apiManager.execute(() -> jedis.migrate(host, port, key, timeout), this);
    }

    @Override
    public String migrate(String host, int port, int timeout, MigrateParams params, String... keys) {
        return apiManager.execute(() -> jedis.migrate(host, port, timeout, params, keys), this);
    }

    @Override
    public Set<String> keys(String pattern) {
        return apiManager.execute(() -> jedis.keys(pattern), this);
    }

    @Override
    public ScanResult<String> scan(String cursor) {
        return apiManager.execute(() -> jedis.scan(cursor), this);
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.scan(cursor, params), this);
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params, String type) {
        return apiManager.execute(() -> jedis.scan(cursor, params, type), this);
    }

    @Override
    public String randomKey() {
        return apiManager.execute(() -> jedis.randomKey(), this);
    }

    @Override
    public long rpush(String key, String... string) {
        return apiManager.execute(() -> jedis.rpush(key, string), this);
    }

    @Override
    public long lpush(String key, String... string) {
        return apiManager.execute(() -> jedis.lpush(key, string), this);
    }

    @Override
    public long llen(String key) {
        return apiManager.execute(() -> jedis.llen(key), this);
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.lrange(key, start, stop), this);
    }

    @Override
    public String ltrim(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.ltrim(key, start, stop), this);
    }

    @Override
    public String lindex(String key, long index) {
        return apiManager.execute(() -> jedis.lindex(key, index), this);
    }

    @Override
    public String lset(String key, long index, String value) {
        return apiManager.execute(() -> jedis.lset(key, index, value), this);
    }

    @Override
    public long lrem(String key, long count, String value) {
        return apiManager.execute(() -> jedis.lrem(key, count, value), this);
    }

    @Override
    public String lpop(String key) {
        return apiManager.execute(() -> jedis.lpop(key), this);
    }

    @Override
    public List<String> lpop(String key, int count) {
        return apiManager.execute(() -> jedis.lpop(key, count), this);
    }

    @Override
    public Long lpos(String key, String element) {
        return apiManager.execute(() -> jedis.lpos(key, element), this);
    }

    @Override
    public Long lpos(String key, String element, LPosParams params) {
        return apiManager.execute(() -> jedis.lpos(key, element, params), this);
    }

    @Override
    public List<Long> lpos(String key, String element, LPosParams params, long count) {
        return apiManager.execute(() -> jedis.lpos(key, element, params, count), this);
    }

    @Override
    public String rpop(String key) {
        return apiManager.execute(() -> jedis.rpop(key), this);
    }

    @Override
    public List<String> rpop(String key, int count) {
        return apiManager.execute(() -> jedis.rpop(key, count), this);
    }

    @Override
    public long linsert(String key, ListPosition where, String pivot, String value) {
        return apiManager.execute(() -> jedis.linsert(key, where, pivot, value), this);
    }

    @Override
    public long lpushx(String key, String... string) {
        return apiManager.execute(() -> jedis.lpushx(key, string), this);
    }

    @Override
    public long rpushx(String key, String... string) {
        return apiManager.execute(() -> jedis.rpushx(key, string), this);
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return apiManager.execute(() -> jedis.blpop(timeout, key), this);
    }

    @Override
    public redis.clients.jedis.util.KeyValue<String, String> blpop(double timeout, String key) {
        return apiManager.execute(() -> jedis.blpop(timeout, key), this);
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        return apiManager.execute(() -> jedis.brpop(timeout, key), this);
    }

    @Override
    public KeyValue<String, String> brpop(double timeout, String key) {
        return apiManager.execute(() -> jedis.brpop(timeout, key), this);
    }

    @Override
    public List<String> blpop(int timeout, String... keys) {
        return apiManager.execute(() -> jedis.blpop(timeout, keys), this);
    }

    @Override
    public KeyValue<String, String> blpop(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.blpop(timeout, keys), this);
    }

    @Override
    public List<String> brpop(int timeout, String... keys) {
        return apiManager.execute(() -> jedis.brpop(timeout, keys), this);
    }

    @Override
    public KeyValue<String, String> brpop(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.brpop(timeout, keys), this);
    }

    @Override
    public String rpoplpush(String srckey, String dstkey) {
        return apiManager.execute(() -> jedis.rpoplpush(srckey, dstkey), this);
    }

    @Override
    public String brpoplpush(String source, String destination, int timeout) {
        return apiManager.execute(() -> jedis.brpoplpush(source, destination, timeout), this);
    }

    @Override
    public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        return apiManager.execute(() -> jedis.lmove(srcKey, dstKey, from, to), this);
    }

    @Override
    public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        return apiManager.execute(() -> jedis.blmove(srcKey, dstKey, from, to, timeout), this);
    }

    @Override
    public Object eval(String script) {
        return apiManager.execute(() -> jedis.eval(script), this);
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        return apiManager.execute(() -> jedis.eval(script, keyCount, params), this);
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return apiManager.execute(() -> jedis.eval(script, keys, args), this);
    }

    @Override
    public Object evalsha(String sha1) {
        return apiManager.execute(() -> jedis.evalsha(sha1), this);
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String... params) {
        return apiManager.execute(() -> jedis.evalsha(sha1, keyCount, params), this);
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return apiManager.execute(() -> jedis.evalsha(sha1, keys, args), this);
    }

    @Override
    public long sadd(String key, String... member) {
        return apiManager.execute(() -> jedis.sadd(key, member), this);
    }

    @Override
    public Set<String> smembers(String key) {
        return apiManager.execute(() -> jedis.smembers(key), this);
    }

    @Override
    public long srem(String key, String... member) {
        return apiManager.execute(() -> jedis.srem(key, member), this);
    }

    @Override
    public String spop(String key) {
        return apiManager.execute(() -> jedis.spop(key), this);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return apiManager.execute(() -> jedis.spop(key, count), this);
    }

    @Override
    public long scard(String key) {
        return apiManager.execute(() -> jedis.scard(key), this);
    }

    @Override
    public boolean sismember(String key, String member) {
        return apiManager.execute(() -> jedis.sismember(key, member), this);
    }

    @Override
    public List<Boolean> smismember(String key, String... members) {
        return apiManager.execute(() -> jedis.smismember(key, members), this);
    }

    @Override
    public String srandmember(String key) {
        return apiManager.execute(() -> jedis.srandmember(key), this);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return apiManager.execute(() -> jedis.srandmember(key, count), this);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.sscan(key, cursor, params), this);
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return apiManager.execute(() -> jedis.sdiff(keys), this);
    }

    @Override
    public long sdiffstore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.sdiffstore(dstkey, keys), this);
    }

    @Override
    public Set<String> sinter(String... keys) {
        return apiManager.execute(() -> jedis.sinter(keys), this);
    }

    @Override
    public long sinterstore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.sinterstore(dstkey, keys), this);
    }

    @Override
    public Set<String> sunion(String... keys) {
        return apiManager.execute(() -> jedis.sunion(keys), this);
    }

    @Override
    public long sunionstore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.sunionstore(dstkey, keys), this);
    }

    @Override
    public long smove(String srckey, String dstkey, String member) {
        return apiManager.execute(() -> jedis.smove(srckey, dstkey, member), this);
    }

    @Override
    public long zadd(String key, double score, String member) {
        return apiManager.execute(() -> jedis.zadd(key, score, member), this);
    }

    @Override
    public long zadd(String key, double score, String member, ZAddParams params) {
        return apiManager.execute(() -> jedis.zadd(key, score, member, params), this);
    }

    @Override
    public long zadd(String key, Map<String, Double> scoreMembers) {
        return apiManager.execute(() -> jedis.zadd(key, scoreMembers), this);
    }

    @Override
    public long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return apiManager.execute(() -> jedis.zadd(key, scoreMembers, params), this);
    }

    @Override
    public Double zaddIncr(String key, double score, String member, ZAddParams params) {
        return apiManager.execute(() -> jedis.zaddIncr(key, score, member, params), this);
    }

    @Override
    public long zrem(String key, String... members) {
        return apiManager.execute(() -> jedis.zrem(key, members), this);
    }

    @Override
    public double zincrby(String key, double increment, String member) {
        return apiManager.execute(() -> jedis.zincrby(key, increment, member), this);
    }

    @Override
    public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        return apiManager.execute(() -> jedis.zincrby(key, increment, member, params), this);
    }

    @Override
    public Long zrank(String key, String member) {
        return apiManager.execute(() -> jedis.zrank(key, member), this);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return apiManager.execute(() -> jedis.zrevrank(key, member), this);
    }

    @Override
    public List<String> zrange(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrange(key, start, stop), this);
    }

    @Override
    public List<String> zrevrange(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrevrange(key, start, stop), this);
    }

    @Override
    public List<Tuple> zrangeWithScores(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrangeWithScores(key, start, stop), this);
    }

    @Override
    public List<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zrevrangeWithScores(key, start, stop), this);
    }

    @Override
    public String zrandmember(String key) {
        return apiManager.execute(() -> jedis.zrandmember(key), this);
    }

    @Override
    public List<String> zrandmember(String key, long count) {
        return apiManager.execute(() -> jedis.zrandmember(key, count), this);
    }

    @Override
    public List<Tuple> zrandmemberWithScores(String key, long count) {
        return apiManager.execute(() -> jedis.zrandmemberWithScores(key, count), this);
    }

    @Override
    public long zcard(String key) {
        return apiManager.execute(() -> jedis.zcard(key), this);
    }

    @Override
    public Double zscore(String key, String member) {
        return apiManager.execute(() -> jedis.zscore(key, member), this);
    }

    @Override
    public List<Double> zmscore(String key, String... members) {
        return apiManager.execute(() -> jedis.zmscore(key, members), this);
    }

    @Override
    public Tuple zpopmax(String key) {
        return apiManager.execute(() -> jedis.zpopmax(key), this);
    }

    @Override
    public List<Tuple> zpopmax(String key, int count) {
        return apiManager.execute(() -> jedis.zpopmax(key, count), this);
    }

    @Override
    public Tuple zpopmin(String key) {
        return apiManager.execute(() -> jedis.zpopmin(key), this);
    }

    @Override
    public List<Tuple> zpopmin(String key, int count) {
        return apiManager.execute(() -> jedis.zpopmin(key, count), this);
    }

    @Override
    public long zcount(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zcount(key, min, max), this);
    }

    @Override
    public long zcount(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zcount(key, min, max), this);
    }

    @Override
    public List<String> zrangeByScore(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max), this);
    }

    @Override
    public List<String> zrangeByScore(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max), this);
    }

    @Override
    public List<String> zrevrangeByScore(String key, double max, double min) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min), this);
    }

    @Override
    public List<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max, offset, count), this);
    }

    @Override
    public List<String> zrevrangeByScore(String key, String max, String min) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min), this);
    }

    @Override
    public List<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScore(key, min, max, offset, count), this);
    }

    @Override
    public List<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min, offset, count), this);
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max), this);
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min), this);
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max, offset, count), this);
    }

    @Override
    public List<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScore(key, max, min, offset, count), this);
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max), this);
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min), this);
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByScoreWithScores(key, min, max, offset, count), this);
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count), this);
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count), this);
    }

    @Override
    public long zremrangeByRank(String key, long start, long stop) {
        return apiManager.execute(() -> jedis.zremrangeByRank(key, start, stop), this);
    }

    @Override
    public long zremrangeByScore(String key, double min, double max) {
        return apiManager.execute(() -> jedis.zremrangeByScore(key, min, max), this);
    }

    @Override
    public long zremrangeByScore(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zremrangeByScore(key, min, max), this);
    }

    @Override
    public long zlexcount(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zlexcount(key, min, max), this);
    }

    @Override
    public List<String> zrangeByLex(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zrangeByLex(key, min, max), this);
    }

    @Override
    public List<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return apiManager.execute(() -> jedis.zrangeByLex(key, min, max, offset, count), this);
    }

    @Override
    public List<String> zrevrangeByLex(String key, String max, String min) {
        return apiManager.execute(() -> jedis.zrevrangeByLex(key, max, min), this);
    }

    @Override
    public List<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return apiManager.execute(() -> jedis.zrevrangeByLex(key, max, min, offset, count), this);
    }

    @Override
    public long zremrangeByLex(String key, String min, String max) {
        return apiManager.execute(() -> jedis.zremrangeByLex(key, min, max), this);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return apiManager.execute(() -> jedis.zscan(key, cursor, params), this);
    }

    @Override
    public KeyValue<String, Tuple> bzpopmax(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.bzpopmax(timeout, keys), this);
    }

    @Override
    public KeyValue<String, Tuple> bzpopmin(double timeout, String... keys) {
        return apiManager.execute(() -> jedis.bzpopmin(timeout, keys), this);
    }

    @Override
    public List<String> zdiff(String... keys) {
        return apiManager.execute(() -> jedis.zdiff(keys), this);
    }

    @Override
    public List<Tuple> zdiffWithScores(String... keys) {
        return apiManager.execute(() -> jedis.zdiffWithScores(keys), this);
    }

    @Override
    public long zdiffStore(String dstkey, String... keys) {
        return apiManager.execute(() -> jedis.zdiffStore(dstkey, keys), this);
    }

    @Override
    public long zinterstore(String dstkey, String... sets) {
        return apiManager.execute(() -> jedis.zinterstore(dstkey, sets), this);
    }

    @Override
    public long zinterstore(String dstkey, ZParams params, String... sets) {
        return apiManager.execute(() -> jedis.zinterstore(dstkey, params, sets), this);
    }

    @Override
    public List<String> zinter(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zinter(params, keys), this);
    }

    @Override
    public List<Tuple> zinterWithScores(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zinterWithScores(params, keys), this);
    }

    @Override
    public List<String> zunion(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zunion(params, keys), this);
    }

    @Override
    public List<Tuple> zunionWithScores(ZParams params, String... keys) {
        return apiManager.execute(() -> jedis.zunionWithScores(params, keys), this);
    }

    @Override
    public long zunionstore(String dstkey, String... sets) {
        return apiManager.execute(() -> jedis.zunionstore(dstkey, sets), this);
    }

    @Override
    public long zunionstore(String dstkey, ZParams params, String... sets) {
        return apiManager.execute(() -> jedis.zunionstore(dstkey, params, sets), this);
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.xadd(key, id, hash), this);
    }

    @Override
    public StreamEntryID xadd(String key, XAddParams params, Map<String, String> hash) {
        return apiManager.execute(() -> jedis.xadd(key, params, hash), this);
    }

    @Override
    public long xlen(String key) {
        return apiManager.execute(() -> jedis.xlen(key), this);
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
        return apiManager.execute(() -> jedis.xrange(key, start, end), this);
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        return apiManager.execute(() -> jedis.xrange(key, start, end, count), this);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start), this);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start, count), this);
    }

    @Override
    public List<StreamEntry> xrange(String key, String start, String end) {
        return apiManager.execute(() -> jedis.xrange(key, start, end), this);
    }

    @Override
    public List<StreamEntry> xrange(String key, String start, String end, int count) {
        return apiManager.execute(() -> jedis.xrange(key, start, end, count), this);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, String end, String start) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start), this);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, String end, String start, int count) {
        return apiManager.execute(() -> jedis.xrevrange(key, end, start, count), this);
    }

    @Override
    public long xack(String key, String group, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xack(key, group, ids), this);
    }

    @Override
    public String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
        return apiManager.execute(() -> jedis.xgroupCreate(key, groupname, id, makeStream), this);
    }

    @Override
    public String xgroupSetID(String key, String groupname, StreamEntryID id) {
        return apiManager.execute(() -> jedis.xgroupSetID(key, groupname, id), this);
    }


    @Override
    public long xgroupDestroy(String key, String groupname) {
        return apiManager.execute(() -> jedis.xgroupDestroy(key, groupname), this);
    }

    @Override
    public long xgroupDelConsumer(String key, String groupname, String consumername) {
        return apiManager.execute(() -> jedis.xgroupDelConsumer(key, groupname, consumername), this);
    }

    @Override
    public StreamPendingSummary xpending(String key, String groupname) {
        return apiManager.execute(() -> jedis.xpending(key, groupname), this);
    }

    @Override
    public long xdel(String key, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xdel(key, ids), this);
    }

    @Override
    public long xtrim(String key, long maxLen, boolean approximate) {
        return apiManager.execute(() -> jedis.xtrim(key, maxLen, approximate), this);
    }

    @Override
    public long xtrim(String key, XTrimParams params) {
        return apiManager.execute(() -> jedis.xtrim(key, params), this);
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xclaim(key, group, consumername, minIdleTime, params, ids), this);
    }

    @Override
    public List<StreamEntryID> xclaimJustId(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return apiManager.execute(() -> jedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids), this);
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntry>> xautoclaim(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return apiManager.execute(() -> jedis.xautoclaim(key, group, consumerName, minIdleTime, start, params), this);
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return apiManager.execute(() -> jedis.xautoclaimJustId(key, group, consumerName, minIdleTime, start, params), this);
    }

    @Override
    public StreamInfo xinfoStream(String key) {
        return apiManager.execute(() -> jedis.xinfoStream(key), this);
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
        return apiManager.execute(() -> jedis.xinfoConsumers(key, group), this);
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
        return apiManager.execute(() -> jedis.xread(xReadParams, streams), this);
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        return apiManager.execute(() -> jedis.xreadGroup(groupname, consumer, xReadGroupParams, streams), this);
    }

    @Override
    public void close()  {
        jedis.close();
    }
}
