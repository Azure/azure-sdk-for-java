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

package com.azure.monitor.opentelemetry.exporter.implementation.configuration;

import io.opentelemetry.instrumentation.api.internal.cache.Cache;

import java.net.URL;

public final class ConnectionString {

  private static final Cache<String, ConnectionString> cache = Cache.bounded(100);

  private final String instrumentationKey;
  private final String ingestionEndpoint;
  private final URL liveEndpoint;
  private final URL profilerEndpoint;

  private final String originalString;

  ConnectionString(
      String instrumentationKey,
      URL ingestionEndpoint,
      URL liveEndpoint,
      URL profilerEndpoint,
      String originalString) {
    this.instrumentationKey = instrumentationKey;
    this.ingestionEndpoint = ingestionEndpoint.toExternalForm();
    this.liveEndpoint = liveEndpoint;
    this.profilerEndpoint = profilerEndpoint;
    this.originalString = originalString;
  }

  public static ConnectionString parse(String connectionString) {
    return cache.computeIfAbsent(
        connectionString, key -> new ConnectionStringBuilder().setConnectionString(key).build());
  }

  public String getInstrumentationKey() {
    return instrumentationKey;
  }

  public String getIngestionEndpoint() {
    return ingestionEndpoint;
  }

  public URL getLiveEndpoint() {
    return liveEndpoint;
  }

  public URL getProfilerEndpoint() {
    return profilerEndpoint;
  }

  public String getOriginalString() {
    return originalString;
  }
}
