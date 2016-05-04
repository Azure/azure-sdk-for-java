/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.CloudException;

/**
 * An exception that we want our mocks to throw sometimes to test out various code paths.
 */
public class IntentionalException extends CloudException { }