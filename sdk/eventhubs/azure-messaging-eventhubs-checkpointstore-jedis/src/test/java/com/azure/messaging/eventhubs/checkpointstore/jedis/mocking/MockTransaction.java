// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis.mocking;

import org.json.JSONArray;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.Connection;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.args.BitCountOption;
import redis.clients.jedis.args.BitOP;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.args.FlushMode;
import redis.clients.jedis.args.FunctionRestorePolicy;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.args.SortedSetOption;
import redis.clients.jedis.bloom.BFInsertParams;
import redis.clients.jedis.bloom.BFReserveParams;
import redis.clients.jedis.bloom.CFInsertParams;
import redis.clients.jedis.bloom.CFReserveParams;
import redis.clients.jedis.bloom.TDigestMergeParams;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.graph.ResultSet;
import redis.clients.jedis.json.JsonObjectMapper;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.params.BitPosParams;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.LCSParams;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.params.RestoreParams;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.SortingParams;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.params.XAutoClaimParams;
import redis.clients.jedis.params.XClaimParams;
import redis.clients.jedis.params.XPendingParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.params.XTrimParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;
import redis.clients.jedis.params.ZParams;
import redis.clients.jedis.params.ZRangeParams;
import redis.clients.jedis.resps.FunctionStats;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.LCSMatchResult;
import redis.clients.jedis.resps.LibraryInfo;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.StreamConsumerInfo;
import redis.clients.jedis.resps.StreamConsumersInfo;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamFullInfo;
import redis.clients.jedis.resps.StreamGroupInfo;
import redis.clients.jedis.resps.StreamInfo;
import redis.clients.jedis.resps.StreamPendingEntry;
import redis.clients.jedis.resps.StreamPendingSummary;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.FTSpellCheckParams;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.timeseries.AggregationType;
import redis.clients.jedis.timeseries.TSAddParams;
import redis.clients.jedis.timeseries.TSAlterParams;
import redis.clients.jedis.timeseries.TSCreateParams;
import redis.clients.jedis.timeseries.TSDecrByParams;
import redis.clients.jedis.timeseries.TSElement;
import redis.clients.jedis.timeseries.TSGetParams;
import redis.clients.jedis.timeseries.TSIncrByParams;
import redis.clients.jedis.timeseries.TSInfo;
import redis.clients.jedis.timeseries.TSMGetElement;
import redis.clients.jedis.timeseries.TSMGetParams;
import redis.clients.jedis.timeseries.TSMRangeElements;
import redis.clients.jedis.timeseries.TSMRangeParams;
import redis.clients.jedis.timeseries.TSRangeParams;
import redis.clients.jedis.util.KeyValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock implementation of {@link Transaction}.
 */
public class MockTransaction extends Transaction {
    public MockTransaction(Connection connection) {
        super(connection);
    }

    public MockTransaction(Jedis jedis) {
        super(jedis);
    }

    public MockTransaction(Connection connection, boolean doMulti) {
        super(connection, doMulti);
    }

    public MockTransaction(Connection connection, boolean doMulti, boolean closeConnection) {
        super(connection, doMulti, closeConnection);
    }

    @Override
    public String watch(String... keys) {
        return "";
    }

    @Override
    public String watch(byte[]... keys) {
        return "";
    }

    @Override
    public String unwatch() {
        return "";
    }

    @Override
    public List<Object> exec() {
        return null;
    }

    @Override
    public String discard() {
        return "";
    }

    @Override
    public Response<Long> waitReplicas(int replicas, long timeout) {
        return null;
    }

    @Override
    public Response<Long> publish(String channel, String message) {
        return null;
    }

    @Override
    public Response<Long> publish(byte[] channel, byte[] message) {
        return null;
    }

    @Override
    public Response<Boolean> exists(String key) {
        return null;
    }

    @Override
    public Response<Long> exists(String... keys) {
        return null;
    }

    @Override
    public Response<Long> persist(String key) {
        return null;
    }

    @Override
    public Response<String> type(String key) {
        return null;
    }

    @Override
    public Response<byte[]> dump(String key) {
        return null;
    }

    @Override
    public Response<String> restore(String key, long ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public Response<String> restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        return null;
    }

    @Override
    public Response<Long> expire(String key, long seconds) {
        return null;
    }

    @Override
    public Response<Long> expire(String key, long seconds, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> pexpire(String key, long milliseconds) {
        return null;
    }

    @Override
    public Response<Long> pexpire(String key, long milliseconds, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> expireTime(String key) {
        return null;
    }

    @Override
    public Response<Long> pexpireTime(String key) {
        return null;
    }

    @Override
    public Response<Long> expireAt(String key, long unixTime) {
        return null;
    }

    @Override
    public Response<Long> expireAt(String key, long unixTime, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> pexpireAt(String key, long millisecondsTimestamp) {
        return null;
    }

    @Override
    public Response<Long> pexpireAt(String key, long millisecondsTimestamp, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> ttl(String key) {
        return null;
    }

    @Override
    public Response<Long> pttl(String key) {
        return null;
    }

    @Override
    public Response<Long> touch(String key) {
        return null;
    }

    @Override
    public Response<Long> touch(String... keys) {
        return null;
    }

    @Override
    public Response<List<String>> sort(String key) {
        return null;
    }

    @Override
    public Response<Long> sort(String key, String dstKey) {
        return null;
    }

    @Override
    public Response<List<String>> sort(String key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public Response<Long> sort(String key, SortingParams sortingParams, String dstKey) {
        return null;
    }

    @Override
    public Response<List<String>> sortReadonly(String key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public Response<Long> del(String key) {
        return null;
    }

    @Override
    public Response<Long> del(String... keys) {
        return null;
    }

    @Override
    public Response<Long> unlink(String key) {
        return null;
    }

    @Override
    public Response<Long> unlink(String... keys) {
        return null;
    }

    @Override
    public Response<Boolean> copy(String srcKey, String dstKey, boolean replace) {
        return null;
    }

    @Override
    public Response<String> rename(String oldkey, String newkey) {
        return null;
    }

    @Override
    public Response<Long> renamenx(String oldkey, String newkey) {
        return null;
    }

    @Override
    public Response<Long> memoryUsage(String key) {
        return null;
    }

    @Override
    public Response<Long> memoryUsage(String key, int samples) {
        return null;
    }

    @Override
    public Response<Long> objectRefcount(String key) {
        return null;
    }

    @Override
    public Response<String> objectEncoding(String key) {
        return null;
    }

    @Override
    public Response<Long> objectIdletime(String key) {
        return null;
    }

    @Override
    public Response<Long> objectFreq(String key) {
        return null;
    }

    @Override
    public Response<String> migrate(String host, int port, String key, int timeout) {
        return null;
    }

    @Override
    public Response<String> migrate(String host, int port, int timeout, MigrateParams params, String... keys) {
        return null;
    }

    @Override
    public Response<Set<String>> keys(String pattern) {
        return null;
    }

    @Override
    public Response<ScanResult<String>> scan(String cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<String>> scan(String cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<ScanResult<String>> scan(String cursor, ScanParams params, String type) {
        return null;
    }

    @Override
    public Response<String> randomKey() {
        return null;
    }

    @Override
    public Response<String> get(String key) {
        return null;
    }

    @Override
    public Response<String> setGet(String key, String value) {
        return null;
    }

    @Override
    public Response<String> setGet(String key, String value, SetParams params) {
        return null;
    }

    @Override
    public Response<String> getDel(String key) {
        return null;
    }

    @Override
    public Response<String> getEx(String key, GetExParams params) {
        return null;
    }

    @Override
    public Response<Boolean> setbit(String key, long offset, boolean value) {
        return null;
    }

    @Override
    public Response<Boolean> getbit(String key, long offset) {
        return null;
    }

    @Override
    public Response<Long> setrange(String key, long offset, String value) {
        return null;
    }

    @Override
    public Response<String> getrange(String key, long startOffset, long endOffset) {
        return null;
    }

    @Override
    public Response<String> getSet(String key, String value) {
        return null;
    }

    @Override
    public Response<Long> setnx(String key, String value) {
        return null;
    }

    @Override
    public Response<String> setex(String key, long seconds, String value) {
        return null;
    }

    @Override
    public Response<String> psetex(String key, long milliseconds, String value) {
        return null;
    }

    @Override
    public Response<List<String>> mget(String... keys) {
        return null;
    }

    @Override
    public Response<String> mset(String... keysvalues) {
        return null;
    }

    @Override
    public Response<Long> msetnx(String... keysvalues) {
        return null;
    }

    @Override
    public Response<Long> incr(String key) {
        return null;
    }

    @Override
    public Response<Long> incrBy(String key, long increment) {
        return null;
    }

    @Override
    public Response<Double> incrByFloat(String key, double increment) {
        return null;
    }

    @Override
    public Response<Long> decr(String key) {
        return null;
    }

    @Override
    public Response<Long> decrBy(String key, long decrement) {
        return null;
    }

    @Override
    public Response<Long> append(String key, String value) {
        return null;
    }

    @Override
    public Response<String> substr(String key, int start, int end) {
        return null;
    }

    @Override
    public Response<Long> strlen(String key) {
        return null;
    }

    @Override
    public Response<Long> bitcount(String key) {
        return null;
    }

    @Override
    public Response<Long> bitcount(String key, long start, long end) {
        return null;
    }

    @Override
    public Response<Long> bitcount(String key, long start, long end, BitCountOption option) {
        return null;
    }

    @Override
    public Response<Long> bitpos(String key, boolean value) {
        return null;
    }

    @Override
    public Response<Long> bitpos(String key, boolean value, BitPosParams params) {
        return null;
    }

    @Override
    public Response<List<Long>> bitfield(String key, String... arguments) {
        return null;
    }

    @Override
    public Response<List<Long>> bitfieldReadonly(String key, String... arguments) {
        return null;
    }

    @Override
    public Response<Long> bitop(BitOP op, String destKey, String... srcKeys) {
        return null;
    }

    @Override
    public Response<LCSMatchResult> lcs(String keyA, String keyB, LCSParams params) {
        return null;
    }

    @Override
    public Response<String> set(String key, String value) {
        return null;
    }

    @Override
    public Response<String> set(String key, String value, SetParams params) {
        return null;
    }

    @Override
    public Response<Long> rpush(String key, String... string) {
        return null;
    }

    @Override
    public Response<Long> lpush(String key, String... string) {
        return null;
    }

    @Override
    public Response<Long> llen(String key) {
        return null;
    }

    @Override
    public Response<List<String>> lrange(String key, long start, long stop) {
        return null;
    }

    @Override
    public Response<String> ltrim(String key, long start, long stop) {
        return null;
    }

    @Override
    public Response<String> lindex(String key, long index) {
        return null;
    }

    @Override
    public Response<String> lset(String key, long index, String value) {
        return null;
    }

    @Override
    public Response<Long> lrem(String key, long count, String value) {
        return null;
    }

    @Override
    public Response<String> lpop(String key) {
        return null;
    }

    @Override
    public Response<List<String>> lpop(String key, int count) {
        return null;
    }

    @Override
    public Response<Long> lpos(String key, String element) {
        return null;
    }

    @Override
    public Response<Long> lpos(String key, String element, LPosParams params) {
        return null;
    }

    @Override
    public Response<List<Long>> lpos(String key, String element, LPosParams params, long count) {
        return null;
    }

    @Override
    public Response<String> rpop(String key) {
        return null;
    }

    @Override
    public Response<List<String>> rpop(String key, int count) {
        return null;
    }

    @Override
    public Response<Long> linsert(String key, ListPosition where, String pivot, String value) {
        return null;
    }

    @Override
    public Response<Long> lpushx(String key, String... strings) {
        return null;
    }

    @Override
    public Response<Long> rpushx(String key, String... strings) {
        return null;
    }

    @Override
    public Response<List<String>> blpop(int timeout, String key) {
        return null;
    }

    @Override
    public Response<KeyValue<String, String>> blpop(double timeout, String key) {
        return null;
    }

    @Override
    public Response<List<String>> brpop(int timeout, String key) {
        return null;
    }

    @Override
    public Response<KeyValue<String, String>> brpop(double timeout, String key) {
        return null;
    }

    @Override
    public Response<List<String>> blpop(int timeout, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, String>> blpop(double timeout, String... keys) {
        return null;
    }

    @Override
    public Response<List<String>> brpop(int timeout, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, String>> brpop(double timeout, String... keys) {
        return null;
    }

    @Override
    public Response<String> rpoplpush(String srcKey, String dstKey) {
        return null;
    }

    @Override
    public Response<String> brpoplpush(String source, String destination, int timeout) {
        return null;
    }

    @Override
    public Response<String> lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        return null;
    }

    @Override
    public Response<String> blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<String>>> lmpop(ListDirection direction, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<String>>> lmpop(ListDirection direction, int count, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<String>>> blmpop(double timeout, ListDirection direction, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<String>>> blmpop(double timeout, ListDirection direction, int count,
        String... keys) {
        return null;
    }

    @Override
    public Response<Long> hset(String key, String field, String value) {
        return null;
    }

    @Override
    public Response<Long> hset(String key, Map<String, String> hash) {
        return null;
    }

    @Override
    public Response<String> hget(String key, String field) {
        return null;
    }

    @Override
    public Response<Long> hsetnx(String key, String field, String value) {
        return null;
    }

    @Override
    public Response<String> hmset(String key, Map<String, String> hash) {
        return null;
    }

    @Override
    public Response<List<String>> hmget(String key, String... fields) {
        return null;
    }

    @Override
    public Response<Long> hincrBy(String key, String field, long value) {
        return null;
    }

    @Override
    public Response<Double> hincrByFloat(String key, String field, double value) {
        return null;
    }

    @Override
    public Response<Boolean> hexists(String key, String field) {
        return null;
    }

    @Override
    public Response<Long> hdel(String key, String... field) {
        return null;
    }

    @Override
    public Response<Long> hlen(String key) {
        return null;
    }

    @Override
    public Response<Set<String>> hkeys(String key) {
        return null;
    }

    @Override
    public Response<List<String>> hvals(String key) {
        return null;
    }

    @Override
    public Response<Map<String, String>> hgetAll(String key) {
        return null;
    }

    @Override
    public Response<String> hrandfield(String key) {
        return null;
    }

    @Override
    public Response<List<String>> hrandfield(String key, long count) {
        return null;
    }

    @Override
    public Response<List<Map.Entry<String, String>>> hrandfieldWithValues(String key, long count) {
        return null;
    }

    @Override
    public Response<ScanResult<Map.Entry<String, String>>> hscan(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<ScanResult<String>> hscanNoValues(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<Long> hstrlen(String key, String field) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpire(String key, long seconds, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpire(String key, long seconds, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpire(String key, long milliseconds, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpire(String key, long milliseconds, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpireAt(String key, long unixTimeSeconds, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpireAt(String key, long unixTimeSeconds, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpireAt(String key, long unixTimeMillis, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpireAt(String key, long unixTimeMillis, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpireTime(String key, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpireTime(String key, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> httl(String key, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpttl(String key, String... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpersist(String key, String... fields) {
        return null;
    }

    @Override
    public Response<Long> sadd(String key, String... members) {
        return null;
    }

    @Override
    public Response<Set<String>> smembers(String key) {
        return null;
    }

    @Override
    public Response<Long> srem(String key, String... members) {
        return null;
    }

    @Override
    public Response<String> spop(String key) {
        return null;
    }

    @Override
    public Response<Set<String>> spop(String key, long count) {
        return null;
    }

    @Override
    public Response<Long> scard(String key) {
        return null;
    }

    @Override
    public Response<Boolean> sismember(String key, String member) {
        return null;
    }

    @Override
    public Response<List<Boolean>> smismember(String key, String... members) {
        return null;
    }

    @Override
    public Response<String> srandmember(String key) {
        return null;
    }

    @Override
    public Response<List<String>> srandmember(String key, int count) {
        return null;
    }

    @Override
    public Response<ScanResult<String>> sscan(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<Set<String>> sdiff(String... keys) {
        return null;
    }

    @Override
    public Response<Long> sdiffstore(String dstKey, String... keys) {
        return null;
    }

    @Override
    public Response<Set<String>> sinter(String... keys) {
        return null;
    }

    @Override
    public Response<Long> sinterstore(String dstKey, String... keys) {
        return null;
    }

    @Override
    public Response<Long> sintercard(String... keys) {
        return null;
    }

    @Override
    public Response<Long> sintercard(int limit, String... keys) {
        return null;
    }

    @Override
    public Response<Set<String>> sunion(String... keys) {
        return null;
    }

    @Override
    public Response<Long> sunionstore(String dstKey, String... keys) {
        return null;
    }

    @Override
    public Response<Long> smove(String srcKey, String dstKey, String member) {
        return null;
    }

    @Override
    public Response<Long> zadd(String key, double score, String member) {
        return null;
    }

    @Override
    public Response<Long> zadd(String key, double score, String member, ZAddParams params) {
        return null;
    }

    @Override
    public Response<Long> zadd(String key, Map<String, Double> scoreMembers) {
        return null;
    }

    @Override
    public Response<Long> zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return null;
    }

    @Override
    public Response<Double> zaddIncr(String key, double score, String member, ZAddParams params) {
        return null;
    }

    @Override
    public Response<Long> zrem(String key, String... members) {
        return null;
    }

    @Override
    public Response<Double> zincrby(String key, double increment, String member) {
        return null;
    }

    @Override
    public Response<Double> zincrby(String key, double increment, String member, ZIncrByParams params) {
        return null;
    }

    @Override
    public Response<Long> zrank(String key, String member) {
        return null;
    }

    @Override
    public Response<Long> zrevrank(String key, String member) {
        return null;
    }

    @Override
    public Response<KeyValue<Long, Double>> zrankWithScore(String key, String member) {
        return null;
    }

    @Override
    public Response<KeyValue<Long, Double>> zrevrankWithScore(String key, String member) {
        return null;
    }

    @Override
    public Response<List<String>> zrange(String key, long start, long stop) {
        return null;
    }

    @Override
    public Response<List<String>> zrevrange(String key, long start, long stop) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeWithScores(String key, long start, long stop) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeWithScores(String key, long start, long stop) {
        return null;
    }

    @Override
    public Response<String> zrandmember(String key) {
        return null;
    }

    @Override
    public Response<List<String>> zrandmember(String key, long count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrandmemberWithScores(String key, long count) {
        return null;
    }

    @Override
    public Response<Long> zcard(String key) {
        return null;
    }

    @Override
    public Response<Double> zscore(String key, String member) {
        return null;
    }

    @Override
    public Response<List<Double>> zmscore(String key, String... members) {
        return null;
    }

    @Override
    public Response<Tuple> zpopmax(String key) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zpopmax(String key, int count) {
        return null;
    }

    @Override
    public Response<Tuple> zpopmin(String key) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zpopmin(String key, int count) {
        return null;
    }

    @Override
    public Response<Long> zcount(String key, double min, double max) {
        return null;
    }

    @Override
    public Response<Long> zcount(String key, String min, String max) {
        return null;
    }

    @Override
    public Response<List<String>> zrangeByScore(String key, double min, double max) {
        return null;
    }

    @Override
    public Response<List<String>> zrangeByScore(String key, String min, String max) {
        return null;
    }

    @Override
    public Response<List<String>> zrevrangeByScore(String key, double max, double min) {
        return null;
    }

    @Override
    public Response<List<String>> zrangeByScore(String key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<String>> zrevrangeByScore(String key, String max, String min) {
        return null;
    }

    @Override
    public Response<List<String>> zrangeByScore(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<String>> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(String key, double min, double max) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, double max, double min) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<String>> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(String key, String min, String max) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, String max, String min) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<String>> zrange(String key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeWithScores(String key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public Response<Long> zrangestore(String dest, String src, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByRank(String key, long start, long stop) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByScore(String key, double min, double max) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByScore(String key, String min, String max) {
        return null;
    }

    @Override
    public Response<Long> zlexcount(String key, String min, String max) {
        return null;
    }

    @Override
    public Response<List<String>> zrangeByLex(String key, String min, String max) {
        return null;
    }

    @Override
    public Response<List<String>> zrangeByLex(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<String>> zrevrangeByLex(String key, String max, String min) {
        return null;
    }

    @Override
    public Response<List<String>> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByLex(String key, String min, String max) {
        return null;
    }

    @Override
    public Response<ScanResult<Tuple>> zscan(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<KeyValue<String, Tuple>> bzpopmax(double timeout, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, Tuple>> bzpopmin(double timeout, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<Tuple>>> zmpop(SortedSetOption option, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<Tuple>>> zmpop(SortedSetOption option, int count, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<Tuple>>> bzmpop(double timeout, SortedSetOption option, String... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<String, List<Tuple>>> bzmpop(double timeout, SortedSetOption option, int count,
        String... keys) {
        return null;
    }

    @Override
    public Response<List<String>> zdiff(String... keys) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zdiffWithScores(String... keys) {
        return null;
    }

    @Override
    public Response<Long> zdiffStore(String dstKey, String... keys) {
        return null;
    }

    @Override
    public Response<Long> zdiffstore(String dstKey, String... keys) {
        return null;
    }

    @Override
    public Response<Long> zinterstore(String dstKey, String... sets) {
        return null;
    }

    @Override
    public Response<Long> zinterstore(String dstKey, ZParams params, String... sets) {
        return null;
    }

    @Override
    public Response<List<String>> zinter(ZParams params, String... keys) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zinterWithScores(ZParams params, String... keys) {
        return null;
    }

    @Override
    public Response<Long> zintercard(String... keys) {
        return null;
    }

    @Override
    public Response<Long> zintercard(long limit, String... keys) {
        return null;
    }

    @Override
    public Response<List<String>> zunion(ZParams params, String... keys) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zunionWithScores(ZParams params, String... keys) {
        return null;
    }

    @Override
    public Response<Long> zunionstore(String dstKey, String... sets) {
        return null;
    }

    @Override
    public Response<Long> zunionstore(String dstKey, ZParams params, String... sets) {
        return null;
    }

    @Override
    public Response<Long> geoadd(String key, double longitude, double latitude, String member) {
        return null;
    }

    @Override
    public Response<Long> geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return null;
    }

    @Override
    public Response<Long> geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        return null;
    }

    @Override
    public Response<Double> geodist(String key, String member1, String member2) {
        return null;
    }

    @Override
    public Response<Double> geodist(String key, String member1, String member2, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<String>> geohash(String key, String... members) {
        return null;
    }

    @Override
    public Response<List<GeoCoordinate>> geopos(String key, String... members) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadius(String key, double longitude, double latitude, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusReadonly(String key, double longitude, double latitude,
        double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadius(String key, double longitude, double latitude, double radius,
        GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusReadonly(String key, double longitude, double latitude,
        double radius, GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(String key, String member, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMember(String key, String member, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(String key, String member, double radius,
        GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<Long> georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit,
        GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return null;
    }

    @Override
    public Response<Long> georadiusByMemberStore(String key, String member, double radius, GeoUnit unit,
        GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(String key, String member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(String key, GeoCoordinate coord, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(String key, String member, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(String key, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(String key, GeoSearchParam params) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(String dest, String src, String member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(String dest, String src, GeoCoordinate coord, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(String dest, String src, String member, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(String dest, String src, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(String dest, String src, GeoSearchParam params) {
        return null;
    }

    @Override
    public Response<Long> geosearchStoreStoreDist(String dest, String src, GeoSearchParam params) {
        return null;
    }

    @Override
    public Response<Long> pfadd(String key, String... elements) {
        return null;
    }

    @Override
    public Response<String> pfmerge(String destkey, String... sourcekeys) {
        return null;
    }

    @Override
    public Response<Long> pfcount(String key) {
        return null;
    }

    @Override
    public Response<Long> pfcount(String... keys) {
        return null;
    }

    @Override
    public Response<StreamEntryID> xadd(String key, StreamEntryID id, Map<String, String> hash) {
        return null;
    }

    @Override
    public Response<StreamEntryID> xadd(String key, XAddParams params, Map<String, String> hash) {
        return null;
    }

    @Override
    public Response<Long> xlen(String key) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrange(String key, StreamEntryID start, StreamEntryID end) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrange(String key, String start, String end) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrange(String key, String start, String end, int count) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrevrange(String key, String end, String start) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xrevrange(String key, String end, String start, int count) {
        return null;
    }

    @Override
    public Response<Long> xack(String key, String group, StreamEntryID... ids) {
        return null;
    }

    @Override
    public Response<String> xgroupCreate(String key, String groupName, StreamEntryID id, boolean makeStream) {
        return null;
    }

    @Override
    public Response<String> xgroupSetID(String key, String groupName, StreamEntryID id) {
        return null;
    }

    @Override
    public Response<Long> xgroupDestroy(String key, String groupName) {
        return null;
    }

    @Override
    public Response<Boolean> xgroupCreateConsumer(String key, String groupName, String consumerName) {
        return null;
    }

    @Override
    public Response<Long> xgroupDelConsumer(String key, String groupName, String consumerName) {
        return null;
    }

    @Override
    public Response<StreamPendingSummary> xpending(String key, String groupName) {
        return null;
    }

    @Override
    public Response<List<StreamPendingEntry>> xpending(String key, String groupName, XPendingParams params) {
        return null;
    }

    @Override
    public Response<Long> xdel(String key, StreamEntryID... ids) {
        return null;
    }

    @Override
    public Response<Long> xtrim(String key, long maxLen, boolean approximate) {
        return null;
    }

    @Override
    public Response<Long> xtrim(String key, XTrimParams params) {
        return null;
    }

    @Override
    public Response<List<StreamEntry>> xclaim(String key, String group, String consumerName, long minIdleTime,
        XClaimParams params, StreamEntryID... ids) {
        return null;
    }

    @Override
    public Response<List<StreamEntryID>> xclaimJustId(String key, String group, String consumerName, long minIdleTime,
        XClaimParams params, StreamEntryID... ids) {
        return null;
    }

    @Override
    public Response<Map.Entry<StreamEntryID, List<StreamEntry>>> xautoclaim(String key, String group,
        String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return null;
    }

    @Override
    public Response<Map.Entry<StreamEntryID, List<StreamEntryID>>> xautoclaimJustId(String key, String group,
        String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return null;
    }

    @Override
    public Response<StreamInfo> xinfoStream(String key) {
        return null;
    }

    @Override
    public Response<StreamFullInfo> xinfoStreamFull(String key) {
        return null;
    }

    @Override
    public Response<StreamFullInfo> xinfoStreamFull(String key, int count) {
        return null;
    }

    @Override
    public Response<List<StreamGroupInfo>> xinfoGroups(String key) {
        return null;
    }

    @Override
    public Response<List<StreamConsumersInfo>> xinfoConsumers(String key, String group) {
        return null;
    }

    @Override
    public Response<List<StreamConsumerInfo>> xinfoConsumers2(String key, String group) {
        return null;
    }

    @Override
    public Response<List<Map.Entry<String, List<StreamEntry>>>> xread(XReadParams xReadParams,
        Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public Response<Map<String, List<StreamEntry>>> xreadAsMap(XReadParams xReadParams,
        Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public Response<List<Map.Entry<String, List<StreamEntry>>>> xreadGroup(String groupName, String consumer,
        XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public Response<Map<String, List<StreamEntry>>> xreadGroupAsMap(String groupName, String consumer,
        XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public Response<Object> eval(String script) {
        return null;
    }

    @Override
    public Response<Object> eval(String script, int keyCount, String... params) {
        return null;
    }

    @Override
    public Response<Object> eval(String script, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Response<Object> evalReadonly(String script, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Response<Object> evalsha(String sha1) {
        return null;
    }

    @Override
    public Response<Object> evalsha(String sha1, int keyCount, String... params) {
        return null;
    }

    @Override
    public Response<Object> evalsha(String sha1, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Response<Object> evalshaReadonly(String sha1, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Response<Long> waitReplicas(String sampleKey, int replicas, long timeout) {
        return null;
    }

    @Override
    public Response<KeyValue<Long, Long>> waitAOF(String sampleKey, long numLocal, long numReplicas, long timeout) {
        return null;
    }

    @Override
    public Response<Object> eval(String script, String sampleKey) {
        return null;
    }

    @Override
    public Response<Object> evalsha(String sha1, String sampleKey) {
        return null;
    }

    @Override
    public Response<List<Boolean>> scriptExists(String sampleKey, String... sha1) {
        return null;
    }

    @Override
    public Response<String> scriptLoad(String script, String sampleKey) {
        return null;
    }

    @Override
    public Response<String> scriptFlush(String sampleKey) {
        return null;
    }

    @Override
    public Response<String> scriptFlush(String sampleKey, FlushMode flushMode) {
        return null;
    }

    @Override
    public Response<String> scriptKill(String sampleKey) {
        return null;
    }

    @Override
    public Response<Object> fcall(byte[] name, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Response<Object> fcall(String name, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Response<Object> fcallReadonly(byte[] name, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Response<Object> fcallReadonly(String name, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Response<String> functionDelete(byte[] libraryName) {
        return null;
    }

    @Override
    public Response<String> functionDelete(String libraryName) {
        return null;
    }

    @Override
    public Response<byte[]> functionDump() {
        return null;
    }

    @Override
    public Response<List<LibraryInfo>> functionList(String libraryNamePattern) {
        return null;
    }

    @Override
    public Response<List<LibraryInfo>> functionList() {
        return null;
    }

    @Override
    public Response<List<LibraryInfo>> functionListWithCode(String libraryNamePattern) {
        return null;
    }

    @Override
    public Response<List<LibraryInfo>> functionListWithCode() {
        return null;
    }

    @Override
    public Response<List<Object>> functionListBinary() {
        return null;
    }

    @Override
    public Response<List<Object>> functionList(byte[] libraryNamePattern) {
        return null;
    }

    @Override
    public Response<List<Object>> functionListWithCodeBinary() {
        return null;
    }

    @Override
    public Response<List<Object>> functionListWithCode(byte[] libraryNamePattern) {
        return null;
    }

    @Override
    public Response<String> functionLoad(byte[] functionCode) {
        return null;
    }

    @Override
    public Response<String> functionLoad(String functionCode) {
        return null;
    }

    @Override
    public Response<String> functionLoadReplace(byte[] functionCode) {
        return null;
    }

    @Override
    public Response<String> functionLoadReplace(String functionCode) {
        return null;
    }

    @Override
    public Response<String> functionRestore(byte[] serializedValue) {
        return null;
    }

    @Override
    public Response<String> functionRestore(byte[] serializedValue, FunctionRestorePolicy policy) {
        return null;
    }

    @Override
    public Response<String> functionFlush() {
        return null;
    }

    @Override
    public Response<String> functionFlush(FlushMode mode) {
        return null;
    }

    @Override
    public Response<String> functionKill() {
        return null;
    }

    @Override
    public Response<FunctionStats> functionStats() {
        return null;
    }

    @Override
    public Response<Object> functionStatsBinary() {
        return null;
    }

    @Override
    public Response<Long> geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return null;
    }

    @Override
    public Response<Long> geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return null;
    }

    @Override
    public Response<Long> geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return null;
    }

    @Override
    public Response<Double> geodist(byte[] key, byte[] member1, byte[] member2) {
        return null;
    }

    @Override
    public Response<Double> geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<byte[]>> geohash(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Response<List<GeoCoordinate>> geopos(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadius(byte[] key, double longitude, double latitude, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusReadonly(byte[] key, double longitude, double latitude,
        double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadius(byte[] key, double longitude, double latitude, double radius,
        GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusReadonly(byte[] key, double longitude, double latitude,
        double radius, GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(byte[] key, byte[] member, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(byte[] key, byte[] member, double radius,
        GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public Response<Long> georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
        GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return null;
    }

    @Override
    public Response<Long> georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit,
        GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(byte[] key, GeoCoordinate coord, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(byte[] key, byte[] member, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(byte[] key, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<List<GeoRadiusResponse>> geosearch(byte[] key, GeoSearchParam params) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(byte[] dest, byte[] src, byte[] member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(byte[] dest, byte[] src, byte[] member, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public Response<Long> geosearchStore(byte[] dest, byte[] src, GeoSearchParam params) {
        return null;
    }

    @Override
    public Response<Long> geosearchStoreStoreDist(byte[] dest, byte[] src, GeoSearchParam params) {
        return null;
    }

    @Override
    public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
        return null;
    }

    @Override
    public Response<Long> hset(byte[] key, Map<byte[], byte[]> hash) {
        return null;
    }

    @Override
    public Response<byte[]> hget(byte[] key, byte[] field) {
        return null;
    }

    @Override
    public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
        return null;
    }

    @Override
    public Response<String> hmset(byte[] key, Map<byte[], byte[]> hash) {
        return null;
    }

    @Override
    public Response<List<byte[]>> hmget(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
        return null;
    }

    @Override
    public Response<Double> hincrByFloat(byte[] key, byte[] field, double value) {
        return null;
    }

    @Override
    public Response<Boolean> hexists(byte[] key, byte[] field) {
        return null;
    }

    @Override
    public Response<Long> hdel(byte[] key, byte[]... field) {
        return null;
    }

    @Override
    public Response<Long> hlen(byte[] key) {
        return null;
    }

    @Override
    public Response<Set<byte[]>> hkeys(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> hvals(byte[] key) {
        return null;
    }

    @Override
    public Response<Map<byte[], byte[]>> hgetAll(byte[] key) {
        return null;
    }

    @Override
    public Response<byte[]> hrandfield(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> hrandfield(byte[] key, long count) {
        return null;
    }

    @Override
    public Response<List<Map.Entry<byte[], byte[]>>> hrandfieldWithValues(byte[] key, long count) {
        return null;
    }

    @Override
    public Response<ScanResult<Map.Entry<byte[], byte[]>>> hscan(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<ScanResult<byte[]>> hscanNoValues(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<Long> hstrlen(byte[] key, byte[] field) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpire(byte[] key, long seconds, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpire(byte[] key, long seconds, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpire(byte[] key, long milliseconds, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpire(byte[] key, long milliseconds, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpireAt(byte[] key, long unixTimeSeconds, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpireAt(byte[] key, long unixTimeSeconds, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpireAt(byte[] key, long unixTimeMillis, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpireAt(byte[] key, long unixTimeMillis, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hexpireTime(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpexpireTime(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> httl(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpttl(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public Response<List<Long>> hpersist(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public Response<Long> pfadd(byte[] key, byte[]... elements) {
        return null;
    }

    @Override
    public Response<String> pfmerge(byte[] destkey, byte[]... sourcekeys) {
        return null;
    }

    @Override
    public Response<Long> pfcount(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> pfcount(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Boolean> exists(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> exists(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> persist(byte[] key) {
        return null;
    }

    @Override
    public Response<String> type(byte[] key) {
        return null;
    }

    @Override
    public Response<byte[]> dump(byte[] key) {
        return null;
    }

    @Override
    public Response<String> restore(byte[] key, long ttl, byte[] serializedValue) {
        return null;
    }

    @Override
    public Response<String> restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
        return null;
    }

    @Override
    public Response<Long> expire(byte[] key, long seconds) {
        return null;
    }

    @Override
    public Response<Long> expire(byte[] key, long seconds, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> pexpire(byte[] key, long milliseconds) {
        return null;
    }

    @Override
    public Response<Long> pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> expireTime(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> pexpireTime(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> expireAt(byte[] key, long unixTime) {
        return null;
    }

    @Override
    public Response<Long> expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> pexpireAt(byte[] key, long millisecondsTimestamp) {
        return null;
    }

    @Override
    public Response<Long> pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
        return null;
    }

    @Override
    public Response<Long> ttl(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> pttl(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> touch(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> touch(byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<byte[]>> sort(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> sort(byte[] key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public Response<List<byte[]>> sortReadonly(byte[] key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public Response<Long> del(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> del(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> unlink(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> unlink(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Boolean> copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        return null;
    }

    @Override
    public Response<String> rename(byte[] oldkey, byte[] newkey) {
        return null;
    }

    @Override
    public Response<Long> renamenx(byte[] oldkey, byte[] newkey) {
        return null;
    }

    @Override
    public Response<Long> sort(byte[] key, SortingParams sortingParams, byte[] dstkey) {
        return null;
    }

    @Override
    public Response<Long> sort(byte[] key, byte[] dstkey) {
        return null;
    }

    @Override
    public Response<Long> memoryUsage(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> memoryUsage(byte[] key, int samples) {
        return null;
    }

    @Override
    public Response<Long> objectRefcount(byte[] key) {
        return null;
    }

    @Override
    public Response<byte[]> objectEncoding(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> objectIdletime(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> objectFreq(byte[] key) {
        return null;
    }

    @Override
    public Response<String> migrate(String host, int port, byte[] key, int timeout) {
        return null;
    }

    @Override
    public Response<String> migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Set<byte[]>> keys(byte[] pattern) {
        return null;
    }

    @Override
    public Response<ScanResult<byte[]>> scan(byte[] cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params, byte[] type) {
        return null;
    }

    @Override
    public Response<byte[]> randomBinaryKey() {
        return null;
    }

    @Override
    public Response<Long> rpush(byte[] key, byte[]... args) {
        return null;
    }

    @Override
    public Response<Long> lpush(byte[] key, byte[]... args) {
        return null;
    }

    @Override
    public Response<Long> llen(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> lrange(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Response<String> ltrim(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Response<byte[]> lindex(byte[] key, long index) {
        return null;
    }

    @Override
    public Response<String> lset(byte[] key, long index, byte[] value) {
        return null;
    }

    @Override
    public Response<Long> lrem(byte[] key, long count, byte[] value) {
        return null;
    }

    @Override
    public Response<byte[]> lpop(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> lpop(byte[] key, int count) {
        return null;
    }

    @Override
    public Response<Long> lpos(byte[] key, byte[] element) {
        return null;
    }

    @Override
    public Response<Long> lpos(byte[] key, byte[] element, LPosParams params) {
        return null;
    }

    @Override
    public Response<List<Long>> lpos(byte[] key, byte[] element, LPosParams params, long count) {
        return null;
    }

    @Override
    public Response<byte[]> rpop(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> rpop(byte[] key, int count) {
        return null;
    }

    @Override
    public Response<Long> linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
        return null;
    }

    @Override
    public Response<Long> lpushx(byte[] key, byte[]... args) {
        return null;
    }

    @Override
    public Response<Long> rpushx(byte[] key, byte[]... args) {
        return null;
    }

    @Override
    public Response<List<byte[]>> blpop(int timeout, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], byte[]>> blpop(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<byte[]>> brpop(int timeout, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], byte[]>> brpop(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public Response<byte[]> rpoplpush(byte[] srckey, byte[] dstkey) {
        return null;
    }

    @Override
    public Response<byte[]> brpoplpush(byte[] source, byte[] destination, int timeout) {
        return null;
    }

    @Override
    public Response<byte[]> lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
        return null;
    }

    @Override
    public Response<byte[]> blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<byte[]>>> lmpop(ListDirection direction, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<byte[]>>> lmpop(ListDirection direction, int count, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<byte[]>>> blmpop(double timeout, ListDirection direction, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<byte[]>>> blmpop(double timeout, ListDirection direction, int count,
        byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> waitReplicas(byte[] sampleKey, int replicas, long timeout) {
        return null;
    }

    @Override
    public Response<KeyValue<Long, Long>> waitAOF(byte[] sampleKey, long numLocal, long numReplicas, long timeout) {
        return null;
    }

    @Override
    public Response<Object> eval(byte[] script, byte[] sampleKey) {
        return null;
    }

    @Override
    public Response<Object> evalsha(byte[] sha1, byte[] sampleKey) {
        return null;
    }

    @Override
    public Response<List<Boolean>> scriptExists(byte[] sampleKey, byte[]... sha1s) {
        return null;
    }

    @Override
    public Response<byte[]> scriptLoad(byte[] script, byte[] sampleKey) {
        return null;
    }

    @Override
    public Response<String> scriptFlush(byte[] sampleKey) {
        return null;
    }

    @Override
    public Response<String> scriptFlush(byte[] sampleKey, FlushMode flushMode) {
        return null;
    }

    @Override
    public Response<String> scriptKill(byte[] sampleKey) {
        return null;
    }

    @Override
    public Response<Object> eval(byte[] script) {
        return null;
    }

    @Override
    public Response<Object> eval(byte[] script, int keyCount, byte[]... params) {
        return null;
    }

    @Override
    public Response<Object> eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Response<Object> evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Response<Object> evalsha(byte[] sha1) {
        return null;
    }

    @Override
    public Response<Object> evalsha(byte[] sha1, int keyCount, byte[]... params) {
        return null;
    }

    @Override
    public Response<Object> evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Response<Object> evalshaReadonly(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Response<Long> sadd(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Response<Set<byte[]>> smembers(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> srem(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Response<byte[]> spop(byte[] key) {
        return null;
    }

    @Override
    public Response<Set<byte[]>> spop(byte[] key, long count) {
        return null;
    }

    @Override
    public Response<Long> scard(byte[] key) {
        return null;
    }

    @Override
    public Response<Boolean> sismember(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Response<List<Boolean>> smismember(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Response<byte[]> srandmember(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> srandmember(byte[] key, int count) {
        return null;
    }

    @Override
    public Response<ScanResult<byte[]>> sscan(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<Set<byte[]>> sdiff(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Set<byte[]>> sinter(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> sintercard(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> sintercard(int limit, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Set<byte[]>> sunion(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> sunionstore(byte[] dstkey, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
        return null;
    }

    @Override
    public Response<Long> zadd(byte[] key, double score, byte[] member) {
        return null;
    }

    @Override
    public Response<Long> zadd(byte[] key, double score, byte[] member, ZAddParams params) {
        return null;
    }

    @Override
    public Response<Long> zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return null;
    }

    @Override
    public Response<Long> zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
        return null;
    }

    @Override
    public Response<Double> zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
        return null;
    }

    @Override
    public Response<Long> zrem(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Response<Double> zincrby(byte[] key, double increment, byte[] member) {
        return null;
    }

    @Override
    public Response<Double> zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
        return null;
    }

    @Override
    public Response<Long> zrank(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Response<Long> zrevrank(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Response<KeyValue<Long, Double>> zrankWithScore(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Response<KeyValue<Long, Double>> zrevrankWithScore(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrange(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrevrange(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeWithScores(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeWithScores(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Response<byte[]> zrandmember(byte[] key) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrandmember(byte[] key, long count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrandmemberWithScores(byte[] key, long count) {
        return null;
    }

    @Override
    public Response<Long> zcard(byte[] key) {
        return null;
    }

    @Override
    public Response<Double> zscore(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Response<List<Double>> zmscore(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Response<Tuple> zpopmax(byte[] key) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zpopmax(byte[] key, int count) {
        return null;
    }

    @Override
    public Response<Tuple> zpopmin(byte[] key) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zpopmin(byte[] key, int count) {
        return null;
    }

    @Override
    public Response<Long> zcount(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public Response<Long> zcount(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrangeByScore(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrevrangeByScore(byte[] key, double max, double min) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByRank(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByScore(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByScore(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public Response<Long> zlexcount(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zrange(byte[] key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zrangeWithScores(byte[] key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public Response<Long> zrangestore(byte[] dest, byte[] src, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public Response<Long> zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public Response<ScanResult<Tuple>> zscan(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], Tuple>> bzpopmax(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], Tuple>> bzpopmin(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<Tuple>>> zmpop(SortedSetOption option, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<Tuple>>> zmpop(SortedSetOption option, int count, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<Tuple>>> bzmpop(double timeout, SortedSetOption option, byte[]... keys) {
        return null;
    }

    @Override
    public Response<KeyValue<byte[], List<Tuple>>> bzmpop(double timeout, SortedSetOption option, int count,
        byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zdiff(byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zdiffWithScores(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> zdiffStore(byte[] dstkey, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> zdiffstore(byte[] dstkey, byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zinter(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zinterWithScores(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> zinterstore(byte[] dstkey, byte[]... sets) {
        return null;
    }

    @Override
    public Response<Long> zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return null;
    }

    @Override
    public Response<Long> zintercard(byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> zintercard(long limit, byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<byte[]>> zunion(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public Response<List<Tuple>> zunionWithScores(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public Response<Long> zunionstore(byte[] dstkey, byte[]... sets) {
        return null;
    }

    @Override
    public Response<Long> zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return null;
    }

    @Override
    public Response<byte[]> xadd(byte[] key, XAddParams params, Map<byte[], byte[]> hash) {
        return null;
    }

    @Override
    public Response<Long> xlen(byte[] key) {
        return null;
    }

    @Override
    public Response<List<Object>> xrange(byte[] key, byte[] start, byte[] end) {
        return null;
    }

    @Override
    public Response<List<Object>> xrange(byte[] key, byte[] start, byte[] end, int count) {
        return null;
    }

    @Override
    public Response<List<Object>> xrevrange(byte[] key, byte[] end, byte[] start) {
        return null;
    }

    @Override
    public Response<List<Object>> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
        return null;
    }

    @Override
    public Response<Long> xack(byte[] key, byte[] group, byte[]... ids) {
        return null;
    }

    @Override
    public Response<String> xgroupCreate(byte[] key, byte[] groupName, byte[] id, boolean makeStream) {
        return null;
    }

    @Override
    public Response<String> xgroupSetID(byte[] key, byte[] groupName, byte[] id) {
        return null;
    }

    @Override
    public Response<Long> xgroupDestroy(byte[] key, byte[] groupName) {
        return null;
    }

    @Override
    public Response<Boolean> xgroupCreateConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
        return null;
    }

    @Override
    public Response<Long> xgroupDelConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
        return null;
    }

    @Override
    public Response<Long> xdel(byte[] key, byte[]... ids) {
        return null;
    }

    @Override
    public Response<Long> xtrim(byte[] key, long maxLen, boolean approximateLength) {
        return null;
    }

    @Override
    public Response<Long> xtrim(byte[] key, XTrimParams params) {
        return null;
    }

    @Override
    public Response<Object> xpending(byte[] key, byte[] groupName) {
        return null;
    }

    @Override
    public Response<List<Object>> xpending(byte[] key, byte[] groupName, XPendingParams params) {
        return null;
    }

    @Override
    public Response<List<byte[]>> xclaim(byte[] key, byte[] group, byte[] consumerName, long minIdleTime,
        XClaimParams params, byte[]... ids) {
        return null;
    }

    @Override
    public Response<List<byte[]>> xclaimJustId(byte[] key, byte[] group, byte[] consumerName, long minIdleTime,
        XClaimParams params, byte[]... ids) {
        return null;
    }

    @Override
    public Response<List<Object>> xautoclaim(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime,
        byte[] start, XAutoClaimParams params) {
        return null;
    }

    @Override
    public Response<List<Object>> xautoclaimJustId(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime,
        byte[] start, XAutoClaimParams params) {
        return null;
    }

    @Override
    public Response<Object> xinfoStream(byte[] key) {
        return null;
    }

    @Override
    public Response<Object> xinfoStreamFull(byte[] key) {
        return null;
    }

    @Override
    public Response<Object> xinfoStreamFull(byte[] key, int count) {
        return null;
    }

    @Override
    public Response<List<Object>> xinfoGroups(byte[] key) {
        return null;
    }

    @Override
    public Response<List<Object>> xinfoConsumers(byte[] key, byte[] group) {
        return null;
    }

    @SafeVarargs
    @Override
    public final Response<List<Object>> xread(XReadParams xReadParams, Map.Entry<byte[], byte[]>... streams) {
        return null;
    }

    @SafeVarargs
    @Override
    public final Response<List<Object>> xreadGroup(byte[] groupName, byte[] consumer, XReadGroupParams xReadGroupParams,
        Map.Entry<byte[], byte[]>... streams) {
        return null;
    }

    @Override
    public Response<String> set(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public Response<String> set(byte[] key, byte[] value, SetParams params) {
        return null;
    }

    @Override
    public Response<byte[]> get(byte[] key) {
        return null;
    }

    @Override
    public Response<byte[]> setGet(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public Response<byte[]> setGet(byte[] key, byte[] value, SetParams params) {
        return null;
    }

    @Override
    public Response<byte[]> getDel(byte[] key) {
        return null;
    }

    @Override
    public Response<byte[]> getEx(byte[] key, GetExParams params) {
        return null;
    }

    @Override
    public Response<Boolean> setbit(byte[] key, long offset, boolean value) {
        return null;
    }

    @Override
    public Response<Boolean> getbit(byte[] key, long offset) {
        return null;
    }

    @Override
    public Response<Long> setrange(byte[] key, long offset, byte[] value) {
        return null;
    }

    @Override
    public Response<byte[]> getrange(byte[] key, long startOffset, long endOffset) {
        return null;
    }

    @Override
    public Response<byte[]> getSet(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public Response<Long> setnx(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public Response<String> setex(byte[] key, long seconds, byte[] value) {
        return null;
    }

    @Override
    public Response<String> psetex(byte[] key, long milliseconds, byte[] value) {
        return null;
    }

    @Override
    public Response<List<byte[]>> mget(byte[]... keys) {
        return null;
    }

    @Override
    public Response<String> mset(byte[]... keysvalues) {
        return null;
    }

    @Override
    public Response<Long> msetnx(byte[]... keysvalues) {
        return null;
    }

    @Override
    public Response<Long> incr(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> incrBy(byte[] key, long increment) {
        return null;
    }

    @Override
    public Response<Double> incrByFloat(byte[] key, double increment) {
        return null;
    }

    @Override
    public Response<Long> decr(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> decrBy(byte[] key, long decrement) {
        return null;
    }

    @Override
    public Response<Long> append(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public Response<byte[]> substr(byte[] key, int start, int end) {
        return null;
    }

    @Override
    public Response<Long> strlen(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> bitcount(byte[] key) {
        return null;
    }

    @Override
    public Response<Long> bitcount(byte[] key, long start, long end) {
        return null;
    }

    @Override
    public Response<Long> bitcount(byte[] key, long start, long end, BitCountOption option) {
        return null;
    }

    @Override
    public Response<Long> bitpos(byte[] key, boolean value) {
        return null;
    }

    @Override
    public Response<Long> bitpos(byte[] key, boolean value, BitPosParams params) {
        return null;
    }

    @Override
    public Response<List<Long>> bitfield(byte[] key, byte[]... arguments) {
        return null;
    }

    @Override
    public Response<List<Long>> bitfieldReadonly(byte[] key, byte[]... arguments) {
        return null;
    }

    @Override
    public Response<Long> bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        return null;
    }

    @Override
    public Response<String> ftCreate(String indexName, IndexOptions indexOptions, Schema schema) {
        return null;
    }

    @Override
    public Response<String> ftCreate(String indexName, FTCreateParams createParams,
        Iterable<SchemaField> schemaFields) {
        return null;
    }

    @Override
    public Response<String> ftAlter(String indexName, Schema schema) {
        return null;
    }

    @Override
    public Response<String> ftAlter(String indexName, Iterable<SchemaField> schemaFields) {
        return null;
    }

    @Override
    public Response<String> ftAliasAdd(String aliasName, String indexName) {
        return null;
    }

    @Override
    public Response<String> ftAliasUpdate(String aliasName, String indexName) {
        return null;
    }

    @Override
    public Response<String> ftAliasDel(String aliasName) {
        return null;
    }

    @Override
    public Response<String> ftDropIndex(String indexName) {
        return null;
    }

    @Override
    public Response<String> ftDropIndexDD(String indexName) {
        return null;
    }

    @Override
    public Response<SearchResult> ftSearch(String indexName, String query) {
        return null;
    }

    @Override
    public Response<SearchResult> ftSearch(String indexName, String query, FTSearchParams searchParams) {
        return null;
    }

    @Override
    public Response<SearchResult> ftSearch(String indexName, Query query) {
        return null;
    }

    @Override
    public Response<SearchResult> ftSearch(byte[] indexName, Query query) {
        return null;
    }

    @Override
    public Response<String> ftExplain(String indexName, Query query) {
        return null;
    }

    @Override
    public Response<List<String>> ftExplainCLI(String indexName, Query query) {
        return null;
    }

    @Override
    public Response<AggregationResult> ftAggregate(String indexName, AggregationBuilder aggr) {
        return null;
    }

    @Override
    public Response<String> ftSynUpdate(String indexName, String synonymGroupId, String... terms) {
        return null;
    }

    @Override
    public Response<Map<String, List<String>>> ftSynDump(String indexName) {
        return null;
    }

    @Override
    public Response<Long> ftDictAdd(String dictionary, String... terms) {
        return null;
    }

    @Override
    public Response<Long> ftDictDel(String dictionary, String... terms) {
        return null;
    }

    @Override
    public Response<Set<String>> ftDictDump(String dictionary) {
        return null;
    }

    @Override
    public Response<Long> ftDictAddBySampleKey(String indexName, String dictionary, String... terms) {
        return null;
    }

    @Override
    public Response<Long> ftDictDelBySampleKey(String indexName, String dictionary, String... terms) {
        return null;
    }

    @Override
    public Response<SearchResult> ftSearch(String indexName) {
        return null;
    }

    @Override
    public Response<String> ftAlter(String indexName, SchemaField... schemaFields) {
        return null;
    }

    @Override
    public Response<String> ftAlter(String indexName, Schema.Field... fields) {
        return null;
    }

    @Override
    public Response<String> ftCreate(String indexName, Iterable<SchemaField> schemaFields) {
        return null;
    }

    @Override
    public Response<String> ftCreate(String indexName, FTCreateParams createParams, SchemaField... schemaFields) {
        return null;
    }

    @Override
    public Response<String> ftCreate(String indexName, SchemaField... schemaFields) {
        return null;
    }

    @Override
    public Response<List<JSONArray>> jsonMGet(String... keys) {
        return null;
    }

    @Override
    public Response<String> jsonSetWithEscape(String key, Object object, JsonSetParams params) {
        return null;
    }

    @Override
    public Response<String> jsonSet(String key, Object object, JsonSetParams params) {
        return null;
    }

    @Override
    public Response<String> jsonSetWithEscape(String key, Object object) {
        return null;
    }

    @Override
    public Response<String> jsonSet(String key, Object object) {
        return null;
    }

    @Override
    public <T> Response<List<T>> jsonMGet(Class<T> clazz, String... keys) {
        return null;
    }

    @Override
    public Response<String> jsonSetLegacy(String key, Object pojo, JsonSetParams params) {
        return null;
    }

    @Override
    public Response<String> jsonSetLegacy(String key, Object pojo) {
        return null;
    }

    @Override
    public Response<StreamEntryID> xadd(String key, Map<String, String> hash, XAddParams params) {
        return null;
    }

    @Override
    public Response<byte[]> xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
        return null;
    }

    @Override
    public Response<ScanResult<Tuple>> zscan(String key, String cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<Tuple>> zscan(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public Response<Long> sdiffStore(String dstKey, String... keys) {
        return null;
    }

    @Override
    public Response<ScanResult<String>> sscan(String key, String cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<byte[]>> sscan(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<String>> hscanNoValues(String key, String cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<Map.Entry<String, String>>> hscan(String key, String cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<byte[]>> hscanNoValues(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public Response<ScanResult<Map.Entry<byte[], byte[]>>> hscan(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public void setJsonObjectMapper(JsonObjectMapper jsonObjectMapper) {
    }

    @Override
    public <T> Response<T> executeCommand(CommandObject<T> command) {
        return null;
    }

    @Override
    public Response<Object> sendCommand(CommandArguments args) {
        return null;
    }

    @Override
    public Response<Object> sendCommand(ProtocolCommand cmd, byte[]... args) {
        return null;
    }

    @Override
    public Response<Object> sendCommand(ProtocolCommand cmd, String... args) {
        return null;
    }

    @Override
    public Response<List<String>> graphProfile(String graphName, String query) {
        return null;
    }

    @Override
    public Response<String> graphDelete(String name) {
        return null;
    }

    @Override
    public Response<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params, long timeout) {
        return null;
    }

    @Override
    public Response<ResultSet> graphQuery(String name, String query, Map<String, Object> params, long timeout) {
        return null;
    }

    @Override
    public Response<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params) {
        return null;
    }

    @Override
    public Response<ResultSet> graphQuery(String name, String query, Map<String, Object> params) {
        return null;
    }

    @Override
    public Response<ResultSet> graphReadonlyQuery(String name, String query, long timeout) {
        return null;
    }

    @Override
    public Response<ResultSet> graphQuery(String name, String query, long timeout) {
        return null;
    }

    @Override
    public Response<ResultSet> graphReadonlyQuery(String name, String query) {
        return null;
    }

    @Override
    public Response<ResultSet> graphQuery(String name, String query) {
        return null;
    }

    @Override
    public Response<List<Double>> tdigestByRevRank(String key, long... ranks) {
        return null;
    }

    @Override
    public Response<List<Double>> tdigestByRank(String key, long... ranks) {
        return null;
    }

    @Override
    public Response<List<Long>> tdigestRevRank(String key, double... values) {
        return null;
    }

    @Override
    public Response<List<Long>> tdigestRank(String key, double... values) {
        return null;
    }

    @Override
    public Response<Double> tdigestTrimmedMean(String key, double lowCutQuantile, double highCutQuantile) {
        return null;
    }

    @Override
    public Response<Double> tdigestMax(String key) {
        return null;
    }

    @Override
    public Response<Double> tdigestMin(String key) {
        return null;
    }

    @Override
    public Response<List<Double>> tdigestQuantile(String key, double... quantiles) {
        return null;
    }

    @Override
    public Response<List<Double>> tdigestCDF(String key, double... values) {
        return null;
    }

    @Override
    public Response<String> tdigestAdd(String key, double... values) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> tdigestInfo(String key) {
        return null;
    }

    @Override
    public Response<String> tdigestMerge(TDigestMergeParams mergeParams, String destinationKey, String... sourceKeys) {
        return null;
    }

    @Override
    public Response<String> tdigestMerge(String destinationKey, String... sourceKeys) {
        return null;
    }

    @Override
    public Response<String> tdigestReset(String key) {
        return null;
    }

    @Override
    public Response<String> tdigestCreate(String key, int compression) {
        return null;
    }

    @Override
    public Response<String> tdigestCreate(String key) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> topkInfo(String key) {
        return null;
    }

    @Override
    public Response<Map<String, Long>> topkListWithCount(String key) {
        return null;
    }

    @Override
    public Response<List<String>> topkList(String key) {
        return null;
    }

    @Override
    public Response<List<Boolean>> topkQuery(String key, String... items) {
        return null;
    }

    @Override
    public Response<List<String>> topkIncrBy(String key, Map<String, Long> itemIncrements) {
        return null;
    }

    @Override
    public Response<List<String>> topkAdd(String key, String... items) {
        return null;
    }

    @Override
    public Response<String> topkReserve(String key, long topk, long width, long depth, double decay) {
        return null;
    }

    @Override
    public Response<String> topkReserve(String key, long topk) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> cmsInfo(String key) {
        return null;
    }

    @Override
    public Response<String> cmsMerge(String destKey, Map<String, Long> keysAndWeights) {
        return null;
    }

    @Override
    public Response<String> cmsMerge(String destKey, String... keys) {
        return null;
    }

    @Override
    public Response<List<Long>> cmsQuery(String key, String... items) {
        return null;
    }

    @Override
    public Response<List<Long>> cmsIncrBy(String key, Map<String, Long> itemIncrements) {
        return null;
    }

    @Override
    public Response<String> cmsInitByProb(String key, double error, double probability) {
        return null;
    }

    @Override
    public Response<String> cmsInitByDim(String key, long width, long depth) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> cfInfo(String key) {
        return null;
    }

    @Override
    public Response<String> cfLoadChunk(String key, long iterator, byte[] data) {
        return null;
    }

    @Override
    public Response<Map.Entry<Long, byte[]>> cfScanDump(String key, long iterator) {
        return null;
    }

    @Override
    public Response<Long> cfCount(String key, String item) {
        return null;
    }

    @Override
    public Response<Boolean> cfDel(String key, String item) {
        return null;
    }

    @Override
    public Response<List<Boolean>> cfMExists(String key, String... items) {
        return null;
    }

    @Override
    public Response<Boolean> cfExists(String key, String item) {
        return null;
    }

    @Override
    public Response<List<Boolean>> cfInsertNx(String key, CFInsertParams insertParams, String... items) {
        return null;
    }

    @Override
    public Response<List<Boolean>> cfInsertNx(String key, String... items) {
        return null;
    }

    @Override
    public Response<List<Boolean>> cfInsert(String key, CFInsertParams insertParams, String... items) {
        return null;
    }

    @Override
    public Response<List<Boolean>> cfInsert(String key, String... items) {
        return null;
    }

    @Override
    public Response<Boolean> cfAddNx(String key, String item) {
        return null;
    }

    @Override
    public Response<Boolean> cfAdd(String key, String item) {
        return null;
    }

    @Override
    public Response<String> cfReserve(String key, long capacity, CFReserveParams reserveParams) {
        return null;
    }

    @Override
    public Response<String> cfReserve(String key, long capacity) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> bfInfo(String key) {
        return null;
    }

    @Override
    public Response<Long> bfCard(String key) {
        return null;
    }

    @Override
    public Response<String> bfLoadChunk(String key, long iterator, byte[] data) {
        return null;
    }

    @Override
    public Response<Map.Entry<Long, byte[]>> bfScanDump(String key, long iterator) {
        return null;
    }

    @Override
    public Response<List<Boolean>> bfMExists(String key, String... items) {
        return null;
    }

    @Override
    public Response<Boolean> bfExists(String key, String item) {
        return null;
    }

    @Override
    public Response<List<Boolean>> bfInsert(String key, BFInsertParams insertParams, String... items) {
        return null;
    }

    @Override
    public Response<List<Boolean>> bfInsert(String key, String... items) {
        return null;
    }

    @Override
    public Response<List<Boolean>> bfMAdd(String key, String... items) {
        return null;
    }

    @Override
    public Response<Boolean> bfAdd(String key, String item) {
        return null;
    }

    @Override
    public Response<String> bfReserve(String key, double errorRate, long capacity, BFReserveParams reserveParams) {
        return null;
    }

    @Override
    public Response<String> bfReserve(String key, double errorRate, long capacity) {
        return null;
    }

    @Override
    public Response<TSInfo> tsInfoDebug(String key) {
        return null;
    }

    @Override
    public Response<TSInfo> tsInfo(String key) {
        return null;
    }

    @Override
    public Response<List<String>> tsQueryIndex(String... filters) {
        return null;
    }

    @Override
    public Response<String> tsDeleteRule(String sourceKey, String destKey) {
        return null;
    }

    @Override
    public Response<String> tsCreateRule(String sourceKey, String destKey, AggregationType aggregationType,
        long bucketDuration, long alignTimestamp) {
        return null;
    }

    @Override
    public Response<String> tsCreateRule(String sourceKey, String destKey, AggregationType aggregationType,
        long timeBucket) {
        return null;
    }

    @Override
    public Response<Map<String, TSMGetElement>> tsMGet(TSMGetParams multiGetParams, String... filters) {
        return null;
    }

    @Override
    public Response<TSElement> tsGet(String key, TSGetParams getParams) {
        return null;
    }

    @Override
    public Response<TSElement> tsGet(String key) {
        return null;
    }

    @Override
    public Response<Map<String, TSMRangeElements>> tsMRevRange(TSMRangeParams multiRangeParams) {
        return null;
    }

    @Override
    public Response<Map<String, TSMRangeElements>> tsMRevRange(long fromTimestamp, long toTimestamp,
        String... filters) {
        return null;
    }

    @Override
    public Response<Map<String, TSMRangeElements>> tsMRange(TSMRangeParams multiRangeParams) {
        return null;
    }

    @Override
    public Response<Map<String, TSMRangeElements>> tsMRange(long fromTimestamp, long toTimestamp, String... filters) {
        return null;
    }

    @Override
    public Response<List<TSElement>> tsRevRange(String key, TSRangeParams rangeParams) {
        return null;
    }

    @Override
    public Response<List<TSElement>> tsRevRange(String key, long fromTimestamp, long toTimestamp) {
        return null;
    }

    @Override
    public Response<List<TSElement>> tsRange(String key, TSRangeParams rangeParams) {
        return null;
    }

    @Override
    public Response<List<TSElement>> tsRange(String key, long fromTimestamp, long toTimestamp) {
        return null;
    }

    @Override
    public Response<Long> tsDecrBy(String key, double subtrahend, TSDecrByParams decrByParams) {
        return null;
    }

    @Override
    public Response<Long> tsDecrBy(String key, double value, long timestamp) {
        return null;
    }

    @Override
    public Response<Long> tsDecrBy(String key, double value) {
        return null;
    }

    @Override
    public Response<Long> tsIncrBy(String key, double addend, TSIncrByParams incrByParams) {
        return null;
    }

    @Override
    public Response<Long> tsIncrBy(String key, double value, long timestamp) {
        return null;
    }

    @Override
    public Response<Long> tsIncrBy(String key, double value) {
        return null;
    }

    @SafeVarargs
    @Override
    public final Response<List<Long>> tsMAdd(Map.Entry<String, TSElement>... entries) {
        return null;
    }

    @Override
    public Response<Long> tsAdd(String key, long timestamp, double value, TSAddParams addParams) {
        return null;
    }

    @Override
    public Response<Long> tsAdd(String key, long timestamp, double value, TSCreateParams createParams) {
        return null;
    }

    @Override
    public Response<Long> tsAdd(String key, long timestamp, double value) {
        return null;
    }

    @Override
    public Response<Long> tsAdd(String key, double value) {
        return null;
    }

    @Override
    public Response<String> tsAlter(String key, TSAlterParams alterParams) {
        return null;
    }

    @Override
    public Response<Long> tsDel(String key, long fromTimestamp, long toTimestamp) {
        return null;
    }

    @Override
    public Response<String> tsCreate(String key, TSCreateParams createParams) {
        return null;
    }

    @Override
    public Response<String> tsCreate(String key) {
        return null;
    }

    @Override
    public Response<Object> jsonArrPop(String key, Path path) {
        return null;
    }

    @Override
    public Response<List<Object>> jsonArrPop(String key, Path2 path) {
        return null;
    }

    @Override
    public <T> Response<T> jsonArrPop(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrLen(String key, Path2 path) {
        return null;
    }

    @Override
    public Response<Long> jsonArrLen(String key) {
        return null;
    }

    @Override
    public <T> Response<T> jsonArrPop(String key, Class<T> clazz, Path path, int index) {
        return null;
    }

    @Override
    public Response<Object> jsonArrPop(String key, Path path, int index) {
        return null;
    }

    @Override
    public Response<List<Object>> jsonArrPop(String key, Path2 path, int index) {
        return null;
    }

    @Override
    public <T> Response<T> jsonArrPop(String key, Class<T> clazz, Path path) {
        return null;
    }

    @Override
    public Response<Long> jsonArrTrim(String key, Path path, int start, int stop) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrTrim(String key, Path2 path, int start, int stop) {
        return null;
    }

    @Override
    public Response<Long> jsonArrLen(String key, Path path) {
        return null;
    }

    @Override
    public Response<Object> jsonArrPop(String key) {
        return null;
    }

    @Override
    public Response<Long> jsonArrInsert(String key, Path path, int index, Object... pojos) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrInsertWithEscape(String key, Path2 path, int index, Object... objects) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrInsert(String key, Path2 path, int index, Object... objects) {
        return null;
    }

    @Override
    public Response<Long> jsonArrIndex(String key, Path path, Object scalar) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrIndexWithEscape(String key, Path2 path, Object scalar) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrIndex(String key, Path2 path, Object scalar) {
        return null;
    }

    @Override
    public Response<Long> jsonArrAppend(String key, Path path, Object... objects) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrAppendWithEscape(String key, Path2 path, Object... objects) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonArrAppend(String key, Path2 path, Object... objects) {
        return null;
    }

    @Override
    public Response<Double> jsonNumIncrBy(String key, Path path, double value) {
        return null;
    }

    @Override
    public Response<Object> jsonNumIncrBy(String key, Path2 path, double value) {
        return null;
    }

    @Override
    public Response<Long> jsonStrLen(String key, Path path) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonStrLen(String key, Path2 path) {
        return null;
    }

    @Override
    public Response<Long> jsonStrLen(String key) {
        return null;
    }

    @Override
    public Response<Long> jsonStrAppend(String key, Path path, Object string) {
        return null;
    }

    @Override
    public Response<List<Long>> jsonStrAppend(String key, Path2 path, Object string) {
        return null;
    }

    @Override
    public Response<Long> jsonStrAppend(String key, Object string) {
        return null;
    }

    @Override
    public Response<Class<?>> jsonType(String key, Path path) {
        return null;
    }

    @Override
    public Response<List<Class<?>>> jsonType(String key, Path2 path) {
        return null;
    }

    @Override
    public Response<Class<?>> jsonType(String key) {
        return null;
    }

    @Override
    public Response<String> jsonToggle(String key, Path path) {
        return null;
    }

    @Override
    public Response<List<Boolean>> jsonToggle(String key, Path2 path) {
        return null;
    }

    @Override
    public Response<Long> jsonClear(String key, Path path) {
        return null;
    }

    @Override
    public Response<Long> jsonClear(String key, Path2 path) {
        return null;
    }

    @Override
    public Response<Long> jsonClear(String key) {
        return null;
    }

    @Override
    public Response<Long> jsonDel(String key, Path path) {
        return null;
    }

    @Override
    public Response<Long> jsonDel(String key, Path2 path) {
        return null;
    }

    @Override
    public Response<Long> jsonDel(String key) {
        return null;
    }

    @Override
    public <T> Response<List<T>> jsonMGet(Path path, Class<T> clazz, String... keys) {
        return null;
    }

    @Override
    public Response<List<JSONArray>> jsonMGet(Path2 path, String... keys) {
        return null;
    }

    @Override
    public <T> Response<T> jsonGet(String key, Class<T> clazz, Path... paths) {
        return null;
    }

    @Override
    public Response<Object> jsonGet(String key, Path... paths) {
        return null;
    }

    @Override
    public Response<Object> jsonGet(String key, Path2... paths) {
        return null;
    }

    @Override
    public <T> Response<T> jsonGet(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public Response<Object> jsonGet(String key) {
        return null;
    }

    @Override
    public Response<String> jsonMerge(String key, Path path, Object object) {
        return null;
    }

    @Override
    public Response<String> jsonMerge(String key, Path2 path, Object object) {
        return null;
    }

    @Override
    public Response<String> jsonSet(String key, Path path, Object object, JsonSetParams params) {
        return null;
    }

    @Override
    public Response<String> jsonSetWithEscape(String key, Path2 path, Object object, JsonSetParams params) {
        return null;
    }

    @Override
    public Response<String> jsonSet(String key, Path2 path, Object object, JsonSetParams params) {
        return null;
    }

    @Override
    public Response<String> jsonSet(String key, Path path, Object object) {
        return null;
    }

    @Override
    public Response<String> jsonSetWithEscape(String key, Path2 path, Object object) {
        return null;
    }

    @Override
    public Response<String> jsonSet(String key, Path2 path, Object object) {
        return null;
    }

    @Override
    public Response<LCSMatchResult> lcs(byte[] keyA, byte[] keyB, LCSParams params) {
        return null;
    }

    @Override
    public Response<Long> ftSugLen(String key) {
        return null;
    }

    @Override
    public Response<Boolean> ftSugDel(String key, String string) {
        return null;
    }

    @Override
    public Response<List<Tuple>> ftSugGetWithScores(String key, String prefix, boolean fuzzy, int max) {
        return null;
    }

    @Override
    public Response<List<Tuple>> ftSugGetWithScores(String key, String prefix) {
        return null;
    }

    @Override
    public Response<List<String>> ftSugGet(String key, String prefix, boolean fuzzy, int max) {
        return null;
    }

    @Override
    public Response<List<String>> ftSugGet(String key, String prefix) {
        return null;
    }

    @Override
    public Response<Long> ftSugAddIncr(String key, String string, double score) {
        return null;
    }

    @Override
    public Response<Long> ftSugAdd(String key, String string, double score) {
        return null;
    }

    @Override
    public Response<String> ftConfigSet(String indexName, String option, String value) {
        return null;
    }

    @Override
    public Response<String> ftConfigSet(String option, String value) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> ftConfigGet(String indexName, String option) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> ftConfigGet(String option) {
        return null;
    }

    @Override
    public Response<Set<String>> ftTagVals(String indexName, String fieldName) {
        return null;
    }

    @Override
    public Response<Map<String, Object>> ftInfo(String indexName) {
        return null;
    }

    @Override
    public Response<Map<String, Map<String, Double>>> ftSpellCheck(String index, String query,
        FTSpellCheckParams spellCheckParams) {
        return null;
    }

    @Override
    public Response<Map<String, Map<String, Double>>> ftSpellCheck(String index, String query) {
        return null;
    }

    @Override
    public Response<Set<String>> ftDictDumpBySampleKey(String indexName, String dictionary) {
        return null;
    }
}
