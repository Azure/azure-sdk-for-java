// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.ExceptionDetailBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuickPulseIntegrationTests extends QuickPulseTestBase {
    private static final ConnectionString connectionString =
        ConnectionString.parse("InstrumentationKey=ikey123");
    private static final String instrumentationKey = "ikey123";


    private QuickPulsePingSender getQuickPulsePingSender(QuickPulseConfiguration quickPulseConfiguration) {
        return new QuickPulsePingSender(
            getHttpPipeline(),
            connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey,
            null,
            "instance1",
            "machine1",
            "qpid123",
            "testSdkVersion",
            quickPulseConfiguration);
    }

    private QuickPulsePingSender getQuickPulsePingSenderWithAuthentication(QuickPulseConfiguration quickPulseConfiguration) {
        return new QuickPulsePingSender(
            getHttpPipelineWithAuthentication(),
            connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey,
            null,
            "instance1",
            "machine1",
            "qpid123",
            "testSdkVersion",
            quickPulseConfiguration);
    }

    private QuickPulsePingSender getQuickPulsePingSenderWithValidator(HttpPipelinePolicy validator, QuickPulseConfiguration quickPulseConfiguration) {
        return new QuickPulsePingSender(
            getHttpPipeline(validator),
            connectionString::getLiveEndpoint,
            connectionString::getInstrumentationKey,
            null,
            "instance1",
            "machine1",
            "qpid123",
            "testSdkVersion",
            quickPulseConfiguration);
    }

    @Disabled
    @Test
    public void testPing() {
        QuickPulsePingSender quickPulsePingSender = getQuickPulsePingSender(new QuickPulseConfiguration());
        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(quickPulseHeaderInfo.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_ON);
    }

    @Disabled
    @Test
    public void testPingWithAuthentication() {
        QuickPulsePingSender quickPulsePingSender = getQuickPulsePingSenderWithAuthentication(new QuickPulseConfiguration());
        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(quickPulseHeaderInfo.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_ON);
    }

    @Disabled
    @Test
    public void testPingRequestBody() throws InterruptedException {
        CountDownLatch pingCountDown = new CountDownLatch(1);
        String expectedRequestBody =
            "\\{\"Documents\":null,\"InstrumentationKey\":null,\"Metrics\":null,\"InvariantVersion\":6,\"Timestamp\":\"\\\\/Date\\(\\d+\\)\\\\/\",\"Version\":\"testSdkVersion\",\"StreamId\":\"qpid123\",\"MachineName\":\"machine1\",\"Instance\":\"instance1\",\"RoleName\":null\\}";
        QuickPulsePingSender quickPulsePingSender =
            getQuickPulsePingSenderWithValidator(
                new ValidationPolicy(pingCountDown, expectedRequestBody), new QuickPulseConfiguration());
        QuickPulseHeaderInfo quickPulseHeaderInfo = quickPulsePingSender.ping(null);
        assertThat(quickPulseHeaderInfo.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_ON);
        assertTrue(pingCountDown.await(60, TimeUnit.SECONDS));
    }

    @Disabled
    @Test
    public void testPostRequest() throws InterruptedException {
        ArrayBlockingQueue<HttpRequest> sendQueue = new ArrayBlockingQueue<>(256, true);
        CountDownLatch pingCountDown = new CountDownLatch(1);
        CountDownLatch postCountDown = new CountDownLatch(1);
        Date currDate = new Date();
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();
        String expectedPingRequestBody =
            "\\{\"Documents\":null,\"InstrumentationKey\":null,\"Metrics\":null,\"InvariantVersion\":6,\"Timestamp\":\"\\\\/Date\\(\\d+\\)\\\\/\",\"Version\":\"testSdkVersion\",\"StreamId\":\"qpid123\",\"MachineName\":\"machine1\",\"Instance\":\"instance1\",\"RoleName\":null\\}";
        String expectedPostRequestBody =
            "\\[\\{\"Documents\":\\[\\{\"__type\":\"RequestTelemetryDocument\",\"DocumentType\":\"Request\",\"Version\":\"1.0\",\"OperationId\":null,\"Properties\":\\{\"customProperty\":\"customValue\"\\},\"Name\":\"request-test\",\"Success\":true,\"Duration\":\"PT.*S\",\"ResponseCode\":\"200\",\"OperationName\":null,\"Url\":\"foo\"\\},\\{\"__type\":\"DependencyTelemetryDocument\",\"DocumentType\":\"RemoteDependency\",\"Version\":\"1.0\",\"OperationId\":null,\"Properties\":\\{\"customProperty\":\"customValue\"\\},\"Name\":\"dep-test\",\"Target\":null,\"Success\":true,\"Duration\":\"PT.*S\",\"ResultCode\":null,\"CommandName\":\"dep-test-cmd\",\"DependencyTypeName\":null,\"OperationName\":null\\},\\{\"__type\":\"ExceptionTelemetryDocument\",\"DocumentType\":\"Exception\",\"Version\":\"1.0\",\"OperationId\":null,\"Properties\":null,\"Exception\":\"\",\"ExceptionMessage\":\"test\",\"ExceptionType\":\"java.lang.Exception\"\\}\\],\"InstrumentationKey\":\""
                + instrumentationKey
                + "\",\"Metrics\":\\[\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Requests\\\\\\/Sec\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Request Duration\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Requests Failed\\\\\\/Sec\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Requests Succeeded\\\\\\/Sec\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Dependency Calls\\\\\\/Sec\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Dependency Call Duration\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Dependency Calls Failed\\\\\\/Sec\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Dependency Calls Succeeded\\\\\\/Sec\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\ApplicationInsights\\\\\\\\Exceptions\\\\\\/Sec\",\"Value\":[0-9.]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\Memory\\\\\\\\Committed Bytes\",\"Value\":[0-9.E]+,\"Weight\":\\d+\\},\\{\"Name\":\"\\\\\\\\Processor\\(_Total\\)\\\\\\\\% Processor Time\",\"Value\":-?[0-9.]+,\"Weight\":\\d+\\}\\],\"InvariantVersion\":1,\"Timestamp\":\"\\\\\\/Date\\(\\d+\\)\\\\\\/\",\"Version\":\"[^\"]*\",\"StreamId\":null,\"MachineName\":\"machine1\",\"Instance\":\"instance1\",\"RoleName\":null\\}\\]";
    QuickPulsePingSender pingSender =
        getQuickPulsePingSenderWithValidator(
            new ValidationPolicy(pingCountDown, expectedPingRequestBody),
            new QuickPulseConfiguration());
        QuickPulseHeaderInfo quickPulseHeaderInfo = pingSender.ping(null);
        QuickPulseDataSender dataSender =
            new QuickPulseDataSender(
                getHttpPipeline(new ValidationPolicy(postCountDown, expectedPostRequestBody)),
                sendQueue,
                quickPulseConfiguration);
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, quickPulseConfiguration);
        QuickPulseDataFetcher dataFetcher =
            new QuickPulseDataFetcher(
                collector,
                sendQueue,
                connectionString::getLiveEndpoint,
                connectionString::getInstrumentationKey,
                null,
                "instance1",
                "machine1",
                null,
                quickPulseConfiguration);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(connectionString::getInstrumentationKey);
        long duration = 112233L;
        // Request Telemetry
        TelemetryItem requestTelemetry =
            createRequestTelemetry("request-test", currDate, duration, "200", true);
        requestTelemetry.setConnectionString(connectionString);
        collector.add(requestTelemetry);
        // Dependency Telemetry
        TelemetryItem dependencyTelemetry =
            createRemoteDependencyTelemetry("dep-test", "dep-test-cmd", duration, true);
        dependencyTelemetry.setConnectionString(connectionString);
        collector.add(dependencyTelemetry);
        // Exception Telemetry
        ExceptionTelemetryBuilder builder = ExceptionTelemetryBuilder.create();
        ExceptionDetailBuilder detailBuilder = new ExceptionDetailBuilder();
        detailBuilder.setMessage("test");
        detailBuilder.setTypeName(Exception.class.getName());
        builder.setExceptions(singletonList(detailBuilder));
        TelemetryItem exceptionTelemetry = builder.build();
        exceptionTelemetry.setConnectionString(connectionString);
        collector.add(exceptionTelemetry);

        QuickPulseCoordinatorInitData initData =
            new QuickPulseCoordinatorInitDataBuilder()
                .withDataFetcher(dataFetcher)
                .withDataSender(dataSender)
                .withPingSender(pingSender)
                .withCollector(collector)
                .withWaitBetweenPingsInMillis(10L)
                .withWaitBetweenPostsInMillis(10L)
                .withWaitOnErrorInMillis(10L)
                .build();
        QuickPulseCoordinator coordinator = new QuickPulseCoordinator(initData);

        Thread coordinatorThread = new Thread(coordinator, QuickPulseCoordinator.class.getSimpleName());
        coordinatorThread.setDaemon(true);
        coordinatorThread.start();

        Thread senderThread = new Thread(dataSender, QuickPulseDataSender.class.getSimpleName());
        senderThread.setDaemon(true);
        senderThread.start();
        Thread.sleep(50);
        assertTrue(pingCountDown.await(5, TimeUnit.SECONDS));
        assertThat(quickPulseHeaderInfo.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_ON);
        assertThat(collector.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_ON);
        assertTrue(postCountDown.await(5, TimeUnit.SECONDS));
        senderThread.interrupt();
        coordinatorThread.interrupt();
    }
}
