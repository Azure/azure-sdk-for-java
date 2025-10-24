// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.data.LinkData
import io.opentelemetry.sdk.trace.samplers.{Sampler, SamplingDecision, SamplingResult}

import java.util
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

private[spark] class CosmosMaxCountPerIntervalSpanHeadSampler(
                                        val maxSpanCount: Int,
                                        val samplingIntervalInSeconds: Int,
                                        val samplingRate: Float
                                      ) extends Sampler with BasicLoggingTrait{

  require(maxSpanCount >= 0, "Parameter 'maxSpanCount' must be >= 0")
  require(samplingIntervalInSeconds > 0, "Parameter 'samplingIntervalInSeconds' must be at least 1.")
  require(samplingRate >= 0.0f && samplingRate <= 1.0f, "Parameter 'samplingRate' must be between [0.0, 1.0]")

  private val sampledSpanCountInInterval: AtomicInteger = new AtomicInteger(0)
  private val nextResetTimestamp: AtomicLong = new AtomicLong(System.currentTimeMillis() + samplingIntervalInSeconds * 1000)
  private val rateLimitedSampler : Sampler = Sampler.traceIdRatioBased(samplingRate)

  logInfo(s"Initialized - $getDescription")

  override def shouldSample(
                             parentContext: Context,
                             traceId: String,
                             name: String,
                             spanKind: SpanKind,
                             attributes: Attributes,
                             parentLinks: util.List[LinkData]): SamplingResult = {

    val rateLimitedSamplingResult = rateLimitedSampler.shouldSample(
      parentContext,
      traceId,
      name,
      spanKind,
      attributes,
      parentLinks
    )

    if (rateLimitedSamplingResult.getDecision == SamplingDecision.RECORD_AND_SAMPLE) {
      val previousSpanCount = sampledSpanCountInInterval.getAndIncrement
      if (previousSpanCount <= maxSpanCount) {
        SamplingResult.recordAndSample()
      } else {
        val nowSnapshot = System.currentTimeMillis()
        val nextResetSnapshot = nextResetTimestamp.get()
        if (nowSnapshot > nextResetSnapshot) {
          nextResetTimestamp.set(nowSnapshot + samplingIntervalInSeconds * 1000)
          sampledSpanCountInInterval.set(0)

          SamplingResult.recordAndSample()
        } else {
          if (previousSpanCount == maxSpanCount + 1) {
            logInfo(s"Already sampled.in $maxSpanCount spans - always recording only until sampling interval is reset at $nextResetSnapshot.")
          }
          SamplingResult.recordOnly()
        }
      }
    } else {
      rateLimitedSamplingResult
    }
  }

  override def getDescription: String = rateLimitedSampler.getDescription +
    s"<=$maxSpanCount/$samplingIntervalInSeconds s"
}
