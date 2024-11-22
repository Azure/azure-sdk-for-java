// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.mocking;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;

import java.util.EnumSet;
import java.util.Map;

/**
 * Mock implementation of the Session interface.
 */
public class MockSession implements Session {
    @Override
    public Sender sender(String name) {
        return null;
    }

    @Override
    public Receiver receiver(String name) {
        return null;
    }

    @Override
    public Session next(EnumSet<EndpointState> local, EnumSet<EndpointState> remote) {
        return null;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public int getIncomingCapacity() {
        return 0;
    }

    @Override
    public void setIncomingCapacity(int bytes) {

    }

    @Override
    public int getIncomingBytes() {
        return 0;
    }

    @Override
    public int getOutgoingBytes() {
        return 0;
    }

    @Override
    public long getOutgoingWindow() {
        return 0;
    }

    @Override
    public void setOutgoingWindow(long outgoingWindowSize) {

    }

    @Override
    public void setProperties(Map<Symbol, Object> properties) {

    }

    @Override
    public Map<Symbol, Object> getProperties() {
        return null;
    }

    @Override
    public Map<Symbol, Object> getRemoteProperties() {
        return null;
    }

    @Override
    public void setOfferedCapabilities(Symbol[] offeredCapabilities) {

    }

    @Override
    public Symbol[] getOfferedCapabilities() {
        return new Symbol[0];
    }

    @Override
    public Symbol[] getRemoteOfferedCapabilities() {
        return new Symbol[0];
    }

    @Override
    public void setDesiredCapabilities(Symbol[] desiredCapabilities) {

    }

    @Override
    public Symbol[] getDesiredCapabilities() {
        return new Symbol[0];
    }

    @Override
    public Symbol[] getRemoteDesiredCapabilities() {
        return new Symbol[0];
    }

    @Override
    public EndpointState getLocalState() {
        return null;
    }

    @Override
    public EndpointState getRemoteState() {
        return null;
    }

    @Override
    public ErrorCondition getCondition() {
        return null;
    }

    @Override
    public void setCondition(ErrorCondition condition) {

    }

    @Override
    public ErrorCondition getRemoteCondition() {
        return null;
    }

    @Override
    public void free() {

    }

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }

    @Override
    public void setContext(Object o) {

    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Record attachments() {
        return null;
    }
}
