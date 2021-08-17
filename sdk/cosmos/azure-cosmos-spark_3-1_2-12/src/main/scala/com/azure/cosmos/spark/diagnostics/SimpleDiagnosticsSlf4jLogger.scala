// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.routing.PartitionKeyInternal
import com.azure.cosmos.implementation.spark.OperationContext
import com.azure.cosmos.implementation.{HttpConstants, OperationType, RxDocumentServiceRequest, RxDocumentServiceResponse}

// scalastyle:off multiple.string.literals

private[spark] final class SimpleDiagnosticsSlf4jLogger(classType: Class[_])
  extends DefaultMinimalSlf4jLogger(classType: Class[_]) {

  override def logItemWriteCompletion(writeOperation: WriteOperation): Unit = {
    logInfo(s"$writeOperation completed")
  }

  override def logItemWriteSkipped(writeOperation: WriteOperation, detail: => String): Unit = {
    logInfo(s"$writeOperation skipped, $detail")
  }

  override def logItemWriteFailure(writeOperation: WriteOperation): Unit = {
    logInfo(s"$writeOperation failed")
  }

  override def logItemWriteFailure(writeOperation: WriteOperation, throwable: Throwable): Unit = {
    logInfo(s"$writeOperation failed", throwable)
  }

  override def logItemWriteDetails(writeOperation: WriteOperation, detail: => String): Unit = {
    logInfo(s"$writeOperation $detail")
  }

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
    sb.append(", partitionKey:").append(safePartitionKeyInternalToJson(request.getPartitionKeyInternal))
    sb.append(", resourceAddress:").append(request.getResourceAddress)
    sb.append(", ")
    requestHeadersDump(sb, request)
    sb.append("}")
    sb.toString()
  }

  private def safePartitionKeyInternalToJson(partitionKeyInternal: PartitionKeyInternal) = {
    if (partitionKeyInternal == null) "null" else partitionKeyInternal.toJson
  }

  private def toString(response: RxDocumentServiceResponse): String = {
    val sb = new StringBuilder()
    sb.append("response{")
    sb.append("statusCode:").append(response.getStatusCode)
    sb.append(", ")
    responseHeadersDump(sb, response)
    sb.append("}")

    sb.toString()
  }

  private def requestHeadersDump(sb: StringBuilder, req: RxDocumentServiceRequest): Unit = {
    val headers = req.getHeaders
    sb.append("headers{")
    sb.append("activityId:").append(req.getActivityId)
    if (req.isReadOnlyRequest) {
      sb.append(", continuationToken:").append(headers.get(HttpConstants.HttpHeaders.CONTINUATION))
      sb.append(", correlationActivityId:").append(headers.get(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID))
    }

    if (req.getOperationType == OperationType.Batch) {
      sb.append(", items:").append(req.getNumberOfItemsInBatchRequest);
    }
    sb.append("}")
  }

  private def responseHeadersDump(sb: StringBuilder, resp: RxDocumentServiceResponse): Unit = {
    val headers = resp.getResponseHeaders
    sb.append("headers{")
    sb.append("activityId:").append(headers.get(HttpConstants.HttpHeaders.ACTIVITY_ID))

    val itemCount = headers.get(HttpConstants.HttpHeaders.ITEM_COUNT)
    if (itemCount != null) {
      sb.append(", itemCount:").append(itemCount)
      sb.append(", continuationToken:").append(headers.get(HttpConstants.HttpHeaders.CONTINUATION))
    }
    sb.append("}")
  }
}
// // scalastyle:on multiple.string.literals
