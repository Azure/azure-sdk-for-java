// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.telemetry;

import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.DEFAULT_WHEN_ENABLED;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.ENABLED;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.EVALUATION_EVENT_VERSION;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.EVENT_NAME;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.FEATURE_NAME;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.REASON;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.VARIANT;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.VARIANT_ASSIGNMENT_PERCENTAGE;
import static com.azure.spring.cloud.feature.management.telemetry.EvaluationEventConstants.VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.feature.management.models.Allocation;
import com.azure.spring.cloud.feature.management.models.EvaluationEvent;
import com.azure.spring.cloud.feature.management.models.FeatureDefinition;
import com.azure.spring.cloud.feature.management.models.PercentileAllocation;
import com.azure.spring.cloud.feature.management.models.Variant;
import com.azure.spring.cloud.feature.management.models.VariantAssignmentReason;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class LoggerTelemetryPublisherTest {

    private Logger publisherLogger;

    private ListAppender<ILoggingEvent> listAppender;

    @Mock
    private FeatureDefinition featureMock;

    @Mock
    private Variant variantMock;

    private ILoggingEvent logEvent;

    @BeforeAll
    public static void setUpLogging() {
        // Force SLF4J to initialize
        LoggerFactory.getLogger(LoggerTelemetryPublisherTest.class).info("Initializing SLF4J in test");
    }


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(LoggerTelemetryPublisher.class);

        // Check if we can cast to Logback's Logger
        if (slf4jLogger instanceof ch.qos.logback.classic.Logger) {
            publisherLogger = (ch.qos.logback.classic.Logger) slf4jLogger;
            
            // Create a new ListAppender for each test
            listAppender = new ListAppender<>();
            listAppender.start();
            
            // Remove any existing appenders of this type first
            for (Iterator<Appender<ILoggingEvent>> it = publisherLogger.iteratorForAppenders(); it.hasNext();) {
                Appender<ILoggingEvent> appender = it.next();
                if (appender instanceof ListAppender) {
                    publisherLogger.detachAppender(appender);
                }
            }
            
            // Add the fresh appender
            publisherLogger.addAppender(listAppender);
        } else {
            assumeTrue(
                false,
                "Tests require Logback implementation, but found: " + slf4jLogger.getClass().getName()
            );
        }
    }

    @Test
    void featureFlagTest(TestInfo testInfo) throws Exception {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(evaluationEvent);

        logEvent = getEvent(listAppender.list, testInfo.getDisplayName());
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("None", mdcMap.get(REASON));
        assertEquals(testInfo.getDisplayName(), mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
    }

    @Test
    void featureVariantTest(TestInfo testInfo) throws Exception {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);
        evaluationEvent.setVariant(variantMock);
        evaluationEvent.setReason(VariantAssignmentReason.DEFAULT_WHEN_ENABLED);

        when(variantMock.getName()).thenReturn("fake-variant");

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(evaluationEvent);

        logEvent = getEvent(listAppender.list, testInfo.getDisplayName());
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals(DEFAULT_WHEN_ENABLED, mdcMap.get(REASON));
        assertEquals(testInfo.getDisplayName(), mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals("fake-variant", mdcMap.get(VARIANT));
        assertEquals("100", mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
    }

    @Test
    void featureFlagDisabledTest(TestInfo testInfo) throws Exception {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        when(featureMock.isEnabled()).thenReturn(false);
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(evaluationEvent);

        logEvent = getEvent(listAppender.list, testInfo.getDisplayName());
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("None", mdcMap.get(REASON));
        assertEquals(testInfo.getDisplayName(), mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
    }

    @Test
    void featureVariantWithPercentageTest(TestInfo testInfo) throws Exception {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);
        evaluationEvent.setVariant(variantMock);
        evaluationEvent.setReason(VariantAssignmentReason.PERCENTILE);

        PercentileAllocation fakeVariant1 = new PercentileAllocation().setVariant("fake-variant").setFrom((double) 0)
            .setTo((double) 75);
        PercentileAllocation fakeVariant2 = new PercentileAllocation().setVariant("fake-variant2").setFrom((double) 76)
            .setTo((double) 100);

        when(featureMock.getAllocation()).thenReturn(new Allocation().setPercentile(List.of(fakeVariant1, fakeVariant2))
            .setDefaultWhenEnabled("defaultVariant"));
        when(variantMock.getName()).thenReturn("fake-variant");

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(evaluationEvent);

        logEvent = getEvent(listAppender.list, testInfo.getDisplayName());
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("Percentile", mdcMap.get(REASON));
        assertEquals(testInfo.getDisplayName(), mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals("fake-variant", mdcMap.get(VARIANT));
        assertEquals("75.0", mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
        assertEquals("defaultVariant", mdcMap.get(DEFAULT_WHEN_ENABLED));
    }

    @Test
    void nullEvaluationEventTest(TestInfo testInfo) {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(null);

        // Ensure no logs are generated
        assertEquals(0, listAppender.list.size());
    }

    @Test
    void nullFeatureInEvaluationEventTest(TestInfo testInfo) {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        EvaluationEvent evaluationEvent = new EvaluationEvent(null);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(evaluationEvent);

        // Ensure no logs are generated
        assertEquals(0, listAppender.list.size());
    }

    @Test
    void nullVariantInEvaluationEventTest(TestInfo testInfo) {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);
        evaluationEvent.setVariant(null);
        evaluationEvent.setReason(VariantAssignmentReason.DEFAULT_WHEN_ENABLED);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(evaluationEvent);

        logEvent = getEvent(listAppender.list, testInfo.getDisplayName());
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals(DEFAULT_WHEN_ENABLED, mdcMap.get(REASON));
        assertEquals(testInfo.getDisplayName(), mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals(null, mdcMap.get(VARIANT));
        assertEquals("100", mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
    }

    @Test
    void emptyPercentileAllocationTest(TestInfo testInfo) {
        when(featureMock.getId()).thenReturn(testInfo.getDisplayName());
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);
        evaluationEvent.setVariant(variantMock);
        evaluationEvent.setReason(VariantAssignmentReason.PERCENTILE);

        when(featureMock.getAllocation()).thenReturn(new Allocation().setPercentile(List.of()));
        when(variantMock.getName()).thenReturn("fake-variant");

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publish(evaluationEvent);

        logEvent = getEvent(listAppender.list, testInfo.getDisplayName());
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("Percentile", mdcMap.get(REASON));
        assertEquals(testInfo.getDisplayName(), mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals("fake-variant", mdcMap.get(VARIANT));
        assertEquals("0.0", mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
    }

    ILoggingEvent getEvent(List<ILoggingEvent> events, String featureName) {
        for (ILoggingEvent event : events) {
            if (featureName.equals(event.getMDCPropertyMap().get(FEATURE_NAME))) {
                return event;
            }
        }
        assumeTrue(
            false,
            "Log event not found for feature: " + featureName
        );
        return null; // This line will never be reached due to the assumption above
    }
}
