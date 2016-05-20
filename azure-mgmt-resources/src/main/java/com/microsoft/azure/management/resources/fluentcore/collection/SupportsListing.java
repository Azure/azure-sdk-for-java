/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;

import java.io.IOException;
import java.util.List;

// Requires class to support listing entities
public interface SupportsListing<T> {
	PagedList<T> list() throws CloudException, IOException;
}
