// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.IsSubscribedHeaders;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.Strings;
import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;
import static com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_SEND_ERROR;

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
        dataFetcher.prepareQuickPulseDataForSend();

        QuickPulseStatus qpStatus = dataSender.getQuickPulseStatus();
        collector.setQuickPulseStatus(qpStatus);
        switch (qpStatus) {
            case ERROR:
                pingMode = true;
                // Below two lines are necessary because there is a case where the last valid request is a post
                // that came 20s before a failing ping, such as a network related error. In this case, if the
                // network error continues, we would want pings to remain in backoff mode instead of pinging every
                // 5 seconds after the initial failed ping. This adjusts the lastTransmissionTime saved in the pingSender such that the pingSender remains
                // in an error state (ping once a minute) if the first ping after the failing post also fails.
                long errorDelayInNs = TimeUnit.SECONDS.toNanos(40);
                pingSender.resetLastValidRequestTimeNs(dataSender.getLastValidPostRequestTimeNs() - errorDelayInNs);
                logger.verbose("Switching to fallback mode.");
                return waitOnErrorInMillis;

            case QP_IS_OFF:
                pingMode = true;
                // Below line is necessary because there is a case where the last valid request is a post
                // before a failing ping due to network related error. Without this line, the ping sender
                // would assume that the last valid request was from before the previous posts, which would cause the ping
                // sender to go into backoff state immediately instead of waiting 60s to go into backoff state like
                // the spec describes. See: https://github.com/aep-health-and-standards/Telemetry-Collection-Spec/blob/main/ApplicationInsights/livemetrics.md#timings
                pingSender.resetLastValidRequestTimeNs(dataSender.getLastValidPostRequestTimeNs());
                logger.verbose("Switching to ping mode.");
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
        IsSubscribedHeaders pingResult = pingSender.ping(qpsServiceRedirectedEndpoint);
        QuickPulseStatus qpStatus = this.handleReceivedPingHeaders(pingResult);
        collector.setQuickPulseStatus(qpStatus);
        switch (qpStatus) {
            case ERROR:
                logger.verbose("In fallback mode");
                return waitOnErrorInMillis;

            case QP_IS_ON:
                pingMode = false;
                logger.verbose("Switching to post mode");
                // Below two lines are necessary because there are cases where the last valid request is a ping
                // before a failing post. This can happen in cases where authentication fails - pings would return
                // http 200 but posts http 401.
                long lastValidRequestTransmission = pingSender.getLastValidPingTransmissionNs();
                dataSender.resetLastValidRequestTimeNs(lastValidRequestTransmission);

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

    private QuickPulseStatus handleReceivedPingHeaders(IsSubscribedHeaders pingHeaders) {
        String redirectLink = pingHeaders.getXMsQpsServiceEndpointRedirectV2();
        if (!Strings.isNullOrEmpty(redirectLink)) {
            try {
                URL redirectUrl = new URL(redirectLink);
                // Taking the QuickPulseService.svc part out if present because the swagger will add that on.
                qpsServiceRedirectedEndpoint = redirectUrl.getProtocol() + "://" + redirectUrl.getHost() + "/";
                logger.verbose("Handling ping header to redirect to {}", qpsServiceRedirectedEndpoint);
                dataSender.setRedirectEndpointPrefix(qpsServiceRedirectedEndpoint);
            } catch (MalformedURLException e) {
                logger.error("The service returned a malformed URL in the redirect header: {}. Exception message: {}",
                    redirectLink, e.getMessage());
            }
        }

        String pollingIntervalHeader = pingHeaders.getXMsQpsServicePollingIntervalHint();
        if (!Strings.isNullOrEmpty(pollingIntervalHeader)) {
            long newPollingInterval = Long.getLong(pingHeaders.getXMsQpsServicePollingIntervalHint());
            if (newPollingInterval > 0) {
                qpsServicePollingIntervalHintMillis = newPollingInterval;
            }
        }

        return getQuickPulseStatusFromHeader(pingHeaders.getXMsQpsSubscribed());
    }

    private QuickPulseStatus getQuickPulseStatusFromHeader(String headerValue) {
        if (Strings.isNullOrEmpty(headerValue)) {
            return QuickPulseStatus.ERROR;
        } else if ("true".equalsIgnoreCase(headerValue)) {
            return QuickPulseStatus.QP_IS_ON;
        } else {
            return QuickPulseStatus.QP_IS_OFF;
        }
    }

    void stop() {
        stopped = true;
    }
}
