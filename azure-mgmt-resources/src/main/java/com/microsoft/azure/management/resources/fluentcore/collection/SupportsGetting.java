/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.CloudException;

import java.io.IOException;

// Requires class to support reading entities
public interface SupportsGetting<T> {
	T get(String name) throws CloudException, IOException;
}
