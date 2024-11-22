// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis.mocking;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.Connection;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Module;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.args.BitCountOption;
import redis.clients.jedis.args.BitOP;
import redis.clients.jedis.args.ClientAttributeOption;
import redis.clients.jedis.args.ClientPauseMode;
import redis.clients.jedis.args.ClientType;
import redis.clients.jedis.args.ClusterFailoverOption;
import redis.clients.jedis.args.ClusterResetType;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.args.FlushMode;
import redis.clients.jedis.args.FunctionRestorePolicy;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.args.LatencyEvent;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.args.SaveMode;
import redis.clients.jedis.args.SortedSetOption;
import redis.clients.jedis.args.UnblockType;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.BitPosParams;
import redis.clients.jedis.params.ClientKillParams;
import redis.clients.jedis.params.CommandListFilterByParams;
import redis.clients.jedis.params.FailoverParams;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.LCSParams;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.params.LolwutParams;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.params.ModuleLoadExParams;
import redis.clients.jedis.params.RestoreParams;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ShutdownParams;
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
import redis.clients.jedis.resps.AccessControlLogEntry;
import redis.clients.jedis.resps.AccessControlUser;
import redis.clients.jedis.resps.ClusterShardInfo;
import redis.clients.jedis.resps.CommandDocument;
import redis.clients.jedis.resps.CommandInfo;
import redis.clients.jedis.resps.FunctionStats;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.LCSMatchResult;
import redis.clients.jedis.resps.LatencyHistoryInfo;
import redis.clients.jedis.resps.LatencyLatestInfo;
import redis.clients.jedis.resps.LibraryInfo;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Slowlog;
import redis.clients.jedis.resps.StreamConsumerInfo;
import redis.clients.jedis.resps.StreamConsumersInfo;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamFullInfo;
import redis.clients.jedis.resps.StreamGroupInfo;
import redis.clients.jedis.resps.StreamInfo;
import redis.clients.jedis.resps.StreamPendingEntry;
import redis.clients.jedis.resps.StreamPendingSummary;
import redis.clients.jedis.resps.TrackingInfo;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.util.KeyValue;
import redis.clients.jedis.util.Pool;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockJedis extends Jedis {
    @Override
    public StreamEntryID xadd(String key, Map<String, String> hash, XAddParams params) {
        return null;
    }

    @Override
    public byte[] xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return null;
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        return null;
    }

    @Override
    public void shutdown(SaveMode saveMode) throws JedisException {
    }

    @Override
    public ScanResult<String> hscanNoValues(String key, String cursor) {
        return null;
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return null;
    }

    @Override
    public ScanResult<byte[]> hscanNoValues(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public Object sendBlockingCommand(ProtocolCommand cmd, String... args) {
        return null;
    }

    @Override
    public Object sendCommand(ProtocolCommand cmd, String... args) {
        return null;
    }

    @Override
    public Object functionStatsBinary() {
        return null;
    }

    @Override
    public String functionRestore(byte[] serializedValue, FunctionRestorePolicy policy) {
        return "";
    }

    @Override
    public String functionRestore(byte[] serializedValue) {
        return "";
    }

    @Override
    public String functionLoadReplace(byte[] functionCode) {
        return "";
    }

    @Override
    public String functionLoad(byte[] functionCode) {
        return "";
    }

    @Override
    public List<Object> functionListWithCode(byte[] libraryNamePattern) {
        return null;
    }

    @Override
    public List<Object> functionListWithCodeBinary() {
        return null;
    }

    @Override
    public List<Object> functionList(byte[] libraryNamePattern) {
        return null;
    }

    @Override
    public List<Object> functionListBinary() {
        return null;
    }

    @Override
    public byte[] functionDump() {
        return null;
    }

    @Override
    public String functionDelete(byte[] libraryName) {
        return "";
    }

    @Override
    public Object fcallReadonly(byte[] name, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Object fcall(byte[] name, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public List<StreamConsumerInfo> xinfoConsumers2(String key, String group) {
        return null;
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
        return null;
    }

    @Override
    public List<StreamGroupInfo> xinfoGroups(String key) {
        return null;
    }

    @Override
    public StreamFullInfo xinfoStreamFull(String key, int count) {
        return null;
    }

    @Override
    public StreamFullInfo xinfoStreamFull(String key) {
        return null;
    }

    @Override
    public StreamInfo xinfoStream(String key) {
        return null;
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(String key, String group, String consumerName,
        long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return null;
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntry>> xautoclaim(String key, String group, String consumerName,
        long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return null;
    }

    @Override
    public List<StreamEntryID> xclaimJustId(String key, String group, String consumerName, long minIdleTime,
        XClaimParams params, StreamEntryID... ids) {
        return null;
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumerName, long minIdleTime,
        XClaimParams params, StreamEntryID... ids) {
        return null;
    }

    @Override
    public List<StreamPendingEntry> xpending(String key, String groupName, XPendingParams params) {
        return null;
    }

    @Override
    public StreamPendingSummary xpending(String key, String groupName) {
        return null;
    }

    @Override
    public Map<String, List<StreamEntry>> xreadGroupAsMap(String groupName, String consumer,
        XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupName, String consumer,
        XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public long xtrim(String key, XTrimParams params) {
        return 0;
    }

    @Override
    public long xtrim(String key, long maxLen, boolean approximateLength) {
        return 0;
    }

    @Override
    public long xdel(String key, StreamEntryID... ids) {
        return 0;
    }

    @Override
    public long xgroupDelConsumer(String key, String groupName, String consumerName) {
        return 0;
    }

    @Override
    public boolean xgroupCreateConsumer(String key, String groupName, String consumerName) {
        return false;
    }

    @Override
    public long xgroupDestroy(String key, String groupName) {
        return 0;
    }

    @Override
    public String xgroupSetID(String key, String groupName, StreamEntryID id) {
        return "";
    }

    @Override
    public String xgroupCreate(String key, String groupName, StreamEntryID id, boolean makeStream) {
        return "";
    }

    @Override
    public long xack(String key, String group, StreamEntryID... ids) {
        return 0;
    }

    @Override
    public Map<String, List<StreamEntry>> xreadAsMap(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams,
        Map<String, StreamEntryID> streams) {
        return null;
    }

    @Override
    public List<StreamEntry> xrevrange(String key, String end, String start, int count) {
        return null;
    }

    @Override
    public List<StreamEntry> xrevrange(String key, String end, String start) {
        return null;
    }

    @Override
    public List<StreamEntry> xrange(String key, String start, String end, int count) {
        return null;
    }

    @Override
    public List<StreamEntry> xrange(String key, String start, String end) {
        return null;
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        return null;
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        return null;
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        return null;
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
        return null;
    }

    @Override
    public long xlen(String key) {
        return 0;
    }

    @Override
    public StreamEntryID xadd(String key, XAddParams params, Map<String, String> hash) {
        return null;
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        return null;
    }

    @Override
    public long latencyReset(LatencyEvent... events) {
        return 0;
    }

    @Override
    public List<LatencyHistoryInfo> latencyHistory(LatencyEvent event) {
        return null;
    }

    @Override
    public Map<String, LatencyLatestInfo> latencyLatest() {
        return null;
    }

    @Override
    public String latencyDoctor() {
        return "";
    }

    @Override
    public String reset() {
        return "";
    }

    @Override
    public String lolwut(LolwutParams lolwutParams) {
        return "";
    }

    @Override
    public String lolwut() {
        return "";
    }

    @Override
    public Map<String, Object> memoryStats() {
        return null;
    }

    @Override
    public String memoryPurge() {
        return "";
    }

    @Override
    public Long memoryUsage(String key, int samples) {
        return null;
    }

    @Override
    public Long memoryUsage(String key) {
        return null;
    }

    @Override
    public String memoryDoctor() {
        return "";
    }

    @Override
    public List<Long> hpersist(String key, String... fields) {
        return null;
    }

    @Override
    public List<Long> hpttl(String key, String... fields) {
        return null;
    }

    @Override
    public List<Long> httl(String key, String... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpireTime(String key, String... fields) {
        return null;
    }

    @Override
    public List<Long> hexpireTime(String key, String... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpireAt(String key, long unixTimeMillis, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpireAt(String key, long unixTimeMillis, String... fields) {
        return null;
    }

    @Override
    public List<Long> hexpireAt(String key, long unixTimeSeconds, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public List<Long> hexpireAt(String key, long unixTimeSeconds, String... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpire(String key, long milliseconds, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpire(String key, long milliseconds, String... fields) {
        return null;
    }

    @Override
    public List<Long> hexpire(String key, long seconds, ExpiryOption condition, String... fields) {
        return null;
    }

    @Override
    public List<Long> hexpire(String key, long seconds, String... fields) {
        return null;
    }

    @Override
    public long hstrlen(String key, String field) {
        return 0;
    }

    @Override
    public List<Long> bitfieldReadonly(String key, String... arguments) {
        return null;
    }

    @Override
    public List<Long> bitfield(String key, String... arguments) {
        return null;
    }

    @Override
    public List<Module> moduleList() {
        return null;
    }

    @Override
    public String moduleUnload(String name) {
        return "";
    }

    @Override
    public String moduleLoadEx(String path, ModuleLoadExParams params) {
        return "";
    }

    @Override
    public String moduleLoad(String path, String... args) {
        return "";
    }

    @Override
    public String moduleLoad(String path) {
        return "";
    }

    @Override
    public long geosearchStoreStoreDist(String dest, String src, GeoSearchParam params) {
        return 0;
    }

    @Override
    public long geosearchStore(String dest, String src, GeoSearchParam params) {
        return 0;
    }

    @Override
    public long geosearchStore(String dest, String src, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return 0;
    }

    @Override
    public long geosearchStore(String dest, String src, String member, double width, double height, GeoUnit unit) {
        return 0;
    }

    @Override
    public long geosearchStore(String dest, String src, GeoCoordinate coord, double radius, GeoUnit unit) {
        return 0;
    }

    @Override
    public long geosearchStore(String dest, String src, String member, double radius, GeoUnit unit) {
        return 0;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(String key, GeoSearchParam params) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(String key, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(String key, String member, double width, double height, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(String key, GeoCoordinate coord, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(String key, String member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param,
        GeoRadiusStoreParam storeParam) {
        return 0;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius,
        GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit,
        GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return 0;
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        return null;
    }

    @Override
    public List<String> geohash(String key, String... members) {
        return null;
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return null;
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        return null;
    }

    @Override
    public long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        return 0;
    }

    @Override
    public long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return 0;
    }

    @Override
    public long geoadd(String key, double longitude, double latitude, String member) {
        return 0;
    }

    @Override
    public List<LibraryInfo> functionListWithCode(String libraryNamePattern) {
        return null;
    }

    @Override
    public List<LibraryInfo> functionListWithCode() {
        return null;
    }

    @Override
    public List<LibraryInfo> functionList(String libraryNamePattern) {
        return null;
    }

    @Override
    public List<LibraryInfo> functionList() {
        return null;
    }

    @Override
    public String functionKill() {
        return "";
    }

    @Override
    public String functionFlush(FlushMode mode) {
        return "";
    }

    @Override
    public String functionFlush() {
        return "";
    }

    @Override
    public FunctionStats functionStats() {
        return null;
    }

    @Override
    public String functionLoadReplace(String functionCode) {
        return "";
    }

    @Override
    public String functionLoad(String functionCode) {
        return "";
    }

    @Override
    public String functionDelete(String libraryName) {
        return "";
    }

    @Override
    public Object fcallReadonly(String name, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Object fcall(String name, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public String pfmerge(String destkey, String... sourcekeys) {
        return "";
    }

    @Override
    public long pfcount(String... keys) {
        return 0;
    }

    @Override
    public long pfcount(String key) {
        return 0;
    }

    @Override
    public long pfadd(String key, String... elements) {
        return 0;
    }

    @Override
    public String asking() {
        return "";
    }

    @Override
    public String clusterDelSlotsRange(int... ranges) {
        return "";
    }

    @Override
    public String clusterAddSlotsRange(int... ranges) {
        return "";
    }

    @Override
    public List<Map<String, Object>> clusterLinks() {
        return null;
    }

    @Override
    public String clusterMyShardId() {
        return "";
    }

    @Override
    public String clusterMyId() {
        return "";
    }

    @Override
    public List<ClusterShardInfo> clusterShards() {
        return null;
    }

    @Override
    public List<Object> clusterSlots() {
        return null;
    }

    @Override
    public String clusterFailover(ClusterFailoverOption failoverOption) {
        return "";
    }

    @Override
    public String clusterFailover() {
        return "";
    }

    @Override
    public List<String> clusterReplicas(String nodeId) {
        return null;
    }

    @Override
    public List<String> clusterSlaves(String nodeId) {
        return null;
    }

    @Override
    public String clusterReplicate(String nodeId) {
        return "";
    }

    @Override
    public String clusterBumpEpoch() {
        return "";
    }

    @Override
    public String clusterSetConfigEpoch(long configEpoch) {
        return "";
    }

    @Override
    public String clusterSaveConfig() {
        return "";
    }

    @Override
    public long clusterCountKeysInSlot(int slot) {
        return 0;
    }

    @Override
    public long clusterCountFailureReports(String nodeId) {
        return 0;
    }

    @Override
    public long clusterKeySlot(String key) {
        return 0;
    }

    @Override
    public String clusterFlushSlots() {
        return "";
    }

    @Override
    public String clusterForget(String nodeId) {
        return "";
    }

    @Override
    public String clusterSetSlotStable(int slot) {
        return "";
    }

    @Override
    public String clusterSetSlotImporting(int slot, String nodeId) {
        return "";
    }

    @Override
    public String clusterSetSlotMigrating(int slot, String nodeId) {
        return "";
    }

    @Override
    public String clusterSetSlotNode(int slot, String nodeId) {
        return "";
    }

    @Override
    public List<byte[]> clusterGetKeysInSlotBinary(int slot, int count) {
        return null;
    }

    @Override
    public List<String> clusterGetKeysInSlot(int slot, int count) {
        return null;
    }

    @Override
    public String clusterInfo() {
        return "";
    }

    @Override
    public String clusterDelSlots(int... slots) {
        return "";
    }

    @Override
    public String clusterAddSlots(int... slots) {
        return "";
    }

    @Override
    public String clusterReset(ClusterResetType resetType) {
        return "";
    }

    @Override
    public String clusterReset() {
        return "";
    }

    @Override
    public String clusterMeet(String ip, int port) {
        return "";
    }

    @Override
    public String clusterNodes() {
        return "";
    }

    @Override
    public String readwrite() {
        return "";
    }

    @Override
    public String readonly() {
        return "";
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<String> hscanNoValues(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params, String type) {
        return null;
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<String> scan(String cursor) {
        return null;
    }

    @Override
    public String migrate(String host, int port, int timeout, MigrateParams params, String... keys) {
        return "";
    }

    @Override
    public String migrate(String host, int port, String key, int timeout) {
        return "";
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
        return "";
    }

    @Override
    public String migrate(String host, int port, String key, int destinationDb, int timeout) {
        return "";
    }

    @Override
    public String clientSetname(String name) {
        return "";
    }

    @Override
    public String clientSetInfo(ClientAttributeOption attr, String value) {
        return "";
    }

    @Override
    public String clientInfo() {
        return "";
    }

    @Override
    public String clientList(long... clientIds) {
        return "";
    }

    @Override
    public String clientList(ClientType type) {
        return "";
    }

    @Override
    public String clientList() {
        return "";
    }

    @Override
    public String clientGetname() {
        return "";
    }

    @Override
    public String clientKill(String ipPort) {
        return "";
    }

    @Override
    public byte[] aclDryRunBinary(byte[] username, CommandArguments commandArgs) {
        return null;
    }

    @Override
    public byte[] aclDryRunBinary(byte[] username, byte[] command, byte[]... args) {
        return null;
    }

    @Override
    public String aclDryRun(String username, CommandArguments commandArgs) {
        return "";
    }

    @Override
    public String aclDryRun(String username, String command, String... args) {
        return "";
    }

    @Override
    public String aclGenPass(int bits) {
        return "";
    }

    @Override
    public String aclGenPass() {
        return "";
    }

    @Override
    public String aclSave() {
        return "";
    }

    @Override
    public String aclLoad() {
        return "";
    }

    @Override
    public List<AccessControlLogEntry> aclLog(int limit) {
        return null;
    }

    @Override
    public List<AccessControlLogEntry> aclLog() {
        return null;
    }

    @Override
    public List<String> aclCat(String category) {
        return null;
    }

    @Override
    public List<String> aclCat() {
        return null;
    }

    @Override
    public String aclWhoAmI() {
        return "";
    }

    @Override
    public List<String> aclList() {
        return null;
    }

    @Override
    public List<String> aclUsers() {
        return null;
    }

    @Override
    public AccessControlUser aclGetUser(String name) {
        return null;
    }

    @Override
    public long aclDelUser(String... names) {
        return 0;
    }

    @Override
    public String aclSetUser(String name, String... rules) {
        return "";
    }

    @Override
    public String aclSetUser(String name) {
        return "";
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return "";
    }

    @Override
    public long pttl(String key) {
        return 0;
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        return "";
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue) {
        return "";
    }

    @Override
    public byte[] dump(String key) {
        return null;
    }

    @Override
    public String sentinelSet(String masterName, Map<String, String> parameterMap) {
        return "";
    }

    @Override
    public String sentinelRemove(String masterName) {
        return "";
    }

    @Override
    public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
        return "";
    }

    @Override
    public String sentinelFailover(String masterName) {
        return "";
    }

    @Override
    public List<Map<String, String>> sentinelReplicas(String masterName) {
        return null;
    }

    @Override
    public List<Map<String, String>> sentinelSlaves(String masterName) {
        return null;
    }

    @Override
    public Long sentinelReset(String pattern) {
        return null;
    }

    @Override
    public List<String> sentinelGetMasterAddrByName(String masterName) {
        return null;
    }

    @Override
    public List<Map<String, String>> sentinelSentinels(String masterName) {
        return null;
    }

    @Override
    public Map<String, String> sentinelMaster(String masterName) {
        return null;
    }

    @Override
    public List<Map<String, String>> sentinelMasters() {
        return null;
    }

    @Override
    public String sentinelMyId() {
        return "";
    }

    @Override
    public List<String> commandListFilterBy(CommandListFilterByParams filterByParams) {
        return null;
    }

    @Override
    public List<String> commandList() {
        return null;
    }

    @Override
    public Map<String, CommandInfo> commandInfo(String... commands) {
        return null;
    }

    @Override
    public List<KeyValue<String, List<String>>> commandGetKeysAndFlags(String... command) {
        return null;
    }

    @Override
    public List<String> commandGetKeys(String... command) {
        return null;
    }

    @Override
    public Map<String, CommandDocument> commandDocs(String... commands) {
        return null;
    }

    @Override
    public long commandCount() {
        return 0;
    }

    @Override
    public long bitop(BitOP op, String destKey, String... srcKeys) {
        return 0;
    }

    @Override
    public long bitcount(String key, long start, long end, BitCountOption option) {
        return 0;
    }

    @Override
    public long bitcount(String key, long start, long end) {
        return 0;
    }

    @Override
    public long bitcount(String key) {
        return 0;
    }

    @Override
    public Long objectFreq(String key) {
        return null;
    }

    @Override
    public List<String> objectHelp() {
        return null;
    }

    @Override
    public Long objectIdletime(String key) {
        return null;
    }

    @Override
    public String objectEncoding(String key) {
        return "";
    }

    @Override
    public Long objectRefcount(String key) {
        return null;
    }

    @Override
    public List<Slowlog> slowlogGet(long entries) {
        return null;
    }

    @Override
    public List<Slowlog> slowlogGet() {
        return null;
    }

    @Override
    public String scriptLoad(String script) {
        return "";
    }

    @Override
    public List<Boolean> scriptExists(String... sha1) {
        return null;
    }

    @Override
    public Boolean scriptExists(String sha1) {
        return null;
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String... params) {
        return null;
    }

    @Override
    public Object evalshaReadonly(String sha1, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Object evalsha(String sha1) {
        return null;
    }

    @Override
    public Object eval(String script) {
        return null;
    }

    @Override
    public Object evalReadonly(String script, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return null;
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        return null;
    }

    @Override
    public Map<String, Long> pubsubShardNumSub(String... channels) {
        return null;
    }

    @Override
    public List<String> pubsubShardChannels(String pattern) {
        return null;
    }

    @Override
    public List<String> pubsubShardChannels() {
        return null;
    }

    @Override
    public Map<String, Long> pubsubNumSub(String... channels) {
        return null;
    }

    @Override
    public Long pubsubNumPat() {
        return null;
    }

    @Override
    public List<String> pubsubChannels(String pattern) {
        return null;
    }

    @Override
    public List<String> pubsubChannels() {
        return null;
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
    }

    @Override
    public long publish(String channel, String message) {
        return 0;
    }

    @Override
    public String configSet(Map<String, String> parameterValues) {
        return "";
    }

    @Override
    public String configSet(String... parameterValues) {
        return "";
    }

    @Override
    public String configSet(String parameter, String value) {
        return "";
    }

    @Override
    public Map<String, String> configGet(String... patterns) {
        return null;
    }

    @Override
    public Map<String, String> configGet(String pattern) {
        return null;
    }

    @Override
    public List<Object> role() {
        return null;
    }

    @Override
    public long bitpos(String key, boolean value, BitPosParams params) {
        return 0;
    }

    @Override
    public long bitpos(String key, boolean value) {
        return 0;
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return "";
    }

    @Override
    public long setrange(String key, long offset, String value) {
        return 0;
    }

    @Override
    public boolean getbit(String key, long offset) {
        return false;
    }

    @Override
    public boolean setbit(String key, long offset, boolean value) {
        return false;
    }

    @Override
    public String brpoplpush(String source, String destination, int timeout) {
        return "";
    }

    @Override
    public long linsert(String key, ListPosition where, String pivot, String value) {
        return 0;
    }

    @Override
    public String echo(String string) {
        return "";
    }

    @Override
    public long rpushx(String key, String... strings) {
        return 0;
    }

    @Override
    public long persist(String key) {
        return 0;
    }

    @Override
    public long lpushx(String key, String... strings) {
        return 0;
    }

    @Override
    public LCSMatchResult lcs(String keyA, String keyB, LCSParams params) {
        return null;
    }

    @Override
    public long strlen(String key) {
        return 0;
    }

    @Override
    public KeyValue<String, List<Tuple>> bzmpop(double timeout, SortedSetOption option, int count, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, List<Tuple>> bzmpop(double timeout, SortedSetOption option, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, List<Tuple>> zmpop(SortedSetOption option, int count, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, List<Tuple>> zmpop(SortedSetOption option, String... keys) {
        return null;
    }

    @Override
    public long zremrangeByLex(String key, String min, String max) {
        return 0;
    }

    @Override
    public List<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Override
    public List<String> zrevrangeByLex(String key, String max, String min) {
        return null;
    }

    @Override
    public List<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public List<String> zrangeByLex(String key, String min, String max) {
        return null;
    }

    @Override
    public long zlexcount(String key, String min, String max) {
        return 0;
    }

    @Override
    public long zinterstore(String dstkey, ZParams params, String... sets) {
        return 0;
    }

    @Override
    public long zinterstore(String dstkey, String... sets) {
        return 0;
    }

    @Override
    public long zintercard(long limit, String... keys) {
        return 0;
    }

    @Override
    public long zintercard(String... keys) {
        return 0;
    }

    @Override
    public List<Tuple> zinterWithScores(ZParams params, String... keys) {
        return null;
    }

    @Override
    public List<String> zinter(ZParams params, String... keys) {
        return null;
    }

    @Override
    public long zunionstore(String dstkey, ZParams params, String... sets) {
        return 0;
    }

    @Override
    public long zunionstore(String dstkey, String... sets) {
        return 0;
    }

    @Override
    public List<Tuple> zunionWithScores(ZParams params, String... keys) {
        return null;
    }

    @Override
    public List<String> zunion(ZParams params, String... keys) {
        return null;
    }

    @Override
    public long zremrangeByScore(String key, String min, String max) {
        return 0;
    }

    @Override
    public long zremrangeByScore(String key, double min, double max) {
        return 0;
    }

    @Override
    public long zremrangeByRank(String key, long start, long stop) {
        return 0;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return null;
    }

    @Override
    public List<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return null;
    }

    @Override
    public List<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public List<String> zrevrangeByScore(String key, String max, String min) {
        return null;
    }

    @Override
    public List<String> zrevrangeByScore(String key, double max, double min) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return null;
    }

    @Override
    public List<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public List<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public List<String> zrangeByScore(String key, String min, String max) {
        return null;
    }

    @Override
    public List<String> zrangeByScore(String key, double min, double max) {
        return null;
    }

    @Override
    public long zcount(String key, String min, String max) {
        return 0;
    }

    @Override
    public long zcount(String key, double min, double max) {
        return 0;
    }

    @Override
    public KeyValue<String, String> brpop(double timeout, String key) {
        return null;
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        return null;
    }

    @Override
    public KeyValue<String, String> blpop(double timeout, String key) {
        return null;
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return null;
    }

    @Override
    public KeyValue<String, Tuple> bzpopmin(double timeout, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, Tuple> bzpopmax(double timeout, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, List<String>> blmpop(double timeout, ListDirection direction, int count, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, List<String>> blmpop(double timeout, ListDirection direction, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, List<String>> lmpop(ListDirection direction, int count, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, List<String>> lmpop(ListDirection direction, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, String> brpop(double timeout, String... keys) {
        return null;
    }

    @Override
    public List<String> brpop(int timeout, String... keys) {
        return null;
    }

    @Override
    public KeyValue<String, String> blpop(double timeout, String... keys) {
        return null;
    }

    @Override
    public List<String> blpop(int timeout, String... keys) {
        return null;
    }

    @Override
    public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        return "";
    }

    @Override
    public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        return "";
    }

    @Override
    public long sort(String key, String dstkey) {
        return 0;
    }

    @Override
    public List<String> sortReadonly(String key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public long sort(String key, SortingParams sortingParams, String dstkey) {
        return 0;
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public List<String> sort(String key) {
        return null;
    }

    @Override
    public String watch(String... keys) {
        return "";
    }

    @Override
    public List<Tuple> zpopmin(String key, int count) {
        return null;
    }

    @Override
    public Tuple zpopmin(String key) {
        return null;
    }

    @Override
    public List<Tuple> zpopmax(String key, int count) {
        return null;
    }

    @Override
    public Tuple zpopmax(String key) {
        return null;
    }

    @Override
    public List<Double> zmscore(String key, String... members) {
        return null;
    }

    @Override
    public Double zscore(String key, String member) {
        return null;
    }

    @Override
    public long zcard(String key) {
        return 0;
    }

    @Override
    public List<Tuple> zrandmemberWithScores(String key, long count) {
        return null;
    }

    @Override
    public List<String> zrandmember(String key, long count) {
        return null;
    }

    @Override
    public String zrandmember(String key) {
        return "";
    }

    @Override
    public long zrangestore(String dest, String src, ZRangeParams zRangeParams) {
        return 0;
    }

    @Override
    public List<Tuple> zrangeWithScores(String key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public List<String> zrange(String key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return null;
    }

    @Override
    public List<Tuple> zrangeWithScores(String key, long start, long stop) {
        return null;
    }

    @Override
    public List<String> zrevrange(String key, long start, long stop) {
        return null;
    }

    @Override
    public KeyValue<Long, Double> zrevrankWithScore(String key, String member) {
        return null;
    }

    @Override
    public KeyValue<Long, Double> zrankWithScore(String key, String member) {
        return null;
    }

    @Override
    public Long zrevrank(String key, String member) {
        return null;
    }

    @Override
    public Long zrank(String key, String member) {
        return null;
    }

    @Override
    public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        return null;
    }

    @Override
    public double zincrby(String key, double increment, String member) {
        return 0;
    }

    @Override
    public long zrem(String key, String... members) {
        return 0;
    }

    @Override
    public List<String> zrange(String key, long start, long stop) {
        return null;
    }

    @Override
    public long zdiffstore(String dstkey, String... keys) {
        return 0;
    }

    @Override
    public long zdiffStore(String dstkey, String... keys) {
        return 0;
    }

    @Override
    public List<Tuple> zdiffWithScores(String... keys) {
        return null;
    }

    @Override
    public List<String> zdiff(String... keys) {
        return null;
    }

    @Override
    public Double zaddIncr(String key, double score, String member, ZAddParams params) {
        return null;
    }

    @Override
    public long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return 0;
    }

    @Override
    public long zadd(String key, Map<String, Double> scoreMembers) {
        return 0;
    }

    @Override
    public long zadd(String key, double score, String member, ZAddParams params) {
        return 0;
    }

    @Override
    public long zadd(String key, double score, String member) {
        return 0;
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return null;
    }

    @Override
    public String srandmember(String key) {
        return "";
    }

    @Override
    public long sdiffstore(String dstkey, String... keys) {
        return 0;
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return null;
    }

    @Override
    public long sunionstore(String dstkey, String... keys) {
        return 0;
    }

    @Override
    public Set<String> sunion(String... keys) {
        return null;
    }

    @Override
    public long sintercard(int limit, String... keys) {
        return 0;
    }

    @Override
    public long sintercard(String... keys) {
        return 0;
    }

    @Override
    public long sinterstore(String dstkey, String... keys) {
        return 0;
    }

    @Override
    public Set<String> sinter(String... keys) {
        return null;
    }

    @Override
    public List<Boolean> smismember(String key, String... members) {
        return null;
    }

    @Override
    public boolean sismember(String key, String member) {
        return false;
    }

    @Override
    public long scard(String key) {
        return 0;
    }

    @Override
    public long smove(String srckey, String dstkey, String member) {
        return 0;
    }

    @Override
    public Set<String> spop(String key, long count) {
        return null;
    }

    @Override
    public String spop(String key) {
        return "";
    }

    @Override
    public long srem(String key, String... members) {
        return 0;
    }

    @Override
    public Set<String> smembers(String key) {
        return null;
    }

    @Override
    public long sadd(String key, String... members) {
        return 0;
    }

    @Override
    public String rpoplpush(String srckey, String dstkey) {
        return "";
    }

    @Override
    public List<String> rpop(String key, int count) {
        return null;
    }

    @Override
    public String rpop(String key) {
        return "";
    }

    @Override
    public List<Long> lpos(String key, String element, LPosParams params, long count) {
        return null;
    }

    @Override
    public Long lpos(String key, String element, LPosParams params) {
        return null;
    }

    @Override
    public Long lpos(String key, String element) {
        return null;
    }

    @Override
    public List<String> lpop(String key, int count) {
        return null;
    }

    @Override
    public String lpop(String key) {
        return "";
    }

    @Override
    public long lrem(String key, long count, String value) {
        return 0;
    }

    @Override
    public String lset(String key, long index, String value) {
        return "";
    }

    @Override
    public String lindex(String key, long index) {
        return "";
    }

    @Override
    public String ltrim(String key, long start, long stop) {
        return "";
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        return null;
    }

    @Override
    public long llen(String key) {
        return 0;
    }

    @Override
    public long lpush(String key, String... strings) {
        return 0;
    }

    @Override
    public long rpush(String key, String... strings) {
        return 0;
    }

    @Override
    public List<Map.Entry<String, String>> hrandfieldWithValues(String key, long count) {
        return null;
    }

    @Override
    public List<String> hrandfield(String key, long count) {
        return null;
    }

    @Override
    public String hrandfield(String key) {
        return "";
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return null;
    }

    @Override
    public List<String> hvals(String key) {
        return null;
    }

    @Override
    public Set<String> hkeys(String key) {
        return null;
    }

    @Override
    public long hlen(String key) {
        return 0;
    }

    @Override
    public long hdel(String key, String... fields) {
        return 0;
    }

    @Override
    public boolean hexists(String key, String field) {
        return false;
    }

    @Override
    public double hincrByFloat(String key, String field, double value) {
        return 0;
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        return 0;
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return null;
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return "";
    }

    @Override
    public long hsetnx(String key, String field, String value) {
        return 0;
    }

    @Override
    public String hget(String key, String field) {
        return "";
    }

    @Override
    public long hset(String key, Map<String, String> hash) {
        return 0;
    }

    @Override
    public long hset(String key, String field, String value) {
        return 0;
    }

    @Override
    public String substr(String key, int start, int end) {
        return "";
    }

    @Override
    public long append(String key, String value) {
        return 0;
    }

    @Override
    public long incr(String key) {
        return 0;
    }

    @Override
    public double incrByFloat(String key, double increment) {
        return 0;
    }

    @Override
    public long incrBy(String key, long increment) {
        return 0;
    }

    @Override
    public long decr(String key) {
        return 0;
    }

    @Override
    public long decrBy(String key, long decrement) {
        return 0;
    }

    @Override
    public long msetnx(String... keysvalues) {
        return 0;
    }

    @Override
    public String mset(String... keysvalues) {
        return "";
    }

    @Override
    public String setex(String key, long seconds, String value) {
        return "";
    }

    @Override
    public long setnx(String key, String value) {
        return 0;
    }

    @Override
    public List<String> mget(String... keys) {
        return null;
    }

    @Override
    public String getSet(String key, String value) {
        return "";
    }

    @Override
    public long move(String key, int dbIndex) {
        return 0;
    }

    @Override
    public long touch(String key) {
        return 0;
    }

    @Override
    public long touch(String... keys) {
        return 0;
    }

    @Override
    public long ttl(String key) {
        return 0;
    }

    @Override
    public long pexpireAt(String key, long millisecondsTimestamp, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long pexpireAt(String key, long millisecondsTimestamp) {
        return 0;
    }

    @Override
    public long expireAt(String key, long unixTime, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long expireAt(String key, long unixTime) {
        return 0;
    }

    @Override
    public long pexpireTime(String key) {
        return 0;
    }

    @Override
    public long expireTime(String key) {
        return 0;
    }

    @Override
    public long pexpire(String key, long milliseconds, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long pexpire(String key, long milliseconds) {
        return 0;
    }

    @Override
    public long expire(String key, long seconds, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long expire(String key, long seconds) {
        return 0;
    }

    @Override
    public long renamenx(String oldkey, String newkey) {
        return 0;
    }

    @Override
    public String rename(String oldkey, String newkey) {
        return "";
    }

    @Override
    public String randomKey() {
        return "";
    }

    @Override
    public Set<String> keys(String pattern) {
        return null;
    }

    @Override
    public String type(String key) {
        return "";
    }

    @Override
    public long unlink(String key) {
        return 0;
    }

    @Override
    public long unlink(String... keys) {
        return 0;
    }

    @Override
    public long del(String key) {
        return 0;
    }

    @Override
    public long del(String... keys) {
        return 0;
    }

    @Override
    public boolean exists(String key) {
        return false;
    }

    @Override
    public long exists(String... keys) {
        return 0;
    }

    @Override
    public String getEx(String key, GetExParams params) {
        return "";
    }

    @Override
    public String getDel(String key) {
        return "";
    }

    @Override
    public String setGet(String key, String value, SetParams params) {
        return "";
    }

    @Override
    public String setGet(String key, String value) {
        return "";
    }

    @Override
    public String get(String key) {
        return "";
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return "";
    }

    @Override
    public String set(String key, String value) {
        return "";
    }

    @Override
    public String ping(String message) {
        return "";
    }

    @Override
    public boolean copy(String srcKey, String dstKey, boolean replace) {
        return false;
    }

    @Override
    public boolean copy(String srcKey, String dstKey, int db, boolean replace) {
        return false;
    }

    @Override
    public Object sendCommand(ProtocolCommand cmd) {
        return null;
    }

    @Override
    public Object sendBlockingCommand(ProtocolCommand cmd, byte[]... args) {
        return null;
    }

    @Override
    public Object sendCommand(ProtocolCommand cmd, byte[]... args) {
        return null;
    }

    @Override
    public List<Object> xinfoConsumers(byte[] key, byte[] group) {
        return null;
    }

    @Override
    public List<Object> xinfoGroups(byte[] key) {
        return null;
    }

    @Override
    public Object xinfoStreamFull(byte[] key, int count) {
        return null;
    }

    @Override
    public Object xinfoStreamFull(byte[] key) {
        return null;
    }

    @Override
    public Object xinfoStream(byte[] key) {
        return null;
    }

    @Override
    public List<Object> xautoclaimJustId(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime,
        byte[] start, XAutoClaimParams params) {
        return null;
    }

    @Override
    public List<Object> xautoclaim(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start,
        XAutoClaimParams params) {
        return null;
    }

    @Override
    public List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumerName, long minIdleTime,
        XClaimParams params, byte[]... ids) {
        return null;
    }

    @Override
    public List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumerName, long minIdleTime, XClaimParams params,
        byte[]... ids) {
        return null;
    }

    @Override
    public List<Object> xpending(byte[] key, byte[] groupName, XPendingParams params) {
        return null;
    }

    @Override
    public Object xpending(byte[] key, byte[] groupName) {
        return null;
    }

    @Override
    public long xtrim(byte[] key, XTrimParams params) {
        return 0;
    }

    @Override
    public long xtrim(byte[] key, long maxLen, boolean approximateLength) {
        return 0;
    }

    @Override
    public long xdel(byte[] key, byte[]... ids) {
        return 0;
    }

    @Override
    public long xgroupDelConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
        return 0;
    }

    @Override
    public boolean xgroupCreateConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
        return false;
    }

    @Override
    public long xgroupDestroy(byte[] key, byte[] consumer) {
        return 0;
    }

    @Override
    public String xgroupSetID(byte[] key, byte[] consumer, byte[] id) {
        return "";
    }

    @Override
    public String xgroupCreate(byte[] key, byte[] consumer, byte[] id, boolean makeStream) {
        return "";
    }

    @Override
    public long xack(byte[] key, byte[] group, byte[]... ids) {
        return 0;
    }

    @Override
    public List<Object> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
        return null;
    }

    @Override
    public List<Object> xrevrange(byte[] key, byte[] end, byte[] start) {
        return null;
    }

    @Override
    public List<Object> xrange(byte[] key, byte[] start, byte[] end, int count) {
        return null;
    }

    @Override
    public List<Object> xrange(byte[] key, byte[] start, byte[] end) {
        return null;
    }

    @Override
    public long xlen(byte[] key) {
        return 0;
    }

    @Override
    public byte[] xadd(byte[] key, XAddParams params, Map<byte[], byte[]> hash) {
        return null;
    }

    @SafeVarargs
    @Override
    public final List<Object> xreadGroup(byte[] groupName, byte[] consumer, XReadGroupParams xReadGroupParams,
        Map.Entry<byte[], byte[]>... streams) {
        return null;
    }

    @SafeVarargs
    @Override
    public final List<Object> xread(XReadParams xReadParams, Map.Entry<byte[], byte[]>... streams) {
        return null;
    }

    @Override
    public List<Long> hpersist(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hpttl(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> httl(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpireTime(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hexpireTime(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpireAt(byte[] key, long unixTimeMillis, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpireAt(byte[] key, long unixTimeMillis, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hexpireAt(byte[] key, long unixTimeSeconds, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hexpireAt(byte[] key, long unixTimeSeconds, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpire(byte[] key, long milliseconds, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hpexpire(byte[] key, long milliseconds, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hexpire(byte[] key, long seconds, ExpiryOption condition, byte[]... fields) {
        return null;
    }

    @Override
    public List<Long> hexpire(byte[] key, long seconds, byte[]... fields) {
        return null;
    }

    @Override
    public long hstrlen(byte[] key, byte[] field) {
        return 0;
    }

    @Override
    public List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
        return null;
    }

    @Override
    public List<Long> bitfield(byte[] key, byte[]... arguments) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public long geosearchStoreStoreDist(byte[] dest, byte[] src, GeoSearchParam params) {
        return 0;
    }

    @Override
    public long geosearchStore(byte[] dest, byte[] src, GeoSearchParam params) {
        return 0;
    }

    @Override
    public long geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return 0;
    }

    @Override
    public long geosearchStore(byte[] dest, byte[] src, byte[] member, double width, double height, GeoUnit unit) {
        return 0;
    }

    @Override
    public long geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double radius, GeoUnit unit) {
        return 0;
    }

    @Override
    public long geosearchStore(byte[] dest, byte[] src, byte[] member, double radius, GeoUnit unit) {
        return 0;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(byte[] key, GeoSearchParam params) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(byte[] key, GeoCoordinate coord, double width, double height,
        GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(byte[] key, byte[] member, double width, double height, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(byte[] key, GeoCoordinate coord, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> geosearch(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param,
        GeoRadiusStoreParam storeParam) {
        return 0;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
        GeoUnit unit, GeoRadiusParam param) {
        return null;
    }

    @Override
    public long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
        GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return 0;
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
        GeoRadiusParam param) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
        GeoUnit unit) {
        return null;
    }

    @Override
    public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public List<byte[]> geohash(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
        return null;
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2) {
        return null;
    }

    @Override
    public long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return 0;
    }

    @Override
    public long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return 0;
    }

    @Override
    public long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return 0;
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
        return null;
    }

    @Override
    public ScanResult<byte[]> hscanNoValues(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor, ScanParams params, byte[] type) {
        return null;
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
        return null;
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor) {
        return null;
    }

    @Override
    public long pfcount(byte[]... keys) {
        return 0;
    }

    @Override
    public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
        return "";
    }

    @Override
    public long pfcount(byte[] key) {
        return 0;
    }

    @Override
    public long pfadd(byte[] key, byte[]... elements) {
        return 0;
    }

    @Override
    public KeyValue<Long, Long> waitAOF(long numLocal, long numReplicas, long timeout) {
        return null;
    }

    @Override
    public long waitReplicas(int replicas, long timeout) {
        return 0;
    }

    @Override
    public String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
        return "";
    }

    @Override
    public String migrate(String host, int port, byte[] key, int timeout) {
        return "";
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
        return "";
    }

    @Override
    public String migrate(String host, int port, byte[] key, int destinationDb, int timeout) {
        return "";
    }

    @Override
    public List<String> time() {
        return null;
    }

    @Override
    public TrackingInfo clientTrackingInfo() {
        return null;
    }

    @Override
    public String clientNoTouchOff() {
        return "";
    }

    @Override
    public String clientNoTouchOn() {
        return "";
    }

    @Override
    public String clientNoEvictOff() {
        return "";
    }

    @Override
    public String clientNoEvictOn() {
        return "";
    }

    @Override
    public String clientUnpause() {
        return "";
    }

    @Override
    public String clientPause(long timeout, ClientPauseMode mode) {
        return "";
    }

    @Override
    public String clientPause(long timeout) {
        return "";
    }

    @Override
    public long clientUnblock(long clientId, UnblockType unblockType) {
        return 0;
    }

    @Override
    public long clientUnblock(long clientId) {
        return 0;
    }

    @Override
    public long clientId() {
        return 0;
    }

    @Override
    public String clientSetname(byte[] name) {
        return "";
    }

    @Override
    public String clientSetInfo(ClientAttributeOption attr, byte[] value) {
        return "";
    }

    @Override
    public byte[] clientInfoBinary() {
        return null;
    }

    @Override
    public byte[] clientListBinary(long... clientIds) {
        return null;
    }

    @Override
    public byte[] clientListBinary(ClientType type) {
        return null;
    }

    @Override
    public byte[] clientListBinary() {
        return null;
    }

    @Override
    public byte[] clientGetnameBinary() {
        return null;
    }

    @Override
    public long clientKill(ClientKillParams params) {
        return 0;
    }

    @Override
    public String clientKill(String ip, int port) {
        return "";
    }

    @Override
    public String clientKill(byte[] ipPort) {
        return "";
    }

    @Override
    public String aclLogReset() {
        return "";
    }

    @Override
    public List<byte[]> aclLogBinary(int limit) {
        return null;
    }

    @Override
    public List<byte[]> aclLogBinary() {
        return null;
    }

    @Override
    public List<byte[]> aclCat(byte[] category) {
        return null;
    }

    @Override
    public List<byte[]> aclCatBinary() {
        return null;
    }

    @Override
    public long aclDelUser(byte[]... names) {
        return 0;
    }

    @Override
    public String aclSetUser(byte[] name, byte[]... rules) {
        return "";
    }

    @Override
    public String aclSetUser(byte[] name) {
        return "";
    }

    @Override
    public AccessControlUser aclGetUser(byte[] name) {
        return null;
    }

    @Override
    public List<byte[]> aclUsersBinary() {
        return null;
    }

    @Override
    public List<byte[]> aclListBinary() {
        return null;
    }

    @Override
    public byte[] aclGenPassBinary(int bits) {
        return null;
    }

    @Override
    public byte[] aclGenPassBinary() {
        return null;
    }

    @Override
    public byte[] aclWhoAmIBinary() {
        return null;
    }

    @Override
    public String failoverAbort() {
        return "";
    }

    @Override
    public String failover(FailoverParams failoverParams) {
        return "";
    }

    @Override
    public String failover() {
        return "";
    }

    @Override
    public Long memoryUsage(byte[] key, int samples) {
        return null;
    }

    @Override
    public Long memoryUsage(byte[] key) {
        return null;
    }

    @Override
    public byte[] memoryDoctorBinary() {
        return null;
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return "";
    }

    @Override
    public long pttl(byte[] key) {
        return 0;
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
        return "";
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue) {
        return "";
    }

    @Override
    public byte[] dump(byte[] key) {
        return null;
    }

    @Override
    public long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        return 0;
    }

    @Override
    public long bitcount(byte[] key, long start, long end, BitCountOption option) {
        return 0;
    }

    @Override
    public long bitcount(byte[] key, long start, long end) {
        return 0;
    }

    @Override
    public long bitcount(byte[] key) {
        return 0;
    }

    @Override
    public Long objectFreq(byte[] key) {
        return null;
    }

    @Override
    public List<byte[]> objectHelpBinary() {
        return null;
    }

    @Override
    public Long objectIdletime(byte[] key) {
        return null;
    }

    @Override
    public byte[] objectEncoding(byte[] key) {
        return null;
    }

    @Override
    public Long objectRefcount(byte[] key) {
        return null;
    }

    @Override
    public List<Object> slowlogGetBinary(long entries) {
        return null;
    }

    @Override
    public List<Object> slowlogGetBinary() {
        return null;
    }

    @Override
    public long slowlogLen() {
        return 0;
    }

    @Override
    public String slowlogReset() {
        return "";
    }

    @Override
    public String scriptKill() {
        return "";
    }

    @Override
    public byte[] scriptLoad(byte[] script) {
        return null;
    }

    @Override
    public List<Boolean> scriptExists(byte[]... sha1) {
        return null;
    }

    @Override
    public Boolean scriptExists(byte[] sha1) {
        return null;
    }

    @Override
    public String scriptFlush(FlushMode flushMode) {
        return "";
    }

    @Override
    public String scriptFlush() {
        return "";
    }

    @Override
    public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
        return null;
    }

    @Override
    public Object evalshaReadonly(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Object evalsha(byte[] sha1) {
        return null;
    }

    @Override
    public Object eval(byte[] script) {
        return null;
    }

    @Override
    public Object eval(byte[] script, int keyCount, byte[]... params) {
        return null;
    }

    @Override
    public Object evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return null;
    }

    @Override
    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
    }

    @Override
    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
    }

    @Override
    public long publish(byte[] channel, byte[] message) {
        return 0;
    }

    @Override
    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        return null;
    }

    @Override
    public long setrange(byte[] key, long offset, byte[] value) {
        return 0;
    }

    @Override
    public long bitpos(byte[] key, boolean value, BitPosParams params) {
        return 0;
    }

    @Override
    public long bitpos(byte[] key, boolean value) {
        return 0;
    }

    @Override
    public boolean getbit(byte[] key, long offset) {
        return false;
    }

    @Override
    public boolean setbit(byte[] key, long offset, boolean value) {
        return false;
    }

    @Override
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        return null;
    }

    @Override
    public long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
        return 0;
    }

    @Override
    public byte[] echo(byte[] string) {
        return null;
    }

    @Override
    public long rpushx(byte[] key, byte[]... strings) {
        return 0;
    }

    @Override
    public long persist(byte[] key) {
        return 0;
    }

    @Override
    public long lpushx(byte[] key, byte[]... strings) {
        return 0;
    }

    @Override
    public LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params) {
        return null;
    }

    @Override
    public long strlen(byte[] key) {
        return 0;
    }

    @Override
    public String configSetBinary(Map<byte[], byte[]> parameterValues) {
        return "";
    }

    @Override
    public String configSet(byte[]... parameterValues) {
        return "";
    }

    @Override
    public String configSet(byte[] parameter, byte[] value) {
        return "";
    }

    @Override
    public String configRewrite() {
        return "";
    }

    @Override
    public String configResetStat() {
        return "";
    }

    @Override
    public Map<byte[], byte[]> configGet(byte[]... patterns) {
        return null;
    }

    @Override
    public Map<byte[], byte[]> configGet(byte[] pattern) {
        return null;
    }

    @Override
    public List<Object> roleBinary() {
        return null;
    }

    @Override
    public String replicaofNoOne() {
        return "";
    }

    @Override
    public String replicaof(String host, int port) {
        return "";
    }

    @Override
    public String slaveofNoOne() {
        return "";
    }

    @Override
    public String slaveof(String host, int port) {
        return "";
    }

    @Override
    public void monitor(JedisMonitor jedisMonitor) {
    }

    @Override
    public String info(String section) {
        return "";
    }

    @Override
    public String info() {
        return "";
    }

    @Override
    public String shutdownAbort() {
        return "";
    }

    @Override
    public void shutdown(ShutdownParams shutdownParams) throws JedisException {
    }

    @Override
    public void shutdown() throws JedisException {
    }

    @Override
    public long lastsave() {
        return 0;
    }

    @Override
    public String bgrewriteaof() {
        return "";
    }

    @Override
    public String bgsaveSchedule() {
        return "";
    }

    @Override
    public String bgsave() {
        return "";
    }

    @Override
    public String save() {
        return "";
    }

    @Override
    public KeyValue<byte[], List<Tuple>> bzmpop(double timeout, SortedSetOption option, int count, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], List<Tuple>> bzmpop(double timeout, SortedSetOption option, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption option, int count, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption option, byte[]... keys) {
        return null;
    }

    @Override
    public long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        return 0;
    }

    @Override
    public List<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return null;
    }

    @Override
    public List<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        return null;
    }

    @Override
    public List<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return null;
    }

    @Override
    public List<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public long zlexcount(byte[] key, byte[] min, byte[] max) {
        return 0;
    }

    @Override
    public long zintercard(long limit, byte[]... keys) {
        return 0;
    }

    @Override
    public long zintercard(byte[]... keys) {
        return 0;
    }

    @Override
    public long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return 0;
    }

    @Override
    public long zinterstore(byte[] dstkey, byte[]... sets) {
        return 0;
    }

    @Override
    public List<Tuple> zinterWithScores(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public List<byte[]> zinter(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return 0;
    }

    @Override
    public long zunionstore(byte[] dstkey, byte[]... sets) {
        return 0;
    }

    @Override
    public List<Tuple> zunionWithScores(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public List<byte[]> zunion(ZParams params, byte[]... keys) {
        return null;
    }

    @Override
    public long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
        return 0;
    }

    @Override
    public long zremrangeByScore(byte[] key, double min, double max) {
        return 0;
    }

    @Override
    public long zremrangeByRank(byte[] key, long start, long stop) {
        return 0;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return null;
    }

    @Override
    public List<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return null;
    }

    @Override
    public List<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return null;
    }

    @Override
    public List<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return null;
    }

    @Override
    public List<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public List<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return null;
    }

    @Override
    public List<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public List<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return null;
    }

    @Override
    public List<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public long zdiffstore(byte[] dstkey, byte[]... keys) {
        return 0;
    }

    @Override
    public long zdiffStore(byte[] dstkey, byte[]... keys) {
        return 0;
    }

    @Override
    public List<Tuple> zdiffWithScores(byte[]... keys) {
        return null;
    }

    @Override
    public List<byte[]> zdiff(byte[]... keys) {
        return null;
    }

    @Override
    public long zcount(byte[] key, byte[] min, byte[] max) {
        return 0;
    }

    @Override
    public long zcount(byte[] key, double min, double max) {
        return 0;
    }

    @Override
    public String auth(String user, String password) {
        return "";
    }

    @Override
    public String auth(String password) {
        return "";
    }

    @Override
    public KeyValue<byte[], Tuple> bzpopmin(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], Tuple> bzpopmax(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], List<byte[]>> blmpop(double timeout, ListDirection direction, int count, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], List<byte[]>> blmpop(double timeout, ListDirection direction, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, int count, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], byte[]> brpop(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public List<byte[]> brpop(int timeout, byte[]... keys) {
        return null;
    }

    @Override
    public KeyValue<byte[], byte[]> blpop(double timeout, byte[]... keys) {
        return null;
    }

    @Override
    public List<byte[]> blpop(int timeout, byte[]... keys) {
        return null;
    }

    @Override
    public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
        return null;
    }

    @Override
    public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
        return null;
    }

    @Override
    public List<byte[]> sortReadonly(byte[] key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public long sort(byte[] key, byte[] dstkey) {
        return 0;
    }

    @Override
    public long sort(byte[] key, SortingParams sortingParams, byte[] dstkey) {
        return 0;
    }

    @Override
    public List<byte[]> sort(byte[] key, SortingParams sortingParams) {
        return null;
    }

    @Override
    public List<byte[]> sort(byte[] key) {
        return null;
    }

    @Override
    public String unwatch() {
        return "";
    }

    @Override
    public String watch(byte[]... keys) {
        return "";
    }

    @Override
    public List<Tuple> zpopmin(byte[] key, int count) {
        return null;
    }

    @Override
    public Tuple zpopmin(byte[] key) {
        return null;
    }

    @Override
    public List<Tuple> zpopmax(byte[] key, int count) {
        return null;
    }

    @Override
    public Tuple zpopmax(byte[] key) {
        return null;
    }

    @Override
    public List<Double> zmscore(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public Double zscore(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public long zcard(byte[] key) {
        return 0;
    }

    @Override
    public List<Tuple> zrandmemberWithScores(byte[] key, long count) {
        return null;
    }

    @Override
    public List<byte[]> zrandmember(byte[] key, long count) {
        return null;
    }

    @Override
    public byte[] zrandmember(byte[] key) {
        return null;
    }

    @Override
    public long zrangestore(byte[] dest, byte[] src, ZRangeParams zRangeParams) {
        return 0;
    }

    @Override
    public List<Tuple> zrangeWithScores(byte[] key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public List<byte[]> zrange(byte[] key, ZRangeParams zRangeParams) {
        return null;
    }

    @Override
    public List<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public List<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public List<byte[]> zrevrange(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public KeyValue<Long, Double> zrevrankWithScore(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public KeyValue<Long, Double> zrankWithScore(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Long zrevrank(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Long zrank(byte[] key, byte[] member) {
        return null;
    }

    @Override
    public Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
        return null;
    }

    @Override
    public double zincrby(byte[] key, double increment, byte[] member) {
        return 0;
    }

    @Override
    public long zrem(byte[] key, byte[]... members) {
        return 0;
    }

    @Override
    public List<byte[]> zrange(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
        return null;
    }

    @Override
    public long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
        return 0;
    }

    @Override
    public long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return 0;
    }

    @Override
    public long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
        return 0;
    }

    @Override
    public long zadd(byte[] key, double score, byte[] member) {
        return 0;
    }

    @Override
    public List<byte[]> srandmember(byte[] key, int count) {
        return null;
    }

    @Override
    public byte[] srandmember(byte[] key) {
        return null;
    }

    @Override
    public long sdiffstore(byte[] dstkey, byte[]... keys) {
        return 0;
    }

    @Override
    public Set<byte[]> sdiff(byte[]... keys) {
        return null;
    }

    @Override
    public long sunionstore(byte[] dstkey, byte[]... keys) {
        return 0;
    }

    @Override
    public Set<byte[]> sunion(byte[]... keys) {
        return null;
    }

    @Override
    public long sintercard(int limit, byte[]... keys) {
        return 0;
    }

    @Override
    public long sintercard(byte[]... keys) {
        return 0;
    }

    @Override
    public long sinterstore(byte[] dstkey, byte[]... keys) {
        return 0;
    }

    @Override
    public Set<byte[]> sinter(byte[]... keys) {
        return null;
    }

    @Override
    public List<Boolean> smismember(byte[] key, byte[]... members) {
        return null;
    }

    @Override
    public boolean sismember(byte[] key, byte[] member) {
        return false;
    }

    @Override
    public long scard(byte[] key) {
        return 0;
    }

    @Override
    public long smove(byte[] srckey, byte[] dstkey, byte[] member) {
        return 0;
    }

    @Override
    public Set<byte[]> spop(byte[] key, long count) {
        return null;
    }

    @Override
    public byte[] spop(byte[] key) {
        return null;
    }

    @Override
    public long srem(byte[] key, byte[]... members) {
        return 0;
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        return null;
    }

    @Override
    public long sadd(byte[] key, byte[]... members) {
        return 0;
    }

    @Override
    public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
        return null;
    }

    @Override
    public List<byte[]> rpop(byte[] key, int count) {
        return null;
    }

    @Override
    public byte[] rpop(byte[] key) {
        return null;
    }

    @Override
    public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
        return null;
    }

    @Override
    public Long lpos(byte[] key, byte[] element, LPosParams params) {
        return null;
    }

    @Override
    public Long lpos(byte[] key, byte[] element) {
        return null;
    }

    @Override
    public List<byte[]> lpop(byte[] key, int count) {
        return null;
    }

    @Override
    public byte[] lpop(byte[] key) {
        return null;
    }

    @Override
    public long lrem(byte[] key, long count, byte[] value) {
        return 0;
    }

    @Override
    public String lset(byte[] key, long index, byte[] value) {
        return "";
    }

    @Override
    public byte[] lindex(byte[] key, long index) {
        return null;
    }

    @Override
    public String ltrim(byte[] key, long start, long stop) {
        return "";
    }

    @Override
    public List<byte[]> lrange(byte[] key, long start, long stop) {
        return null;
    }

    @Override
    public long llen(byte[] key) {
        return 0;
    }

    @Override
    public long lpush(byte[] key, byte[]... strings) {
        return 0;
    }

    @Override
    public long rpush(byte[] key, byte[]... strings) {
        return 0;
    }

    @Override
    public List<Map.Entry<byte[], byte[]>> hrandfieldWithValues(byte[] key, long count) {
        return null;
    }

    @Override
    public List<byte[]> hrandfield(byte[] key, long count) {
        return null;
    }

    @Override
    public byte[] hrandfield(byte[] key) {
        return null;
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return null;
    }

    @Override
    public List<byte[]> hvals(byte[] key) {
        return null;
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return null;
    }

    @Override
    public long hlen(byte[] key) {
        return 0;
    }

    @Override
    public long hdel(byte[] key, byte[]... fields) {
        return 0;
    }

    @Override
    public boolean hexists(byte[] key, byte[] field) {
        return false;
    }

    @Override
    public double hincrByFloat(byte[] key, byte[] field, double value) {
        return 0;
    }

    @Override
    public long hincrBy(byte[] key, byte[] field, long value) {
        return 0;
    }

    @Override
    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return null;
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return "";
    }

    @Override
    public long hsetnx(byte[] key, byte[] field, byte[] value) {
        return 0;
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return null;
    }

    @Override
    public long hset(byte[] key, Map<byte[], byte[]> hash) {
        return 0;
    }

    @Override
    public long hset(byte[] key, byte[] field, byte[] value) {
        return 0;
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return null;
    }

    @Override
    public long append(byte[] key, byte[] value) {
        return 0;
    }

    @Override
    public long incr(byte[] key) {
        return 0;
    }

    @Override
    public double incrByFloat(byte[] key, double increment) {
        return 0;
    }

    @Override
    public long incrBy(byte[] key, long increment) {
        return 0;
    }

    @Override
    public long decr(byte[] key) {
        return 0;
    }

    @Override
    public long decrBy(byte[] key, long decrement) {
        return 0;
    }

    @Override
    public long msetnx(byte[]... keysvalues) {
        return 0;
    }

    @Override
    public String mset(byte[]... keysvalues) {
        return "";
    }

    @Override
    public String setex(byte[] key, long seconds, byte[] value) {
        return "";
    }

    @Override
    public long setnx(byte[] key, byte[] value) {
        return 0;
    }

    @Override
    public List<byte[]> mget(byte[]... keys) {
        return null;
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public long move(byte[] key, int dbIndex) {
        return 0;
    }

    @Override
    public long touch(byte[] key) {
        return 0;
    }

    @Override
    public long touch(byte[]... keys) {
        return 0;
    }

    @Override
    public long ttl(byte[] key) {
        return 0;
    }

    @Override
    public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long pexpireAt(byte[] key, long millisecondsTimestamp) {
        return 0;
    }

    @Override
    public long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long expireAt(byte[] key, long unixTime) {
        return 0;
    }

    @Override
    public long pexpireTime(byte[] key) {
        return 0;
    }

    @Override
    public long expireTime(byte[] key) {
        return 0;
    }

    @Override
    public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long pexpire(byte[] key, long milliseconds) {
        return 0;
    }

    @Override
    public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
        return 0;
    }

    @Override
    public long expire(byte[] key, long seconds) {
        return 0;
    }

    @Override
    public long dbSize() {
        return 0;
    }

    @Override
    public long renamenx(byte[] oldkey, byte[] newkey) {
        return 0;
    }

    @Override
    public String rename(byte[] oldkey, byte[] newkey) {
        return "";
    }

    @Override
    public byte[] randomBinaryKey() {
        return null;
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
        return null;
    }

    @Override
    public String type(byte[] key) {
        return "";
    }

    @Override
    public long unlink(byte[] key) {
        return 0;
    }

    @Override
    public long unlink(byte[]... keys) {
        return 0;
    }

    @Override
    public long del(byte[] key) {
        return 0;
    }

    @Override
    public long del(byte[]... keys) {
        return 0;
    }

    @Override
    public boolean exists(byte[] key) {
        return false;
    }

    @Override
    public long exists(byte[]... keys) {
        return 0;
    }

    @Override
    public byte[] getEx(byte[] key, GetExParams params) {
        return null;
    }

    @Override
    public byte[] getDel(byte[] key) {
        return null;
    }

    @Override
    public byte[] setGet(byte[] key, byte[] value, SetParams params) {
        return null;
    }

    @Override
    public byte[] setGet(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public byte[] get(byte[] key) {
        return null;
    }

    @Override
    public String set(byte[] key, byte[] value, SetParams params) {
        return "";
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return "";
    }

    @Override
    public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        return false;
    }

    @Override
    public boolean copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
        return false;
    }

    @Override
    public String flushAll(FlushMode flushMode) {
        return "";
    }

    @Override
    public String flushAll() {
        return "";
    }

    @Override
    public String flushDB(FlushMode flushMode) {
        return "";
    }

    @Override
    public String flushDB() {
        return "";
    }

    @Override
    public String swapDB(int index1, int index2) {
        return "";
    }

    @Override
    public String select(int index) {
        return "";
    }

    @Override
    public byte[] ping(byte[] message) {
        return null;
    }

    @Override
    public String ping() {
        return "";
    }

    @Override
    public int getDB() {
        return 0;
    }

    @Override
    protected void checkIsInMultiOrPipeline() {
    }

    @Override
    public Pipeline pipelined() {
        return null;
    }

    @Override
    public Transaction multi() {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    protected void setDataSource(Pool<Jedis> jedisPool) {
    }

    @Override
    public void resetState() {
    }

    @Override
    public boolean isBroken() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void connect() {
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public Connection getClient() {
        return null;
    }
}
