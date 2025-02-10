// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.MessageTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.DerivedMetricProjections;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.FilteringConfiguration;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.KnownExceptionColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.KnownRequestColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.QuickPulseTestBase.createRemoteDependencyTelemetry;
import static com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.QuickPulseTestBase.createRequestTelemetry;
import static org.assertj.core.api.Assertions.assertThat;

class QuickPulseDataCollectorTests {

    private static final String FAKE_INSTRUMENTATION_KEY = "fake-instrumentation-key";
    private static final ConnectionString FAKE_CONNECTION_STRING
        = ConnectionString.parse("InstrumentationKey=" + FAKE_INSTRUMENTATION_KEY);

    @Test
    void initialStateIsDisabled() {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        assertThat(new QuickPulseDataCollector(configuration).peek()).isNull();
    }

    @Test
    void emptyCountsAndDurationsAfterEnable() {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertCountersReset(counters);
    }

    @Test
    void nullCountersAfterDisable() {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);
        collector.disable();
        assertThat(collector.peek()).isNull();
    }

    @Test
    void requestTelemetryIsCounted_DurationIsSum() {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        // add a success and peek
        long duration = 112233L;
        TelemetryItem telemetry = createRequestTelemetry("request-test", new Date(), duration, "200", true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertThat(counters.requests).isEqualTo(1);
        assertThat(counters.unsuccessfulRequests).isEqualTo(0);
        assertThat(counters.requestsDuration).isEqualTo((double) duration);

        // add another success and peek
        long duration2 = 65421L;
        telemetry = createRequestTelemetry("request-test-2", new Date(), duration2, "200", true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.peek();
        double total = duration + duration2;
        assertThat(counters.requests).isEqualTo(2);
        assertThat(counters.unsuccessfulRequests).isEqualTo(0);
        assertThat(counters.requestsDuration).isEqualTo(total);

        // add a failure and get/reset
        long duration3 = 9988L;
        telemetry = createRequestTelemetry("request-test-3", new Date(), duration3, "400", false);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.getAndRestart();
        total += duration3;
        assertThat(counters.requests).isEqualTo(3);
        assertThat(counters.unsuccessfulRequests).isEqualTo(1);
        assertThat(counters.requestsDuration).isEqualTo(total);

        assertCountersReset(collector.peek());
    }

    @Test
    void dependencyTelemetryIsCounted_DurationIsSum() {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        // add a success and peek.
        long duration = 112233L;
        TelemetryItem telemetry = createRemoteDependencyTelemetry("dep-test", "dep-test-cmd", duration, true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertThat(counters.rdds).isEqualTo(1);
        assertThat(counters.unsuccessfulRdds).isEqualTo(0);
        assertThat(counters.rddsDuration).isEqualTo((double) duration);

        // add another success and peek.
        long duration2 = 334455L;
        telemetry = createRemoteDependencyTelemetry("dep-test-2", "dep-test-cmd-2", duration2, true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.peek();
        assertThat(counters.rdds).isEqualTo(2);
        assertThat(counters.unsuccessfulRdds).isEqualTo(0);
        double total = duration + duration2;
        assertThat(counters.rddsDuration).isEqualTo(total);

        // add a failure and get/reset.
        long duration3 = 123456L;
        telemetry = createRemoteDependencyTelemetry("dep-test-3", "dep-test-cmd-3", duration3, false);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.getAndRestart();
        assertThat(counters.rdds).isEqualTo(3);
        assertThat(counters.unsuccessfulRdds).isEqualTo(1);
        total += duration3;
        assertThat(counters.rddsDuration).isEqualTo(total);

        assertCountersReset(collector.peek());
    }

    @Test
    void exceptionTelemetryIsCounted() {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        TelemetryItem telemetry = ExceptionTelemetryBuilder.create().build();
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertThat(counters.exceptions).isEqualTo(1);

        telemetry = ExceptionTelemetryBuilder.create().build();
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.getAndRestart();
        assertThat(counters.exceptions).isEqualTo(2);

        assertCountersReset(collector.peek());
    }

    @Test
    void encodeDecodeIsIdentity() {
        long count = 456L;
        long duration = 112233L;
        long encoded = QuickPulseDataCollector.Counters.encodeCountAndDuration(count, duration);
        QuickPulseDataCollector.CountAndDuration inputs
            = QuickPulseDataCollector.Counters.decodeCountAndDuration(encoded);
        assertThat(inputs.count).isEqualTo(count);
        assertThat(inputs.duration).isEqualTo(duration);
    }

    @Test
    void parseDurations() {
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:00.123456")).isEqualTo(123);
        // current behavior rounds down (not sure if that's good or not?)
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:00.123999")).isEqualTo(123);
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:01.123456"))
            .isEqualTo(Duration.ofSeconds(1).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:12.123456"))
            .isEqualTo(Duration.ofSeconds(12).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:01:23.123456"))
            .isEqualTo(Duration.ofMinutes(1).plusSeconds(23).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:12:34.123456"))
            .isEqualTo(Duration.ofMinutes(12).plusSeconds(34).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("01:23:45.123456"))
            .isEqualTo(Duration.ofHours(1).plusMinutes(23).plusSeconds(45).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("12:34:56.123456"))
            .isEqualTo(Duration.ofHours(12).plusMinutes(34).plusSeconds(56).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("1.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(1).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("11.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(11).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("111.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(111).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("1111.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(1111).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
    }

    private static void assertCountersReset(QuickPulseDataCollector.FinalCounters counters) {
        assertThat(counters).isNotNull();

        assertThat(counters.rdds).isEqualTo(0);
        assertThat(counters.rddsDuration).isEqualTo(0);
        assertThat(counters.unsuccessfulRdds).isEqualTo(0);

        assertThat(counters.requests).isEqualTo(0);
        assertThat(counters.requestsDuration).isEqualTo(0);
        assertThat(counters.unsuccessfulRequests).isEqualTo(0);

        assertThat(counters.exceptions).isEqualTo(0);
    }

    @Test
    void checkDocumentsListSize() {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        long duration = 112233L;
        TelemetryItem telemetry = createRequestTelemetry("request-test", new Date(), duration, "200", true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        for (int i = 0; i < 1005; i++) {
            collector.add(telemetry);
        }
        // check max documentList size
        assertThat(collector.getAndRestart().documentList.size()).isEqualTo(1000);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_OFF);
        for (int i = 0; i < 5; i++) {
            collector.add(telemetry);
        }
        // no telemetry items are added when QP_IS_OFF
        assertThat(collector.getAndRestart().documentList.size()).isEqualTo(0);
    }

    @Test
    void honorDefaultConfig() {
        CollectionConfigurationInfo defaultConfig = createDefaultConfig();
        AtomicReference<FilteringConfiguration> configuration
            = new AtomicReference<>(new FilteringConfiguration(defaultConfig));
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        createTelemetryItemsForFiltering(collector);

        QuickPulseDataCollector.FinalCounters counters = collector.peek();

        // The default documents config asks to collect documents of the "Event" telemetry type
        // As the SDK does not collect the Event telemetry type for live metrics, we consider that part of the config invalid
        List<CollectionConfigurationError> errors = counters.configErrors;
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0).getCollectionConfigurationErrorType())
            .isEqualTo(CollectionConfigurationErrorType.METRIC_TELEMETRY_TYPE_UNSUPPORTED);

        List<DocumentIngress> documents = counters.documentList;

        assertThat(documents.size()).isEqualTo(4);
        DocumentIngress failedReqDoc = counters.documentList.get(0);
        assertThat(failedReqDoc.getDocumentType()).isEqualTo(DocumentType.REQUEST);
        assertThat(((Request) failedReqDoc).getName()).isEqualTo("request-failed");
        DocumentIngress failedDepDoc = counters.documentList.get(1);
        assertThat(failedDepDoc.getDocumentType()).isEqualTo(DocumentType.REMOTE_DEPENDENCY);
        assertThat(((RemoteDependency) failedDepDoc).getName()).isEqualTo("dep-failed");
        DocumentIngress exceptionDoc = counters.documentList.get(2);
        assertThat(exceptionDoc.getDocumentType()).isEqualTo(DocumentType.EXCEPTION);
        DocumentIngress traceDoc = counters.documentList.get(3);
        assertThat(traceDoc.getDocumentType()).isEqualTo(DocumentType.TRACE);

        assertDefaultMetrics(counters);

        counters = collector.getAndRestart();
        assertCountersReset(collector.peek());
    }

    @Test
    void honorDifferentMultipleSessionDocConfig() {
        CollectionConfigurationInfo multiSessionDocsConfig = createMultiSessionDocumentsConfig(false);
        AtomicReference<FilteringConfiguration> configuration
            = new AtomicReference<>(new FilteringConfiguration(multiSessionDocsConfig));
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        createTelemetryItemsForFiltering(collector);

        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        List<DocumentIngress> documents = counters.documentList;

        assertThat(documents.size()).isEqualTo(5);

        DocumentIngress sucReqDoc = counters.documentList.get(0);
        assertThat(sucReqDoc.getDocumentType()).isEqualTo(DocumentType.REQUEST);
        assertThat(((Request) sucReqDoc).getName()).isEqualTo("request-success");
        assertThat(sucReqDoc.getDocumentStreamIds().get(0)).isEqualTo("random-stream-id");

        DocumentIngress failedReqDoc = counters.documentList.get(1);
        assertThat(failedReqDoc.getDocumentType()).isEqualTo(DocumentType.REQUEST);
        assertThat(((Request) failedReqDoc).getName()).isEqualTo("request-failed");
        assertThat(failedReqDoc.getDocumentStreamIds().get(0)).isEqualTo("all-types-default");

        DocumentIngress failedDepDoc = counters.documentList.get(2);
        assertThat(failedDepDoc.getDocumentType()).isEqualTo(DocumentType.REMOTE_DEPENDENCY);
        assertThat(((RemoteDependency) failedDepDoc).getName()).isEqualTo("dep-failed");
        assertThat(failedDepDoc.getDocumentStreamIds().size()).isEqualTo(2);

        DocumentIngress exceptionDoc = counters.documentList.get(3);
        assertThat(exceptionDoc.getDocumentType()).isEqualTo(DocumentType.EXCEPTION);
        assertThat(exceptionDoc.getDocumentStreamIds().size()).isEqualTo(2);

        DocumentIngress traceDoc = counters.documentList.get(4);
        assertThat(traceDoc.getDocumentType()).isEqualTo(DocumentType.TRACE);
        assertThat(traceDoc.getDocumentStreamIds().size()).isEqualTo(2);

        counters = collector.getAndRestart();
        assertCountersReset(collector.peek());
    }

    @Test
    void honorDuplicateMultipleSessionDocConfig() {
        CollectionConfigurationInfo multiSessionDocsConfig = createMultiSessionDocumentsConfig(true);
        AtomicReference<FilteringConfiguration> configuration
            = new AtomicReference<>(new FilteringConfiguration(multiSessionDocsConfig));

        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);
        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);
        createTelemetryItemsForFiltering(collector);

        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        List<DocumentIngress> documents = counters.documentList;

        assertThat(documents.size()).isEqualTo(4);

        DocumentIngress failedReqDoc = counters.documentList.get(0);
        assertThat(failedReqDoc.getDocumentType()).isEqualTo(DocumentType.REQUEST);
        assertThat(((Request) failedReqDoc).getName()).isEqualTo("request-failed");
        assertThat(failedReqDoc.getDocumentStreamIds().size()).isEqualTo(1);

        DocumentIngress failedDepDoc = counters.documentList.get(1);
        assertThat(failedDepDoc.getDocumentType()).isEqualTo(DocumentType.REMOTE_DEPENDENCY);
        assertThat(((RemoteDependency) failedDepDoc).getName()).isEqualTo("dep-failed");
        assertThat(failedDepDoc.getDocumentStreamIds().size()).isEqualTo(1);

        DocumentIngress exceptionDoc = counters.documentList.get(2);
        assertThat(exceptionDoc.getDocumentType()).isEqualTo(DocumentType.EXCEPTION);
        assertThat(exceptionDoc.getDocumentStreamIds().size()).isEqualTo(1);

        DocumentIngress traceDoc = counters.documentList.get(3);
        assertThat(traceDoc.getDocumentType()).isEqualTo(DocumentType.TRACE);
        assertThat(traceDoc.getDocumentStreamIds().size()).isEqualTo(1);

        counters = collector.getAndRestart();
        assertCountersReset(collector.peek());
    }

    @Test
    void testMetricChartFiltering() {
        CollectionConfigurationInfo derivedMetricsConfig = createDerivedMetricConfig();
        AtomicReference<FilteringConfiguration> configuration
            = new AtomicReference<>(new FilteringConfiguration(derivedMetricsConfig));

        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);
        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);
        createTelemetryItemsForFiltering(collector);

        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        // The default metrics should not be impacted by derived metric filters
        assertDefaultMetrics(counters);
        Map<String, Double> finalDerivedMetricValues = counters.projections;

        // The config asks to take the avg duration of requests that have response code 200.
        // Only one such request came through and that request has a duration of 300.
        assertThat(finalDerivedMetricValues.get("request-duration")).isEqualTo(300.0);

        // The config asks to count the # of exceptions that contain the message "hi". No
        // exceptions contain that message.
        assertThat(finalDerivedMetricValues.get("exception-count")).isEqualTo(0.0);

        counters = collector.getAndRestart();
        QuickPulseDataCollector.FinalCounters resetCounters = collector.peek();
        assertCountersReset(resetCounters);

        Map<String, Double> resetProjections = new HashMap<>();
        resetProjections.put("request-duration", 0.0);
        resetProjections.put("exception-count", 0.0);

        assertThat(resetCounters.projections).isEqualTo(resetProjections);
    }

    private void assertDefaultMetrics(QuickPulseDataCollector.FinalCounters counters) {
        assertThat(counters.rdds).isEqualTo(2);
        assertThat(counters.unsuccessfulRdds).isEqualTo(1);
        // The below line represents the "\\ApplicationInsights\\Dependency Call Duration" counter, which is meant to be an average in the 1s interval. (500 + 300) / 2 = 400.
        // See this same logic used in the QuickPulseDataFetcher when building the monitoring point.
        assertThat(counters.rddsDuration / counters.rdds).isEqualTo(400);
        assertThat(counters.requests).isEqualTo(2);
        assertThat(counters.unsuccessfulRequests).isEqualTo(1);
        // The below line represents the "\\ApplicationInsights\\Request Duration" counter, which is meant to be an average in the 1s interval. (500 + 300) / 2 = 400.
        // See this same logic used in the QuickPulseDataFetcher when building the monitoring point.
        assertThat(counters.requestsDuration / counters.requests).isEqualTo(400);
        assertThat(counters.exceptions).isEqualTo(1);
    }

    private void createTelemetryItemsForFiltering(QuickPulseDataCollector collector) {
        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        TelemetryItem successRequest = createRequestTelemetry("request-success", new Date(), 300, "200", true);
        successRequest.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(successRequest);

        TelemetryItem failedRequest = createRequestTelemetry("request-failed", new Date(), 500, "400", false);
        failedRequest.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(failedRequest);

        TelemetryItem sucDep = createRemoteDependencyTelemetry("dep-success", "dep-success", 300, true);
        sucDep.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(sucDep);

        TelemetryItem failedDep = createRemoteDependencyTelemetry("dep-failed", "dep-failed", 500, false);
        failedDep.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(failedDep);

        TelemetryItem exception = ExceptionTelemetryBuilder.create().build();
        exception.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(exception);

        TelemetryItem trace = MessageTelemetryBuilder.create().build();
        trace.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(trace);
    }

    private CollectionConfigurationInfo createDefaultConfig() {
        CollectionConfigurationInfo defaultConfig = new CollectionConfigurationInfo();
        List<DocumentStreamInfo> documentStreams = new ArrayList<>();
        DocumentStreamInfo defaultStream = createDocumentStream(true);
        documentStreams.add(defaultStream);

        defaultConfig.setDocumentStreams(documentStreams);
        defaultConfig.setETag("random-etag");
        defaultConfig.setMetrics(new ArrayList<>());

        return defaultConfig;
    }

    private CollectionConfigurationInfo createMultiSessionDocumentsConfig(boolean duplicateSession) {
        CollectionConfigurationInfo config = new CollectionConfigurationInfo();
        List<DocumentStreamInfo> documentStreams = new ArrayList<>();
        DocumentStreamInfo defaultStream = createDocumentStream(true);
        DocumentStreamInfo secondSessionStream
            = duplicateSession ? createDocumentStream(true) : createDocumentStream(false);
        documentStreams.add(defaultStream);
        documentStreams.add(secondSessionStream);

        config.setDocumentStreams(documentStreams);
        config.setETag("random-etag");
        config.setMetrics(new ArrayList<>());

        return config;
    }

    private DocumentStreamInfo createDocumentStream(boolean isDefault) {
        List<DocumentFilterConjunctionGroupInfo> docFilterGroups = new ArrayList<>();

        FilterInfo successFalse = new FilterInfo();
        successFalse.setFieldName(KnownRequestColumns.SUCCESS);
        successFalse.setPredicate(PredicateType.EQUAL);
        successFalse.setComparand("false");

        FilterInfo successTrue = new FilterInfo();
        successTrue.setFieldName(KnownRequestColumns.SUCCESS);
        successTrue.setPredicate(PredicateType.EQUAL);
        successTrue.setComparand("true");

        List<FilterInfo> successTrueList = new ArrayList<>();
        successTrueList.add(successTrue);

        List<FilterInfo> successFalseList = new ArrayList<>();
        successFalseList.add(successFalse);

        DocumentFilterConjunctionGroupInfo requestDocFilterGroup = new DocumentFilterConjunctionGroupInfo();
        requestDocFilterGroup.setTelemetryType(TelemetryType.REQUEST);
        FilterConjunctionGroupInfo requestGroup = new FilterConjunctionGroupInfo();
        requestGroup.setFilters(isDefault ? successFalseList : successTrueList);
        requestDocFilterGroup.setFilters(requestGroup);
        docFilterGroups.add(requestDocFilterGroup);

        DocumentFilterConjunctionGroupInfo dependencyDocFilterGroup = new DocumentFilterConjunctionGroupInfo();
        dependencyDocFilterGroup.setTelemetryType(TelemetryType.DEPENDENCY);
        FilterConjunctionGroupInfo dependencyGroup = new FilterConjunctionGroupInfo();
        dependencyGroup.setFilters(successFalseList);
        dependencyDocFilterGroup.setFilters(dependencyGroup);
        docFilterGroups.add(dependencyDocFilterGroup);

        DocumentFilterConjunctionGroupInfo exceptionDocFilterGroup = new DocumentFilterConjunctionGroupInfo();
        exceptionDocFilterGroup.setTelemetryType(TelemetryType.EXCEPTION);
        FilterConjunctionGroupInfo exceptionGroup = new FilterConjunctionGroupInfo();
        exceptionGroup.setFilters(new ArrayList<>());
        exceptionDocFilterGroup.setFilters(exceptionGroup);
        docFilterGroups.add(exceptionDocFilterGroup);

        DocumentFilterConjunctionGroupInfo eventDocFilterGroup = new DocumentFilterConjunctionGroupInfo();
        eventDocFilterGroup.setTelemetryType(TelemetryType.EVENT);
        FilterConjunctionGroupInfo eventGroup = new FilterConjunctionGroupInfo();
        eventGroup.setFilters(new ArrayList<>());
        eventDocFilterGroup.setFilters(eventGroup);
        docFilterGroups.add(eventDocFilterGroup);

        DocumentFilterConjunctionGroupInfo traceDocFilterGroup = new DocumentFilterConjunctionGroupInfo();
        traceDocFilterGroup.setTelemetryType(TelemetryType.TRACE);
        FilterConjunctionGroupInfo traceGroup = new FilterConjunctionGroupInfo();
        traceGroup.setFilters(new ArrayList<>());
        traceDocFilterGroup.setFilters(traceGroup);
        docFilterGroups.add(traceDocFilterGroup);

        DocumentStreamInfo documentStreamInfo = new DocumentStreamInfo();
        documentStreamInfo.setId(isDefault ? "all-types-default" : "random-stream-id");
        documentStreamInfo.setDocumentFilterGroups(docFilterGroups);

        return documentStreamInfo;
    }

    private CollectionConfigurationInfo createDerivedMetricConfig() {
        CollectionConfigurationInfo config = new CollectionConfigurationInfo();
        List<DocumentStreamInfo> documentStreams = new ArrayList<>();
        DocumentStreamInfo defaultStream = createDocumentStream(true);
        documentStreams.add(defaultStream);

        config.setDocumentStreams(documentStreams);
        config.setETag("random-etag");

        List<DerivedMetricInfo> metrics = new ArrayList<>();
        DerivedMetricInfo requestDuration = createRequestDurationDerivedMetricInfo();
        DerivedMetricInfo exceptionCount = createExceptionCountDerivedMetricInfo();
        metrics.add(requestDuration);
        metrics.add(exceptionCount);

        config.setMetrics(metrics);

        return config;
    }

    private DerivedMetricInfo createRequestDurationDerivedMetricInfo() {
        DerivedMetricInfo dmi = new DerivedMetricInfo();
        dmi.setId("request-duration");
        dmi.setTelemetryType("Request");
        dmi.setAggregation(AggregationType.AVG);
        dmi.setBackEndAggregation(AggregationType.AVG);
        dmi.setProjection(KnownRequestColumns.DURATION);

        FilterInfo filter = new FilterInfo();
        filter.setFieldName(KnownRequestColumns.RESPONSE_CODE);
        filter.setPredicate(PredicateType.EQUAL);
        filter.setComparand("200");
        List<FilterInfo> filters = new ArrayList<>();
        filters.add(filter);

        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        filterGroup.setFilters(filters);

        List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>();
        filterGroups.add(filterGroup);
        dmi.setFilterGroups(filterGroups);

        return dmi;
    }

    private DerivedMetricInfo createExceptionCountDerivedMetricInfo() {
        DerivedMetricInfo dmi = new DerivedMetricInfo();
        dmi.setId("exception-count");
        dmi.setTelemetryType("Exception");
        dmi.setAggregation(AggregationType.SUM);
        dmi.setBackEndAggregation(AggregationType.SUM);
        dmi.setProjection(DerivedMetricProjections.COUNT);

        FilterInfo filter = new FilterInfo();
        filter.setFieldName(KnownExceptionColumns.MESSAGE);
        filter.setPredicate(PredicateType.CONTAINS);
        filter.setComparand("hi");
        List<FilterInfo> filters = new ArrayList<>();
        filters.add(filter);

        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        filterGroup.setFilters(filters);

        List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>();
        filterGroups.add(filterGroup);
        dmi.setFilterGroups(filterGroups);

        return dmi;
    }
}
