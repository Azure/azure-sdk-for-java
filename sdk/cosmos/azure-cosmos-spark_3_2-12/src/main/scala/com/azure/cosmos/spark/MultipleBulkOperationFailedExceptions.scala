package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.spark.BulkWriter.BulkOperationFailedException

class MultipleBulkOperationFailedExceptions(exceptions: List[Throwable]) extends
    CosmosException(0, "Multiple bulk operation failures") {
    def getExceptions: List[Throwable] = exceptions

    override def getMessage: String = {
        val exceptionMessages = exceptions.toArray.map(_.asInstanceOf[BulkOperationFailedException].getMessage)
        s"Multiple bulk operation failed: ${exceptionMessages.mkString(", ")}"
    }
}
