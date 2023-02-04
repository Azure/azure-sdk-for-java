// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import com.azure.core.management.exception.ManagementException
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils

private[spark] object ManagementExceptions {
    private val notFoundCode = "NotFound"
    private val badRequestCode = "BadRequest"

    def isNotFoundException(throwable: Throwable): Boolean = {
        throwable match {
            case managementException: ManagementException =>
                StringUtils.equalsIgnoreCase(managementException.getValue.getCode, notFoundCode)
            case _ => false
        }
    }

    def isBadRequestException(throwable: Throwable): Boolean = {
        throwable match {
            case managementException: ManagementException =>
                StringUtils.equalsIgnoreCase(managementException.getValue.getCode, badRequestCode)
            case _ => false
        }
    }
}
