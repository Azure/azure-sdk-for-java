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

import com.azure.spring.cloud.feature.management.models.EvaluationEvent;
import com.azure.spring.cloud.feature.management.models.Feature;
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

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("FeatureEvaluation", logsList.get(0).getMessage());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        Map<String, String> mdcMap = logsList.get(0).getMDCPropertyMap();
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

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("FeatureEvaluation", logsList.get(0).getMessage());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        Map<String, String> mdcMap = logsList.get(0).getMDCPropertyMap();
        assertEquals("DefaultWhenEnabled", mdcMap.get("VariantAssignmentReason"));
        assertEquals("fake-id", mdcMap.get("FeatureName"));
        assertEquals("false", mdcMap.get("Enabled"));
        assertEquals("1.1.0", mdcMap.get("Version"));
        assertEquals("FeatureEvaluation", mdcMap.get("microsoft.custom_event.name"));
        assertEquals("fake-variant", mdcMap.get("Variant"));
        assertEquals("100", mdcMap.get("VariantAssignmentPercentage"));
    }
}
