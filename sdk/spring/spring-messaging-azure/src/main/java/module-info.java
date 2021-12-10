module spring.messaging.azure {
    requires spring.messaging;
    requires spring.core;
    requires spring.beans;
    requires reactor.core;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires spring.context;
    requires spring.aop;
    exports com.azure.spring.messaging.checkpoint;
    exports com.azure.spring.messaging;
}