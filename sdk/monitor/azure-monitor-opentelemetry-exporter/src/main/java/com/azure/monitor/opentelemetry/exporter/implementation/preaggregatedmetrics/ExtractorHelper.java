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

package com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;

import javax.annotation.Nullable;
import java.util.Map;

public final class ExtractorHelper {

  // visible for testing
  public static final String MS_METRIC_ID = "_MS.MetricId";
  public static final String MS_IS_AUTOCOLLECTED = "_MS.IsAutocollected";
  public static final String TRUE = "True";
  public static final String FALSE = "False";
  public static final String OPERATION_SYNTHETIC = "operation/synthetic";
  public static final String CLOUD_ROLE_NAME = "cloud/roleName";
  public static final String CLOUD_ROLE_INSTANCE = "cloud/roleInstance";

  static void extractCommon(MetricTelemetryBuilder metricBuilder, @Nullable Boolean isSynthetic) {
    metricBuilder.addProperty(MS_IS_AUTOCOLLECTED, TRUE);
    Map<String, String> tags = metricBuilder.build().getTags();
    if (tags != null) {
      String cloudName = tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString());
      if (cloudName != null && !cloudName.isEmpty()) {
        metricBuilder.addProperty(CLOUD_ROLE_NAME, cloudName);
      }

      String cloudRoleInstance = tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString());
      if (cloudRoleInstance != null && !cloudRoleInstance.isEmpty()) {
        metricBuilder.addProperty(CLOUD_ROLE_INSTANCE, cloudRoleInstance);
      }
    }

    metricBuilder.addProperty(
        OPERATION_SYNTHETIC, isSynthetic != null && isSynthetic ? TRUE : FALSE);
  }

  private ExtractorHelper() {}
}
