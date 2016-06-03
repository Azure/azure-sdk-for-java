/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A WebHDFS exception thrown when an unexpected error occurs during an
 * operation. Thrown when a 500 error response code is returned (Internal
 * server error).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "exception")
@JsonTypeName("RuntimeException")
public class AdlsRuntimeException extends AdlsRemoteException {
}
