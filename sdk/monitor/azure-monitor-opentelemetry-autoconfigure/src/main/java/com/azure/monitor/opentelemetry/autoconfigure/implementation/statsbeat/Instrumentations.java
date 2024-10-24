// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// this class is not currently implemented as an enum (similar to Feature)
// because instrumentations may be more dynamic than features
public class Instrumentations {

    public static final String AZURE_OPENTELEMETRY = "Azure-OpenTelemetry";
    private static final Logger logger = LoggerFactory.getLogger(Instrumentations.class);
    private static final Map<String, Integer> INSTRUMENTATION_MAP;

    static {
        INSTRUMENTATION_MAP = new HashMap<>();
        INSTRUMENTATION_MAP.put("io.opentelemetry.apache-httpasyncclient-4.1", 0);
        INSTRUMENTATION_MAP.put("io.opentelemetry.apache-httpclient-2.0", 1);
        INSTRUMENTATION_MAP.put("io.opentelemetry.apache-httpclient-4.0", 2);
        INSTRUMENTATION_MAP.put("io.opentelemetry.apache-httpclient-5.0", 3);
        // TODO (trask) start capturing this
        INSTRUMENTATION_MAP.put("io.opentelemetry.applicationinsights-web-2.3", 4);
        INSTRUMENTATION_MAP.put("io.opentelemetry.tomcat-7.0", 5);
        INSTRUMENTATION_MAP.put(AZURE_OPENTELEMETRY, 6); // bridged by azure-core-1.14 module
        INSTRUMENTATION_MAP.put("io.opentelemetry.cassandra-3.0", 7);
        INSTRUMENTATION_MAP.put("io.opentelemetry.cassandra-4.0", 8);
        INSTRUMENTATION_MAP.put("io.opentelemetry.java-http-client", 9);
        INSTRUMENTATION_MAP.put("io.opentelemetry.rabbitmq-2.7", 10);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-integration-4.1", 11);
        INSTRUMENTATION_MAP.put("io.opentelemetry.grpc-1.6", 12);
        INSTRUMENTATION_MAP.put("io.opentelemetry.http-url-connection", 13);
        INSTRUMENTATION_MAP.put("io.opentelemetry.servlet-5.0", 14);
        // index 15 retired in 3.3.0 GA (was java-util-logging)
        INSTRUMENTATION_MAP.put("io.opentelemetry.jaxrs-1.0", 16); // no usage yet
        // index 17 retired in 3.3.0 GA (was jaxrs-2.0-common)
        INSTRUMENTATION_MAP.put("io.opentelemetry.async-http-client-1.9", 18);
        INSTRUMENTATION_MAP.put("io.opentelemetry.async-http-client-2.0", 19);
        INSTRUMENTATION_MAP.put("io.opentelemetry.google-http-client-1.19", 20);
        INSTRUMENTATION_MAP.put("io.opentelemetry.jdbc", 21);
        INSTRUMENTATION_MAP.put("io.opentelemetry.jedis-1.4", 22);
        INSTRUMENTATION_MAP.put("io.opentelemetry.jedis-3.0", 23);
        INSTRUMENTATION_MAP.put("io.opentelemetry.jetty-8.0", 24);
        INSTRUMENTATION_MAP.put("io.opentelemetry.jms-1.1", 25);
        INSTRUMENTATION_MAP.put("io.opentelemetry.kafka-clients-0.11", 26);
        INSTRUMENTATION_MAP.put("io.opentelemetry.kafka-streams-0.11", 27);
        INSTRUMENTATION_MAP.put("io.opentelemetry.jetty-httpclient-9.2", 28);
        INSTRUMENTATION_MAP.put("io.opentelemetry.lettuce-4.0", 29);
        INSTRUMENTATION_MAP.put("io.opentelemetry.lettuce-5.0", 30);
        INSTRUMENTATION_MAP.put("io.opentelemetry.lettuce-5.1", 31);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-rabbit-1.0", 32);
        // index 33 retired in 3.3.0 GA (was jaxrs-2.0-client)
        // index 34 retired in 3.3.0 GA (was log4j-1.2)
        // index 35 retired in 3.3.0 GA (was log4j-2.0)
        // index 36 retired in 3.3.0 GA (was jaxrs-client-2.0-resteasy-3.0)
        // index 37 retired in 3.3.0 GA (was logback-1.0)
        // TODO (trask) start capturing this
        INSTRUMENTATION_MAP.put("io.opentelemetry.micrometer-1.0", 38);
        INSTRUMENTATION_MAP.put("io.opentelemetry.mongo-3.1", 39); // mongo 4.0 is covered in 3.1
        INSTRUMENTATION_MAP.put("io.opentelemetry.grizzly-2.0", 40);
        INSTRUMENTATION_MAP.put("io.opentelemetry.quartz-2.0", 41);
        INSTRUMENTATION_MAP.put("io.opentelemetry.apache-camel-2.20", 42); // no usage yet
        INSTRUMENTATION_MAP.put("io.opentelemetry.netty-4.0", 43);
        INSTRUMENTATION_MAP.put("io.opentelemetry.netty-4.1", 44);
        INSTRUMENTATION_MAP.put("io.opentelemetry.okhttp-3.0", 45);
        INSTRUMENTATION_MAP.put("io.opentelemetry.opentelemetry-extension-annotations-1.0", 46);
        INSTRUMENTATION_MAP.put("io.opentelemetry.akka-http-10.0", 47);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-webmvc-3.1", 48);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-webflux-5.0", 49);
        INSTRUMENTATION_MAP.put("io.opentelemetry.reactor-netty-1.0", 50);
        INSTRUMENTATION_MAP.put("io.opentelemetry.servlet-2.2", 51);
        INSTRUMENTATION_MAP.put("io.opentelemetry.servlet-3.0", 52);
        // index 53 is open (was servlet-common but that instrumentation doesn't emit telemetry)
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-scheduling-3.1", 54);
        INSTRUMENTATION_MAP.put("io.opentelemetry.play-mvc-2.4", 55);
        INSTRUMENTATION_MAP.put("io.opentelemetry.play-mvc-2.6", 56);
        INSTRUMENTATION_MAP.put("io.opentelemetry.vertx-http-client-3.0", 57);
        INSTRUMENTATION_MAP.put("io.opentelemetry.vertx-http-client-4.0", 58); // no usage yet
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-jms-2.0", 59);
        INSTRUMENTATION_MAP.put("io.opentelemetry.tomcat-10.0", 60);
        INSTRUMENTATION_MAP.put("io.opentelemetry.jetty-11.0", 61);
        INSTRUMENTATION_MAP.put("io.opentelemetry.liberty", 62);
        INSTRUMENTATION_MAP.put("io.opentelemetry.liberty-dispatcher", 63);
        INSTRUMENTATION_MAP.put("io.opentelemetry.methods", 64); // used by "custom instrumentation"
        INSTRUMENTATION_MAP.put("io.opentelemetry.okhttp-2.2", 65);
        INSTRUMENTATION_MAP.put("io.opentelemetry.opentelemetry-instrumentation-annotations-1.16", 66);
        INSTRUMENTATION_MAP.put("io.opentelemetry.undertow-1.4", 67);
        INSTRUMENTATION_MAP.put("io.opentelemetry.play-ws-1.0", 68);
        INSTRUMENTATION_MAP.put("io.opentelemetry.play-ws-2.0", 69);
        INSTRUMENTATION_MAP.put("io.opentelemetry.play-ws-2.1", 70);
        INSTRUMENTATION_MAP.put("io.opentelemetry.vertx-kafka-client-3.5", 71);
        INSTRUMENTATION_MAP.put("io.opentelemetry.hikaricp-3.0", 72);
        INSTRUMENTATION_MAP.put("io.opentelemetry.micrometer-1.5", 73);
        INSTRUMENTATION_MAP.put("io.opentelemetry.kafka-clients-2.6", 74);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-kafka-2.7", 75);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-web-3.1", 76);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-webmvc-5.3", 77);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-webmvc-6.0", 78);
        INSTRUMENTATION_MAP.put("io.opentelemetry.spring-webflux-5.3", 79);
        INSTRUMENTATION_MAP.put("io.opentelemetry.runtime-telemetry-java8", 80);
        INSTRUMENTATION_MAP.put("io.opentelemetry.runtime-telemetry-java17", 81);
        INSTRUMENTATION_MAP.put("io.opentelemetry.pekko-http-1.0", 82);
        // See https://github.com/quarkusio/quarkus/blob/962bae330b65af161731e5b588fc940c8f3ca086/extensions/opentelemetry/runtime/src/main/java/io/quarkus/opentelemetry/runtime/config/build/OTelBuildConfig.java#L22
        INSTRUMENTATION_MAP.put("io.quarkus.opentelemetry", 83);
    }

    // encode BitSet to a long
    static long[] encode(Set<String> instrumentations) {
        BitSet bitSet = new BitSet(64 * 2);
        for (String instrumentation : instrumentations) {
            Integer index = INSTRUMENTATION_MAP.get(instrumentation);
            if (index != null) {
                bitSet.set(index);
            } else {
                logger.debug("{} is not part of INSTRUMENTATION_MAP.", instrumentation);
            }
        }
        return bitSet.toLongArray();
    }

    private Instrumentations() {
    }
}
