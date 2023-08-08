// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.spring.data.cosmos {

    requires transitive com.azure.cosmos;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.module.paramnames;
    requires java.desktop;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.expression;
    requires spring.tx;

    exports com.azure.spring.data.cosmos;
    exports com.azure.spring.data.cosmos.common;
    exports com.azure.spring.data.cosmos.config;
    exports com.azure.spring.data.cosmos.core.convert;
    exports com.azure.spring.data.cosmos.core;
    exports com.azure.spring.data.cosmos.core.mapping;
    exports com.azure.spring.data.cosmos.exception;
    exports com.azure.spring.data.cosmos.repository;
    exports com.azure.spring.data.cosmos.repository.config;
    exports com.azure.spring.data.cosmos.repository.support;

    opens com.azure.spring.data.cosmos.config to spring.core;
    opens com.azure.spring.data.cosmos.core.mapping to spring.core;
    opens com.azure.spring.data.cosmos.repository.support to spring.beans;
}
