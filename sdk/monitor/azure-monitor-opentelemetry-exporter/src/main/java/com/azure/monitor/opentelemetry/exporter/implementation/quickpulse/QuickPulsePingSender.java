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

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.NetworkFriendlyExceptions;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseEnvelope;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.util.CustomCharacterEscapes;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.QUICK_PULSE_PING_ERROR;

class QuickPulsePingSender {

  private static final Logger logger = LoggerFactory.getLogger(QuickPulsePingSender.class);

  private static final ObjectMapper mapper;

  private static final OperationLogger operationLogger =
      new OperationLogger(QuickPulsePingSender.class, "Pinging live metrics endpoint");

  // TODO (kryalama) do we still need this AtomicBoolean, or can we use throttling built in to the
  //  operationLogger?
  private static final AtomicBoolean friendlyExceptionThrown = new AtomicBoolean();

  static {
    mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.getFactory().setCharacterEscapes(new CustomCharacterEscapes());
  }

  private final HttpPipeline httpPipeline;
  private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
  private volatile QuickPulseEnvelope pingEnvelope; // cached for performance

  private final Supplier<URL> endpointUrl;
  private final Supplier<String> instrumentationKey;
  private final String roleName;
  private final String instanceName;
  private final String machineName;
  private final String quickPulseId;
  private long lastValidTransmission = 0;
  private final String sdkVersion;

  QuickPulsePingSender(
      HttpPipeline httpPipeline,
      Supplier<URL> endpointUrl,
      Supplier<String> instrumentationKey,
      String roleName,
      String instanceName,
      String machineName,
      String quickPulseId,
      String sdkVersion) {
    this.httpPipeline = httpPipeline;
    this.endpointUrl = endpointUrl;
    this.instrumentationKey = instrumentationKey;
    this.roleName = roleName;
    this.instanceName = instanceName;
    this.machineName = machineName;
    this.quickPulseId = quickPulseId;
    this.sdkVersion = sdkVersion;
    if (logger.isTraceEnabled()) {
      logger.trace(
          "{} using endpoint {}",
          QuickPulsePingSender.class.getSimpleName(),
          getQuickPulseEndpoint());
    }
  }

  QuickPulseHeaderInfo ping(String redirectedEndpoint) {
    String instrumentationKey = getInstrumentationKey();
    if (Strings.isNullOrEmpty(instrumentationKey)) {
      // Quick Pulse Ping uri will be null when the instrumentation key is null. When that happens,
      // turn off quick pulse.
      return new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF);
    }

    Date currentDate = new Date();
    String endpointPrefix =
        Strings.isNullOrEmpty(redirectedEndpoint) ? getQuickPulseEndpoint() : redirectedEndpoint;
    HttpRequest request =
        networkHelper.buildPingRequest(
            currentDate,
            getQuickPulsePingUri(endpointPrefix),
            quickPulseId,
            machineName,
            roleName,
            instanceName);

    long sendTime = System.nanoTime();
    HttpResponse response = null;
    try {
      request.setBody(buildPingEntity(currentDate.getTime()));
      response = httpPipeline.send(request).block();
      if (response == null) {
        // this shouldn't happen, the mono should complete with a response or a failure
        throw new AssertionError("http response mono returned empty");
      }

      if (networkHelper.isSuccess(response)) {
        QuickPulseHeaderInfo quickPulseHeaderInfo = networkHelper.getQuickPulseHeaderInfo(response);
        switch (quickPulseHeaderInfo.getQuickPulseStatus()) {
          case QP_IS_OFF:
          case QP_IS_ON:
            lastValidTransmission = sendTime;
            operationLogger.recordSuccess();
            return quickPulseHeaderInfo;

          default:
            break;
        }
      }
    } catch (Throwable t) {
      if (!NetworkFriendlyExceptions.logSpecialOneTimeFriendlyException(
          t, getQuickPulseEndpoint(), friendlyExceptionThrown, logger)) {
        operationLogger.recordFailure(
            t.getMessage() + " (" + endpointPrefix + ")", t, QUICK_PULSE_PING_ERROR);
      }
    } finally {
      if (response != null) {
        response.close();
      }
    }
    return onPingError(sendTime);
  }

  // visible for testing
  String getQuickPulsePingUri(String endpointPrefix) {
    return endpointPrefix + "/ping?ikey=" + getInstrumentationKey();
  }

  @Nullable
  private String getInstrumentationKey() {
    return instrumentationKey.get();
  }

  // visible for testing
  String getQuickPulseEndpoint() {
    return endpointUrl.get().toString() + "QuickPulseService.svc";
  }

  private String buildPingEntity(long timeInMillis) throws JsonProcessingException {
    if (pingEnvelope == null) {
      pingEnvelope = new QuickPulseEnvelope();
      pingEnvelope.setInstance(instanceName);
      pingEnvelope.setInvariantVersion(QuickPulse.QP_INVARIANT_VERSION);
      pingEnvelope.setMachineName(machineName);
      pingEnvelope.setRoleName(roleName);
      pingEnvelope.setStreamId(quickPulseId);
      pingEnvelope.setVersion(sdkVersion);
    }
    pingEnvelope.setTimeStamp("/Date(" + timeInMillis + ")/");
    return mapper.writeValueAsString(pingEnvelope);
  }

  private QuickPulseHeaderInfo onPingError(long sendTime) {
    double timeFromLastValidTransmission = (sendTime - lastValidTransmission) / 1000000000.0;
    if (timeFromLastValidTransmission >= 60.0) {
      return new QuickPulseHeaderInfo(QuickPulseStatus.ERROR);
    }

    return new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF);
  }
}
