/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A WebHDFS exception thrown indicating that access is denied due to
 * insufficient permissions. Thrown when a 403 error response code is
 * returned (forbidden).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "exception")
@JsonTypeName("AccessControlException")
public class AdlsAccessControlException extends AdlsRemoteException {
}
