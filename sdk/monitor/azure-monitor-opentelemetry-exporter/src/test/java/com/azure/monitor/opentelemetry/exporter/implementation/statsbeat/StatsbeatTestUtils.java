// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class StatsbeatTestUtils {

    private static final Map<Integer, String> INSTRUMENTATION_MAP_DECODING;
    private static final Map<Integer, Feature> FEATURE_MAP_DECODING;

    static {
        INSTRUMENTATION_MAP_DECODING = new HashMap<>();
        INSTRUMENTATION_MAP_DECODING.put(0, "io.opentelemetry.apache-httpasyncclient-4.1");
        INSTRUMENTATION_MAP_DECODING.put(1, "io.opentelemetry.apache-httpclient-2.0");
        INSTRUMENTATION_MAP_DECODING.put(2, "io.opentelemetry.apache-httpclient-4.0");
        INSTRUMENTATION_MAP_DECODING.put(3, "io.opentelemetry.apache-httpclient-5.0");
        INSTRUMENTATION_MAP_DECODING.put(4, "io.opentelemetry.applicationinsights-web-2.3");
        INSTRUMENTATION_MAP_DECODING.put(5, "io.opentelemetry.tomcat-7.0");
        INSTRUMENTATION_MAP_DECODING.put(6, "Azure-OpenTelemetry"); // bridged by azure-core-1.14 module
        INSTRUMENTATION_MAP_DECODING.put(7, "io.opentelemetry.cassandra-3.0");
        INSTRUMENTATION_MAP_DECODING.put(8, "io.opentelemetry.cassandra-4.0");
        INSTRUMENTATION_MAP_DECODING.put(9, "io.opentelemetry.java-http-client");
        INSTRUMENTATION_MAP_DECODING.put(10, "io.opentelemetry.rabbitmq-2.7");
        INSTRUMENTATION_MAP_DECODING.put(11, "io.opentelemetry.spring-integration-4.1");
        INSTRUMENTATION_MAP_DECODING.put(12, "io.opentelemetry.grpc-1.6");
        INSTRUMENTATION_MAP_DECODING.put(13, "io.opentelemetry.http-url-connection");
        INSTRUMENTATION_MAP_DECODING.put(14, "io.opentelemetry.servlet-5.0");
        // index 15 retired in 3.3.0 GA (was java-util-logging)
        INSTRUMENTATION_MAP_DECODING.put(16, "io.opentelemetry.jaxrs-1.0");
        // index 17 retired in 3.3.0 GA (was jaxrs-2.0-common)
        INSTRUMENTATION_MAP_DECODING.put(18, "io.opentelemetry.async-http-client-1.9");
        INSTRUMENTATION_MAP_DECODING.put(19, "io.opentelemetry.async-http-client-2.0");
        INSTRUMENTATION_MAP_DECODING.put(20, "io.opentelemetry.google-http-client-1.19");
        INSTRUMENTATION_MAP_DECODING.put(21, "io.opentelemetry.jdbc");
        INSTRUMENTATION_MAP_DECODING.put(22, "io.opentelemetry.jedis-1.4");
        INSTRUMENTATION_MAP_DECODING.put(23, "io.opentelemetry.jedis-3.0");
        INSTRUMENTATION_MAP_DECODING.put(24, "io.opentelemetry.jetty-8.0");
        INSTRUMENTATION_MAP_DECODING.put(25, "io.opentelemetry.jms-1.1");
        INSTRUMENTATION_MAP_DECODING.put(26, "io.opentelemetry.kafka-clients-0.11");
        INSTRUMENTATION_MAP_DECODING.put(27, "io.opentelemetry.kafka-streams-0.11");
        INSTRUMENTATION_MAP_DECODING.put(28, "io.opentelemetry.jetty-httpclient-9.2");
        INSTRUMENTATION_MAP_DECODING.put(29, "io.opentelemetry.lettuce-4.0");
        INSTRUMENTATION_MAP_DECODING.put(30, "io.opentelemetry.lettuce-5.0");
        INSTRUMENTATION_MAP_DECODING.put(31, "io.opentelemetry.lettuce-5.1");
        INSTRUMENTATION_MAP_DECODING.put(32, "io.opentelemetry.spring-rabbit-1.0");
        // index 33 retired in 3.3.0 GA (was jaxrs-2.0-client)
        // index 34 retired in 3.3.0 GA (was log4j-1.2)
        // index 35 retired in 3.3.0 GA (was log4j-2.0)
        // index 36 retired in 3.3.0 GA (was jaxrs-client-2.0-resteasy-3.0)
        // index 37 retired in 3.3.0 GA (was logback-1.0)
        INSTRUMENTATION_MAP_DECODING.put(38, "io.opentelemetry.micrometer-1.0");
        INSTRUMENTATION_MAP_DECODING.put(39, "io.opentelemetry.mongo-3.1"); // mongo 4.0 is covered in 3.1
        INSTRUMENTATION_MAP_DECODING.put(40, "io.opentelemetry.grizzly-2.0");
        INSTRUMENTATION_MAP_DECODING.put(41, "io.opentelemetry.quartz-2.0");
        INSTRUMENTATION_MAP_DECODING.put(42, "io.opentelemetry.apache-camel-2.20");
        INSTRUMENTATION_MAP_DECODING.put(43, "io.opentelemetry.netty-4.0");
        INSTRUMENTATION_MAP_DECODING.put(44, "io.opentelemetry.netty-4.1");
        INSTRUMENTATION_MAP_DECODING.put(45, "io.opentelemetry.okhttp-3.0");
        INSTRUMENTATION_MAP_DECODING.put(46, "io.opentelemetry.opentelemetry-annotations-1.0");
        INSTRUMENTATION_MAP_DECODING.put(47, "io.opentelemetry.akka-http-10.0");
        INSTRUMENTATION_MAP_DECODING.put(48, "io.opentelemetry.spring-webmvc-3.1");
        INSTRUMENTATION_MAP_DECODING.put(49, "io.opentelemetry.spring-webflux-5.0");
        INSTRUMENTATION_MAP_DECODING.put(50, "io.opentelemetry.reactor-netty-1.0");
        INSTRUMENTATION_MAP_DECODING.put(51, "io.opentelemetry.servlet-2.2");
        INSTRUMENTATION_MAP_DECODING.put(52, "io.opentelemetry.servlet-3.0");
        // index 53 is open (was servlet-common but that instrumentation doesn't emit telemetry)
        INSTRUMENTATION_MAP_DECODING.put(54, "io.opentelemetry.spring-scheduling-3.1");
        INSTRUMENTATION_MAP_DECODING.put(55, "io.opentelemetry.play-mvc-2.4");
        INSTRUMENTATION_MAP_DECODING.put(56, "io.opentelemetry.play-mvc-2.6");
        INSTRUMENTATION_MAP_DECODING.put(57, "io.opentelemetry.vertx-http-client-3.0");
        INSTRUMENTATION_MAP_DECODING.put(58, "io.opentelemetry.vertx-http-client-4.0");
        INSTRUMENTATION_MAP_DECODING.put(59, "io.opentelemetry.spring-jms-2.0");
        INSTRUMENTATION_MAP_DECODING.put(60, "io.opentelemetry.tomcat-10.0");
        INSTRUMENTATION_MAP_DECODING.put(61, "io.opentelemetry.jetty-11.0");
        INSTRUMENTATION_MAP_DECODING.put(62, "io.opentelemetry.liberty");
        INSTRUMENTATION_MAP_DECODING.put(63, "io.opentelemetry.liberty-dispatcher");
        INSTRUMENTATION_MAP_DECODING.put(64, "io.opentelemetry.methods"); // used by "custom instrumentation"
        INSTRUMENTATION_MAP_DECODING.put(65, "io.opentelemetry.okhttp-2.2");
        INSTRUMENTATION_MAP_DECODING.put(66, "io.opentelemetry.opentelemetry-instrumentation-annotations-1.16");
        INSTRUMENTATION_MAP_DECODING.put(67, "io.opentelemetry.undertow-1.4");
        INSTRUMENTATION_MAP_DECODING.put(68, "io.opentelemetry.play-ws-1.0");
        INSTRUMENTATION_MAP_DECODING.put(69, "io.opentelemetry.play-ws-2.0");
        INSTRUMENTATION_MAP_DECODING.put(70, "io.opentelemetry.play-ws-2.1");
        INSTRUMENTATION_MAP_DECODING.put(71, "io.opentelemetry.vertx-kafka-client-3.5");
        INSTRUMENTATION_MAP_DECODING.put(72, "io.opentelemetry.hikaricp-3.0");
        INSTRUMENTATION_MAP_DECODING.put(73, "io.opentelemetry.micrometer-1.5");
        INSTRUMENTATION_MAP_DECODING.put(74, "io.opentelemetry.kafka-clients-2.6");
        INSTRUMENTATION_MAP_DECODING.put(75, "io.opentelemetry.spring-kafka-2.7");
        INSTRUMENTATION_MAP_DECODING.put(76, "io.opentelemetry.spring-web-3.1");
        INSTRUMENTATION_MAP_DECODING.put(77, "io.opentelemetry.spring-webmvc-5.3");
        INSTRUMENTATION_MAP_DECODING.put(78, "io.opentelemetry.spring-webmvc-6.0");
        INSTRUMENTATION_MAP_DECODING.put(79, "io.opentelemetry.spring-webflux-5.3");
        INSTRUMENTATION_MAP_DECODING.put(80, "io.opentelemetry.runtime-telemetry-java8");
        INSTRUMENTATION_MAP_DECODING.put(81, "io.opentelemetry.runtime-telemetry-java17");

        FEATURE_MAP_DECODING = new HashMap<>();
        FEATURE_MAP_DECODING.put(0, Feature.JAVA_VENDOR_ORACLE);
        FEATURE_MAP_DECODING.put(1, Feature.JAVA_VENDOR_ZULU);
        FEATURE_MAP_DECODING.put(2, Feature.JAVA_VENDOR_MICROSOFT);
        FEATURE_MAP_DECODING.put(3, Feature.JAVA_VENDOR_ADOPT_OPENJDK);
        FEATURE_MAP_DECODING.put(4, Feature.JAVA_VENDOR_REDHAT);
        FEATURE_MAP_DECODING.put(5, Feature.JAVA_VENDOR_OTHER);
        FEATURE_MAP_DECODING.put(6, Feature.AAD);
        FEATURE_MAP_DECODING.put(7, Feature.CASSANDRA_DISABLED);
        FEATURE_MAP_DECODING.put(8, Feature.JDBC_DISABLED);
        FEATURE_MAP_DECODING.put(9, Feature.JMS_DISABLED);
        FEATURE_MAP_DECODING.put(10, Feature.KAFKA_DISABLED);
        FEATURE_MAP_DECODING.put(11, Feature.MICROMETER_DISABLED);
        FEATURE_MAP_DECODING.put(12, Feature.MONGO_DISABLED);
        FEATURE_MAP_DECODING.put(13, Feature.REDIS_DISABLED);
        FEATURE_MAP_DECODING.put(14, Feature.SPRING_SCHEDULING_DISABLED);
        FEATURE_MAP_DECODING.put(15, Feature.AZURE_SDK_DISABLED);
        FEATURE_MAP_DECODING.put(16, Feature.RABBITMQ_DISABLED);
        FEATURE_MAP_DECODING.put(17, Feature.SPRING_INTEGRATION_DISABLED);
        FEATURE_MAP_DECODING.put(18, Feature.LEGACY_PROPAGATION_ENABLED);
        FEATURE_MAP_DECODING.put(19, Feature.GRIZZLY_ENABLED);
        FEATURE_MAP_DECODING.put(20, Feature.STATSBEAT_DISABLED);
        FEATURE_MAP_DECODING.put(21, Feature.QUARTZ_DISABLED);
        FEATURE_MAP_DECODING.put(22, Feature.APACHE_CAMEL_DISABLED);
        FEATURE_MAP_DECODING.put(23, Feature.AKKA_DISABLED);
    }

    static Set<String> decodeInstrumentations(long[] longArray) {
        Set<String> result = new HashSet<>();
        if (longArray.length > 0) {
            result.addAll(decode(false, longArray[0], INSTRUMENTATION_MAP_DECODING));
            if (longArray.length == 2) {
                result.addAll(decode(true, longArray[1], INSTRUMENTATION_MAP_DECODING));
            }
        }
        return result;
    }

    static Set<Feature> decodeFeature(long num) {
        return decode(false, num, FEATURE_MAP_DECODING);
    }

    private static <E> Set<E> decode(boolean greaterThan64Bits, long num, Map<Integer, E> decodedMap) {
        Set<E> result = new HashSet<>();
        for (Map.Entry<Integer, E> entry : decodedMap.entrySet()) {
            int value = entry.getKey();
            long powerVal;
            if (greaterThan64Bits && value > 63) {
                powerVal = (long) Math.pow(2, value - 64);
            } else {
                powerVal = (long) Math.pow(2, value);
            }
            if ((powerVal & num) == powerVal) {
                E target = decodedMap.get(value + 64);
                if (greaterThan64Bits && target != null) {
                    result.add(target);
                } else {
                    result.add(entry.getValue());
                }
            }
        }
        return result;
    }

    private StatsbeatTestUtils() {
    }
}
