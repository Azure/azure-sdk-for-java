package com.azure.spring.cloud.feature.management;

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

        assertEquals("FeatureEvaluation", logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("None", mdcMap.get("VariantAssignmentReason"));
        assertEquals("fake-id", mdcMap.get("FeatureName"));
        assertEquals("false", mdcMap.get("Enabled"));
        assertEquals("1.1.0", mdcMap.get("Version"));
        assertEquals("FeatureEvaluation", mdcMap.get("microsoft.custom_event.name"));
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

        assertEquals("FeatureEvaluation", logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("DefaultWhenEnabled", mdcMap.get("VariantAssignmentReason"));
        assertEquals("fake-id", mdcMap.get("FeatureName"));
        assertEquals("false", mdcMap.get("Enabled"));
        assertEquals("1.1.0", mdcMap.get("Version"));
        assertEquals("FeatureEvaluation", mdcMap.get("microsoft.custom_event.name"));
        assertEquals("fake-variant", mdcMap.get("Variant"));
        assertEquals("100", mdcMap.get("VariantAssignmentPercentage"));
    }

    @Test
    void featureFlagDisabledTest() throws Exception {
        when(featureMock.isEnabled()).thenReturn(false);
        EvaluationEvent evaluationEvent = new EvaluationEvent(featureMock);

        LoggerTelemetryPublisher publisher = new LoggerTelemetryPublisher();
        publisher.publishTelemetry(evaluationEvent);

        logEvent = listAppender.list.get(0);
        Map<String, String> mdcMap = logEvent.getMDCPropertyMap();

        assertEquals("FeatureEvaluation", logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("None", mdcMap.get("VariantAssignmentReason"));
        assertEquals("fake-id", mdcMap.get("FeatureName"));
        assertEquals("false", mdcMap.get("Enabled"));
        assertEquals("1.1.0", mdcMap.get("Version"));
        assertEquals("FeatureEvaluation", mdcMap.get("microsoft.custom_event.name"));
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

        assertEquals("FeatureEvaluation", logEvent.getMessage());
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("Percentile", mdcMap.get("VariantAssignmentReason"));
        assertEquals("fake-id", mdcMap.get("FeatureName"));
        assertEquals("false", mdcMap.get("Enabled"));
        assertEquals("1.1.0", mdcMap.get("Version"));
        assertEquals("FeatureEvaluation", mdcMap.get("microsoft.custom_event.name"));
        assertEquals("fake-variant", mdcMap.get("Variant"));
        assertEquals("75.0", mdcMap.get("VariantAssignmentPercentage"));
        assertEquals("defaultVariant", mdcMap.get("DefaultWhenEnabled"));
    }
}
