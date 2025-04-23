package com.azure.spring.cloud.feature.management.telemetry;

import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.DEFAULT_WHEN_ENABLED;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.ENABLED;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.EVALUATION_EVENT_VERSION;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.EVENT_NAME;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.FEATURE_NAME;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.REASON;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.VARIANT;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.VARIANT_ASSIGNMENT_PERCENTAGE;
import static com.azure.spring.cloud.feature.management.telemetry.TelemetryConstants.VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.feature.management.models.Allocation;
import com.azure.spring.cloud.feature.management.models.EvaluationEvent;
import com.azure.spring.cloud.feature.management.models.Feature;
import com.azure.spring.cloud.feature.management.models.PercentileAllocation;
import com.azure.spring.cloud.feature.management.models.Variant;
import com.azure.spring.cloud.feature.management.models.VariantAssignmentReason;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class LoggerTelemetryPublisherTest {

    private Logger publisherLogger = (Logger) LoggerFactory.getLogger(LoggerTelemetryPublisher.class);

    private ListAppender<ILoggingEvent> listAppender;

    @Mock
    private Feature featureMock;

    @Mock
    private Variant variantMock;

    private ILoggingEvent logEvent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        listAppender = new ListAppender<>();
        listAppender.start();
        publisherLogger.addAppender(listAppender);
        when(featureMock.getId()).thenReturn("fake-id");
    }

    @Test
    void featureFlagTest() throws Exception {
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(evaluationEvent);

        logEvent = listAppender.list.get(0);
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("None", mdcMap.get(REASON));
        assertEquals("fake-id", mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
    }

    @Test
    void featureVariantTest() throws Exception {
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);
        evaluationEvent.setVariant(variantMock);
        evaluationEvent.setReason(VariantAssignmentReason.DEFAULT_WHEN_ENABLED);

        when(variantMock.getName()).thenReturn("fake-variant");

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(evaluationEvent);

        logEvent = listAppender.list.get(0);
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals(DEFAULT_WHEN_ENABLED, mdcMap.get(REASON));
        assertEquals("fake-id", mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals("fake-variant", mdcMap.get(VARIANT));
        assertEquals("100", mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
    }

    @Test
    void featureFlagDisabledTest() throws Exception {
        when(featureMock.isEnabled()).thenReturn(false);
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(evaluationEvent);

        logEvent = listAppender.list.get(0);
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("None", mdcMap.get(REASON));
        assertEquals("fake-id", mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
    }

    @Test
    void featureVariantWithPercentageTest() throws Exception {
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
        publisher.publishTelemetry(evaluationEvent);

        logEvent = listAppender.list.get(0);
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("Percentile", mdcMap.get(REASON));
        assertEquals("fake-id", mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals("fake-variant", mdcMap.get(VARIANT));
        assertEquals("75.0", mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
        assertEquals("defaultVariant", mdcMap.get(DEFAULT_WHEN_ENABLED));
    }

    @Test
    void nullEvaluationEventTest() {
        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(null);

        // Ensure no logs are generated
        assertEquals(0, listAppender.list.size());
    }

    @Test
    void nullFeatureInEvaluationEventTest() {
        EvaluationEvent evaluationEvent = new EvaluationEvent(null);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(evaluationEvent);

        // Ensure no logs are generated
        assertEquals(0, listAppender.list.size());
    }

    @Test
    void nullVariantInEvaluationEventTest() {
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);
        evaluationEvent.setVariant(null);
        evaluationEvent.setReason(VariantAssignmentReason.DEFAULT_WHEN_ENABLED);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(evaluationEvent);

        logEvent = listAppender.list.get(0);
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals(DEFAULT_WHEN_ENABLED, mdcMap.get(REASON));
        assertEquals("fake-id", mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals(null, mdcMap.get(VARIANT));
        assertEquals("100", mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
    }

    @Test
    void emptyPercentileAllocationTest() {
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);
        evaluationEvent.setVariant(variantMock);
        evaluationEvent.setReason(VariantAssignmentReason.PERCENTILE);

        when(featureMock.getAllocation()).thenReturn(new Allocation().setPercentile(List.of()));
        when(variantMock.getName()).thenReturn("fake-variant");

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(evaluationEvent);

        logEvent = listAppender.list.get(0);
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals(EVENT_NAME, logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("Percentile", mdcMap.get(REASON));
        assertEquals("fake-id", mdcMap.get(FEATURE_NAME));
        assertEquals("false", mdcMap.get(ENABLED));
        assertEquals(EVALUATION_EVENT_VERSION, mdcMap.get(VERSION));
        assertEquals(EVENT_NAME, mdcMap.get(APPLICATION_INSIGHTS_CUSTOM_EVENT_KEY));
        assertEquals("fake-variant", mdcMap.get(VARIANT));
        assertEquals(null, mdcMap.get(VARIANT_ASSIGNMENT_PERCENTAGE));
    }
}
