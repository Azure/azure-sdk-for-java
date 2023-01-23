// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import com.azure.core.management.exception.{ManagementError, ManagementException}
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils

private object ManagementExceptions {
    def isNotFoundException(throwable: Throwable): Boolean = {
        throwable match {
            case managementException: ManagementException =>
                isNotFoundExceptionCore(managementException.getValue)
            case _ => false
        }
    }

    def isBadRequestException(throwable: Throwable): Boolean = {
        throwable match {
            case managementException: ManagementException =>
                isBadRequestExceptionCore(managementException.getValue)
            case _ => false
        }
    }

    def isNotFoundExceptionCore(managementError: ManagementError): Boolean = {
        managementError != null && StringUtils.equalsIgnoreCase(managementError.getCode, "NotFound")
    }

    def isBadRequestExceptionCore(managementError: ManagementError): Boolean = {
        managementError != null && StringUtils.equalsIgnoreCase(managementError.getCode, "BadRequest")
    }
}
