/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableImpl;

public abstract class IndexableRefreshableImpl<T> 
	extends IndexableImpl
	implements Refreshable<T> {

	protected IndexableRefreshableImpl(String name) {
		super(name);
	}

	public abstract T refresh() throws Exception;
}
