// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.mocking;

import org.apache.qpid.proton.engine.Collector;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;

import java.nio.channels.SelectableChannel;

/**
 * Mock implementation of the Selectable interface.
 */
public class MockSelectable implements Selectable {
    @Override
    public boolean isReading() {
        return false;
    }

    @Override
    public boolean isWriting() {
        return false;
    }

    @Override
    public long getDeadline() {
        return 0;
    }

    @Override
    public void setReading(boolean reading) {

    }

    @Override
    public void setWriting(boolean writing) {

    }

    @Override
    public void setDeadline(long deadline) {

    }

    @Override
    public void onReadable(Callback runnable) {

    }

    @Override
    public void onWritable(Callback runnable) {

    }

    @Override
    public void onExpired(Callback runnable) {

    }

    @Override
    public void onError(Callback runnable) {

    }

    @Override
    public void onRelease(Callback runnable) {

    }

    @Override
    public void onFree(Callback runnable) {

    }

    @Override
    public void readable() {

    }

    @Override
    public void writeable() {

    }

    @Override
    public void expired() {

    }

    @Override
    public void error() {

    }

    @Override
    public void release() {

    }

    @Override
    public void free() {

    }

    @Override
    public void setChannel(SelectableChannel channel) {

    }

    @Override
    public SelectableChannel getChannel() {
        return null;
    }

    @Override
    public boolean isRegistered() {
        return false;
    }

    @Override
    public void setRegistered(boolean registered) {

    }

    @Override
    public void setCollector(Collector collector) {

    }

    @Override
    public Reactor getReactor() {
        return null;
    }

    @Override
    public void terminate() {

    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public Record attachments() {
        return null;
    }
}
