/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

public abstract class IndexableWrapperImpl<I> extends IndexableImpl {
	protected I innerObject; 
	protected IndexableWrapperImpl(String name, I innerObject) {
		super(name);
		this.innerObject = innerObject;
	}
	
	public I inner() {
		return this.innerObject;
	}
}
