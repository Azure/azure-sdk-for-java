module spring.messaging.azure {
    requires spring.messaging;
    requires spring.core;
    requires spring.beans;
    requires reactor.core;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.dataformat.xml;
    requires org.slf4j;
    requires spring.context;
    requires spring.aop;
    exports com.azure.spring.messaging.checkpoint;
    exports com.azure.spring.messaging;
    exports com.azure.spring.messaging.core;
    exports com.azure.spring.messaging.converter;
}