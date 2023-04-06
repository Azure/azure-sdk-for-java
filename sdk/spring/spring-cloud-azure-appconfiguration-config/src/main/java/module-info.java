// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.spring.cloud.appconfiguration.config {
    exports com.azure.spring.cloud.appconfiguration.config;

    exports com.azure.spring.cloud.appconfiguration.config.implementation.config
        to spring.beans, spring.context, com.azure.spring.cloud.appconfiguration.config.web;

    exports com.azure.spring.cloud.appconfiguration.config.implementation.properties
        to spring.beans, spring.boot, com.azure.spring.cloud.appconfiguration.config.web;

    requires com.azure.core;

    requires transitive com.azure.data.appconfiguration;

    requires com.azure.identity;

    requires transitive com.azure.security.keyvault.secrets;

    requires com.azure.spring.cloud.autoconfigure;

    requires com.azure.spring.cloud.core;

    requires com.azure.spring.cloud.service;

    requires com.fasterxml.jackson.annotation;

    requires com.fasterxml.jackson.core;

    requires com.fasterxml.jackson.databind;

    requires jakarta.annotation;

    requires jakarta.validation;

    requires jcip.annotations;

    requires org.reactivestreams;

    requires org.slf4j;

    requires reactor.core;

    requires spring.beans;

    requires spring.boot;

    requires spring.boot.autoconfigure;

    requires spring.cloud.context;

    requires spring.context;

    requires spring.core;

    requires spring.jcl;

    opens com.azure.spring.cloud.appconfiguration.config to spring.core;

    opens com.azure.spring.cloud.appconfiguration.config.implementation.config to spring.core;

    opens com.azure.spring.cloud.appconfiguration.config.implementation.properties to spring.core;
}
