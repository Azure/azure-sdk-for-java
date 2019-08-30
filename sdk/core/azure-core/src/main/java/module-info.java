module com.azure.core {
    requires java.xml;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires reactor.core;
    requires org.reactivestreams;

    requires slf4j.api;

    exports com.azure.core;
    exports com.azure.core.credentials;
    exports com.azure.core.exception;
    exports com.azure.core.http;
    exports com.azure.core.http.policy;
    exports com.azure.core.http.rest;
    exports com.azure.core.util;
    exports com.azure.core.util.configuration;
    exports com.azure.core.util.logging;
    exports com.azure.core.util.polling;

    opens com.azure.core.implementation to com.fasterxml.jackson.databind;
}
