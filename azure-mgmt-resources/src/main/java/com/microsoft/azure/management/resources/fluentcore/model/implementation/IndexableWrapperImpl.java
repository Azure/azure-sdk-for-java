/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

/**
 * Base wrapper implementation
 * @param <InnerT> wrapped type
 */
public abstract class IndexableWrapperImpl<InnerT> extends IndexableImpl {
	private InnerT innerObject; 
	protected IndexableWrapperImpl(String name, InnerT innerObject) {
		super(name);
		this.innerObject = innerObject;
	}
	
	/**
	 * @return wrapped inner object providing direct access to the underlying 
	 * auto-generated API implementation, based on Azure REST API
	 */
	public InnerT inner() {
		return this.innerObject;
	}
}
