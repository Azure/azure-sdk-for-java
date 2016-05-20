/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

import java.util.Map;

// Encapsulates the create() method
public interface Creatable<T> extends Indexable {
	T create() throws Exception;
	Map<String, Creatable<?>> prerequisites();
	Map<String, Creatable<?>> created();
}