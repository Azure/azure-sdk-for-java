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

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HeartbeatTests {
  @SuppressWarnings("unchecked")
  private final Consumer<List<TelemetryItem>> telemetryItemsConsumer = mock(Consumer.class);

  @Test
  void heartBeatPayloadContainsDataByDefault() throws InterruptedException {
    // given
    HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {}, telemetryItemsConsumer);

    // some of the initialization above happens in a separate thread
    Thread.sleep(500);

    // then
    MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
    assertThat(data).isNotNull();
    assertThat(data.getProperties().size() > 0).isTrue();
  }

  @Test
  void heartBeatPayloadContainsSpecificProperties() {
    // given
    HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {}, telemetryItemsConsumer);

    // then
    assertThat(provider.addHeartBeatProperty("test", "testVal", true)).isTrue();

    MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
    assertThat(data.getProperties()).containsEntry("test", "testVal");
  }

  @Test
  void heartbeatMetricIsNonZeroWhenFailureConditionPresent() {
    // given
    HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {}, telemetryItemsConsumer);

    // then
    assertThat(provider.addHeartBeatProperty("test", "testVal", false)).isTrue();

    MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
    assertThat(data.getMetrics().get(0).getValue()).isEqualTo(1);
  }

  @Test
  void heartbeatMetricCountsForAllFailures() {
    // given
    HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {}, telemetryItemsConsumer);

    // then
    assertThat(provider.addHeartBeatProperty("test", "testVal", false)).isTrue();
    assertThat(provider.addHeartBeatProperty("test1", "testVal1", false)).isTrue();

    MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
    assertThat(data.getMetrics().get(0).getValue()).isEqualTo(2);
  }

  @SuppressWarnings("unchecked")
  @Test
  void sentHeartbeatContainsExpectedDefaultFields() throws Exception {
    HeartbeatExporter mockProvider = Mockito.mock(HeartbeatExporter.class);
    ConcurrentMap<String, String> props = new ConcurrentHashMap<>();
    Mockito.when(
            mockProvider.addHeartBeatProperty(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
        .then(
            (Answer<Boolean>)
                invocation -> {
                  props.put(
                      invocation.getArgument(0, String.class),
                      invocation.getArgument(1, String.class));
                  return true;
                });
    DefaultHeartBeatPropertyProvider defaultProvider = new DefaultHeartBeatPropertyProvider();

    HeartbeatDefaultPayload.populateDefaultPayload(mockProvider).call();
    Field field = defaultProvider.getClass().getDeclaredField("defaultFields");
    field.setAccessible(true);
    Set<String> defaultFields = (Set<String>) field.get(defaultProvider);
    for (String fieldName : defaultFields) {
      assertThat(props.containsKey(fieldName)).isTrue();
      assertThat(props.get(fieldName).length() > 0).isTrue();
    }
  }

  @Test
  void heartBeatProviderDoesNotAllowDuplicateProperties() {
    // given
    HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {}, telemetryItemsConsumer);

    // then
    provider.addHeartBeatProperty("test01", "test val", true);
    assertThat(provider.addHeartBeatProperty("test01", "test val 2", true)).isFalse();
  }

  @SuppressWarnings("unchecked")
  @Test
  void cannotAddUnknownDefaultProperty() throws Exception {
    DefaultHeartBeatPropertyProvider base = new DefaultHeartBeatPropertyProvider();
    String testKey = "testKey";

    Field field = base.getClass().getDeclaredField("defaultFields");
    field.setAccessible(true);
    Set<String> defaultFields = (Set<String>) field.get(base);
    defaultFields.add(testKey);

    HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {}, telemetryItemsConsumer);

    base.setDefaultPayload(provider).call();
    MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
    assertThat(data.getProperties().containsKey("testKey")).isFalse();
  }
}
