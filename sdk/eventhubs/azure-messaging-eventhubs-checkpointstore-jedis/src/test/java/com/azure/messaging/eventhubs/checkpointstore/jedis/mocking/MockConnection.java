// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis.mocking;

import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.args.Rawable;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.util.RedisInputStream;

import java.util.List;
import java.util.Map;

/**
 * Mock class for {@link Connection}.
 */
public class MockConnection extends Connection {
    public MockConnection() {
        super();
    }

    @Override
    public boolean ping() {
        return false;
    }

    @Override
    public String select(int index) {
        return "";
    }

    @Override
    protected byte[] encodeToBytes(char[] chars) {
        return null;
    }

    @Override
    protected Map<String, Object> hello(byte[]... args) {
        return null;
    }

    @Override
    protected void initializeFromClientConfig(JedisClientConfig config) {
    }

    @Override
    public List<Object> getMany(int count) {
        return null;
    }

    @Override
    protected void readPushesWithCheckingBroken() {
    }

    @Override
    protected Object readProtocolWithCheckingBroken() {
        return null;
    }

    @Override
    protected void protocolReadPushes(RedisInputStream is) {
    }

    @Override
    protected Object protocolRead(RedisInputStream is) {
        return null;
    }

    @Override
    protected void flush() {
    }

    @Override
    public Object getOne() {
        return null;
    }

    @Override
    public List<Long> getIntegerMultiBulkReply() {
        return null;
    }

    @Override
    public List<Object> getObjectMultiBulkReply() {
        return null;
    }

    @Override
    public Object getUnflushedObject() {
        return null;
    }

    @Override
    public List<Object> getUnflushedObjectMultiBulkReply() {
        return null;
    }

    @Override
    public List<byte[]> getBinaryMultiBulkReply() {
        return null;
    }

    @Override
    public List<String> getMultiBulkReply() {
        return null;
    }

    @Override
    public Long getIntegerReply() {
        return null;
    }

    @Override
    public byte[] getBinaryBulkReply() {
        return null;
    }

    @Override
    public String getBulkReply() {
        return "";
    }

    @Override
    public String getStatusCodeReply() {
        return "";
    }

    @Override
    public void setBroken() {
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
    public void close() {
    }

    @Override
    public void connect() throws JedisConnectionException {
    }

    @Override
    public void sendCommand(CommandArguments args) {
    }

    @Override
    public void sendCommand(ProtocolCommand cmd, byte[]... args) {
    }

    @Override
    public void sendCommand(ProtocolCommand cmd, String... args) {
    }

    @Override
    public void sendCommand(ProtocolCommand cmd, Rawable keyword) {
    }

    @Override
    public void sendCommand(ProtocolCommand cmd) {
    }

    @Override
    public <T> T executeCommand(CommandObject<T> commandObject) {
        return null;
    }

    @Override
    public Object executeCommand(CommandArguments args) {
        return null;
    }

    @Override
    public Object executeCommand(ProtocolCommand cmd) {
        return null;
    }

    @Override
    public void rollbackTimeout() {
    }

    @Override
    public void setTimeoutInfinite() {
    }

    @Override
    public void setSoTimeout(int soTimeout) {
    }

    @Override
    public int getSoTimeout() {
        return -1;
    }

    @Override
    public String toIdentityString() {
        return "";
    }

    @Override
    public String toString() {
        return "";
    }
}
