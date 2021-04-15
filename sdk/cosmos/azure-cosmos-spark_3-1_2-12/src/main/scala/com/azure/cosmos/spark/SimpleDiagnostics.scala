// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.{OperationContext, OperationListener}
import com.azure.cosmos.implementation.{HttpConstants, RxDocumentServiceRequest, RxDocumentServiceResponse}

// scalastyle:off multiple.string.literals
private[spark] class SimpleDiagnostics extends OperationListener with CosmosLoggingTrait {
  override def requestListener(context: OperationContext, request: RxDocumentServiceRequest): Unit = {
    logInfo(s"${context.toString}, request: ${toString(request)}")
  }

  override def responseListener(context: OperationContext, response: RxDocumentServiceResponse): Unit = {
    logInfo(s"${context.toString}, response: ${toString(response)}")
  }

  override def exceptionListener(context: OperationContext, exception: Throwable): Unit = {
    logInfo(s"${context.toString}, response: ${exception.getMessage}")
  }

  private def toString(request: RxDocumentServiceRequest): String = {
    val sb = new StringBuilder()
    sb.append("request{")
    sb.append("operationType:").append(request.getOperationType)
    sb.append(", resourceType:").append(request.getResourceType)
    sb.append(", partitionKeyRangeIdentity:").append(request.getPartitionKeyRangeIdentity)
    sb.append(", partitionKeyInternal:").append(request.getPartitionKeyInternal)
    sb.append(", feedRange:").append(request.getFeedRange)
    sb.append(", effectiveRange:").append(request.getEffectiveRange)
    sb.append(", resourceAddress:").append(request.getResourceAddress)
    sb.append(", ")
    requestHeadersDump(sb, request.getHeaders)
    sb.append("}")
    sb.toString()
  }

  private def toString(response: RxDocumentServiceResponse): String = {
    val sb = new StringBuilder()
    sb.append("response{")
    sb.append("statusCode:").append(response.getStatusCode)
    sb.append(", ")
    responseHeadersDump(sb, response.getResponseHeaders)
    sb.append("}")

    sb.toString()
  }

  private def requestHeadersDump(sb: StringBuilder, headers: java.util.Map[String, String]): Unit = {
    sb.append("headers{")
    sb.append("continuationToken:").append(headers.get(HttpConstants.HttpHeaders.CONTINUATION))
    sb.append(", activityId:").append(headers.get(HttpConstants.HttpHeaders.ACTIVITY_ID))
    sb.append(", correlationActivityId:").append(headers.get(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID))
    sb.append("}")
  }

  private def responseHeadersDump(sb: StringBuilder, headers: java.util.Map[String, String]): Unit = {
    sb.append("headers{")
    sb.append("continuationToken:").append(headers.get(HttpConstants.HttpHeaders.CONTINUATION))
    sb.append(", activityId:").append(headers.get(HttpConstants.HttpHeaders.ACTIVITY_ID))
    sb.append(", itemCount:").append(headers.get(HttpConstants.HttpHeaders.ITEM_COUNT))
    sb.append("}")
  }
}
// scalastyle:on multiple.string.literals
