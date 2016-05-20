/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

public abstract class IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT> 
	extends IndexableRefreshableImpl<FluentModelT> {

	private InnerModelT innerObject;
	protected IndexableRefreshableWrapperImpl(String name, InnerModelT innerObject) {
		super(name);
		this.innerObject = innerObject;
	}
	
	public InnerModelT inner() {
		return this.innerObject;
	}
	
	protected void setInner(InnerModelT inner) {
		this.innerObject = inner;
	}
}
