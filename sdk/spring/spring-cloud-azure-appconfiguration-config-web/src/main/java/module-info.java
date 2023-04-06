// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.spring.cloud.appconfiguration.config.web {
    exports com.azure.spring.cloud.appconfiguration.config.web.implementation.pushbusrefresh;

    exports com.azure.spring.cloud.appconfiguration.config.web.implementation.pullrefresh;

    exports com.azure.spring.cloud.appconfiguration.config.web.implementation;

    exports com.azure.spring.cloud.appconfiguration.config.web.implementation.pushrefresh;

    requires com.azure.data.appconfiguration;

    requires transitive com.azure.spring.cloud.appconfiguration.config;

    requires com.azure.spring.cloud.autoconfigure;

    requires com.fasterxml.jackson.core;

    requires com.fasterxml.jackson.databind;

    requires transitive org.apache.tomcat.embed.core;

    requires org.reactivestreams;

    requires org.slf4j;

    requires reactor.core;

    requires spring.beans;

    requires spring.boot;

    requires spring.boot.actuator;

    requires spring.boot.actuator.autoconfigure;

    requires spring.boot.autoconfigure;

    requires spring.cloud.bus;

    requires spring.cloud.context;

    requires spring.context;

    requires spring.core;

    requires transitive spring.web;

    opens com.azure.spring.cloud.appconfiguration.config.web.implementation to spring.core;
}
