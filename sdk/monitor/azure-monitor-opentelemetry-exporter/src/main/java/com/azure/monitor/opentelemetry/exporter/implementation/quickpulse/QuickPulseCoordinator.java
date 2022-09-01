/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nullable;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_SEND_ERROR;

final class QuickPulseCoordinator implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(QuickPulseCoordinator.class);

  @Nullable private String qpsServiceRedirectedEndpoint;
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
    waitOnErrorInMillis = initData.waitBetweenPingsInMillis;
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
    } catch (ThreadDeath td) {
      throw td;
    } catch (Throwable t) {
      // chomp
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
