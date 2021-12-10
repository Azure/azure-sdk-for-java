module spring.integration.azure.core {
    exports com.azure.spring.integration.instrumentation;
    exports com.azure.spring.integration.handler;
    requires spring.messaging.azure;
    requires spring.integration.core;
    requires spring.messaging;
    requires spring.core;
    requires org.slf4j;
    requires spring.expression;
    requires reactor.core;
}