module com.azure.spring.cloud.feature.management {
    exports com.azure.spring.cloud.feature.management.targeting;

    exports com.azure.spring.cloud.feature.management.models;

    exports com.azure.spring.cloud.feature.management;

    exports com.azure.spring.cloud.feature.management.filters;

    exports com.azure.spring.cloud.feature.management.implementation.targeting to com.fasterxml.jackson.databind;

    exports com.azure.spring.cloud.feature.management.implementation to spring.beans, spring.context;

    requires com.fasterxml.jackson.annotation;

    requires com.fasterxml.jackson.core;

    requires com.fasterxml.jackson.databind;

    requires org.reactivestreams;

    requires org.slf4j;

    requires reactor.core;

    requires spring.beans;

    requires spring.boot;

    requires spring.context;

    requires spring.core;

    opens com.azure.spring.cloud.feature.management to spring.core;

    opens com.azure.spring.cloud.feature.management.implementation to spring.core;
}