// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_SEND_ERROR;

final class QuickPulseCoordinator implements Runnable {

    private static final ClientLogger logger = new ClientLogger(QuickPulseCoordinator.class);

    @Nullable
    private String qpsServiceRedirectedEndpoint;
    private long qpsServicePollingIntervalHintMillis;

    private volatile boolean stopped = false;
    private volatile boolean pingMode = true;

    private final QuickPulseDataCollector collector;
    private final QuickPulsePingSender pingSender;
    private final QuickPulseDataFetcher dataFetcher;
    private final QuickPulseDataSender dataSender;

    private final long waitBetweenPingsInMillis;
    private final long waitBetweenPostsInMillis;
    private final long waitOnErrorInMillis;

    QuickPulseCoordinator(QuickPulseCoordinatorInitData initData) {
        dataSender = initData.dataSender;
        pingSender = initData.pingSender;
        dataFetcher = initData.dataFetcher;
        collector = initData.collector;

        waitBetweenPingsInMillis = initData.waitBetweenPingsInMillis;
        waitBetweenPostsInMillis = initData.waitBetweenPostsInMillis;
        waitOnErrorInMillis = initData.waitOnErrorInMillis;
        qpsServiceRedirectedEndpoint = null;
        qpsServicePollingIntervalHintMillis = -1;
    }

    @Override
    public void run() {
        try {
            while (!stopped) {
                long sleepInMillis;
                if (pingMode) {
                    sleepInMillis = ping();
                } else {
                    sleepInMillis = sendData();
                }
                Thread.sleep(sleepInMillis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("QuickPulseCoordinator was interrupted", e);
        } catch (Throwable t) {
            // Not supposed to happen
            logger.error("QuickPulseCoordinator failed", t);
        }
    }
    @SuppressWarnings("try")
    private long sendData() {
        dataFetcher.prepareQuickPulseDataForSend(qpsServiceRedirectedEndpoint);
        QuickPulseHeaderInfo currentQuickPulseHeaderInfo = dataSender.getQuickPulseHeaderInfo();

        this.handleReceivedHeaders(currentQuickPulseHeaderInfo);
        collector.setQuickPulseStatus(currentQuickPulseHeaderInfo.getQuickPulseStatus());
        switch (currentQuickPulseHeaderInfo.getQuickPulseStatus()) {
            case ERROR:
                pingMode = true;
                return waitOnErrorInMillis;

            case QP_IS_OFF:
                pingMode = true;
                collector.flushOpenTelMetrics();
                return qpsServicePollingIntervalHintMillis > 0
                    ? qpsServicePollingIntervalHintMillis
                    : waitBetweenPingsInMillis;

            case QP_IS_ON:
                return waitBetweenPostsInMillis;
        }

        try (MDC.MDCCloseable ignored = QUICK_PULSE_SEND_ERROR.makeActive()) {
            logger.error("Critical error while sending QP data: unknown status, aborting");
        }
        collector.disable();
        stopped = true;
        return 0;
    }

    @SuppressWarnings("try")
    private long ping() {
        QuickPulseHeaderInfo pingResult = pingSender.ping(qpsServiceRedirectedEndpoint);
        this.handleReceivedHeaders(pingResult);
        collector.setQuickPulseStatus(pingResult.getQuickPulseStatus());
        switch (pingResult.getQuickPulseStatus()) {
            case ERROR:
                return waitOnErrorInMillis;

            case QP_IS_ON:
                pingMode = false;
                dataSender.startSending();
                return waitBetweenPostsInMillis;
            case QP_IS_OFF:
                return qpsServicePollingIntervalHintMillis > 0
                    ? qpsServicePollingIntervalHintMillis
                    : waitBetweenPingsInMillis;
        }

        try (MDC.MDCCloseable ignored = QUICK_PULSE_PING_ERROR.makeActive()) {
            logger.error("Critical error while ping QP: unknown status, aborting");
        }
        collector.disable();
        stopped = true;
        return 0;
    }

    private void handleReceivedHeaders(QuickPulseHeaderInfo currentQuickPulseHeaderInfo) {
        String redirectLink = currentQuickPulseHeaderInfo.getQpsServiceEndpointRedirect();
        if (!Strings.isNullOrEmpty(redirectLink)) {
            qpsServiceRedirectedEndpoint = redirectLink;
        }

        long newPollingInterval = currentQuickPulseHeaderInfo.getQpsServicePollingInterval();
        if (newPollingInterval > 0) {
            qpsServicePollingIntervalHintMillis = newPollingInterval;
        }
    }

    void stop() {
        stopped = true;
    }
}
