// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.mocking;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Collector;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Reactor;

import java.util.EnumSet;
import java.util.Map;

/**
 * Mock implementation of the Connection interface.
 */
public class MockReactorConnection implements Connection {
    @Override
    public Session session() {
        return null;
    }

    @Override
    public Session sessionHead(EnumSet<EndpointState> local, EnumSet<EndpointState> remote) {
        return null;
    }

    @Override
    public Link linkHead(EnumSet<EndpointState> local, EnumSet<EndpointState> remote) {
        return null;
    }

    @Override
    public Delivery getWorkHead() {
        return null;
    }

    @Override
    public void setContainer(String container) {

    }

    @Override
    public String getContainer() {
        return "";
    }

    @Override
    public void setHostname(String hostname) {

    }

    @Override
    public String getHostname() {
        return "";
    }

    @Override
    public String getRemoteContainer() {
        return "";
    }

    @Override
    public String getRemoteHostname() {
        return "";
    }

    @Override
    public void setOfferedCapabilities(Symbol[] capabilities) {

    }

    @Override
    public void setDesiredCapabilities(Symbol[] capabilities) {

    }

    @Override
    public Symbol[] getRemoteOfferedCapabilities() {
        return new Symbol[0];
    }

    @Override
    public Symbol[] getRemoteDesiredCapabilities() {
        return new Symbol[0];
    }

    @Override
    public Map<Symbol, Object> getRemoteProperties() {
        return null;
    }

    @Override
    public void setProperties(Map<Symbol, Object> properties) {

    }

    @Override
    public Object getContext() {
        return null;
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
    public void setContext(Object context) {

    }

    @Override
    public void collect(Collector collector) {

    }

    @Override
    public Transport getTransport() {
        return null;
    }

    @Override
    public Reactor getReactor() {
        return null;
    }

    @Override
    public Record attachments() {
        return null;
    }
}
