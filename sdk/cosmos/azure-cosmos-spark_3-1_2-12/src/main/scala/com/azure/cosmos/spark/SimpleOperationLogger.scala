// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.{OperationContext, OperationListener}
import com.azure.cosmos.implementation.{HttpConstants, RxDocumentServiceRequest, RxDocumentServiceResponse}

class SimpleOperationLogger extends OperationListener with CosmosLoggingTrait {
  override def requestListener(context: OperationContext, request: RxDocumentServiceRequest): Unit = {
    logInfo(s"request: ${context.toString} ${toString(request)}")
  }

  override def responseListener(context: OperationContext, response: RxDocumentServiceResponse): Unit = {
    logInfo(s"response: ${context.toString} ${toString(response)}")
  }

  override def exceptionListener(context: OperationContext, exception: Throwable): Unit = {
    logInfo(s"response: ${context.toString} ${exception.getMessage}")
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
    headerDump(sb, request.getHeaders)
    sb.append("}")
    sb.toString()
  }

  private def toString(response: RxDocumentServiceResponse): String = {
    val sb = new StringBuilder()
    sb.append("response{")
    sb.append("statusCode:").append(response.getStatusCode)
    sb.append(", ")
    headerDump(sb, response.getResponseHeaders)
    sb.append("}")

    sb.toString()
  }

  private def headerDump(sb: StringBuilder, headers: java.util.Map[String, String]): Unit = {
    sb.append("headers{")
    sb.append("continuationToken:").append(headers.get(HttpConstants.HttpHeaders.CONTINUATION))
    sb.append(", activityId:").append(headers.get(HttpConstants.HttpHeaders.ACTIVITY_ID))
    sb.append(", subStatusCode:").append(headers.get(HttpConstants.HttpHeaders.SUB_STATUS))
    sb.append("}")
  }
}
