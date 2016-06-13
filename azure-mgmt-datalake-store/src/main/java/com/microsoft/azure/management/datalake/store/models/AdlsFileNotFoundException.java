/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A WebHDFS exception thrown indicating the file or folder could not be
 * found. Thrown when a 404 error response code is returned (not found).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "exception")
@JsonTypeName("FileNotFoundException")
public class AdlsFileNotFoundException extends AdlsRemoteException {
}
