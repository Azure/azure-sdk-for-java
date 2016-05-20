/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import java.util.List;

// Requires class to support listing entities
public interface SupportsListingNames {
	List<String> names() throws Exception;
}
